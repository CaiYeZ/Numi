# Debug Session: app-boot-dex-permission

## Session Information
- **Session ID**: app-boot-dex-permission
- **Created**: 2026-05-20 17:12:48
- **Status**: [OPEN]
- **Platform**: Android (MIUI)
- **App Package**: com.herb.numi

## Problem Description
App is experiencing boot failure with MiSight reporting `exp_appbootfail` event. Log shows:
- Permission denied when accessing dex parent directory
- Missing odex/vdex optimization files
- Event triggered 3 times consecutively

## Key Observations

### Log Evidence
1. **2026-05-20 17:12:48.374**: MiSight event `exp_appbootfail` with package `com.herb.numi`, times=3
2. **2026-05-20 17:12:58.923**: `Dex parent of /data/app/... is not writable: Permission denied`
3. Multiple odex/vdex files not found in dalvik-cache

### Code Analysis
- **Application Class**: `NumiApplication.kt` with lazy initialization of database and repository
- **AndroidManifest.xml**: Properly registered with `.NumiApplication`
- **Target SDK**: 36 (Android 14+)
- **Min SDK**: 24

## Hypotheses

### H1: Android 14+ Dex2oat Permission Issue
**Description**: Android 14+ changed dex optimization permissions. Apps installed via ADB or from unknown sources may not have write access to dalvik-cache.
**Evidence Point**: `Permission denied` when ART tries to write odex/vdex files
**Falsifiable**: YES - Check if app works after clean reinstall from Play Store or proper package installer

### H2: Application.onCreate() Timeout
**Description**: Lazy initialization of Room database in onCreate() may cause ANR or boot timeout on slow devices.
**Evidence Point**: Multiple boot fail events (3 times)
**Falsifiable**: YES - Add logging to track onCreate() execution time

### H3: Native Library Loading Failure
**Description**: App may depend on native libraries (.so files) that fail to load, causing boot failure.
**Evidence Point**: MiSight specifically tracks native library loading failures
**Falsifiable**: YES - Check build.gradle for native dependencies

### H4: Insufficient Storage Space
**Description**: System may not have enough space to create odex/vdex optimization files.
**Evidence Point**: All odex/vdex files missing, not just one
**Falsifiable**: YES - Check device storage availability

### H5: MIUI-Specific Compatibility Issue
**Description**: MIUI's custom ART implementation may have compatibility issues with this app.
**Evidence Point**: Logcat specifically shows MiSight tracking
**Falsifiable**: YES - Test on stock Android device or emulator

## Instrumentation Plan
1. Add startup logging to `NumiApplication.onCreate()`
2. Add lifecycle logging to `MainActivity`
3. Start debug server to collect runtime evidence

## Next Steps
- [ ] Create debug server for log collection
- [ ] Add instrumentation to Application class
- [ ] Request user to reproduce issue
- [ ] Analyze collected logs
- [ ] Implement minimal fix
