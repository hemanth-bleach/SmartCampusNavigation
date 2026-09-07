# Software Requirements Specification (SRS)
## Smart Campus Navigation System

### 1. Introduction
The Smart Campus Navigation System is a Java-based desktop application designed to solve routing problems on a university campus. By representing buildings as vertices and roads as edges, the system allows students to search for the shortest pathways and estimated travel times. It also gives administrators the ability to manage the map layout (building and road management) in real-time, including blocking or unblocking roads during construction or emergencies.

### 2. Functional Requirements
- **User Authentication**: Login screens for Students and Administrators.
- **Student Module**:
  - View campus map visually.
  - Find the shortest path between a source and a destination building using Dijkstra's Algorithm.
  - Calculate total walking distance and walking time based on an average speed of $1.4\text{ m/s}$.
  - Compute a secondary (alternative) route in case the primary route is congested.
  - Check overall route connectivity using BFS (Breadth-First Search).
  - Find all reachable destinations from a starting building using DFS (Depth-First Search).
- **Admin Module**:
  - Add new buildings visually or through a form.
  - Move/drag buildings on the map canvas to reposition them.
  - Add/delete road connections with distances.
  - Block/unblock roads, triggering dynamic route updates.
  - Persist all changes instantly.

### 3. Non-Functional Requirements
- **Performance**: Pathfinding algorithms must execute in under $50\text{ ms}$ for typical campus graphs.
- **Reliability**: A dual-storage manager that attempts JDBC SQLite database connections but fallback to flat CSV-like text files if driver libraries are unavailable.
- **Usability**: Graphical canvas with color-coded paths, interactive clicking, and dragging, using Segoe UI modern typography.
