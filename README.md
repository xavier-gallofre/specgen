# OpenAPI Generator Core

Este proyecto es el motor principal para generar especificaciones OpenAPI a partir de un pseudo-formato intermedio (YAML/JSON).

## Objetivo

Automatizar la creación de contratos de API mediante reglas y plantillas, permitiendo que la definición de modelos de negocio sea el punto central de la especificación.

## Características

* **Core Reutilizable**: Diseñado como una librería para ser integrada en CLI (PicoCLI/Micronaut) o herramientas de ingeniería inversa (DB to OpenAPI).
* **Motor de Plantillas**: Basado en **FreeMarker** para una flexibilidad total en la salida generada.
* **Soporte Java 26**: Aprovecha las últimas características del lenguaje como `Records` y `Pattern Matching`.

## Uso básico

```java
// Ejemplo conceptual
OpenApiGenerator generator = new OpenApiGenerator();
String openapiSpec = generator.generate(myPseudoSpec);
```

Para más detalles sobre la estructura interna, consulta [Architecture](docs/architecture.md).
