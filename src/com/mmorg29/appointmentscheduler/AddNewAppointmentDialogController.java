/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mmorg29.appointmentscheduler;

import com.mmorg29.agenda.DailyAgenda;
import com.mmorg29.agenda.MonthlyAgenda;
import com.mmorg29.dbtools.Address;
import com.mmorg29.dbtools.Appointment;
import com.mmorg29.dbtools.AppointmentBuilder;
import com.mmorg29.dbtools.Customer;
import com.mmorg29.dbtools.DBManager;
import com.mmorg29.dbtools.InvalidDBOperationException;
import com.mmorg29.dbtools.Reminder;
import com.mmorg29.dbtools.ReminderBuilder;
import java.net.URL;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * FXML Controller class
 *
 * @author mam
 * This class handles the GUI operations associated with creating a new appointment.
 */
public class AddNewAppointmentDialogController implements Initializable {

    @FXML
    private VBox add_new_appointment_vbox;
    @FXML
    private ComboBox<String> customer_selector_combo_box;
    @FXML
    private DatePicker start_date_picker;
    @FXML
    private ComboBox<String> start_time_selector_combo_box;
    @FXML
    private ComboBox<String> end_time_selector_combo_box;
    @FXML
    private ComboBox<String> type_selector_combo_box;
    @FXML
    private TextField location_text_field;
    @FXML
    private TextField contact_text_field;
    @FXML
    private TextField description_text_field;
    @FXML
    private Button add_button;
    @FXML
    private Button cancel_button;

    private MonthlyAgenda monthlyAgenda;

    private final Map<String, Integer> customerMap = new HashMap<>();

    private final ObservableList<String> appointmentTypes
            = FXCollections.observableArrayList(
                    "Initial Consultation",
                    "Regular Consultation",
                    "Emergency Consultation",
                    "Exit Consultation"
            );

    //mam tests if the required piece of appointment info is entered
    private final Predicate<Object> NOT_VALID = (input) -> {
        if (input == null) {
            return true;
        }
        if (input instanceof String) {
            return ((String) input).isEmpty();
        }
        return false;
    };

    //mam validates that all required appointment info is complete
    private final BooleanSupplier FORM_IS_VALID = () -> {
        boolean isValid = true;
        if (NOT_VALID.test(customer_selector_combo_box.getValue())) {
            isValid = false;
        }

        if (NOT_VALID.test(start_date_picker.getValue())) {
            isValid = false;
        }

        if (NOT_VALID.test(start_time_selector_combo_box.getValue())) {
            isValid = false;
        }

        if (NOT_VALID.test(end_time_selector_combo_box.getValue())) {
            isValid = false;
        }

        if (NOT_VALID.test(type_selector_combo_box.getValue())) {
            isValid = false;
        }
        return isValid;
    };

