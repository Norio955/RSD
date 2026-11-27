CREATE TABLE public.travel_plans (
                                     id uuid NOT NULL,
                                     budget numeric(10,2),
                                     currency character varying(3),
                                     description character varying(255),
                                     end_date date,
                                     is_public boolean,
                                     start_date date,
                                     title character varying(200) NOT NULL,
                                     version integer,
                                     CONSTRAINT travel_plans_version_check CHECK (version >= 1)
);

ALTER TABLE ONLY public.travel_plans
    ADD CONSTRAINT travel_plans_pkey PRIMARY KEY (id);

CREATE TABLE public.locations (
                                  id uuid NOT NULL,
                                  address character varying(255),
                                  arrival_date timestamp(6) with time zone,
                                  budget numeric(38,2),
                                  departure_date timestamp(6) with time zone,
                                  latitude numeric(38,2),
                                  longitude numeric(38,2),
                                  name character varying(200) NOT NULL,
                                  notes character varying(255),
                                  version bigint,
                                  visit_order integer,
                                  travel_plan_id uuid NOT NULL
);

ALTER TABLE ONLY public.locations
    ADD CONSTRAINT locations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.locations
    ADD CONSTRAINT fkmx71fq1x9iwuo3a5mh930oory
    FOREIGN KEY (travel_plan_id)
    REFERENCES public.travel_plans(id);