package dk.openesdh.repo.services.audit;

import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_PATH;
import static org.junit.Assert.*;

import java.io.Serializable;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class AuditUtilsTest {

    private static final String DOC_ROOT_DIR_PATH = "/app:company_home/oe:OpenESDH/oe:cases/base:2016/base:5/base:9/base:20160509-993/base:documents/doc:sample_docx1";
    private static final String DOC_PATH = "/app:company_home/oe:OpenESDH/oe:cases/base:2016/base:5/base:9/base:20160509-993/base:documents/cm:first/cm:second/doc:sample_docx2";
    private static final String DOC_CONTENT_PATH = "/app:company_home/oe:OpenESDH/oe:cases/base:2016/base:5/base:9/base:20160509-993/base:documents/cm:first/doc:sample_docx4/doc:content_sample_docx4";
    private static final String ATT_PATH = "/app:company_home/oe:OpenESDH/oe:cases/base:2016/base:5/base:9/base:20160509-993/base:documents/cm:first/doc:sample_docx4/cm:sample_odt1.odt";

    @Test
    public void testGetLastPathElement() {
        assertArrayEquals("in root", new String[]{"doc", "sample_docx1"}, AuditUtils.getLastPathElement(getPath(DOC_ROOT_DIR_PATH)));
        assertArrayEquals("doc", new String[]{"doc", "sample_docx2"}, AuditUtils.getLastPathElement(getPath(DOC_PATH)));
        assertArrayEquals("content", new String[]{"doc", "content_sample_docx4"}, AuditUtils.getLastPathElement(getPath(DOC_CONTENT_PATH)));
        assertArrayEquals("atachment", new String[]{"cm", "sample_odt1.odt"}, AuditUtils.getLastPathElement(getPath(ATT_PATH)));
    }

    @Test
    public void testGetDocumentPath() {
        assertEquals("Root doc should not haved path", "", AuditUtils.getDocumentPath(getPath(DOC_ROOT_DIR_PATH)));
        assertEquals("Invalid path of doc folder", "first/second/", AuditUtils.getDocumentPath(getPath(DOC_PATH)));
        assertEquals("Invalid path of doc content", "first/", AuditUtils.getDocumentPath(getPath(DOC_CONTENT_PATH)));
        assertEquals("Invalid path of doc attachment", "first/sample_docx4/", AuditUtils.getDocumentPath(getPath(ATT_PATH)));
    }

    private static ImmutableMap<String, Serializable> getPath(String path) {
        return ImmutableMap.of(TRANSACTION_PATH, path);
    }

}
