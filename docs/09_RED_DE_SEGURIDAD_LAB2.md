# Laboratorio 2 | Red de seguridad con JUnit 5

Rama `lab2-red-de-seguridad`, creada desde `main` (estado final del
Laboratorio 1). Aquí no hay diagnóstico nuevo: el objetivo es proteger con
pruebas el comportamiento que las refactorizaciones planificadas podrían
romper, hacer una primera refactorización pequeña y comprobar en la práctica
cómo una prueba detecta una regresión.

## Punto de partida

```bash
git status            # working tree clean, sobre main
mvn clean compile     # BUILD SUCCESS, 5 archivos (los 4 heredados + LineaBase)
```

## Configuración

`pom.xml`: `junit-jupiter` 5.10.2 con `scope` `test` y `maven-surefire-plugin`
3.2.5. Estructura `src/main/java` (código) y `src/test/java` (pruebas), con
el mismo paquete que la clase probada.

## Pruebas de caracterización (17) y qué protege cada una

Todas usan Arrange → Act → Assert con los tres bloques marcados.

| Prueba | Comportamiento protegido | Aserciones |
|---|---|---|
| `reservaNormalValidaRetorna40YQuedaConfirmada` | LB-01: se confirma y devuelve 40 | `assertEquals` ×2 |
| `reservaVipValidaRetorna34YQuedaConfirmada` | LB-02: se confirma y devuelve 34 | `assertEquals` ×2 |
| `correoSinArrobaRetorna0YNoConfirma` | LB-03: retorna 0 y queda PENDIENTE | `assertEquals` ×2 |
| `periodoConFinIgualAlInicioRetorna0YNoConfirma` | LB-04: retorna 0 y queda PENDIENTE | `assertEquals` ×2 |
| `dosHorasDeAnticipacionPermitenProcesar` | LB-05: 2 h procesa (frontera inclusiva) | `assertEquals` ×2 |
| `unaHoraDeAnticipacionNoPermiteProcesar` | LB-06: 1 h retorna 0 y no confirma | `assertEquals` ×2 |
| `reservaNulaRetorna0SinLanzarExcepcion` | LB-07: `null` no lanza excepción, retorna 0 | `assertDoesNotThrow`, `assertEquals` |
| `correoNuloRetorna0YNoConfirma` | Correo ausente: mismo rechazo silencioso | `assertEquals` ×2 |
| `correoConSoloArrobaSigueSiendoAceptado` | La regla débil (`"a@"`) no se endurece por accidente | `assertEquals` ×2 |
| `periodoConFinAnteriorAlInicioRetorna0YNoConfirma` | Fin anterior al inicio: rechazo | `assertEquals` ×2 |
| `tipoVipEnMinusculaNoAplicaDescuento` | LB-08: `"vip"` cobra 40 (comparación exacta) | `assertEquals` ×2 |
| `tipoDesconocidoCobraTarifaBase` | LB-09: `"PREMIUM"` cobra 40 | `assertEquals` ×2 |
| `reservaValidaGuardaYNotificaEnEseOrden` | Mensajes exactos y orden guardar → notificar | `assertEquals` sobre `System.out` |
| `reservaRechazadaNoGuardaNiNotifica` | Un rechazo no imprime nada | `assertTrue` |
| `correoInvalidoYUnaHoraSiguenRetornando0` | Dos defectos a la vez: rechazo silencioso | `assertEquals`, `assertTrue` |
| `reservaNuevaIniciaPendiente` (`ReservaTest`) | Estado inicial no nulo y PENDIENTE | `assertNotNull`, `assertEquals` |
| `confirmarCambiaElEstadoAConfirmada` (`ReservaTest`) | `confirmar()` cambia el estado | `assertEquals` |

Resultado: `Tests run: 17, Failures: 0, Errors: 0` → `BUILD SUCCESS`
(`docs/lab2/tests-01-red-de-seguridad.txt`, `cap2-tests-verde.png`).
Commit: `test: caracterizar comportamiento heredado`.

