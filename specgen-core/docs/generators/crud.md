# Generador CRUD

El generador `CRUD` (Create, Read, Update, Delete) automatiza la creación de los endpoints estándar para un modelo de negocio.

## Comportamiento

Cuando se incluye `"CRUD"` en la lista de generadores de un modelo, el motor producirá las siguientes rutas siguiendo las convenciones REST:

1. **`GET /{model}s`**: Lista todos los elementos.
2. **`POST /{model}s`**: Crea un nuevo elemento.
3. **`GET /{model}s/{id}`**: Obtiene un detalle por ID.
4. **`PUT /{model}s/{id}`**: Actualiza un elemento existente.
5. **`DELETE /{model}s/{id}`**: Elimina un elemento.

## Ejemplo de Configuración

En el pseudo-formato (YAML):

```yaml
models:
  - name: User
    properties:
      id: 
        type: integer
        description: ID único
      name:
        type: string
        maxLength: 100
    generate:
      - CRUD
```

## Resultado OpenAPI (Fragmento)

```yaml
paths:
  /users:
    get:
      summary: Lista todos los users
      ...
    post:
      ...
  /users/{id}:
    get:
      ...
```

## Implementación Técnica

* **Plantilla**: `src/main/resources/templates/paths_crud.ftl`
* **Test de referencia**: `src/test/java/com/openapi/generator/core/CrudGenerationTest.java`
