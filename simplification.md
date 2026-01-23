# Code Simplification Plan for AI_MultiBarcodes_Capture

## Objective
Improve code maintainability by reducing duplication, simplifying complex methods, and standardizing patterns across the codebase.

---

## Phase 1: Create BaseActivity (High Priority)

### Goal
Eliminate duplicate code across all 8+ activities.

### Changes
1. **Create `BaseActivity.java`** in `helpers/` package
   - Move common `attachBaseContext()` with locale handling
   - Add common `applyTheme()` call
   - Handle common toolbar setup pattern

2. **Modify all activities** to extend `BaseActivity`:
   - `CameraXLivePreviewActivity`
   - `CapturedBarcodesActivity`
   - `EntryChoiceActivity`
   - `BrowserActivity`
   - `AutoCaptureConditionsActivity`
   - `FilteringConditionsActivity`
   - `SettingsActivity`
   - `SplashActivity`

### Files to Modify
- Create: `helpers/BaseActivity.java`
- Edit: All activity files (remove duplicate methods)

---

## Phase 2: Consolidate Conditions System (High Priority)

### Goal
Reduce ~80% code duplication between AutoCapture and Filtering systems.

### Changes
1. **Create base classes:**
   - `BaseConditionsActivity.java` - common activity logic
   - `BaseConditionsPreferencesHelper.java` - generic preferences handling

2. **Refactor existing classes** to extend base:
   - `AutoCaptureConditionsActivity` extends `BaseConditionsActivity`
   - `FilteringConditionsActivity` extends `BaseConditionsActivity`
   - Both PreferencesHelpers use generic base

### Files to Modify
- Create: `conditions/BaseConditionsActivity.java`
- Create: `conditions/BaseConditionsPreferencesHelper.java`
- Edit: `autocapture/AutoCaptureConditionsActivity.java`
- Edit: `filtering/FilteringConditionsActivity.java`
- Edit: `autocapture/AutoCapturePreferencesHelper.java`
- Edit: `filtering/FilteringPreferencesHelper.java`

---

## Phase 3: Simplify Complex Methods (Medium Priority)

### Goal
Break down large methods into smaller, focused units.

### Changes
1. **Refactor `onDetectionResult()`** (145 lines → 5-6 smaller methods):
   - `processDetectedBarcodes()`
   - `applyDebounceLogic()`
   - `transformBoundingBoxes()`
   - `evaluateAutoCapture()`
   - `updateOverlayGraphics()`

2. **Consolidate settings loading** in `CameraXLivePreviewActivity`:
   - Merge 9 `loadXxxSettings()` methods into organized `loadAllSettings()`

3. **Create dialog builder** for `BrowserActivity`:
   - Extract common pattern from 6 dialog methods into reusable builder

### Files to Modify
- Edit: `CameraXLivePreviewActivity.java`
- Edit: `BrowserActivity.java`

---

## Phase 4: Standardize Patterns (Medium Priority)

### Goal
Improve code consistency.

### Changes
1. **Replace anonymous classes with lambdas** where applicable
2. **Standardize boolean comparisons**: Use `if (!condition)` everywhere
3. **Optimize ThemeHelpers.isLatinScript()**: Replace array + loop with `Set.of()`

### Files to Modify
- Edit: `CameraXLivePreviewActivity.java`
- Edit: `BrowserActivity.java`
- Edit: `helpers/ThemeHelpers.java`

---

## Phase 5: Remove Dead Code (Low Priority)

### Goal
Clean up unused code.

### Changes
1. Remove duplicate private `applyTheme()` methods (use `ThemeHelpers.applyTheme()`)
2. Remove unused imports
3. Review and remove unused constants

### Files to Modify
- Edit: `CameraXLivePreviewActivity.java`
- Edit: `CapturedBarcodesActivity.java`
- Edit: `BrowserActivity.java`

---

## Verification Plan

1. **Build verification**: `./gradlew assembleDebug` must succeed
2. **Manual testing**:
   - Launch app and navigate through all screens
   - Test barcode capture functionality
   - Test AutoCapture and Filtering conditions screens
   - Test file browser operations
   - Test settings changes persist correctly
   - Verify theme switching works
   - Verify language switching works
3. **Regression check**: Ensure no behavioral changes

---

## Estimated Impact

| Metric | Before | After (Est.) |
|--------|--------|--------------|
| Duplicate attachBaseContext() | 8 copies | 1 |
| Conditions Activity LOC | ~1200 combined | ~800 |
| onDetectionResult() size | 145 lines | ~30 lines (main method) |
| Boolean comparison style | Mixed | Consistent |

---

## Risk Mitigation

- Make incremental commits after each phase
- Test thoroughly after each phase before proceeding
- Keep all existing functionality intact
