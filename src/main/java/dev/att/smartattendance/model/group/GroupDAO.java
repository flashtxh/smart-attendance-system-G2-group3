package dev.att.smartattendance.model.group;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
        String sql = "SELECT g.group_id, g.group_name, g.course_code, g.professor_id, g.academic_year, g.term FROM groups g INNER JOIN courses c ON g.course_code = c.course_id ORDER BY c.course_code";

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
        String sql = "SELECT g.group_id, g.group_name, g.course_code, g.professor_id, g.academic_year, g.term FROM groups g INNER JOIN courses c ON g.course_code = c.course_id WHERE professor_id = ? ORDER BY c.course_code";

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
}
