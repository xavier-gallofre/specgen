  /${model.name()?lower_case}s/selector:
    get:
      summary: Obtiene una lista simplificada de ${model.name()?lower_case}s para selectores/combos
      responses:
        '200':
          description: Listado simplificado exitoso
          content:
            application/json:
              schema:
                type: array
                items:
                  type: object
                  properties:
                    id:
                      type: string
                      description: Identificador único
                    label:
                      type: string
                      description: Texto descriptivo para mostrar en el selector
