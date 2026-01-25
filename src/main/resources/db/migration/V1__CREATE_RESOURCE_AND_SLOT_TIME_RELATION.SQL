CREATE TABLE resource (
    id                     VARCHAR(36) NOT NULL,
    user_id                VARCHAR(36) NOT NULL,
    enabled                TINYINT(1) NOT NULL DEFAULT 1,
    name                   VARCHAR(100) NOT NULL,
    last_name              VARCHAR(100) NOT NULL,
    is_default             TINYINT(1) NOT NULL DEFAULT 0,
    creation_user          VARCHAR(100),
    modification_user      VARCHAR(100),
    creation_timestamp     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modification_timestamp DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_resource_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    INDEX idx_resource_user (user_id),
    INDEX idx_resource_user_default (user_id, is_default)
);

-- Create Resource Audit Table
CREATE TABLE resource_audit (
    id        VARCHAR(36) NOT NULL,
    rev       INT NOT NULL,
    revtype   TINYINT,
    user_id   VARCHAR(36),
    enabled   TINYINT(1),
    name      VARCHAR(100),
    last_name VARCHAR(100),
    is_default TINYINT(1),
    PRIMARY KEY (id, rev)
);

INSERT INTO resource (
    id,
    user_id,
    name,
    last_name,
    is_default,
    enabled,
    creation_timestamp
)
SELECT
    UUID(),
    u.id,
    u.name,
    u.last_name,
    1,
    1,
    NOW()
FROM user u
WHERE NOT EXISTS (
    SELECT 1
    FROM resource r
    WHERE r.user_id = u.id
);

ALTER TABLE slot_time
ADD COLUMN resource_id VARCHAR(36);

ALTER TABLE slot_time_audit
ADD COLUMN resource_id VARCHAR(36);

UPDATE slot_time st
JOIN offering o ON o.id = st.offering_id
JOIN resource r ON r.user_id = o.user_id AND r.is_default = 1
SET st.resource_id = r.id
WHERE st.resource_id IS NULL;

UPDATE slot_time_audit sta
JOIN offering o ON o.id = sta.offering_id
JOIN resource r ON r.user_id = o.user_id AND r.is_default = 1
SET sta.resource_id = r.id
WHERE sta.resource_id IS NULL;

ALTER TABLE slot_time
ADD CONSTRAINT fk_slot_time_resource
FOREIGN KEY (resource_id)
REFERENCES resource (id);

ALTER TABLE slot_time_audit
ADD CONSTRAINT fk_slot_time_resource_audit
FOREIGN KEY (resource_id)
REFERENCES resource (id);

ALTER TABLE slot_time
MODIFY resource_id VARCHAR(36) NOT NULL;

ALTER TABLE slot_time_audit
MODIFY resource_id VARCHAR(36) NOT NULL;