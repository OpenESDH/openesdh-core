package dk.openesdh.repo.webscripts.xsearch;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Map;

public class FileTemplateSearch extends XSearchWebscript {
    protected static Logger log = Logger.getLogger(FileTemplateSearch.class);

    @Override
    protected JSONObject nodeToJSON(NodeRef nodeRef) throws JSONException {
        JSONObject json = new JSONObject();
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        json.put("name", properties.get(ContentModel.PROP_NAME));
        json.put("title", properties.getOrDefault(ContentModel.PROP_TITLE, ""));
        json.put("description", properties.getOrDefault(ContentModel.PROP_DESCRIPTION, ""));
        json.put("nodeRef", nodeRef.toString());
        return json;
    }
}
