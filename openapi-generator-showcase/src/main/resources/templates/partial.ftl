paths:
<#if model.generate()?seq_contains("CRUD")>
<#include "paths_crud.ftl">
</#if>
<#if model.generate()?seq_contains("SELECTOR")>
<#include "paths_selector.ftl">
</#if>

components:
  schemas:
    ${textUtils.capitalize(textUtils.singularize(model.name()))}View:
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
    ${textUtils.capitalize(textUtils.singularize(model.name()))}Form:
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
