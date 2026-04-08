  /${model.name()?lower_case}s:
    get:
      summary: Lista todos los ${model.name()?lower_case}s
      responses:
        '200':
          description: Listado exitoso
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/${model.name()}'
    post:
      summary: Crea un nuevo ${model.name()?lower_case}
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/${model.name()}'
      responses:
        '201':
          description: Creado correctamente
  /${model.name()?lower_case}s/{id}:
    get:
      summary: Obtiene un ${model.name()?lower_case} por su ID
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: ${model.name()} encontrado
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/${model.name()}'
    put:
      summary: Actualiza un ${model.name()?lower_case} existente
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/${model.name()}'
      responses:
        '204':
          description: Actualizado con éxito
    delete:
      summary: Elimina un ${model.name()?lower_case}
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
      responses:
        '204':
          description: Eliminado con éxito
