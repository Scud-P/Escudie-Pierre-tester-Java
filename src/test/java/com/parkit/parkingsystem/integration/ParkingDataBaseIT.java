package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @Mock
    private FareCalculatorService fareCalculatorService;

    @BeforeAll
    public static void setUp() throws Exception {
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
    public static void tearDown() {

    }

    @Test
    public void testExitingACar() {
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        assertNotNull(ticket.getPrice());
        assertNotNull(ticket.getOutTime());
    }

    @Test
    public void testParkingACar() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        ParkingSpot spot = ticket.getParkingSpot();

        ParkingSpot nextSpot = parkingService.getNextParkingNumberIfAvailable();

        assertNotNull(ticket);
        assertNotNull(spot);
        assertFalse(spot.isAvailable());
        assertTrue(nextSpot.isAvailable());
    }

    @Test
    public void testParkingLotExitForRecurringUser() throws InterruptedException {

        testParkingACar();
        Thread.sleep(1000);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.setFareCalculatorService(fareCalculatorService);

        parkingService.processExitingVehicle();
        Thread.sleep(1000);

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket.getOutTime());
        assertEquals(1, ticketDAO.getNbTicket("ABCDEF"));
        verify(fareCalculatorService).calculateFare(any(Ticket.class), eq(false));

        Thread.sleep(1000);

        testParkingACar();
        Thread.sleep(1000);

        parkingService.processExitingVehicle();
        Thread.sleep(1000);

        Ticket ticket2 = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket2.getOutTime());
        assertEquals(2, ticketDAO.getNbTicket("ABCDEF"));
        verify(fareCalculatorService).calculateFare(any(Ticket.class), eq(true));
    }
}
