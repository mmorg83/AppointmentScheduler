/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mmorg29.dbtools;

import com.mmorg29.appointmentscheduler.DateTimeConversion;
import com.mmorg29.appointmentscheduler.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 *
 * @author mam
 * A data model for the Appointments table in the DB.
 */
public class Appointment implements DBObject {

    private int id;
    private int customerId;
    private String title;
    private String description;
    private String location;
    private String contact;
    private Timestamp start;
    private Timestamp end;
    private Timestamp createDate;
    private String createdBy;
    private Timestamp lastUpdate;
    private String lastUpdatedBy;
    
    //mam Validates that a piece of appointment info is present
    private final Predicate<String> NOT_EMPTY = (input) -> input != null && !input.isEmpty();
    
    //mam Gets the Customer this appointment is created for.
    public final Supplier<Customer> CUSTOMER = () -> {
        return new CustomerBuilder().fromDB(this.customerId).build();
    };

    //mam Deletes the reminder associated with this appointment.
    public final BooleanSupplier DELETE_REMINDER = () -> {
        String sql = "SELECT reminderId FROM " + DBManager.REMINDER_TABLE + " WHERE appointmentId = " + this.id;
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet resultSet = statement.executeQuery(sql);) {
            if (resultSet.first()) {
                ReminderBuilder reminderBuilder = new ReminderBuilder();
                reminderBuilder.setId(resultSet.getInt(1));
                return reminderBuilder.build().deleteFromDB();
            }else {
                return false;
            }
        } catch (SQLException | InvalidDBOperationException ex) {
            return false;
        }
    };

    public Appointment(int appointmentId, int customerId, String title, String description, String location, String contact, Timestamp start,
            Timestamp end, Timestamp createDate, String createdBy, Timestamp lastUpdate, String lastUpdatedBy) {
        this.id = appointmentId;
        this.customerId = customerId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.contact = contact;
        this.start = start;
        this.end = end;
        this.createDate = createDate;
        this.createdBy = createdBy;
        this.lastUpdate = lastUpdate;
        this.lastUpdatedBy = lastUpdatedBy;
    }

    //mam Getters and Setters
    
    @Override
    public String getTable() {
        return DBManager.APPOINTMENT_TABLE;
    }

    /**
     * @return the id
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * @return the customerId
     */
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the contact
     */
    public String getContact() {
        return contact;
    }

    /**
     * @param contact the contact to set
     */
    public void setContact(String contact) {
        this.contact = contact;
    }

    /**
     * @return the start
     */
    public Timestamp getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(Timestamp start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public Timestamp getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(Timestamp end) {
        this.end = end;
    }

    /**
     * @return the createDate
     */
    public Timestamp getCreateDate() {
        return createDate;
    }

    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @return the lastUpdate
     */
    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    /**
     * @return the lastUpdatedBy
     */
    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    //mam see DBObject interface
    @Override
    public boolean writeToDB() throws InvalidDBOperationException {
        if(this.id == -1) {
            throw new InvalidDBOperationException("Unable to add appointment.");
        }
        
        if (!EXISTS.test(DBManager.CUSTOMER_TABLE, this.customerId)) {
            throw new InvalidDBOperationException("Unable to add appointment.");
        }

        this.lastUpdate = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.now());
        this.lastUpdatedBy = User.getInstance().getUserId() + "";
        this.createDate = lastUpdate;
        this.createdBy = lastUpdatedBy;

        int result = 0;
        String sql = "INSERT INTO " + DBManager.APPOINTMENT_TABLE + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (DBManager dbManager = new DBManager(); PreparedStatement preparedStatement = dbManager.getPreparedStatement(sql);) {
            preparedStatement.setInt(1, this.id);
            preparedStatement.setInt(2, this.customerId);
            preparedStatement.setString(3, this.title);
            preparedStatement.setString(4, this.description);
            preparedStatement.setString(5, this.location);
            preparedStatement.setString(6, contact);
            preparedStatement.setString(7, "");
            preparedStatement.setTimestamp(8, this.start);
            preparedStatement.setTimestamp(9, this.end);
            preparedStatement.setTimestamp(10, this.createDate);
            preparedStatement.setString(11, this.createdBy);
            preparedStatement.setTimestamp(12, this.lastUpdate);
            preparedStatement.setString(13, this.lastUpdatedBy);
            result = preparedStatement.executeUpdate();
        } catch (SQLException sqlEx) {
            throw new InvalidDBOperationException("Unable to add appointment.");
        }

        if (SUCCESSFUL.test(result)) {
            return true;
        } else {
            throw new InvalidDBOperationException("Unable to add appointment.");
        }
    }

    //mam see DBObject interface
    @Override
    public boolean updateInDB() throws InvalidDBOperationException {
        if (!EXISTS.test(DBManager.CUSTOMER_TABLE, this.customerId)) {
            throw new InvalidDBOperationException("Unable to update appointment info.");
        }

        this.lastUpdate = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.now());
        this.lastUpdatedBy = User.getInstance().getUserId() + "";

        int result = 0;
        String sql = "UPDATE " + DBManager.APPOINTMENT_TABLE + " SET title = ?, description = ?, location = ?, contact = ?, start = ?, end = ?, lastUpdate = ?, lastUpdateBy = ?"
                + " WHERE appointmentId = " + this.id;
        try (DBManager dbManager = new DBManager(); PreparedStatement preparedStatement = dbManager.getPreparedStatement(sql);) {
            preparedStatement.setString(1, this.title);
            preparedStatement.setString(2, this.description);
            preparedStatement.setString(3, this.location);
            preparedStatement.setString(4, this.contact);
            preparedStatement.setTimestamp(5, this.start);
            preparedStatement.setTimestamp(6, this.end);
            preparedStatement.setTimestamp(7, this.lastUpdate);
            preparedStatement.setString(8, this.lastUpdatedBy);
            result = preparedStatement.executeUpdate();
        } catch (SQLException sqlEx) {
            throw new InvalidDBOperationException("Unable to update appointment info.");
        }

        if (SUCCESSFUL.test(result)) {
            return true;
        } else {
            throw new InvalidDBOperationException("Unable to update appointment info.");
        }
    }
    
    /**
     * mam
     * Creates the display Strings for the appointment info.
     * Will be 2 forms a detailed form and a short form.
     * @param customer the Customer object for the customer of this appointment.  This is used to cut down on timely queries to the Database.  If null the a new Customer object will be created.
     * @return a 2 element String array with element 0 being a short display string and element 1 being a detailed display string.
     */
    public String[] getDisplayStrings(Customer customer) {
        if(customer == null) {
            customer = this.CUSTOMER.get();
        }
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        LocalDateTime zonedStart = DateTimeConversion.ZONED_LOCAL_DATE_TIME.apply(this.start);
        LocalDateTime zonedEnd = DateTimeConversion.ZONED_LOCAL_DATE_TIME.apply(this.end);
        String[] displayStrings = new String[2];
        displayStrings[0] = getShortDisplayString(customer, zonedStart, zonedEnd, timeFormatter);
        displayStrings[1] = getDetailedDisplayString(customer, zonedStart, zonedEnd, timeFormatter);
        return displayStrings;
    }
    
    /**
     * mam
     * Creates a short version display string of the appointment info
     * @param customer the Customer object this appointment is created for.
     * @param zonedStart the appointment start date and time (zoned)
     * @param zonedEnd the appointment end date and time (zoned)
     * @param timeFormatter a DateTimeFormatter to format the start and end times
     * @return a short version string of appointment info
     */
    private String getShortDisplayString(Customer customer, LocalDateTime zonedStart, LocalDateTime zonedEnd, DateTimeFormatter timeFormatter) {
        StringBuilder appointmentDisplayStringBuilder = new StringBuilder();
        appointmentDisplayStringBuilder.append(zonedStart.format(timeFormatter));
        appointmentDisplayStringBuilder.append(" - ");
        appointmentDisplayStringBuilder.append(zonedEnd.format(timeFormatter));
        appointmentDisplayStringBuilder.append(", ");
        appointmentDisplayStringBuilder.append(customer.getCustomerName());
        return appointmentDisplayStringBuilder.toString();
    }
    
    /**
     * mam
     * Creates a detailed version display string of the appointment info 
     * @param customer the Customer object this appointment is created for.
     * @param zonedStart the appointment start date and time (zoned)
     * @param zonedEnd the appointment end date and time (zoned)
     * @param timeFormatter a DateTimeFormatter to format the start and end times
     * @return a detailed version string of appointment info
     */
    private String getDetailedDisplayString(Customer customer, LocalDateTime zonedStart, LocalDateTime zonedEnd, DateTimeFormatter timeFormatter) {
        StringBuilder appointmentDisplayStringBuilder = new StringBuilder();
        appointmentDisplayStringBuilder.append(zonedStart.format(timeFormatter));
        appointmentDisplayStringBuilder.append(" - ");
        appointmentDisplayStringBuilder.append(zonedEnd.format(timeFormatter));
        appointmentDisplayStringBuilder.append("\nCustomer: ");
        appointmentDisplayStringBuilder.append(customer.getCustomerName());
        appointmentDisplayStringBuilder.append("\nType: ");
        appointmentDisplayStringBuilder.append(this.title);
        if (NOT_EMPTY.test(this.contact)) {
            appointmentDisplayStringBuilder.append("\nContact: ").append(this.contact);
        }
        appointmentDisplayStringBuilder.append("\nLocation: ");
        if (NOT_EMPTY.test(this.location)) {
            appointmentDisplayStringBuilder.append(this.location);
        } else {
            appointmentDisplayStringBuilder.append(customer.ADDRESS.get().getPhoneNumber());
        }
        if (NOT_EMPTY.test(this.description)) {
            appointmentDisplayStringBuilder.append("\nDescription: ").append(this.description);
        }
        return appointmentDisplayStringBuilder.toString();
    }
}
