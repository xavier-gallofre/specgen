# Generador SELECTOR

El generador `SELECTOR` crea un endpoint diseñado para ser consumido por componentes de interfaz de usuario como selectores, comboboxes o drop-downs, donde solo se requiere información mínima del objeto.

## Comportamiento

Cuando se incluye `"SELECTOR"` en la lista de generadores de un modelo, el motor producirá el siguiente endpoint:

1. **`GET /{model}s/selector`**: Devuelve una lista de objetos simplificados con una estructura fija orientada a UI.

## Estructura de Salida

A diferencia del CRUD, el SELECTOR no devuelve el esquema completo del objeto, sino una versión reducida:

```yaml
type: array
items:
  type: object
  properties:
    id:
      type: string
    label:
      type: string
```

## Ejemplo de Configuración

En el pseudo-formato (YAML):

```yaml
models:
  - name: Product
    properties:
      id: 
        type: integer
      name:
        type: string
    generate:
      - SELECTOR
```

## Resultado OpenAPI (Fragmento)

```yaml
paths:
  /products/selector:
    get:
      summary: Obtiene una lista simplificada de products para selectores/combos
      ...
```

## Implementación Técnica

* **Plantilla**: `src/main/resources/templates/paths_selector.ftl`
* **Test de referencia**: `src/test/java/com/openapi/generator/core/SelectorGenerationTest.java`
