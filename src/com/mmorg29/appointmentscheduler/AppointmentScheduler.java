/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mmorg29.appointmentscheduler;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author mmorg
 * Main Class
 * Loads and displays Login Form.
 * Creates Executor Service for appointment notifications.
 * Creates Logger for user login events.
 */
public class AppointmentScheduler extends Application {

    //mam Executor service for running appointment reminder notifications thread
    public static final ScheduledExecutorService SERVICE = Executors.newSingleThreadScheduledExecutor();
    //mam Logger to log login events
    public static final Logger LOGGER = Logger.getLogger("com.mmorg29.appointmentscheduler");

    @Override
    public void start(Stage stage) throws Exception {
        //mam setup logger to log login events.  Uses simple text format instead of XML.
        FileHandler handler = new FileHandler("Login.txt", true);
        handler.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(handler);
        ResourceBundle resourceBundle = ResourceBundle.getBundle("internationalization.AppointmentScheduler");
        Parent root = FXMLLoader.load(getClass().getResource("LoginForm.fxml"), resourceBundle);
        Scene scene = new Scene(root);
        stage.setTitle("Appointment Scheduler");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        //mam Record logout event
        if(User.getInstance().getUserName() != null) {
            AppointmentScheduler.LOGGER.log(Level.INFO, "{0} has logged out", User.getInstance().getUserName());
        }
        //mam Gracefully shutdown the executor service for the appointment reminder notifications thread
        SERVICE.shutdown();
        SERVICE.awaitTermination(5, TimeUnit.SECONDS);
        SERVICE.shutdownNow();
        super.stop();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //mam For testing purposes sets language to spanish and changes timezone
        //Locale.setDefault(new Locale("es", "ES"));
        //TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
        launch(args);
    }

}
