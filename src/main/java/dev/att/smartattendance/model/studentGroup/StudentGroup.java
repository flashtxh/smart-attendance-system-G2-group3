package dev.att.smartattendance.model.studentGroup;

public class StudentGroup {
    private String student_id;
    private String group_id;
    private String enrollment_date;  // NEW - matches DB
    
    public StudentGroup(String student_id, String group_id, String enrollment_date) {
        this.student_id = student_id;
        this.group_id = group_id;
        this.enrollment_date = enrollment_date;
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

    public String getEnrollment_date() {
        return enrollment_date;
    }

    public void setEnrollment_date(String enrollment_date) {
        this.enrollment_date = enrollment_date;
    }

    @Override
    public String toString() {
        return "StudentGroup [student_id=" + student_id + ", group_id=" + group_id + 
               ", enrollment_date=" + enrollment_date + "]";
    }
}
