package dk.openesdh.repo.webscripts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.classification.sync.kle.KLEClassificationSynchronizer;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(families = { "Classification" }, description = "Starts KLE synchronization", defaultFormat = "json")
public class KLEClassificationSynchronizerWebScript {

    @Autowired
    @Qualifier("kleClassificationSynchronizer")
    private KLEClassificationSynchronizer kleSynchronizer;

    @Uri(value = "/kle", method = HttpMethod.GET)
    public Resolution doIt() {
        kleSynchronizer.synchronize();
        return WebScriptUtils.jsonResolution("done");
    }
}
