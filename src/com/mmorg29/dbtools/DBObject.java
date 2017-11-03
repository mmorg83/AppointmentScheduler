/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mmorg29.dbtools;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Function;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 *
 * @author mmorg
 */
public interface DBObject {
    /**
     *mam 
     * Validates that a record exists in a database table
     */
    BiPredicate<String, Integer> EXISTS = (table, id) -> DBManager.recordExists(table, id);
   
    /**
     *mam 
     * Gets the next primary key of the desired table
     */
    Function<String, Integer> NEXT_ID = DBManager::getNextId;
    /**
     *mam 
     * Validates that a create or updates operation completed successfully
     */
    Predicate<Integer> SUCCESSFUL = (result) -> result == 1;


    /**
     * mam
     * Gets the name of a database table the implementing data model is created for or from.
     * @return the table name
     */
    String getTable();

    /**
     * mam
     * Gets the primary key of the database record the implementing data model is created for or from.
     * @return the primary key
     */
    int getId();

    /**
     * mam
     * Creates a new database record in the table the implementing data model is created for.
     * @return true if creation successful; false otherwise
     * @throws InvalidDBOperationException if the operation raises an exception
     */
    boolean writeToDB() throws InvalidDBOperationException;
    
    /**
     * mam
     * Updates the database record the implementing data model is created from.
     * @return true when the update is successful; false otherwise
     * @throws InvalidDBOperationException if the operation raises and exception
     */
    boolean updateInDB() throws InvalidDBOperationException;
    
    /**
     * mam
     * Deletes the database record the implementing data model is created from.
     * @return
     * @throws InvalidDBOperationException
     */
    default boolean deleteFromDB() throws InvalidDBOperationException {
        int result = 0;
        String table = this.getTable();
        String sql = "DELETE FROM " + table + " WHERE " + table + "Id = " + getId();
        try(DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement()) {
            result = statement.executeUpdate(sql);
        }catch (SQLException sqlEx) {
            throw new InvalidDBOperationException("Unable to delete " + table + ".");
        }
        if(SUCCESSFUL.test(result)) {
        return true;
        }else {
            throw new InvalidDBOperationException("Unable to delete" + table + ".");
        }
    }
}
