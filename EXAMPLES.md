# SDLMM-Java Examples Guide

This document describes the new particle simulation and 3D examples added to sdlmm-java.

## New Examples

### 1. Particles.java - Basic Particle Simulation

A particle simulation with collision detection and wall bouncing.

**Features:**
- Configurable number of particles
- Particle-to-particle collision detection
- Wall bounce physics
- Velocity damping
- Configurable particle radius

**Usage:**
```bash
java -cp classes sunneo.sdlmm.exams.Particles [particle_count] [radius]
```

**Parameters:**
- `particle_count`: Number of particles (default: 300)
- `radius`: Particle radius (default: 5)

**Controls:**
- `Q` or `ESC`: Quit

---

### 2. Particles2.java - Advanced Particle Simulation with Gravity

An advanced particle simulation featuring gravity effects and dynamic particle generation.

**Features:**
- Dynamic particle generation
- Gravity simulation with inverse square law
- Particle removal when settled or off-screen
- Configurable maximum particle count
- Collision detection with visual feedback

**Usage:**
```bash
java -cp classes sunneo.sdlmm.exams.Particles2 [particle_count] [max_particles] [radius] [max_velocity]
```

**Parameters:**
- `particle_count`: Initial particle generation count (default: 300)
- `max_particles`: Maximum particles in scene (default: 1000)
- `radius`: Particle radius (default: 5)
- `max_velocity`: Maximum velocity (default: 10)

**Controls:**
- `Q` or `ESC`: Quit

---

### 3. NBody3D.java - 3D N-Body Gravitational Simulation

A 3D N-body gravitational simulation using the Babylon3D rendering engine. Simulates gravitational interactions between particles in 3D space.

**Features:**
- Toggle between 2D and 3D rendering modes
- Rotating 3D camera view
- Real-time gravitational calculations
- Interactive parameter adjustment
- Multiple visualization modes (2D only)
- Sphere-based particle rendering in 3D

**Usage:**
```bash
java -cp classes sunneo.sdlmm.exams.NBody3D [body_count]
```

**Parameters:**
- `body_count`: Number of bodies to simulate (default: 500)

**Controls:**
- `0-3`: Switch visualization mode (2D mode only)
  - 0: Circle outlines
  - 1: Points only
  - 2: Points with circles
  - 3: Filled circles
- `D`: Toggle 3D/2D rendering
- `C`: Toggle centralize mode
- `R`: Toggle random simulation factor
- `H`: Toggle help display
- Mouse: Click and drag on simulation factor bar to adjust

---

### 4. MissileCmd3D.java - 3D Missile Command Game

A 3D version of the classic Missile Command game using the Babylon3D rendering engine.

**Features:**
- Toggle between 2D and 3D rendering modes
- Enemy missiles falling from the sky
- Player-launched counter-missiles
- Explosion chain reactions
- Building defense mechanics
- Score tracking
- Dynamic 3D camera movement

**Usage:**
```bash
java -cp classes sunneo.sdlmm.exams.MissileCmd3D
```

**Controls:**
- `Mouse Move`: Aim cursor
- `Mouse Click`: Launch missile
- `D`: Toggle 3D/2D rendering

**Gameplay:**
- Defend your buildings from enemy missiles
- Click to launch counter-missiles
- Create chain reactions by hitting enemy missiles
- Game restarts when all enemies are destroyed
- Score increases by 100 for each enemy missile destroyed

---

## Implementation Details

All examples are based on the C reference implementations from ref-sdlmm:
- `Particles.java` ← `ref-sdlmm/exams/particles.c`
- `Particles2.java` ← `ref-sdlmm/exams/particles2.c`
- `NBody3D.java` ← `ref-sdlmm/exams/nbody.c` (enhanced with 3D)
- `MissileCmd3D.java` ← `ref-sdlmm/exams/missilecmd.c` (enhanced with 3D)

### Babylon3D Enhancements

The 3D examples use the Babylon3D software rendering engine with:
- Sphere mesh generation for particles
- 3D transformations and projections
- Depth buffering
- Dynamic camera positioning
- Lighting calculations

### Mesh.createSphere()

New sphere generation methods added to `Mesh.java`:
```java
// Create sphere with custom segments and rings
public static Mesh createSphere(float radius, int segments, int rings)

// Create sphere with default 16x16 subdivisions
public static Mesh createSphere(float radius)
```

---

## Building and Running

1. Compile the examples:
```bash
javac -cp src -d classes src/sunneo/sdlmm/exams/*.java src/sunneo/sdlmm/babylon3d/*.java
```

2. Run an example:
```bash
java -cp classes sunneo.sdlmm.exams.NBody3D 1000
```

---

## Technical Notes

- All simulations use timestep-based physics
- Collision detection uses distance-squared calculations for efficiency
- 3D rendering uses software rasterization with depth buffering
- Camera updates are synchronized with simulation timestep
- All examples maintain ~60 FPS target
