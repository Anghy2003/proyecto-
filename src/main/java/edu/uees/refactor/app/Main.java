package edu.uees.refactor.app;

import edu.uees.refactor.domain.Reserva;
import edu.uees.refactor.service.ServicioReservas;

import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {

        LocalDateTime inicio =
                LocalDateTime.now().plusDays(1);

        Reserva reserva =
                new Reserva(
                        "R-001",
                        "ana@uees.edu.ec",
                        inicio,
                        inicio.plusHours(1),
                        "VIP"
                );

        ServicioReservas servicio =
                new ServicioReservas();

        double total =
                servicio.procesar(
                        reserva,
                        5
                );

        System.out.println(
                "Estado: " + reserva.getEstado()
        );

        System.out.println(
                "Total: " + total
        );
    }
}
