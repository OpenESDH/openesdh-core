package dk.openesdh.repo.services.parameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OEParameter;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.system.OpenESDHFoldersService;

@Service
public class OEParametersServiceImpl implements OEParametersService {

    @Autowired
    private NodeService nodeService;
    @Autowired
    private OpenESDHFoldersService openESDHFoldersService;

    public List<OEParameter> getOEParameters() {
        Map<String, OEParameter> savedOEParamemeters = nodeService.getChildAssocs(openESDHFoldersService.getParametersRootNodeRef())
                .stream()
                .map(assocItem -> getOEParameter(assocItem.getChildRef()))
                .collect(Collectors.toMap(OEParameter::getName, item -> item));

        List<OEParameter> fullOEParamemeters = new ArrayList<>();
        for (OEParam param : OEParam.values()) {
            fullOEParamemeters.add(savedOEParamemeters.containsKey(param.name())
                    ? savedOEParamemeters.get(param.name())
                    : defaultOEParameter(param)
            );
        }
        return fullOEParamemeters;
    }

    public OEParameter getOEParameter(String name) {
        NodeRef paramNodeRef = nodeService.getChildByName(
                openESDHFoldersService.getParametersRootNodeRef(),
                ContentModel.ASSOC_CONTAINS,
                name);
        return paramNodeRef == null
                ? defaultOEParameter(OEParam.valueOf(name))
                : getOEParameter(paramNodeRef);
    }

    public void saveOEParameter(NodeRef nodeRef, String name, Object value) {
        OEParam param = OEParam.valueOf(name);
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, name);
        properties.put(param.qname, prepareValue(param.qname, value));
        if (nodeRef == null) {
            nodeService.createNode(
                    openESDHFoldersService.getParametersRootNodeRef(),
                    ContentModel.ASSOC_CONTAINS,
                    param.qname,
                    OpenESDHModel.TYPE_OE_PARAMETER,
                    properties);
        } else {
            nodeService.setProperties(nodeRef, properties);
        }
    }

    private Serializable prepareValue(QName propQName, Object value) {
        if (propQName.isMatch(OpenESDHModel.PROP_OE_PARAMETER_BOOL_VALUE)) {
            return BooleanUtils.isTrue((Boolean) value);
        }
        return (Serializable) value;
    }

    private OEParameter defaultOEParameter(OEParam param) {
        OEParameter parameter = new OEParameter();
        parameter.setName(param.name());
        parameter.setValue(param.defaultVal);
        return parameter;
    }

    private OEParameter getOEParameter(NodeRef nodeRef) {
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        OEParameter parameter = new OEParameter();
        parameter.setNodeRef(nodeRef);
        parameter.setName((String) properties.get(ContentModel.PROP_NAME));
        OEParam param = OEParam.valueOf(parameter.getName());
        parameter.setValue(properties.get(param.qname));
        return parameter;
    }
}
