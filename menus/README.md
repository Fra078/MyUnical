# Menù della mensa

L'app mostra i menù della mensa leggendo `core/datastore/src/main/assets/canteen/menu.json`, generato
dai PDF pubblicati da Unical sul [sito della mensa](https://www.unical.it/campus/vivere-il-campus/servizio-mensa/).

## Aggiornare i menù

1. Scarica il nuovo PDF (es. `Menu_Mense_Unical_dal_16_novembre_al_20_dicembre_2026.pdf`) in `menus/pdf/`,
   lasciando il nome originale: l'anno viene letto da lì.
2. Chiedi a Claude Code "aggiorna i menù della mensa" (oppure `/update-canteen-menu`), oppure a mano:

   ```sh
   python3 -m venv tools/canteen/.venv
   tools/canteen/.venv/bin/pip install -r tools/canteen/requirements.txt
   tools/canteen/.venv/bin/python tools/canteen/parse_menu.py menus/pdf/<file>.pdf
   ```

   Servono anche `pdftotext`, `pdftoppm` e `pdfinfo` (pacchetto poppler).
3. Controlla gli avvisi dello script e le pagine renderizzate in `menus/pages/` (non versionate).
4. Esegui `./gradlew :core:datastore:testDebugUnitTest` (verifica che il file sia completo), ricompila l'app e fai commit.

I giorni coperti dal nuovo PDF sostituiscono quelli già presenti; quelli più vecchi di 30 giorni prima
dell'inizio del nuovo file vengono rimossi.

## Correzioni automatiche

- Se in una pagina i contorni sono una copia dei secondi (errore nel PDF), i contorni di quella pagina vengono omessi.
- Se un piatto con carne o pesce nel nome è segnato come vegetariano/vegano, l'etichetta viene tolta.

Entrambe vengono riportate negli avvisi dello script.
