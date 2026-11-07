# Architettura

## Moduli (`settings.gradle.kts`)

| Modulo | Tipo | Package | Contenuto |
|---|---|---|---|
| `:app` | application | `it.appm.myunical` | `MainActivity`, `MyUnicalApplication` (avvio Koin), navigazione, `MainScreen` (tab), tema (`ui/theme/Color.kt`, `Theme.kt`, `Type.kt`), `di/KoinModules.kt` (use case) e `CoilModule.kt` |
| `:core:domain` | JVM puro | `it.fra.myunical.domain` | modelli, interfacce di repository e data source, use case. Nessuna dipendenza Android |
| `:core:network` | android lib | `it.fra.myunical.network` | Ktor + kotlinx.serialization: implementazioni dei data source remoti, DTO (`models/`), mapper DTO→domain, repository con cache in memoria |
| `:core:datastore` | android lib | `it.fra.myunical.datastore` | DataStore Preferences (credenziali, profilo, preferenze, provider orario) e asset (`assets/canteen/menu.json`) |
| `:core:ui` | android lib | `it.fra.myunical.ui` | componenti Compose condivisi, `ExtendedColors`/`Accent`, animazioni shared element, `Esse3Links`, formattazione date |
| `:features:*` | android lib | `it.fra.myunical.<feature>` | una per area: login, today (Home), career, schedule, taxes, news, directory, canteen |

Le feature dipendono solo da `:core:domain` e `:core:ui` (template: `features/taxes/build.gradle.kts`).
Non vedono network/datastore: parlano con use case o interfacce di repository del domain.

## Struttura di una feature

```
features/<x>/src/main/java/it/fra/myunical/<x>/
  di/Module.kt          val <x>Module = module { viewModelOf(::XViewModel) ... }
  models/               UiState (sealed interface Loading/Error/Loaded) e ui model
  mapper/               domain → ui model (funzioni di estensione)
  presentation/         XScreen.kt (Composable) + XViewModel.kt (StateFlow<UiState>)
```

## Dependency injection (Koin)

- `MyUnicalApplication.onCreate` carica: `networkModule`, `datastoreModule`, `domainModule`,
  `coilModule` e un modulo per feature.
- `domainModule` (in `app/.../di/KoinModules.kt`): use case `factoryOf`, tranne quelli con cache
  condivisa tra schermate, che sono `single` (`GetCareerDataUseCase`, `GetTaxSummaryUseCase`,
  `GetMonthlyScheduleUseCase`).
- `networkModule`: un `HttpClient` per servizio, con qualifier `HttpClients.ESSE3`, `ESSE3_IMAGES`,
  `UNICAL_SITE`, `UNICAL_STORAGE` (vedi `apis.md`). Repository `single`, perché tengono le cache.
- `datastoreModule`: un file DataStore per area (`auth_secure`, `user`, `user_info`,
  `schedule_providers`); `CanteenMenuRepositoryImpl` legge l'asset.

## Navigazione (Navigation 3)

- `app/.../ui/navigation/AppRoute.kt`: `sealed interface AppRoute : NavKey`, tutte `@Serializable`.
  `Main` contiene le tab (`MainTab`: HOME, CAREER, SCHEDULE) gestite da `MainScreen`.
- Le altre schermate sono route a pieno schermo: `ScheduleProviders`, `ScheduleProviderEditor(providerId?)`,
  `Taxes`, `News`, `NewsDetail(newsId)`, `Directory`, `ContactDetail(contactId)`,
  `CourseDetail(courseRowId)`, `Canteen`.
- `AppNavigation.kt`: back stack con `rememberNavBackStack`, `entry<AppRoute.X>`; le callback
  `onOpenX` scendono da `MainScreen` alle tab (es. `TodayScreen`).
- Transizioni in `NavTransitions.kt`; shared element tra card e schermate tramite
  `core/ui/animation/SharedElements.kt` (`LocalSharedTransitionScope`, `LocalSharedCornerRegistry`).
- Gli argomenti della route arrivano al ViewModel come parametro Koin (`koinViewModel { parametersOf(id) }`).

## Aggiungere una feature (checklist)

1. Modulo `features/<x>` (copia `build.gradle.kts` di taxes, cambia `namespace`), `include` in
   `settings.gradle.kts`, `implementation(project(":features:<x>"))` in `app/build.gradle.kts`.
2. Domain: modelli, interfaccia repository/data source, use case; registra lo use case in `domainModule`.
3. Implementazione in `core:network` o `core:datastore` e registrazione nel relativo modulo Koin.
4. Feature: UiState, mapper, ViewModel, Screen, `<x>Module` aggiunto in `MyUnicalApplication`.
5. Route in `AppRoute`, `entry` in `AppNavigation`, callback fino al punto d'ingresso.
6. Aggiorna `docs/claude/*.md`.

## Test

- Unit test JVM: `core/domain/src/test` (use case con fake dei data source: `GetCareerDataUseCaseTest`,
  `GetMonthlyScheduleUseCaseTest`, `DegreeRegulationFinderTest`, `GetNextLessonUseCaseTest`, `GetTaxSummaryUseCaseTest`,
  `GetNextMealUseCaseTest`, `CourseLookupTest`), `core/network/src/test` (mapper con JSON reali:
  Career, Tax, Directory, Syllabus), `core/datastore/src/test` (`CanteenMenuRepositoryTest`, che
  verifica anche che l'asset del menù sia completo).
- Aggiungendo metodi a un'interfaccia di data source aggiorna i fake nei test, o la compilazione dei
  test fallisce.
- Nelle feature ci sono solo gli `ExampleUnitTest` del template.

## Trappole note

- Smart cast su proprietà pubbliche di classi di un altro modulo non funziona: copiale in una `val` locale.
- `AnnotatedString.fromHtml` per i testi HTML delle API (pagine dei corsi, notizie).
- Non inserire caratteri Unicode invisibili (U+2028 ecc.) nei sorgenti: usa gli escape.
