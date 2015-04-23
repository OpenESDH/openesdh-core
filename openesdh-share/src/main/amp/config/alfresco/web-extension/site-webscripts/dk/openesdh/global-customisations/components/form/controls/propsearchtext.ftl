<div class="form-field">
      <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      <input id="${fieldHtmlId}" name="prop_${field.name}" tabindex="0" />
      <@formLib.renderFieldHelp field=field />
</div>
