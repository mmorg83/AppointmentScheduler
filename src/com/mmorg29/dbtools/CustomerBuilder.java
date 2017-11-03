package com.mmorg29.dbtools;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 *
 * @author mam
 * Builder model for Customer objects
 */
public class CustomerBuilder {

    private int customerId;
    private String customerName;
    private int addressId;
    private boolean status;
    private Timestamp createDate;
    private String createdBy;
    private Timestamp lastUpdate;
    private String lastUpdatedBy;

    public CustomerBuilder() {

    }

    /**
     * mam
     * Creates a CustomerBuilder object populated with info from a specified customer table record
     * @param id the primary key of the database record
     * @return a CustomerBuilder object populated with the info from the specified customer table record
     */
    public CustomerBuilder fromDB(int id) {
        String sql = "SELECT * FROM " + DBManager.CUSTOMER_TABLE + " WHERE customerId = " + id;
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet resultSet = statement.executeQuery(sql);) {
            if (resultSet.first()) {
                this.customerId = id;
                this.customerName = resultSet.getString("customerName");
                this.addressId = resultSet.getInt("addressId");
                byte customerStatus = resultSet.getByte("active");
                if (customerStatus > 0) {
                    this.status = true;
                }
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

    public CustomerBuilder setCustomerId(int customerId) {
        this.customerId = customerId;
        return this;
    }

    public CustomerBuilder setCustomerName(String customerName) {
        this.customerName = customerName;
        return this;
    }

    public CustomerBuilder setAddress(int addressId) {
        this.addressId = addressId;
        return this;
    }

    public CustomerBuilder setStatus(boolean status) {
        this.status = status;
        return this;
    }

    public CustomerBuilder setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
        return this;
    }

    public CustomerBuilder setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public CustomerBuilder setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }

    public CustomerBuilder setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
        return this;
    }

    public Customer build() {
        return new Customer(this.customerId, this.customerName, this.addressId, this.status, this.createDate,
                this.createdBy, this.lastUpdate, this.lastUpdatedBy);
    }
}
