package com.mmorg29.appointmentscheduler;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author mam Singleton class used to store the important information of the user that is logged in.
 *
 */
public class User {

    //mam This variable prevents user data from changing while the user is still logged in.
    private boolean fuse = false;

    private String userName = null;
    private int userId = -1;

    //mam Singleton reference to a User class object.
    private static volatile User instance;

    /**
     * mam The Constructor. Is private to prevent users from creating separate instance objects of the User class which break the Singleton pattern.
     *
     */
    private User() {
    }

    /**
     * mam Gets a Singleton instance of the User class. Uses a Double-Checked Locking pattern to create the Singleton instance.
     *
     * @return Singleton instance of the User class.
     */
    public static User getInstance() {
        if (instance == null) {
            synchronized (User.class) {
                if (instance == null) {
                    instance = new User();
                }
            }
        }
        return instance;
    }

    /**
     * mam Sets the user data. Checks if fuse is blown before changing the user data.
     *
     * @param userName The Username of the logged in user.
     * @param userId The Primary Key field value from the user table record associated with the logged in user.
     */
    public synchronized void setUserData(String userName, int userId) {
        if (!fuse) {
            this.userName = userName;
            this.userId = userId;
        }
    }

    /**
     * mam Blows the fuse to prevent accidental changing of user data while user is logged in. Makes sure user data is set before it blows the fuse.
     */
    public synchronized void blowFuse() {
        this.fuse = userName != null && userId != -1;
    }

    //mam Getters
    /**
     * @return String representing the username of the logged in user.
     */
    public synchronized String getUserName() {
        return this.userName;
    }

    /**
     * @return Integer value representing the Primary Key from the user table record associated with the logged in user.
     */
    public synchronized int getUserId() {
        return this.userId;
    }

    /**
     * mam Logs the user out by resetting the fuse and displaying the LoginForm so a new user can log in.
     */
    public synchronized void logout() {
        try {
            ResourceBundle resourceBundle = ResourceBundle.getBundle("internationalization.AppointmentScheduler");
            Parent root = FXMLLoader.load(getClass().getResource("LoginForm.fxml"), resourceBundle);
            Scene mainFormScene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(mainFormScene);
            stage.sizeToScene();
            stage.show();
            if (this.userName != null) {
                AppointmentScheduler.LOGGER.log(Level.INFO, "{0} has logged out", this.userName);
            }
            this.fuse = false;
            this.userName = null;
            this.userId = -1;
        } catch (IOException ioEx) {
            //mam This shouldn't ever occur but if it does just exit the application.
            Platform.exit();
        }
    }
}
