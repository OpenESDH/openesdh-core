package dk.openesdh.repo.services.audit;

import java.io.Serializable;
import java.util.Map;

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
        String path = (String) values.get("/esdh/transaction/path");
        String[] pArray = path.split("/");
        return pArray[pArray.length - 1].split(":");
    }
}
