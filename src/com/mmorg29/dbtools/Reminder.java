/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mmorg29.dbtools;

import com.mmorg29.appointmentscheduler.DateTimeConversion;
import com.mmorg29.appointmentscheduler.User;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.function.Supplier;

/**
 *
 * @author mam
 * A data model for the Reminder table in the Database
 */
public class Reminder implements DBObject {
    
    private int id;
    private Timestamp reminderDate;
    private int appointmentId;
    private String createdBy;
    private Timestamp createdDate;
    private String displayString;
    private int snoozeIncrement;
    
    //mam Supplies an Appointment object for the appointment this remider was created for.
    public Supplier<Appointment> APPOINTMENT = () -> {
        return new AppointmentBuilder().fromDB(this.appointmentId).build();
    };
    
    public Reminder(int id, Timestamp reminderDate, int snoozeIncrement, int appointmentId, String createdBy, Timestamp createdDate) {
        this.id = id;
        this.reminderDate = reminderDate;
        this.snoozeIncrement = snoozeIncrement;
        this.appointmentId = appointmentId;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
    }
    
    @Override
    public String getTable() {
        return DBManager.REMINDER_TABLE;
    }

    @Override
    public int getId() {
        return this.id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the reminderDate
     */
    public Timestamp getReminderDate() {
        return reminderDate;
    }

    /**
     * @param reminderDate the reminderDate to set
     */
    public void setReminderDate(Timestamp reminderDate) {
        this.reminderDate = reminderDate;
    }

    /**
     * @return the snoozeIncrement
     */
    public int getSnoozeIncrement() {
        return snoozeIncrement;
    }

    /**
     * @param snoozeIncrement the snoozeIncrement to set
     */
    public void setSnoozeIncrement(int snoozeIncrement) {
        this.snoozeIncrement = snoozeIncrement;
    }

    /**
     * @return the appointmentId
     */
    public int getAppointmentId() {
        return appointmentId;
    }

    /**
     * @param appointmentId the appointmentId to set
     */
    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the createdDate
     */
    public Timestamp getCreatedDate() {
        return createdDate;
    }

    /**
     * @param createdDate the createdDate to set
     */
    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }
   
    /**
     * @return the displayString
     */
    public String getDisplayString() {
        return displayString;
    }

    /**
     * @param displayString the displayString to set
     */
    public void setDisplayString(String displayString) {
        this.displayString = displayString;
    }

    //mam see DBObject interface
    @Override
    public boolean writeToDB() throws InvalidDBOperationException {
        if(this.id == -1) {
            throw new InvalidDBOperationException("Unable to add reminder.");
        }
        
        if(!EXISTS.test(DBManager.APPOINTMENT_TABLE, this.appointmentId)) {
            throw new InvalidDBOperationException("Unable to add reminder.");
        }
        
        this.createdBy = User.getInstance().getUserId() + "";
        this.createdDate = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.now());
        
        int result = 0;
        String sql = "INSERT INTO " + DBManager.REMINDER_TABLE 
                + " (reminderId, reminderDate, snoozeIncrement, snoozeIncrementTypeId, appointmentId, createdBy, createdDate, remindercol) VALUES(?, ?, ?, ?, ?, ? , ?, ?)";
        try(DBManager dbManager = new DBManager(); PreparedStatement preparedStatement = dbManager.getPreparedStatement(sql)) {
            preparedStatement.setInt(1, this.id);
            preparedStatement.setTimestamp(2, this.reminderDate);
            preparedStatement.setInt(3, this.snoozeIncrement);
            preparedStatement.setInt(4, 0);
            preparedStatement.setInt(5, this.appointmentId);
            preparedStatement.setString(6, this.createdBy);
            preparedStatement.setTimestamp(7, this.createdDate);
            preparedStatement.setString(8, "");
            result = preparedStatement.executeUpdate();
        }catch (SQLException sqlEx) {
            throw new InvalidDBOperationException("Unable to add reminder.");
        }
        
        if(SUCCESSFUL.test(result)) {
            return true;
        }else {
            throw new InvalidDBOperationException("Unable to add reminder.");
        }
    }
    
    //mam see DBObject interface
    @Override
    public boolean updateInDB() throws InvalidDBOperationException {
        if(!EXISTS.test(DBManager.APPOINTMENT_TABLE, this.appointmentId)) {
            throw new InvalidDBOperationException("Unable to update reminder.");
        }
        
        int result = 0;
        String sql = "UPDATE " + DBManager.REMINDER_TABLE + " SET reminderDate = ? WHERE reminderId = " + this.id;
        try(DBManager dbManager = new DBManager(); PreparedStatement preparedStatement = dbManager.getPreparedStatement(sql);) {
            preparedStatement.setTimestamp(1, reminderDate);
            result = preparedStatement.executeUpdate();
        }catch (SQLException sqlEX) {
            throw new InvalidDBOperationException("Unable to update reminder");
        }
        
        if(SUCCESSFUL.test(result)) {
            return true;
        }else {
            throw new InvalidDBOperationException("Unable to update reminder.");
        }
    }
    
}
