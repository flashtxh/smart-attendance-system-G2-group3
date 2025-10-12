package dev.att.smartattendance.model.group;

import dev.att.smartattendance.model.course.Course;

public class Group {
    private String group_id;
    private String group_name;
    private String course_code;
    private String professor_id;

    public Group(String group_id, String group_name, String course_code, String professor_id) {
        this.group_id = group_id;
        this.group_name = group_name;
        this.course_code = course_code;
        this.professor_id = professor_id;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getcourse_code() {
        return course_code;
    }

    public void setcourse_code(String course_code) {
        this.course_code = course_code;
    }

    public String getProfessor_id() {
        return professor_id;
    }

    public void setProfessor_id(String professor_id) {
        this.professor_id = professor_id;
    }
    
    @Override
    public String toString() {
        return "Group [group_id=" + group_id + ", group_name=" + group_name + ", course_code=" + course_code
                + ", professor_id=" + professor_id + "]";
    }

}
