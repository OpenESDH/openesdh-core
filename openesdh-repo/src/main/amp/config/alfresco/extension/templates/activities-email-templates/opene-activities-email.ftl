<html>
   <head>
      <style type="text/css"><!--
      body
      {
         font-family: Arial, sans-serif;
         font-size: 14px;
         color: #4c4c4c;
      }
      
      a, a:visited
      {
         color: #0072cf;
      }
      
      .activity a
      {
         text-decoration: none;
      }
      
      .activity a:hover
      {
         text-decoration: underline;
      }
      -->
      .activity
      {
        margin: 15px;
      }
      </style>
   </head>
   
   <body bgcolor="#dddddd">
      <table width="100%" cellpadding="20" cellspacing="0" border="0" bgcolor="#dddddd">
         <tr>
            <td width="100%" align="center">
               <table width="70%" cellpadding="0" cellspacing="0" bgcolor="white" style="background-color: white; border: 1px solid #aaaaaa;">
                  <tr>
                     <td width="100%">
                        <table width="100%" cellpadding="0" cellspacing="0" border="0">
                           <tr>
                              <td style="padding: 20px 30px 0px;">
                                 <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                    <tr>
                                       <td>
                                          <div style="font-size: 22px; padding-bottom: 4px;">
                                             ${message('recent.activities')}
                                          </div>
                                          <div style="font-size: 13px; margin-top: 10px;">
                                             ${date?datetime?string("dd-MM-yyyy hh:mm")}
                                          </div>
                                          <div style="font-size: 14px; margin: 18px 0px 24px 0px; padding-top: 18px; border-top: 1px solid #aaaaaa;">
                                             <#if activities?exists && activities?size &gt; 0>
                                                 <#list activities as activityObj>
                                                    <#assign activity=activityObj.activitySummary>
                                                    <#assign activityType=activityObj.activityType>
                                                    <#switch activityObj.activityType>
                                                       <#case "dk.openesdh.case-update">
                                                          <#assign detail=message(activityType?html, activity.modifierDisplayName, activity.caseTitle, activity.caseId)!"">                                                          
                                                          <#break>
                                                       <#case "dk.openesdh.case.member-add">
                                                          <#assign detail=message(activityType?html, activity.modifierDisplayName, activity.member, activity.role, activity.caseTitle, activity.caseId)!"">                                                          
                                                          <#break>
                                                       <#case "dk.openesdh.case.member-remove">
                                                          <#assign detail=message(activityType?html, activity.modifierDisplayName, activity.member, activity.role, activity.caseTitle, activity.caseId)!"">                                                          
                                                          <#break>
                                                       <#case "dk.openesdh.case.workflow-start">
                                                          <#assign detail=message(activityType?html, activity.modifierDisplayName, activity.workflowDescription, activity.caseTitle, activity.caseId)!"">                                                          
                                                          <#break>
                                                       <#case "dk.openesdh.case.workflow-cancel">
                                                          <#assign detail=message(activityType?html, activity.modifierDisplayName, activity.workflowDescription, activity.caseTitle, activity.caseId)!"">                                                          
                                                          <#break>
                                                       <#case "dk.openesdh.case.workflow.task-end">
                                                          <#assign detail=message(activityType?html, activity.modifierDisplayName, activity.workflowDescription, activity.caseTitle, activity.caseId)!"">                                                          
                                                          <#break>
                                                       <#case "dk.openesdh.case.workflow.task-approve">
                                                          <#assign detail=message(activityType?html, activity.modifierDisplayName, activity.workflowDescription, activity.caseTitle, activity.caseId)!"">                                                          
                                                          <#break>
                                                       <#case "dk.openesdh.case.workflow.task-reject">
                                                          <#assign detail=message(activityType?html, activity.modifierDisplayName, activity.workflowDescription, activity.caseTitle, activity.caseId)!"">                                                          
                                                          <#break>
                                                       <#case "dk.openesdh.case.document-upload">
                                                          <#assign detail=message(activityType?html, activity.modifierDisplayName, activity.docTitle, activity.caseTitle, activity.caseId)!"">                                                          
                                                          <#break>
                                                       <#case "dk.openesdh.case.document.new.version-upload">
                                                          <#assign detail=message(activityType?html, activity.modifierDisplayName, activity.docTitle, activity.caseTitle, activity.caseId)!"">                                                          
                                                          <#break>
                                                       <#case "dk.openesdh.case.document.attachment-upload">
                                                          <#assign detail=message(activityType?html, activity.modifierDisplayName, activity.attachmentTitle, activity.docTitle, activity.caseTitle, activity.caseId)!"">                                                          
                                                          <#break>
                                                       <#case "dk.openesdh.case.document.attachment.new.version-upload">
                                                          <#assign detail=message(activityType?html, activity.modifierDisplayName, activity.attachmentTitle, activity.docTitle, activity.caseTitle, activity.caseId)!"">                                                          
                                                          <#break>
                                                       <#default>
                                                    </#switch>
                                                    <div class="activity">
                                                       ${detail}
                                                    </div>
                                                 </#list>
                                             </#if>
                                          </div>
                                       </td>
                                    </tr>
                                 </table>
                              </td>
                           </tr>
                        </table>
                     </td>
                  </tr>
               </table>
            </td>
         </tr>
      </table>
   </body>
</html>
