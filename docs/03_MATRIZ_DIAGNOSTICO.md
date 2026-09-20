# Fases E–I | Diagnóstico de clases, datos, condicionales y testabilidad

## Fase E | Diagnóstico de clases

### E.1 Long Class / exceso de responsabilidades

`ServicioReservas` tiene 55 líneas: el problema no es el tamaño, es que un solo
método concentra seis razones de cambio (ver [mapa de responsabilidades](02_MAPA_RESPONSABILIDADES.md)).

| Señal | ¿Aparece? | Evidencia |
|---|---|---|
| Responsabilidades de dominio e infraestructura mezcladas | Sí | Las líneas 37–41 calculan precio (dominio) y las 43–49 imprimen persistencia y correo (infraestructura), dentro del mismo método. |
| Varios motivos para cambiar | Sí | Seis motivos identificados: correo, periodo, anticipación, tarifa, persistencia y notificación. |
| Método principal con demasiadas decisiones | Sí | `procesar()` toma 5 decisiones (líneas 18, 22, 27, 33, 39) antes de producir un resultado. |
| Dependencias futuras difíciles de aislar | Sí | La persistencia y el correo están escritos como `System.out.println` fijos: no hay ningún punto de sustitución para una base de datos o un servidor de correo real. |

### E.2 Feature Envy

`procesar()` consulta seis veces datos de `Reserva` (`getCorreo()` dos veces,
`getInicio()`, `getFin()`, `getTipo()`, `getId()`) y decide con ellos, mientras
que `Reserva` no decide nada: solo entrega atributos.

Distinción que hago antes de proponer mover código:

| Conocimiento | ¿De quién es realmente? | Por qué |
|---|---|---|
| «Un correo válido contiene @» | De un concepto `Correo`, no de `Reserva` | Es una regla del dato, reutilizable fuera de una reserva. |
| «`fin` debe ser posterior a `inicio`» | De un concepto `PeriodoReserva` | Es una invariante del par de fechas; hoy una reserva puede existir con un periodo imposible. |
| «VIP paga 15 % menos» | De una política de precios, **no** de `Reserva` | Cambia por decisión comercial, no por la naturaleza de la reserva. |
| «Una reserva confirmada cambia de estado» | De `Reserva` | Ya está donde corresponde (línea 28). |

La conclusión no es «mover todo a `Reserva`»: parte del conocimiento pertenece a
value objects y otra parte a una política de precios separada.

### E.3 Shotgun Surgery potencial

Hoy la regla del correo está en un solo lugar, así que todavía **no** hay
Shotgun Surgery. El riesgo es futuro y concreto: como la validación no está
encapsulada en ningún tipo, el segundo servicio que reciba un correo copiará el
`contains("@")`. A partir de ahí, cambiar la regla obligará a tocar varios
sitios a la vez. Lo registro como riesgo latente, no como problema actual.

## Fase F | Diagnóstico de datos

### F.1 Primitive Obsession

| Dato actual | Concepto posible | Invariante que lo justifica | ¿Lo justifico hoy? |
|---|---|---|---|
| `String correo` | `Correo` | No nulo, formato válido, normalización | Sí: la regla ya existe y está suelta en el servicio. |
| `String tipo` | `TipoReserva` (enum) | Valores permitidos | Sí, y con evidencia: LB-08 (`"vip"`) y LB-09 (`"PREMIUM"`) se aceptan y cobran 40. |
| `LocalDateTime inicio` + `fin` | `PeriodoReserva` | `fin > inicio` | Sí: hoy `Reserva` permite construir un periodo inválido y nadie lo impide hasta llegar al servicio. |
| `double total` | `Dinero` | No negativo, moneda, redondeo | Todavía no. Con un solo importe y sin redondeos no hay invariante que lo justifique; lo dejo fuera del plan inmediato. |
| `int horasAnticipacion` | Política con nombre | Mínimo de 2 h | Parcial: más que un tipo nuevo, necesita una constante con nombre. |

No convierto todo primitivo en clase: solo aquellos donde ya identifiqué una
regla que hoy vive fuera del dato.

