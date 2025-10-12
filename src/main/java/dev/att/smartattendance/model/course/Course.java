package dev.att.smartattendance.model.course;

import dev.att.smartattendance.model.professor.Professor;

public class Course {
    private String course_code;
    private String course_name;
    private int year;
    private int semester;

    public Course(String course_code, String course_name, int year, int semester) {
        this.course_code = course_code;
        this.course_name = course_name;
        this.year = year;
        this.semester = semester;
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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
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
        return "Course [course_code=" + course_code + ", course_name=" + course_name + ", year=" + year + ", semester=" + semester + "]";
    }

}
