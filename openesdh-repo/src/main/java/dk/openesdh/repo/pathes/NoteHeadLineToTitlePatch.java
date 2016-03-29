package dk.openesdh.repo.pathes;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.openesdh.repo.model.OpenESDHModel;

public class NoteHeadLineToTitlePatch extends AbstractPatch {

    private final Logger logger = LoggerFactory.getLogger(NoteHeadLineToTitlePatch.class);
    private static final String PATCH_ID = "patch.noteHeadlineToTitle";

    @Override
    protected String applyInternal() throws Exception {
        logger.info("Starting execution of patch: " + PATCH_ID);

        long maxNodeId = patchDAO.getMaxAdmNodeID();
        Pair<Long, QName> typeNoteQNameId = qnameDAO.getQName(OpenESDHModel.TYPE_NOTE_NOTE);
        if (typeNoteQNameId == null) {
            return "Notes are not initiliazed yet, so migration is not necessary";
        }
        Long qnameId = typeNoteQNameId.getFirst();

        List<Long> allNotes = patchDAO.getNodesByTypeQNameId(qnameId, 0L, maxNodeId + 1);

        allNotes.forEach(id -> {
            Map<QName, Serializable> nodeProperties = nodeDAO.getNodeProperties(id);
            if (nodeProperties.containsKey(OLD_PROP_HEADLINE)) {
                String headlineToTitle = (String) nodeProperties.get(OLD_PROP_HEADLINE);
                nodeProperties.remove(OLD_PROP_HEADLINE);
                if (StringUtils.isNotEmpty(headlineToTitle)) {
                    nodeProperties.putIfAbsent(ContentModel.PROP_TITLE, headlineToTitle);
                    nodeDAO.setNodeProperties(id, nodeProperties);
                    logger.debug("Notes headline moved to title: ", headlineToTitle);
                }
            }
        });

        String success = "Finished execution of patch: " + PATCH_ID;
        logger.info(success);
        return success;
    }

    QName OLD_PROP_HEADLINE = QName.createQName(OpenESDHModel.NOTE_URI, "headline");

    private PatchDAO patchDAO;
    private QNameDAO qnameDAO;
    private NodeDAO nodeDAO;

    public void setPatchDAO(PatchDAO patchDAO) {
        this.patchDAO = patchDAO;
    }

    public void setQnameDAO(QNameDAO qnameDAO) {
        this.qnameDAO = qnameDAO;
    }

    public void setNodeDAO(NodeDAO nodeDAO) {
        this.nodeDAO = nodeDAO;
    }
}
