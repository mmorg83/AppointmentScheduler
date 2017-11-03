package com.mmorg29.dbtools;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 *
 * @author mam
 * Builder model for City objects
 */
public class CityBuilder {

    private int cityId;
    private String cityName;
    private int countryId;
    private Timestamp createDate;
    private String createdBy;
    private Timestamp lastUpdate;
    private String lastUpdatedBy;

    public CityBuilder() {
    }

    /**
     * mam
     * Creates a CityBuilder object populated with info from a specified city table record
     * @param id the primary key of the database record
     * @return a CityBuilder object populated with the info from the specified city table record
     */
    public CityBuilder fromDB(int id) {
        String sql = "SELECT * FROM " + DBManager.CITY_TABLE + " WHERE cityId = " + id;
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet resultSet = statement.executeQuery(sql);) {
            if (resultSet.first()) {
                this.cityId = id;
                this.cityName = resultSet.getString("city");
                this.countryId = resultSet.getInt("countryId");
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

    public CityBuilder setCityId(int cityId) {
        this.cityId = cityId;
        return this;
    }

    public CityBuilder setCityName(String cityName) {
        this.cityName = cityName;
        return this;
    }

    public CityBuilder setCountry(int countryId) {
        this.countryId = countryId;
        return this;
    }

    public CityBuilder setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
        return this;
    }

    public CityBuilder setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public CityBuilder setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }

    public CityBuilder setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
        return this;
    }

    public City build() {
        return new City(cityId, cityName, countryId, createDate, createdBy, lastUpdate, lastUpdatedBy);
    }

}
