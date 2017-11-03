/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mmorg29.appointmentscheduler;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author mam
 * Handles the GUI operation of the user login.
 * Can display in English and Spanish languages.
 */
public class LoginFormController implements Initializable {

    @FXML
    private Label login_error_message_lbl;
    @FXML
    private TextField login_username_input;
    @FXML
    private PasswordField login_password_input;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //mam Don't show error message when scene loads.
        login_error_message_lbl.setVisible(false);
    }

    @FXML
    private void handlePasswordEnterKey(KeyEvent event) throws IOException {
        //mam Get stage from event and call loadMainForm function
        if (event.getCode() == KeyCode.ENTER) {
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            loadMainForm(window);
        }
    }

    @FXML
    private void handleLoginButtonAction(ActionEvent event) throws IOException {
        //mam Get stage from event and call loadMainForm function
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        loadMainForm(window);
    }

    /**
     * mam 
     * Verifies login info and displays the main form if the login info is correct.
     * Logs the login event.
     * @param stage reference to the stage to display the main form in.
     * @throws IOException 
     */
    private void loadMainForm(final Stage stage) throws IOException {
        try {
            SecurePassword.verifyUserAndPassword(login_username_input.getText(), login_password_input.getText());
            AppointmentScheduler.LOGGER.log(Level.INFO, "Successful login for: {0}", this.login_username_input.getText());
            User.getInstance().blowFuse();
            Parent mainFormParent = FXMLLoader.load(getClass().getResource("MainForm.fxml"));
            Scene mainFormScene = new Scene(mainFormParent);
            stage.setScene(mainFormScene);
            stage.show();
        } catch (InvalidLoginException ilEx) {
            //mam Login attempt unsuccessful display error message label and log failed login attempt.
            login_error_message_lbl.setVisible(true);
            AppointmentScheduler.LOGGER.log(Level.WARNING, "Failed login attempt for: {0}", this.login_username_input.getText());
        }
    }

}
