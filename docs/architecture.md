# Arquitectura del Generador OpenAPI Core

## El Pseudo-formato

La base fundamental es un archivo (YAML o JSON) que define los modelos de negocio. 
Cada modelo tiene:

1. **Nombre**: El identificador del objeto (ej: `User`, `Product`).
2. **Propiedades**: Atributos con tipo, descripción y validaciones (maxLength, etc.).
3. **Generadores**: Una lista de estrategias (ej: `CRUD`, `SELECTOR`).

## Flujo de Generación

1. **Lectura**: El núcleo lee el pseudo-formato y lo convierte en un objeto `OpenApiSpec` (Java Record).
2. **Transformación**: Basándose en la lista de generadores, el núcleo expande estos registros en contextos de datos compatibles con OpenAPI (Rutas, Esquemas, Parámetros).
3. **Renderizado**: El motor **FreeMarker** utiliza plantillas para producir el archivo final YAML de la especificación OpenAPI 3.0+.

## Reutilización

El núcleo está desacoplado de la entrada de datos. Esto permite que:

* Un **CLI** pase archivos de texto al core.
* Un **Módulo de DB** lea esquemas de base de datos y llame al core con el modelo ya construido.

## Plantillas

Las plantillas se encuentran en `src/main/resources/templates` y están organizadas por fragmentos:
* `schemas.ftl`
* `paths.ftl`
* `main.ftl`
