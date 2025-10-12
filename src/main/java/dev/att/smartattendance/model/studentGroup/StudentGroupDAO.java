package dev.att.smartattendance.model.studentGroup;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import dev.att.smartattendance.util.DatabaseManager;

public class StudentGroupDAO {
    
    public void insert_student_group(String student_id, String group_id) {
        String sql = "insert into student_group (student_id, group_id) values (?, ?)";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, student_id);
            ps.setString(2, group_id);

            ps.executeUpdate();
            System.out.println("Student group inserted successfully");
        } catch (SQLException e) {
            System.err.println("Failed to insert student group: " + e.getMessage());
        }
    }

    public List<StudentGroup> get_all_student_groups() {
        List<StudentGroup> student_groups = new ArrayList<>();
        String sql = "select * from student_group";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
        ) {
            while(rs.next()) {
                student_groups.add(new StudentGroup(
                    rs.getString("student_id"), 
                    rs.getString("group_id")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve student_groups: " + e.getMessage());
        }

        return student_groups;
    }
}
