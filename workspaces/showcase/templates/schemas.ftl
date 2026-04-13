<#macro property_template prop propName>
  <#if prop.type() == "string">
    <#include "property_string.ftl">
  <#elseif prop.type() == "integer" || prop.type() == "number">
    <#include "property_number.ftl">
  <#elseif prop.type() == "boolean">
    <#include "property_boolean.ftl">
  <#elseif prop.type() == "date">
    <#include "property_date.ftl">
  <#else>
    <#include "property_generic.ftl">
  </#if>
</#macro>
  schemas:
<#list models as model>
    ${textUtils.capitalize(textUtils.singularize(model.name()))}View:
      title: ${textUtils.capitalize(textUtils.singularize(model.name()))} (View)
      type: object
      description: Modelo de vista para ${model.name()} (showcase).
      properties:
<#list model.properties()?keys as propName>
<#assign prop = model.properties()[propName]>
<@property_template prop=prop propName=propName />
</#list>
    ${textUtils.capitalize(textUtils.singularize(model.name()))}Form:
      title: ${textUtils.capitalize(textUtils.singularize(model.name()))} (Form)
      type: object
      description: Modelo de formulario para ${model.name()} (showcase).
      properties:
<#list model.properties()?keys as propName>
<#assign prop = model.properties()[propName]>
<#if propName != "id">
<@property_template prop=prop propName=propName />
</#if>
</#list>
</#list>
