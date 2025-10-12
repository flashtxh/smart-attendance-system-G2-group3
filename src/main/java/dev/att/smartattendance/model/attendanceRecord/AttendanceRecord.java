package dev.att.smartattendance.model.attendanceRecord;

import dev.att.smartattendance.model.attendanceSession.AttendanceSession;
import dev.att.smartattendance.model.student.Student;

public class AttendanceRecord {
    private String rercord_id;
    private AttendanceSession session;
    private Student student;
    private String status;

    public AttendanceRecord(String rercord_id, AttendanceSession session, Student student, String status) {
        this.rercord_id = rercord_id;
        this.session = session;
        this.student = student;
        this.status = status;
    }

    public String getRercord_id() {
        return rercord_id;
    }

    public void setRercord_id(String rercord_id) {
        this.rercord_id = rercord_id;
    }

    public AttendanceSession getSession() {
        return session;
    }

    public void setSession(AttendanceSession session) {
        this.session = session;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AttendanceRecord [rercord_id=" + rercord_id + ", session=" + session + ", student=" + student + ", status=" + status + "]";
    }
}
