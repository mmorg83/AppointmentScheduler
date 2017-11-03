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
public class AddCountriesToDB {

    public static void main(String[] args) {
        System.out.println(insertCountry("United States"));
        System.out.println(insertCountry("England"));
        System.out.println(insertCountry("Ireland"));
        System.out.println(insertCountry("Germany"));
        System.out.println(insertCountry("France"));
        System.out.println(insertCountry("Spain"));
        System.out.println(insertCountry("Mexico"));
        System.out.println(insertCountry("Panama"));

    }

    private static boolean insertCountry(String countryName) {
        Timestamp now = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.now());
        String sql = "INSERT INTO " + DBManager.COUNTRY_TABLE + " Values(?, ?, ?, ?, ?, ?)";
        try (DBManager dbManager = new DBManager(); PreparedStatement preparedStatement = dbManager.getPreparedStatement(sql);) {
            preparedStatement.setInt(1, DBManager.getNextId(DBManager.COUNTRY_TABLE));
            preparedStatement.setString(2, countryName);
            preparedStatement.setTimestamp(3, now);
            preparedStatement.setString(4, "administrator");
            preparedStatement.setTimestamp(5, now);
            preparedStatement.setString(6, "administrator");
            preparedStatement.execute();
            return true;
        } catch (SQLException sqlEx) {
            System.out.println(sqlEx.getMessage());
            return false;
        }
    }

}