    //mam converts a string of "h:mm a" format to a LocalTime object
    private final Function<String, LocalTime> TO_LOCAL_TIME = (timeStr) -> LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("h:mm a"));

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCustomerMap();
        ObservableList<String> sortedCustomers = FXCollections.observableArrayList(this.customerMap.keySet()).sorted();
        this.customer_selector_combo_box.setItems(sortedCustomers);
        this.customer_selector_combo_box.getEditor().setEditable(false);
        this.start_time_selector_combo_box.getEditor().setEditable(false);
        this.end_time_selector_combo_box.getEditor().setEditable(false);
        this.type_selector_combo_box.setItems(appointmentTypes);
    }

    /**
     * mam
     * Setter for the reference to the MonthlyAgenda object that may need to be updated if a new appointment is added to the monthly schedule currently being viewed.
     * @param monthlyAgenda the MonthlyAgenda object of the month currently being viewed
     */
    public void setMonthlyAgenda(MonthlyAgenda monthlyAgenda) {
        this.monthlyAgenda = monthlyAgenda;
    }
    
    /**
     * mam
     * Loads the the HashMap of customers for which a new appointment can be created.
     * Customers must be active.
     */
    private void loadCustomerMap() {
        String sql = "SELECT customerId, customerName FROM " + DBManager.CUSTOMER_TABLE + " WHERE active = 1";
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet resultSet = statement.executeQuery(sql);) {
            while (resultSet.next()) {
                this.customerMap.put(resultSet.getString(2), resultSet.getInt(1));
            }
        } catch (SQLException sqlEx) {
            Alert alert = new Alert(AlertType.ERROR, "Something went wrong and the application is unable to add appointments.", ButtonType.OK);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> {
                        alert.close();
                        Stage stage = (Stage) this.add_new_appointment_vbox.getScene().getWindow();
                        stage.close();
                    });
        }
    }

    /**
     * mam
     * Creates the new Appointment and Reminder for the new appointment.
     * Commits the new Appointment and Reminder to the database.
     * @param start the start time and date of the new appointment
     * @param end the end time and date of the new appointment
     * @return an Appointment object for the newly created appointment.
     * @throws InvalidAppointmentInfoException when something went wrong trying to create or commit the new appointment
     */
    private Appointment addAppointment(Timestamp start, Timestamp end) throws InvalidAppointmentInfoException {
        Appointment appointment = null;
        try {
            AppointmentBuilder appointmentBuilder = new AppointmentBuilder();
            int nextId = Appointment.NEXT_ID.apply(DBManager.APPOINTMENT_TABLE);
            if(nextId == -1) {
                throw new InvalidAppointmentInfoException("Unable to genterate a unique Id for this appointment.");
            }
            appointmentBuilder.setAppointmentId(nextId);
            appointmentBuilder.setCustomerId(customerMap.get(customer_selector_combo_box.getValue()));
            appointmentBuilder.setStart(start);
            appointmentBuilder.setEnd(end);
            appointmentBuilder.setTitle(MainFormController.capitalize(type_selector_combo_box.getValue()));
            appointmentBuilder.setContact(contact_text_field.getText());
            appointmentBuilder.setLocation(location_text_field.getText());
            appointmentBuilder.setDescription(description_text_field.getText());
            appointment = appointmentBuilder.build();
            if (NOT_VALID.test(appointment.getLocation())) {
                Customer customer = appointment.CUSTOMER.get();
                Address address = customer.ADDRESS.get();
                appointment.setLocation(address.getPhoneNumber());
            }
            ReminderBuilder reminderBuilder = new ReminderBuilder();
            nextId = Reminder.NEXT_ID.apply(DBManager.REMINDER_TABLE);
            if(nextId == -1) {
                throw new InvalidAppointmentInfoException("Unable to genterate a unique Id for this reminder.");
            }
            reminderBuilder.setId(nextId);
            reminderBuilder.setAppointmentId(appointment.getId());
            reminderBuilder.setReminderDate(appointment.getStart());
            reminderBuilder.setSnoozeIncrement(0);
            Reminder reminder = reminderBuilder.build();

            if (!appointment.writeToDB() || !reminder.writeToDB()) {
                appointment.deleteFromDB();
            }
        } catch (InvalidDBOperationException idboEx) {
            throw new InvalidAppointmentInfoException(idboEx.getMessage());
        }
        return appointment;
    }

    /**
     * mam
     * Creates an Observable list of 15 minutes appointment time increments.
     * @param start the time of day that work hours begin (8:00 AM)
     * @return an ObsevableList of time slots
     */
    private ObservableList<String> loadTimeSlots(LocalTime start) {
        ObservableList<String> timeSlots = FXCollections.observableArrayList();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        for (int i = 0; i <= (12 * 4) - 1; i++) {
            timeSlots.add(start.plusMinutes(15 * i).format(timeFormatter));
        }
        return timeSlots;
    }
    
    /**
     * mam
     * Handles the GUI action of selecting a date for the appointment.
     * Loads the start time slots with available time slots for the user to select from.
     * All time slots that are currently filled are removed.
     * Warns user if no open time slots are available.
     */
    @FXML
    private void handleStartDatePickerOnAction(ActionEvent event) {
        try {
            LocalDate selectedDate = this.start_date_picker.getValue();
            if (selectedDate != null && selectedDate.isBefore(LocalDate.now())) {
                //this.start_date_picker.getEditor().clear();
                throw new InvalidAppointmentInfoException("Can not schedule appointments in the past.");
            }
            ObservableList<String> timeSlots = loadTimeSlots(LocalTime.MIDNIGHT.plusHours(8));
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
            Date utcDate = DateTimeConversion.UTC_SQL_DATE_FROM_LOCAL_DATE.apply(selectedDate);
            String sql = "SELECT start, end FROM " + DBManager.APPOINTMENT_TABLE + " WHERE DATE(start) = '" + utcDate.toString()
                    + "' AND createdBy = '" + User.getInstance().getUserId() + "'";
            try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet resultSet = statement.executeQuery(sql);) {
                while (resultSet.next()) {
                    LocalTime appointmentStart = DateTimeConversion.ZONED_LOCAL_DATE_TIME.apply(resultSet.getTimestamp(1)).toLocalTime();
                    LocalTime appointmentEnd = DateTimeConversion.ZONED_LOCAL_DATE_TIME.apply(resultSet.getTimestamp(2)).toLocalTime();
                    int count = 0;
                    while (appointmentStart.plusMinutes(15 * count).isBefore(appointmentEnd)) {
                        timeSlots.remove(appointmentStart.plusMinutes(15 * count).format(timeFormatter));
                        count++;
                    }
                }
                if (timeSlots.isEmpty()) {
                    throw new InvalidAppointmentInfoException("No open time slots for selected date.");
                } else {
                    this.start_time_selector_combo_box.setItems(timeSlots);
                }
            } catch (SQLException sqlEx) {
                throw new InvalidAppointmentInfoException("Something went wrong and the application is unable to add the appointment.");
            }
        } catch (InvalidAppointmentInfoException iaiEx) {
            Alert alert = new Alert(AlertType.ERROR, iaiEx.getMessage(), ButtonType.OK);
            alert.setTitle("Appointment Info Error");
            alert.initStyle(StageStyle.UNDECORATED);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> {
                        alert.close();
                    });
        }
    }

    /**
     * mam
     * Handles the GUI event of selecting a start time for the new appointment.
     * Loads the appointment end time selector with times that will be continuous and not overlap times of existing appointments.
     * Warns User if no open time slots are available.
     */
    @FXML
    private void handleStartTimeSelectorComboBoxOnAction(ActionEvent event) {
        this.end_time_selector_combo_box.getItems().clear();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        LocalTime startTime = this.TO_LOCAL_TIME.apply(this.start_time_selector_combo_box.getValue());
        ObservableList<String> startTimeSlots = this.start_time_selector_combo_box.getItems();
        int count = 1;
        while (startTimeSlots.contains(startTime.plusMinutes(15 * count).format(timeFormatter))) {
            this.end_time_selector_combo_box.getItems().add(startTime.plusMinutes(15 * count).format(timeFormatter));
            count++;
        }
        this.end_time_selector_combo_box.getItems().add(startTime.plusMinutes(15 * count).format(timeFormatter));
        try {
            if (this.end_time_selector_combo_box.getItems().isEmpty()) {
                throw new InvalidAppointmentInfoException("No open time slots for selected start time and date.");
            }
        } catch (InvalidAppointmentInfoException iaiEx) {
            Alert alert = new Alert(AlertType.ERROR, iaiEx.getMessage(), ButtonType.OK);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> alert.close());
        }
    }

    /**
     * mam
     * Handles the GUI event for the add new appointments button.
     * Validates that the required appointment info is complete.
     * Adds the new appointment to the DailyAgenda object if the appointment date is being displayed
     */
    @FXML
    private void handleAddBtnOnAction(ActionEvent event) {
        try {
            if (FORM_IS_VALID.getAsBoolean()) {
                LocalDate appointmentDate = start_date_picker.getValue();
                LocalTime startTime = TO_LOCAL_TIME.apply(start_time_selector_combo_box.getValue());
                Timestamp startTimestamp = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.of(appointmentDate, startTime));
                LocalTime endTime = TO_LOCAL_TIME.apply(end_time_selector_combo_box.getValue());
                Timestamp endTimestamp = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.of(appointmentDate, endTime));
                Appointment newAppointment = addAppointment(startTimestamp, endTimestamp);
                if (newAppointment != null) {
                    LocalDate appointmentStartDate = DateTimeConversion.ZONED_LOCAL_DATE_TIME.apply(startTimestamp).toLocalDate();
                    DailyAgenda dailyAgenda = this.monthlyAgenda.getDailyAgendaByDate(appointmentStartDate);
                    if (dailyAgenda != null) {
                        dailyAgenda.addAppointment(newAppointment);
                    }
                }
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();
            } else {
                throw new InvalidAppointmentInfoException("Missing required appointment info. Please fill in required data and try again.");
            }
        } catch (InvalidAppointmentInfoException iaiEx) {
            Alert alert = new Alert(AlertType.ERROR, iaiEx.getMessage(), ButtonType.OK);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> alert.close());
        }
    }

    /**
     * mam
     * Handles the GUI event for the cancel button.
     * Closes the add new appointments dialog without adding a new appointment.
     * @param event 
     */
    @FXML
    private void handleCancelBtnOnAction(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

}
