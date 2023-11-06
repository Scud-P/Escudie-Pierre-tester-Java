package com.parkit.parkingsystem;

import com.parkit.parkingsystem.service.InteractiveShell;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;


import java.io.ByteArrayOutputStream;

import java.io.PrintStream;

@ExtendWith(MockitoExtension.class)

public class InteractiveShellTest {


    @Test
    public void testLoadMenu() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        InteractiveShell.loadMenu();

        System.setOut(System.out);

        String printedMenu = outputStream.toString().trim();

        String expectedMenu = "Please select an option. Simply enter the number to choose an action\r\n" +
                "1 New Vehicle Entering - Allocate Parking Space\r\n" +
                "2 Vehicle Exiting - Generate Ticket Price\r\n" +
                "3 Shutdown System";
        assertEquals(expectedMenu, printedMenu);
    }
}