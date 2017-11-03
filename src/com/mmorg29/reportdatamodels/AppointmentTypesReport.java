package com.mmorg29.reportdatamodels;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author mam
 * Data Model for Appointment Types by Month Report.  
 * Used by the TableView in the report.
 */
public class AppointmentTypesReport {

    private final SimpleStringProperty appointmentType;
    private final SimpleIntegerProperty total;

    public AppointmentTypesReport(String appointmentType, int total) {
        this.appointmentType = new SimpleStringProperty(appointmentType);
        this.total = new SimpleIntegerProperty(total);
    }

    //Getters and Setters
    
    /**
     * @return the appointmentType which is the textual representation of a specific appointment type.
     */
    public String getAppointmentType() {
        return appointmentType.get();
    }

    /**
     * @return the total which is the total count of a particular appointment type.
     */
    public Integer getTotal() {
        return total.get();
    }
}
