# Fase K | Proponer pruebas antes de proponer código

| Refactorización candidata | Comportamiento a proteger | Prueba propuesta |
|---|---|---|
| Extraer cálculo VIP | VIP conserva el resultado actual | vipConservaResultadoActual() |
| Simplificar validación | 1h retorna 0 y no confirma | unaHoraNoPermiteProcesar() |
| Separar notificación | Reserva válida sigue confirmándose | reservaValidaSeConfirma() |
| Introducir periodo | Periodo inválido sigue rechazándose | periodoInvalidoNoProcesa() |
| | | |

> Primero define qué comportamiento necesitas proteger; después decide cómo reorganizar la estructura.
