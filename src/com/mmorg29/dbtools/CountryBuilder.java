package com.mmorg29.dbtools;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 *
 * @author mam
 * Builder model for Country objects
 */
public class CountryBuilder {

    private int countryId = 0;
    private String countryName;
    private Timestamp createDate;
    private String createdBy;
    private Timestamp lastUpdate;
    private String lastUpdatedBy;

    public CountryBuilder() {
    }

    /**
     * mam
     * Creates a CountryBuilder object populated with info from a specified country table record
     * @param id the primary key of the database record
     * @return a CountryBuilder object populated with the info from the specified country table record
     */
    public CountryBuilder fromDB(int id) {
        String sql = "SELECT * FROM " + DBManager.COUNTRY_TABLE + " WHERE countryId = " + id;
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet resultSet = statement.executeQuery(sql);) {
            if (resultSet.first()) {
                this.countryId = id;
                this.countryName = resultSet.getString("country");
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

    public CountryBuilder setCountryId(int countryId) {
        this.countryId = countryId;
        return this;
    }

    public CountryBuilder setCountryName(String countryName) {
        this.countryName = countryName;
        return this;
    }

    public CountryBuilder setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
        return this;
    }

    public CountryBuilder setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public CountryBuilder setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }

    public CountryBuilder setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
        return this;
    }

    public Country build() {
        return new Country(countryId, countryName, createDate, createdBy, lastUpdate, lastUpdatedBy);
    }

}
