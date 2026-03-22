CREATE TABLE offering_category (
    id VARCHAR(36) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    creation_user VARCHAR(255),
    modification_user VARCHAR(255),
    creation_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modification_timestamp TIMESTAMP,
    user_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_offering_category PRIMARY KEY (id),
    CONSTRAINT fk_offering_category_user FOREIGN KEY (user_id) REFERENCES user(id)
);

ALTER TABLE offering ADD COLUMN category_id VARCHAR(36);
ALTER TABLE offering_audit ADD COLUMN category_id VARCHAR(36);

ALTER TABLE offering
ADD CONSTRAINT fk_offering_category
FOREIGN KEY (category_id) REFERENCES offering_category(id);

INSERT INTO offering_category (id, user_id, name, is_default)
SELECT UUID(), u.id, 'Otros', true
FROM user u
WHERE NOT EXISTS (
    SELECT 1
    FROM offering_category c
    WHERE c.user_id = u.id
      AND c.is_default = true
);

UPDATE offering o
JOIN offering_category c ON o.user_id = c.user_id
SET o.category_id = c.id
WHERE c.name = 'Otros';

ALTER TABLE offering MODIFY category_id VARCHAR(36) NOT NULL;