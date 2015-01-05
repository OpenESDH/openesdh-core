<%@ page import="org.alfresco.web.site.*" %>
<%@ page import="org.springframework.extensions.surf.*" %>
<%@ page import="org.springframework.extensions.surf.site.*" %>
<%@ page import="org.springframework.extensions.surf.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.alfresco.web.site.servlet.SlingshotLoginController" %>
<%
    /**
     * Not sure if this will auto copy but just copy and replace the exploded war version.
     */
    // retrieve user name from the session
    String userid = (String) session.getAttribute(SlingshotUserFactory.SESSION_ATTRIBUTE_KEY_USER_ID);
    //retrieve the groups the user belongs to
    String groups = (String) session.getAttribute(SlingshotLoginController.SESSION_ATTRIBUTE_KEY_USER_GROUPS);
    final String extGroup = "GROUP_OPENESDH_external_users";

    //A list of groups that the user belongs to
    // test user dashboard page exists?
    RequestContext context = (RequestContext) request.getAttribute(RequestContext.ATTR_REQUEST_CONTEXT);

    //We do this so as to be able to control user dashboard presets. Also we regenerate the preset each time
    //in case we want to roll out dashboard updates
    if (groups.contains(extGroup)) {
        // always create the external dashboard for these kinds of users...
        Map<String, String> tokens = new HashMap<String, String>();
        tokens.put("userid", userid);
        FrameworkUtil.getServiceRegistry().getPresetsManager().constructPreset("external-user-dashboard", tokens);
    } else if (!context.getObjectService().hasPage("user/" + userid + "/dashboard")) {
        // no user dashboard page found! create initial dashboard for this user...
        Map<String, String> tokens = new HashMap<String, String>();
        tokens.put("userid", userid);
        FrameworkUtil.getServiceRegistry().getPresetsManager().constructPreset("user-dashboard", tokens);
    }

    // redirect to site or user dashboard as appropriate
    String siteName = request.getParameter("site");
    if (siteName == null || siteName.length() == 0) {
        // forward to user specific dashboard page
        response.sendRedirect(request.getContextPath() + "/page/user/" + URLEncoder.encode(userid) + "/dashboard");
    } else {
        // forward to site specific dashboard page
        response.sendRedirect(request.getContextPath() + "/page/site/" + URLEncoder.encode(siteName) + "/dashboard");
    }
%>