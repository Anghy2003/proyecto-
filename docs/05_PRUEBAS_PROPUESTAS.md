# Fase K | Pruebas antes de proponer código

Para cada refactorización candidata defino primero **qué comportamiento debe
seguir siendo cierto** después del cambio, y solo después qué prueba lo
vigilaría. Ninguna de estas pruebas se implementa en el Laboratorio 1: son el
plan de la red de seguridad que construiré con JUnit 5 y estructura AAA en el
Laboratorio 2.

## Pruebas de caracterización (la base de todo)

Estas seis pruebas congelan la línea base ya observada y deben existir **antes**
de cualquier otro cambio.

| Escenario | Prueba propuesta | Qué afirma (Assert) |
|---|---|---|
| LB-01 | `normalRetorna40YConfirma()` | Retorno `40.0` y estado `CONFIRMADA`. |
| LB-02 | `vipConservaResultadoActual()` | Retorno `34.0` y estado `CONFIRMADA`. |
| LB-03 | `correoInvalidoNoProcesa()` | Retorno `0.0` y estado `PENDIENTE`. |
| LB-04 | `periodoInvalidoNoProcesa()` | Retorno `0.0` y estado `PENDIENTE`. |
| LB-05 | `dosHorasPermiteProcesar()` | Retorno `40.0` y estado `CONFIRMADA`. |
| LB-06 | `unaHoraNoPermiteProcesar()` | Retorno `0.0` y estado `PENDIENTE`. |

Ejemplo de estructura AAA que usaré (todavía no implementado):

```java
@Test
void vipConservaResultadoActual() {
    // Arrange
    LocalDateTime inicio = LocalDateTime.of(2026, 9, 22, 10, 0);
    Reserva reserva = new Reserva(
            "R-002", "ana@uees.edu.ec", inicio, inicio.plusHours(1), "VIP");

    // Act
    double total = new ServicioReservas().procesar(reserva, 5);

    // Assert
    assertEquals(34.0, total, 0.0001);
    assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
}
```

## Pruebas asociadas a cada refactorización candidata

| Refactorización candidata | Comportamiento a proteger | Prueba propuesta |
|---|---|---|
| Extraer cálculo VIP a un método con intención | VIP sigue retornando 34.0 y NORMAL 40.0 | `vipConservaResultadoActual()` + `normalRetorna40YConfirma()` |
| Extraer constante de anticipación | La frontera sigue siendo inclusiva: 2 h procesa, 1 h no | `dosHorasPermiteProcesar()` + `unaHoraNoPermiteProcesar()` |
| Separar notificación (`Notificador`) | Una reserva válida se sigue confirmando y el aviso se sigue emitiendo | `reservaValidaSeConfirma()` y, temporalmente, `reservaValidaNotificaAlCorreo()` capturando `System.out` |
| Separar persistencia (`RepositorioReservas`) | El guardado ocurre antes de confirmar y solo para reservas válidas | `reservaRechazadaNoSeGuarda()` (hoy verificable por ausencia de mensajes) |
| Simplificar / extraer validaciones | Los cuatro rechazos siguen retornando 0.0 sin excepción, y en el mismo orden de prioridad | Las cuatro pruebas de rechazo + `correoInvalidoYUnaHoraSigueRetornandoCero()` |
| Introducir `PeriodoReserva` | Un periodo inválido se sigue rechazando por el camino actual, sin excepción | `periodoInvalidoNoProcesa()` + `reservaConPeriodoInvalidoSePuedeConstruir()` (fija que hoy sí se puede) |
| Introducir `Correo` | La regla actual no se endurece por accidente | `correoInvalidoNoProcesa()` + `correoConArrobaMinimaSigueSiendoValido()` |
| Introducir `TipoReserva` (enum) | El comportamiento actual de los tipos no declarados se conserva o se cambia **a propósito** | `tipoEnMinusculaNoAplicaDescuento()` y `tipoDesconocidoCobraTarifaBase()` (documentan LB-08 y LB-09) |
| Reserva nula | Sigue retornando 0.0 sin lanzar excepción | `reservaNulaNoLanzaExcepcion()` |

## Qué no pruebo todavía, y por qué

- **Mocks / dobles de prueba.** La persistencia y la notificación solo existen
  como `System.out.println`. Verificarlos hoy implicaría capturar la salida
  estándar, lo que ata la prueba al texto literal del mensaje. Prefiero esperar
  a que existan las interfaces `RepositorioReservas` y `Notificador` y entonces
  usar dobles, como corresponde a las semanas siguientes.
- **La causa del rechazo.** Hoy no es observable: los cuatro caminos devuelven
  `0.0`. Una prueba no puede afirmar algo que el código no expone.

## Principio aplicado

Primero defino qué comportamiento necesito proteger; después decido cómo
reorganizar la estructura. Una prueba escrita después del cambio no protege
nada: solo documenta el resultado que quedó.
