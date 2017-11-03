package com.mmorg29.dbtools;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 *
 * @author mam
 * Builder model for Reminder objects
 */
public class ReminderBuilder {

    private int id;
    private Timestamp reminderDate;
    private int snoozeIncrement;
    private int appointmentId;
    private String createdBy;
    private Timestamp createdDate;
    private String displayString;

    public ReminderBuilder() {
    }

    /**
     * mam
     * Creates a ReminderBuilder object populated with info from a reminder table record associated to a specific foreign key from the appointment table
     * @param appointmentId the primary key of the appointment database record
     * @return a ReminderBuilder object populated with the info from the specified reminder table record
     */
    public ReminderBuilder fromDB(int appointmentId) {
        String sql = "SELECT * FROM " + DBManager.REMINDER_TABLE + " WHERE appointmentId = " + appointmentId;
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet resultSet = statement.executeQuery(sql);) {
            if (resultSet.first()) {
                this.id = resultSet.getInt("reminderId");
                this.reminderDate = resultSet.getTimestamp("reminderDate");
                this.snoozeIncrement = resultSet.getInt("snoozeIncrement");
                this.appointmentId = resultSet.getInt("appointmentId");
                this.createdBy = resultSet.getString("createdBy");
                this.createdDate = resultSet.getTimestamp("createdDate");
            }else {
                return null;
            }
        } catch (SQLException sqlEx) {
            return null;
        }
        return this;
    }

    public ReminderBuilder setId(int id) {
        this.id = id;
        return this;
    }

    public ReminderBuilder setReminderDate(Timestamp reminderDate) {
        this.reminderDate = reminderDate;
        return this;
    }

    public ReminderBuilder setSnoozeIncrement(int snoozeIncrement) {
        this.snoozeIncrement = snoozeIncrement;
        return this;
    }

    public ReminderBuilder setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
        return this;
    }

    public ReminderBuilder setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public ReminderBuilder setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public ReminderBuilder setDisplayString(String displayString) {
        this.displayString = displayString;
        return this;
    }

    public Reminder build() {
        Reminder reminder = new Reminder(id, reminderDate, snoozeIncrement, appointmentId, createdBy, createdDate);
        //mam set the display string if one is provided
        if (this.displayString != null) {
            reminder.setDisplayString(this.displayString);
        }
        return reminder;
    }

}
