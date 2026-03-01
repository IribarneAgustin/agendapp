ALTER TABLE offering
ADD COLUMN terms_and_conditions TEXT;

ALTER TABLE offering_audit
ADD COLUMN terms_and_conditions TEXT;
