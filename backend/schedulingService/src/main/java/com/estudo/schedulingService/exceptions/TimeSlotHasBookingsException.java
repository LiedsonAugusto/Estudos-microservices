package com.estudo.schedulingService.exceptions;

public class TimeSlotHasBookingsException extends RuntimeException {
    public TimeSlotHasBookingsException(String message) {
        super(message);
    }
}
