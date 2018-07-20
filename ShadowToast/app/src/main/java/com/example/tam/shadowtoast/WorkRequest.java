package com.example.tam.shadowtoast;

import android.arch.persistence.room.*;

import java.util.Date;

@Entity(tableName = "workrequest")
public final class WorkRequest {

    @PrimaryKey private int id;
    private String title;
    private String description;
    private int payment;
    private Date assignmentDate;
    private Date deadline;
    private boolean complete;
    private boolean sentToDb;

    public WorkRequest(int id, String title, String description, int payment, Date assignmentDate, Date deadline, boolean complete, boolean sentToDb){
        this.id = id;
        this.title = title;
        this.description = description;
        this.payment = payment;
        this.assignmentDate = assignmentDate;
        this.deadline = deadline;
        this.complete = complete;
        this.sentToDb = sentToDb;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public int getPayment() {
        return payment;
    }
    public void setPayment(int payment) {
        this.payment = payment;
    }

    public Date getAssignmentDate() {
        return assignmentDate;
    }
    public void setAssignmentDate(Date assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public Date getDeadline() {
        return deadline;
    }
    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public boolean isComplete(){
        return complete;
    }
    public void setComplete(boolean complete){
        this.complete = complete;
    }

    public boolean isSentToDb() {
        return sentToDb;
    }
    public void setSentToDb(boolean sentToDb) {
        this.sentToDb = sentToDb;
    }
}
