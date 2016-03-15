package dk.openesdh.repo.webscripts.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.auth.CapabilitiesService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Returns capabilities for current authentication", families = "Authentication")
public class CapabilitiesWebScript {

    @Autowired
    @Qualifier(CapabilitiesService.BEAN_ID)
    private CapabilitiesService capabilitiesService;

    @Uri(value = "/api/openesdh/capabilities", defaultFormat = WebScriptUtils.JSON)
    public Resolution getCapabilities() {
        return WebScriptUtils.jsonResolution(capabilitiesService.getCapabilities());
    }
}
