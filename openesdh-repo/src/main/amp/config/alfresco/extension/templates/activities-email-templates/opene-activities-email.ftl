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
                                                    <div class="activity">
                                                       ${activitymessage(activityObj)}
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
