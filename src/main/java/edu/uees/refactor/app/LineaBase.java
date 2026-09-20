package edu.uees.refactor.app;

import edu.uees.refactor.domain.Reserva;
import edu.uees.refactor.service.ServicioReservas;

import java.time.LocalDateTime;

/**
 * Ejecutor de observacion para la Fase C del Laboratorio 1.
 *
 * Esta clase NO refactoriza ni modifica el codigo heredado:
 * unicamente invoca ServicioReservas.procesar() con distintas
 * entradas y registra el comportamiento observable de cada caso.
 *
 * La fecha de inicio es fija para que la evidencia sea reproducible
 * en cualquier ejecucion.
 */
public class LineaBase {

    private static final LocalDateTime INICIO =
            LocalDateTime.of(2026, 9, 22, 10, 0);

    public static void main(String[] args) {

        ejecutar(
                "LB-01",
                "NORMAL valida | correo valido | 5 h",
                new Reserva("R-001", "ana@uees.edu.ec",
                        INICIO, INICIO.plusHours(1), "NORMAL"),
                5);

        ejecutar(
                "LB-02",
                "VIP valida | correo valido | 5 h",
                new Reserva("R-002", "ana@uees.edu.ec",
                        INICIO, INICIO.plusHours(1), "VIP"),
                5);

        ejecutar(
                "LB-03",
                "Correo invalido | \"incorrecto\" | 5 h",
                new Reserva("R-003", "incorrecto",
                        INICIO, INICIO.plusHours(1), "NORMAL"),
                5);

        ejecutar(
                "LB-04",
                "Periodo invalido | fin = inicio | 5 h",
                new Reserva("R-004", "ana@uees.edu.ec",
                        INICIO, INICIO, "NORMAL"),
                5);

        ejecutar(
                "LB-05",
                "Limite valido | 2 h de anticipacion",
                new Reserva("R-005", "ana@uees.edu.ec",
                        INICIO, INICIO.plusHours(1), "NORMAL"),
                2);

        ejecutar(
                "LB-06",
                "Limite invalido | 1 h de anticipacion",
                new Reserva("R-006", "ana@uees.edu.ec",
                        INICIO, INICIO.plusHours(1), "NORMAL"),
                1);

        System.out.println("--- Observaciones complementarias ---");
        System.out.println();

        ejecutar(
                "LB-07",
                "Reserva nula | 5 h",
                null,
                5);

        ejecutar(
                "LB-08",
                "Tipo \"vip\" en minuscula | 5 h",
                new Reserva("R-008", "ana@uees.edu.ec",
                        INICIO, INICIO.plusHours(1), "vip"),
                5);

        ejecutar(
                "LB-09",
                "Tipo desconocido \"PREMIUM\" | 5 h",
                new Reserva("R-009", "ana@uees.edu.ec",
                        INICIO, INICIO.plusHours(1), "PREMIUM"),
                5);
    }

    private static void ejecutar(
            String id,
            String escenario,
            Reserva reserva,
            int horasAnticipacion) {

        System.out.println("=== " + id + " | " + escenario + " ===");

        ServicioReservas servicio = new ServicioReservas();

        double total = servicio.procesar(reserva, horasAnticipacion);

        System.out.println("Retorno: " + total);
        System.out.println("Estado : "
                + (reserva == null ? "sin reserva" : reserva.getEstado()));
        System.out.println();
    }
}
