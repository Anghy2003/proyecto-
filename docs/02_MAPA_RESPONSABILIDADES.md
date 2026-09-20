# Fase D | Mapa actual de responsabilidades

Antes de nombrar smells identifico qué hace cada fragmento y a qué clase
pertenece hoy. Las líneas citadas corresponden al código tal como llegó.

| Fragmento | Líneas | Responsabilidad observada | Clase actual | ¿Es su responsabilidad natural? |
|---|---|---|---|---|
| `if (r == null)` | 18–20 | Protección contra ausencia de argumento | `ServicioReservas` | Sí, es propia del servicio que recibe el parámetro. |
| `r.getCorreo() == null \|\| !contains("@")` | 22–25 | Validación del formato de correo | `ServicioReservas` | No: es una regla del dato «correo», no del proceso de reserva. |
| `!r.getFin().isAfter(r.getInicio())` | 27–31 | Validación del periodo (fin > inicio) | `ServicioReservas` | No: es una invariante del par inicio/fin. |
| `horasAnticipacion < 2` | 33–35 | Política de anticipación mínima | `ServicioReservas` | Discutible: es una política de negocio, hoy sin nombre y con el 2 incrustado. |
| `double total = 40;` | 37 | Tarifa base | `ServicioReservas` | No: es política de precios. |
| `if ("VIP".equals(r.getTipo())) total *= 0.85;` | 39–41 | Descuento por tipo de cliente | `ServicioReservas` | No: es política de precios, y además interpreta un `String` del dominio. |
| `System.out.println("Guardando reserva ...")` | 43–45 | Persistencia simulada | `ServicioReservas` | No: es infraestructura. |
| `System.out.println("Correo enviado a ...")` | 47–49 | Notificación simulada | `ServicioReservas` | No: es infraestructura. |
| `r.confirmar()` | 51 | Orden de cambio de estado | `ServicioReservas` | Sí, el servicio coordina; el cambio en sí vive en `Reserva`. |
| `estado = EstadoReserva.CONFIRMADA` | `Reserva` 28–30 | Cambio de estado de dominio | `Reserva` | Sí. |
| Almacenar id, correo, inicio, fin, tipo | `Reserva` 7–26 | Contenedor de datos | `Reserva` | Sí, pero sin ninguna regla propia: hoy es casi un registro pasivo. |

## Mapa conceptual

```text
ServicioReservas  (un único método público: procesar)
├── valida ausencia de reserva
├── interpreta el correo            → regla del dato
├── interpreta el periodo           → regla del dato
├── decide la anticipación mínima   → política de negocio
├── fija la tarifa base             → política de precios
├── conoce el descuento VIP         → política de precios
├── simula persistencia             → infraestructura
├── simula notificación             → infraestructura
└── ordena confirmar la Reserva     → coordinación

Reserva
├── guarda id, correo, inicio, fin, tipo
└── mantiene y cambia su estado
```

## Pregunta clave: ¿cuántas razones tiene `ServicioReservas` para cambiar?

Conté **seis razones independientes**, y cada una proviene de un interesado
distinto:

| # | Si cambia… | Hay que editar `procesar()` porque… | Quién lo pide |
|---:|---|---|---|
| 1 | La regla de correo (dominios permitidos) | La condición vive en las líneas 22–25 | Reglas de admisión |
| 2 | La regla de periodo (duración mínima, solapes) | La condición vive en las líneas 27–31 | Coordinación académica |
| 3 | La anticipación mínima (de 2 h a 4 h) | El literal está en la línea 33 | Coordinación académica |
| 4 | La tarifa o el descuento (40, 0,85, nuevos tipos) | Están en las líneas 37–41 | Finanzas |
| 5 | La persistencia (guardar en base de datos) | El `println` está en las líneas 43–45 | Infraestructura |
| 6 | La notificación (correo real, SMS) | El `println` está en las líneas 47–49 | Infraestructura |

Seis motivos de cambio en 40 líneas es la evidencia concreta de que la clase
concentra demasiado, y es lo que sustenta las filas 1 y 2 de la matriz de
diagnóstico. El desequilibrio se ve también del otro lado: `Reserva` tiene
**una sola** razón para cambiar y ningún comportamiento propio más allá de
`confirmar()`.
