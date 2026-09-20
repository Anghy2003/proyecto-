# Reflexión técnica final

**¿Qué necesito comprender y evidenciar antes de tocar una clase heredada que
aparentemente «funciona»?**

Lo primero que aprendí en este laboratorio es que «funciona» no es una
afirmación verificable hasta que existe una línea base. Ejecuté el proyecto sin
modificarlo y registré nueve escenarios: los seis obligatorios más tres que
agregué al notar comportamientos que nadie había declarado. Gracias a ellos
descubrí que una reserva de tipo `"vip"` en minúscula cobra 40 en lugar de 34, y
que un tipo inexistente como `"PREMIUM"` se acepta y se confirma. Eso no lo
sabía por leer el código: lo supe por ejecutarlo.

El problema de diseño que genera mayor riesgo es la mezcla de dominio e
infraestructura dentro de `procesar()`. La persistencia y la notificación están
escritas como dos `System.out.println` en las líneas 43 a 49, sin ningún punto
de sustitución. Mientras eso siga así, no puedo probar los efectos sin capturar
la salida estándar ni conectar una base de datos real sin editar la lógica de
negocio.

El problema que parece fácil pero puede alterar el comportamiento es introducir
`Correo` o `PeriodoReserva`. Suena a mejora limpia, pero si la validación se
muda al constructor, un periodo inválido dejaría de retornar 0 y pasaría a
lanzar una excepción. Eso ya no es refactorizar: es cambiar el contrato. La
diferencia entre ambas cosas es exactamente esa: refactorizar reorganiza la
estructura interna dejando intacto lo observable; un cambio funcional modifica
lo que el sistema promete y por eso debe declararse y autorizarse.

Las pruebas indispensables antes de tocar nada son las seis de caracterización,
sobre todo las de límite: 2 h procesa y 1 h no. Esa frontera inclusiva no está
documentada en ninguna parte y es lo más fácil de romper al extraer la
constante.

La responsabilidad que movería primero es la notificación, porque es un bloque
contiguo, sin lógica y de riesgo bajo. Para defender la decisión usaría la
evidencia de LB-01 y LB-02: retorno, estado y mensajes quedan registrados antes
del cambio y deben reproducirse idénticos después.

## Checklist

- [x] Proyecto base compila (`BUILD SUCCESS`) y ejecuta.
- [x] Seis escenarios de línea base (más tres observaciones complementarias).
- [x] Mapa de responsabilidades.
- [x] Mínimo cinco problemas diagnosticados (registré ocho).
- [x] Matriz de riesgo.
- [x] Pruebas propuestas para cada cambio crítico.
- [x] Plan priorizado.
- [x] Commit Git del estado inicial.
- [x] Reflexión técnica.
