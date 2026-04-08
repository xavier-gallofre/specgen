  schemas:
<#list models as model>
    ${model.name()}:
      type: object
      properties:
<#list model.properties()?keys as propName>
<#assign prop = model.properties()[propName]>
        ${propName}:
          type: ${prop.type()}
<#if prop.description()??>
          description: ${prop.description()}
</#if>
<#if prop.maxLength()??>
          maxLength: ${prop.maxLength()}
</#if>
</#list>
</#list>
