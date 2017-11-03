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
 * A data model for the City table in the DB.
 */
public class City implements DBObject {

    private int id;
    private String cityName;
    private int countryId;
    private Timestamp createDate;
    private String createdBy;
    private Timestamp lastUpdate;
    private String lastUpdatedBy;

    //mam Supplies a Country object for the country this city is in
    public final Supplier<Country> COUNTRY = () -> {
        return new CountryBuilder().fromDB(this.countryId).build();
    };

    public City(int cityId, String cityName, int countryId, Timestamp createDate, String createdBy, Timestamp lastUpdate, String lastUpdatedBy) {
        this.id = cityId;
        this.cityName = cityName;
        this.countryId = countryId;
        this.createDate = createDate;
        this.createdBy = createdBy;
        this.lastUpdate = lastUpdate;
        this.lastUpdatedBy = lastUpdatedBy;
    }

    @Override
    public String getTable() {
        return DBManager.CITY_TABLE;
    }

    /**
     * @return the id
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the cityName
     */
    public String getCityName() {
        return cityName;
    }

    /**
     * @param cityName the cityName to set
     */
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    /**
     * @return the country
     */
    public int getCountryId() {
        return countryId;
    }

    /**
     * @param countryId the country to set
     */
    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    /**
     * @return the createDate
     */
    public Timestamp getCreateDate() {
        return createDate;
    }

    /**
     * @param createDate the createDate to set
     */
    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
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
            throw new InvalidDBOperationException("Unable to add city.");
        }
        
        if (!EXISTS.test(DBManager.COUNTRY_TABLE, this.countryId)) {
            throw new InvalidDBOperationException("Unable to add city.");
        }

        this.lastUpdate = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.now());
        this.lastUpdatedBy = User.getInstance().getUserId() + "";
        this.createDate = lastUpdate;
        this.createdBy = lastUpdatedBy;

        int result = 0;
        String sql = "INSERT INTO " + DBManager.CITY_TABLE + " VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (DBManager dbManager = new DBManager(); PreparedStatement preparedStatement = dbManager.getPreparedStatement(sql);) {
            preparedStatement.setInt(1, this.id);
            preparedStatement.setString(2, this.cityName);
            preparedStatement.setInt(3, this.countryId);
            preparedStatement.setTimestamp(4, this.createDate);
            preparedStatement.setString(5, this.createdBy);
            preparedStatement.setTimestamp(6, this.lastUpdate);
            preparedStatement.setString(7, this.lastUpdatedBy);
            result = preparedStatement.executeUpdate();
        } catch (SQLException sqlEx) {
            throw new InvalidDBOperationException("Unable to add city.");
        }

        if (SUCCESSFUL.test(result)) {
            return true;
        } else {
            throw new InvalidDBOperationException("Unable to add city.");
        }
    }

    //mam see DBObject interface
    @Override
    public boolean updateInDB() throws InvalidDBOperationException {
        if (!EXISTS.test(DBManager.COUNTRY_TABLE, this.countryId)) {
            throw new InvalidDBOperationException("Unable to update city info");
        }

        this.lastUpdate = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.now());
        this.lastUpdatedBy = User.getInstance().getUserId() + "";

        int result = 0;
        String sql = "UPDATE " + DBManager.CITY_TABLE + " SET city = ?, countryId = ?, lastUpdate = ?, lastUpdateBy = ?"
                + " WHERE cityId = " + this.id;
        try (DBManager dbManager = new DBManager(); PreparedStatement preparedStatement = dbManager.getPreparedStatement(sql)) {
            preparedStatement.setString(1, this.cityName);
            preparedStatement.setInt(2, this.countryId);
            preparedStatement.setTimestamp(3, this.lastUpdate);
            preparedStatement.setString(4, this.lastUpdatedBy);
            result = preparedStatement.executeUpdate();
        } catch (SQLException sqlEx) {
            throw new InvalidDBOperationException("Unable to update city info");
        }

        if (SUCCESSFUL.test(result)) {
            return true;
        } else {
            throw new InvalidDBOperationException("Unable to update city info");
        }
    }
}
