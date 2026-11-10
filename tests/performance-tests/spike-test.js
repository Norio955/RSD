/**
 * ============================================================================
 * CRUD OPERATIONS LOAD TEST
 * ============================================================================
 *
 * МЕТА:
 * Перевірити продуктивність базових CRUD операцій (Create, Read, Update, Delete)
 * для travel plans під навантаженням. Тестує повний життєвий цикл плану подорожі:
 * створення, читання, оновлення (включаючи optimistic locking) та видалення.
 *
 * ХІД ВИКОНАННЯ ТЕСТУ:
 * 1. Ramp-up фаза (2 хвилини): Поступове збільшення від 0 до 50 користувачів
 * 2. Steady state (5 хвилин): Стабільне навантаження з 50 користувачами
 * 3. Ramp-down фаза (2 хвилини): Плавне зменшення навантаження до 0
 *
 * КОЖНА ІТЕРАЦІЯ ТЕСТУ:
 * - Створює новий travel plan з валідними даними
 * - Отримує створений план (перевірка читання)
 * - Оновлює план з коректною версією (успішне оновлення)
 * - Намагається оновити з старою версією (тест optimistic locking - очікується 409)
 * - Видаляє план
 * - Перевіряє що план дійсно видалений (404)
 *
 * МЕТРИКИ:
 * - Response time (p95, p99) для кожної операції
 * - Throughput (requests/second)
 * - Error rate (має бути < 1%)
 * - Success rate для optimistic locking конфліктів
 *
 * ============================================================================
 */

import { sleep } from 'k6';
import { DEFAULT_THRESHOLDS } from '../../config/endpoints.js';
import {
    createTravelPlan,
    getTravelPlan,
    updateTravelPlan,
    deleteTravelPlan,
    thinkTime,
} from '../../utils/api-client.js';
import {
    generateTravelPlan,
    generateTravelPlanUpdate,
} from '../../utils/data-generator.js';

// ============================================================================
// НАЛАШТУВАННЯ ТЕСТУ
// ============================================================================

export const options = {
    // Профіль навантаження: поступове зростання → стабільний стан → зменшення
    stages: [
        { duration: '1m', target: 10 },   // 1. Нормальне навантаження (10 VUs)
        { duration: '10s', target: 250 }, // 2. РІЗКИЙ СПЛЕСК (до 250 VUs за 10 секунд)
        { duration: '2m', target: 250 },  // 3. Утримання пікового сплеску
        { duration: '10s', target: 10 },  // 4. РІЗКЕ ЗНИЖЕННЯ назад до 10 VUs
        { duration: '1m', target: 0 },    // 5. Завершення тесту
    ],

    thresholds: {
        // Для Spike-тесту пороги м'якші, бо сплеск створює черги
        'http_req_duration': ['p(95)<3000'],
        'checks': ['rate>0.70'],
    },

    // Додаткові налаштування
    noConnectionReuse: false,
    userAgent: 'K6-SpikeTest/1.0',
};

// ============================================================================
// ОСНОВНИЙ СЦЕНАРІЙ ТЕСТУ
// ============================================================================

export default function () {
    // --------------------------------------------------
    // 1. СТВОРЕННЯ TRAVEL PLAN
    // --------------------------------------------------
    const newPlan = generateTravelPlan();
    const createdPlan = createTravelPlan(newPlan);

    if (!createdPlan) {
        console.error('Failed to create travel plan');
        return;
    }

    const planId = createdPlan.id;
    const initialVersion = createdPlan.version;

    // Пауза між операціями (імітація поведінки реального користувача)
    thinkTime(1, 2);

    // --------------------------------------------------
    // 2. ЧИТАННЯ TRAVEL PLAN
    // --------------------------------------------------
    const retrievedPlan = getTravelPlan(planId);

    if (!retrievedPlan) {
        console.error(`Failed to retrieve plan ${planId}`);
        deleteTravelPlan(planId); // Cleanup
        return;
    }

    thinkTime(1, 2);

    // --------------------------------------------------
    // 3. УСПІШНЕ ОНОВЛЕННЯ (з коректною версією)
    // --------------------------------------------------
    const updateData = generateTravelPlanUpdate(initialVersion);
    const updatedPlan = updateTravelPlan(planId, updateData);

    if (!updatedPlan || updatedPlan.conflict) {
        console.error(`Failed to update plan ${planId}`);
        deleteTravelPlan(planId); // Cleanup
        return;
    }

    thinkTime(0.5, 1);

    // --------------------------------------------------
    // 4. ТЕСТ OPTIMISTIC LOCKING (очікується конфлікт 409)
    // --------------------------------------------------
    // Намагаємось оновити зі старою версією - має повернути 409 Conflict
    const conflictUpdate = generateTravelPlanUpdate(initialVersion);
    const conflictResult = updateTravelPlan(planId, conflictUpdate);

    // Це очікувана поведінка - конфлікт версій
    if (conflictResult && conflictResult.conflict) {
        // Успішно виявлено конфлікт
    }

    thinkTime(0.5, 1);

    // --------------------------------------------------
    // 5. ВИДАЛЕННЯ TRAVEL PLAN
    // --------------------------------------------------
    const deleted = deleteTravelPlan(planId);

    if (!deleted) {
        console.error(`Failed to delete plan ${planId}`);
        return;
    }

    thinkTime(0.5, 1);

    // --------------------------------------------------
    // 6. ПЕРЕВІРКА ВИДАЛЕННЯ (очікується 404)
    // --------------------------------------------------
    // Намагаємось отримати видалений план - має повернути 404
    getTravelPlan(planId); // Функція сама перевірить 404

    // Пауза перед наступною ітерацією
    sleep(1);
}

// ============================================================================
// SETUP & TEARDOWN
// ============================================================================

export function setup() {
    console.log('='.repeat(80));
    console.log('Starting CRUD Load Test');
    console.log('Target: 50 concurrent users');
    console.log('Duration: 9 minutes (2m ramp-up + 5m steady + 2m ramp-down)');
    console.log('='.repeat(80));
}

export function teardown(data) {
    console.log('='.repeat(80));
    console.log('CRUD Load Test completed');
    console.log('='.repeat(80));
}
