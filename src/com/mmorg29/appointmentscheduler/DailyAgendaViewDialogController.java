/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mmorg29.appointmentscheduler;

import com.mmorg29.agenda.DailyAgenda;
import com.mmorg29.dbtools.Appointment;
import com.mmorg29.dbtools.InvalidDBOperationException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * FXML Controller class
 *
 * @author mam
 * This Class provides a detailed view of appointment info for a date selected in the Monthly or Weekly schedules.
 * It allows the User to remove an appointments created from their schedule.
 */
public class DailyAgendaViewDialogController implements Initializable {

    @FXML
    private Label date_label;
    @FXML
    private ListView<String> daily_agenda_list_view;
    @FXML
    private Button remove_button;
    @FXML
    private Button close_button;

    private DailyAgenda dailyAgenda;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    /**
     * mam
     * Removes the selected appointment and its reminder from the database. 
     */
    @FXML
    private void handleRemoveOnAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Remove selected appointment? (THIS ACTION CAN NOT BE UNDONE)");
        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> {
                    Appointment appointment = this.dailyAgenda.getAppointment(this.daily_agenda_list_view.getSelectionModel().getSelectedItem());
                    try {
                        if (appointment.deleteFromDB()) {
                            appointment.DELETE_REMINDER.getAsBoolean();
                            dailyAgenda.removeAppointment(appointment);
                            this.daily_agenda_list_view.setItems(dailyAgenda.getDisplayableAgenda(true));
                        }
                    } catch (InvalidDBOperationException idboEx) {
                        Alert dbErrorAlert = new Alert(Alert.AlertType.ERROR, idboEx.getMessage(), ButtonType.OK);
                        dbErrorAlert.initStyle(StageStyle.UNDECORATED);
                        dbErrorAlert.initModality(Modality.APPLICATION_MODAL);
                        dbErrorAlert.showAndWait()
                                .filter(errorResponse -> errorResponse == ButtonType.OK)
                                .ifPresent(errorResponse -> {
                                    dbErrorAlert.close();
                                    alert.close();
                                });
                    }
                });
    }

    @FXML
    private void handleCloseOnAction(ActionEvent event) {
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
    }

    public void setDateLabel(LocalDate date) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
        this.date_label.setText(date.format(dateFormatter));
    }

    public void setDailyAgenda(DailyAgenda dailyAgenda) {
        this.dailyAgenda = dailyAgenda;
        this.daily_agenda_list_view.setItems(dailyAgenda.getDisplayableAgenda(true));
    }

    public void setSelectedAppointment(int index) {
        this.daily_agenda_list_view.getSelectionModel().select(index);
        this.daily_agenda_list_view.getFocusModel().focus(index);
        this.daily_agenda_list_view.scrollTo(index);
    }
}
