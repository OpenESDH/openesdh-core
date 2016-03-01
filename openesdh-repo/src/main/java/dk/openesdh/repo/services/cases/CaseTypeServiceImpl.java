package dk.openesdh.repo.services.cases;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

@Service("CaseTypeService")
public class CaseTypeServiceImpl implements CaseTypeService {

    @Autowired
    @Qualifier("NamespaceService")
    private NamespaceService namespaceService;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Override
    public String getCaseType(NodeRef caseNodeRef) {
        return getCaseType(nodeService.getType(caseNodeRef));
    }

    @Override
    public String getCaseType(QName typeQName) {
        return namespaceService.getPrefixes(typeQName.getNamespaceURI()).stream()
                .findAny().orElseThrow(RuntimeException::new)
                + ":" + typeQName.getLocalName();
    }

    @Override
    public String getCaseTypeTitle(NodeRef caseNodeRef) {
        return getCaseTypeTitle(getCaseType(caseNodeRef));
    }

    @Override
    public String getCaseTypeTitle(QName typeQName) {
        return getCaseTypeTitle(getCaseType(typeQName));
    }

    private String getCaseTypeTitle(String caseType) {
        //case.type.simple_case.title
        //case.type.staff_case.title
        return I18NUtil.getMessage(
                String.format("case.type.%1$s.title",
                        StringUtils.replace(caseType, ":", "_")));
    }

}
