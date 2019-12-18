package com.example.a611_windows;

public class GroupMessages {

    private String message;
    private String type;
    private String time;
    private String date;
    private String name;

    public GroupMessages(String message, String type, String time, String date, String name) {
        this.message = message;
        this.type = type;
        this.time = time;
        this.date = date;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
