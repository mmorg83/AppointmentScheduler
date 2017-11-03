package com.mmorg29.appointmentscheduler;

import com.mmorg29.dbtools.Appointment;
import com.mmorg29.dbtools.DBManager;
import com.mmorg29.dbtools.InvalidDBOperationException;
import com.mmorg29.dbtools.ReminderBuilder;
import com.mmorg29.dbtools.Reminder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

/**
 *
 * @author mam
 * Thread run by an Executor Service to display appointment reminders at most 15 minutes before they occur.
 * Reminders may not be displayed if the user is in the process of adding a new appointment due to modality issues.
 */
class Notifications implements Runnable {

    //mam Converts a stored UTC timestamp stored in the database to a LocalTime of the users Time Zone
    private final Function<Timestamp, LocalTime> APPOINTMENT_TIME = (utcTimestamp) -> DateTimeConversion.ZONED_LOCAL_DATE_TIME.apply(utcTimestamp).toLocalTime();

    //mam Displays the appointment reminder in an alert dialog.
    //The user can dismiss the reminder and it will be deleted. 
    //the user can snooze the reminder and it will be displayed again within 10 minutes if the time left until appointment is greater than 11 minutes 
    //or 5 minutes if the time left until the appointment is greater than 6 minutes
    //or 1/2 of the time left until tne appointment if it is 6 or less mintutes away
    private final Consumer<Reminder> DISPLAY_REMINDER = (reminder) -> {
        Platform.runLater(() -> {
            //mam check to make sure reminder time is still in future.
            if (APPOINTMENT_TIME.apply(reminder.getReminderDate()).isAfter(LocalTime.now())) {
                Alert appointmentReminderAlert = new Alert(AlertType.NONE, reminder.getDisplayString(), ButtonType.OK, ButtonType.CANCEL);
                Button okBtn = (Button) appointmentReminderAlert.getDialogPane().lookupButton(ButtonType.OK);
                okBtn.setText("Snooze");
                okBtn.setDefaultButton(false);
                okBtn.setOnAction((event) -> {
                    int snooze = 5;
                    int minutesBetween = APPOINTMENT_TIME.apply(reminder.getReminderDate()).getMinute() - LocalTime.now().getMinute();
                    if (minutesBetween > 11) {
                        snooze = 10;
                    } else if (minutesBetween < 6) {
                        snooze = minutesBetween / 2;
                    }
                    reminder.setSnoozeIncrement(snooze);
                    reminder.setReminderDate(DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.now()));
                    try {
                        reminder.updateInDB();
                    } catch (InvalidDBOperationException idboEx) {
                        Alert dbErrorAlert = new Alert(Alert.AlertType.ERROR, idboEx.getMessage(), ButtonType.OK);
                        dbErrorAlert.initStyle(StageStyle.UNDECORATED);
                        dbErrorAlert.initModality(Modality.APPLICATION_MODAL);
                        dbErrorAlert.showAndWait()
                                .filter(response -> response == ButtonType.OK)
                                .ifPresent(response -> appointmentReminderAlert.close());
                    }
                    appointmentReminderAlert.close();
                });
                Button cancelBtn = (Button) appointmentReminderAlert.getDialogPane().lookupButton(ButtonType.CANCEL);
                cancelBtn.setText("Dismiss");
                cancelBtn.setOnAction((event) -> {
                    try {
                        reminder.deleteFromDB();
                    } catch (InvalidDBOperationException idboEx) {
                        Alert dbErrorAlert = new Alert(Alert.AlertType.ERROR, idboEx.getMessage(), ButtonType.OK);
                        dbErrorAlert.setTitle("Database Error");
                        dbErrorAlert.showAndWait()
                                .filter(response -> response == ButtonType.OK)
                                .ifPresent(response -> appointmentReminderAlert.close());
                    }
                    appointmentReminderAlert.close();
                });
                appointmentReminderAlert.setTitle("Upcoming Appointment");
                appointmentReminderAlert.initStyle(StageStyle.UNDECORATED);
                appointmentReminderAlert.show();
            } else {
                try {
                    reminder.deleteFromDB();
                } catch (InvalidDBOperationException idboEx) {
                    Alert dbErrorAlert = new Alert(Alert.AlertType.ERROR, idboEx.getMessage(), ButtonType.OK);
                    dbErrorAlert.initStyle(StageStyle.UNDECORATED);
                    dbErrorAlert.initModality(Modality.APPLICATION_MODAL);
                    dbErrorAlert.showAndWait()
                            .filter(response -> response == ButtonType.OK)
                            .ifPresent(response -> dbErrorAlert.close());
                }
            }

        });
    };

    //mam The Task that will be run by the Executor Service.  Retrieves appointment reminders that will occur in the next 15 minutes.
    @Override
    public void run() {
        String sql = "SELECT * FROM " + DBManager.REMINDER_TABLE
                + " WHERE reminderDate BETWEEN '" + DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.now()).toString() + "'"
                + " AND '" + DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.now().plusMinutes(15)).toString() + "'"
                + " AND createdBy = '" + User.getInstance().getUserId() + "'"
                + " ORDER BY reminderDate DESC";
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                //mam make sure to not display before snooze time is up.
                if (LocalTime.now().isAfter(this.APPOINTMENT_TIME.apply(resultSet.getTimestamp(2)).plusMinutes(resultSet.getInt(3)))) {
                    continue;
                }
                //mam Create a Reminder object for the appointment reminder.
                ReminderBuilder reminderBuilder = new ReminderBuilder();
                reminderBuilder.setId(resultSet.getInt(1));
                reminderBuilder.setReminderDate(resultSet.getTimestamp(2));
                reminderBuilder.setSnoozeIncrement(resultSet.getInt(3));
                reminderBuilder.setAppointmentId(resultSet.getInt(5));
                reminderBuilder.setCreatedBy(resultSet.getString(6));
                reminderBuilder.setCreatedDate(resultSet.getTimestamp(7));
                Reminder reminder = reminderBuilder.build();
                Appointment appointment = reminder.APPOINTMENT.get();
                //mam Gets the display string of the appointment associated with this reminder. Uses the detailed reminder format.
                String[] displayStrings = appointment.getDisplayStrings(null);
                reminder.setDisplayString(displayStrings[1]);
                this.DISPLAY_REMINDER.accept(reminder);
            }
        } catch (SQLException sqlEx) {
            //mam Application was unable to access the reminder info. Notify user that no reminders will be displayed.
            Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to display appointment reminders at this Time.", ButtonType.OK);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> alert.close());
        }
    }

}
