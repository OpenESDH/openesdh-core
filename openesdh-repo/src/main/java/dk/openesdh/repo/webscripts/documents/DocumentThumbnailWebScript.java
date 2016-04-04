package dk.openesdh.repo.webscripts.documents;

import java.io.IOException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;

import dk.openesdh.repo.webscripts.WebScriptParams;

@Component
@WebScript(description = "Generates document thumbnail and sends it back as response", families = "Case Documents")
public class DocumentThumbnailWebScript {
    
    @Autowired
    @Qualifier("ContentService")
    private ContentService contentService;

    @Uri(value = "/api/openesdh/case/document/{storeType}/{storeId}/{id}/thumbnail", method = HttpMethod.GET)
    public void generateThumbnail(
            @UriVariable(WebScriptParams.STORE_TYPE) String storeType,
            @UriVariable(WebScriptParams.STORE_ID) String storeId,
            @UriVariable(WebScriptParams.ID) String id,
            WebScriptResponse res) throws ContentIOException, IOException{

        NodeRef nodeRef = new NodeRef(storeType, storeId, id);
        TransformationOptions options = new TransformationOptions();
        options.setSourceNodeRef(nodeRef);
        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        ContentWriter writer = contentService.getTempWriter();
        writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
        contentService.transform(reader, writer, options);
        res.setContentType(MimetypeMap.MIMETYPE_PDF);
        writer.getReader().getContent(res.getOutputStream());
    }
    
}
