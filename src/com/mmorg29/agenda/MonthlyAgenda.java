package com.mmorg29.agenda;

import com.mmorg29.appointmentscheduler.DateTimeConversion;
import com.mmorg29.dbtools.AppointmentBuilder;
import com.mmorg29.dbtools.DBManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mam
 * This class is a representation of a Monthly Agenda for a particular month and consultant combination.  
 * It stores a collection of WeeklyAgenda objects for the six week span of the desired month. 
 */
public class MonthlyAgenda {

    private final Map<LocalDate, WeeklyAgenda> weeklyAgendas = new HashMap<>();
    private final LocalDate monthStart;
    private final LocalDate monthEnd;

    /**
     * mam
     * Constructor
     * Creates a MonthlyAgenda object for the desired month and year.
     * Creates six WeeklyAgenda objects and stores them in a HashMap for later retrieval.
     * @param month the String representation of the month for this MonthlyAgenda
     * @param year  the year number of the year for this MonthlyAgenda
     */
    public MonthlyAgenda(String month, int year) {
        //mam get the date of the first Sunday of the six week span this MonthlyAgenda object represents
        this.monthStart = getTrueStartOfMonth(month, year);
        //mam in this context a month will be considered 6 weeks (42 days)
        this.monthEnd = this.monthStart.plusDays(41);

        for (int i = 0; i < 6; i++) {
            weeklyAgendas.put(this.monthStart.plusWeeks(i), new WeeklyAgenda(this.monthStart.plusWeeks(i)));
        }
    }

    /**
     * mam
     * Gets a WeeklyAgenda object from a date within the week.
     * @param date A date within the week
     * @return A WeeklyAgenda object for the week containing the supplied date
     */
    public WeeklyAgenda getWeeklyAgenda(LocalDate date) {
        while(date.getDayOfWeek() != DayOfWeek.SUNDAY) {
            date = date.minusDays(1);
        }
        return weeklyAgendas.get(date);
    }
    
    /**
     * mam
     * Gets a WeeklyAgenda object from the week number in the month
     * @param weekNumber the week number in the month 1-6
     * @return A WeeklyAgenda object for the desired week of the month
     */
    public WeeklyAgenda getWeeklyAgenda(int weekNumber) {
        return getWeeklyAgenda(monthStart.plusWeeks(weekNumber));
    }

    /**
     * mam
     * Gets a DailyAgenda by date
     * @param date the date of the agenda to get
     * @return A DailyAgenda object for the desired date
     */
    public DailyAgenda getDailyAgendaByDate(LocalDate date) {
        LocalDate weekStartDate = date;
        while(weekStartDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
            weekStartDate = weekStartDate.minusDays(1);
        }
        
        WeeklyAgenda weeklyAgenda = getWeeklyAgenda(weekStartDate);
        try {
            return weeklyAgenda.getDailyAgendaByDate(date);
        }catch(InvalidAgendaException iaEx) {
            return null;
        }
    }

    /**
     * mam
     * Getter for the month start date of this MonthlyAgenda
     * @return A LocalDate object reference of the monthStart
     */
    public LocalDate getMonthStart() {
        return this.monthStart;
    }

    /**
     * mam
     * Getter for the month end date of this MonthlyAgenda
     * @return A LocalDate object reference of the monthEnd
     */
    public LocalDate getMonthEnd() {
        return monthEnd;
    }
    
    /**
     * mam
     * Calculates the first Sunday for the six week span of this MonthlyAgenda
     * @param month the String representation of the month for this MonthlyAgenda
     * @param year  the year number of the year for this MonthlyAgenda
     * @return A LocalDate object of the first Sunday in this MonthlyAgenda
     */
    private LocalDate getTrueStartOfMonth(String month, int year) {
        LocalDate firstOfMonth = LocalDate.of(year, Month.valueOf(month.toUpperCase()), 1);
        LocalDate trueStartOfMonth = firstOfMonth;
        while (trueStartOfMonth.getDayOfWeek() != DayOfWeek.SUNDAY) {
            trueStartOfMonth = trueStartOfMonth.minusDays(1);
        }
        return trueStartOfMonth;
    }

    /**
     * mam
     * Creates the Appointment objects for the entire span of this MonthlyAgenda.
     * Stores the Appointment objects in the appropriate DailyAgenda object based on date.
     * @param userId the id of the user for which the appointments belong
     * @throws InvalidAgendaException
     */
    public void loadMonthlySchedule(String userId) throws InvalidAgendaException {
        Timestamp utcMonthStart = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.of(this.monthStart, LocalTime.MIN));
        Timestamp utcMonthEnd = DateTimeConversion.UTC_TIMESTAMP.apply(LocalDateTime.of(this.monthEnd, LocalTime.MAX));

        String sql = "SELECT * FROM " + DBManager.APPOINTMENT_TABLE + " WHERE start BETWEEN ? AND ? AND createdBy = ? ORDER BY start ASC";
        try (DBManager dbManager = new DBManager(); PreparedStatement preparedStatement = dbManager.getPreparedStatement(sql);) {
            preparedStatement.setTimestamp(1, utcMonthStart);
            preparedStatement.setTimestamp(2, utcMonthEnd);
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
