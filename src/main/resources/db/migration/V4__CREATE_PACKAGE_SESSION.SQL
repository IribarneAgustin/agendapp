CREATE TABLE package_session (
    id VARCHAR(255) PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    offering_id VARCHAR(255) NOT NULL,
    session_limit INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_package_session_offering FOREIGN KEY (offering_id) REFERENCES offering (id)
);

CREATE TABLE package_session_audit (
    id VARCHAR(255) NOT NULL,
    rev INT NOT NULL,
    revtype TINYINT,
    enabled BOOLEAN,
    offering_id VARCHAR(255),
    session_limit INT,
    price DECIMAL(10, 2),
    status VARCHAR(50),
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_package_session_audit_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE TABLE booking_package (
    id VARCHAR(255) PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    package_session_id VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(255),
    sessions_used INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    external_payment_id VARCHAR(255),
    CONSTRAINT fk_booking_package_session FOREIGN KEY (package_session_id) REFERENCES package_session (id)
);

CREATE TABLE booking_package_audit (
    id VARCHAR(255) NOT NULL,
    rev INT NOT NULL,
    revtype TINYINT,
    enabled BOOLEAN,
    package_session_id VARCHAR(255),
    customer_email VARCHAR(255),
    customer_name VARCHAR(255),
    customer_phone VARCHAR(255),
    sessions_used INT,
    status VARCHAR(50),
    external_payment_id VARCHAR(255),
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_booking_package_audit_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

ALTER TABLE booking ADD COLUMN booking_package_id VARCHAR(255) NULL;
ALTER TABLE booking ADD CONSTRAINT fk_booking_package FOREIGN KEY (booking_package_id) REFERENCES booking_package (id);
ALTER TABLE booking_audit ADD COLUMN booking_package_id VARCHAR(255) NULL;
