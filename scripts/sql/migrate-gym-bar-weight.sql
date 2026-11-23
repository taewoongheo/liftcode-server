-- Run once on databases with the separate bar settings table.
BEGIN;
LOCK TABLE gym_equipment_weight_settings, gym_equipment_bar_settings IN ACCESS EXCLUSIVE MODE;

ALTER TABLE gym_equipment_weight_settings
    ADD COLUMN bar_weight double precision
    CHECK (bar_weight >= 0 AND bar_weight < 'Infinity');

UPDATE gym_equipment_weight_settings AS setting
SET bar_weight = bar.bar_weight
FROM gym_equipment_bar_settings AS bar
WHERE setting.gym_id = bar.gym_id AND setting.equipment_id = bar.equipment_id;

DROP TABLE gym_equipment_bar_settings;
COMMIT;
