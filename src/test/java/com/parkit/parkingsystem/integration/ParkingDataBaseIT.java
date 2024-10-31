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
    public static void setUp() {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    public void setUpPerTest() throws Exception {

        // On se concentre sur le cas d'une voiture pour une plaque d'immatriculation donnée (ABCDEF) et on vide la BDD
        // entre chaque test pour s'assurer qu'ils ne dépendent pas l'un de l'autre

        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    public static void tearDown() {
    }

    @Test
    public void testParkingACar() {

        // On crée un ParkingService  et on appelle la méthode processIncomingVehicle()
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        // On stocke le ticket et la place attribuée dans des objets de type correspondants, on stocke aussi la prochaine place libre
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        ParkingSpot spot = ticket.getParkingSpot();
        ParkingSpot nextSpot = parkingService.getNextParkingNumberIfAvailable();

        // On vérifie que notre ticket n'est pas nul, que notre place attribuée n'est pas nulle, que la place attribuée n'est plus disponible mais que la prochaine place l'est
        assertNotNull(ticket);
        assertNotNull(spot);
        assertFalse(spot.isAvailable());
        assertTrue(nextSpot.isAvailable());
    }

    @Test
    public void testExitingACar() {

        // On gare une voiture
        testParkingACar();

        // On crée un parkingService et on appelle la méthode de sortie du véhicule
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        // On récupère le ticket et le spot dans des objets du type correspondant
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        ParkingSpot spot = ticket.getParkingSpot();

        // On vérifie que le prix du ticket n'est pas null et qu'il existe bien une heure de sortie
        assertNotNull(ticket.getPrice());
        assertNotNull(ticket.getOutTime());
    }

    @Test
    public void testParkingLotExitForRecurringUser() throws InterruptedException {

        // On gare une voiture

        testParkingACar();
        Thread.sleep(1000);

        // On crée et paramètre un parkingService
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.setFareCalculatorService(fareCalculatorService);

        // On sort notre voiture

        parkingService.processExitingVehicle();
        Thread.sleep(1000);

        // On vérifie que notre ticket possède bien une heure de sortie et qu'il reste correctement dans la base de donnée
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket.getOutTime());
        assertEquals(1, ticketDAO.getNbTicket("ABCDEF"));

        // on vérifie que la méthode de calcul du prix du ticket a bien été appelée avec le paramètre de discount à false
        verify(fareCalculatorService).calculateFare(any(Ticket.class), eq(false));

        Thread.sleep(1000);

        // On gare la même voiture une seconde fois
        testParkingACar();

        Thread.sleep(1000);

        // On sort notre voiture une nouvelle fois
        parkingService.processExitingVehicle();

        // On vérifie que notre second ticket existe, qu'il a une heure de sortie et qu'il y a bien maintenant
        // deux tickets dans la DB pour cette plaque
        Ticket ticket2 = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket2.getOutTime());
        assertEquals(2, ticketDAO.getNbTicket("ABCDEF"));

        // on vérifie que la méthode de calcul du prix du ticket a bien été appelée avec le paramètre de discount à true
        verify(fareCalculatorService).calculateFare(any(Ticket.class), eq(true));
    }
}
