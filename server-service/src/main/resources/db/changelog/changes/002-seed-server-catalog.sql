INSERT INTO data_type (code) VALUES
    ('STRING'),
    ('DECIMAL'),
    ('BOOLEAN'),
    ('TIMESTAMP'),
    ('OPTION');

INSERT INTO entity_type (code, name) VALUES
    ('SERVER', 'Monitored server');

INSERT INTO attribute (code, name, data_type_id)
SELECT 'hostname', 'Hostname', id FROM data_type WHERE code = 'STRING';
INSERT INTO attribute (code, name, data_type_id)
SELECT 'ip_address', 'IP address', id FROM data_type WHERE code = 'STRING';
INSERT INTO attribute (code, name, data_type_id)
SELECT 'description', 'Description', id FROM data_type WHERE code = 'STRING';
INSERT INTO attribute (code, name, data_type_id)
SELECT 'environment', 'Environment', id FROM data_type WHERE code = 'OPTION';
INSERT INTO attribute (code, name, data_type_id)
SELECT 'status', 'Status', id FROM data_type WHERE code = 'OPTION';

INSERT INTO entity_type_attribute (entity_type_id, attribute_id, required, unique_within_type, sort_order)
SELECT t.id, a.id, true, true, 1
FROM entity_type t, attribute a
WHERE t.code = 'SERVER' AND a.code = 'hostname';

INSERT INTO entity_type_attribute (entity_type_id, attribute_id, required, unique_within_type, sort_order)
SELECT t.id, a.id, true, false, 2
FROM entity_type t, attribute a
WHERE t.code = 'SERVER' AND a.code = 'ip_address';

INSERT INTO entity_type_attribute (entity_type_id, attribute_id, required, unique_within_type, sort_order)
SELECT t.id, a.id, true, false, 3
FROM entity_type t, attribute a
WHERE t.code = 'SERVER' AND a.code = 'environment';

INSERT INTO entity_type_attribute (entity_type_id, attribute_id, required, unique_within_type, sort_order)
SELECT t.id, a.id, true, false, 4
FROM entity_type t, attribute a
WHERE t.code = 'SERVER' AND a.code = 'status';

INSERT INTO entity_type_attribute (entity_type_id, attribute_id, required, unique_within_type, sort_order)
SELECT t.id, a.id, false, false, 5
FROM entity_type t, attribute a
WHERE t.code = 'SERVER' AND a.code = 'description';

INSERT INTO attribute_option (attribute_id, id, code, label)
SELECT a.id, v.id, v.code, v.label
FROM attribute a
JOIN (VALUES
    (1, 'DEV', 'Development'),
    (2, 'STAGE', 'Stage'),
    (3, 'PROD', 'Production')
) AS v(id, code, label) ON true
WHERE a.code = 'environment';

INSERT INTO attribute_option (attribute_id, id, code, label)
SELECT a.id, v.id, v.code, v.label
FROM attribute a
JOIN (VALUES
    (1, 'UP', 'Up'),
    (2, 'DOWN', 'Down'),
    (3, 'UNKNOWN', 'Unknown'),
    (4, 'MAINTENANCE', 'Maintenance')
) AS v(id, code, label) ON true
WHERE a.code = 'status';
