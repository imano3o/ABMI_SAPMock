# ABMI_SAPMock

Lokale Simulation der SAP-S/4HANA-OData-APIs, die das ABMI-Backend (MediFlow) braucht.
Der Mock läuft auf Port **8081** und bildet zwei SAP-APIs nach: Bestände (Material Stock) und Materialbelege (Material Document), jeweils mit SAP-Fehlermeldungen.
Fachliche Regeln wie Mindestbestand oder Status liegen im ABMI-Backend.

| API | Endpunkt | Stand |
|---|---|---|
| Material Stock | `GET /sap/opu/odata/sap/API_MATERIAL_STOCK_SRV/A_MatlStkInAcctMod` | fertig |
| Material Document | `POST /sap/opu/odata/sap/API_MATERIAL_DOCUMENT_SRV/A_MaterialDocumentHeader` | fertig (nur Bewegungsart 201) |

## Voraussetzungen

- Java 25
- Docker (für PostgreSQL), alternativ eine eigene PostgreSQL-Installation

## Starten

```bash
# 1. Datenbank starten
docker compose up -d

# 2. Mock starten (Windows: mvnw.cmd statt ./mvnw)
./mvnw spring-boot:run
```

Danach ist der Mock unter `http://localhost:8081` erreichbar. Beispielaufrufe stehen in `requests.http`
(direkt in IntelliJ ausführbar).

Datenbank stoppen: `docker compose down`. Mit `docker compose down -v` wird auch das Volume gelöscht.

### Eigene Datenbank

Die Verbindung lässt sich über Umgebungsvariablen ändern:

| Variable | Standard |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/sapmock` |
| `DB_USERNAME` | `sapmock` |
| `DB_PASSWORD` | `sapmock` |

## Startbestände

Bei jedem Start werden die Tabellen neu angelegt und aus `src/main/resources/data.sql` befüllt,
so beginnt jede Demo mit denselben Zahlen. Werk `1710`, Einheit `PC`. Der Lagerort ist der Stationscode aus ABMI.
Der Status in der Tabelle ergibt sich aus den Mindestbeständen in ABMI; der Mock selbst kennt keinen Status.

| Lagerort | Material | Medikament | Bestand |
|---|---|---|---|
| `ST01` Chirurgie | `MED-PCM500` | Paracetamol 500 mg Tabletten | 140 |
| `ST01` Chirurgie | `MED-MORPH10` | Morphin 10 mg Ampullen | 8 (kritisch) |
| `ST01` Chirurgie | `MED-AMOXI1000` | Amoxicillin 1000 mg Tabletten | 32 (knapp) |
| `ST01` Chirurgie | `MED-NACL500` | NaCl 0,9 % 500 ml Infusion | 45 |
| `ST01` Chirurgie | `MED-IBU400` | Ibuprofen 400 mg Tabletten | 160 |
| `ST01` Chirurgie | `MED-PANTO40` | Pantoprazol 40 mg Tabletten | 60 |
| `ST02` Kardiologie | `MED-HEP5000` | Heparin 5.000 I.E. Ampullen | 28 (knapp) |
| `ST02` Kardiologie | `MED-FUROS40` | Furosemid 40 mg Tabletten | 20 (kritisch) |
| `ST02` Kardiologie | `MED-ENOX40` | Enoxaparin 40 mg Fertigspritzen | 30 |
| `ST02` Kardiologie | `MED-PANTO40` | Pantoprazol 40 mg Tabletten | 80 |
| `ST02` Kardiologie | `MED-INSACT` | Insulin Actrapid 10 ml Durchstechflasche | 10 |
| `ST03` Innere Medizin | `MED-IBU400` | Ibuprofen 400 mg Tabletten | 240 |
| `ST03` Innere Medizin | `MED-PCM500` | Paracetamol 500 mg Tabletten | 180 |
| `ST03` Innere Medizin | `MED-METAM500` | Metamizol 500 mg Tabletten | 18 (knapp) |
| `ST03` Innere Medizin | `MED-PANTO40` | Pantoprazol 40 mg Tabletten | 120 |
| `ST03` Innere Medizin | `MED-ENOX40` | Enoxaparin 40 mg Fertigspritzen | 6 (knapp) |
| `ST03` Innere Medizin | `MED-HEP5000` | Heparin 5.000 I.E. Ampullen | 35 |
| `ST03` Innere Medizin | `MED-NACL500` | NaCl 0,9 % 500 ml Infusion | 14 (knapp) |
| `ST03` Innere Medizin | `MED-AMOXI1000` | Amoxicillin 1000 mg Tabletten | 64 |
| `ST03` Innere Medizin | `MED-FUROS40` | Furosemid 40 mg Tabletten | 90 |
| `ST03` Innere Medizin | `MED-ONDAN4` | Ondansetron 4 mg Ampullen | 3 (kritisch) |
| `ST03` Innere Medizin | `MED-MORPH10` | Morphin 10 mg Ampullen | 25 |
| `ST03` Innere Medizin | `MED-INSACT` | Insulin Actrapid 10 ml Durchstechflasche | 12 |

## Verhalten wie SAP

- Antworten im OData-v2-Format: Listen als `{"d":{"results":[…]}}`, einzelne Objekte als `{"d":{…}}`.
- Mengen werden als String übertragen, Datumswerte als `/Date(<Millisekunden>)/`.
- Belegnummern beginnen bei `4900000001`.
- `MaterialDocumentHeaderText` wird auf 25 Zeichen gekürzt.
- Ein Beleg wird ganz oder gar nicht gebucht: Scheitert eine Position, ändert sich kein Bestand.
- `$filter`, `$select` und `$top` werden ignoriert; es kommen immer alle Bestände aller Lagerorte, sortiert nach Lagerort und Material.

Fehler kommen im SAP-Format mit HTTP-Status `400`:

```json
{"error":{"code":"M7/021","message":{"lang":"en","value":"Deficit of unrestricted-use stock 94 PC : MED-ENOX40 1710 ST03"}}}
```

| Code | Wann |
|---|---|
| `M7/021` | weniger Bestand vorhanden als gebucht werden soll |
| `STOCK_NOT_FOUND` | Material gibt es in Werk/Lagerort nicht |
| `UNSUPPORTED_MOVEMENT_TYPE` | Bewegungsart ist nicht `201` |
| `INVALID_QUANTITY` | Menge fehlt, ist keine Zahl oder ≤ 0 |
| `INVALID_DATE` | `PostingDate` nicht im Format `/Date(…)/` |
| `NO_ITEMS` | Beleg ohne Positionen |

## Tests

```bash
./mvnw test
```

Die Tests laufen gegen eine H2-In-Memory-Datenbank und brauchen weder Docker noch PostgreSQL.
Vor jedem Test werden die Startbestände neu eingespielt.

## Projektstruktur

```
src/main/java/de/hhn/sapmock/
  materialstock/      API_MATERIAL_STOCK_SRV
  materialdocument/   API_MATERIAL_DOCUMENT_SRV
  odata/              OData-Antwort- und Fehlerformat
src/main/resources/
  specs/              Original-API-Spezifikationen aus dem SAP Business Accelerator Hub
  samples/            Beispielantworten aus der SAP-Sandbox
```
