CREATE DATABASE postgres;

CREATE TABLE public.planned
(
    id integer,
    train_id integer,
    location_id integer,
    planned_time double,
    status integer,
    first_train_number integer,
    train_number_reference integer,
    planned_departure_reference double,
    planned_arrival_reference double,
    train_operator_id integer,
    transfer_location_id integer,
    reporting_im_id integer,
    next_im_id integer,
    message_status integer,
    message_creation double,
    original_train_number integer,
);

INSERT INTO public.planned(
            id  ,
            train_id  ,
            location_id  ,
            planned_time ,
            status,
            first_train_number,
            train_number_reference  ,
            planned_departure_reference ,
            planned_arrival_reference ,
            train_operator_id  ,
            transfer_location_id  ,
            reporting_im_id  ,
            next_im_id  ,
            message_status  ,
            message_creation ,
            original_train_number  ,)
    VALUES (1,42382923,3,4,5,6,7,8,9,10,11,12,13,14,15);
    
    
    
INSERT INTO public.planned(
            id  ,
            train_id  ,
            location_id  ,
            planned_time ,
            status,
            first_train_number,
            train_number_reference  ,
            planned_departure_reference ,
            planned_arrival_reference ,
            train_operator_id  ,
            transfer_location_id  ,
            reporting_im_id  ,
            next_im_id  ,
            message_status  ,
            message_creation ,
            original_train_number  ,)
    VALUES (1,42093766,3,4,5,6,7,8,9,10,11,12,13,14,15);
