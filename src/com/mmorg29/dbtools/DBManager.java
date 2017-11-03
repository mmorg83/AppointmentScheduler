package com.mmorg29.dbtools;

import com.mmorg29.appointmentscheduler.User;
import java.io.Closeable;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

/**
 *
 * @author mam
 * This class is used to interface with the database. 
 * Implements closable so that it can be used in a try with resources.
 */
public class DBManager implements Closeable {

    //mam Database connection strings
    private final String DRIVER = "com.mysql.jdbc.Driver";
    private final String DB = "U03Pvd";
    private final String URL = "jdbc:mysql://52.206.157.109/" + DB;
    private final String USER = "U03Pvd";
    private final String PASSWORD = "53688044881";

    //mam table name constants
    public static final String USER_TABLE = "user";
    public static final String CITY_TABLE = "city";
    public static final String ADDRESS_TABLE = "address";
    public static final String COUNTRY_TABLE = "country";
    public static final String CUSTOMER_TABLE = "customer";
    public static final String APPOINTMENT_TABLE = "appointment";
    public static final String REMINDER_TABLE = "reminder";
    public static final String INCREMENT_TYPES_TABLE = "incrementtypes";

    private Connection conn = null;

    public DBManager() throws SQLException {
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException cnfEx) {
            Alert alert = new Alert(AlertType.ERROR, "Application is unable to access database. It will be closed.", ButtonType.OK);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> {
                        alert.close();
                        Platform.exit();
                    });
        }
    }

    public Statement getStatement() throws SQLException {
        return this.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
    }

    public PreparedStatement getPreparedStatement(String sql) throws SQLException {
        return this.conn.prepareStatement(sql);
    }

    @Override
    public void close() {
        try {
            if (!conn.isClosed()) {
                conn.commit();
                conn.close();
                conn = null;
            }
        } catch (SQLException sqlEx) {
            //mam If this occurs it means that database is not reachable and there is no way to gracefully close connection
            conn = null;
        }
    }

    /**
     * mam
     * Gets the next primary key for the desired database table
     * @param table the database table to get the next primary key from
     * @return the next primary key for the desired table; if an error occurs -1 is returned
     */
    public static int getNextId(String table) {
        String sql = "SELECT MAX(" + table + "Id) FROM " + table;
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet rs = statement.executeQuery(sql);) {
            rs.first();
            return rs.getInt(1) + 1;
        } catch (SQLException ex) {
            return -1;
        }
    }

    /**
     * mam
     * Tests whether a record exists in the desired database table.
     * Used to validate that a foreign key exists prior to creating or updating a record in another table with a relationship to the desired table.
     * @param table the desired table to check for a record
     * @param id the primary key of the record to check
     * @return true if the record exists; false otherwise
     */
    public static boolean recordExists(String table, int id) {
        String sql = "SELECT Count(*) FROM " + table + " WHERE " + table + "Id = " + id;
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet rs = statement.executeQuery(sql);) {
            if (rs.first()) {
                return rs.getInt(1) == 1;
            } else {
                return false;
            }
        } catch (SQLException sqlEx) {
            return false;
        }
    }

}
