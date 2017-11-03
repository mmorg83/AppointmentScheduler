/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mmorg29.appointmentscheduler;

import com.mmorg29.dbtools.DBManager;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import com.mmorg29.reportdatamodels.AppointmentTypesReport;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

/**
 * FXML Controller class
 *
 * @author mam
 * Handles the Creation and GUI interaction for the Appointment Types by Month Report.
 */
public class AppointmentTypesByMonthReportController implements Initializable {

    @FXML
    private ComboBox<String> month_selector;
    @FXML
    private Spinner<Integer> year_spinner;
    @FXML
    private TableColumn<AppointmentTypesReport, String> appointment_types_column;
    @FXML
    private TableColumn<AppointmentTypesReport, Integer> appointment_totals_column;
    @FXML
    private TableView<AppointmentTypesReport> appointment_types_table;

    private Tab parentTab;

    private final ObservableList<AppointmentTypesReport> tableData = FXCollections.observableArrayList();
    private final ObservableList<String> months
            = FXCollections.observableArrayList(
                    "January",
                    "February",
                    "March",
                    "April",
                    "May",
                    "June",
                    "July",
                    "August",
                    "September",
                    "October",
                    "November",
                    "December"
            );

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Label label = new Label("No Appointments Scheduled This Month");
        label.setStyle("-fx-font-size:20px");
        this.appointment_types_table.setPlaceholder(label);
        this.appointment_types_column.setCellValueFactory(
                new PropertyValueFactory<>("appointmentType")
        );
        this.appointment_totals_column.setCellValueFactory(
                new PropertyValueFactory<>("total")
        );
        this.month_selector.setItems(months);
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue() - 1;
        int year = now.getYear();
        month_selector.getSelectionModel().select(month);
        SpinnerValueFactory.IntegerSpinnerValueFactory spinnerIntegerFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(2010, 2050, year);
        spinnerIntegerFactory.valueProperty().addListener((obs, oldValue, newValue) -> {
            loadMonthReport();
        });
        this.year_spinner.valueFactoryProperty().set(spinnerIntegerFactory);
        loadMonthReport();
    }

    /**
     * mam
     * Sets a reference to the tab that this report is displayed in
     * @param tab the tab displaying this report
     */
    public void setParentTab(Tab tab) {
        this.parentTab = tab;
    }

    @FXML
    private void handleMonthSelectorOnAction(ActionEvent event) {
        loadMonthReport();
    }

    /**
     * Creates The AppointmentTypesReport data models to display the number of appointments in the TableView.
     */
    private void loadMonthReport() {
        tableData.clear();
        int year = this.year_spinner.getValue();
        Month month = Month.valueOf(this.month_selector.getValue().toUpperCase());
        LocalDateTime start = LocalDateTime.of(LocalDate.of(year, month, 1), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(start.toLocalDate().with(TemporalAdjusters.lastDayOfMonth()), LocalTime.MAX);
        Timestamp utcStart = DateTimeConversion.UTC_TIMESTAMP.apply(start);
        Timestamp utcEnd = DateTimeConversion.UTC_TIMESTAMP.apply(end);
        String sql = "SELECT title, Count(title) AS total FROM " + DBManager.APPOINTMENT_TABLE
                + " WHERE start BETWEEN '" + utcStart.toString() + "' AND '" + utcEnd.toString() + "' GROUP BY title";
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet resultSet = statement.executeQuery(sql);) {
            while (resultSet.next()) {
                AppointmentTypesReport appointmentTypesReport = new AppointmentTypesReport(resultSet.getString(1), resultSet.getInt(2));
                tableData.add(appointmentTypesReport);
            }
            this.appointment_types_table.setItems(this.tableData);
        } catch (SQLException sqlEx) {
            Alert alert = new Alert(AlertType.ERROR, "Unable to load the appointment types report for the selected month.", ButtonType.OK);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> {
                        alert.close();
                        Platform.runLater(() -> {
                            if (parentTab != null) {
                                parentTab.getTabPane().getTabs().remove(parentTab);
                            }
                        });
                    });
        }
    }

}
