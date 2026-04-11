  schemas:
<#list models as model>
    ${textUtils.capitalize(textUtils.singularize(model.name()))}View:
      title: ${textUtils.capitalize(textUtils.singularize(model.name()))} (View)
      type: object
      description: Modelo de vista para ${model.name()} (showcase).
      properties:
<#list model.properties()?keys as propName>
<#assign prop = model.properties()[propName]>
        ${propName}:
          type: ${prop.type()}
<#if prop.description()??>
          description: ${prop.description()}
</#if>
</#list>
    ${textUtils.capitalize(textUtils.singularize(model.name()))}Form:
      title: ${textUtils.capitalize(textUtils.singularize(model.name()))} (Form)
      type: object
      description: Modelo de formulario para ${model.name()} (showcase).
      properties:
<#list model.properties()?keys as propName>
<#assign prop = model.properties()[propName]>
<#if propName != "id">
        ${propName}:
          type: ${prop.type()}
<#if prop.description()??>
          description: ${prop.description()}
</#if>
</#if>
</#list>
</#list>
