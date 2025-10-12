package dev.att.smartattendance.model.studentGroup;

public class StudentGroup {
    private String student_id;
    private String group_id;
    
    public StudentGroup(String student_id, String group_id) {
        this.student_id = student_id;
        this.group_id = group_id;
    }

    public String getStudent_id() {
        return student_id;
    }

    public void setStudent_id(String student_id) {
        this.student_id = student_id;
    }

    public String getGroup_id() {
        return group_id;
    }
    
    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    @Override
    public String toString() {
        return "StudentGroup [student_id=" + student_id + ", group_id=" + group_id + "]";
    }

}
