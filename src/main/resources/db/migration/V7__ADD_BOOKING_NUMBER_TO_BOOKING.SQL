ALTER TABLE booking ADD COLUMN booking_number INT;
ALTER TABLE booking_audit ADD COLUMN booking_number INT;

UPDATE booking b
JOIN (
    SELECT
        b.id,
        ROW_NUMBER() OVER (
            PARTITION BY o.user_id, b.phone_number
            ORDER BY b.creation_timestamp, b.id
        ) AS rn
    FROM booking b
    JOIN slot_time st ON b.slot_time_id = st.id
    JOIN offering o ON st.offering_id = o.id
) t ON b.id = t.id
SET b.booking_number = t.rn;

UPDATE booking_audit ba
JOIN booking b ON ba.id = b.id
SET ba.booking_number = b.booking_number;