# Fase A–C | Entorno, ejecución y línea base manual

> Regla respetada en este laboratorio: **no refactoricé nada**. Para observar los
> escenarios agregué una clase aparte (`app/LineaBase.java`) que solo invoca al
> código heredado con distintas entradas. `ServicioReservas`, `Reserva`,
> `EstadoReserva` y `Main` quedaron exactamente como llegaron.

## Fase A | Validación del entorno

Evidencia completa en [`evidencia/00-entorno.txt`](evidencia/00-entorno.txt).

| Herramienta | Versión verificada |
|---|---|
| Java | 21.0.10 LTS (Oracle, JDK 21.0.10) |
| Maven | Apache Maven 3.9.16 |
| Git | 2.54.0.windows.1 |
| Sistema | Windows 11, encoding de plataforma UTF-8 |

Compilación (`mvn clean compile`) → **BUILD SUCCESS**, 4 archivos fuente
compilados. Evidencia en [`evidencia/01-compilacion.txt`](evidencia/01-compilacion.txt).

## Fase B | Ejecución inicial sin interpretar

```bash
mvn exec:java -Dexec.mainClass="edu.uees.refactor.app.Main"
```

Salida real obtenida ([`evidencia/02-ejecucion-main.txt`](evidencia/02-ejecucion-main.txt)):

```text
Guardando reserva R-001
Correo enviado a ana@uees.edu.ec
Estado: CONFIRMADA
Total: 34.0
```

La salida coincide con la evidencia esperada del enunciado; no hubo diferencias
que registrar.

### Observable vs. detalle interno

| Observación | ¿Comportamiento observable? | ¿Detalle interno? | Comentario |
|---|---|---|---|
| La reserva termina CONFIRMADA | Sí | No | Es el resultado que percibe quien usa el sistema; debe preservarse en cualquier refactorización. |
| Se imprime un mensaje de persistencia | Sí, hoy | Parcialmente | El *hecho* de guardar es observable; que se haga con `System.out.println` es una decisión interna sustituible. |
| `ServicioReservas` contiene un `if` de VIP | No | Sí | Es estructura: puede desaparecer sin que cambie ningún resultado. |
| El total VIP es 34 | Sí | No | Contrato numérico verificable; cambiarlo sería un cambio funcional, no una refactorización. |
| `Reserva` almacena `inicio` y `fin` por separado | No | Sí | Forma de representar los datos; podría agruparse sin alterar la salida. |

La diferencia práctica: *«VIP devuelve 34»* describe comportamiento, *«se usa un
if para VIP»* describe estructura. La refactorización puede cambiar lo segundo y
está obligada a conservar lo primero.

## Fase C | Línea base de los seis escenarios

Ejecución reproducible (fecha de inicio fija `2026-09-22T10:00`):

```bash
mvn exec:java -Dexec.mainClass="edu.uees.refactor.app.LineaBase"
```

Salida completa en [`evidencia/03-linea-base.txt`](evidencia/03-linea-base.txt).

| ID | Escenario | Entrada principal | Estado | Retorno | Mensajes / excepción |
|---|---|---|---|---|---|
| LB-01 | NORMAL válida | tipo `NORMAL`, `ana@uees.edu.ec`, 5 h | CONFIRMADA | `40.0` | `Guardando reserva R-001` + `Correo enviado a ana@uees.edu.ec`. Sin excepción. |
| LB-02 | VIP válida | tipo `VIP`, `ana@uees.edu.ec`, 5 h | CONFIRMADA | `34.0` | `Guardando reserva R-002` + `Correo enviado a ana@uees.edu.ec`. Sin excepción. |
| LB-03 | Correo inválido | correo `"incorrecto"`, 5 h | PENDIENTE | `0.0` | Ninguno. Sin excepción y sin explicación del rechazo. |
| LB-04 | Periodo inválido | `fin = inicio`, 5 h | PENDIENTE | `0.0` | Ninguno. Sin excepción. |
| LB-05 | Límite válido | 2 h de anticipación | CONFIRMADA | `40.0` | `Guardando reserva R-005` + `Correo enviado a ana@uees.edu.ec`. |
| LB-06 | Límite inválido | 1 h de anticipación | PENDIENTE | `0.0` | Ninguno. Sin excepción. |

LB-05 y LB-06 delimitan la frontera exacta de la regla: **2 h procesa, 1 h no**.

### Observaciones complementarias

Agregué tres casos más porque aportan evidencia para el diagnóstico de datos:

| ID | Escenario | Estado | Retorno | Qué evidencia |
|---|---|---|---|---|
| LB-07 | Reserva nula, 5 h | sin reserva | `0.0` | El `null` se absorbe en silencio: el mismo `0` que un correo inválido. |
| LB-08 | Tipo `"vip"` en minúscula, 5 h | CONFIRMADA | `40.0` | El descuento depende de la comparación exacta `"VIP".equals(...)`: un dato tipográficamente distinto cobra tarifa completa sin avisar. |
| LB-09 | Tipo `"PREMIUM"` (inexistente), 5 h | CONFIRMADA | `40.0` | Un tipo desconocido se acepta y se confirma como si fuera NORMAL. |

Estos tres casos no son bugs declarados por el enunciado: son **comportamiento
actual** que también quedaría congelado por las pruebas de caracterización, y por
eso los registro antes de proponer cambios.

## Preguntas de análisis

1. **¿Qué valores cambian entre NORMAL y VIP?** Solo el retorno: `40.0` frente a
   `34.0` (40 × 0,85). El estado final, los mensajes y el flujo son idénticos.
2. **¿Qué casos dejan la reserva en PENDIENTE?** LB-03, LB-04 y LB-06, es decir
   los tres rechazos por validación. La reserva nunca pasa a CANCELADA: ese valor
   del enum existe pero ninguna ruta del código lo usa.
3. **¿Qué devuelve `procesar()` cuando la entrada no es procesable?** Siempre
   `0.0`, sin distinguir la causa ni lanzar excepción. El `0` es a la vez «no se
   pudo procesar» y un posible importe, lo que lo vuelve ambiguo.
4. **¿Existe alguna excepción visible en el flujo actual?** No. Ni siquiera con
   reserva nula (LB-07): el método la intercepta en la primera guarda.
5. **¿Qué mensajes aparecen solo cuando la reserva se confirma?**
   `Guardando reserva <id>` y `Correo enviado a <correo>`. Ambos se imprimen
   *antes* de `r.confirmar()`, así que el orden observable es: persistencia,
   notificación y por último cambio de estado.

## Punto de control B

Los seis escenarios están completos, ejecutados y explicados. Puedo pasar al
diagnóstico.
