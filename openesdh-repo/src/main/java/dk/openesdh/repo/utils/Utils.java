package dk.openesdh.repo.utils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.QName;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import dk.openesdh.repo.model.OpenESDHModel;

/**
 * Created by syastrov on 8/26/14.
 */
public class Utils {
    /**
     * Alfresco's (or Java's) query string parsing doesn't handle UTF-8
     * encoded values. We parse the query string ourselves here.
     * @param url
     * @return
     */
    public static Map<String, String> parseParameters(String url) {
        // Do our own parsing to get the query string since java.net.URI can't
        // handle some URIs
        int queryStringStart = url.indexOf('?');
        String queryString = "";
        if (queryStringStart != -1) {
            queryString = url.substring(queryStringStart+1);
        }
        List<NameValuePair> params = URLEncodedUtils.parse(queryString,
                Charset.forName("UTF-8"));
        Map<String, String> parameters = new HashMap<>();
        for (NameValuePair param : params) {
            parameters.put(param.getName(), param.getValue());
        }
        return parameters;
    }

    /**
     * Creates name for document content association.
     * 
     * @param documentName
     *            name of the document to create the content association name
     *            for
     * @return document content association name.
     */
    public static QName createDocumentContentAssociationName(String documentName) {
        return QName.createQName(OpenESDHModel.DOC_URI, "content_" + documentName);
    }
}