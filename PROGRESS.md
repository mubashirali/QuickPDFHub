# QuickPDF Hub — Build Progress

## Status: Phase 1 complete ✅ | Phase 2 in progress 🔧 (Feature 2 done)

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
│   ├── MockData.kt                    # Hardcoded mock files & page counts
│   └── PdfWorkSession.kt              # In-memory op state: inputUris, compressionQuality, lastResult
├── domain/
│   ├── WorkResult.kt                  # sealed class Success / Error + formatBytes()
│   └── CompressPdf.kt                 # PdfRenderer → JPEG round-trip → PdfDocument
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

## Phase 2 — Implementation Plan (7 features, one at a time)

### Shared infrastructure (built once, used by all features)

| File | Purpose |
|------|---------|
| `data/PdfWorkSession.kt` | In-memory singleton holding current URIs across screens |
| `domain/PdfWorker.kt` | Coroutine-based processor; runs on `IO` dispatcher; emits `WorkResult` |
| `domain/WorkResult.kt` | `sealed class`: `Success(outputUri, sizeBefore, sizeAfter)` / `Error(message)` |

---

### Feature 1 — File Picker ✅ Done
**Status:** Complete  
**Approach:** `rememberLauncherForActivityResult` with `OpenDocument` (single) or `OpenMultipleDocuments` (merge). On result, URIs stored in `PdfWorkSession`, then navigate forward. No library needed — pure SAF.  
**Files changed:** `ToolEntryScreen.kt`, `data/PdfWorkSession.kt`  
**MIME types:** `application/pdf` for PDF tools, `image/*` for JPG→PDF

---

### Feature 2 — Compress PDF ✅ Done
**Approach:** `PdfRenderer` renders each page to `Bitmap` at DPI scaled by quality (2.0× Low / 1.5× Medium / 1.0× High). JPEG round-trip at quality int 90/65/35 bakes lossy compression into the bitmap before packing into `PdfDocument`. `ProcessingViewModel` (AndroidViewModel) runs the work in `viewModelScope`, stores `WorkResult.Success` in `PdfWorkSession.lastResult`. `ResultScreen` reads real before/after byte counts and formats them with `formatBytes()`. Share and Open buttons wired to real FileProvider URI.  
**Files:** `domain/WorkResult.kt`, `domain/CompressPdf.kt`, `ui/screens/ProcessingViewModel.kt`, `data/PdfWorkSession.kt` (compressionQuality + lastResult), `AndroidManifest.xml` (FileProvider), `res/xml/file_paths.xml`  
**Judgment call:** Android's `PdfDocument.writeTo()` does not guarantee JPEG internal encoding; file size reduction mainly comes from the reduced-resolution bitmap + JPEG round-trip information loss. Lossless PDFs (pure vector/text) will see minimal size reduction — this is a system API limitation without PdfBox.

---

### Feature 3 — PDF → JPG ⬜ Pending
**Approach:** Same `PdfRenderer` bitmap loop as Compress, but saves each page as a numbered `.jpg` to the app's cache dir. Result screen shows image count instead of before/after size.  
**Files:** `domain/PdfToJpg.kt`, wiring in `ProcessingScreen` + `ResultScreen`

---

### Feature 4 — JPG → PDF ⬜ Pending
**Approach:** `BitmapFactory.decodeStream` each selected image URI, draw onto `PdfDocument.Page` canvas sized to the bitmap, write to output file.  
**Files:** `domain/JpgToPdf.kt`, wiring in `ProcessingScreen` + `ResultScreen`

---

### Feature 5 — Merge PDF ⬜ Pending
**Approach:** Add `pdfbox-android` (Apache 2.0). `PDDocument.load()` each input URI, iterate pages, append to a new `PDDocument`, save. Multiple-file picker already wired from Feature 1.  
**Dependency:** `com.tom-roush:pdfbox-android:2.0.27.0`  
**Files:** `domain/MergePdf.kt`, wiring in `ProcessingScreen` + `ResultScreen`

---

### Feature 6 — Split / Delete / Reorder Pages ⬜ Pending
**Approach:** All three share the same `PDDocument` page-level API from PdfBox (Feature 5 dependency already present). Split: extract page index ranges into separate files. Delete: write all pages except deselected ones. Reorder: write pages in the order from `PageThumbnailScreen` drag state.  
**Files:** `domain/SplitPdf.kt`, `domain/DeletePages.kt`, `domain/ReorderPages.kt`, `PageThumbnailScreen.kt` (real drag-to-reorder)

---

### Feature 7 — Recent Files (Room) ⬜ Pending
**Approach:** `RecentFile` Room entity + DAO. Insert a row after every `WorkResult.Success`. Query on Home (last 4) and RecentFilesScreen (all, grouped by date). Replaces `MockData` everywhere.  
**Dependencies:** `room-runtime`, `room-ktx`, `room-compiler` (kapt/ksp)  
**Files:** `data/db/AppDatabase.kt`, `data/db/RecentFileDao.kt`, `data/db/RecentFileEntity.kt`, `data/RecentFileRepository.kt`

---

### Post-feature work (after all 7 are reviewed)
- **Share** — `Intent.ACTION_SEND` on Result screen Share button
- **Settings persistence** — DataStore for dark mode + default save path
- **Error handling** — real `WorkResult.Error` states surfaced to `ErrorScreen`

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
