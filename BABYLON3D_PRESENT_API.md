# Babylon3D Device.presentToScreen() API

## Overview
The `presentToScreen()` method is the primary API for displaying rendered content from the Babylon3D rendering pipeline to the screen.

## Problem Solved
Previously, applications had to manually copy the device backbuffer to the screen:
```java
// Old approach - direct access to backbuffer
drawPixels(m_device.backbuffer, 0, 0, SCREENX, SCREENY);
```

This approach had several issues:
- Mixed rendering pipeline logic with UI framework calls
- Required direct access to internal backbuffer
- Not encapsulated within the Babylon3D API
- Tight coupling between Device and application code

## New API

### Method Signature
```java
public void presentToScreen(SDLMMInterface screen)
```

### Parameters
- `screen` - The SDLMMInterface to draw to (typically the application window/frame)

### Usage Example
```java
// In your rendering loop
Device m_device = new Device(width, height);
Camera camera = new Camera();

// 1. Clear the buffers
m_device.clear();

// 2. Render your content
m_device.renderParticles(camera, positions, colors, count, size, texture, true);
// or
m_device.render(camera, meshes, lightPosition);

// 3. Present to screen using the new API
m_device.presentToScreen(this);  // 'this' is your SDLMMFrame or SDLMMInterface
```

## Benefits

### 1. Better Encapsulation
The Device class now handles its own presentation, keeping the rendering pipeline logic contained within Babylon3D.

### 2. Cleaner Code
```java
// Before
drawPixels(m_device.backbuffer, 0, 0, SCREENX, SCREENY);

// After
m_device.presentToScreen(this);
```

### 3. Consistent API
Matches the pattern used in other rendering frameworks where the device/context presents its content.

### 4. Performance
Uses optimized bulk copy operation (`drawPixels`) internally, maintaining the same performance as before.

### 5. Flexibility
The method accepts any `SDLMMInterface`, allowing rendering to different targets.

## Implementation Details

The method internally uses `drawPixels()` for optimal performance:
```java
public void presentToScreen(SDLMMInterface screen) {
    if (screen == null) {
        return;
    }
    // Use bulk copy operation for optimal performance
    screen.drawPixels(backbuffer, 0, 0, workingWidth, workingHeight);
}
```

## Migration Guide

### For Existing Code
If you have code that directly accesses the backbuffer:
```java
// Old code
int[] backbuffer = m_device.backbuffer;
for (int y = 0; y < height; y++) {
    for (int x = 0; x < width; x++) {
        drawPixel(x, y, backbuffer[y * width + x]);
    }
}
```

Or:
```java
// Old code
drawPixels(m_device.backbuffer, 0, 0, width, height);
```

Replace with:
```java
// New code
m_device.presentToScreen(this);
```

### Backward Compatibility
The backbuffer is still publicly accessible for advanced use cases or debugging:
```java
int[] buffer = m_device.backbuffer;  // Still works if needed
```

## Related Methods

### present()
The legacy `present()` method is still available for C compatibility but is now a no-op:
```java
m_device.present();  // Legacy method - does nothing
```

Consider using `presentToScreen()` instead.

## Example: Complete Rendering Loop
```java
public class My3DApp extends SDLMMFrame {
    private Device m_device;
    private Camera camera;
    private Mesh[] meshes;
    private Vector3 lightPosition;
    
    @Override
    public void run() {
        m_device = new Device(800, 600);
        camera = new Camera();
        // ... initialize other components
        
        while (running) {
            // Clear buffers
            m_device.clear();
            
            // Render 3D content
            m_device.render(camera, meshes, lightPosition);
            
            // Present to screen - ONE LINE!
            m_device.presentToScreen(this);
            
            // Draw 2D HUD overlay after presentation
            drawString("FPS: 60", 10, 10, 0xFFFFFFFF);
            
            flush();
            sleep(16);
        }
    }
}
```

## Performance Notes
- Uses the same optimized bulk copy as before
- No performance regression
- Single method call per frame instead of accessing backbuffer directly
- Better cache locality due to encapsulation

## Thread Safety
The method is thread-safe as long as the SDLMMInterface parameter is accessed from the appropriate thread (typically the UI/render thread).
