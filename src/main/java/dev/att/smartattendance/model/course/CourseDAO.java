package dev.att.smartattendance.model.course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import dev.att.smartattendance.util.DatabaseManager;

public class CourseDAO {
    public void insert_course(String course_code, String course_name, int year, int semester) {
        String sql = "insert into courses (course_code, course_name, year, semester) values (?, ?, ?, ?)";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, course_code);
            ps.setString(2, course_name);
            ps.setInt(3, year);
            ps.setInt(4, semester);

            ps.executeUpdate();
            System.out.println("Course inserted successfuly");
        } catch (SQLException e) {
            System.err.println("Failed to insert course: " + e.getMessage());
        }
    }

    public List<Course> get_all_courses() {
        List<Course> courses = new ArrayList<>();
        String sql = "select * from courses";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
        ) {
            while(rs.next()) {
                courses.add(new Course(
                    rs.getString("course_code"), 
                    rs.getString("course_name"),
                    rs.getInt("year"),
                    rs.getInt("semester")
                ));
            }
        } catch (SQLException e) {  
            System.err.println("Failed to retreive courses: " + e.getMessage());
        }

        return courses;
    }

    

    
}
