# Custom OS Shell - Technical Architecture and Project Structure

## 1. Project Overview
This project is a custom shell implementation in Java that simulates a Unix-like Operating System environment. The application must internally manage process execution, job control, and eventually scale to handle process scheduling, memory management, and process synchronization[cite: 3, 4, 5].

## 2. Core Architecture & Module Breakdown
To enable our team of 4 to work simultaneously without merge conflicts, the system is designed with strict interfaces and decoupled modules.

### Module 1: The REPL and Command Parser (Core)
* **Responsibility**: The main entry point of the application. It runs the infinite Read-Eval-Print Loop (REPL), reads user input, and parses it into actionable command objects.
* **Key Classes**:
    * `Shell.java`: Contains the `main` method and the core `while` loop.
    * `CommandParser.java`: Parses the raw string input into a `ParsedCommand` data structure, safely separating the command name from its arguments.
    * `CommandDispatcher.java`: Routes the parsed command to either a built-in handler or the `ProcessManager` for external execution.

### Module 2: Built-in Commands (File System & Utils)
* **Responsibility**: Implements the shell's internal utilities.
* **Key Classes**:
    * `Command.java` (Interface): Defines a standard `execute(String[] args)` method.
    * `FileSystemCommands.java`: Implements directory and file operations: `cd`, `pwd`, `ls`, `mkdir`, `rmdir`, `rm`, `touch`, and `cat`.
    * `UtilityCommands.java`: Implements standard terminal interactions: `echo`, `clear`, and `exit`.

### Module 3: Process Management (Execution)
* **Responsibility**: Manages the creation and execution of external processes using Java's native process handling libraries (e.g., `ProcessBuilder`).
* **Key Classes**:
    * `ProcessManager.java`: Wraps native system calls to launch external commands. It must handle routing standard I/O streams and explicitly support both foreground and background process execution.

### Module 4: Job Control and State Tracking
* **Responsibility**: Tracks the status of running processes and provides basic job control functionalities.
* **Key Classes**:
    * `JobTracker.java`: Maintains a thread-safe data structure mapping custom internal Job IDs to native Java Process IDs.
    * `JobCommands.java`: Implements the job control built-ins: `jobs` (list all background jobs), `fg` (bring job to foreground), `bg` (resume stopped job in background), and `kill` (terminate by PID).
    * `ErrorHandler.java`: Standardizes system feedback for invalid inputs, unrecognized commands, or failed process executions.

### Module 5: Process Scheduling
* **Responsibility**: Simulates OS-level process scheduling using Round-Robin and Priority-Based algorithms.
* **Key Classes**:
    * `ProcessScheduler.java` (Interface): Defines the contract for scheduling algorithms with methods for adding processes, running the scheduler, and getting metrics.
    * `RoundRobinScheduler.java`: Implements Round-Robin scheduling with configurable time quantum. Manages process queue and switches processes after each time slice.
    * `PriorityScheduler.java`: Implements Priority-Based scheduling with preemption. Uses priority queue to ensure highest-priority processes run first.
    * `SimulatedProcess.java`: Represents a simulated process with properties like process ID, burst time, priority, arrival time, and execution state.
    * `SchedulingMetrics.java`: Tracks and calculates performance metrics: waiting time, turnaround time, and response time.
    * `SchedulingCommands.java`: Implements user commands for scheduling: `schedule-rr` (Round-Robin), `schedule-priority` (Priority-Based), `add-process`, and `show-metrics`.

## 3. Technical Specifications
* **Tech Stack**: Pure core Java. **Strictly do not use Spring Boot or any external web frameworks.** Use `java.lang.ProcessBuilder` for process management.
* **Build Tool**: Maven (for basic compiling and packaging only).
* **Package Structure**:
    * `com.osshell.core` - Shell REPL and command parsing
    * `com.osshell.commands` - Built-in command implementations
    * `com.osshell.process` - External process execution
    * `com.osshell.jobs` - Job control and tracking
    * `com.osshell.scheduling` - Process scheduling algorithms and simulation