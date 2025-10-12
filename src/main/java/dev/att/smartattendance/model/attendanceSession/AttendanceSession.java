package dev.att.smartattendance.model.attendanceSession;

import dev.att.smartattendance.model.course.Course;
import dev.att.smartattendance.model.group.Group;

public class AttendanceSession {
    private String session_id;
    private String group_id;
    private String date;

    public AttendanceSession(String session_id, String group_id, String date) {
        this.session_id = session_id;
        this.group_id = group_id;
        this.date = date;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "AttendanceSession [session_id=" + session_id + ", group_id=" + group_id + ", date=" + date + "]";
    }

}