### F.2 Data Clumps

`inicio` y `fin` viajan siempre juntos: juntos en el constructor de `Reserva`
(líneas 17–18), juntos en los getters y juntos en la validación del servicio
(líneas 27–31). Comparten además una sola regla (`fin > inicio`). Ese patrón
—mismos datos, mismo orden, misma regla— es Data Clumps.

### F.3 Long Parameter List

```java
new Reserva(id, correo, inicio, fin, tipo);
```

Cinco parámetros, de los cuales tres son `String`. Todavía es legible, pero ya
es posible invertir `id` y `correo` —o pasar `tipo` donde va `correo`— y que el
compilador no diga nada: el error solo aparecería en ejecución como un rechazo
silencioso con retorno 0. Lo clasifico como problema incipiente, no crítico.

## Fase G | Diagnóstico de condicionales

| Condición | Líneas | Regla expresada | Riesgo de mantenimiento |
|---|---|---|---|
| `r == null` | 18–20 | No procesar ausencia de reserva | Bajo. Es una guarda defensiva legítima; lo único cuestionable es que devuelva el mismo `0` que un rechazo de negocio. |
| `correo == null` o sin `@` | 22–25 | Correo inválido | Medio. La regla no tiene nombre y es débil: `"a@"` se acepta. Cualquier endurecimiento obliga a editar el servicio. |
| `fin == null` o `fin` no posterior a `inicio` | 27–31 | Periodo inválido | Medio. Mezcla control de nulos con regla de negocio en una sola condición de tres partes. |
| `horasAnticipacion < 2` | 33–35 | Anticipación insuficiente | Alto. El `2` es un número mágico sin nombre ni constante; nada documenta que el límite es inclusivo (2 h sí procesa, LB-05). |
| `"VIP".equals(r.getTipo())` | 39–41 | Aplicar descuento | Alto. Es el punto que más probablemente crecerá (nuevos tipos y descuentos) y hoy depende de coincidencia exacta de texto (LB-08). |

### Criterio aplicado para decidir si un condicional merece refactorización

1. ¿Expresa una regla de negocio con nombre propio? → anticipación y descuento, sí.
2. ¿Se repite? → hoy no; el riesgo es de duplicación futura.
3. ¿Oculta el flujo principal? → sí: las cuatro guardas empujan el cálculo real al final del método.
4. ¿Mezcla validación con cálculo y efectos? → sí, todo ocurre dentro de `procesar()`.
5. ¿Es probable que la variante crezca? → el `if` de VIP, claramente.

## Fase H | Evaluación de testabilidad

| Zona | Qué sería deseable probar | Qué lo dificulta hoy |
|---|---|---|
| Descuento VIP | Que VIP retorne 34 y NORMAL 40 | Es comprobable por el valor de retorno, pero solo atravesando las cuatro validaciones; no se puede probar el cálculo aislado. |
| Notificación | Que se solicite enviar el correo | Solo existe `System.out.println`: verificarlo obliga a capturar `System.out`, lo que acopla la prueba al texto exacto del mensaje. |
| Persistencia | Que se guarde la reserva | Mismo caso: no hay colaborador observable, solo una impresión en consola. |
| Regla de periodo | Que se exija `fin > inicio` | La regla vive dentro del servicio, así que solo puede probarse indirectamente a través de `procesar()`. |
| Causa del rechazo | Distinguir correo inválido de anticipación insuficiente | Los cuatro rechazos devuelven `0.0` y dejan el estado PENDIENTE: la prueba no puede afirmar *por qué* falló. |

Anotación de alcance: no implemento mocks en esta semana. Solo dejo registrado
qué dependencias y efectos dificultan probar, para tenerlo resuelto cuando toque
introducir dobles de prueba.

## Fase I | Matriz de diagnóstico

