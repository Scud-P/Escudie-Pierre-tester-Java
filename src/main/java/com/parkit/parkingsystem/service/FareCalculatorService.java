package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    private static final int MILLIS_PER_MINUTE = 60000;

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false); // Default to no discount
    }

    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        double inTime = ticket.getInTime().getTime();
        double outTime = ticket.getOutTime().getTime();
        double durationMillis = outTime - inTime;
        double duration = durationMillis / MILLIS_PER_MINUTE;

        int freeDuration = 30;

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                if (duration <= freeDuration) {
                    ticket.setPrice(0);
                } else {
                    double farePerMin = Fare.CAR_RATE_PER_HOUR / 60;
                    double payingDuration = duration - freeDuration;
                    double fare = payingDuration * farePerMin;
                    if (discount) {
                        double discountedFare = fare * 95 / 100;
                        ticket.setPrice(discountedFare);
                    } else {
                        ticket.setPrice(fare);
                    }
                    break;
                }
            }

            case BIKE: {
                if (duration <= freeDuration) {
                    ticket.setPrice(0);
                } else {
                    double farePerMin = Fare.BIKE_RATE_PER_HOUR / 60;
                    double payingDuration = duration - freeDuration;
                    double fare = payingDuration * farePerMin;
                    if (discount) {
                        double discountedFare = fare * 95 / 100;
                        ticket.setPrice(discountedFare);
                    } else {
                        ticket.setPrice(fare);
                    }
                    break;
                }
            }
        }
    }
}