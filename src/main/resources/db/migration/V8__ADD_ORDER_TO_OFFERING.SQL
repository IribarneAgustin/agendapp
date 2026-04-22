ALTER TABLE offering
ADD COLUMN display_order INT DEFAULT 0;

ALTER TABLE offering_audit
ADD COLUMN display_order INT DEFAULT 0;

UPDATE offering SET display_order = 0 WHERE display_order IS NULL;;

UPDATE offering_audit SET display_order = 0 WHERE display_order IS NULL;;