| # | Ubicación | Smell / problema | Categoría | Impacto | Candidato | Prueba necesaria |
|---:|---|---|---|---|---|---|
| 1 | `ServicioReservas.procesar()` 43–49 | Persistencia y notificación resueltas con `System.out.println` dentro del método de negocio: infraestructura mezclada con dominio | Responsabilidades / Long Class | Alto: impide sustituir consola por base de datos o correo real, y obliga a capturar `System.out` para verificar efectos | Extract Class → `RepositorioReservas` y `Notificador` | `reservaValidaSeConfirma()` y `normalRetorna40()`: retorno y estado deben sobrevivir a la extracción |
| 2 | `ServicioReservas.procesar()` 18–53 | Un único método concentra validación, cálculo, efectos y coordinación (seis razones de cambio) | Responsabilidades / SRP | Alto: cualquier cambio de precio, de regla o de infraestructura toca la misma función, con riesgo de colisión entre cambios | Extract Method y después Extract Class por responsabilidad | Suite completa de caracterización LB-01…LB-06 antes de mover nada |
| 3 | `ServicioReservas` 33 (`horasAnticipacion < 2`) | Número mágico: la política de anticipación mínima no tiene nombre ni constante | Condicionales / legibilidad | Medio: la regla es invisible al leer y su carácter inclusivo (2 h sí procesa) no está documentado | Extraer constante `HORAS_MINIMAS_ANTICIPACION` y método con intención | `dosHorasPermiteProcesar()` y `unaHoraNoPermiteProcesar()`: fijan la frontera exacta (LB-05/LB-06) |
| 4 | `ServicioReservas` 39 y `Reserva.tipo` | Primitive Obsession: el tipo de reserva es `String` y el descuento depende de `"VIP".equals(...)` | Datos | Alto: `"vip"` cobra 40 (LB-08) y `"PREMIUM"` se acepta como válido (LB-09); el error es silencioso y llega al importe facturado | Introducir enum `TipoReserva` y separar la política de precio | `vipConservaResultadoActual()`, `normalRetorna40()` y un caso que fije hoy el comportamiento de `"vip"` |
| 5 | `Reserva` 9–10 y 17–18, `ServicioReservas` 27–31 | Data Clumps: `inicio` y `fin` viajan siempre juntos y comparten la regla `fin > inicio`, pero la regla vive fuera del dato | Datos | Medio-alto: `Reserva` puede construirse con un periodo imposible y nadie lo detecta hasta llegar al servicio | Introducir `PeriodoReserva` con la invariante | `periodoInvalidoNoProcesa()` antes de tocar el constructor |
| 6 | `ServicioReservas` 22–25 | La validación de correo (`contains("@")`) es una regla del dato implementada dentro del servicio, y es deliberadamente débil | Datos / Feature Envy | Medio: el siguiente servicio que reciba un correo copiará la regla (Shotgun Surgery latente); además `"a@"` se considera válido | Introducir `Correo` como value object | `correoInvalidoNoProcesa()` y un caso que documente qué acepta hoy la regla |
| 7 | `ServicioReservas.procesar()` 19, 24, 30 y 34 | Los cuatro rechazos devuelven el mismo `0` y dejan el mismo estado: el valor de retorno está sobrecargado | Condicionales / contrato | Medio: quien llama no puede distinguir la causa ni diferenciar «rechazado» de «importe cero» | `Result`/`Optional` o excepciones de dominio — **cambio funcional**, no refactorización: requiere autorización | Las cuatro pruebas de rechazo deben existir antes, porque este cambio sí altera el contrato observable |
| 8 | `Reserva` 14–19 | Long Parameter List incipiente: cinco parámetros, tres `String` intercambiables sin error de compilación | Datos | Bajo-medio: un intercambio accidental produce un rechazo silencioso con retorno 0, no una excepción | Agrupar en value objects (`Correo`, `PeriodoReserva`, `TipoReserva`) deja la lista en tres | Pruebas de construcción válida e inválida una vez introducidos los tipos |

Los ocho problemas están ubicados por archivo y línea, y cada uno indica qué
riesgo produce y cómo verificaría el cambio. La fila 7 la marco explícitamente
como cambio funcional —no como refactorización— porque modificaría el contrato
observable que acabo de fijar en la línea base.
