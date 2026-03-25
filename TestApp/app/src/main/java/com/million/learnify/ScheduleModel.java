package com.million.learnify;

public class ScheduleModel {
    private String subject;
    private String teacher;
    private String room;
    private String time;
    private String duration;

    public ScheduleModel() {}

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
}
