-- Synthetische Startbestände der Stationsapotheken.
-- Werk 1710 wie in der SAP-Sandbox; der Lagerort ist der Stationscode aus ABMI:
-- ST01 = Station 1 Chirurgie, ST02 = Station 2 Kardiologie, ST03 = Station 3 Innere Medizin.
-- Einige Medikamente liegen bewusst unter dem Mindestbestand aus ABMI, damit die Ampel im KIS sichtbar ist.
INSERT INTO material_stock (material, plant, storage_location, base_unit, quantity) VALUES
    -- Station 1 – Chirurgie
    ('MED-PCM500',    '1710', 'ST01', 'PC', 140),  -- Paracetamol 500 mg Tabletten
    ('MED-MORPH10',   '1710', 'ST01', 'PC', 8),    -- Morphin 10 mg Ampullen (kritisch, Mindestbestand 20)
    ('MED-AMOXI1000', '1710', 'ST01', 'PC', 32),   -- Amoxicillin 1000 mg Tabletten (knapp, Mindestbestand 40)
    ('MED-NACL500',   '1710', 'ST01', 'PC', 45),   -- NaCl 0,9 % 500 ml Infusion
    ('MED-IBU400',    '1710', 'ST01', 'PC', 160),  -- Ibuprofen 400 mg Tabletten
    ('MED-PANTO40',   '1710', 'ST01', 'PC', 60),   -- Pantoprazol 40 mg Tabletten
    -- Station 2 – Kardiologie
    ('MED-HEP5000',   '1710', 'ST02', 'PC', 28),   -- Heparin 5.000 I.E. Ampullen (knapp, Mindestbestand 40)
    ('MED-FUROS40',   '1710', 'ST02', 'PC', 20),   -- Furosemid 40 mg Tabletten (kritisch, Mindestbestand 50)
    ('MED-ENOX40',    '1710', 'ST02', 'PC', 30),   -- Enoxaparin 40 mg Fertigspritzen
    ('MED-PANTO40',   '1710', 'ST02', 'PC', 80),   -- Pantoprazol 40 mg Tabletten
    ('MED-INSACT',    '1710', 'ST02', 'PC', 10),   -- Insulin Actrapid 10 ml Durchstechflasche
    -- Station 3 – Innere Medizin
    ('MED-IBU400',    '1710', 'ST03', 'PC', 240),  -- Ibuprofen 400 mg Tabletten
    ('MED-PCM500',    '1710', 'ST03', 'PC', 180),  -- Paracetamol 500 mg Tabletten
    ('MED-METAM500',  '1710', 'ST03', 'PC', 18),   -- Metamizol 500 mg Tabletten (knapp)
    ('MED-PANTO40',   '1710', 'ST03', 'PC', 120),  -- Pantoprazol 40 mg Tabletten
    ('MED-ENOX40',    '1710', 'ST03', 'PC', 6),    -- Enoxaparin 40 mg Fertigspritzen (knapp)
    ('MED-HEP5000',   '1710', 'ST03', 'PC', 35),   -- Heparin 5.000 I.E. Ampullen
    ('MED-NACL500',   '1710', 'ST03', 'PC', 14),   -- NaCl 0,9 % 500 ml Infusion (knapp)
    ('MED-AMOXI1000', '1710', 'ST03', 'PC', 64),   -- Amoxicillin 1000 mg Tabletten
    ('MED-FUROS40',   '1710', 'ST03', 'PC', 90),   -- Furosemid 40 mg Tabletten
    ('MED-ONDAN4',    '1710', 'ST03', 'PC', 3),    -- Ondansetron 4 mg Ampullen (kritisch)
    ('MED-MORPH10',   '1710', 'ST03', 'PC', 25),   -- Morphin 10 mg Ampullen
    ('MED-INSACT',    '1710', 'ST03', 'PC', 12);   -- Insulin Actrapid 10 ml Durchstechflasche
