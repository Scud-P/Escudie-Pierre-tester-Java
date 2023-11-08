package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParkingSpotTest {

    @Test
    public void testEquals_SameObject() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        assertEquals(parkingSpot, parkingSpot);
    }

    @Test
    public void testEquals_NullObject() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        assertNotNull(parkingSpot);
    }

    @Test
    public void testEquals_DifferentClass() {
        ParkingSpot parkingSpotCar = new ParkingSpot(1, ParkingType.CAR, true);
        assertNotEquals("Not a ParkingSpot", parkingSpotCar);
    }

    @Test
    public void testEquals_SameAttributes_DifferentAvailability() {
        ParkingSpot parkingSpot1 = new ParkingSpot(1, ParkingType.CAR, true);
        ParkingSpot parkingSpot2 = new ParkingSpot(1, ParkingType.CAR, false);
        assertEquals(parkingSpot1, parkingSpot2);
    }

    @Test
    public void testEquals_DifferentAttributes() {
        ParkingSpot parkingSpot1 = new ParkingSpot(1, ParkingType.CAR, true);
        ParkingSpot parkingSpot2 = new ParkingSpot(2, ParkingType.BIKE, true); // Different 'number' and 'parkingType'
        assertNotEquals(parkingSpot1, parkingSpot2);
    }

    @Test
    public void testEquals_SameNumber() {
        ParkingSpot parkingSpot1 = new ParkingSpot(1, ParkingType.CAR, true);
        ParkingSpot parkingSpot2 = new ParkingSpot(1, ParkingType.BIKE, false); // Same 'number'
        assertEquals(parkingSpot1, parkingSpot2);
    }
}