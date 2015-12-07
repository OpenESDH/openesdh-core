package dk.openesdh.repo.services.documents;

import java.io.InputStream;
import java.util.Optional;
import org.alfresco.service.cmr.repository.NodeRef;

public interface DocumentPDFService {

    public Optional<InputStream> getDocumentPdfThumbnailStream(NodeRef documentNodeRef);
}