## Primera refactorización protegida

Extract Method: el cálculo de la tarifa sale de `procesar()` a
`calcularTarifa(Reserva)`. Ningún otro cambio.

```java
// antes (dentro de procesar)
double total = 40;
if ("VIP".equals(r.getTipo())) {
    total = total * 0.85;
}

// despues
double total = calcularTarifa(r);

private double calcularTarifa(Reserva r) {
    double total = 40;
    if ("VIP".equals(r.getTipo())) {
        total = total * 0.85;
    }
    return total;
}
```

`mvn clean test` → 17 verdes (`tests-02-refactor-tarifa.txt`,
`cap3-refactor-verde.png`). Commit: `refactor: extraer calculo de tarifa`.

## Experimento de aprendizaje: 0.85 → 0.80

Cambié temporalmente `0.85` por `0.80` en `calcularTarifa` y corrí la suite:

```
[ERROR] ServicioReservasTest.reservaVipValidaRetorna34YQuedaConfirmada:74
        expected: <34.0> but was: <32.0>
[ERROR] Tests run: 17, Failures: 1, Errors: 0, Skipped: 0
[INFO] BUILD FAILURE
```

Falló exactamente una prueba, la de VIP, porque 40 × 0,80 = 32. Las otras 16
siguieron verdes: NORMAL no usa el factor. Después revertí con
`git checkout -- ServicioReservas.java` y la suite volvió a
`Tests run: 17, Failures: 0` (`tests-03-experimento-regresion.txt`,
`tests-04-revertido.txt`, `cap4-experimento-regresion.png`). El experimento
no se commitea: su evidencia sí.

## Reflexión técnica

**¿Qué prueba es la más importante para comenzar la refactorización?**
`reservaValidaGuardaYNotificaEnEseOrden()`. Las refactorizaciones que vienen
(extraer persistencia y notificación) pueden perder un mensaje o cambiar su
orden sin que el retorno ni el estado cambien; ninguna otra prueba lo vería.
En segundo lugar, las dos de límite (2 h / 1 h), porque la constante de
anticipación es lo primero que se extrae.

**¿Qué comportamiento fue más difícil de caracterizar?** Los efectos en
consola. No hay un valor que consultar: tuve que capturar `System.out` con un
`ByteArrayOutputStream` en `@BeforeEach` y restaurarlo en `@AfterEach`, y
comparar las líneas exactas. También el rechazo silencioso: los cuatro caminos
de error devuelven el mismo 0, así que la prueba no puede afirmar *por qué*
falló, solo que falló.

**¿Qué diferencia hay entre comprobar un valor retornado y el estado de un
objeto?** El retorno es el resultado de la función (34.0); el estado es un
efecto sobre el objeto que se pasó (CONFIRMADA). Una refactorización puede
conservar uno y romper el otro: si `confirmar()` se pierde al mover código, el
total sigue siendo 34 pero la reserva queda PENDIENTE. Por eso cada prueba
del flujo válido afirma las dos cosas.

**¿Cómo detectó JUnit la regresión del experimento?** `assertEquals(34.0,
total, 0.0001)` comparó el valor esperado con el real y lanzó
`AssertionFailedError: expected: <34.0> but was: <32.0>`, con la clase, el
método y la línea 74. Surefire contó `Failures: 1` y Maven terminó en
`BUILD FAILURE`. El nombre de la prueba dijo qué regla se rompió sin abrir el
código.

**¿Qué riesgo habría al hacer varias refactorizaciones antes de volver a
ejecutar las pruebas?** Si una prueba falla no sé cuál de los cambios la
rompió; tengo que revisar todos o revertirlos en bloque, y pierdo también los
cambios correctos. Con un cambio por ejecución, el culpable es siempre el
último `diff`, y revertir cuesta un comando.

## Relación con la Ae5

La rama `ae5-refactorizacion` parte de esta misma suite (mismas 17 pruebas
como primer commit) y la amplía a 31 con cinco refactorizaciones de mayor
alcance.
