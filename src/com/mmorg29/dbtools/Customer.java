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
 * A data model for the Customer Table in the Database
 */
public class Customer implements DBObject {

    private int id;
    private String customerName;
    private int addressId;
    private boolean status;
    private Timestamp createDate;
    private String createdBy;
    private Timestamp lastUpdate;
    private String lastUpdatedBy;
    
    //mam Supplies a Address object for the address of the customer
    public final Supplier<Address> ADDRESS = () -> {
        return new AddressBuilder().fromDB(this.addressId).build();
    };

    public Customer(int customerId, String customerName, int addressId, boolean status, Timestamp createDate,
            String createdBy, Timestamp lastUpdate, String lastUpdatedBy) {

        this.id = customerId;
        this.customerName = customerName;
        this.addressId = addressId;
        this.status = status;
        this.createDate = createDate;
        this.createdBy = createdBy;
        this.lastUpdate = lastUpdate;
        this.lastUpdatedBy = lastUpdatedBy;
    }

    @Override
    public String getTable() {
        return DBManager.CUSTOMER_TABLE;
    }

    /**
     * @return the id
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * @return the customerName
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * @param customerName the customerName to set
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * @return the address
     */
    public int getAddressId() {
        return addressId;
    }

    /**
     * @param addressId the address to set
     */
    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    /**
     * @return the status
     */
    public boolean getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(boolean status) {
        this.status = status;
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
     * @param lastUpdate the lastUpdate to set
     */
    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    /**
     * @return the lastUpdatedBy
     */
    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    /**
     * @param lastUpdatedBy the lastUpdatedBy to set
     */
    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    //mam see DBObject interface
    @Override
    public boolean writeToDB() throws InvalidDBOperationException {
        if(this.id == -1) {
            throw new InvalidDBOperationException("Unable to add customer.");
        }
        
        if (!EXISTS.test(DBManager.ADDRESS_TABLE, this.addressId)) {
            throw new InvalidDBOperationException("Unable to add customer.");
        }

        this.lastUpdate = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.now());
        this.lastUpdatedBy = User.getInstance().getUserId() + "";
        this.createDate = lastUpdate;
        this.createdBy = lastUpdatedBy;

        int result = 0;
        String sql = "INSERT INTO " + DBManager.CUSTOMER_TABLE + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (DBManager dbManager = new DBManager(); PreparedStatement preparedStatement = dbManager.getPreparedStatement(sql);) {
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, this.customerName);
            preparedStatement.setInt(3, this.addressId);
            preparedStatement.setBoolean(4, this.status);
            preparedStatement.setTimestamp(5, this.createDate);
            preparedStatement.setString(6, this.createdBy);
            preparedStatement.setTimestamp(7, this.lastUpdate);
            preparedStatement.setString(8, this.lastUpdatedBy);
            result = preparedStatement.executeUpdate();
        } catch (SQLException sqlEx) {
            throw new InvalidDBOperationException("Unable to add customer.");
        }
        
        if(SUCCESSFUL.test(result)) {
            return true;
        }else {
            throw new InvalidDBOperationException("Unable to add customer.");
        }
    }

    //mam see DBObject interface
    @Override
    public boolean updateInDB() throws InvalidDBOperationException {
        if (!EXISTS.test(DBManager.ADDRESS_TABLE, this.addressId)) {
            throw new InvalidDBOperationException("Unable to update customer info.");
        }

        this.lastUpdate = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.now());
        this.lastUpdatedBy = User.getInstance().getUserId() + "";
        
        int result = 0;
        String sql = "UPDATE " + DBManager.CUSTOMER_TABLE + " SET customerName = ?, addressId = ?, active = ?, lastUpdate = ?, lastUpdateBy = ? "
                + "WHERE customerId = " + this.id;
        try (DBManager dbManager = new DBManager(); PreparedStatement preparedStatement = dbManager.getPreparedStatement(sql);) {
            preparedStatement.setString(1, this.customerName);
            preparedStatement.setInt(2, this.addressId);
            preparedStatement.setBoolean(3, this.status);
            preparedStatement.setTimestamp(4, this.lastUpdate);
            preparedStatement.setString(5, this.lastUpdatedBy);
            result = preparedStatement.executeUpdate();
        } catch (SQLException sqlEx) {
            throw new InvalidDBOperationException("Unable to update customer info.");
        }
        
        if(SUCCESSFUL.test(result)) {
            return true;
        }else {
            throw new InvalidDBOperationException("Unable to update customer info.");
        }
    }

}
