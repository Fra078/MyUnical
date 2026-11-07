# MyUnical

App Android (Kotlin, Jetpack Compose Material 3 Expressive) per gli studenti dell'Università della
Calabria: carriera Esse3, orario lezioni, tasse, notizie, rubrica, menù della mensa.

Prima di analizzare il codice leggi i file in `docs/claude/`, scritti per evitare di riesplorare il
progetto:

- [`docs/claude/architecture.md`](docs/claude/architecture.md): moduli, livelli, DI Koin,
  navigazione, come aggiungere una feature, test.
- [`docs/claude/apis.md`](docs/claude/apis.md): servizi remoti (Esse3, storage.portale.unical.it,
  sito Unical), endpoint usati e loro stranezze.
- [`docs/claude/features.md`](docs/claude/features.md): cosa fa ogni schermata e dove sta la logica.
- [`docs/claude/ui.md`](docs/claude/ui.md): tema, palette, ruoli colore, componenti condivisi.
- Menù mensa: `menus/README.md` e la skill `.claude/skills/update-canteen-menu/SKILL.md`.

**Tieni aggiornati questi file**: quando un cambiamento rende falso o incompleto qualcosa che c'è
scritto (nuovo modulo, schermata, endpoint, route, convenzione, comando), aggiorna il file giusto
nello stesso commit. Scrivi solo ciò che non si ricava in fretta dal codice: struttura, motivi,
stranezze delle API, trappole già incontrate.

## Build e test

`gradlew` non è eseguibile e il JDK è quello di Android Studio:

```sh
export JAVA_HOME=/var/home/fra078/.local/share/JetBrains/Toolbox/apps/android-studio/jbr
sh ./gradlew :core:domain:test :core:datastore:testDebugUnitTest :core:network:testDebugUnitTest --offline
sh ./gradlew :app:assembleDebug --offline
```

Installazione sul telefono (Pixel, adb via TLS; il seriale IP non è stabile, controlla `adb devices`):

```sh
adb -s <device> install -r app/build/outputs/apk/debug/app-debug.apk
```

## Regole di lavoro

- Si lavora sul branch `feature/app-refactor`. **Committa ma non fare push**: il push lo fa l'utente.
- Non committare `.idea/compiler.xml` e `.idea/gradle.xml` (modifiche locali dell'utente).
- Messaggi di commit in inglese, all'imperativo ("Add the canteen menus"), che terminano con
  `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.
- Codice, commenti e KDoc in inglese; testi dell'interfaccia in italiano.
- L'utente comunica in italiano.
