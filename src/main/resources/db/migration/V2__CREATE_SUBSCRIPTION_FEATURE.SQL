CREATE TABLE subscription_feature (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(50) NOT NULL,
    usage_limit INTEGER NULL,
    enabled BOOLEAN DEFAULT TRUE,
    price NUMERIC(10,2) NOT NULL,
    creation_user VARCHAR(100),
    modification_user VARCHAR(100),
    creation_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modification_timestamp TIMESTAMP,
    CONSTRAINT pk_subscription_feature PRIMARY KEY (id)
);

CREATE TABLE feature_usage (
    id VARCHAR(36) NOT NULL,
    subscription_id VARCHAR(36) NOT NULL,
    subscription_feature_id VARCHAR(36) NOT NULL,
    usage_count INT NULL DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE,
    feature_status VARCHAR(30) NOT NULL,
    creation_user VARCHAR(100),
    modification_user VARCHAR(100),
    creation_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modification_timestamp TIMESTAMP,
    CONSTRAINT pk_feature_usage PRIMARY KEY (id),

    CONSTRAINT fk_feature_usage_subscription
        FOREIGN KEY (subscription_id)
        REFERENCES subscription (id),

    CONSTRAINT fk_feature_usage_subscription_feature
        FOREIGN KEY (subscription_feature_id)
        REFERENCES subscription_feature (id)
);


INSERT INTO subscription_feature (id, name, usage_limit, enabled, price)
VALUES
(UUID(), 'WHATSAPP_NOTIFICATIONS', 50,  TRUE,  3500.00),
(UUID(), 'WHATSAPP_NOTIFICATIONS', 100, TRUE,  6500.00),
(UUID(), 'WHATSAPP_NOTIFICATIONS', 300, TRUE, 18000.00),
(UUID(), 'WHATSAPP_NOTIFICATIONS', 500, TRUE, 29000.00);