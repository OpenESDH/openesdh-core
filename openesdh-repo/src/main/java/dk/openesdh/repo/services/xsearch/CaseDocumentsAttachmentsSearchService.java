package dk.openesdh.repo.services.xsearch;

import java.util.Map;

/**
 * Created by lanre on 26/02/2015.
 */
public interface CaseDocumentsAttachmentsSearchService extends XSearchService {

    public XResultSet getAttachments(Map<String, String> params);

}
