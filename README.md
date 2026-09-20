# UEES | Diseño de Software | UCOM0310
## Semana 6 | Laboratorio evaluado 1
### Diagnóstico técnico de código heredado

Este es el **proyecto base** para desarrollar la actividad:

**Actividad 1 | Diagnóstico de código heredado**

La actividad consiste en **comprender, observar, evidenciar y diagnosticar** el código antes de realizar cualquier refactorización estructural.

> **Regla del laboratorio:** durante el Laboratorio 1 NO debes refactorizar todavía.

---

## Requisitos

- Java 21
- Maven
- Git
- IDE de preferencia: STS, IntelliJ IDEA, Eclipse o VS Code

Verifica:

```bash
java -version
mvn -version
git --version
```

---

## Compilar

```bash
mvn clean compile
```

Debes obtener:

```text
BUILD SUCCESS
```

---

## Ejecutar

```bash
mvn exec:java -Dexec.mainClass="edu.uees.refactor.app.Main"
```

La salida inicial esperada para el caso VIP es:

```text
Guardando reserva R-001
Correo enviado a ana@uees.edu.ec
Estado: CONFIRMADA
Total: 34.0
```

Si obtienes una salida diferente, registra la evidencia real.

---

## Estructura

```text
UEES_UCOM0310_Semana6_Lab1_Proyecto_BASE/
├── pom.xml
├── README.md
├── src/main/java/edu/uees/refactor/
│   ├── app/Main.java
│   ├── domain/EstadoReserva.java
│   ├── domain/Reserva.java
│   └── service/ServicioReservas.java
└── docs/
    ├── 01_LINEA_BASE.md
    ├── 02_MAPA_RESPONSABILIDADES.md
    ├── 03_MATRIZ_DIAGNOSTICO.md
    ├── 04_MATRIZ_RIESGO.md
    ├── 05_PRUEBAS_PROPUESTAS.md
    ├── 06_PLAN_REFACTORIZACION.md
    └── 07_REFLEXION_TECNICA.md
```

---

## Lo que debes hacer

1. Validar el entorno.
2. Compilar el proyecto.
3. Ejecutar el código sin modificarlo.
4. Registrar la salida.
5. Construir seis escenarios de línea base.
6. Identificar responsabilidades actuales.
7. Diagnosticar problemas de clases.
8. Diagnosticar problemas de datos.
9. Diagnosticar condicionales.
10. Evaluar testabilidad.
11. Completar la matriz de diagnóstico.
12. Completar la matriz de riesgo.
13. Proponer pruebas antes de modificar.
14. Priorizar el plan de refactorización.
15. Registrar el estado inicial en Git.

---

## No debes hacer todavía

- Extract Class.
- Move Method.
- Introducir Value Objects.
- Simplificar condicionales.
- Aplicar Strategy.
- Cambiar reglas funcionales.
- Implementar mocks.

Estas acciones se realizarán posteriormente, una vez construida la red de seguridad.

---

## Git

Al finalizar el diagnóstico:

```bash
git init
git add .
git commit -m "chore: registrar proyecto heredado y linea base"
```

La meta del Laboratorio 1 no es terminar con código más limpio.

La meta es terminar con evidencia suficiente para responder:

> **¿Qué hace el código, dónde están sus riesgos y qué pruebas necesito antes de cambiarlo?**
