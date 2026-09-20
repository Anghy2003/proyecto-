# Fase J | Matriz de riesgo

Evalúo cada cambio candidato por dos cosas distintas: qué tan fácil es
equivocarse al aplicarlo (**probabilidad**) y qué tan grave sería que el error
pasara inadvertido (**impacto**). El riesgo resultante es lo que define el orden
del plan.

## Escala

| Nivel | Interpretación |
|---|---|
| Bajo | Cambio local, comportamiento bien entendido y prueba fácil de crear. |
| Medio | Afecta varias decisiones o requiere adaptar la construcción de objetos. |
| Alto | Puede alterar el contrato observable, los flujos de error o efectos externos. |

## Matriz

| Cambio candidato | Probabilidad de romper | Impacto si rompe | Riesgo | Cómo reducirlo |
|---|---|---|---|---|
| Extraer clase de notificación (`Notificador`) | Baja: el bloque a mover son dos líneas contiguas sin lógica | Medio: si el mensaje deja de emitirse o cambia de orden respecto a `confirmar()`, se pierde un efecto que hoy es observable | **Bajo-medio** | Fijar primero LB-01 y LB-02 con sus mensajes; mover el bloque sin reescribir el texto; conservar el orden persistencia → notificación → confirmar, que es parte de lo observado. |
| Introducir `Correo` | Media: obliga a cambiar el constructor de `Reserva` y todos los puntos de creación (`Main`, `LineaBase`) | Alto: si el nuevo tipo valida más estricto que `contains("@")`, casos que hoy retornan 40 pasarían a retornar 0 | **Alto** | Replicar *exactamente* la regla actual, incluido lo que hoy acepta (`"a@"`); dejar el endurecimiento para un cambio funcional posterior y declarado; cubrir LB-01, LB-02 y LB-03 antes de empezar. |
| Introducir `PeriodoReserva` | Media: cambia la firma del constructor y agrupa dos parámetros | Alto: si la invariante se valida en el constructor, LB-04 dejaría de retornar 0 y lanzaría excepción — eso rompe el contrato | **Alto** | Decidir primero dónde vive el rechazo: mientras `procesar()` siga devolviendo 0 en periodo inválido, el comportamiento se conserva. Proteger con `periodoInvalidoNoProcesa()` y con una prueba de construcción. |
| Simplificar validaciones | Baja: son guardas independientes y ya están en forma de *early return* | Alto: reordenarlas o fusionarlas cambia cuál gana cuando hay dos defectos a la vez, y con ello qué caso se rechaza primero | **Medio** | No fusionar condiciones; extraer cada guarda a un método con nombre y mantener el orden original. Añadir un caso con correo inválido *y* 1 h para fijar el orden de evaluación. |
| Separar cálculo VIP | Baja: el bloque son tres líneas y el resultado es un `double` fácil de comparar | Alto: un redondeo o un cambio de orden en la multiplicación altera el importe, que es el dato más visible del sistema | **Medio** | Comparar con `assertEquals(34.0, ..., 0.0001)` sobre los valores reales de LB-01 y LB-02, y congelar además el comportamiento de `"vip"` (LB-08) para no «arreglarlo» sin decidirlo. |

## Lectura de la matriz

Los dos cambios más atractivos a primera vista —`Correo` y `PeriodoReserva`—
son los de mayor riesgo, porque tocan la construcción de objetos y pueden
convertir un rechazo silencioso en una excepción. Los de menor riesgo son los
que no cruzan fronteras de clase: extraer la constante de anticipación y dar
nombre al cálculo del total.

Eso invierte el orden intuitivo de trabajo: primero lo interno y barato, después
lo estructural y caro, y siempre con la red de pruebas puesta antes.
