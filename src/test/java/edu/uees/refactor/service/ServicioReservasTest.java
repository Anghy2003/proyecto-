package edu.uees.refactor.service;

import edu.uees.refactor.domain.EstadoReserva;
import edu.uees.refactor.domain.Reserva;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Red de seguridad del Laboratorio 2: pruebas de caracterizacion.
 *
 * Congelan el comportamiento heredado tal como lo observe en la linea base
 * del Laboratorio 1 (LB-01 a LB-09). Describen como funciona el codigo HOY,
 * no como deberia funcionar. Por eso tambien protegen LB-08 y LB-09, que son
 * decisiones funcionales pendientes y no defectos a corregir aqui.
 */
class ServicioReservasTest {

    private static final LocalDateTime INICIO =
            LocalDateTime.of(2026, 9, 22, 10, 0);
    private static final int CINCO_HORAS = 5;

    private ServicioReservas servicio;
    private ByteArrayOutputStream consola;
    private PrintStream salidaOriginal;

    @BeforeEach
    void preparar() {
        servicio = new ServicioReservas();
        salidaOriginal = System.out;
        consola = new ByteArrayOutputStream();
        System.setOut(new PrintStream(consola, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restaurarConsola() {
        System.setOut(salidaOriginal);
    }

    // ------------------------------------------ escenarios minimos del laboratorio

    @Test
    void reservaNormalValidaRetorna40YQuedaConfirmada() {
        // Arrange
        Reserva reserva = reservaValida("R-001", "NORMAL");

        // Act
        double total = servicio.procesar(reserva, CINCO_HORAS);

        // Assert
        assertEquals(40.0, total, 0.0001);
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void reservaVipValidaRetorna34YQuedaConfirmada() {
        // Arrange
        Reserva reserva = reservaValida("R-002", "VIP");

        // Act
        double total = servicio.procesar(reserva, CINCO_HORAS);

        // Assert
        assertEquals(34.0, total, 0.0001);
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void correoSinArrobaRetorna0YNoConfirma() {
        // Arrange
        Reserva reserva = new Reserva("R-003", "incorrecto",
                INICIO, INICIO.plusHours(1), "NORMAL");

        // Act
        double total = servicio.procesar(reserva, CINCO_HORAS);

        // Assert
        assertEquals(0.0, total, 0.0001);
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void periodoConFinIgualAlInicioRetorna0YNoConfirma() {
        // Arrange
        Reserva reserva = new Reserva("R-004", "ana@uees.edu.ec",
                INICIO, INICIO, "NORMAL");

        // Act
        double total = servicio.procesar(reserva, CINCO_HORAS);

        // Assert
        assertEquals(0.0, total, 0.0001);
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void dosHorasDeAnticipacionPermitenProcesar() {
        // Arrange
        Reserva reserva = reservaValida("R-005", "NORMAL");

        // Act
        double total = servicio.procesar(reserva, 2);

        // Assert
        assertEquals(40.0, total, 0.0001);
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void unaHoraDeAnticipacionNoPermiteProcesar() {
        // Arrange
        Reserva reserva = reservaValida("R-006", "NORMAL");

        // Act
        double total = servicio.procesar(reserva, 1);

        // Assert
        assertEquals(0.0, total, 0.0001);
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    // ------------------------------------------ condiciones especiales

    @Test
    void reservaNulaRetorna0SinLanzarExcepcion() {
        // Act
        double total = assertDoesNotThrow(
                () -> servicio.procesar(null, CINCO_HORAS));

        // Assert
        assertEquals(0.0, total, 0.0001);
    }

    @Test
    void correoNuloRetorna0YNoConfirma() {
        // Arrange
        Reserva reserva = new Reserva("R-003b", null,
                INICIO, INICIO.plusHours(1), "NORMAL");

        // Act
        double total = servicio.procesar(reserva, CINCO_HORAS);

        // Assert
        assertEquals(0.0, total, 0.0001);
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void correoConSoloArrobaSigueSiendoAceptado() {
        // Arrange: la regla heredada es debil a proposito; aqui la conservo
        Reserva reserva = new Reserva("R-003c", "a@",
                INICIO, INICIO.plusHours(1), "NORMAL");

        // Act
        double total = servicio.procesar(reserva, CINCO_HORAS);

        // Assert
        assertEquals(40.0, total, 0.0001);
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void periodoConFinAnteriorAlInicioRetorna0YNoConfirma() {
        // Arrange
        Reserva reserva = new Reserva("R-004b", "ana@uees.edu.ec",
                INICIO, INICIO.minusHours(1), "NORMAL");

        // Act
        double total = servicio.procesar(reserva, CINCO_HORAS);

        // Assert
        assertEquals(0.0, total, 0.0001);
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void tipoVipEnMinusculaNoAplicaDescuento() {
        // Arrange: comportamiento actual (LB-08), no una regla deseada
        Reserva reserva = reservaValida("R-008", "vip");

        // Act
        double total = servicio.procesar(reserva, CINCO_HORAS);

        // Assert
        assertEquals(40.0, total, 0.0001);
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void tipoDesconocidoCobraTarifaBase() {
        // Arrange: comportamiento actual (LB-09)
        Reserva reserva = reservaValida("R-009", "PREMIUM");

        // Act
        double total = servicio.procesar(reserva, CINCO_HORAS);

        // Assert
        assertEquals(40.0, total, 0.0001);
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    // ------------------------------------------ efectos en consola y su orden

    @Test
    void reservaValidaGuardaYNotificaEnEseOrden() {
        // Arrange
        Reserva reserva = reservaValida("R-001", "NORMAL");

        // Act
        servicio.procesar(reserva, CINCO_HORAS);

        // Assert
        assertEquals(
                List.of("Guardando reserva R-001",
                        "Correo enviado a ana@uees.edu.ec"),
                lineasDeConsola());
    }

    @Test
    void reservaRechazadaNoGuardaNiNotifica() {
        // Arrange
        Reserva reserva = reservaValida("R-006", "NORMAL");

        // Act
        servicio.procesar(reserva, 1);

        // Assert
        assertTrue(lineasDeConsola().isEmpty());
    }

    @Test
    void correoInvalidoYUnaHoraSiguenRetornando0() {
        // Arrange: dos defectos a la vez; fija que el rechazo es silencioso
        Reserva reserva = new Reserva("R-010", "incorrecto",
                INICIO, INICIO.plusHours(1), "NORMAL");

        // Act
        double total = servicio.procesar(reserva, 1);

        // Assert
        assertEquals(0.0, total, 0.0001);
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertTrue(lineasDeConsola().isEmpty());
    }

    // ------------------------------------------ apoyo

    private static Reserva reservaValida(String id, String tipo) {
        return new Reserva(id, "ana@uees.edu.ec",
                INICIO, INICIO.plusHours(1), tipo);
    }

    private List<String> lineasDeConsola() {
        String texto = consola.toString(StandardCharsets.UTF_8).trim();
        return texto.isEmpty() ? List.of() : List.of(texto.split("\\R"));
    }
}
