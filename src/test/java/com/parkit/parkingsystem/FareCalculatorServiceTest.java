package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    private static final int MILLIS_PER_HOUR = 3600000;
    double testDurationInHours;
    private static final double FREE_DURATION_IN_HOURS = 0.5;


    @BeforeAll
    public static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    public void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar(){
        double testDurationInHours = 1; //Set test duration here (in hours)
        double payingDuration = testDurationInHours - FREE_DURATION_IN_HOURS;

        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - MILLIS_PER_HOUR);
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR*payingDuration);
        System.out.println("calculateFareCar " + ticket.getPrice());
    }

    @Test
    public void calculateFareBike(){
        double testDurationInHours = 1; //Set test duration here (in hours)
        double payingDuration = testDurationInHours - FREE_DURATION_IN_HOURS;
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - MILLIS_PER_HOUR);
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR*payingDuration);
        System.out.println("calculateFareBike " + ticket.getPrice());

    }

    @Test
    public void calculateFareUnknownType(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (MILLIS_PER_HOUR));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithFutureInTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() + MILLIS_PER_HOUR);
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        double testDurationInHours = 0.75; //Set test duration here (in hours)
        double payingDuration = testDurationInHours - FREE_DURATION_IN_HOURS;
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals((payingDuration * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
        System.out.println("calculateFareBikeWithLessThanOneHourParkingTime " + ticket.getPrice());

    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime(){

        double testDurationInHours = 0.75; //Set test duration here (in hours)
        double testDuration = MILLIS_PER_HOUR * testDurationInHours;
        double payingDuration = testDurationInHours - FREE_DURATION_IN_HOURS;

        Date inTime = new Date();
        inTime.setTime((long) (System.currentTimeMillis() - testDuration));//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);

        double expectedPrice = payingDuration * Fare.CAR_RATE_PER_HOUR;
        expectedPrice = Math.round(expectedPrice*100);
        expectedPrice = expectedPrice/100;

        assertEquals(expectedPrice, ticket.getPrice());

        System.out.println("calculateFareCarWithLessThanOneHourParkingTime " + ticket.getPrice());

    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime(){
        double testDurationInHours = 36; //Set test duration here (in hours)
        double payingDuration = testDurationInHours - FREE_DURATION_IN_HOURS;

        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (MILLIS_PER_HOUR * 36) ); //Testing for 36 hours
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( (payingDuration * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
        System.out.println("calculateFareCarWithMoreThanADayParkingTime " + ticket.getPrice());

    }

    @Test
    public void calculateFareCarWithLessThan30MinutesParkingTime() {
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis());//less than 30 minutes parking should be free
        Date outTime = new Date();
        outTime.setTime(System.currentTimeMillis() + (MILLIS_PER_HOUR/3));// testing for 20 minutes
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals((0), ticket.getPrice());
        System.out.println("calculateFareCarWithLessThan30MinutesParkingTime " + ticket.getPrice());


    }

    @Test
    public void calculateFareBikeWithLessThan30MinutesParkingTime() {
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis());//less than 30 minutes parking should be free
        Date outTime = new Date();
        outTime.setTime(System.currentTimeMillis() + (MILLIS_PER_HOUR/3));// testing for 20 minutes
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( (0) , ticket.getPrice());
        System.out.println("calculateFareBikeWithLessThan30MinutesParkingTime " + ticket.getPrice());

    }
    @Test
    public void calculateFareCarWithDiscount() {

        double testDurationInHours = 1; //Set test duration here (in hours)
        double testDuration = MILLIS_PER_HOUR * testDurationInHours;
        double payingDuration = testDurationInHours - FREE_DURATION_IN_HOURS;

        Date inTime = new Date();
        inTime.setTime((long) (System.currentTimeMillis() - testDuration));
        Date outTime = new Date();
        outTime.setTime(System.currentTimeMillis());
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, true);

        double discount = 0.95;
        double expectedPrice = payingDuration * Fare.CAR_RATE_PER_HOUR * discount;
        expectedPrice = Math.round(expectedPrice*100);
        expectedPrice = expectedPrice/100;

        assertEquals(expectedPrice, ticket.getPrice());

        System.out.println("calculateFareCarWithDiscount " + ticket.getPrice());

    }

    @Test
    public void calculateFareBikeWithDiscount() {

        double testDurationInHours = 1; //Set test duration here (in hours)
        double testDuration = MILLIS_PER_HOUR * testDurationInHours;
        double payingDuration = testDurationInHours - FREE_DURATION_IN_HOURS;

        Date inTime = new Date();
        inTime.setTime((long) (System.currentTimeMillis() - testDuration));
        Date outTime = new Date();
        outTime.setTime(System.currentTimeMillis());
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, true);

        double discount = 0.95;
        double expectedPrice = payingDuration * Fare.BIKE_RATE_PER_HOUR * discount;
        expectedPrice = Math.round(expectedPrice*100);
        expectedPrice = expectedPrice/100;

        assertEquals(expectedPrice, ticket.getPrice());

        System.out.println("calculateFareBikeWithDiscount " + ticket.getPrice());

    }

}
