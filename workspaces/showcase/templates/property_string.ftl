        ${propName}:
          type: string
<#if prop.description()??>
          description: ${prop.description()}
</#if>
<#if prop.maxLength()??>
          maxLength: ${prop.maxLength()}
</#if>
<#if prop.required()?? && prop.required()>
          nullable: false
<#else>
          nullable: true
</#if>
