package dk.openesdh.repo.services.classification;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;

import dk.openesdh.repo.model.ClassifValue;

public interface ClassifierAdminService {

    List<? extends ClassifValue> getAdminClassifValues();

    /**
     * Create/Update
     *
     * @param classifValue
     * @param mlDisplayNames
     * @return
     */
    ClassifValue createOrUpdateClassifValue(ClassifValue classifValue) throws JSONException;

    /**
     * Delete
     *
     * @param classifValue
     */
    void deleteClassifValue(NodeRef classifValueRef);

    Class<? extends ClassifValue> getClassifValueClass();

}
