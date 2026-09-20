# Semana 6 | Laboratorio evaluado 1 · Diagnóstico técnico de código heredado

Diagnóstico del sistema de reservas de tutorías heredado. El objetivo de este
laboratorio **no** es mejorar el código: es comprender qué hace, evidenciarlo y
decidir con argumentos qué debería refactorizarse y con qué pruebas.

- Asignatura: Diseño de Software (UCOM0310), Semana 6 · PEL 4 – 2026
- Autora: Andrea Illescas
- Java 21 · Maven 3.9.16 · Git 2.54

> **Regla respetada:** no apliqué Extract Class, Move Method, Value Objects,
> simplificación de condicionales, patrones ni mocks. `ServicioReservas`,
> `Reserva`, `EstadoReserva` y `Main` están exactamente como llegaron en el
> proyecto base (commit inicial `chore: registrar proyecto heredado y linea base`).
> Lo único que agregué es `app/LineaBase.java`, una clase de observación que
> invoca al código heredado sin modificarlo, tal como permite la Fase C.

## Reproducir la evidencia

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="edu.uees.refactor.app.Main"
mvn exec:java -Dexec.mainClass="edu.uees.refactor.app.LineaBase"
```

La segunda ejecución imprime los nueve escenarios de la línea base con una fecha
de inicio fija (`2026-09-22T10:00`), de modo que el resultado es idéntico en
cualquier corrida.

## Línea base observada

| ID | Escenario | Estado | Retorno |
|---|---|---|---|
| LB-01 | NORMAL válida, 5 h | CONFIRMADA | `40.0` |
| LB-02 | VIP válida, 5 h | CONFIRMADA | `34.0` |
| LB-03 | Correo inválido | PENDIENTE | `0.0` |
| LB-04 | Periodo inválido (`fin = inicio`) | PENDIENTE | `0.0` |
| LB-05 | Límite válido, 2 h | CONFIRMADA | `40.0` |
| LB-06 | Límite inválido, 1 h | PENDIENTE | `0.0` |
| LB-07 | Reserva nula | sin reserva | `0.0` |
| LB-08 | Tipo `"vip"` en minúscula | CONFIRMADA | `40.0` |
| LB-09 | Tipo desconocido `"PREMIUM"` | CONFIRMADA | `40.0` |

LB-07, LB-08 y LB-09 son observaciones que agregué por mi cuenta: no estaban en
el enunciado y son las que revelan que el descuento depende de una comparación
exacta de texto.

## Documentos del diagnóstico

| Archivo | Contenido |
|---|---|
| [`docs/01_LINEA_BASE.md`](docs/01_LINEA_BASE.md) | Entorno, ejecución inicial, observable vs. detalle interno y los seis escenarios |
| [`docs/02_MAPA_RESPONSABILIDADES.md`](docs/02_MAPA_RESPONSABILIDADES.md) | Responsabilidades por fragmento y las seis razones de cambio |
| [`docs/03_MATRIZ_DIAGNOSTICO.md`](docs/03_MATRIZ_DIAGNOSTICO.md) | Diagnóstico de clases, datos, condicionales, testabilidad y la matriz con ocho problemas |
| [`docs/04_MATRIZ_RIESGO.md`](docs/04_MATRIZ_RIESGO.md) | Probabilidad, impacto y mitigación de cada cambio candidato |
| [`docs/05_PRUEBAS_PROPUESTAS.md`](docs/05_PRUEBAS_PROPUESTAS.md) | Qué prueba debe existir antes de cada refactorización |
| [`docs/06_PLAN_REFACTORIZACION.md`](docs/06_PLAN_REFACTORIZACION.md) | Plan priorizado en siete pasos y lo que queda fuera |
| [`docs/07_REFLEXION_TECNICA.md`](docs/07_REFLEXION_TECNICA.md) | Reflexión técnica y checklist del producto |

## Evidencia de ejecución

| Archivo | Qué muestra |
|---|---|
| `docs/evidencia/00-entorno.txt` | Versiones de Java, Maven y Git |
| `docs/evidencia/01-compilacion.txt` | `BUILD SUCCESS` del proyecto base (4 fuentes) |
| `docs/evidencia/02-ejecucion-main.txt` | Salida original de `Main` |
| `docs/evidencia/03-linea-base.txt` | Los nueve escenarios |
| `docs/evidencia/04-compilacion-final.txt` | `BUILD SUCCESS` con la clase de observación (5 fuentes) |
| `docs/cap1-entorno.png` … `docs/cap5-git-log.png` | Capturas de consola de cada paso |

## Hallazgos principales

1. `ServicioReservas.procesar()` tiene **seis razones de cambio** distintas:
   correo, periodo, anticipación, tarifa, persistencia y notificación.
2. Persistencia y notificación son `System.out.println` dentro del método de
   negocio: no hay punto de sustitución ni forma limpia de verificarlos.
3. El `2` de la anticipación mínima es un número mágico y su frontera inclusiva
   (2 h sí procesa) no está documentada en ninguna parte.
4. El tipo de reserva es un `String`: `"vip"` cobra tarifa completa y
   `"PREMIUM"` se acepta como válido, ambos sin aviso.
5. Los cuatro rechazos devuelven `0.0`: quien llama no puede distinguir la causa.

## Estado del repositorio

El historial deja recuperable el proyecto heredado tal como llegó y separa la
evidencia del diagnóstico:

```bash
git log --oneline
```

En el Laboratorio 2 esta línea base se convierte en pruebas JUnit 5 con
estructura AAA, antes de tocar la estructura del código.
