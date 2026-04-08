openapi: 3.0.3
info:
  title: ${info!"API Generada"}
  version: 1.0.0

paths:
<#list models as model>
  <#if model.generate()?seq_contains("CRUD")>
<#include "paths_crud.ftl">
  </#if>
  <#if model.generate()?seq_contains("SELECTOR")>
<#include "paths_selector.ftl">
  </#if>
</#list>

components:
<#include "schemas.ftl">
