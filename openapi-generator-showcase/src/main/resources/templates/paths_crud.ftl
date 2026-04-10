  /${model.name()?lower_case}:
    get:
      summary: Operación de lectura (showcase)
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/${model.name()}'
    post:
      summary: Operación de creación (showcase)
      responses:
        '201':
          description: Creado
