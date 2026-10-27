# MaCassette

Android app to keep track of expenses. MVVM architecture, single `:app` Gradle module.

This document describes the standards **actually used** in this codebase today, so new code stays
consistent. It does not prescribe tooling or practices that aren't already in place — where
something is missing (e.g. there are no tests), that's noted explicitly rather than invented.

## Build

- Single module: `:app`. `namespace` / `applicationId` = `com.ragicorp.macassette`.
- `compileSdk = release(37)`, `minSdk = 24`, `targetSdk = 37`.
  Java target 11. Kotlin `2.4.21`, AGP `9.4.1`, Gradle `9.8.2`.
- AGP 9's built-in Kotlin support is used — there is no separate `org.jetbrains.kotlin.android`
  plugin applied (removed from both `build.gradle.kts` and `app/build.gradle.kts`);
  `org.jetbrains.kotlin.plugin.compose` and `org.jetbrains.kotlin.plugin.serialization` are still
  applied explicitly since built-in Kotlin doesn't cover those. Other plugins: KSP (for Room) and
  `org.jlleitschuh.gradle.ktlint`.
- No product flavors. `release` build type uses a signing config loaded from
  `config/signing.properties` (gitignored) pointing at `app/wtc_key.jks`.
- **All dependencies and plugins go through the version catalog** (`gradle/libs.versions.toml`) —
  `app/build.gradle.kts` never declares inline coordinates or versions. Add new deps to the catalog
  first, then reference via `libs.*` / `libs.plugins.*`.
- Compose libraries take their versions from the Compose BOM, **except material3**, which is pinned
  to the pre-release `1.5.0-beta02` via `material3` in the catalog because `FloatingActionButtonMenu`
  (used by `HomeFabMenu`) only exists in the 1.5.0 pre-releases. Once a Compose BOM ships a material3
  version that includes `FloatingActionButtonMenu`, bump the BOM, then remove the `material3`
  version entry and the `version.ref = "material3"` on `androidx-compose-material3` so it goes back
  to being BOM-managed.

## Architecture

- Plain **MVVM**: `androidx.lifecycle.ViewModel` + Compose. No DI framework (no Hilt/Koin) —
  ViewModels are constructed manually.
- Package-per-feature under `app/src/main/java/com/ragicorp/macassette/screens/`. Each feature
  package holds a `<Feature>Screen.kt`, plus its ViewModel when the screen has state.
- A bottom sheet with its own form gets its own sub-package inside the feature that opens it, with a
  `<Name>BottomSheet.kt` + `<Name>ViewModel.kt` pair (e.g. `screens/home/addoperation/`,
  `screens/settings/categoryform/`).
- Cross-cutting code: `ui/theme/`, `ui/views/` (shared composables), `utils/`.
- Navigation uses **Navigation Compose with type-safe routes**: each destination is a
  `@Serializable` route (`data object` for no args, `data class` for args) declared next to its
  screen (e.g. `Home` in `HomeScreen.kt`), registered with `composable<Route> { ... }` in the
  `NavHost` in `MainActivity`. Don't use string routes.

## Room / persistence

- Persistence lives under a top-level package named after the aggregate — not nested under
  `screens/`. Its root holds only the domain models and the repository (e.g. `operation/` holds
  `Operation`, `Category` and `OperationRepository`); everything Room-specific (entities, DAOs,
  type converters, the database) goes in a `db/` sub-package (e.g. `operation/db/` holds
  `CategoryEntity`, `OperationEntity`, `Converters`, `OperationDao`, `OperationDatabase`).
- Each domain model has a matching `<Model>Entity` in `db/`, with mapping functions next to the
  entity (e.g. `CategoryEntity.toCategory()` / `Category.toEntity()`). UI code and ViewModels only
  see domain models; the repository maps to/from entities before calling the DAO.
- `OperationDatabase` is a singleton via a `getInstance(context: Context)` in its companion
  object (double-checked locking, `context.applicationContext`). Follow this pattern for any
  future Room database rather than introducing DI.
- ViewModels never touch DAOs directly — they go through a repository in the same aggregate
  package (e.g. `OperationRepository` wrapping `OperationDao`). Repositories are plain classes
  constructed with a `Context` (not singletons) — only the database itself is a singleton.
- Schema export is on (`room.schemaLocation` in `app/build.gradle.kts`); the generated JSON under
  `app/schemas/` is committed.
- **Never bump `@Database(version = ...)` as a side effect of adding/changing entities or columns
  — only bump it when explicitly asked to.** While nothing has shipped yet, entity changes just
  regenerate the same version's schema JSON in place. Once the app has shipped, an un-asked-for
  version bump would silently break migrations for existing installs, so this stays a human call
  either way.
- Renaming an entity class changes its default table name (and therefore the schema). When that
  isn't intended, pin the old name with `@Entity(tableName = ...)`.

## ViewModel conventions

- No constructor args needed → default `viewModel()` factory, wired as the Screen's / BottomSheet's
  default `viewModel` param.
- Needs `Application`/`Context` (e.g. `Preferences`, constructing a repository) → extend
  `AndroidViewModel(application)`. This is what every existing ViewModel does.
