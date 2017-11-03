/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appointmentschedulertests;

import com.mmorg29.dbtools.DBManager;
import com.mmorg29.appointmentscheduler.DateTimeConversion;
import com.mmorg29.appointmentscheduler.SecurePassword;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;


/**
 *
 * @author mmorg
 */
public class AddUsersToDB {
    
    public static void main(String[] args) {
        System.out.println(insertUser("test", "test"));
        System.out.println(insertUser("mmorg29", "administrator"));
        System.out.println(insertUser("user1", "user1Password"));
        System.out.println(insertUser("user2", "user2P@ssword"));   
    }
    
    public static boolean insertUser(String user, String password) {
        String securePassword = SecurePassword.makeSecurePassword(password);
        Timestamp now = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.now());
        String sql = "INSERT INTO " + DBManager.USER_TABLE + " Values(?, ?, ?, ?, ?, ?, ?, ?)";
        try(DBManager dbManager = new DBManager(); PreparedStatement preparedStatement = dbManager.getPreparedStatement(sql);){
            preparedStatement.setInt(1, DBManager.getNextId(DBManager.USER_TABLE));
            preparedStatement.setString(2, user);
            preparedStatement.setString(3, securePassword);
            preparedStatement.setInt(4, 1);
            preparedStatement.setString(5, "administrator");
            preparedStatement.setTimestamp(6, now);
            preparedStatement.setTimestamp(7, now);
            preparedStatement.setString(8, "administrator");
            preparedStatement.execute();
            return true;
        }catch (SQLException sqlEx) {
            System.out.println(sqlEx.getMessage());
            return false;
        }
    }
}
