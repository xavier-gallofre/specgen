paths:
<#if model.generate()?seq_contains("CRUD")>
<#include "paths_crud.ftl">
</#if>
<#if model.generate()?seq_contains("SELECTOR")>
<#include "paths_selector.ftl">
</#if>

components:
  schemas:
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
