# CLAUDE.md — Godot Noctua SDK

## Project Overview

A Godot Engine plugin bridging Godot games to the Noctua analytics, attribution, and revenue backend. It exposes event tracking, revenue tracking, session management, experiments, and network-state reporting via a unified GDScript interface backed by the Noctua Android SDK.

- **Godot**: 4.x
- **Platform**: Android (Java/AAR)
- **Android SDK**: `com.noctuagames.sdk:noctua-android-sdk:0.32.0`
- **License**: MIT

---

## Repository Layout

```
gd/                      # GDScript interface (developer-facing API)
  noctua.gd              # Autoload singleton — thin wrapper over the native layer
android-plugin/          # Android native implementation
  src/
    main/
      java/…/GodotNoctua.java   # Java plugin bridge (shared by both flavors)
      AndroidManifest.xml       # Shared manifest — permissions only
    godot3/
      AndroidManifest.xml       # Godot 3.x: org.godotengine.plugin.v1 meta-data
    godot4/
      AndroidManifest.xml       # Godot 4.x: org.godotengine.plugin.v2 meta-data
  build.gradle                  # Gradle build — godot3 / godot4 product flavors
  GodotNoctua.godot3.gdap       # Plugin descriptor for Godot 3.x projects (.gdap format)
  libs/
    godot3/                     # Place godot-lib-3.6.2.*.aar here (not committed)
    godot4/                     # Place godot-lib-4.x.x.*.aar here (not committed)
SConstruct               # SCons build script (legacy — unused, kept for reference)
scripts/                 # iOS build helpers (legacy — unused since iOS bridge removed)
```

> **Dual Godot version support**: The plugin builds two AARs via Gradle product flavors.  
> - `GodotNoctua.godot3Release.aar` — Godot 3.6.x, plugin v1 (`org.godotengine.plugin.v1`), minSdk 19  
> - `GodotNoctua.godot4Release.aar` — Godot 4.2+, plugin v2 (`org.godotengine.plugin.v2`), minSdk 22

---

## GDScript API (`gd/noctua.gd`)

The plugin auto-initialises as the **`noctua`** autoload singleton. No `AppToken` or manual `init()` call is needed — the SDK reads config from `noctuagg.json` automatically via the native layer.

```gdscript
# ── Event Tracking ────────────────────────────────────────────────────────────
noctua.track_event(event: String)
noctua.track_event_with_params(event: String, params: Dictionary)

# ── Revenue Tracking ──────────────────────────────────────────────────────────
noctua.track_revenue(event: String, revenue: float, currency := "USD")
noctua.track_purchase(order_id: String, amount: String, currency: String, payload: Dictionary)
noctua.track_ad_revenue(ad_source: String, revenue: String, currency: String, params: Dictionary)
noctua.track_custom_event_with_revenue(event_name: String, revenue: String, currency: String, payload: Dictionary)

# ── Session ───────────────────────────────────────────────────────────────────
noctua.set_session_tag(session_name: String)
noctua.get_session_tag() -> String
noctua.set_session_extra_params(params: Dictionary)

# ── Experiments ───────────────────────────────────────────────────────────────
noctua.set_experiment(experiment: String)
noctua.get_experiment() -> String
noctua.set_general_experiment(experiment: String)
noctua.get_general_experiment(key: String) -> String

# ── Network State ─────────────────────────────────────────────────────────────
noctua.on_online()
noctua.on_offline()
```

All calls are null-safe on desktop — methods silently no-op and return empty strings when the native plugin is not present.

---

## Android Build

