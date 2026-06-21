# QuickPDF Hub — Build Progress

## Status: Phase 1 complete ✅ | Phase 2 not started

---

## Design Tokens

### Color Palette

| Token | Hex | Usage |
|-------|-----|-------|
| `BrandTeal` (Primary) | `#2D7168` | Buttons, icons, badges, checkmarks, progress ring, selected borders |
| `BrandTealLight` | `#4A9E94` | Primary in dark mode |
| `TealContainer` | `#C8EDE8` | Icon backgrounds (empty/permission/result states), selected card tint |
| `OnTealContainer` | `#002B26` | Icons inside teal containers |
| `BackgroundLight` | `#F5F5F5` | App background (light) |
| `SurfaceLight` | `#FFFFFF` | Cards, top bar, list rows (light) |
| `SurfaceVariantLight` | `#ECECEC` | Chip backgrounds, secondary cards (light) |
| `OnSurfaceVariantLight` | `#666666` | Subtitles, secondary text (light) |
| `BackgroundDark` | `#111111` | App background (dark) |
| `SurfaceDark` | `#1E1E1E` | Cards, top bar (dark) |
| `SurfaceVariantDark` | `#2A2A2A` | Chip backgrounds (dark) |
| `OnSurfaceVariantDark` | `#AAAAAA` | Secondary text (dark) |
| `ErrorRed` | `#E53935` | Error icon, PDF icon accent |
| `ErrorContainer` | `#FFEBEE` | Error icon background |
| `PdfIconRed` | `#E53935` | PDF file icon color |
| `PdfIconRedBg` | `#FCE8E8` | PDF file icon background |
| `BannerBgLight` | `#E8F5F3` | "Processed on your device" banner |
| `BannerTextLight` | `#2D7168` | Banner text/icon |

> **Note:** Hex values estimated from compressed screenshots. Adjust `Color.kt` if you have exact brand hex codes.

### Typography

| Role | Weight | Size | Usage |
|------|--------|------|-------|
| `displayLarge` | ExtraBold | 30sp | App name on Splash |
| `headlineMedium` | Bold | 24sp | Screen titles (PDF ready, Processing…) |
| `headlineSmall` | Bold | 18sp | Section headers (Recent files, More tools) |
| `titleLarge` | SemiBold | 20sp | Top bar title |
| `titleMedium` | SemiBold | 15sp | Card titles, compression option labels |
| `titleSmall` | SemiBold | 14sp | File names, settings row labels |
| `bodyLarge` | Regular | 16sp | Descriptions, settings rows |
| `bodyMedium` | Regular | 14sp | Subtitles, body copy |
| `bodySmall` | Regular | 12sp | Metadata (size, date, operation) |
| `labelMedium` | Medium | 13sp | Compact card text |
| `labelSmall` | Medium | 11sp | Banner text, chip labels |

Font family: system default (Roboto). No custom typeface in designs.

### Spacing

| Context | Value |
|---------|-------|
| Screen horizontal padding | 16 dp |
| Card corner radius | 12–16 dp |
| Button corner radius | 28 dp (pill) |
| Tool grid gap | 12 dp |
| Section gap | 24 dp |
| List row vertical padding | 12 dp |
| Page badge diameter | 22 dp |
| Top bar icon | 24 dp |
| App badge size (top bar) | 32 dp |

---

## Architecture

```
app/src/main/java/com/mobiapps/quickpdfhub/
├── MainActivity.kt                    # Single Activity, Compose entry point
├── navigation/
│   ├── NavRoutes.kt                   # Route constants + ToolType enum
│   └── NavGraph.kt                    # NavHost with all composable destinations
├── data/
│   └── MockData.kt                    # Hardcoded mock files & page counts
└── ui/
    ├── theme/
    │   ├── Color.kt                   # All color tokens
    │   ├── Type.kt                    # Typography scale
    │   └── Theme.kt                   # Light/Dark MaterialTheme
    ├── components/
    │   ├── QuickPdfTopBar.kt          # Shared top app bar + AppIconBadge
    │   └── RecentFileRow.kt           # RecentFileRow (list) + RecentFileCard (scroll)
    └── screens/
        ├── SplashScreen.kt
        ├── HomeScreen.kt
        ├── ToolEntryScreen.kt
        ├── PageThumbnailScreen.kt
        ├── CompressOptionsScreen.kt
        ├── ProcessingScreen.kt
        ├── ResultScreen.kt
        ├── RecentFilesScreen.kt
        ├── SettingsScreen.kt
        ├── UpgradeScreen.kt
        ├── ErrorScreen.kt
        └── PermissionRationaleScreen.kt
```

---

## Phase 1 — Screen Checklist

