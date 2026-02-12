# Fix IllegalFormatFlagsException in MissileCmd3D

## Problem
MissileCmd3D was crashing on startup with:
```
Exception in thread "Thread-0" java.util.IllegalFormatFlagsException: Flags = '-0'
at java.base/java.util.Formatter$FormatSpecifier.checkNumeric(Formatter.java:3206)
at java.base/java.util.Formatter$FormatSpecifier.checkInteger(Formatter.java:3161)
```

## Root Cause
The `drawMessage()` method in MissileCmd3D.java used illegal format flag combinations in `String.format()` calls:

**Lines 280-281 (BROKEN):**
```java
String cscore = String.format("Score:%-04d", score);
String cmissile = String.format(":%-04d", remainMissile);
```

The format specifier `%-04d` combines two conflicting flags:
- `-` : Left-justify the output
- `0` : Zero-pad the output

These flags are mutually exclusive in Java's format strings and cause `IllegalFormatFlagsException`.

## Solution
Removed the conflicting `-` flag, keeping only the `0` flag for zero-padding:

**Lines 280-281 (FIXED):**
```java
String cscore = String.format("Score:%04d", score);
String cmissile = String.format(":%04d", remainMissile);
```

## Verification

### Test Results
```
Testing original (broken) format:
  Exception (expected): IllegalFormatFlagsException

Testing fixed format:
  cscore: Score:0123
  cmissile: :0045
  cenermy: Enemy:006/010

All format strings work correctly!
```

### Format String Behavior

| Format | Input | Output | Description |
|--------|-------|--------|-------------|
| `%04d` | 123 | `0123` | Zero-padded, right-justified (4 digits) |
| `%-4d` | 123 | `123 ` | Space-padded, left-justified (4 chars) |
| `%-04d` | 123 | **ERROR** | Illegal: conflicting flags |

## Impact
- **Before**: Game crashed on startup
- **After**: Game runs correctly, displaying scores with zero-padding (e.g., "Score:0123")

## Files Changed
- `src/sunneo/sdlmm/exams/MissileCmd3D.java` (lines 280-281)

## Testing
✅ Code compiles successfully  
✅ Format strings validated with test program  
✅ No other format string issues found in the file