### Prerequisites
- Android SDK (`compileSdkVersion 36`, `minSdkVersion 22`)
- JDK 17+ (Android Studio's JBR works)
- Godot 4.x `.aar` placed in `android-plugin/libs/` (download from [Godot releases](https://github.com/godotengine/godot/releases))
- Internet access for Maven dependencies

### Build commands

```bash
cd android-plugin

# Build both Godot versions at once
./gradlew assembleRelease
# → GodotNoctua.godot3Release.aar  (Godot 3.6.x)
# → GodotNoctua.godot4Release.aar  (Godot 4.x)

# Build a single flavor
./gradlew assembleGodot3Release
./gradlew assembleGodot4Release
```

If Android SDK is not on PATH, set it in `android-plugin/local.properties`:
```
sdk.dir=/Users/<you>/Library/Android/sdk
```

### Godot AAR setup (required before building)

Download the matching AAR from [Godot releases](https://github.com/godotengine/godot/releases) and place it in the correct `libs/` subfolder:

| Godot version | File to download | Destination |
|--------------|-----------------|-------------|
| 3.6.2 | `godot-lib-3.6.2.stable.template_release.aar` | `android-plugin/libs/godot3/` |
| 4.x | `godot-lib-4.x.x.stable.template_release.aar` | `android-plugin/libs/godot4/` |

### Android Dependencies

| Dependency | Scope | Notes |
|-----------|-------|-------|
| Godot AAR (`godot-lib*.aar`) | `compileOnly` | Not bundled — Godot provides it at build time |
| `com.noctuagames.sdk:noctua-android-sdk:0.32.0` | `implementation` | Noctua SDK via Maven Central |

### Key implementation notes

- `Noctua` is a Kotlin **`object`** singleton — always call via `Noctua.INSTANCE.method()` from Java. Never use `Noctua.Companion.*`.
- All Noctua SDK calls run on `activity.runOnUiThread()`.
- `toSafeMap()` converts a Godot `Dictionary` to `HashMap<String, Object>` with safe type coercion: `Double→Float`, `Integer→Long`.
- Methods exposed to GDScript must be annotated with `@UsedByGodot`.
- Revenue and amount values are passed as `String` from GDScript and parsed via `Double.parseDouble()` in Java.

---

## Architecture Patterns

| Pattern | Where |
|---------|-------|
| Kotlin object singleton | Android — `Noctua.INSTANCE.*` from Java |
| Autoload singleton | `noctua.gd` registered as `noctua` in project settings |
| Platform bridge | `GodotNoctua.java` translates GDScript calls to Noctua SDK |
| UI-thread dispatch | Every Android SDK call wrapped in `runOnUiThread()` |
| Auto-init from config | SDK reads `noctuagg.json` in `onMainCreate()` — no GDScript config needed |

---

## Common Tasks

### Add a new GDScript-exposed method

1. Add a Java method in `GodotNoctua.java` annotated `@UsedByGodot`.
2. Call the Noctua SDK inside `activity.runOnUiThread(() -> { ... })`.
3. Add a thin wrapper in `gd/noctua.gd` that delegates to `_noctua.<method>()`.
4. **Update this CLAUDE.md** — add the new method to the GDScript API table above.

### Update Noctua Android SDK version

1. Edit `android-plugin/build.gradle`:
   ```groovy
   implementation("com.noctuagames.sdk:noctua-android-sdk:<new_version>")
   ```
2. Update `export_plugin.gd` in the sample app's `addons/GodotNoctua/` — bump the version in `_get_android_dependencies()`.
3. Rebuild the AAR and copy to the sample app's `android/plugins/`.
4. **Update this CLAUDE.md** — bump the version in the header and dependencies table.

### Update Godot AAR version

Download the matching `godot-lib-<version>-template_release.aar` from the [Godot releases page](https://github.com/godotengine/godot/releases) and drop it in `android-plugin/libs/`.

---

## Maintenance — Keeping This File Current

**Update this CLAUDE.md whenever:**

| Change | Section to update |
|--------|------------------|
| New file or directory added | Repository Layout |
| File or directory removed | Repository Layout |
| New `@UsedByGodot` method added to `GodotNoctua.java` | GDScript API |
| Method removed or signature changed | GDScript API |
| Noctua Android SDK version bumped | Header · Android Dependencies |
| Godot AAR version changed | Android Build prerequisites |
| New build step or Gradle flag added | Android Build commands |

---

## Code Style

- **GDScript**: Godot style guide; `noctua.gd` is a thin pass-through — no business logic.
- **Java**: Standard Android conventions; all GDScript-facing methods must have `@UsedByGodot`.
- **Immutability**: Prefer creating new objects over mutating existing ones.
- **File size**: Keep files under 800 lines; extract utilities when approaching the limit.

---

## Testing

No automated test suite. Manual workflow:
1. Build the AAR (`./gradlew assembleRelease` in `android-plugin/`).
2. Copy the AAR into the sample app's `android/plugins/`.
3. Place `noctuagg.json` with `"sandboxEnabled": true` in the app root.
4. Run on a physical Android device.
5. Verify events appear in the Noctua dashboard.

---

## Config Files (never commit)

| File | Purpose |
|------|---------|
| `noctuagg.json` | Noctua SDK config — `clientId`, `gameId`, Noctua tokens, AdMob/AppLovin ad unit IDs, IAA/IAP settings |
| `google-services.json` | Firebase config — project number, OAuth client IDs, API key |
