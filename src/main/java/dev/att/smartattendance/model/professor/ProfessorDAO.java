package dev.att.smartattendance.model.professor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.att.smartattendance.util.DatabaseManager;

public class ProfessorDAO {
    
    public void insert_professor(String professor_id, String username, String email, String password) {
        String sql = "INSERT INTO professors (professor_id, username, email, password, is_ta) VALUES (?, ?, ?, ?, 0)";

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

    /**
     * Insert a Teaching Assistant (TA)
     */
    public boolean insert_ta(String taId, String username, String email, String password) {
        String sql = "INSERT INTO professors (professor_id, username, email, password, is_ta) " +
                     "VALUES (?, ?, ?, ?, 1)";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, taId);
            ps.setString(2, username);
            ps.setString(3, email);
            ps.setString(4, password);
            
            ps.executeUpdate();
            System.out.println("TA created successfully: " + username + " (" + email + ")");
            return true;
            
        } catch (SQLException e) {
            System.err.println("Failed to create TA: " + e.getMessage());
            return false;
        }
    }

    public List<Professor> get_all_professors() {
        List<Professor> professors = new ArrayList<>();
        String sql = "SELECT * FROM professors";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
        ) {
            while(rs.next()) {
                String professor_id = rs.getString("professor_id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String password = rs.getString("password");

                professors.add(new Professor(professor_id, username, email, password));
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving professors: " + e.getMessage());
        }

        return professors;
    }

    public Professor get_professor_by_email(String email) {
        String sql = "SELECT * FROM professors WHERE email = ?";
        
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
            System.err.println("Error retrieving professor by email: " + e.getMessage());
        }

        return null;
    }

    public Professor get_professor_by_id(String professor_id) {
        String sql = "SELECT * FROM professors WHERE professor_id = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, professor_id);
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
            System.err.println("Error retrieving professor by ID: " + e.getMessage());
        }

        return null;
    }

    public List<Professor> get_all_tas() {
        List<Professor> tas = new ArrayList<>();
        String sql = "SELECT * FROM professors WHERE is_ta = 1";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
        ) {
            while (rs.next()) {
                Professor ta = new Professor(
                    rs.getString("professor_id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password")
                );
                tas.add(ta);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving TAs: " + e.getMessage());
        }
        
        return tas;
    }

    public boolean is_ta(String email) {
        String sql = "SELECT is_ta FROM professors WHERE email = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("is_ta") == 1;
            }
        } catch (SQLException e) {
            System.err.println("Error checking TA status: " + e.getMessage());
        }
        
        return false;
    }

    /**
     * Check if professor/TA email already exists
     */
    public boolean email_exists(String email) {
        String sql = "SELECT COUNT(*) FROM professors WHERE email = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get assigned TA IDs for a group
     */
    public Set<String> get_assigned_ta_ids(String groupId) {
        Set<String> assignedIds = new HashSet<>();
        String sql = "SELECT ta_id FROM ta_assignments WHERE group_id = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, groupId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                assignedIds.add(rs.getString("ta_id"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get assigned TAs: " + e.getMessage());
        }
        
        return assignedIds;
    }

    /**
     * Assign TA to group
     */
    public boolean assign_ta_to_group(String taId, String groupId) {
        String sql = "INSERT INTO ta_assignments (ta_id, group_id) VALUES (?, ?)";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, taId);
            ps.setString(2, groupId);
            ps.executeUpdate();
            
            System.out.println("TA " + taId + " assigned to group " + groupId);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to assign TA to group: " + e.getMessage());
            return false;
        }
    }

    /**
     * Remove TA from group
     */
    public boolean remove_ta_from_group(String taId, String groupId) {
        String sql = "DELETE FROM ta_assignments WHERE ta_id = ? AND group_id = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, taId);
            ps.setString(2, groupId);
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("TA " + taId + " removed from group " + groupId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Failed to remove TA from group: " + e.getMessage());
            return false;
        }
    }
}