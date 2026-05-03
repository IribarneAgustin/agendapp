UPDATE subscription s
SET s.selected_resources_limit = (
    SELECT COUNT(r.id)
    FROM resource r
    JOIN `user` u ON r.user_id = u.id
    WHERE u.subscription_id = s.id
    AND r.enabled = 1
);

UPDATE subscription s
SET s.subscription_plan_id = (SELECT id FROM subscription_plan WHERE code = 'PROFESSIONAL')
WHERE s.selected_resources_limit > 2
AND EXISTS (
    SELECT 1 FROM payment p
    WHERE p.subscription_id = s.id
    AND p.payment_status = 'COMPLETED'
);

UPDATE subscription s
SET s.subscription_plan_id = (SELECT id FROM subscription_plan WHERE code = 'BASIC')
WHERE s.selected_resources_limit <= 2
AND EXISTS (
    SELECT 1 FROM payment p
    WHERE p.subscription_id = s.id
    AND p.payment_status = 'COMPLETED'
);

UPDATE subscription s
SET s.subscription_plan_id = (SELECT id FROM subscription_plan WHERE code = 'FREE_TIER')
WHERE s.subscription_plan_id IS NULL;

UPDATE subscription s
SET s.checkout_link = 'https://reservalink.site/public/subscription-plans.html'
WHERE s.subscription_plan_id = (SELECT id FROM subscription_plan WHERE code = 'FREE_TIER');

ALTER TABLE subscription
MODIFY subscription_plan_id VARCHAR(36) NOT NULL;

ALTER TABLE subscription
ADD CONSTRAINT fk_subscription_plan
FOREIGN KEY (subscription_plan_id) REFERENCES subscription_plan(id);