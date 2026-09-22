package edu.uees.refactor.service;

import edu.uees.refactor.domain.Reserva;

/**
 * Código heredado intencional para el Laboratorio 1.
 *
 * IMPORTANTE:
 * No refactorizar antes de completar la línea base,
 * el diagnóstico y el plan de refactorización.
 */
public class ServicioReservas {

    public double procesar(
            Reserva r,
            int horasAnticipacion) {

        if (r == null) {
            return 0;
        }

        if (r.getCorreo() == null
                || !r.getCorreo().contains("@")) {
            return 0;
        }

        if (r.getInicio() == null
                || r.getFin() == null
                || !r.getFin().isAfter(r.getInicio())) {
            return 0;
        }

        if (horasAnticipacion < 2) {
            return 0;
        }

        double total = calcularTarifa(r);

        System.out.println(
                "Guardando reserva " + r.getId()
        );

        System.out.println(
                "Correo enviado a " + r.getCorreo()
        );

        r.confirmar();

        return total;
    }

    /**
     * Refactorizacion protegida del Laboratorio 2 (Extract Method):
     * el calculo de la tarifa sale del flujo principal a un metodo con
     * intencion. La regla no cambia: 40 de base y 15 % menos para VIP.
     */
    private double calcularTarifa(Reserva r) {
        double total = 40;

        if ("VIP".equals(r.getTipo())) {
            total = total * 0.85;
        }

        return total;
    }
}
