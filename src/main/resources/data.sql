-- Synthetische Startbestände der Stationsapotheke.
-- Werk 1710 wie in der SAP-Sandbox, Lagerort ST03 = Station 3.
-- Einige Medikamente liegen bewusst niedrig, damit die Hervorhebung im KIS sichtbar ist.
INSERT INTO material_stock (material, plant, storage_location, base_unit, quantity) VALUES
    ('MED-IBU400',    '1710', 'ST03', 'PC', 240),  -- Ibuprofen 400 mg Tabletten
    ('MED-PCM500',    '1710', 'ST03', 'PC', 180),  -- Paracetamol 500 mg Tabletten
    ('MED-METAM500',  '1710', 'ST03', 'PC', 18),   -- Metamizol 500 mg Tabletten (knapp)
    ('MED-PANTO40',   '1710', 'ST03', 'PC', 120),  -- Pantoprazol 40 mg Tabletten
    ('MED-ENOX40',    '1710', 'ST03', 'PC', 6),    -- Enoxaparin 40 mg Fertigspritzen (kritisch)
    ('MED-HEP5000',   '1710', 'ST03', 'PC', 35),   -- Heparin 5.000 I.E. Ampullen
    ('MED-NACL500',   '1710', 'ST03', 'PC', 14),   -- NaCl 0,9 % 500 ml Infusion (knapp)
    ('MED-AMOXI1000', '1710', 'ST03', 'PC', 64),   -- Amoxicillin 1000 mg Tabletten
    ('MED-FUROS40',   '1710', 'ST03', 'PC', 90),   -- Furosemid 40 mg Tabletten
    ('MED-ONDAN4',    '1710', 'ST03', 'PC', 3),    -- Ondansetron 4 mg Ampullen (kritisch)
    ('MED-MORPH10',   '1710', 'ST03', 'PC', 25),   -- Morphin 10 mg Ampullen
    ('MED-INSACT',    '1710', 'ST03', 'PC', 12);   -- Insulin Actrapid 10 ml Durchstechflasche
