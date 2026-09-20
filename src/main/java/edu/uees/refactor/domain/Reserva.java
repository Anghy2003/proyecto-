package edu.uees.refactor.domain;

import java.time.LocalDateTime;

public class Reserva {

    private final String id;
    private final String correo;
    private final LocalDateTime inicio;
    private final LocalDateTime fin;
    private final String tipo;
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    public Reserva(
            String id,
            String correo,
            LocalDateTime inicio,
            LocalDateTime fin,
            String tipo) {

        this.id = id;
        this.correo = correo;
        this.inicio = inicio;
        this.fin = fin;
        this.tipo = tipo;
    }

    public void confirmar() {
        estado = EstadoReserva.CONFIRMADA;
    }

    public String getId() {
        return id;
    }

    public String getCorreo() {
        return correo;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public String getTipo() {
        return tipo;
    }

    public EstadoReserva getEstado() {
        return estado;
    }
}
