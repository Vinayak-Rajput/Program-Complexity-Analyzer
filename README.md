# 🚀 Algorithm Complexity Analyzer

![Java](https://img.shields.io/badge/Java-17%2B-orange)
![Framework](https://img.shields.io/badge/JavaFX-GUI-blue)
![Build](https://img.shields.io/badge/Maven-Project-C71A36)
![License](https://img.shields.io/badge/License-MIT-green)

A dynamic Java desktop application that visualizes the **Time** and **Space** complexity of algorithms in real-time. This tool bridges the gap between theoretical *Big O* notation and actual runtime performance by executing compiled Java classes dynamically.

## 📸 Demo
*(Upload a screenshot of your running application here and update the path below)*
![Application Screenshot](screenshot.png)

## ✨ Key Features
* **Dynamic Class Loading:** Load external `.class` files at runtime without recompiling the application.
* **Reflection-Based Execution:** Analyze any method (currently supporting `int[]` inputs) by name.
* **Dual Visualization:**
  * **Time Complexity:** Measures execution duration in nanoseconds.
  * **Space Complexity:** Estimates memory allocation in bytes.
* **JVM Warmup:** Includes an automatic warmup phase to trigger JIT optimization for accurate results.
* **Interactive UI:** Built with JavaFX for smooth, interactive charting.

## 🛠️ Tech Stack
* **Language:** Java 17+
* **GUI Framework:** JavaFX
* **Build Tool:** Maven
* **Testing:** JUnit 5
* **IDE:** IntelliJ IDEA

## ⚙️ Prerequisites
Ensure you have the following installed:
* **Java Development Kit (JDK):** Version 17 or higher.
* **Maven:** (Usually bundled with IntelliJ).

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone [https://github.com/yourusername/complexity-analyzer.git](https://github.com/yourusername/complexity-analyzer.git)
cd complexity-analyzer
