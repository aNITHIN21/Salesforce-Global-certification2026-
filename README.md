# Smart Pole Lighting System

A simple Java Swing desktop app simulating an intelligent 7-pole
street-light system with automated holiday-shifting logic, using real
Indian holiday data from 1976 to 2026.

## Requirements
- JDK 17 or later
- No external libraries

## Project Structure (flat, no packages)
```
SmartPoleLighting/
└── src/
    ├── Main.java            entry point
    ├── AppWindow.java        the whole UI: year buttons, calendar, 7-pole dashboard, info panel
    ├── PoleLogic.java        pole assignment + holiday-shifting logic
    └── HolidayManager.java   holiday dataset (1976-2026) + lookup methods
```

## How to Run

### Command line (Windows PowerShell)
```powershell
cd SmartPoleLighting
mkdir out
javac -d out src\*.java
java -cp out Main
```

### Command line (macOS/Linux)
```bash
cd SmartPoleLighting
mkdir out
javac -d out src/*.java
java -cp out Main
```

### VS Code
1. Install "Extension Pack for Java".
2. Open the `SmartPoleLighting` folder.
3. Open `src/Main.java`, click "Run" above `main()`.

### IntelliJ IDEA
1. Open the folder as a project.
2. Mark `src` as Sources Root if not auto-detected.
3. Right-click `Main.java` → Run.

## Features
- **Year navigation**: "◀ Prev Year" / "Next Year ▶" buttons at the top
  switch years (range: 1976–2026, matching the available holiday data).
- **7 Poles**: Mon/Wed/Fri/Sun → Poles 1,3,5,7. Tue/Thu/Sat → Poles 2,4,6.
- **Real holiday data**: sourced from the user-provided dataset covering
  every year from 1976 to 2026, including movable/estimated dates for
  Eid, Muharram, Milad-un-Nabi, etc.
- **Holiday shifting**: on a holiday, all 7 poles show purple, and the
  system calculates the pole set required on the next non-holiday date.
- **Dashboard colors**: Green = ON, Red = OFF, Purple = Holiday.
- Only Sundays show in red text on the calendar; Saturdays render as
  normal working days (matching the pole logic).
