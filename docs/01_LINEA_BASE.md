# Fase C | Línea base manual

Completa los seis escenarios **sin refactorizar el diseño**.

| ID | Escenario | Entrada principal | Estado | Retorno | Mensajes / excepción |
|---|---|---|---|---|---|
| LB-01 | NORMAL válida | NORMAL, correo válido, 5h | | | |
| LB-02 | VIP válida | VIP, correo válido, 5h | | | |
| LB-03 | Correo inválido | "incorrecto" | | | |
| LB-04 | Periodo inválido | fin <= inicio | | | |
| LB-05 | Límite válido | 2h anticipación | | | |
| LB-06 | Límite inválido | 1h anticipación | | | |

## Preguntas

1. ¿Qué valores cambian entre NORMAL y VIP?
2. ¿Qué casos dejan la reserva en PENDIENTE?
3. ¿Qué devuelve `procesar()` cuando una entrada no es procesable?
4. ¿Existe alguna excepción visible en el flujo actual?
5. ¿Qué mensajes aparecen solo cuando la reserva se confirma?
