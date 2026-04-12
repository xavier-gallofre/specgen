# Generador Personalizado Spring Boot

Este módulo contiene un generador personalizado para el framework oficial [OpenAPI Generator](https://openapi-generator.tech/).

## Características

*   Extiende `SpringCodegen` (el generador oficial de Spring).
*   Utiliza plantillas Mustache personalizadas ubicadas en `src/main/resources/springboot-custom`.
*   Registrado mediante el mecanismo `ServiceLoader` (SPI) con el nombre `springboot-custom`.

## Uso como librería

Para utilizar este generador en tu código Java:

```java
CodegenConfigurator configurator = new CodegenConfigurator()
    .setGeneratorName("springboot-custom")
    .setInputSpec("api.yaml")
    .setOutputDir("generated-code")
    .setApiPackage("com.example.api")
    .setModelPackage("com.example.model");

final ClientOptInput clientOptInput = configurator.toClientOptInput();
DefaultGenerator generator = new DefaultGenerator();
generator.opts(clientOptInput).generate();
```

## Personalización de plantillas

Las plantillas se encuentran en `src/main/resources/springboot-custom/`:

*   `api.mustache`: Controla la generación de las interfaces/controladores de la API.
*   `pojo.mustache`: Controla la generación de los modelos (DTOs).

Puedes modificar estas plantillas para añadir anotaciones propias, lógica de validación o cambiar la estructura del código generado.

## Ejemplo en el proyecto

Consulta la clase `CustomSpringServerApp` en el módulo `specgen-showcase` para ver un ejemplo completo de integración.
