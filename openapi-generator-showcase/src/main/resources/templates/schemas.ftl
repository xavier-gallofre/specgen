  schemas:
<#list models as model>
    ${model.name()}:
      type: object
      description: Modelo para ${model.name()} generado en el showcase.
      properties:
<#list model.properties()?keys as propName>
<#assign prop = model.properties()[propName]>
        ${propName}:
          type: ${prop.type()}
<#if prop.description()??>
          description: ${prop.description()}
</#if>
</#list>
</#list>
