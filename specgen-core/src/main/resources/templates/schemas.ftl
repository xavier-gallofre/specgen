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
    ${textUtils.capitalizeFirst(textUtils.singularize(model.name()))}View:
      type: object
      properties:
<#list model.properties()?keys as propName>
<#assign prop = model.properties()[propName]>
<@property_template prop=prop propName=propName />
</#list>
    ${textUtils.capitalizeFirst(textUtils.singularize(model.name()))}Form:
      type: object
      properties:
<#list model.properties()?keys as propName>
<#assign prop = model.properties()[propName]>
<#if propName != "id">
<@property_template prop=prop propName=propName />
</#if>
</#list>
</#list>
