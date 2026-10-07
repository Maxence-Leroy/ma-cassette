# MaCassette

Android app to keep track of expenses. MVVM architecture, single `:app` Gradle module.

This document describes the standards **actually used** in this codebase today, so new code stays
consistent. It does not prescribe tooling or practices that aren't already in place — where
something is missing (tests, lint), that's noted explicitly rather than invented.

## Build

- Single module: `:app`. `namespace` / `applicationId` = `com.ragicorp.macassette`.
- `compileSdk = release(37)`, `minSdk = 24`, `targetSdk = 37`.
  Java target 11. Kotlin `2.4.20`, AGP `9.4.1`, Gradle `9.8.0`.
- AGP 9's built-in Kotlin support is used — there is no separate `org.jetbrains.kotlin.android`
  plugin applied (removed from both `build.gradle.kts` and `app/build.gradle.kts`);
  `org.jetbrains.kotlin.plugin.compose` is still applied explicitly since built-in Kotlin doesn't
  cover it. Other plugins: KSP (for Room) and `org.jlleitschuh.gradle.ktlint`.
- No product flavors. `release` build type uses a signing config loaded from
  `config/signing.properties` (gitignored) pointing at `app/wtc_key.jks`.
- **All dependencies and plugins go through the version catalog** (`gradle/libs.versions.toml`) —
  `app/build.gradle.kts` never declares inline coordinates or versions. Add new deps to the catalog
  first, then reference via `libs.*` / `libs.plugins.*`.

## Architecture

- Plain **MVVM**: `androidx.lifecycle.ViewModel` + Compose. No DI framework (no Hilt/Koin) —
  ViewModels are constructed manually.
- Package-per-feature under `app/src/main/java/com/ragicorp/macassette/`. Each feature package holds
  a `<Feature>Screen.kt` + `<Feature>ViewModel.kt` pair.
- Cross-cutting code: `ui/theme/`, `ui/views/` (shared composables), `utils/`.

## Room / persistence

- Room entities/DAOs/database live under a top-level package named after the aggregate (e.g.
  `operation/` holds `Operation`, `Category`, `OperationCategoryCrossRef`, `OperationDao`,
  `OperationDatabase`) — not nested under `screens/`.
- `OperationDatabase` is a singleton via a `getInstance(context: Context)` in its companion
  object (double-checked locking, `context.applicationContext`). Follow this pattern for any
  future Room database rather than introducing DI.
- Schema export is on (`room.schemaLocation` in `app/build.gradle.kts`); the generated JSON under
  `app/schemas/` is committed.
- **Never bump `@Database(version = ...)` as a side effect of adding/changing entities or columns
  — only bump it when explicitly asked to.** While nothing has shipped yet, entity changes just
  regenerate the same version's schema JSON in place. Once the app has shipped, an un-asked-for
  version bump would silently break migrations for existing installs, so this stays a human call
  either way.

## ViewModel conventions

- No constructor args needed → default `viewModel()` factory .
- Needs constructor args (nav params, callbacks) → define a nested factory named
  `<Feature>ViewModelFactory : ViewModelProvider.NewInstanceFactory()` inside the ViewModel class
  itself, wired as the Screen's default param:
- Needs `Application`/`Context` (e.g. `CredentialManager`, clipboard) → extend
  `AndroidViewModel(application)`.
- There is **no aggregated `UiState` data class** — each piece of state is its own property. Follow
  this per-property style rather than introducing a combined state object.

## Compose UI

- Material3 throughout, with `@OptIn(ExperimentalMaterial3Api::class)` where needed (bottom sheets,
  dropdowns, date pickers).
- Theming lives in `ui/theme/` (`Color.kt`, `Theme.kt`, `Type.kt`) — mostly stock Compose-template
  setup with `MaCassetteTheme` supporting dynamic color on API 31+.
- **Dimensions always come from `dimensionResource(R.dimen.*)`** — never hardcode `.dp` literals.
- **UI text always comes from `stringResource(R.string....)` in composables /
  `context.getString(...)` in ViewModels.** String resource keys follow `<feature>_<camelCaseName>`.
- Reusable composables: app-wide ones in `ui/views/`; feature-local ones in a `<feature>/views/`
  sub-package.

## Naming

- Files/classes/objects: PascalCase, file name matches the top-level type.
- Functions/properties: camelCase.
- Top-level constants: SCREAMING_SNAKE_CASE.

## Code style

- No wildcard imports; imports grouped/sorted AndroidX → `com.google.*` → `com.ragicorp.*` →
  `kotlinx`/`java`.
- Comments are sparse: short `//` notes for non-obvious platform workarounds or a source link, no
  KDoc blocks. Don't add doc comments unless explaining a genuine non-obvious constraint.
- Encapsulation idiom: `private val _x` backing field + public `val x` read-only exposure.
- **Before considering any task done, run `./gradlew ktlintCheck`** and fix any violations
  (`./gradlew ktlintFormat` can auto-fix most). A task isn't finished until this passes clean.

## Git conventions

- Commit subjects follow a lightweight Conventional-Commits style: `feat: ...`, `fix: ...`,
  `chore: ...`, short and imperative (2-6 words), no scopes, no body text (e.g.
  `feat: delete storage element`, `fix: changing level 1 remove level 2`).
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

When driving the app via `adb` to verify a change, find tap coordinates from the UI hierarchy rather
than eyeballing raw screenshots:

```
adb shell uiautomator dump /sdcard/window_dump.xml
adb shell cat /sdcard/window_dump.xml | grep -o 'text="Some Label"[^/]*bounds="[^"]*"'
```

Use the `bounds` rectangle to compute the tap center. Screenshots (`adb exec-out screencap -p`) are
still useful for visually confirming the result after tapping, but not for locating elements to tap.
