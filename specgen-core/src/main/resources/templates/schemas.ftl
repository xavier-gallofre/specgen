  schemas:
<#list models as model>
    ${textUtils.capitalizeFirst(textUtils.singularize(model.name()))}View:
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
    ${textUtils.capitalizeFirst(textUtils.singularize(model.name()))}Form:
      type: object
      properties:
<#list model.properties()?keys as propName>
<#assign prop = model.properties()[propName]>
<#if propName != "id">
        ${propName}:
          type: ${prop.type()}
<#if prop.description()??>
          description: ${prop.description()}
</#if>
<#if prop.maxLength()??>
          maxLength: ${prop.maxLength()}
</#if>
</#if>
</#list>
</#list>
