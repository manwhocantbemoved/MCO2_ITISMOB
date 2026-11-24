package com.example.taskease;

public class TaskItem {
    public String id;
    public String name;
    public int duration;
    public String priority;
    public String energy;
    public String category;
    public String status = "pending";
    public int actualMinutes = 0;
    public long completedAt = 0;
    public TaskItem() {}

    public TaskItem(String name, int duration, String priority, String energy, String category) {
        this.name = name;
        this.duration = duration;
        this.priority = priority;
        this.energy = energy;
        this.category = category;
    }
}