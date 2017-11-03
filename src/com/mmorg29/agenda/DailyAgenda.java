package com.mmorg29.agenda;

import com.mmorg29.dbtools.Appointment;
import com.mmorg29.appointmentscheduler.DateTimeConversion;
import com.mmorg29.dbtools.AppointmentBuilder;
import com.mmorg29.dbtools.DBManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

/**
 *
 * @author mam
 * This class is a representation of a Daily Agenda for a particular date and consultant combination.  
 * It stores all the appointments associated with its particular date and consultant combination.
 */
public class DailyAgenda {

    private final LocalDate date;
    private final Map<Integer, Appointment> appointments = new HashMap<>();
    private final Map<String, Integer> shortDisplayStrings = new HashMap<>();
    private final Map<String, Integer> detailedDisplayStrings = new HashMap<>();
    private ListView monthDisplay;
    private ListView weekDisplay;
    
    /**
     * mam 
     * Constructor
     * @param date the date that this agenda is tied to.
     */
    public DailyAgenda(LocalDate date) {
        this.date = date;
    }

    /**
     * mam 
     * Getter for date field
     * @return the date field that this agenda is tied to.
     */
    public LocalDate getDate() {
        return this.date;
    }

    /**
     * mam 
     * Adds a new appointment to this agenda by placing the Appointment object in the appointments Map. 
     * Gets the strings used to display the appointment information and stores them. 
     * Requests an update to the monthly and weekly ListViews used to display the appointment info.
     * @param appointment the appointment to add
     */
    public void addAppointment(Appointment appointment) {
        this.appointments.put(appointment.getId(), appointment);
        String[] displayStrings = appointment.getDisplayStrings(null);
        shortDisplayStrings.put(displayStrings[0], appointment.getId());
        detailedDisplayStrings.put(displayStrings[1], appointment.getId());
        updateDisplay();
    }

    /**
     * mam
     * Removes an appointment from this agenda by removing it from the appointments Map.
     * Removes the strings used to display the appointment information.
     * Request an update to the monthly and weekly ListViews used to display the appointment information.
     * @param appointment the appointment to remove
     */
    public void removeAppointment(Appointment appointment) {
        this.appointments.remove(appointment.getId());
        String[] displayStrings = appointment.getDisplayStrings(null);
        shortDisplayStrings.remove(displayStrings[0]);
        detailedDisplayStrings.remove(displayStrings[1]);
        updateDisplay();
    }

    /**
     * mam 
     * Gets an Appointment object based on a unique identifier(Primary Key in DB) for the appointment
     * @param id The unique identifier from the database primary key field
     * @return An Appointment object with an id that matches the desired id
     */
    public Appointment getAppointment(int id) {
        return this.appointments.get(id);
    }

    /**
     * mam
     * Gets an Appointment object based on the string used to display the appointment info.
     * @param displayString the String object used to display the appointment info; Can be in one of two forms either detailed or short
     * @return the Appointment object associated with the appointment info display String
     */
    public Appointment getAppointment(String displayString) {
        Integer appointmentId = null;
        //If displayString contains Customer: then it is of the detailed form
        if (displayString.contains("Customer:")) {
            appointmentId = detailedDisplayStrings.get(displayString);
        } else {
            appointmentId = shortDisplayStrings.get(displayString);
        }
        return getAppointment(appointmentId);
    }

    /**
     * mam 
     * Sets a reference to the ListView object used to display all appointments info for a specific date in the monthly schedule view
     * @param display the ListView object that displays the appointments info in the monthly view
     */
    public void setMonthDislplay(ListView display) {
        this.monthDisplay = display;
    }

    /**
     * mam
     * Sets a reference to the ListView object used to display all appointments info for a specific date in the weekly schedule view
     * @param display the ListView object that displays the appointments info in the weekly view
     */
    public void setWeekDisplay(ListView display) {
        this.weekDisplay = display;
    }

    /**
     * mam
     * Updates the items in the ListViews used to display the appointments info in the monthly and weekly schedule views
     * utilizes the runnable interface due to being on a different thread than the GUI
     */
    public void updateDisplay() {
        ObservableList<String> displayableAgenda = getDisplayableAgenda(false);
        if (this.monthDisplay != null) {
            Platform.runLater(() -> {
                this.monthDisplay.setItems(displayableAgenda);
            });
        }
        if (this.weekDisplay != null) {
            Platform.runLater(() -> {
                this.weekDisplay.setItems(displayableAgenda);
            });
        }
    }

    /**
     * mam
     * Gets an ObservableList<String> object used for the ListView object displaying the appointments info
     * @param isDetailed flag to determine which form of display String to return; true returns a more detailed form; false returns a short form
     * @return ObservableList<String> object containing the display Strings
     */
    public ObservableList<String> getDisplayableAgenda(Boolean isDetailed) {
        if (isDetailed) {
            return FXCollections.observableArrayList(this.detailedDisplayStrings.keySet()).sorted(new SortByTime());
        } else {
            return FXCollections.observableArrayList(this.shortDisplayStrings.keySet()).sorted(new SortByTime());
        }
    }

    /**
     * mam
     * Creates Appointment objects for all appointments on the date associated with this DailyAgenda Object.
     * Then adds the Objects to a HashMap for easy retrieval.  
     * This eliminates repeated calls to the DB which are time consuming.
     * @param userId The userId of the appointments creator
     * @throws InvalidAgendaException
     */
    public void loadDailySchedule(String userId) throws InvalidAgendaException {
        Timestamp utcStart = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.of(this.date, LocalTime.MIN));
        Timestamp utcEnd = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.of(this.date, LocalTime.MAX));
        String sql = "SELECT * FROM " + DBManager.APPOINTMENT_TABLE + " WHERE start BETWEEN ? AND ? AND createdBy = ? ORDER BY start ASC";
        try (DBManager dbManager = new DBManager(); PreparedStatement preparedStatement = dbManager.getPreparedStatement(sql);) {
            preparedStatement.setTimestamp(1, utcStart);
            preparedStatement.setTimestamp(2, utcEnd);
            preparedStatement.setString(3, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
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
                addAppointment(appointmentBuilder.build());
            }
            resultSet.close();
        } catch (SQLException sqlEx) {
            throw new InvalidAgendaException("Unable to load daily schedule.");
        }

    }

    /**
    *
    * @author mam
    * This class is an implementation of a Comparator object.  
    * It is used to sort appointment display Strings according to start time.
    */
    class SortByTime implements Comparator<String> {

        @Override
        public int compare(String appointment1, String appointment2) {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
            LocalTime appointmentTime1 = LocalTime.parse(appointment1.substring(0, appointment1.indexOf(" -")), timeFormatter);
            LocalTime appointmentTime2 = LocalTime.parse(appointment2.substring(0, appointment2.indexOf(" -")), timeFormatter);
            return appointmentTime1.compareTo(appointmentTime2);
        }
    }
}
