package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    public static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        ParkingSpot spot = ticket.getParkingSpot();

        ParkingSpot nextSpot = parkingService.getNextParkingNumberIfAvailable();

        assertNotNull(ticket);
        assertNotNull(spot);
        assertFalse(spot.isAvailable());
        assertTrue(nextSpot.isAvailable());

//        Debug prints
//        System.out.println("Attributed spot availability: " + spot.isAvailable());
//        System.out.println("attributed spot id: " + spot.getId());
//        System.out.println("Next spot availability: " + nextSpot.isAvailable());
//        System.out.println("next spot id: " + nextSpot.getId());


        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
    }

    @Test
    public void testParkingLotExit() throws InterruptedException {

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processIncomingVehicle();

        Thread.sleep(1000);

        parkingService.processExitingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");


        assertNotNull(ticket.getPrice());
        assertNotNull(ticket.getOutTime());
        assertEquals(0, ticketDAO.getNbTicket("ABCDEF"));

        System.out.println("Fare: " + ticket.getPrice());
        System.out.println("In time: " + ticket.getInTime());
        System.out.println("Out time: " +ticket.getOutTime());
        System.out.println("Number of tickets in db" + ticketDAO.getNbTicket("ABCDEF"));

        parkingService.processIncomingVehicle();

        Thread.sleep(1000);

        parkingService.processExitingVehicle();

        Ticket ticket2 = ticketDAO.getTicket("ABCDEF");

        assertNotNull(ticket2.getPrice());
        assertNotNull(ticket2.getOutTime());
        assertEquals(1, ticketDAO.getNbTicket("ABCDEF"));

        System.out.println("Fare: " + ticket2.getPrice());
        System.out.println("In time: " + ticket2.getInTime());
        System.out.println("Out time: " +ticket2.getOutTime());
        System.out.println("Number of tickets in db" + ticketDAO.getNbTicket("ABCDEF"));


        //TODO: check that the fare generated and out time are populated correctly in the database
    }

}
