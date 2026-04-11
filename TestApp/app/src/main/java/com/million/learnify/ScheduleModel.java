package com.million.learnify;

public class ScheduleModel {
    private int id;
    private String subject;
    private String instructor;
    private String location;
    private String startTime;
    private String duration;

    public ScheduleModel() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    public String getTeacher() { return instructor; }
    public void setTeacher(String teacher) { this.instructor = teacher; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getRoom() { return location; }
    public void setRoom(String room) { this.location = room; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getTime() { return startTime; }
    public void setTime(String time) { this.startTime = time; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
}
