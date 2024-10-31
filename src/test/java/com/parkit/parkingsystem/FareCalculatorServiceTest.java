package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    private static final int MILLIS_PER_HOUR = 3600000;
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
    @DisplayName("Generic test, 1 hour parking price for a car")
    public void calculateFareCar_forASpecificDuration_shouldReturnTheCorrectFare() {
        double testDurationInHours = 1; //Set test duration here (in hours)
        double payingDuration = testDurationInHours - FREE_DURATION_IN_HOURS;

        // Création et paramétrage du ticket

        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - MILLIS_PER_HOUR);
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Appel de la méthode - vérifier que le prix du ticket est égal à la durée multipliée par le coût horaire

        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR * payingDuration);
    }

    @Test
    @DisplayName("Generic test, 1 hour parking price for a bike")

    public void calculateFareBike_forASpecificDuration_shouldReturnTheCorrectFare() {
        double testDurationInHours = 1; //Set test duration here (in hours)
        double payingDuration = testDurationInHours - FREE_DURATION_IN_HOURS;

        // Création et paramétrage du ticket

        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - MILLIS_PER_HOUR);
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Appel de la méthode - vérifier que le prix du ticket est égal à la durée payante multipliée par le coût horaire

        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR * payingDuration);
    }



    @Test
    @DisplayName("Edge case, less than 1 hour parking - Bike")
    public void calculateFareBike_withLessThanOneHourParkingTime_shouldCalculateTheFareAccordingly() {

        double testDurationInHours = 0.75; //Set test duration here (in hours)
        double testDuration = MILLIS_PER_HOUR * testDurationInHours;
        double payingDuration = testDurationInHours - FREE_DURATION_IN_HOURS;

        // Création et paramétrage du ticket

        Date inTime = new Date();
        inTime.setTime((long) (System.currentTimeMillis() - testDuration));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Appel de la méthode - vérifier que le prix est égal à la durée payante fois le taux horaire

        fareCalculatorService.calculateFare(ticket);
        double expectedPrice = payingDuration * Fare.BIKE_RATE_PER_HOUR;
        expectedPrice = Math.round(expectedPrice * 100);
        expectedPrice = expectedPrice / 100;
        assertEquals(expectedPrice, ticket.getPrice());
    }

    @Test
    @DisplayName("Edge case, less than 1 hour parking - Car")
    public void calculateFareCar_withLessThanOneHourParkingTime_shouldCalculateTheFareAccordingly() {

        double testDurationInHours = 0.75; //Set test duration here (in hours)
        double testDuration = MILLIS_PER_HOUR * testDurationInHours;
        double payingDuration = testDurationInHours - FREE_DURATION_IN_HOURS;

        // Création et paramétrage du ticket
        Date inTime = new Date();
        inTime.setTime((long) (System.currentTimeMillis() - testDuration));//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Appel de la méthode - vérifier que le prix est égal à la durée payante fois le taux horaire

        fareCalculatorService.calculateFare(ticket);
        double expectedPrice = payingDuration * Fare.CAR_RATE_PER_HOUR;
        expectedPrice = Math.round(expectedPrice * 100);
        expectedPrice = expectedPrice / 100;
        assertEquals(expectedPrice, ticket.getPrice());
    }

    @Test
    @DisplayName("Edge case, more than a day parking - Car")
    public void calculateFareCar_withMoreThanADayParkingTime_shouldCalculateTheFareAccordingly() {
        double testDurationInHours = 36; //Set test duration here (in hours)
        double payingDuration = testDurationInHours - FREE_DURATION_IN_HOURS;

        // Création et paramétrage du ticket

        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (MILLIS_PER_HOUR * 36)); //Testing for 36 hours
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Appel de la méthode - vérifier que le prix est égal à la durée payante fois le taux horaire

        fareCalculatorService.calculateFare(ticket);
        assertEquals((payingDuration * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    @DisplayName("30 free minutes feature, 20 minutes should be free - Car")
    public void calculateFareCar_withLessThan30MinutesParkingTime_shouldReturnAFreeFare() {

        // Création et paramétrage du ticket

        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis());
        Date outTime = new Date();
        outTime.setTime(System.currentTimeMillis() + (MILLIS_PER_HOUR / 3));// testing for 20 minutes
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Appel de la méthode - vérifier que le prix est égal à 0

        fareCalculatorService.calculateFare(ticket);
        assertEquals((0), ticket.getPrice());
    }

    @Test
    @DisplayName("30 free minutes feature, 20 minutes should be free - Bike")
    public void calculateFareBike_withLessThan30MinutesParkingTime_shouldReturnAFreeFare() {

        // Création et paramétrage du ticket

        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis());
        Date outTime = new Date();
        outTime.setTime(System.currentTimeMillis() + (MILLIS_PER_HOUR / 3));// testing for 20 minutes
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Appel de la méthode - vérifier que le prix est égal à 0

        fareCalculatorService.calculateFare(ticket);
        assertEquals((0), ticket.getPrice());
    }

    @Test
    @DisplayName("5% discount feature, discount should be granted when calculateFare() is called with second parameter true - Car")
    public void calculateFareCarWithDiscount_shouldReturn95Percent_ofARegularFare() {

        double testDurationInHours = 1; //Set test duration here (in hours)
        double testDuration = MILLIS_PER_HOUR * testDurationInHours;
        double payingDuration = testDurationInHours - FREE_DURATION_IN_HOURS;

        // Création et paramétrage du ticket

        Date inTime = new Date();
        inTime.setTime((long) (System.currentTimeMillis() - testDuration));
        Date outTime = new Date();
        outTime.setTime(System.currentTimeMillis());
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Appel de la méthode avec le second paramètre true - vérifier que le prix est bien discounté

        fareCalculatorService.calculateFare(ticket, true);
        double discount = 0.95;
        double expectedPrice = payingDuration * Fare.CAR_RATE_PER_HOUR * discount;
        expectedPrice = Math.round(expectedPrice * 100);
        expectedPrice = expectedPrice / 100;

        assertEquals(expectedPrice, ticket.getPrice());
    }

    @Test
    @DisplayName("5% discount feature, discount should be granted when calculateFare() is called with second parameter true - Bike")
    public void calculateFareBikeWithDiscount_shouldReturn95Percent_ofARegularFare() {

        double testDurationInHours = 1; //Set test duration here (in hours)
        double testDuration = MILLIS_PER_HOUR * testDurationInHours;
        double payingDuration = testDurationInHours - FREE_DURATION_IN_HOURS;

        // Création et paramétrage du ticket

        Date inTime = new Date();
        inTime.setTime((long) (System.currentTimeMillis() - testDuration));
        Date outTime = new Date();
        outTime.setTime(System.currentTimeMillis());
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Appel de la méthode avec le second paramètre true - vérifier que le prix est bien discounté

        fareCalculatorService.calculateFare(ticket, true);
        double discount = 0.95;
        double expectedPrice = payingDuration * Fare.BIKE_RATE_PER_HOUR * discount;
        expectedPrice = Math.round(expectedPrice * 100);
        expectedPrice = expectedPrice / 100;

        assertEquals(expectedPrice, ticket.getPrice());
    }

    @Test
    @DisplayName("Null pointer exception thrown when parkingType is null")

    public void calculateFare_forAParkingType_thatDoesNotExist_shouldThrowANullPointerException() {

        // Création et paramétrage du ticket

        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (MILLIS_PER_HOUR));
        Date outTime = new Date();

        // parkingType est null
        ParkingSpot parkingSpot = new ParkingSpot(1, null, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Appel de la méthode - vérifier que calculateFare() lève une exception de type NullPointer
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    @DisplayName("IllegalArgument Exception thrown when duration < 0")

    public void calculateFareBike_WithANegativeDuration_ShouldThrowAnIllegalArgumentException() {

        // Création et paramétrage du ticket

        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() + MILLIS_PER_HOUR);
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Appel de la méthode - vérifier que calculateFare() lève une exception de type IllegalArgument
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }
}
