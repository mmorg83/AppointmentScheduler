/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appointmentschedulertests;

import com.mmorg29.appointmentscheduler.DateTimeConversion;
import com.mmorg29.dbtools.DBManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 *
 * @author mmorg
 */
public class AddCitiesToDB {
    public static void main(String[] args) {
        System.out.println(insertCity("Phoenix", 1));
        System.out.println(insertCity("New York", 1));
        System.out.println(insertCity("London", 2));
        System.out.println(insertCity("Liverpool", 2));
        System.out.println(insertCity("Dublin", 3));
        System.out.println(insertCity("Cork", 3));
        System.out.println(insertCity("Berlin", 4));
        System.out.println(insertCity("Munich", 4));
        System.out.println(insertCity("Paris", 5));
        System.out.println(insertCity("Marseille", 5));
        System.out.println(insertCity("Madrid", 6));
        System.out.println(insertCity("Barcelona", 6));
        System.out.println(insertCity("Mexico City", 7));
        System.out.println(insertCity("Puebla", 7));
        System.out.println(insertCity("Panama City", 8));
        System.out.println(insertCity("Colón", 8));
        
    }

    private static boolean insertCity(String cityName, int countryId) {
        Timestamp now = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.now());
        String sql = "INSERT INTO " + DBManager.CITY_TABLE + " Values(?, ?, ?, ?, ?, ?, ?)";
        try (DBManager dbManager = new DBManager(); PreparedStatement preparedStatement = dbManager.getPreparedStatement(sql);) {
            preparedStatement.setInt(1, DBManager.getNextId(DBManager.CITY_TABLE));
            preparedStatement.setString(2, cityName);
            preparedStatement.setInt(3, countryId);
            preparedStatement.setTimestamp(4, now);
            preparedStatement.setString(5, "administrator");
            preparedStatement.setTimestamp(6, now);
            preparedStatement.setString(7, "administrator");
            preparedStatement.execute();
            return true;
        } catch (SQLException sqlEx) {
            System.out.println(sqlEx.getMessage());
            return false;
        }
    }
}
