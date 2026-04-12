  /${textUtils.toKebabCase(textUtils.pluralize(model.name()))}:
    get:
      summary: Lista todos los ${textUtils.toPhrase(textUtils.pluralize(model.name()))?lower_case}
      responses:
        '200':
          description: Listado exitoso
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/${textUtils.capitalizeFirst(textUtils.singularize(model.name()))}View'
    post:
      summary: Crea un nuevo ${textUtils.toPhrase(textUtils.singularize(model.name()))?lower_case}
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/${textUtils.capitalizeFirst(textUtils.singularize(model.name()))}Form'
      responses:
        '201':
          description: Creado correctamente
  /${textUtils.toKebabCase(textUtils.pluralize(model.name()))}/{id}:
    get:
      summary: Obtiene un ${textUtils.toPhrase(textUtils.singularize(model.name()))?lower_case} por su ID
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: ${textUtils.capitalizeFirst(textUtils.singularize(model.name()))} encontrado
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/${textUtils.capitalizeFirst(textUtils.singularize(model.name()))}View'
    put:
      summary: Actualiza un ${textUtils.toPhrase(textUtils.singularize(model.name()))?lower_case} existente
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
              $ref: '#/components/schemas/${textUtils.capitalizeFirst(textUtils.singularize(model.name()))}Form'
      responses:
        '204':
          description: Actualizado con éxito
    delete:
      summary: Elimina un ${textUtils.toPhrase(textUtils.singularize(model.name()))?lower_case}
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
      responses:
        '204':
          description: Eliminado con éxito
