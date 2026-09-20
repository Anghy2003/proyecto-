# Fase L | Plan priorizado de refactorización

Nada de esto se implementa en el Laboratorio 1. Es la secuencia que propongo,
ordenada por riesgo creciente: primero construyo la red de seguridad, después
hago los cambios internos baratos y solo al final los que tocan la construcción
de objetos.

| Orden | Cambio | Por qué primero / después | Pruebas requeridas | Dependencias |
|---:|---|---|---|---|
| 1 | Caracterizar los seis escenarios con JUnit 5 (AAA) | Sin pruebas, cualquier cambio posterior se hace a ciegas. Además convierte la línea base manual en verificación automática y repetible. | Las seis de caracterización (`normalRetorna40YConfirma`, `vipConservaResultadoActual`, `correoInvalidoNoProcesa`, `periodoInvalidoNoProcesa`, `dosHorasPermiteProcesar`, `unaHoraNoPermiteProcesar`) | Agregar `junit-jupiter` y `maven-surefire-plugin` al `pom.xml`, que hoy no tiene dependencias |
| 2 | Extraer la constante `HORAS_MINIMAS_ANTICIPACION = 2` y nombrar el cálculo del total | Riesgo bajo: no cruza fronteras de clase ni cambia firmas. Da nombre a dos reglas de negocio y prepara el terreno para separar la política de precios. | Las seis del paso 1; en particular LB-05 y LB-06, que fijan la frontera inclusiva | Paso 1 |
| 3 | Extraer las validaciones a métodos con intención (`correoEsValido`, `periodoEsValido`, `cumpleAnticipacion`) | Sigue siendo riesgo bajo mientras no se fusionen condiciones ni se altere su orden. Reduce el ruido del método principal y deja visible el flujo real. | Las cuatro de rechazo + un caso con dos defectos simultáneos, para fijar el orden de evaluación | Paso 2 |
| 4 | Separar persistencia y notificación (`RepositorioReservas`, `Notificador`) | Es el cambio que más responsabilidades quita al servicio y el que habilita usar dobles de prueba. Se hace después de los internos porque introduce colaboradores nuevos. | `reservaValidaSeConfirma()`, `reservaRechazadaNoSeGuarda()` y la verificación del orden persistencia → notificación → confirmar | Pasos 1–3. Habilita el uso de mocks a partir de aquí |
| 5 | Separar la política de precios (tarifa base y descuento VIP) | Aísla la razón de cambio de Finanzas. Conviene hacerlo antes del enum, porque deja un único lugar donde vive la interpretación del tipo. | `vipConservaResultadoActual()`, `normalRetorna40YConfirma()` y `tipoDesconocidoCobraTarifaBase()` | Paso 4 |
| 6 | Introducir `TipoReserva` como enum | Riesgo medio-alto: hay que decidir explícitamente qué pasa con `"vip"` y `"PREMIUM"`, que hoy se aceptan (LB-08 y LB-09). Esa decisión es funcional y debe declararse, no colarse dentro de una refactorización. | `tipoEnMinusculaNoAplicaDescuento()`, `tipoDesconocidoCobraTarifaBase()` + las de precio | Paso 5 y una decisión explícita sobre los tipos no declarados |
| 7 | Introducir `PeriodoReserva` y `Correo` | Los dejo al final porque cambian el constructor de `Reserva` y todos los puntos de creación, y porque mover la validación al constructor convertiría un retorno 0 en una excepción: eso sería cambio de contrato, no refactorización. | Las seis de caracterización + pruebas de construcción válida e inválida | Pasos 1–6 y una decisión sobre dónde se rechaza lo inválido |

## Lo que queda fuera de este plan

- **Distinguir la causa del rechazo** (`Result`, `Optional` o excepciones de
  dominio). Es el problema n.º 7 de la matriz de diagnóstico y es un **cambio
  funcional**: alteraría el contrato observable que acabo de fijar. Lo propongo
  como historia aparte, con autorización explícita, no dentro de una
  refactorización.
- **Convertir `double total` en un tipo `Dinero`.** Sin redondeos, múltiples
  monedas ni acumulaciones, no hay invariante que lo justifique todavía.
- **Aplicar Strategy para los tipos de reserva.** Con un solo descuento sería
  introducir un patrón sin necesidad. Tendría sentido a partir del tercer tipo
  con regla propia.

## Criterio que ordena el plan

Cada paso cumple tres condiciones antes de ejecutarse: existe una prueba que
falla si rompo el comportamiento, el cambio es reversible con un `git revert` de
un solo commit, y su justificación no es estética sino una razón de cambio
identificada en el mapa de responsabilidades.
