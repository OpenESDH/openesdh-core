package dk.openesdh.repo.utils;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;

/**
 * Created by syastrov on 8/26/14.
 */
public class Utils {

    private static final String ROLE_NAME_MESSAGE_PREFIX = "role.";

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
        Map<String, String> parameters = URLEncodedUtils
                .parse(queryString, Charset.forName("UTF-8"))
                .stream()
                .collect(
                        Collectors.groupingBy(
                                NameValuePair::getName,
                                Collectors.collectingAndThen(Collectors.toList(), Utils::paramValuesToString)));
        return parameters;
    }
    
    private static String paramValuesToString(List<NameValuePair> paramValues) {
        if (paramValues.size() == 1) {
            return paramValues.get(0).getValue();
        }
        List<String> values = paramValues.stream().map(NameValuePair::getValue).collect(Collectors.toList());
        return "[" + StringUtils.join(values, ",") + "]";
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

    public static String getRoleDisplayLabel(String role, MessageLookup messageLookup) {
        String displayRoleName = messageLookup.getMessage(ROLE_NAME_MESSAGE_PREFIX + role, I18NUtil.getLocale());
        return StringUtils.isEmpty(displayRoleName) ? role : displayRoleName;
    }

    public static JSONObject getCaseTypeJson(QName caseType, DictionaryService dictionaryService,
            CaseService caseService) throws JSONException {
        JSONObject c = getCaseTypeJson(caseType, dictionaryService);
        String type = StringUtils.substringBefore(caseType.getPrefixString(), ":");
        c.put("Type", type);
        return c;
    }

    public static JSONObject getCaseTypeJson(QName caseType, DictionaryService dictionaryService)
            throws JSONException {
        JSONObject c = new JSONObject();

        c.put("NamespaceURI", caseType.getNamespaceURI());
        c.put("Prefix", caseType.getPrefixString());
        c.put("Name", caseType.getLocalName());
        c.put("Title", dictionaryService.getType(caseType).getTitle(dictionaryService));
        return c;
    }
    
    public static String extractCaseType(String caseTypePrefixString){
        return StringUtils.replace(StringUtils.lowerCase(caseTypePrefixString), ":case", "");
    }
}