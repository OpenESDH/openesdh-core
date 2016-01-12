package dk.openesdh.repo.webscripts.authentication;

import org.alfresco.repo.web.scripts.BufferedRequest;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.AnnotationWebScriptRequest;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;

import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Performs logout by http session invalidation", families = "Authentication")
public class LogoutWebScript {

    @Uri(value = "/api/openesdh/logout", method = HttpMethod.POST, defaultFormat = WebScriptUtils.JSON)
    public void logout(WebScriptRequest req) {
        ((WebScriptServletRequest) ((BufferedRequest) ((AnnotationWebScriptRequest) req).getWebScriptRequest())
                .getNext())
                .getHttpServletRequest().getSession().invalidate();
    }
}
