package com.example.a611_windows;

public class GroupMessages {

    private String message;
    private String type;
    private String time;
    private String date;
    private String id;

    public GroupMessages(String message, String type, String time, String date, String id) {
        this.message = message;
        this.type = type;
        this.time = time;
        this.date = date;
        this.id = id;
    }
    public GroupMessages(){


    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.id = id;
    }
}
