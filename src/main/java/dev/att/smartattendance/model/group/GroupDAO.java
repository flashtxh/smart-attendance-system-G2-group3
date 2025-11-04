package dev.att.smartattendance.model.group;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.att.smartattendance.util.DatabaseManager;

public class GroupDAO {
    
    public void insert_group(String group_id, String group_name, String course_code, 
                            String professor_id, String academic_year, String term) {
        String sql = "INSERT INTO groups (group_id, group_name, course_code, professor_id, academic_year, term) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, group_id);
            ps.setString(2, group_name);
            ps.setString(3, course_code);
            ps.setString(4, professor_id);
            ps.setString(5, academic_year);
            ps.setString(6, term);

            ps.executeUpdate();
            System.out.println("Group inserted successfully");
        } catch (SQLException e) {
            System.err.println("Failed to insert group: " + e.getMessage());
        }
    }

    public List<Group> get_all_groups() {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT g.group_id, g.group_name, g.course_code, g.professor_id, g.academic_year, g.term " +
                     "FROM groups g INNER JOIN courses c ON g.course_code = c.course_id ORDER BY c.course_code";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
        ) {
            while (rs.next()) {
                groups.add(new Group(
                    rs.getString("group_id"), 
                    rs.getString("group_name"), 
                    rs.getString("course_code"), 
                    rs.getString("professor_id"),
                    rs.getString("academic_year"),
                    rs.getString("term")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve groups: " + e.getMessage());
        }

        return groups;
    }

    public List<Group> get_groups_by_professor(String professor_id) {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT g.group_id, g.group_name, g.course_code, g.professor_id, g.academic_year, g.term " +
                     "FROM groups g INNER JOIN courses c ON g.course_code = c.course_id " +
                     "WHERE professor_id = ? ORDER BY c.course_code";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, professor_id);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    groups.add(new Group(
                        rs.getString("group_id"), 
                        rs.getString("group_name"), 
                        rs.getString("course_code"), 
                        rs.getString("professor_id"),
                        rs.getString("academic_year"),
                        rs.getString("term")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve groups by professor: " + e.getMessage());
        }

        return groups;
    }

    public List<Group> get_groups_by_ta(String taId) {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT g.* FROM groups g " +
                    "INNER JOIN ta_assignments ta ON g.group_id = ta.group_id " +
                    "WHERE ta.ta_id = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, taId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Group group = new Group(
                    rs.getString("group_id"),
                    rs.getString("group_name"),
                    rs.getString("course_code"),
                    rs.getString("professor_id"),
                    rs.getString("academic_year"),
                    rs.getString("term")
                );
                groups.add(group);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving groups for TA: " + e.getMessage());
        }
        
        return groups;
    }

    /**
     * Get group name by group ID
     */
    public String get_group_name(String groupId) {
        String sql = "SELECT group_name FROM groups WHERE group_id = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, groupId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getString("group_name");
            }
        } catch (SQLException e) {
            System.err.println("Error getting group name: " + e.getMessage());
        }
        return "Unknown Group";
    }

    /**
     * Create a class with students assigned
     */
    public boolean create_class_with_students(String groupId, String groupName, String courseId, 
                                              String professorId, String academicYear, String term,
                                              List<String> studentIds) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            
            // Insert group
            String groupSql = "INSERT INTO groups (group_id, group_name, course_code, professor_id, academic_year, term) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(groupSql)) {
                ps.setString(1, groupId);
                ps.setString(2, groupName);
                ps.setString(3, courseId);
                ps.setString(4, professorId);
                ps.setString(5, academicYear);
                ps.setString(6, term);
                ps.executeUpdate();
            }
            
            // Insert student enrollments
            String studentGroupSql = "INSERT INTO student_group (student_id, group_id, enrollment_date) " +
                                    "VALUES (?, ?, CURRENT_DATE)";
            try (PreparedStatement ps = conn.prepareStatement(studentGroupSql)) {
                for (String studentId : studentIds) {
                    ps.setString(1, studentId);
                    ps.setString(2, groupId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            
            conn.commit();
            System.out.println("Class created successfully: " + groupName + " with " + studentIds.size() + " students");
            return true;
            
        } catch (SQLException e) {
            System.err.println("Failed to create class: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
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

    /**
     * Get enrolled student IDs for a group
     */
    public Set<String> get_enrolled_student_ids(String groupId) {
        Set<String> enrolledIds = new HashSet<>();
        String sql = "SELECT student_id FROM student_group WHERE group_id = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, groupId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                enrolledIds.add(rs.getString("student_id"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get enrolled students: " + e.getMessage());
        }
        
        return enrolledIds;
    }

    /**
     * Add student to group
     */
    public boolean add_student_to_group(String studentId, String groupId) {
        String sql = "INSERT INTO student_group (student_id, group_id, enrollment_date) " +
                     "VALUES (?, ?, CURRENT_DATE)";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, studentId);
            ps.setString(2, groupId);
            ps.executeUpdate();
            
            System.out.println("Student " + studentId + " added to group " + groupId);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to add student to group: " + e.getMessage());
            return false;
        }
    }

    /**
     * Remove student from group
     */
    public boolean remove_student_from_group(String studentId, String groupId) {
        String sql = "DELETE FROM student_group WHERE student_id = ? AND group_id = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, studentId);
            ps.setString(2, groupId);
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Student " + studentId + " removed from group " + groupId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Failed to remove student from group: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a class/group and all associated data
     */
    public boolean delete_class(String groupId) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            
            // Delete attendance records
            String deleteAttendanceSql = "DELETE FROM attendanceRecords WHERE group_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteAttendanceSql)) {
                ps.setString(1, groupId);
                int attendanceDeleted = ps.executeUpdate();
                System.out.println("Deleted " + attendanceDeleted + " attendance records");
            }
            
            // Delete attendance sessions
            String deleteSessionsSql = "DELETE FROM attendanceSessions WHERE group_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSessionsSql)) {
                ps.setString(1, groupId);
                int sessionsDeleted = ps.executeUpdate();
                System.out.println("Deleted " + sessionsDeleted + " attendance sessions");
            }
            
            // Delete TA assignments
            String deleteTAsSql = "DELETE FROM ta_assignments WHERE group_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteTAsSql)) {
                ps.setString(1, groupId);
                int tasDeleted = ps.executeUpdate();
                System.out.println("Deleted " + tasDeleted + " TA assignments");
            }
            
            // Delete enrollments
            String deleteEnrollmentsSql = "DELETE FROM student_group WHERE group_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteEnrollmentsSql)) {
                ps.setString(1, groupId);
                int enrollmentsDeleted = ps.executeUpdate();
                System.out.println("Deleted " + enrollmentsDeleted + " student enrollments");
            }
            
            // Delete group
            String deleteGroupSql = "DELETE FROM groups WHERE group_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteGroupSql)) {
                ps.setString(1, groupId);
                int groupDeleted = ps.executeUpdate();
                System.out.println("Deleted group: " + (groupDeleted > 0 ? "Success" : "Failed"));
            }
            
            conn.commit();
            System.out.println("Class deleted successfully: " + groupId);
            return true;
            
        } catch (SQLException e) {
            System.err.println("Failed to delete class: " + e.getMessage());
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