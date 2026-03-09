CREATE TABLE package_session (
    id VARCHAR(255) PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    offering_id VARCHAR(255) NOT NULL,
    session_limit INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    creation_user VARCHAR(100),
    modification_user VARCHAR(100),
    creation_timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modification_timestamp DATETIME,
    CONSTRAINT fk_package_session_offering FOREIGN KEY (offering_id) REFERENCES offering (id)
);


CREATE TABLE booking_package (
    id VARCHAR(255) PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    package_session_id VARCHAR(255),
    sessions_total INT NOT NULL,
    sessions_used INT NOT NULL DEFAULT 0,
    price_paid DECIMAL(10,2) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    external_payment_id VARCHAR(255),
    creation_user VARCHAR(100),
    modification_user VARCHAR(100),
    creation_timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modification_timestamp DATETIME,
    CONSTRAINT fk_booking_package_session FOREIGN KEY (package_session_id) REFERENCES package_session (id)
);


ALTER TABLE booking
ADD COLUMN booking_package_id VARCHAR(255) NULL;

ALTER TABLE booking
ADD CONSTRAINT fk_booking_package
FOREIGN KEY (booking_package_id) REFERENCES booking_package (id);

ALTER TABLE booking_audit
ADD COLUMN booking_package_id VARCHAR(255) NULL;