- Needs other constructor args (nav params, callbacks) → define a nested factory named
  `<Feature>ViewModelFactory : ViewModelProvider.NewInstanceFactory()` inside the ViewModel class
  itself, wired as the Screen's default param. (No ViewModel needs this yet.)
- A bottom sheet's ViewModel is scoped to the sheet so its form starts empty each time it opens:
  wrap the sheet in `CompositionLocalProvider(LocalViewModelStoreOwner provides
  rememberViewModelStoreOwner())` where it's shown (see `HomeScreen` / `CategoriesSetting`).
- There is **no aggregated `UiState` data class** — each piece of state is its own property. Follow
  this per-property style rather than introducing a combined state object.
- A `Flow` from a repository is converted to a `StateFlow` in the ViewModel with
  `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), <initial>)` and exposed as-is
  as a public `val x: StateFlow<T>` (no `_x` backing field, no `init { collect { } }` into a
  `mutableStateOf`). The screen reads it with `val x by viewModel.x.collectAsStateWithLifecycle()`.

## Compose UI

- Material3 throughout, with `@OptIn(ExperimentalMaterial3Api::class)` where needed (bottom sheets,
  dropdowns, date pickers).
- Theming lives in `ui/theme/` (`Color.kt`, `Theme.kt`, `Type.kt`) — mostly stock Compose-template
  setup with `MaCassetteTheme` supporting dynamic color on API 31+.
- **Dimensions always come from `dimensionResource(R.dimen.*)`** — never hardcode `.dp` literals.
  Dimen names are snake_case (e.g. `single_space`, `double_space`, `color_swatch`).
- **UI text always comes from `stringResource(R.string....)` in composables /
  `context.getString(...)` in ViewModels.** String resource keys follow `<feature>_<camelCaseName>`.
- Reusable composables: app-wide ones in `ui/views/`; feature-local ones in a `<feature>/views/`
  sub-package.

## Naming

- Files/classes/objects: PascalCase, file name matches the top-level type.
- Functions/properties: camelCase.
- Top-level constants: SCREAMING_SNAKE_CASE.

## Code style

- No wildcard imports; imports are in a single lexicographically sorted block (ktlint's default
  layout: `java.*`, `javax.*` and `kotlin.*` go last). `./gradlew ktlintFormat` fixes the order.
- **No default values on data class constructor params** (domain models and entities), except the
  `id` (`= 0` for auto-generated keys) and optional fields (nullable, `= null`). Required values —
  booleans included — must be passed explicitly at every call site.
- Comments are sparse: short `//` notes for non-obvious platform workarounds or a source link, no
  KDoc blocks. Don't add doc comments unless explaining a genuine non-obvious constraint.
- Encapsulation idiom: `private val _x` backing field + public `val x` read-only exposure.
- **Before considering any task done, run `./gradlew ktlintCheck`** and fix any violations
  (`./gradlew ktlintFormat` can auto-fix most). A task isn't finished until this passes clean.

## Git conventions

- Commit subjects follow a lightweight Conventional-Commits style: `feat: ...`, `fix: ...`,
  `refactor: ...`, `chore: ...`, short and imperative (2-6 words), no scopes, no body text (e.g.
  `feat: delete storage element`, `fix: changing level 1 remove level 2`).
- Commits made by Claude **always** end with a `Co-Authored-By:` trailer for the Claude model
  (e.g. `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`), after a blank line. This
  trailer is the only exception to "no body text".
- `refactor:` is for code changes that don't change behavior (e.g. restructuring state); `chore:`
  is only for tooling, config and build housekeeping.
- No `.github/` CI workflows, no CONTRIBUTING.md, no CODEOWNERS.
- **Never run `git commit` unless the user explicitly asks for a commit in that specific turn.**
  Finishing a task, fixing a bug, or the user saying "looks good"/confirming a change works are NOT
  requests to commit — implement/verify the change and stop; wait for an explicit "commit this"
  before running `git commit`. This applies per-change: an earlier "commit this" does not authorize
  committing a later, different change.

## Secrets & config

- `config/` (including `signing.properties`) is gitignored — keep it that way.
- **Known discrepancy**: the release keystore `app/wtc_key.jks` is *not* gitignored and is committed
  to the repository (introduced in `chore: signing config`). This is a pre-existing risk, flagged
  here rather than silently fixed — don't assume it's safe to treat keystores as trackable elsewhere
  in the project.

## Running

Avoid running on an emulator, always prefer a real device (either from USB or adb Wi-Fi)

**After a Room schema change without a `@Database(version = ...)` bump** (i.e. the schema JSON
under `app/schemas/` changed but the version didn't), the database already on the device no longer
matches and the app will crash when opening it. Clear the app data before testing:

```
adb shell pm clear com.ragicorp.macassette
```

This also wipes any test data and preferences on the device, so mention it to the user when you do it.

When driving the app via `adb` to verify a change, find tap coordinates from the UI hierarchy rather
than eyeballing raw screenshots:

```
adb shell uiautomator dump /sdcard/window_dump.xml
adb shell cat /sdcard/window_dump.xml | grep -o 'text="Some Label"[^/]*bounds="[^"]*"'
```

Use the `bounds` rectangle to compute the tap center. Screenshots (`adb exec-out screencap -p`) are
still useful for visually confirming the result after tapping, but not for locating elements to tap.
