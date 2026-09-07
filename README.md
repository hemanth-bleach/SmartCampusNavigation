# Smart Campus Navigation System

A high-performance Java desktop application that simulates and optimizes campus navigation. It uses graph data structures and path-finding algorithms (Dijkstra, BFS, DFS) to find the shortest routes, calculate walking times, present alternative path selections, and support live administrative graph manipulation (adding/deleting buildings, creating/blocking roads) from an interactive GUI.

## Features

- **Interactive Graphic Map**: Rendered using custom Swing graphics.
  - **Click to Route**: Left-click a building to set the Start Location, Right-click to set the Destination.
  - **Interactive Dragging (Admin)**: Admins can drag buildings to rearrange the campus layout, which persists to storage automatically.
  - **Dynamic Highlights**: The shortest path is overlaid in gold, and alternative routes are shown as dotted teal paths. Blocked roads are shown in red.
  - **Double-Click addition (Admin)**: Double-click empty space on the canvas to place a new building.
- **Advanced Graph Algorithms**:
  - **Dijkstra's Algorithm**: Finds the shortest paths based on road distance. It skips blocked roads, calculates walking times at $1.4\text{ m/s}$, and generates secondary (alternative) routes by temporarily excluding active path edges.
  - **BFS (Breadth-First Search)**: Used to verify route connectivity and calculate the shortest paths by number of road hops (turns).
  - **DFS (Depth-First Search)**: Used to traverse the graph and compute reachable networks or detect isolated zones.
- **Dual Database Fallback**:
  - Automatically tries to connect to an **SQLite database** (`database/campus.db`) using JDBC.
  - If the SQLite JDBC driver is not found on the classpath, the system gracefully falls back to a **local text-file storage** (`database/buildings.txt` and `database/roads.txt`), requiring **zero installation** or jar configurations to run out-of-the-box.
- **Admin Dashboard**: Permits adding/deleting buildings, building connections (roads), updating road distances, and toggling road blockages.
- **Student Dashboard**: Quick route searching, connectivity checking, and DFS exploring.

---

## Directory Structure

```text
SmartCampusNavigation/
├── src/
│   └── com/campus/
│       ├── main/
│       │   └── Main.java             # Main Entry point
│       ├── graph/
│       │   ├── Vertex.java           # Building model
│       │   ├── Edge.java             # Road model
│       │   └── Graph.java            # Adjacency list representation
│       ├── algorithm/
│       │   ├── Dijkstra.java         # Shortest path & alternative path
│       │   ├── BFS.java              # Connectivity verification
│       │   └── DFS.java              # Reachability explorer
│       ├── database/
│       │   ├── DBConnection.java     # SQLite connection & Schema initialization
│       │   ├── BuildingDAO.java      # Building CRUD with fallback
│       │   └── RoadDAO.java          # Road CRUD with fallback
│       ├── gui/
│       │   ├── CampusMapPanel.java   # Custom graphical canvas
│       │   ├── LoginFrame.java       # User authentication screen
│       │   ├── HomeFrame.java        # Main student/admin dashboard
│       │   └── UIComponents.java     # Custom modern Swing widgets
│       ├── student/
│       │   └── Student.java          # Student session model
│       ├── admin/
│       │   └── Admin.java            # Admin session model
│       ├── utils/
│       │   ├── Constants.java        # Design theme, speeds, default logins
│       │   └── Validation.java       # Coordinates, passwords, and name checkers
│       └── test/
│           └── AlgorithmTest.java    # Automated algorithm unit tester
├── database/
│   └── campus.sql                    # Bootstrap schema backup
└── README.md                         # Project documentation
```

---

## Getting Started

### Prerequisites

- Java SE Development Kit (JDK) 21 or higher (Compatible with JDK 8+).

### Option 1: Run with Out-of-the-Box Text File Storage (Zero Config)

No JARs or database drivers are required. Simply compile and execute from the terminal:

1. **Compile the Application**:
   ```bash
   javac -d bin -sourcepath src src/com/campus/main/Main.java
   ```

2. **Run the Application**:
   ```bash
   java -cp bin com.campus.main.Main
   ```

### Option 2: Run with SQLite Database (JDBC Support)

To run with a full SQLite database instead of text files:

1. Download the SQLite JDBC Driver jar file (e.g. `sqlite-jdbc-3.45.1.0.jar` from Maven Central).
2. Place the JAR file in a `lib` folder or keep it in your working directory.
3. **Compile the Application**:
   ```bash
   javac -d bin -sourcepath src src/com/campus/main/Main.java
   ```
4. **Run with Driver**:
   - On Windows (PowerShell/CMD):
     ```powershell
     java -cp "bin;path/to/sqlite-jdbc.jar" com.campus.main.Main
     ```
   - On macOS/Linux:
     ```bash
     java -cp "bin:path/to/sqlite-jdbc.jar" com.campus.main.Main
     ```

---

## Running the Automated Algorithm Tests

To verify that the Dijkstra calculations, BFS reachability, DFS traversals, and road-blocking updates are performing correctly:

1. **Compile the Tests**:
   ```bash
   javac -d bin -sourcepath src src/com/campus/test/AlgorithmTest.java
   ```

2. **Run the Tests**:
   ```bash
   java -cp bin com.campus.test.AlgorithmTest
   ```

---

## Default Login Credentials

- **Student Login**: 
  - Username: `student`
  - Password: `student123`
- **Admin Login**: 
  - Username: `admin`
  - Password: `admin123`
