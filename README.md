# Smart Attendance System G2 (Group 3)
Smart Attendance System built in JavaFX with modular OOP design, clean architecture, and usability in mind. Automates attendance with OpenCV face detection/recognition, supports student enrollment, session management, and auto/manual marking. Data is stored in SQLite with exportable reports (CSV).
The system also includes Role-Based Access Control (RBAC), ensuring different permissions for Admins, Professors, and TAs to manage classes and attendance securely.

## Setup Checklist

1. **Clone the repository**
	```
	git clone https://github.com/flashtxh/smart-attendance-system-G2-group3.git
	```
2. **Install Java 17+ and Maven**
3. **Build the project**
	```
	mvn clean install
	```
4. **Add OpenCV JAR to your classpath manually**
	- Download OpenCV (https://opencv.org/releases/)
	- Add the .jar to your project classpath
	- Add the native library directory (.dll or .so) to your system PATH
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
```
mvn install:install-file 
	"-Dfile=C:\opencv\build\java\opencv-480.jar" 
	"-DgroupId=org.opencv" 
	"-DartifactId=opencv" 
	"-Dversion=4.8.0" 
	"-Dpackaging=jar"
```
## Role-Based Permissions
- **Admin**
  - Create/edit courses
  - Create new class sessions
  - Add professors and TAs
  - Enroll students
  - Manage classes
  - Take attendance
- **Professor / TA**
  - Enroll students (for classes assigned to them)
  - Take attendance

## Default Login Accounts

**Admin Account**
- Username: admin
- Password: admin

**Professor Account**
- Username: robertlee@smu.edu.sg
- Password: hashed_pw_1

**TA Account**
- Username: TA7@smu.edu.sg
- Password: ta123

## Notes
- Make sure `attendance.db` is created in the project directory on first run.
- Log files are written to `attendance.log`.
- For any issues, check the logs and ensure all dependencies are set up correctly.