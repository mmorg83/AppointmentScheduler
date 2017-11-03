/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mmorg29.appointmentscheduler;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author mam
 * This class is a series of Lambda functions to manipulate and convert dates and times from UTC format to a Zoned Format or vice versa.
 * ALL DATES AND TIMES ARE STORED IN THE DATABASE IN UTC FORMAT
 */
public class DateTimeConversion {

    //mam Supplies the current user timezone id
    public static final Supplier<ZoneId> ZONE_ID = () -> ZoneId.systemDefault();

    //mam provides conversion from a UTC timestamp to a Zoned LocalDateTime object
    public static final Function<Timestamp, LocalDateTime> ZONED_LOCAL_DATE_TIME = (utcTimestamp) -> {
        LocalDateTime utcLocalDateTime = utcTimestamp.toLocalDateTime();
        ZonedDateTime zonedDateTime = utcLocalDateTime.atZone(ZoneOffset.UTC);
        return zonedDateTime.withZoneSameInstant(ZONE_ID.get()).toLocalDateTime();
    };
    
    //mam provides conversion from a LocalDateTime to a UTC timestamp
    public static final Function<LocalDateTime, Timestamp> UTC_TIMESTAMP = (localDateTime) -> {
        ZonedDateTime utcDateTime = ZonedDateTime.of(localDateTime, ZONE_ID.get()).withZoneSameInstant(ZoneOffset.UTC);
        return Timestamp.valueOf(utcDateTime.toLocalDateTime());
    };

    //mam provides conversion from a zonded LocalDateTime to a UTC LocalDateTime
    public static final Function<LocalDateTime, LocalDateTime> UTC_LOCAL_DATE_TIME = (localDateTime) -> {
        return UTC_TIMESTAMP.apply(localDateTime).toLocalDateTime();
    };
    
    //mam provides conversion from a UTC timestamp to a Zoned LocalDate
    public static final Function<Timestamp, LocalDate> ZONED_LOCAL_DATE = (utcTimestamp) -> {
        return ZONED_LOCAL_DATE_TIME.apply(utcTimestamp).toLocalDate();
    };

    //mam provides conversion from a UTC Timestamp to a UTC SQLDate
    public static final Function<Timestamp, Date> UTC_SQL_DATE = (utcTimestamp) -> {
        return Date.valueOf(utcTimestamp.toLocalDateTime().toLocalDate());
    };

    //mam provides conversion from a zoned LocalDate to a UTC SQLDate
   public static final Function<LocalDate, Date> UTC_SQL_DATE_FROM_LOCAL_DATE = (localDate) -> {
       LocalDateTime localDateTime = localDate.atTime(LocalTime.MIN);
       Timestamp utcTimestamp = UTC_TIMESTAMP.apply(localDateTime);
       return UTC_SQL_DATE.apply(utcTimestamp);
   };
}
