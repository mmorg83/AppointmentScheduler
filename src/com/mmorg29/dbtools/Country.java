package com.mmorg29.dbtools;

import com.mmorg29.appointmentscheduler.DateTimeConversion;
import com.mmorg29.appointmentscheduler.User;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 *
 * @author mam
 * A data model for the Country table in the Database
 */
public class Country implements DBObject {

    private int id;
    private String countryName;
    private Timestamp createDate;
    private String createdBy;
    private Timestamp lastUpdate;
    private String lastUpdatedBy;

    public Country(int countryId, String countryName, Timestamp createDate, String createdBy, Timestamp lastUpdate, String lastUpdatedBy) {

        this.id = countryId;
        this.countryName = countryName;
        this.createDate = createDate;
        this.createdBy = createdBy;
        this.lastUpdate = lastUpdate;
        this.lastUpdatedBy = lastUpdatedBy;
    }

    /**
     * @return the table
     */
    @Override
    public String getTable() {
        return DBManager.COUNTRY_TABLE;
    }

    /**
     * @return the id
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * @return the countryName
     */
    public String getCountryName() {
        return countryName;
    }

    /**
     * @param countryName the countryName to set
     */
    public void setCountryName(String countryName) {
        this.countryName = countryName;
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
            throw new InvalidDBOperationException("Unable to add country.");
        }
        
        this.lastUpdate = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.now());
        this.lastUpdatedBy = User.getInstance().getUserId() + "";
        this.createDate = lastUpdate;
        this.createdBy = lastUpdatedBy;
        
        int result = 0;
        String sql = "INSERT INTO " + DBManager.COUNTRY_TABLE + (" VALUES(?, ?, ?, ?, ?, ?)");
        try (DBManager dbManager = new DBManager(); PreparedStatement preparedStatement = dbManager.getPreparedStatement(sql);) {
            preparedStatement.setInt(1, this.id);
            preparedStatement.setString(2, this.countryName);
            preparedStatement.setTimestamp(3, this.createDate);
            preparedStatement.setString(4, this.createdBy);
            preparedStatement.setTimestamp(5, this.lastUpdate);
            preparedStatement.setString(6, this.lastUpdatedBy);
            result = preparedStatement.executeUpdate();
        } catch (SQLException sqlEx) {
            throw new InvalidDBOperationException("Unable to add country.");
        }
        
        if(SUCCESSFUL.test(result)) {
            return true;
        }else {
            throw new InvalidDBOperationException("Unable to add country.");
        }
    }

    //mam see DBObject interface
    @Override
    public boolean updateInDB() throws InvalidDBOperationException {
        this.lastUpdate = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.now());
        this.lastUpdatedBy = User.getInstance().getUserId() + "";
        
        int result = 0;
        String sql = "UPDATE " + DBManager.COUNTRY_TABLE + " SET country = ?, lastUpdate = ?, lastUpdateBy = ? WHERE countryId = " + this.id;
        try(DBManager dbManager = new DBManager(); PreparedStatement preparedStatement = dbManager.getPreparedStatement(sql);) {
            preparedStatement.setString(1, this.countryName);
            preparedStatement.setTimestamp(2, this.lastUpdate);
            preparedStatement.setString(3, this.lastUpdatedBy);
            result = preparedStatement.executeUpdate();
        }catch (SQLException sqlEx) {
            throw new InvalidDBOperationException("Unable to update country info.");
        }
        
        if(SUCCESSFUL.test(result)) {
            return true;
        }else {
            throw new InvalidDBOperationException("Unable to update country info.");
        }
    }
}
