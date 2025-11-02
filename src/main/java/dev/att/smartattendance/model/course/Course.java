package dev.att.smartattendance.model.course;

public class Course {
    private String course_id;      // Added course_id
    private String course_code;
    private String course_name;
    private String year;           // Changed from int to String
    private int semester;

    public Course(String course_id, String course_code, String course_name, String year, int semester) {
        this.course_id = course_id;
        this.course_code = course_code;
        this.course_name = course_name;
        this.year = year;
        this.semester = semester;
    }

    public String getCourse_id() {
        return course_id;
    }

    public void setCourse_id(String course_id) {
        this.course_id = course_id;
    }

    public String getCourse_code() {
        return course_code;
    }

    public void setCourse_code(String course_code) {
        this.course_code = course_code;
    }

    public String getCourse_name() {
        return course_name;
    }

    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    public String getYear() {       // Changed return type from int to String
        return year;
    }

    public void setYear(String year) {  // Changed parameter from int to String
        this.year = year;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    @Override
    public String toString() {
        return "Course [course_id=" + course_id + ", course_code=" + course_code + 
               ", course_name=" + course_name + ", year=" + year + ", semester=" + semester + "]";
    }
}