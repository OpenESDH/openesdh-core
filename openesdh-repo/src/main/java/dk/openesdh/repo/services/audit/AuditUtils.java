package dk.openesdh.repo.services.audit;

import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_PATH;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

import com.google.common.base.Strings;

public class AuditUtils {

    public static String getTitle(Map<String, Serializable> values) {
        String title;
        if (values.containsKey("/esdh/transaction/properties/title")) {
            title = (String) values.get("/esdh/transaction/properties/title");
        } else {
            title = getLastPathElement(values)[1];
            if (title.startsWith("content_")) {
                title = title.replaceFirst("content_", "");
            }
        }
        return title;
    }

    public static String[] getLastPathElement(Map<String, Serializable> values) {
        String path = (String) values.get(TRANSACTION_PATH);
        String[] pArray = path.split("/");
        return pArray[pArray.length - 1].split(":");
    }

    public static String getDocumentPath(Map<String, Serializable> values) {
        String path = (String) values.get(TRANSACTION_PATH);
        //remove begining
        path = StringUtils.substringAfter(path, "base:documents");
        //remove content if exists
        path = StringUtils.substringBeforeLast(path, "/doc:content_");
        //remove all namespaces ( "/ns:name" -> "/name" )
        path = StringUtils.replacePattern(path, "\\/\\w*:", "/");
        //remove last element, as it is current object
        path = StringUtils.substringBeforeLast(path, "/");
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        //return path/
        return StringUtils.substring(path, 1) + "/";
    }

    public static Optional<String> getLocalizedProperty(Map<QName, Serializable> properties, QName propQName) {
        return getLocalizedPropertyValue(properties.get(propQName));
    }

    public static Optional<String> getLocalizedPropertyValue(Serializable property) {
        if (property == null) {
            return Optional.empty();
        }
        if ((property instanceof Date)) {
            return Optional.of(AuditSearchService.AUDIT_DATE_FORMAT.format(property));
        }
        if (!(property instanceof Map)) {
            return Optional.ofNullable(Strings.emptyToNull(Objects.toString(property, null)));
        }

        String value = ((Map<Locale, String>) property).get(I18NUtil.getContentLocale());
        if (value == null) {
            value = ((Map<Locale, String>) property).get(Locale.ENGLISH);
        }
        return Optional.ofNullable(value);
    }
}
