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
so beginnt jede Demo mit denselben Zahlen. Werk `1710`, Lagerort `ST03` (Station 3), Einheit `PC`.

| Material | Medikament | Bestand |
|---|---|---|
| `MED-IBU400` | Ibuprofen 400 mg Tabletten | 240 |
| `MED-PCM500` | Paracetamol 500 mg Tabletten | 180 |
| `MED-METAM500` | Metamizol 500 mg Tabletten | 18 (knapp) |
| `MED-PANTO40` | Pantoprazol 40 mg Tabletten | 120 |
| `MED-ENOX40` | Enoxaparin 40 mg Fertigspritzen | 6 (kritisch) |
| `MED-HEP5000` | Heparin 5.000 I.E. Ampullen | 35 |
| `MED-NACL500` | NaCl 0,9 % 500 ml Infusion | 14 (knapp) |
| `MED-AMOXI1000` | Amoxicillin 1000 mg Tabletten | 64 |
| `MED-FUROS40` | Furosemid 40 mg Tabletten | 90 |
| `MED-ONDAN4` | Ondansetron 4 mg Ampullen | 3 (kritisch) |
| `MED-MORPH10` | Morphin 10 mg Ampullen | 25 |
| `MED-INSACT` | Insulin Actrapid 10 ml Durchstechflasche | 12 |

## Verhalten wie SAP

- Antworten im OData-v2-Format: Listen als `{"d":{"results":[…]}}`, einzelne Objekte als `{"d":{…}}`.
- Mengen werden als String übertragen, Datumswerte als `/Date(<Millisekunden>)/`.
- Belegnummern beginnen bei `4900000001`.
- `MaterialDocumentHeaderText` wird auf 25 Zeichen gekürzt.
- Ein Beleg wird ganz oder gar nicht gebucht: Scheitert eine Position, ändert sich kein Bestand.
- `$filter`, `$select` und `$top` werden ignoriert; es kommen immer alle Bestände.

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
