package dev.att.smartattendance.model.group;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import dev.att.smartattendance.util.DatabaseManager;

public class GroupDAO {
    
    public void insert_group(String group_id, String group_name, String course_code, String professor_id) {
        String sql = "insert into groups (group_id, group_name, course_code, professor_id) values (?, ?, ?, ?)";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, group_id);
            ps.setString(2, group_name);
            ps.setString(3, course_code);
            ps.setString(4, professor_id);

            ps.executeUpdate();
            System.out.println("Group inserted successfully");
        } catch (SQLException e) {
            System.err.println("Failed to insert group: " + e.getMessage());
        }
    }


    public List<Group> get_all_groups() {
        List<Group> groups = new ArrayList<>();
        String sql = "select * from groups";

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
                    rs.getString("professor_id")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to retreive groups: " + e.getMessage());
        }

        return groups;
    }
}
