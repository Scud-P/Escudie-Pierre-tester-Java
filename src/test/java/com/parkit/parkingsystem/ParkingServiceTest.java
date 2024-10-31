package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    // Mock DAOs and inputReaderUtil to test behavior of parkingService methods

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    public void setUpPerTest() {
        try {
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }


    @Test
    public void processIncomingCarTest()  {

        // 1 = garer la voiture
        when(inputReaderUtil.readSelection()).thenReturn(1);

        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // on spécifie le spot attribué
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        // on appelle la fonction
        parkingService.processIncomingVehicle();

        // on vérifie que getNextAvailableSlot() a été appelée pour une voiture
        verify(parkingSpotDAO).getNextAvailableSlot(ParkingType.CAR);

        // on vérifie que updateParking() a bien été appelé et a updaté la disponibilité de la place
        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));

        // on vérifie que le ticket a bien été enregistré dans la DB
        verify(ticketDAO).saveTicket(any(Ticket.class));
    }

    @Test
    public void processIncomingBikeTest()  {
        when(inputReaderUtil.readSelection()).thenReturn(2);

        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        } catch (Exception e) {
            e.printStackTrace();
        }

        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(1);

        parkingService.processIncomingVehicle();

        verify(parkingSpotDAO).getNextAvailableSlot(ParkingType.BIKE);
        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
        verify(ticketDAO).saveTicket(any(Ticket.class));
    }

    @Test
    public void processExitingVehicleTest()  {

        // On crée la place de parking

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        // On crée et paramètre le ticket

        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Paramétrer les mocks

        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);

        // Appel de la méthode et on vérifie que la place de parking a bien été mise à jour et que le ticket est bien resté en DB
        parkingService.processExitingVehicle();

        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        assertEquals(1, ticketDAO.getNbTicket("ABCDEF"));

    }

    @Test
    public void processIncomingVehicle_shouldDisplayRecurringUserMessage_whenMoreThan1TicketForThePlateNumber_IsInDB() {

        when(inputReaderUtil.readSelection()).thenReturn(1);

        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        } catch (Exception e) {
            e.printStackTrace();
        }

        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        // Cette fois on spécifie que 2 tickets sont dans la DB pour cette plaque
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // On appelle la méthode d'entrée du véhicule et on vérifie que le message d'utilisateur récurrent s'affiche
        parkingService.processIncomingVehicle();

        System.setOut(System.out);
        String printedOutput = outputStream.toString().trim();

        assertTrue(printedOutput.contains("Happy to see you again! As a recurring user of our parking, you will get a 5% rebate."));
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() {

        // Test lorsque updateTicket() renvoie false on vérifie que la disponibilité de la place n'a pas été mise à jour

        // On crée un spot et un ticket

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000))); // One hour
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // On spécifie que updateTicket() renvoit false pour simuler qu'il n' pas réussi à update le ticket

        when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        // On appelle la fonction de sortie et vérifie que la disponibilité de la place n'a pas été mise à jour
        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));

    }

    @Test
    public void getNextParkingNumberIfAvailable_shouldReturnAnAvailableSpot_whenAnAdequateSpotIsAvailable() {
        // On teste que getNextParkingNumberIfAvailable() renvoie bien une place libre lorsqu'il en existe un
        // On spécifie qu'on veut garer une voiture et que le parkingSpotDAO attribue le spot numéro 1

        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);


        // On appelle la méthode getNextParkingNumberIfAvailable() et on vérifie que le spot n'est pas null
        //  qu'il possède l'ID spécifié et qu'il est bien disponible
        ParkingSpot spot = parkingService.getNextParkingNumberIfAvailable();
        assertNotNull(spot);
        assertEquals(1, spot.getId());
        assertTrue(spot.isAvailable());

    }

    @Test
    public void getNextNextParkingNumberIfAvailable_ShouldReturnANullSpot_whenNoAdequateSpotIsFound() {

        // On spécifie qu'on veut garer une voiture mais que la méthode getNextAvailableSlot() retourne
        // une place qui n'existe pas
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);

        // On appelle la méthode getNextParkingNumberIfAvailable() et vérifie que le spot est null
        ParkingSpot spot = parkingService.getNextParkingNumberIfAvailable();
        assertNull(spot);
    }

    @Test
    public void getVehicleType_shouldThrow_IllegalArgumentException_whenIncorrectInputIsProvided() {

        // On vérifie que getVehicleType() envoie une IllegalArgumentException quand readSelection() retourne autre
        // chose qu'une voiture (1) ou une moto (2)

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            when(inputReaderUtil.readSelection()).thenReturn(3);
            parkingService.getVehicleType();
        });

        Assertions.assertEquals("Entered input is invalid", thrown.getMessage());
    }
}

