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
 * a data model for the Address table
 */
public class Address implements DBObject {

    private int id;
    private String address;
    private String address2;
    private int cityId;
    private String postalCode;
    private String phoneNumber;
    private Timestamp createDate;
    private String createdBy;
    private Timestamp lastUpdate;
    private String lastUpdatedBy;
    
    public final Supplier<City> CITY = () -> {
        return new CityBuilder().fromDB(this.cityId).build();
    };

    public Address(int addressId, String address, String address2, int cityId, String postalCode, String phoneNumber,
            Timestamp createDate, String createdBy, Timestamp lastUpdate, String lastUpdatedBy) {

        this.id = addressId;
        this.address = address;
        this.address2 = address2;
        this.cityId = cityId;
        this.postalCode = postalCode;
        this.phoneNumber = phoneNumber;
        this.createDate = createDate;
        this.createdBy = createdBy;
        this.lastUpdate = lastUpdate;
        this.lastUpdatedBy = lastUpdatedBy;
    }

    //mam Getters and Setters for Address fields and info
    @Override
    public String getTable() {
        return DBManager.ADDRESS_TABLE;
    }

    @Override
    public int getId() {
        return this.id;
    }

    
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Timestamp getCreateDate() {
        return this.createDate;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public Timestamp getLastUpdate() {
        return this.lastUpdate;
    }

    public String getLastUpdatedBy() {
        return this.lastUpdatedBy;
    }

    //mam see DBObject interface
    @Override
    public boolean writeToDB() throws InvalidDBOperationException{
        if(this.id == -1) {
            throw new InvalidDBOperationException("Unable to add address.");
        }
        
        if (!EXISTS.test(DBManager.CITY_TABLE, this.cityId)) {
            throw new InvalidDBOperationException("Unable to add address.");
        }
        
        this.lastUpdate = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.now());
        this.lastUpdatedBy = User.getInstance().getUserId() + "";
        this.createDate = lastUpdate;
        this.createdBy = lastUpdatedBy;

        int result = 0;
        String sql = "INSERT INTO " + DBManager.ADDRESS_TABLE + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (DBManager dbManager = new DBManager(); PreparedStatement preparedStatement = dbManager.getPreparedStatement(sql);) {
            preparedStatement.setInt(1, this.id);
            preparedStatement.setString(2, this.address);
            preparedStatement.setString(3, this.address2);
            preparedStatement.setInt(4, this.cityId);
            preparedStatement.setString(5, this.postalCode);
            preparedStatement.setString(6, this.phoneNumber);
            preparedStatement.setTimestamp(7, this.createDate);
            preparedStatement.setString(8, this.createdBy);
            preparedStatement.setTimestamp(9, this.lastUpdate);
            preparedStatement.setString(10, this.lastUpdatedBy);
            result = preparedStatement.executeUpdate();
        } catch (SQLException sqlEx) {
            throw new InvalidDBOperationException("Unable to add address.");
        }
        
        if(SUCCESSFUL.test(result)) {
            return true;
        }else {
            throw new InvalidDBOperationException("Unable to add address.");
        }
    }

    //mam see DBObject interface
    @Override
    public boolean updateInDB() throws InvalidDBOperationException{
        if (!EXISTS.test(DBManager.CITY_TABLE, this.cityId)) {
            throw new InvalidDBOperationException("Unable to update address info.");
        }

        this.lastUpdate = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.now());
        this.lastUpdatedBy = User.getInstance().getUserId() + "";

        int result = 0;
        String sql = "UPDATE " + DBManager.ADDRESS_TABLE + " SET address = ?, address2 = ?, cityId = ?, postalCode = ?, phone = ?, lastUpdate = ?, lastUpdateBy = ?"
                + " WHERE addressId = " + this.id;
        try (DBManager dbManager = new DBManager(); PreparedStatement preparedStatement = dbManager.getPreparedStatement(sql);) {
            preparedStatement.setString(1, this.address);
            preparedStatement.setString(2, this.address2);
            preparedStatement.setInt(3, this.cityId);
            preparedStatement.setString(4, this.postalCode);
            preparedStatement.setString(5, this.phoneNumber);
            preparedStatement.setTimestamp(6, this.lastUpdate);
            preparedStatement.setString(7, this.lastUpdatedBy);
            result = preparedStatement.executeUpdate();
        } catch (SQLException sqlEx) {
            throw new InvalidDBOperationException("Unable to update address info.");
        }
        
        if(SUCCESSFUL.test(result)) {
            return true;
        }else {
            throw new InvalidDBOperationException("Unable to update address info.");
        }
    }
}
