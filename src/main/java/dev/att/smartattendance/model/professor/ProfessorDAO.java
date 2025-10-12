package dev.att.smartattendance.model.professor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dev.att.smartattendance.util.DatabaseManager;

public class ProfessorDAO {
    
    public void insert_professor(String professor_id, String username, String email, String password) {
        String sql = "insert into professors (professor_id, username, email, password) values (?, ?, ?, ?)";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, professor_id);
            ps.setString(2, username);
            ps.setString(3, email);
            ps.setString(4, password);

            ps.executeUpdate();
            System.out.println("Professor inserted successfully");
        } catch (SQLException e) {
            System.err.println("Failed to insert professor: " + e.getMessage());
        }
    }

    public List<String> get_all_professors() {
        List<String> professors = new ArrayList<>();
        String sql = "select * from professors";
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
        ) {

            while(rs.next()) {
                String professor_id = rs.getString("professor_id");
                String username = rs.getString("username");
                String email = rs.getString("email");

                String info = String.format("[%s] %s %s", professor_id, username, email);
                professors.add(info);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving professors: " + e.getMessage());
        }

        return professors;
    }

    public Professor get_professor_by_email(String email) {
        String sql = "select * from professors where email = ?";
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Professor(
                    rs.getString("professor_id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving professors: " + e.getMessage());
        }

        return null;
    }
}
