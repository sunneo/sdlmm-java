# MissileCmd3D Fix - Coordinate System Correction

## Problem
The MissileCmd3D implementation had two major issues:
1. **Reversed positions**: Missiles and enemies appeared in wrong positions (反過來的)
2. **Nothing visible**: Objects were not rendering visibly (看不到任何東西)

## Root Cause

### Coordinate System Mismatch
The Java implementation was using **screen pixel coordinates** while the reference C implementation (ref-sdlmm) uses **3D world coordinates**:

| Aspect | Screen Coordinates (WRONG) | World Coordinates (CORRECT) |
|--------|---------------------------|----------------------------|
| Y-axis direction | Y=0 at top, increases downward | Y up, like real world |
| Y range | 0 to 600 (pixels) | -3.0 (ground) to 15.0 (sky) |
| X range | 0 to 800 (pixels) | -10.0 to +10.0 (world units) |
| Scale | Pixels (large numbers) | World units (small numbers) |

## Fixes Applied

### 1. Camera Positioning
**Before (screen space):**
```java
camera.Position = new Vector3(400, 300, -800);  // Screen center
camera.Target = new Vector3(400, 300, 0);       // Screen center
```

**After (world space - matching ref-sdlmm):**
```java
// Spherical coordinates: camDist=25.0, camAngleX=0.4, camAngleY rotating
camera.Position = new Vector3(
    camDist * sin(camAngleY) * cos(camAngleX),
    camDist * sin(camAngleX),
    -camDist * cos(camAngleY) * cos(camAngleX)
);
camera.Target = new Vector3(0, 2, 0);  // World center, above ground
```

### 2. Enemy Missile Generation
**Before (screen space - REVERSED):**
```java
int sx = (int)(Math.random() * width);  // 0-800 pixels
int sy = 0;  // TOP of screen
int ty = build[targetIdx].top;  // BOTTOM area
```
Enemy missiles started at TOP (sy=0) and went DOWN - opposite of reality!

**After (world space - CORRECT):**
```java
float sx = (float)((Math.random() - 0.5) * 20.0f);  // -10 to +10
float sy = 15.0f;   // SKY (high up)
float ty = -2.0f;   // GROUND (low down)
```
Now missiles correctly come FROM sky TO ground!

### 3. Building Positions
**Before (screen pixels):**
```java
buildingMeshes[i].Position = new Vector3(
    (build[i].left + build[i].right) / 2,  // 0-800 range
    (build[i].top + build[i].bottom) / 2,   // 450-600 range
    0
);
```

**After (world units):**
```java
float spacing = 20.0f / MAX_BUILD;  // Divide world width
float startX = -10.0f + spacing / 2;
float buildX = startX + i * spacing;  // -10 to +10
float buildY = -2.0f;  // At ground level

buildingMeshes[i].Position = new Vector3(buildX, buildY, 0);
```

### 4. Our Missile Launch
**Before (screen space):**
```java
int sx = build[2].left + 32;  // Screen pixels
int sy = build[2].top;         // Screen pixels
float dx = ((float)(mx - sx)) / 50;  // Pixel-based velocity
```

**After (world space with screen-to-world mapping):**
```java
float launcherX = 0.0f;  // Center in world coords
float launcherY = -2.4f; // Ground level

// Map screen click to world coordinates
float tx = (mx / (float)width) * 20.0f - 10.0f;  // Screen X -> World X
float ty = 10.0f - (my / (float)height) * 13.0f;  // Screen Y -> World Y
```

### 5. Collision Detection
**Before (screen units):**
```java
if (distSqr < launchedMissile[j].r * launchedMissile[j].r)
```
Used screen pixel radius values (too large).

**After (world units):**
```java
float explRadius = launchedMissile[j].r * 0.3f;  // Scale to world
if (distSqr < explRadius * explRadius)
```

## World Coordinate System

### Reference Implementation (missilecmd3d.c)
```c
#define WORLD_WIDTH 20.0f     // X: -10.0 to +10.0
#define WORLD_DEPTH 5.0f      // Z: -2.5 to +2.5
#define GROUND_Y (-3.0f)      // Ground level

// Enemy spawn
sy = 15.0f;                    // Sky
ty = GROUND_Y + 1.0f;         // Ground (-2.0)

// Buildings
pos.y = GROUND_Y + 1.0f;      // -2.0

// Camera
camDist = 25.0f;
camAngleX = 0.4f;
```

### Java Implementation (now matches)
```java
float WORLD_WIDTH = 20.0f;    // X: -10.0 to +10.0
float WORLD_DEPTH = 5.0f;     // Z: -2.5 to +2.5
float GROUND_Y = -3.0f;       // Ground level

// Enemy spawn
float sy = 15.0f;              // Sky
float ty = GROUND_Y + 1.0f;   // Ground (-2.0)

// Buildings
float buildY = GROUND_Y + 1.0f; // -2.0

// Camera
float camDist = 25.0f;
float camAngleX = 0.4f;
```

## Visual Comparison

### Before Fix
```
Screen Space (WRONG):
   Y=0 (Top)
     ↓ Enemy missiles going DOWN
   Y=300
     ↓
   Y=600 (Buildings at bottom)

Issues:
- Missiles came from TOP going to BOTTOM
- Used screen pixel values (0-800, 0-600)
- Camera at (400, 300, -800) - wrong scale
- Nothing visible due to coordinate mismatch
```

### After Fix
```
World Space (CORRECT):
   Y=15 (Sky)
     ↓ Enemy missiles descending
   Y=2 (Mid-air)
     ↓ Camera looking at this level
   Y=0
     ↓
   Y=-2 (Buildings on ground)
   Y=-3 (Ground plane)

Correct behavior:
- Missiles descend from SKY to GROUND
- Uses world units (-10 to +10, -3 to +15)
- Camera at proper spherical distance (25 units)
- Everything visible and properly scaled
```

## Testing Results

### Coordinate Verification
✅ Enemy missiles spawn at Y=15.0 (sky)  
✅ Enemy missiles target Y=-2.0 (ground)  
✅ Buildings positioned at Y=-2.0 (ground level)  
✅ Camera positioned at distance 25 units from origin  
✅ Camera target at (0, 2, 0) - world center  

### Compilation
✅ Code compiles without errors  
✅ All coordinate transformations syntactically correct  

### Expected Behavior
- Enemy missiles should be visible descending from above
- Buildings should be visible on the ground
- Our missiles should launch upward toward click position
- Camera should rotate around the scene
- Collisions should work properly

## Key Takeaways

1. **Screen coordinates ≠ World coordinates**: 2D screen pixels vs 3D world space
2. **Y-axis direction matters**: Screen Y goes down, World Y goes up
3. **Scale matters**: Pixels (100s) vs world units (10s)
4. **Camera positioning**: Must match the world scale, not screen scale
5. **Consistency**: All systems (enemies, missiles, buildings) must use same coordinate system

## Files Changed
- `src/sunneo/sdlmm/exams/MissileCmd3D.java`:
  - init3DRender(): Camera and mesh initialization
  - init_build(): Building positioning
  - generate_enermy(): Enemy spawn coordinates
  - generate_missile(): Mouse click to world mapping
  - update_enermy(): Collision detection in world space
  - update_missile(): Target detection in world space
  - render3D(): Camera and mesh positioning

Total changes: ~150 lines modified to implement correct coordinate system.
