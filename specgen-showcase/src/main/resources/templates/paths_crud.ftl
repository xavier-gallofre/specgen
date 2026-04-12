  /${textUtils.toKebabCase(textUtils.pluralize(model.name()))}:
    get:
      summary: Lista todos los ${textUtils.toPhrase(textUtils.pluralize(model.name()))?lower_case} (showcase)
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/${textUtils.capitalize(textUtils.singularize(model.name()))}View'
    post:
      summary: Crea un nuevo ${textUtils.toPhrase(textUtils.singularize(model.name()))?lower_case} (showcase)
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/${textUtils.capitalize(textUtils.singularize(model.name()))}Form'
      responses:
        '201':
          description: Creado
  /${textUtils.toKebabCase(textUtils.pluralize(model.name()))}/{id}:
    get:
      summary: Obtiene un ${textUtils.toPhrase(textUtils.singularize(model.name()))?lower_case} por su ID (showcase)
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Encontrado
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/${textUtils.capitalize(textUtils.singularize(model.name()))}View'
    put:
      summary: Actualiza un ${textUtils.toPhrase(textUtils.singularize(model.name()))?lower_case} existente (showcase)
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
              $ref: '#/components/schemas/${textUtils.capitalize(textUtils.singularize(model.name()))}Form'
      responses:
        '204':
          description: Actualizado
    delete:
      summary: Elimina un ${textUtils.toPhrase(textUtils.singularize(model.name()))?lower_case} (showcase)
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
      responses:
        '204':
          description: Eliminado
