package com.example.tam.shadowtoast;

import java.util.Date;

public final class WorkRequest {

    private int id;
    private String title;
    private String description;
    private int payment;
    private Date assignmentDate;
    private Date deadline;
    private boolean completed;

    public WorkRequest(int id, String title, String description, int payment, Date assignmentDate, Date deadline, boolean completed){
        this.id = id;
        this.title = title;
        this.description = description;
        this.payment = payment;
        this.assignmentDate = assignmentDate;
        this.deadline = deadline;
        this.completed = completed;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getPayment() {
        return payment;
    }

    public Date getAssignmentDate() {
        return assignmentDate;
    }

    public Date getDeadline() {
        return deadline;
    }

    public boolean isCompleted(){
        return completed;
    }
    public void setCompleted(boolean value){
        completed = value;
    }
}
