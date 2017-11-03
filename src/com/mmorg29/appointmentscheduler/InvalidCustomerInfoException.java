/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mmorg29.appointmentscheduler;

/**
 *
 * @author mam
 * Exception for an operation when the customer info is invalid
 */
class InvalidCustomerInfoException extends Exception {

    InvalidCustomerInfoException(String message) {
        super(message);
    }
    
}