| # | Screen | File | Status | Notes |
|---|--------|------|--------|-------|
| 1 | Splash | `SplashScreen.kt` | ✅ Done | Auto-advances after 2 s via LaunchedEffect |
| 2 | Home (light + dark) | `HomeScreen.kt` | ✅ Done | 2-col tool grid, collapsible More Tools, horizontal recent scroll |
| 3 | Tool Entry | `ToolEntryScreen.kt` | ✅ Done | Reusable; copy driven by ToolType enum |
| 4 | Page Thumbnail / Reorder | `PageThumbnailScreen.kt` | ✅ Done | 3-col lazy grid, page badges, checkmark toggle |
| 5 | Compress Options | `CompressOptionsScreen.kt` | ✅ Done | 3 selectable rows + Advanced slider |
| 6 | Processing | `ProcessingScreen.kt` | ✅ Done | Circular progress + doc icon; auto-advances after 2.5 s |
| 7 | Result / Success | `ResultScreen.kt` | ✅ Done | Before/after sizes, 3-button action row |
| 8 | Recent Files | `RecentFilesScreen.kt` | ✅ Done | Filter chips, grouped by date |
| 9 | Settings | `SettingsScreen.kt` | ✅ Done | Icon-labelled list with chevrons |
| 10 | Upgrade / Pro | `UpgradeScreen.kt` | ✅ Done | Feature comparison table + CTA |
| 11 | Empty State | `HomeScreen.kt` | ✅ Done | Embedded in HomeScreen when recentFiles is empty |
| 12 | Error State | `ErrorScreen.kt` | ✅ Done | Red X icon + Try again button |
| 13 | Permission Rationale | `PermissionRationaleScreen.kt` | ✅ Done | Lock icon + Continue button |
| — | Onboarding | — | ⏭ Skipped | User decision: not needed now |

---

## Navigation Flow (Phase 1 click-through)

```
Splash (2 s) ──► Home
Home ──► Tool Entry (merge/split/compress/pdf_to_jpg/jpg_to_pdf/rotate/reorder/delete/extract)
Tool Entry (merge/split/rotate/reorder/delete/extract) ──► Page Thumbnail
Tool Entry (compress) ──► Compress Options ──► Processing
Tool Entry (pdf_to_jpg / jpg_to_pdf) ──► Processing
Page Thumbnail ──► Processing
Processing (2.5 s auto) ──► Result
Result "Do another action" ──► Home
Settings ──► Upgrade (via "Upgrade to Pro" row)
Home gear icon ──► Settings
Any screen gear icon ──► Settings
```

---

## Judgment Calls Logged

| # | Decision | Rationale |
|---|----------|-----------|
| 1 | Onboarding skipped | User confirmed not needed |
| 2 | Dark mode applied uniformly | Only Home + Result had explicit dark designs; all other screens use the same dark scheme |
| 3 | ToolEntryScreen reused for all 9 tools | Parameterised by `ToolType` enum (label, subtitle, CTA label) |
| 4 | "More tools" defaults to expanded | Chevron toggles collapse; starts open so tools are discoverable |
| 5 | All 9 page thumbnails start checked | Matches design where all pages show a checkmark |
| 6 | Share button is primary (wide), Save+Open secondary | Inferred from design — Share is the main post-action |
| 7 | No bottom navigation bar | None visible in any design screen |
| 8 | Processing auto-advances after 2.5 s | Prototype behaviour; Phase 2 will tie this to real work completion |
| 9 | Primary color `#2D7168` | Best estimate from screenshots — update `Color.kt` BrandTeal if exact brand hex differs |
| 10 | `kotlin-android` plugin removed | AGP 9.1.1 bundles Kotlin internally; applying `org.jetbrains.kotlin.android` explicitly caused "extension already registered" crash — only `kotlin-compose` is applied |

---

## Phase 2 — Functionality Roadmap (not started)

| # | Feature | Library / API |
|---|---------|---------------|
| 1 | File picker | Storage Access Framework (`ActivityResultContracts.OpenDocument`) |
| 2 | Merge PDF | PdfBox-Android (Apache 2.0) |
| 3 | Split / Reorder / Delete pages | PdfBox-Android |
| 4 | Compress | Bitmap re-compression matching 3 quality presets |
| 5 | PDF → JPG / JPG → PDF | PdfRenderer (system) + BitmapFactory |
| 6 | Recent files persistence | Room |
| 7 | Share | `Intent.ACTION_SEND` via Android share sheet |
| 8 | Settings persistence | DataStore Preferences |
| 9 | Storage permission flow | `ActivityResultContracts.RequestPermission` |
| 10 | Error handling | Real states for corrupted / unsupported / oversized files |

---

## Dependency Versions

| Library | Version |
|---------|---------|
| AGP | 9.1.1 |
| Kotlin | 2.1.0 |
| Compose BOM | 2024.12.01 |
| Navigation Compose | 2.8.9 |
| Activity Compose | 1.10.1 |
| Material Icons Extended | (via BOM) |
| compileSdk | 36 (release, minorApiLevel = 1) |
| minSdk | 24 |
| targetSdk | 36 |
