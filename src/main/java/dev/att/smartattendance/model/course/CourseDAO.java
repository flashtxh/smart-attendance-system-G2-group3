package dev.att.smartattendance.model.course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dev.att.smartattendance.util.DatabaseManager;

public class CourseDAO {
    
    public void insert_course(String course_id, String course_code, String course_name, String year, int semester) {
        String sql = "INSERT INTO courses (course_id, course_code, course_name, year, semester) VALUES (?, ?, ?, ?, ?)";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, course_id);
            ps.setString(2, course_code);
            ps.setString(3, course_name);
            ps.setString(4, year);
            ps.setInt(5, semester);

            ps.executeUpdate();
            System.out.println("Course inserted successfully");
        } catch (SQLException e) {
            System.err.println("Failed to insert course: " + e.getMessage());
        }
    }
// Now let's update GroupDAO    }

    public List<Course> get_all_courses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
        ) {
            while(rs.next()) {
                courses.add(new Course(
                    rs.getString("course_id"),
                    rs.getString("course_code"), 
                    rs.getString("course_name"),
                    rs.getString("year"),
                    rs.getInt("semester")
                ));
            }
        } catch (SQLException e) {  
            System.err.println("Failed to retrieve courses: " + e.getMessage());
        }

        return courses;
    }

    public String getCourseCodeById(String course_id) {
        String sql = "SELECT course_code FROM courses WHERE course_id = ?";
        String courseCode = null;

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, course_id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    courseCode = rs.getString("course_code");
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch course code: " + e.getMessage());
        }

        return courseCode != null ? courseCode : "Unknown";
    }

    /**
     * Delete a course and all associated data (groups, attendance, etc.)
     * @param courseId The ID of the course to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean delete_course(String courseId) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            
            // Get all groups for this course
            List<String> groupIds = new ArrayList<>();
            String getGroupsSql = "SELECT group_id FROM groups WHERE course_code = ?";
            try (PreparedStatement ps = conn.prepareStatement(getGroupsSql)) {
                ps.setString(1, courseId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    groupIds.add(rs.getString("group_id"));
                }
            }
            
            // Delete all attendance records for all groups
            if (!groupIds.isEmpty()) {
                String deleteAttendanceRecordsSql = "DELETE FROM attendanceRecords WHERE group_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteAttendanceRecordsSql)) {
                    for (String groupId : groupIds) {
                        ps.setString(1, groupId);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                    System.out.println("Deleted attendance records for " + groupIds.size() + " groups");
                }
                
                // Delete all attendance sessions for all groups
                String deleteAttendanceSessionsSql = "DELETE FROM attendanceSessions WHERE group_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteAttendanceSessionsSql)) {
                    for (String groupId : groupIds) {
                        ps.setString(1, groupId);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                    System.out.println("Deleted attendance sessions for " + groupIds.size() + " groups");
                }
                
                // Delete TA assignments
                String deleteTAAssignmentsSql = "DELETE FROM ta_assignments WHERE group_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteTAAssignmentsSql)) {
                    for (String groupId : groupIds) {
                        ps.setString(1, groupId);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                    System.out.println("Deleted TA assignments for " + groupIds.size() + " groups");
                }
                
                // Delete student enrollments for all groups
                String deleteEnrollmentsSql = "DELETE FROM student_group WHERE group_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteEnrollmentsSql)) {
                    for (String groupId : groupIds) {
                        ps.setString(1, groupId);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                    System.out.println("Deleted student enrollments for " + groupIds.size() + " groups");
                }
            }
            
            // Delete all groups for this course
            String deleteGroupsSql = "DELETE FROM groups WHERE course_code = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteGroupsSql)) {
                ps.setString(1, courseId);
                int groupsDeleted = ps.executeUpdate();
                System.out.println("Deleted " + groupsDeleted + " groups");
            }
            
            // Finally, delete the course
            String deleteCourseSql = "DELETE FROM courses WHERE course_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteCourseSql)) {
                ps.setString(1, courseId);
                int courseDeleted = ps.executeUpdate();
                System.out.println("Deleted course: " + (courseDeleted > 0 ? "Success" : "Failed"));
            }
            
            conn.commit();
            System.out.println("Course deleted successfully: " + courseId);
            return true;
            
        } catch (SQLException e) {
            System.err.println("Failed to delete course: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Transaction rolled back");
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }
}