openapi: 3.0.3
info:
  title: ${.vars['api.title']!'API de Showcase'}
  version: ${.vars['api.version']!'1.0.0'}
  description: Generada automáticamente mediante el módulo showcase.

paths:
<#list models as model>
  <#if model.generate()?seq_contains("CRUD")>
<#include "paths_crud.ftl">
  </#if>
</#list>

components:
<#include "schemas.ftl">
