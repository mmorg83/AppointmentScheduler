/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mmorg29.appointmentscheduler;

/**
 *
 * @author mam
 * Exception for an operation when the Appointment info is invalid
 */
public class InvalidAppointmentInfoException extends Exception {

    public InvalidAppointmentInfoException(String message) {
        super(message);
    }
    
}
