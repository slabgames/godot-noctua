# CLAUDE.md — Godot Noctua SDK

## Project Overview

A Godot Engine plugin bridging Godot games to the Noctua analytics, attribution, and revenue backend. It exposes event tracking, revenue tracking, session management, experiments, and network-state reporting via a unified GDScript interface backed by platform-native implementations.

- **Godot**: 4.x (primary) · 3.3.0+ (legacy support via conditional compilation)
- **Platforms**: Android (Java/AAR) · iOS (ObjC++/XCFramework)
- **Android SDK**: `com.noctuagames.sdk:noctua-android-sdk:0.32.0`
- **License**: MIT

---

## Repository Layout

```
gd/                      # GDScript interface (developer-facing API)
  noctua.gd              # Autoload singleton — thin wrapper over the native layer
noctua/                  # iOS native implementation
  adjust.h / adjust.mm   # ObjC++ Godot ↔ Noctua iOS bridge (legacy filenames)
  adjust_module.h/.cpp   # Module registration (singleton lifecycle)
  adjust.gdip            # iOS plugin descriptor (xcframework + system frameworks)
  AdjustSdk.framework/   # Pre-built Noctua iOS framework (do not modify)
android-plugin/          # Android native implementation
  src/main/java/com/slabgames/noctua/GodotNoctua.java   # Java plugin bridge
  GodotNoctua.gdap       # Android plugin descriptor
  build.gradle           # Gradle build (produces AAR)
  libs/                  # Godot AAR (compile-only, not bundled in output)
scripts/                 # Shell build helpers for iOS
  generate_xcframework.sh
  generate_static_library.sh
  release_xcframework.sh
  release_static_library.sh
  extract_headers.sh
SConstruct               # SCons build script for iOS static libs / xcframework
noctua.json              # Plugin manifest (name, version, autoload, file mappings)
```

> **Note on iOS filenames**: The iOS bridge files (`adjust.h`, `adjust.mm`, `adjust.gdip`, `AdjustSdk.framework`) use legacy naming from the original fork. The framework is Noctua's iOS SDK — do not mistake the filename for a reference to a third-party dependency.

---

## GDScript API (`gd/noctua.gd`)

The plugin auto-initialises as the `adjust` autoload singleton. No `AppToken` or manual `init()` call is needed — the SDK reads config from `noctuagg.json` automatically via the native layer.

```gdscript
# ── Event Tracking ────────────────────────────────────────────────────────────
adjust.track_event(event: String)
adjust.track_event_with_params(event: String, params: Dictionary)

# ── Revenue Tracking ──────────────────────────────────────────────────────────
adjust.track_revenue(event: String, revenue: float, currency := "USD")
adjust.track_purchase(order_id: String, amount: String, currency: String, payload: Dictionary)
adjust.track_ad_revenue(ad_source: String, revenue: String, currency: String, params: Dictionary)
adjust.track_custom_event_with_revenue(event_name: String, revenue: String, currency: String, payload: Dictionary)

# ── Session ───────────────────────────────────────────────────────────────────
adjust.set_session_tag(session_name: String)
adjust.get_session_tag() -> String
adjust.set_session_extra_params(params: Dictionary)

# ── Experiments ───────────────────────────────────────────────────────────────
adjust.set_experiment(experiment: String)
adjust.get_experiment() -> String
adjust.set_general_experiment(experiment: String)
adjust.get_general_experiment(key: String) -> String

# ── Network State ─────────────────────────────────────────────────────────────
adjust.on_online()
adjust.on_offline()
```

The `adjust` singleton is null-safe on desktop (no native layer) — all methods silently no-op and return empty strings when the native plugin is not present.

---

## Android Build

