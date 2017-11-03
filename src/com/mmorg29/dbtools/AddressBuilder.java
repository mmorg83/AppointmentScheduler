package com.mmorg29.dbtools;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 *
 * @author mam 
 * Builder pattern for the Address data models.
 */
public class AddressBuilder {

    private int addressId;
    private String address;
    private String address2;
    private int cityId;
    private String postalCode;
    private String phoneNumber;
    private Timestamp createDate;
    private String createdBy;
    private Timestamp lastUpdate;
    private String lastUpdatedBy;

    public AddressBuilder() {
    }

    /**
     * mam
     * Creates an AddressBuilder from the primary key of a desired Address record
     * @param id the primary key of the desired address record
     * @return an AddressBuilder with the info of the desired Address record
     */
    public AddressBuilder fromDB(int id) {
        String sql = "SELECT * FROM " + DBManager.ADDRESS_TABLE + " WHERE addressId = " + id;
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.first()) {
                this.addressId = id;
                this.address = resultSet.getString("address");
                this.address2 = resultSet.getString("address2");
                this.cityId = resultSet.getInt("cityId");
                this.postalCode = resultSet.getString("postalCode");
                this.phoneNumber = resultSet.getString("phone");
                this.createDate = resultSet.getTimestamp("createDate");
                this.createdBy = resultSet.getString("createdBy");
                this.lastUpdate = resultSet.getTimestamp("lastUpdate");
                this.lastUpdatedBy = resultSet.getString("lastUpdateBy");
            } else {
                return null;
            }
        } catch (SQLException sqlEx) {
            return null;
        }
        return this;
    }

    public AddressBuilder setAddressId(int addressId) {
        this.addressId = addressId;
        return this;
    }

    public AddressBuilder setAddress(String address) {
        this.address = address;
        return this;
    }

    public AddressBuilder setAddress2(String address2) {
        this.address2 = address2;
        return this;
    }

    public AddressBuilder setCityId(int cityId) {
        this.cityId = cityId;
        return this;
    }

    public AddressBuilder setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    public AddressBuilder setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public AddressBuilder setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
        return this;
    }

    public AddressBuilder setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public AddressBuilder setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }

    public AddressBuilder setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
        return this;
    }

    public Address build() {
        return new Address(addressId, address, address2, cityId, postalCode, phoneNumber, createDate, createdBy, lastUpdate, lastUpdatedBy);
    }

}
