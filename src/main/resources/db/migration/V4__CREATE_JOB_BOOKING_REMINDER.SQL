CREATE TABLE booking_reminder_job (
    id VARCHAR(36)         PRIMARY KEY,
    booking_id VARCHAR(36) NOT NULL,
    trigger_datetime       TIMESTAMP NULL,
    status VARCHAR(20)     NOT NULL,
    notification_channel   VARCHAR(20) NOT NULL,
    processed_at           TIMESTAMP NULL,
    re_attempts            INT NOT NULL DEFAULT 0,
    enabled                TINYINT(1) NOT NULL DEFAULT 1,
    creation_user          VARCHAR(100),
    modification_user      VARCHAR(100),
    creation_timestamp     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modification_timestamp DATETIME,
    CONSTRAINT fk_booking_reminder_job_booking FOREIGN KEY (booking_id) REFERENCES booking(id)
);

CREATE INDEX idx_booking_reminder_job_status_trigger
ON booking_reminder_job(status, trigger_datetime);

INSERT INTO booking_reminder_job (
    id,
    booking_id,
    trigger_datetime,
    status,
    notification_channel,
    enabled,
    creation_user,
    creation_timestamp
)
SELECT
    UUID(),
    b.id,
    DATE_ADD(DATE(DATE_SUB(st.start_date_time, INTERVAL 1 DAY)), INTERVAL 12 HOUR),
    'PENDING',
    'EMAIL',
    1,
    'SYSTEM_MIGRATOR',
    NOW()
FROM booking b
JOIN slot_time st ON b.slot_time_id = st.id
WHERE st.start_date_time > NOW()
  AND b.status = 'CONFIRMED'
  AND b.enabled = 1
  AND b.id NOT IN (SELECT booking_id FROM booking_reminder_job);