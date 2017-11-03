/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mmorg29.appointmentscheduler;

import com.mmorg29.dbtools.Customer;
import com.mmorg29.dbtools.CustomerBuilder;
import com.mmorg29.dbtools.DBManager;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

/**
 * FXML Controller class
 *
 * @author mmorg
 */
public class CustomerHistoryReportController implements Initializable {

    @FXML
    private ComboBox<String> customer_selector;
    @FXML
    private ListView<String> customer_history_list_view;

    private Map<String, Customer> customers = new HashMap<>();

    private Tab parentTab;

    private final Predicate<String> NOT_EMPTY = (input) -> input != null && !input.isEmpty();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCustomers();
        ObservableList<String> sortedCustomers = FXCollections.observableArrayList(customers.keySet()).sorted();
        this.customer_selector.setItems(sortedCustomers);
        this.customer_selector.getSelectionModel().select(0);
        loadCustomerHistory();
    }

    @FXML
    private void handleCustomerSelectorOnAction(ActionEvent event) {
        loadCustomerHistory();
    }

    /**
     * Creates a Customer object for each customer and adds it to a HashMap for later retrieval.
     */
    private void loadCustomers() {
        String sql = "SELECT * FROM " + DBManager.CUSTOMER_TABLE + " ORDER BY customerName ASC";
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet resultSet = statement.executeQuery(sql);) {
            while (resultSet.next()) {
                CustomerBuilder customerBuilder = new CustomerBuilder();
                customerBuilder.setCustomerId(resultSet.getInt(1));
                customerBuilder.setCustomerName(resultSet.getString(2));
                customerBuilder.setAddress(resultSet.getInt(3));
                customerBuilder.setStatus(resultSet.getBoolean(4));
                customerBuilder.setCreateDate(resultSet.getTimestamp(5));
                customerBuilder.setCreatedBy(resultSet.getString(6));
                customerBuilder.setLastUpdate(resultSet.getTimestamp(7));
                customerBuilder.setLastUpdatedBy(resultSet.getString(8));
                Customer customer = customerBuilder.build();
                this.customers.put(customer.getCustomerName(), customer);
            }
        } catch (SQLException sqlEx) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "There was an issue retrieving required data.  Report will be closed.", ButtonType.OK);
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
    
    /**
     * mam
     * Sets a reference to the tab that this report is displayed in
     * @param tab the tab displaying this report
     */
    public void setParentTab(Tab tab) {
        this.parentTab = tab;
    }
    
    /**
     * Creates the customer history report for all appointments the selected customer has ever had.
     * Displays the appointment history for the customer in a ListView
     */
    private void loadCustomerHistory() {
        this.customer_history_list_view.getItems().clear();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        Customer customer = this.customers.get(this.customer_selector.getValue());
        String sql = "SELECT * FROM " + DBManager.APPOINTMENT_TABLE
                + " JOIN " + DBManager.USER_TABLE + " ON user.userId = CAST(appointment.createdBy AS UNSIGNED) WHERE appointment.customerId = "
                + customer.getId() + " ORDER BY appointment.start ASC";
        try (DBManager dbManager = new DBManager();
                Statement statement = dbManager.getStatement();
                ResultSet resultSet = statement.executeQuery(sql);) {
            while (resultSet.next()) {
                LocalDateTime zonedStartDateTime = DateTimeConversion.ZONED_LOCAL_DATE_TIME.apply(resultSet.getTimestamp(8));
                LocalDateTime zonedEndDateTime = DateTimeConversion.ZONED_LOCAL_DATE_TIME.apply(resultSet.getTimestamp(9));
                StringBuilder customerAppointmentBuilder = new StringBuilder();
                customerAppointmentBuilder.append(zonedStartDateTime.toLocalDate().format(dateFormatter));
                customerAppointmentBuilder.append(" ").append(zonedStartDateTime.toLocalTime().format(timeFormatter)).append(" - ");
                customerAppointmentBuilder.append(zonedEndDateTime.toLocalTime().format(timeFormatter));
                customerAppointmentBuilder.append("\nType: ").append(resultSet.getString(3));
                customerAppointmentBuilder.append("\nConsultant: ").append(resultSet.getString("user.userName"));
                if (NOT_EMPTY.test(resultSet.getString(6))) {
                    customerAppointmentBuilder.append("\nContact: ").append(resultSet.getString(6));
                }
                customerAppointmentBuilder.append("\nLocation: ");
                if (NOT_EMPTY.test(resultSet.getString(5))) {
                    customerAppointmentBuilder.append(resultSet.getString(5));
                } else {
                    customerAppointmentBuilder.append(customer.ADDRESS.get().getPhoneNumber());
                }
                if (NOT_EMPTY.test(resultSet.getString(4))) {
                    customerAppointmentBuilder.append("\nDescription: ").append(resultSet.getString(4));
                }
                this.customer_history_list_view.getItems().add(customerAppointmentBuilder.toString());
            }
        } catch (SQLException sqlEx) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "There was an issue retrieving the customers history.  Report will be closed.", ButtonType.OK);
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
