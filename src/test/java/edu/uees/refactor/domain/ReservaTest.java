package edu.uees.refactor.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReservaTest {

    private static final LocalDateTime INICIO =
            LocalDateTime.of(2026, 9, 22, 10, 0);

    @Test
    void reservaNuevaIniciaPendiente() {
        // Arrange
        Reserva reserva = new Reserva("R-001", "ana@uees.edu.ec",
                INICIO, INICIO.plusHours(1), "NORMAL");

        // Act
        EstadoReserva estado = reserva.getEstado();

        // Assert
        assertNotNull(estado);
        assertEquals(EstadoReserva.PENDIENTE, estado);
    }

    @Test
    void confirmarCambiaElEstadoAConfirmada() {
        // Arrange
        Reserva reserva = new Reserva("R-001", "ana@uees.edu.ec",
                INICIO, INICIO.plusHours(1), "NORMAL");

        // Act
        reserva.confirmar();

        // Assert
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }
}
