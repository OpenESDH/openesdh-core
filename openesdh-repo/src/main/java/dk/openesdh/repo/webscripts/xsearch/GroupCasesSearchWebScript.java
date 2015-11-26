package dk.openesdh.repo.webscripts.xsearch;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;

import dk.openesdh.repo.services.xsearch.XSearchService;

@Component
@WebScript(description = "Retrieves group cases with documents", families = "Case Search")
public class GroupCasesSearchWebScript extends XSearchWebscript {

    @Override
    @Uri(value = "/api/openesdh/group/cases/search", defaultFormat = "json", method = HttpMethod.GET)
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        super.execute(req, res);
    }

    @Autowired
    @Qualifier("GroupCasesSearchService")
    public void setxSearchService(XSearchService xSearchService) {
        super.setxSearchService(xSearchService);
    }
}
