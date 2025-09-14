# Proyecto Final – Bases de Datos II

**Título:** Sistema Académico con Extensión Analítica y NoSQL

---

## Índice

1. [Contexto](#contexto)
2. [Objetivo](#objetivo)
3. [Alcance](#alcance)
4. [Requisitos funcionales (Lógica de negocio)](#requisitos-funcionales-l%C3%B3gica-de-negocio)
   - Matrícula y carga académica
   - Prerrequisitos académicos
   - Calificaciones y evaluación
   - Riesgo académico
   - Gestión de docentes
   - Seguridad y auditoría
   - Integración Relacional–NoSQL
5. [Reportes SQL requeridos](#reportes-sql-requeridos)
6. [Requisitos técnicos](#requisitos-t%C3%A9cnicos)
7. [Manejo físico y rendimiento](#manejo-f%C3%ADsico-y-rendimiento)
8. [Seguridad y calidad del dato](#seguridad-y-calidad-del-dato)
9. [Extensión NoSQL](#extensi%C3%B3n-nosql)
10. [Entregables y criterios de aceptación](#entregables-y-criterios-de-aceptaci%C3%B3n)
11. [Anexos y notas](#anexos-y-notas)

---

## Contexto

La Universidad del Quindío administra de manera integral la información de sus programas académicos, estudiantes, docentes, asignaturas y matrículas. Se requiere diseñar un sistema de base de datos que permita registrar procesos transaccionales y ejecutar análisis avanzados, garantizando seguridad, optimización del almacenamiento y una extensión NoSQL para retroalimentación estudiantil.


## Objetivo

Diseñar e implementar un modelo físico relacional (Oracle) y una extensión NoSQL (colección documental) que permitan:

- Gestión completa de matrículas, calificaciones y avance académico.
- Validación de reglas de negocio (prerrequisitos, riesgo, ventanas de calendario, cupos y choques de horario).
- Soporte para generación de reportes analíticos avanzados.
- Registro de retroalimentación estudiantil en NoSQL con enlace al contexto relacional.


## Alcance

El proyecto incluye diseño conceptual, lógico y físico de la BD relacional, scripts DDL para Oracle, procedimientos y triggers clave (PL/SQL), carga de datos simulados, diseño de colección NoSQL (MongoDB u otra), y consultas/consultas analíticas requeridas por la especificación.


## Requisitos funcionales (Lógica de negocio)

### 1. Matrícula y Carga Académica

- Validación de créditos según riesgo académico:
  - Sin riesgo: máximo 21 créditos.
  - Riesgo 1 o 3: máximo 8 créditos.
  - Riesgo 2: máximo 12 créditos.
  - Riesgo 4: máximo 16 créditos.
  - Artefacto sugerido: trigger BEFORE INSERT/UPDATE sobre inscripción/matrícula.

- Inscripción automática de primer semestre:
  - Al crear matrícula para un estudiante de primer semestre, inscribir automáticamente las asignaturas iniciales del plan.
  - Artefacto: procedimiento invocado al crear la matrícula.

- Registro obligatorio de asignaturas perdidas:
  - Las materias reprobadas deben ser incluidas en la matrícula siguiente. Si exceden topes de crédito, priorizar las de menor semestre.
  - Artefacto: trigger en matrícula que invoque procedimiento de priorización.

- Validación de choques de horario y capacidad:
  - Verificar solapamientos entre horarios y que los grupos no excedan capacidad.
  - Artefacto: procedimiento de validación invocado por trigger.

- Ventanas de calendario académico:
  - Inscripciones, modificaciones y retiros solo dentro de fechas oficiales.
  - Artefacto: función de validación + triggers.

- Límite en cancelación de materias:
  - No se puede cancelar si es de primer semestre, si se está repitiendo o si ya la canceló 2 o más veces.
  - Artefacto: función de conteo + trigger.


### 2. Prerrequisitos Académicos

- Verificación de prerrequisitos:
  - Solo se puede inscribir una asignatura si se aprobaron sus prerrequisitos.
  - Artefacto: función + trigger que valide antes de insertar inscripción.

- Trabajo de fin de carrera (TFC):
  - Solo inscribible si el estudiante aprobó ≥80% de créditos del programa y cuenta con director asignado.
  - Artefacto: función + trigger.


### 3. Calificaciones y Evaluación

- Regla de evaluación con suma 100%:
  - Ítems de evaluación por grupo deben sumar exactamente 100%.
  - Artefacto: trigger al insertar/actualizar ítems de evaluación.

- Registro de notas parciales válido y oportuno:
  - Aceptar solo notas de ítems definidos, en rango permitido y antes de fecha de cierre.
  - Artefacto: trigger sobre inserción/actualización de notas.

- Cálculo automático de nota definitiva:
  - La nota final se calcula como suma ponderada de parciales y se almacena automáticamente.
  - Artefacto: procedimiento invocado tras consolidar ítems/guardar notas.

- Actualización de historial académico:
  - Al consolidar notas, actualizar créditos aprobados y promedio acumulado.
  - Artefacto: procedimiento asociado al cierre de notas.

- Cierre de notas y bloqueo de cambios:
  - Una vez cerradas las notas, bloquear modificaciones salvo reapertura autorizada.
  - Artefacto: triggers + procedimiento de reapertura con autorización y bitácora.


### 4. Riesgo Académico

- Clasificación automática de riesgo:
  - Al cierre del semestre, recalcular nivel de riesgo según promedio, materias perdidas y repeticiones.
  - Artefacto: procedimiento batch al cierre de periodo.

- Matrícula condicionada por riesgo:
  - Aplicar topes de créditos según nivel de riesgo vigente.
  - Artefacto: funciones de consulta y triggers.

- Alertas tempranas (opcional):
  - Generar notificaciones si un estudiante muestra señales de bajo rendimiento en parciales.
  - Artefacto: procedimiento de alertas y bitácora.


### 5. Gestión de Docentes

- Control de carga docente:
  - Los docentes deben dar entre 8 y 16 horas semanales.
  - Artefacto: trigger al asignar grupos que calcule la carga.

- Conflictos de horario:
  - Un docente no puede dictar simultáneamente en horarios solapados.
  - Artefacto: procedimiento de validación invocado al asignar.


### 6. Seguridad y Auditoría

- Bitácora de operaciones críticas:
  - Registrar matrícula, notas, cierres y otras operaciones con usuario, fecha y detalle.
  - Artefacto: triggers de auditoría que inserten en tabla de logs.

- Controles por rol:
  - Definir permisos (administrador, docente, estudiante) y validar en funciones/procedimientos.


### 7. Integración Relacional–NoSQL

- Enlace de contexto académico:
  - Cada comentario en NoSQL debe referenciar estudiante, asignatura (o grupo) y periodo en el sistema relacional.
  - Artefacto: procedimientos de validación y procesos ETL/consistencia.

- Anonimización y privacidad:
  - Al generar reportes agregados, anonimizar información personal.
  - Artefacto: procedimiento de anonimización para reportes.


## Reportes SQL requeridos

1. Matrícula y carga por periodo: inscritos, créditos y % de ocupación por programa, sede, asignatura y grupo.
2. Ocupación y top grupos: grupos con mayor % de ocupación por sede y periodo.
3. Intentos fallidos de matrícula: conteo por choques de horario o cupo lleno, por asignatura y grupo.
4. Rendimiento por asignatura: promedio, mínima, máxima y desviación estándar por asignatura y periodo.
5. Distribución de notas: frecuencias en rangos (0–2.9, 3.0–3.9, 4.0–5.0) por asignatura.
6. Evolución de promedio por estudiante: por periodo y variación respecto al periodo anterior.
7. Riesgo académico por periodo: conteo por niveles de riesgo (0..4) por programa y periodo.
8. Intentos por asignatura: tasa de aprobación según número de intentos.
9. Trayectoria por cohorte: % de asignaturas cursadas oportunamente vs con atraso por cohorte.
10. Mapa de prerrequisitos: árbol jerárquico de prerrequisitos.
11. Impacto de prerrequisitos: relación entre aprobación de prerrequisitos y reprobación en la asignatura objetivo.
12. Reglas de evaluación incompletas: grupos con suma de porcentajes ≠ 100%.
13. Reprobación por ítem: promedio por ítem y su relación con notas finales.
14. Avance en créditos vs plan: % de créditos aprobados respecto al plan.
15. Opinión estudiantil consolidada: número de comentarios, etiquetas y proporción positiva/negativa.
16. Cruce de opiniones y desempeño: comparación entre comentarios negativos y tasa de reprobación.
17. Asignaturas "cuello de botella": alta reprobación y frecuencia de atraso.
18. Calidad de datos: % de campos nulos o inválidos por tabla y periodo.


## Requisitos técnicos

- Base de datos relacional Oracle (DDL y PL/SQL para triggers/procedimientos).
- Consultas analíticas avanzadas: ROLLUP, CUBE, GROUPING SETS, PIVOT.
- Subconsultas correlacionadas y anidadas donde aplique.
- Se deben incluir scripts de creación, índices y ejemplos de consultas explicadas.


## Manejo físico y rendimiento

- Definir tablespaces separando datos y índices según criticidad y frecuencia de acceso.
- Proponer particionamiento (por sede o periodo académico) y justificar elección.
- Crear y justificar índices (B-tree, Bitmap, compuestos). Evaluar impacto con planes de ejecución.


## Seguridad y calidad del dato

- Definir al menos tres perfiles de usuario (administrador, docente, estudiante) y permisos.
- Aplicar validaciones, restricciones y control de duplicados para garantizar calidad.
- Implementar auditoría básica para rastrear operaciones críticas.


## Extensión NoSQL

- Diseñar colección en MongoDB (u otra) para comentarios abiertos de estudiantes con campos: id_comentario, id_estudiante (o anonimizado), id_asignatura, id_periodo, texto, etiquetas, fecha, metadatos.
- Proveer scripts de carga con al menos 50 documentos de ejemplo.
- Consultas ejemplo: palabras frecuentes (tokenización simple), clasificación por asignatura y agrupación por periodo.


## Entregables y criterios de aceptación

- Modelo físico con diagrama (PlantUML o similar) y scripts DDL para Oracle (`.sql`).
- Scripts PL/SQL para triggers y procedimientos clave (validación de créditos, prerrequisitos, cálculo de nota definitiva, recalculo de riesgo, auditoría, ventanas de calendario).
- Script de carga de datos simulados: mínimo 100 estudiantes y 10 asignaturas; en otras tablas mínimo 20 registros por tabla y 50 por tablas intermedias (N:M).
- Colección NoSQL con ≥50 documentos de comentarios estudiantiles.
- Documentación técnica que incluya:
  - Consultas avanzadas implementadas (ejemplos y explicación).
  - Descripción de índices y estrategia de tablespaces.
  - Justificación de decisiones de diseño.
- Informe final narrativo con análisis de resultados y recomendaciones.

Criterios de aceptación:
- El esquema debe crear correctamente en Oracle y permitir la carga mínima de datos.
- Procedimientos y triggers clave deben implementarse y documentarse.
- Consultas y reportes solicitados deben ejecutarse y devolver resultados coherentes con los datos simulados.


## Anexos y notas

- El TFC (Trabajo de fin de carrera) se modela como asignatura con prerrequisito adicional: haber aprobado ≥80% de créditos del programa y registro de director.
- Los niveles de riesgo y sus topes se deben parametrizar en la tabla `RiesgoAcademico` para facilitar cambios futuros.
- Recomendar desplegar Oracle en Docker para pruebas locales (imagen oficial) y usar MongoDB en Docker para la colección NoSQL.

---

> Última actualización: revisar `esquema_oracle.sql`, `modelo_academico.puml` y `DISEÑO_MODELO.md` en el repositorio para ver la implementación propuesta.
