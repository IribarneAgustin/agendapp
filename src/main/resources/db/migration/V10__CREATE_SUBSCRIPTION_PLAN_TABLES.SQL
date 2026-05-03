CREATE TABLE subscription_plan (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    price DECIMAL(10,2) NOT NULL,
    max_bookings INT NULL,
    max_resources INT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    creation_user          VARCHAR(100),
    modification_user      VARCHAR(100),
    creation_timestamp     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modification_timestamp DATETIME
);

ALTER TABLE subscription
ADD COLUMN subscription_plan_id VARCHAR(36) NULL,
ADD COLUMN selected_resources_limit INT NULL;

ALTER TABLE subscription_audit
ADD COLUMN subscription_plan_id VARCHAR(36) NULL,
ADD COLUMN selected_resources_limit INT NULL;

CREATE TABLE subscription_usage (
    id VARCHAR(36) PRIMARY KEY,
    subscription_id VARCHAR(36),
    booking_usage INT DEFAULT 0,
    start_period_date_time DATETIME NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    period_status VARCHAR(36) NOT NULL,
    creation_user          VARCHAR(100),
    modification_user      VARCHAR(100),
    creation_timestamp     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modification_timestamp DATETIME,
    CONSTRAINT fk_usage_subscription FOREIGN KEY (subscription_id) REFERENCES subscription(id)
);

INSERT INTO subscription_plan (id, code, price, max_bookings, max_resources)
VALUES (UUID(), 'FREE_TIER', 0, 100, 2);

INSERT INTO subscription_plan (id, code, price, max_bookings, max_resources)
VALUES (UUID(), 'BASIC', 15000, 500, 2);

INSERT INTO subscription_plan (id, code, price, max_bookings, max_resources)
VALUES (UUID(), 'PROFESSIONAL', 29000, NULL, NULL);

