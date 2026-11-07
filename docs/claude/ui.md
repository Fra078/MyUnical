# Tema e componenti

## Tema
- `app/.../ui/theme/Theme.kt`: `MaterialExpressiveTheme`, colori dinamici disattivati in favore della
  palette brand; fornisce `LocalExtendedColors` (chiaro/scuro).
- `app/.../ui/theme/Color.kt`: schema generato con `materialyoucolor` (Python) da sorgenti
  primary `#B30605` (rosso Unical), secondary `#00658F`, tertiary `#E09B2D`; neutri a chroma 2.5/6.
  Per rigenerarlo serve un venv con `materialyoucolor` (non nel repo).
- Le API Expressive sono sperimentali: `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` per
  `LoadingIndicator`, `ToggleButton`, `ButtonGroupDefaults`, forme `MaterialShapes`.

## Colori semantici (`core/ui/.../ui/theme/ExtendedColors.kt`)
- `ColorRole(color, onColor, container, onContainer)`.
- `MaterialTheme.extendedColors`: `success` (verde), `warning` (arancio), `info` (viola), non
  armonizzati col primario (l'armonizzazione spostava il warning verso il rosso).
- `enum Accent { PRIMARY, SECONDARY, TERTIARY, SUCCESS, WARNING, INFO, ERROR, NEUTRAL }` e `Accent.role`:
  le schermate scelgono il colore per significato, non per ruolo Material grezzo.
  Esempi d'uso: esito superato SUCCESS, scadenze WARNING, link Mensa INFO, linee della mensa
  (Classica PRIMARY, Vegetariana SUCCESS, Minestra WARNING, Piatto freddo INFO, Mediterranea SECONDARY).

## Componenti condivisi (`core/ui/.../ui/components`)
- `GroupedListItem` + `GroupedShapes`: liste a gruppi con angoli arrotondati solo agli estremi.
- `ShapeBadge`: icona dentro una forma Material colorata (per voci di lista e link rapidi).
- `EmptyState`, `CustomTabs` (apertura URL in Custom Tab).
- `format/RelativeDate.kt`: "oggi", "domani", date in italiano.
- `animation/SharedElements.kt`: shared element e angoli tra card della Home e schermate.
