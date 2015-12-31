package dk.openesdh.repo.services.parameters;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import dk.openesdh.repo.model.OEParameter;

public interface OEParametersService {

    /**
     * retrieves ALL available parameters. If parameter is not saved, than default value will be returned;
     *
     * @return
     */
    public List<OEParameter> getOEParameters();

    /**
     * get OpenE parameter by name
     *
     * @param name
     * @return
     */
    public OEParameter getOEParameter(String name);

    /**
     * create or update OpenE parameter
     *
     * @param nodeRef
     * @param name
     * @param value
     */
    public void saveOEParameter(NodeRef nodeRef, String name, Object value);
}
