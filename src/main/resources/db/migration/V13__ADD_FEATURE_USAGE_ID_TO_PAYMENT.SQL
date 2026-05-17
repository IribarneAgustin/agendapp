ALTER TABLE payment
ADD COLUMN feature_usage_id VARCHAR(36) NULL;

ALTER TABLE payment
ADD CONSTRAINT fk_payment_feature_usage
FOREIGN KEY (feature_usage_id) REFERENCES feature_usage (id);

ALTER TABLE payment_audit
ADD COLUMN feature_usage_id VARCHAR(36) NULL;