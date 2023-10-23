package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    private static final int MILLIS_PER_HOUR = 3600000;

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        int inTime = (int) ticket.getInTime().getTime();
        int outTime = (int) ticket.getOutTime().getTime();
        int durationMillis = outTime - inTime;

        float duration = (float)durationMillis / MILLIS_PER_HOUR;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                if (duration < 0.5) {
                    ticket.setPrice(0);
                } else {
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                }
                break;

            }
            case BIKE: {
                if (duration < 0.5) {
                    ticket.setPrice(0);
                } else {
                    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                }
                break;
            }
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }
    }
}