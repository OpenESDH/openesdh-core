package dk.openesdh.repo.webscripts.classification;

import java.io.IOException;

import org.springframework.extensions.webscripts.WebScriptRequest;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Attribute;

import dk.openesdh.repo.model.ClassifValue;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

public class ClassificatorValuesWebScript {

    @Attribute("classifValue")
    public ClassifValue getClassifValue(WebScriptRequest req) throws IOException {
        if (!WebScriptUtils.isContentTypeJson(req)) {
            return null;
        }
        return (ClassifValue) WebScriptUtils.readJson(ClassifValue.class, req);
    }

}
