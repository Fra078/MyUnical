# Schermate e logica

Percorsi relativi a `features/<x>/src/main/java/it/fra/myunical/<x>/` salvo dove indicato.

## Login (`features/login`)
Credenziali Esse3 → `LoginUseCase` → `AuthRemoteDataSource.validateCredentials` (`login`). Salva
credenziali (`auth_secure`) e profilo con le carriere (`user_info`). Con più carriere l'utente sceglie;
la carriera corrente è in `UserPreferenceRepository` (`Get/SetCurrentCareerUseCase`).
`GetAuthStateUseCase` decide se mostrare login o `Main`.

## Home (`features/today`)
`TodayScreenViewModel` combina più sorgenti, ognuna con stato proprio:
- `UpcomingCommitmentHeroCard`: prossima lezione (`GetNextLessonUseCase`, dai provider orario) o appello;
- `CareerSnapshotCard`: media, CFU (`GetCareerDataUseCase`);
- `TuitionStatusCard`: tasse da pagare (`GetTaxSummaryUseCase`);
- `CanteenCard`: prossimo pasto (`GetNextMealUseCase`), apre la schermata Mensa;
- `NewsCarousel`, `QuickLinkSection` (link in `models/QuickLink.kt`, ognuno con un `Accent`;
  `CANTEEN_LINK_ID` apre la schermata interna invece del sito);
- `ProfileSheet`: profilo, cambio carriera, voci verso Tasse, Documenti, Rubrica, Mensa, logout.

## Carriera (`features/career`)
- `CareerScreen`: libretto con filtri (`ExamFilter`) e ordinamento (`ExamSortOption`), appelli e
  prenotazioni. Dati da `GetCareerDataUseCase` (singleton, cache per carriera, `refresh()` incrementa
  la generazione).
- `presentation/detail/CourseDetailScreen`: dettaglio esame del libretto (`AppRoute.CourseDetail(adsceId)`).
  Mostra subito i dati del libretto, poi carica la pagina pubblica del corso (`CatalogState`): fatti,
  carico di lavoro, docenti, sezioni HTML, link Teams. Esami a moduli/gruppi: chip per scegliere l'unità
  (`UnitOption`, etichette in `mapper/CourseDetailUiMapper.kt`). Logica di ricerca in `apis.md`.

## Orario (`features/schedule`)
- Un "provider" (`ScheduleProvider`) è un orario configurato dall'utente: corso di laurea (`cdsCode`,
  `regulationId`), percorso, insiemi di corsi seguiti (`TrackedCourse` con codici dei moduli). Salvati in
  DataStore (`ScheduleProviderRepositoryImpl`).
- `providers/` elenco, `editor/` creazione e modifica (usa `GetDegreeProgramsUseCase`,
  `GetStudyPathsUseCase` sullo storage), `schedule/` calendario mensile (`GetMonthlyScheduleUseCase`,
  singleton con cache per mese/anno di corso; filtra gli eventi per `trackedCodes`).
- Colori dei corsi: `components/CourseColor.kt`.

## Tasse (`features/taxes`)
`GetTaxSummaryUseCase` (singleton): addebiti per `stuId`, pagati/da pagare/scaduti. Le quietanze si
scaricano come documenti.

## Documenti (`features/documents`)
Verbali d'esame e quietanze in PDF (`GetStudentDocumentsUseCase`, `DownloadDocumentUseCase`), salvati
da `storage/DocumentStore.kt` in `cacheDir/documents` ed esposti con un `FileProvider`
(`<package>.documents`), poi aperti con `ACTION_VIEW` in un'app esterna.

## Notizie (`features/news`)
Lista paginata e dettaglio (`NewsRepository`, cache in memoria), HTML reso in Compose.

## Rubrica (`features/directory`)
Ricerca persone (`ricerca/addressbook/`), dettaglio contatto; per i docenti anche insegnamenti, linee di
ricerca e pubblicazioni (`detail/TeacherSections.kt`).

## Mensa (`features/canteen`)
- Dati: asset `core/datastore/src/main/assets/canteen/menu.json`, letto da `CanteenMenuRepositoryImpl`
  (DTO in `datastore/models/CanteenMenuDataModel.kt`, enum come stringhe, valori sconosciuti ignorati).
- Domain `model/canteen/CanteenMenu.kt`: giorno → pasti (LUNCH/DINNER) → portate (FIRST/SECOND/SIDE) →
  piatti con linea (CLASSIC, VEGETARIAN, SOUP, COLD, MEDITERRANEAN), tag dieta e `frozen`.
  `DietFilter.VEGETARIAN` accetta anche i tag VEGAN e la linea vegetariana.
- `GetNextMealUseCase`: pranzo fino alle 15:00, cena fino alle 21:30, poi pranzo del giorno dopo; salta
  i giorni senza menù.
- `CanteenScreen`: striscia dei giorni, toggle pranzo/cena, chip filtri dieta, portate con pill della
  linea e tag.
- Aggiornamento dei menù: skill `update-canteen-menu` (`menus/pdf/` → `tools/canteen/parse_menu.py`
  → controllo pagina per pagina con le immagini in `menus/pages/` → test → commit). Il parser segnala
  e corregge anomalie note dei PDF (contorni copiati dai secondi, carne/pesce marcati vegetariani).
