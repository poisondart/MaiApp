package com.mai.nix.maiapp.model;

/**
 * Created by Nix on 02.08.2017.
 */

public class SubjectBodies {
    private String title;
    private String teacher;
    private String type;
    private String time;
    private String room;

    public SubjectBodies(String title, String teacher, String type, String time, String room) {
        this.title = title;
        this.teacher = teacher;
        this.type = type;
        this.time = time;
        this.room = room;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}
