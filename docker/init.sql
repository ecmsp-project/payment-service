-- -- Table: payment_events
-- CREATE TABLE payment_events (
--                                 id UUID PRIMARY KEY,
--                                 payment_id UUID  NOT NULL,
--                                 event_type varchar(50)  NOT NULL,
--                                 event_data text  NULL,
--                                 status varchar(20)  NOT NULL DEFAULT 'pending',
--                                 retry_count integer  NULL DEFAULT 0,
--                                 processed_at timestamp  NULL,
--                                 created_at timestamp  NOT NULL DEFAULT current_timestamp,
--                                 CONSTRAINT CHECK_1 CHECK (( event_type IN ( 'PENDING' , 'PAID' , 'EXPIRED' , 'CREATED' ) )) NOT DEFERRABLE INITIALLY IMMEDIATE,
--                                 CONSTRAINT CHECK_2 CHECK (( status IN ( 'PENDING' , 'PAID' , 'EXPIRED' , 'CREATED' ) )) NOT DEFERRABLE INITIALLY IMMEDIATE,
--                                 CONSTRAINT payment_events_pk PRIMARY KEY (id)
-- );

-- CREATE INDEX idx_payment_events_payment_id on payment_events (payment_id ASC);
--
-- CREATE INDEX idx_payment_events_status on payment_events (status ASC);
--
-- CREATE INDEX idx_payment_events_event_type on payment_events (event_type ASC);
--
-- CREATE INDEX idx_payment_events_created_at on payment_events (created_at ASC);
--
-- CREATE INDEX idx_payment_events_status_retry_count on payment_events (status ASC,retry_count ASC);

-- Table: payments
CREATE TABLE payments (
                          id UUID PRIMARY KEY,
                          order_id UUID  NOT NULL,
                          user_id UUID  NOT NULL,
                          order_total numeric(10,2)  NOT NULL,
                          currency varchar(3)  NOT NULL DEFAULT 'PLN',
                          status varchar(20)  NOT NULL DEFAULT 'PENDING',
                          payment_link varchar(255)  NULL,
                          expires_at timestamp  NOT NULL,
                          paid_at timestamp  NULL,
                          created_at timestamp  NOT NULL DEFAULT current_timestamp,
                          updated_at timestamp  NULL,
                          CONSTRAINT AK_0 UNIQUE (payment_link) NOT DEFERRABLE  INITIALLY IMMEDIATE,
                          CONSTRAINT CHECK_0 CHECK (( status IN ( 'PENDING' , 'PAID' , 'EXPIRED' , 'CREATED' ) )) NOT DEFERRABLE INITIALLY IMMEDIATE
);



CREATE INDEX idx_payments_order_id on payments (order_id ASC);

CREATE INDEX idx_payments_user_id on payments (user_id ASC);

CREATE INDEX idx_payments_status on payments (status ASC);

CREATE INDEX idx_payments_expires_at on payments (expires_at ASC);

CREATE INDEX idx_payments_payment_link on payments (payment_link ASC);

CREATE INDEX idx_payments_status_expires_at on payments (status ASC,expires_at ASC);


CREATE TABLE kafka_outbox
(
    event_id     UUID PRIMARY KEY,
    payload      TEXT,
    key          TEXT,
    topic        TEXT,
    created_at   TIMESTAMP    NOT NULL,
    processed    BOOLEAN      NOT NULL DEFAULT FALSE,
    processed_at TIMESTAMP
);

CREATE INDEX idx_kafka_outbox_processed ON kafka_outbox (processed);
CREATE INDEX idx_kafka_outbox_created_at ON kafka_outbox (created_at);
CREATE INDEX idx_kafka_outbox_processed_at ON kafka_outbox (processed_at) WHERE processed = TRUE;
