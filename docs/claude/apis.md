# Servizi remoti

Base URL in `core/network/.../di/Module.kt` (`Endpoints`). JSON comune: `ignoreUnknownKeys`,
`isLenient`, `coerceInputValues`; per campi sporchi esistono `LenientBooleanSerializer` e
`LenientDoubleSerializer`. Log HTTP solo in debug, senza header `Authorization`.

## Esse3 REST — `https://unical.esse3.cineca.it/e3rest/api/`

Autenticazione Basic con le credenziali salvate (`AuthLocalDataSource`), inviata solo all'host Esse3.
Il client di login (`AuthRemoteDataSourceImpl`) manda invece le credenziali da verificare.

| Endpoint | Uso | Data source |
|---|---|---|
| `login` | verifica credenziali, restituisce utente e carriere (`matId`, `stuId`, `cdsCode`...) | `AuthRemoteDataSourceImpl` |
| `libretto-service-v2/libretti/{matId}/righe` | righe del libretto (esami, moduli, voti) | `CinecaRemoteDataSourceImpl.fetchStudyPlan` |
| `.../{matId}/medie`, `.../{matId}/stats` | medie e statistiche | `fetchCareerOverview` |
| `.../{matId}/appelli`, `.../{matId}/prenotazioni` | appelli e prenotazioni | `fetchExamSessions`, `fetchBookings` |
| `tasse-service-v1/addebiti-studente` | tasse (per `stuId`) | `TaxRemoteDataSourceImpl` |
| foto profilo | URL assoluto via `getUserPhotoEndpoint(persId)`, caricato da Coil con `HttpClients.ESSE3_IMAGES` | |

- `adsceId` è l'id della riga del libretto ed è il `courseRowId` della route `CourseDetail`.
- Valori codificati Esse3 (stati, tipi attività LEZ/ESE/LAB...) in `network/mapper/Esse3Values.kt`.
- Le pagine web Esse3 aperte nel browser sono in `core/ui/.../Esse3Links.kt`.

## Storage portale — `https://storage.portale.unical.it/api/`

Pubblico, senza autenticazione, `expectSuccess = true` (un 404 lancia un'eccezione).
È la stessa API di corsi.unical.it e della rubrica.

| Endpoint | Uso |
|---|---|
| `ricerca/cds/` (`cdscod`, `academicyear`) | corsi di laurea (`findDegreeProgram`, `getAllDegreeCourses`) → `regulationId` (regdid). Un record per anno accademico; `DidacticRegulation` è il PDF del regolamento di quell'anno |
| `ricerca/cds/{regdid}/studyplans-activities/` | piano di studi per percorso e anno, con gli id delle attività, dei moduli e dei gruppi |
| `ricerca/cds-websites/{cdsCode}/timetable/` (`academic_year`, `year`, `date_year`, `date_month`) | orario lezioni del mese |
| `ricerca/activities/` (`name`, `academic_year`, `page_size`) | ricerca attività. **Il parametro `search` è ignorato: usare `name`** |
| `ricerca/activities/{id}/` | pagina del corso (syllabus) |
| `ricerca/addressbook/`, `ricerca/addressbook/{id}/` | rubrica |
| `ricerca/teachers/{id}/` (+ `researchlines/`, `publications/`) | profilo docente |

Regolamenti (`DidacticRegulation`): l'URL è senza schema (`//storage.portale.unical.it/media/portale/cds/regolamenti/<anno>/...pdf`)
e a volte ha spazi nel nome; si normalizza con `toAbsoluteUrl()` + `%20`. Gli anni recenti possono non avere
il record o il PDF: `DegreeRegulationFinder` cerca la coorte (e fino a 3 anni prima) e l'ultimo anno pubblicato.

Stranezze delle pagine dei corsi (`ricerca/activities/{id}/`):
- I contenuti sono in `StudyActivityContents`, per codice: OBIETT_FORM, CONTENUTI, PROGR_EST, PREREQ,
  PROPEDE, METODI_DID, MOD_VER_APPR, TESTI_RIF, STIMA_CAR_LAV, LINK_TEAMS, CODICE_TEAMS (mappati in
  `SyllabusMapper` → `SyllabusSectionKind`). Il testo è HTML.
- Le attività "padre" degli esami a moduli rispondono 404: le pagine esistono solo per i moduli e i
  gruppi (partizioni A/B...), che si trovano nel piano di studi della coorte.
- Le attività mutuate hanno pagina vuota e `StudyActivityBorrowedFrom` punta all'originale:
  `CourseCatalogRepositoryImpl.getSyllabus` la segue.
- Ricerca dei moduli di un esame (`CourseCatalogRepositoryImpl.findUnits`): prima il piano della coorte
  (`cdsCode` + anno di immatricolazione da `cohortYear()`), poi ricerca per nome nell'anno di frequenza
  e nell'anno corrente. `CourseLookup.unitsFor` fa il match per codice.
- Le carriere salvate da versioni vecchie non hanno `cdsCode`: `GetCourseDetailUseCase` rifà il login
  con le credenziali salvate e aggiorna il profilo.

## Sito Unical — `https://www.unical.it/api/`

- `news/by-context/1` (paginato): notizie (`NewsRemoteDataSourceImpl`).
- Pagine web aperte in Custom Tab: link rapidi in `features/today/.../models/QuickLink.kt`.

## Menù della mensa

Non c'è un'API: Unical pubblica PDF. Sono convertiti a mano in
`core/datastore/src/main/assets/canteen/menu.json` con `tools/canteen/parse_menu.py` (vedi
`menus/README.md` e la skill `update-canteen-menu`). `CanteenMenuRepository` è pronto per un download
remoto futuro.
