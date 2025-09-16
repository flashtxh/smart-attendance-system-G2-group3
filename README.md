# Smart Attendance System (Group 3)

## Setup Checklist

1. **Clone the repository**
	```
	git clone <repo-url>
	```
2. **Install Java 17+ and Maven**
3. **Build the project**
	```
	mvn clean install
	```
4. **Add OpenCV JAR to your classpath manually**
	- Download OpenCV (https://opencv.org/releases/)
	- Add the JAR to your project classpath
	- Add the native library directory to your system PATH
5. **Run the application**
	```
	mvn javafx:run
	```

## Dependencies
- JavaFX (controls, fxml)
- SQLite JDBC (org.xerial:sqlite-jdbc)
- SLF4J + Logback
- OpenCSV
- OpenCV (manual setup)

## OpenCV setup command (powershell)
Replace -Dfile with your own opencv filepath.
mvn install:install-file "-Dfile=C:\opencv\build\java\opencv-480.jar" "-DgroupId=org.opencv" "-DartifactId=opencv" "-Dversion=4.8.0" "-Dpackaging=jar"


## Notes
- Make sure `attendance.db` is created in the project directory on first run.
- Log files will be written to `attendance.log`.
- For any issues, check the logs and ensure all dependencies are set up correctly.
# smart-attendance-system-G2-group3
Smart Attendance System built in JavaFX for CS102. Automates attendance with OpenCV face detection/recognition, supports student enrollment, session management, and auto/manual marking. Data stored in SQLite with exportable reports (CSV/PDF). Designed with OOP, modular architecture, and usability in mind.

test
