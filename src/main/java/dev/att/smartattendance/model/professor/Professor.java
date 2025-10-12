package dev.att.smartattendance.model.professor;

public class Professor {
    private String professor_id;
    private String username;
    private String email;
    private String password;
    
    public Professor(String professor_id, String username, String email, String password) {
        this.professor_id = professor_id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getProfessor_id() {
        return professor_id;
    }

    public void setProfessor_id(String professor_id) {
        this.professor_id = professor_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Professor [professor_id=" + professor_id + ", username=" + username + ", email=" + email + ", password="+ password + "]";
    }

}
