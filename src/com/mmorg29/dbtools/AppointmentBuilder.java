package com.mmorg29.dbtools;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 *
 * @author mam
 * Builder model for an Appointment object.
 */
public class AppointmentBuilder {

    private int appointmentId;
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

    public AppointmentBuilder() {

    }

    /**
     * mam
     * Creates an AppointmentBuilder object and populates it with the data for the record specified by the id
     * @param id the primary key of the appointment table database record
     * @return an AppointmentBuilder with the info of the desired appointment table record
     */
    public AppointmentBuilder fromDB(int id) {
        String sql = "SELECT * FROM " + DBManager.APPOINTMENT_TABLE + " WHERE appointmentId = " + id;
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet resultSet = statement.executeQuery(sql);) {
            if (resultSet.first()) {
                this.appointmentId = id;
                this.customerId = resultSet.getInt("customerId");
                this.title = resultSet.getString("title");
                this.description = resultSet.getString("description");
                this.location = resultSet.getString("location");
                this.contact = resultSet.getString("contact");
                this.start = resultSet.getTimestamp("start");
                this.end = resultSet.getTimestamp("end");
                this.createDate = resultSet.getTimestamp("createDate");
                this.createdBy = resultSet.getString("createdBy");
                this.lastUpdate = resultSet.getTimestamp("lastUpdate");
                this.lastUpdatedBy = resultSet.getString("lastUpdateBy");
            }
        } catch (SQLException sqlEx) {
            return null;
        }
        return this;
    }

    public AppointmentBuilder setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
        return this;
    }

    public AppointmentBuilder setCustomerId(int customerId) {
        this.customerId = customerId;
        return this;
    }

    public AppointmentBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public AppointmentBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public AppointmentBuilder setLocation(String location) {
        this.location = location;
        return this;
    }

    public AppointmentBuilder setContact(String contact) {
        this.contact = contact;
        return this;
    }

    public AppointmentBuilder setStart(Timestamp start) {
        this.start = start;
        return this;
    }

    public AppointmentBuilder setEnd(Timestamp end) {
        this.end = end;
        return this;
    }

    public AppointmentBuilder setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
        return this;
    }

    public AppointmentBuilder setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public AppointmentBuilder setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }

    public AppointmentBuilder setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
        return this;
    }

    public Appointment build() {
        return new Appointment(this.appointmentId, this.customerId, this.title, this.description, this.location, this.contact,
                this.start, this.end, this.createDate, this.createdBy, this.lastUpdate, this.lastUpdatedBy);
    }
}
