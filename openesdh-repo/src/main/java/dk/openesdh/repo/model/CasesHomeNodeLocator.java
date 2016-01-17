package dk.openesdh.repo.model;

import dk.openesdh.repo.services.cases.CaseService;
import org.alfresco.repo.nodelocator.AbstractNodeLocator;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.Serializable;
import java.util.Map;

/**
 * Locates the Cases Home (Root folder) {@link NodeRef}.
 *
 * @author Lanre Abiwon
 */
public class CasesHomeNodeLocator extends AbstractNodeLocator {
    public static final String NAME = "casesHome";

    private CaseService caseService;

    /**
     * {@inheritDoc}
     */
    public NodeRef getNode(NodeRef source, Map<String, Serializable> params) {
        return caseService.getCasesRootNodeRef();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * @param caseService
     */
    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }
}