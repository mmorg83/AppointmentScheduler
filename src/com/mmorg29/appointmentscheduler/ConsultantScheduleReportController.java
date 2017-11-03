/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mmorg29.appointmentscheduler;

import com.mmorg29.dbtools.Appointment;
import com.mmorg29.dbtools.AppointmentBuilder;
import com.mmorg29.dbtools.DBManager;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

/**
 * FXML Controller class
 *
 * @author mam
 * Handles the Creation and GUI interaction of the Consultant Schedules Report.
 * Consultant Schedule information is shown on a daily basis.
 */
public class ConsultantScheduleReportController implements Initializable {

    @FXML
    private ComboBox<String> consultantSelector;
    @FXML
    private DatePicker consultantScheduleDatePicker;
    @FXML
    private ListView<String> consultantScheduleListView;

    private Tab parentTab;

    private final Map<String, Integer> consultants = new HashMap<>();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadConsultants();
        this.consultantSelector.getSelectionModel().select(0);
        this.consultantScheduleDatePicker.setValue(LocalDate.now());
        loadConsultantSchedule();
    }

    @FXML
    private void handleConsultantSelectorOnAction(ActionEvent event) {
        loadConsultantSchedule();
    }

    @FXML
    private void handleConsultantScheduleDatePickerOnAction(ActionEvent event) {
        loadConsultantSchedule();
    }

    /**
     * Creates the list of Consultants to select from to view their schedule
     */
    private void loadConsultants() {
        String sql = "SELECT userId, userName FROM " + DBManager.USER_TABLE + " ORDER BY userName ASC";
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet resultSet = statement.executeQuery(sql);) {
            while (resultSet.next()) {
                this.consultants.put(resultSet.getString(2), resultSet.getInt(1));
            }
            this.consultantSelector.setItems(FXCollections.observableArrayList(this.consultants.keySet()).sorted());
        } catch (SQLException sqlEx) {
            Alert alert = new Alert(AlertType.ERROR, "There was an issue loading consultant info.", ButtonType.OK);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> {
                        alert.close();
                        if (parentTab != null) {
                            parentTab.getTabPane().getTabs().remove(parentTab);
                        }
                    });
        }
    }

    /**
     * mam
     * Creates the Appointment objects of the Consultant on the selected date and adds them to the ListView that displays the appointment info.
     */
    private void loadConsultantSchedule() {
        this.consultantScheduleListView.getItems().clear();
        Integer consultantId = this.consultants.get(this.consultantSelector.getValue());
        Timestamp utcStart = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.of(this.consultantScheduleDatePicker.getValue(), LocalTime.MIN));
        Timestamp utcEnd = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.of(this.consultantScheduleDatePicker.getValue(), LocalTime.MAX));
        String sql = "SELECT * FROM " + DBManager.APPOINTMENT_TABLE + " WHERE start BETWEEN '"
                + utcStart.toString() + "' AND '" + utcEnd.toString() + "' AND createdBy = '" + consultantId + "' ORDER BY start ASC";
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet resultSet = statement.executeQuery(sql);) {
            while(resultSet.next()) {
                AppointmentBuilder appointmentBuilder = new AppointmentBuilder();
                appointmentBuilder.setAppointmentId(resultSet.getInt(1));
                appointmentBuilder.setCustomerId(resultSet.getInt(2));
                appointmentBuilder.setTitle(resultSet.getString(3));
                appointmentBuilder.setDescription(resultSet.getString(4));
                appointmentBuilder.setLocation(resultSet.getString(5));
                appointmentBuilder.setContact(resultSet.getString(6));
                appointmentBuilder.setStart(resultSet.getTimestamp(8));
                appointmentBuilder.setEnd(resultSet.getTimestamp(9));
                appointmentBuilder.setCreateDate(resultSet.getTimestamp(10));
                appointmentBuilder.setCreatedBy(resultSet.getString(11));
                appointmentBuilder.setLastUpdate(resultSet.getTimestamp(12));
                appointmentBuilder.setLastUpdatedBy(resultSet.getString(13));
                Appointment appointment = appointmentBuilder.build();
                String[] appointmentDisplayStrings = appointment.getDisplayStrings(null);
                this.consultantScheduleListView.getItems().add(appointmentDisplayStrings[1]);
            }
        } catch (SQLException sqlEx) {
            Alert alert = new Alert(AlertType.ERROR, "There was an issue loading the consultant's schedule.", ButtonType.OK);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> {
                        alert.close();
                        if (parentTab != null) {
                            parentTab.getTabPane().getTabs().remove(parentTab);
                        }
                    });
        }
    }
    
    /**
     * mam
     * Sets a reference to the tab that this report is displayed in
     * @param tab the tab displaying this report
     */
    public void setParentTab(Tab tab) {
        this.parentTab = tab;
    }
}