### Prerequisites
- Android SDK (`compileSdkVersion 36`, `minSdkVersion 22`)
- JDK 17+ (Android Studio's JBR works)
- Godot 4.x `.aar` placed in `android-plugin/libs/` (download from Godot GitHub releases)
- Internet access for Maven dependencies (`mavenCentral`)

### Build commands

```bash
cd android-plugin
./gradlew assembleDebug    # → build/outputs/aar/GodotNoctua.debug.aar
./gradlew assembleRelease  # → build/outputs/aar/GodotNoctua.release.aar
```

If Android SDK is not on PATH, set it in `android-plugin/local.properties`:
```
sdk.dir=/Users/<you>/Library/Android/sdk
```

### Android Dependencies

| Dependency | Scope | Notes |
|-----------|-------|-------|
| Godot AAR (`godot-lib*.aar`) | `compileOnly` | Not bundled — Godot provides it at build time |
| `com.noctuagames.sdk:noctua-android-sdk:0.32.0` | `implementation` | Noctua SDK from Maven Central |

### Key implementation notes

- `Noctua` is a Kotlin **`object`** singleton — always call via `Noctua.INSTANCE.method()` from Java. Never use `Noctua.Companion.*`.
- All Noctua SDK calls run on `activity.runOnUiThread()`.
- `toSafeMap()` converts a Godot `Dictionary` to `HashMap<String, Object>` with safe type coercion: `Double→Float`, `Integer→Long`.
- Methods exposed to GDScript must be annotated with `@UsedByGodot`.
- Revenue and amount values are passed as `String` from GDScript and parsed via `Double.parseDouble()` in Java.

---

## iOS Build

### Prerequisites
- Xcode + command-line tools
- SCons (`pip install scons`)
- Godot source headers (extracted via `scripts/extract_headers.sh`)

### Build commands

```bash
# 1. Extract Godot headers
./scripts/extract_headers.sh

# 2. Build xcframework (preferred for distribution)
#    Usage: ./scripts/generate_xcframework.sh <plugin_name> <target> <godot_version>
./scripts/generate_xcframework.sh adjust release 4.0

# 3. Or build static library
./scripts/generate_static_library.sh adjust release 4.0

# 4. Release build (produces both release + release_debug variants)
./scripts/release_xcframework.sh adjust 4.0
```

### SCons parameters

| Parameter | Values | Default |
|-----------|--------|---------|
| `target` | `debug`, `release`, `release_debug` | `debug` |
| `arch` | `arm64`, `armv7`, `x86_64` | `arm64` |
| `simulator` | `yes`, `no` | `no` |
| `version` | `3.2`, `4.0` | `3.2` |
| `use_llvm` | `yes`, `no` | `no` |

### iOS System Framework Dependencies
`AdSupport`, `iAd`, `AdServices`, `CoreTelephony`, `StoreKit`, `AppTrackingTransparency`

---

## Architecture Patterns

| Pattern | Where |
|---------|-------|
| Kotlin object singleton | Android — `Noctua.INSTANCE.*` from Java |
| Autoload singleton | `noctua.gd` registered as `adjust` in project settings |
| Platform bridge (Android) | `GodotNoctua.java` translates GDScript calls to Noctua SDK |
| Platform bridge (iOS) | `adjust.mm` translates GDScript calls to Noctua iOS framework |
| UI-thread dispatch | Every Android SDK call wrapped in `runOnUiThread()` |
| Auto-init from config | SDK reads `noctuagg.json` in `onMainCreate()` — no GDScript config needed |
| Godot 3/4 compat | iOS bridge: conditional compilation via `#ifdef GDEXTENSION` |

---

## Common Tasks

### Add a new GDScript-exposed method (Android)

1. Add a Java method in `GodotNoctua.java` annotated `@UsedByGodot`.
2. Call the Noctua SDK inside `activity.runOnUiThread(() -> { ... })`.
3. Add a thin wrapper in `gd/noctua.gd` that delegates to `_noctua.<method>()`.

### Add a new GDScript-exposed method (iOS)

1. Declare in `noctua/adjust.h`, implement in `noctua/adjust.mm`.
2. Bind via `ClassDB::bind_method(D_METHOD(...), &AdjustSdk::method)` in `adjust.mm`.
3. Add a thin wrapper in `gd/noctua.gd`.

### Update Noctua Android SDK version

1. Edit `android-plugin/build.gradle`:
   ```groovy
   implementation("com.noctuagames.sdk:noctua-android-sdk:<new_version>")
   ```
2. Update `android-plugin/GodotNoctua.gdap` → `remote` array to match.
3. Rebuild the AAR and copy to the sample app's `android/plugins/`.

### Update Godot AAR version

Download the matching `godot-lib-<version>-template_release.aar` from the [Godot releases page](https://github.com/godotengine/godot/releases) and drop it in `android-plugin/libs/`.

---

## Code Style

- **ObjC++**: LLVM style enforced by `.clang-format` — 4-space indent, no column limit.
- **GDScript**: Godot style guide; `noctua.gd` is a thin pass-through — no business logic.
- **Java**: Standard Android conventions; all GDScript-facing methods must have `@UsedByGodot`.
- **Immutability**: Prefer creating new objects over mutating existing ones.
- **File size**: Keep files under 800 lines; extract utilities when approaching the limit.

---

## Testing

No automated test suite. Manual workflow:
1. Build the AAR (Android) or xcframework (iOS).
2. Copy the binary into the sample app (`godot-noctua-app`).
3. Place `noctuagg.json` with `"sandboxEnabled": true` in the app root.
4. Run on a device or emulator.
5. Verify events appear in the Noctua dashboard.

---

## Config Files (never commit)

| File | Purpose |
|------|---------|
| `noctuagg.json` | Noctua SDK config — `clientId`, `gameId`, Noctua tokens, AdMob/AppLovin ad unit IDs, IAA/IAP settings |
| `google-services.json` | Firebase config — project number, OAuth client IDs, API key |
