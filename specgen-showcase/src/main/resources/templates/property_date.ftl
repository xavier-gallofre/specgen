        ${propName}:
          type: string
          format: date
<#if prop.description()??>
          description: ${prop.description()}
</#if>
<#if prop.required()?? && prop.required()>
          nullable: false
<#else>
          nullable: true
</#if>
