package dev.att.smartattendance.model.facedata;

import java.util.Arrays;


public class FaceData {
    private String face_id;
    private String student_id;
    private String folder_path;
    private byte[] embedding;

    public FaceData(String face_id, String student_id, String folder_path, byte[] embedding) {
        this.face_id = face_id;
        this.student_id = student_id;
        this.folder_path = folder_path;
        this.embedding = embedding;
    }

    public String getFace_id() {
        return face_id;
    }

    public void setFace_id(String face_id) {
        this.face_id = face_id;
    }

    public String getStudent_id() {
        return student_id;
    }

    public void setStudent_id(String student_id) {
        this.student_id = student_id;
    }

    public String getfolder_path() {
        return folder_path;
    }

    public void setfolder_path(String folder_path) {
        this.folder_path = folder_path;
    }

    public byte[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(byte[] embedding) {
        this.embedding = embedding;
    }

    @Override
    public String toString() {
        return "FaceData [face_id=" + face_id + ", student_id=" + student_id + ", folder_path=" + folder_path + ", embedding=" + Arrays.toString(embedding) + "]";
    }


}
