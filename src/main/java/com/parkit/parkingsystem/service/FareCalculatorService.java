package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import static com.parkit.parkingsystem.constants.ParkingType.BIKE;

public class FareCalculatorService {

    private static final int MILLIS_PER_MINUTE = 60000;

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false); // Default to no discount
    }

    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        int inTime = (int) ticket.getInTime().getTime();
        int outTime = (int) ticket.getOutTime().getTime();
        int durationMillis = outTime - inTime;
        int duration = durationMillis / MILLIS_PER_MINUTE;

        int freeDuration = 30;

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                if (duration <= freeDuration) {
                    ticket.setPrice(0);
                } else {
                    if (discount) {
                        ticket.setPrice((duration - freeDuration) * (Fare.CAR_RATE_PER_HOUR * 0.95 / 60));
                    } else {
                        ticket.setPrice((duration - freeDuration) * (Fare.CAR_RATE_PER_HOUR  / 60));
                    }
                    break;
                }
            }

            case BIKE: {
                if (duration <= freeDuration) {
                    ticket.setPrice(0);
                } else {
                    if (discount) {
                        ticket.setPrice((duration - freeDuration) * (Fare.BIKE_RATE_PER_HOUR * 0.95 / 60));

                    } else {
                        ticket.setPrice((duration - freeDuration) * (Fare.BIKE_RATE_PER_HOUR / 60));
                    }
                    break;
                }

            }
        }
    }
}