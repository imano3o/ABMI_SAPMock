-- Synthetic initial stock of the ward pharmacies.
-- Plant 1710 as in the SAP sandbox; the storage location is the ward code from ABMI:
-- ST01 = ward 1 Surgery, ST02 = ward 2 Cardiology, ST03 = ward 3 Internal Medicine.
-- Some medications are deliberately below the minimum stock from ABMI, so the traffic light in the hospital system is visible.
INSERT INTO material_stock (material, plant, storage_location, base_unit, quantity) VALUES
    -- Ward 1 – Surgery
    ('MED-PCM500',    '1710', 'ST01', 'PC', 140),  -- Paracetamol 500 mg tablets
    ('MED-MORPH10',   '1710', 'ST01', 'PC', 8),    -- Morphine 10 mg ampoules (critical, minimum stock 20)
    ('MED-AMOXI1000', '1710', 'ST01', 'PC', 32),   -- Amoxicillin 1000 mg tablets (low, minimum stock 40)
    ('MED-NACL500',   '1710', 'ST01', 'PC', 45),   -- NaCl 0.9% 500 ml infusion
    ('MED-IBU400',    '1710', 'ST01', 'PC', 160),  -- Ibuprofen 400 mg tablets
    ('MED-PANTO40',   '1710', 'ST01', 'PC', 60),   -- Pantoprazol 40 mg tablets
    -- Ward 2 – Cardiology
    ('MED-HEP5000',   '1710', 'ST02', 'PC', 28),   -- Heparin 5,000 IU ampoules (low, minimum stock 40)
    ('MED-FUROS40',   '1710', 'ST02', 'PC', 20),   -- Furosemid 40 mg tablets (critical, minimum stock 50)
    ('MED-ENOX40',    '1710', 'ST02', 'PC', 30),   -- Enoxaparin 40 mg prefilled syringes
    ('MED-PANTO40',   '1710', 'ST02', 'PC', 80),   -- Pantoprazol 40 mg tablets
    ('MED-INSACT',    '1710', 'ST02', 'PC', 10),   -- Insulin Actrapid 10 ml vial
    -- Ward 3 – Internal Medicine
    ('MED-IBU400',    '1710', 'ST03', 'PC', 240),  -- Ibuprofen 400 mg tablets
    ('MED-PCM500',    '1710', 'ST03', 'PC', 180),  -- Paracetamol 500 mg tablets
    ('MED-METAM500',  '1710', 'ST03', 'PC', 18),   -- Metamizol 500 mg tablets (low)
    ('MED-PANTO40',   '1710', 'ST03', 'PC', 120),  -- Pantoprazol 40 mg tablets
    ('MED-ENOX40',    '1710', 'ST03', 'PC', 6),    -- Enoxaparin 40 mg prefilled syringes (low)
    ('MED-HEP5000',   '1710', 'ST03', 'PC', 35),   -- Heparin 5,000 IU ampoules
    ('MED-NACL500',   '1710', 'ST03', 'PC', 14),   -- NaCl 0.9% 500 ml infusion (low)
    ('MED-AMOXI1000', '1710', 'ST03', 'PC', 64),   -- Amoxicillin 1000 mg tablets
    ('MED-FUROS40',   '1710', 'ST03', 'PC', 90),   -- Furosemid 40 mg tablets
    ('MED-ONDAN4',    '1710', 'ST03', 'PC', 3),    -- Ondansetron 4 mg ampoules (critical)
    ('MED-MORPH10',   '1710', 'ST03', 'PC', 25),   -- Morphine 10 mg ampoules
    ('MED-INSACT',    '1710', 'ST03', 'PC', 12);   -- Insulin Actrapid 10 ml vial
