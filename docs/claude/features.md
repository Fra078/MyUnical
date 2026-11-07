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
- `DepartmentNoticesSection`: ultimi 3 avvisi del dipartimento sotto il carosello (nascosta se il
  dipartimento è ignoto o non ci sono avvisi), "Vedi tutti" apre la scheda del dipartimento;
- `NewsCarousel`, `QuickLinkSection` (azioni rapide in `models/QuickLink.kt`, ognuna con un `Accent`:
  Esse3, Documenti = certificati Esse3 nel browser, Webmail, Biblioteche, Mappa; tutte aprono un Custom Tab);
- `ProfileSheet`: profilo, cambio carriera e voci di menu (`ProfileDestination`). Studente: Libretto,
  Appelli, Bacheca esiti, Piano di studi, regolamenti del corso, Tasse. Servizi: Documenti e certificati
  (Esse3), Diritto allo studio (pagina unical.it), Rubrica, Portale. Campus: Webmail, Mensa (schermata
  interna, `CANTEEN_LINK_ID`), Biblioteche, Mappa, Sito. Logout.
- Regolamenti del corso: `GetDegreeRegulationsUseCase` → `DegreeRegulations(cohort, latest)`; il
  ViewModel li carica al cambio di carriera. Le voci compaiono solo se pubblicati: "Regolamento in vigore"
  (ultimo) e "Regolamento della tua immatricolazione" (coorte), oppure una sola voce se coincidono.

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
`GetTaxSummaryUseCase` (singleton): addebiti per `stuId`, pagati/da pagare/scaduti. Il pagamento si fa su Esse3 (`Esse3Links.TAXES`).

## Notizie (`features/news`)
- Fonti (`NewsSource`): `University` (notizie con immagine), `Notices` (avvisi d'ateneo, brevi e
  senza immagine), `Department(host, noticesOnly)` (sito del dipartimento dello studente). Mappatura in
  `NewsRemoteDataSourceImpl`; `NewsRepositoryImpl` ha una cache di pagine per fonte.
- `NoticeModel.id` è una stringa col prefisso della fonte (`n<id contesto>` per le notizie d'ateneo,
  `s<content_id>` per la ricerca): i due spazi di numerazione sono diversi e possono collidere.
- `NewsListScreen`: chip Notizie · Avvisi · <sigla dipartimento> (la terza solo se il dipartimento è noto),
  un feed paginato per scheda che parte alla prima apertura. Voci senza immagine = riga compatta col sommario.
  `AppRoute.News(openDepartment)` apre direttamente la scheda del dipartimento.
- Dipartimento: `GetStudentDepartmentUseCase` (cds della carriera → `DepartmentCod` del record della
  coorte, altrimenti dell'anno corrente → `ricerca/departments/`); la sigla è l'host in maiuscolo
  (`desf.unical.it` → DESF), perché `DepartmentNameShort` è incoerente. `DepartmentFinder` tiene la lista.
- Dettaglio: HTML reso in Compose, "Apri sul sito" usa `link`.

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
