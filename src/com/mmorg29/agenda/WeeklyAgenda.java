/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mmorg29.agenda;

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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 *
 * @author mmorg
 */
public class WeeklyAgenda {

    private final Map<LocalDate, DailyAgenda> dailyAgendas = new HashMap<>();
    private final LocalDate weekStart;
    private final LocalDate weekEnd;

    //mam tests whether a date is actually in the week associated with this WeeklyAgenda
    Predicate<LocalDate> IS_IN_WEEK = (date) -> date.isEqual(getWeekStart()) || date.isEqual(getWeekEnd()) || (date.isAfter(getWeekStart()) && date.isBefore(getWeekEnd()));

    /**
     * mam
     * Constructor
     * Creates a WeeklyAgenda object for the given week start date.
     * Creates a DailyAgenda for each day of the week and adds it to a HashMap for later retrieval
     * @param start the first day of the week
     */
    public WeeklyAgenda(LocalDate start) {
        this.weekStart = start;
        this.weekEnd = start.plusDays(6);

        for (int i = 0; i < 7; i++) {
            dailyAgendas.put(start.plusDays(i), new DailyAgenda(start.plusDays(i)));
        }
    }

    //mam Getter for the start date of this week
    public LocalDate getWeekStart() {
        return this.weekStart;
    }

    //mam Getter for the end date of this week
    public LocalDate getWeekEnd() {
        return this.weekEnd;
    }

    /**
     * mam
     * Gets a DailyAgenda object for the desired date if it is part of this week.
     * @param date the date of the desired DailyAgenda
     * @return a DailyAgenda object for the desired date
     * @throws InvalidAgendaException when the desired date is not in this week.
     */
    public DailyAgenda getDailyAgendaByDate(LocalDate date) throws InvalidAgendaException {
        if (IS_IN_WEEK.test(date)) {
            return dailyAgendas.get(date);
        } else {
            throw new InvalidAgendaException("The requested date is not in this week.");
        }
    }

    /**
     * mam
     * Creates Appointment objects for all appointments in this week.
     * Adds appointments to the appropriate DailyAgenda object based on date.
     * @param userId the user id of the creator of these appointments
     * @throws InvalidAgendaException when unable to successfully retrieve the appointment info for the given week.
     */
    public void loadWeeklySchedule(String userId) throws InvalidAgendaException {
        Timestamp utcWeekStart = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.of(this.weekStart, LocalTime.MIN));
        Timestamp utcWeekEnd = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.of(this.weekEnd, LocalTime.MAX));

        String sql = "SELECT * FROM " + DBManager.APPOINTMENT_TABLE + " WHERE start BETWEEN ? AND ? AND createdBy = ? ORDER BY start ASC";
        try (DBManager dbManager = new DBManager(); PreparedStatement preparedStatement = dbManager.getPreparedStatement(sql);) {
            preparedStatement.setTimestamp(1, utcWeekStart);
            preparedStatement.setTimestamp(2, utcWeekEnd);
            preparedStatement.setString(3, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                LocalDate appointmentDate = DateTimeConversion.ZONED_LOCAL_DATE.apply(resultSet.getTimestamp("start"));
                DailyAgenda dailyAgenda = getDailyAgendaByDate(appointmentDate);
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
                dailyAgenda.addAppointment(appointmentBuilder.build());
            }
            resultSet.close();

        } catch (SQLException sqlEx) {
            throw new InvalidAgendaException("Unable to load monthly schedule");
        }
    }

}
