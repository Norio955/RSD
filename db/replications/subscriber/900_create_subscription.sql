CREATE SUBSCRIPTION tp_sub
CONNECTION 'host=postgres port=5432 dbname=traveler_db user=repuser password=rep_pass'
PUBLICATION tp_pub
WITH (
    create_slot = true,
    slot_name = 'tp_sub_slot',
    copy_data = true,
    enabled = true
);