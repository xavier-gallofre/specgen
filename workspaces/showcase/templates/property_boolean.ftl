        ${propName}:
          type: boolean
<#if prop.description()??>
          description: ${prop.description()}
</#if>
<#if prop.required()?? && prop.required()>
          nullable: false
<#else>
          nullable: true
</#if>
