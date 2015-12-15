package dk.openesdh.repo.services.authorities;

import java.io.IOException;
import java.io.InputStream;

import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;

public interface GroupsService {

    public void uploadGroupsCSV(InputStream groupsCsv) throws IOException;

    boolean typeEqualsOpenEType(String type, String authorityName) throws InvalidNodeRefException;

    boolean hasAspectTypeOPENE(String authorityName);

    void addAspectTypeOPENE(String fullName) throws InvalidNodeRefException, InvalidAspectException;
}
