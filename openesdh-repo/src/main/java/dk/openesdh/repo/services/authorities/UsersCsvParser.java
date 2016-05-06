package dk.openesdh.repo.services.authorities;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVStrategy;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import dk.openesdh.repo.exceptions.DomainException;

public class UsersCsvParser {

    private static final QName[] COLUMNS = new QName[]{
        ContentModel.PROP_USERNAME, ContentModel.PROP_FIRSTNAME,
        ContentModel.PROP_LASTNAME, ContentModel.PROP_EMAIL,
        null,
        ContentModel.PROP_PASSWORD, ContentModel.PROP_ORGANIZATION,
        ContentModel.PROP_JOBTITLE, ContentModel.PROP_LOCATION,
        ContentModel.PROP_TELEPHONE, ContentModel.PROP_MOBILE,
        ContentModel.PROP_SKYPE, ContentModel.PROP_INSTANTMSG,
        ContentModel.PROP_GOOGLEUSERNAME,
        ContentModel.PROP_COMPANYADDRESS1, ContentModel.PROP_COMPANYADDRESS2,
        ContentModel.PROP_COMPANYADDRESS3, ContentModel.PROP_COMPANYPOSTCODE,
        ContentModel.PROP_COMPANYTELEPHONE, ContentModel.PROP_COMPANYFAX,
        ContentModel.PROP_COMPANYEMAIL, ContentModel.ASSOC_MEMBER
    };

    private static final String ERROR_INVALID_CSV_FILE = "ERROR.INVALID_CSV_FILE";

    public List<User> parse(InputStream usersCsv) {
        try {
            InputStreamReader reader = new InputStreamReader(usersCsv, Charset.forName("UTF-8"));
            CSVParser csv = new CSVParser(reader, CSVStrategy.EXCEL_STRATEGY);
            String[][] data = csv.getAllValues();
            List<User> users = new ArrayList<>();
            for (int lineNumber = 0; lineNumber < data.length; lineNumber++) {
                String[] line = data[lineNumber];
                if (isEmptyLine(line) || isHeader(line)) {
                    continue;
                }
                users.add(parseUser(line, lineNumber));
            }
            return users;
        } catch (IOException ex) {
            throw new DomainException(ERROR_INVALID_CSV_FILE, ex);
        }
    }

    private boolean isEmptyLine(String[] line) {
        return line == null || line.length == 0 || (line.length == 1 && line[0].trim().length() == 0);
    }

    private boolean isHeader(String[] line) {
        return "username".equalsIgnoreCase(line[0]) || "user name".equalsIgnoreCase(line[0]);
    }

    private User parseUser(String[] line, int lineNumber) {
        Map<QName, String> userProps = new HashMap<>();
        boolean required = true;
        for (int i = 0; i < COLUMNS.length; i++) {
            if (COLUMNS[i] == null) {
                required = false;
                continue;
            }

            String value = null;
            if (line.length > i) {
                value = line[i];
            }
            if (required && StringUtils.isEmpty(value)) {
                try {
                    JSONObject errorData = new JSONObject();
                    errorData.put("columnName", COLUMNS[i].getLocalName());
                    errorData.put("columnNumber", i + 1);
                    errorData.put("line", lineNumber + 1);
                    throw new DomainException("USER.ERRORS.INVALID_CSV_COLUMN", errorData);
                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                userProps.put(COLUMNS[i], value);
            }
        }

        // If no password was given, use their surname
        if (!userProps.containsKey(ContentModel.PROP_PASSWORD)) {
            userProps.put(ContentModel.PROP_PASSWORD, userProps.get(ContentModel.PROP_LASTNAME));
        }

        User user = new User(userProps);
        if (userProps.containsKey(ContentModel.ASSOC_MEMBER)) {
            String groups = userProps.get(ContentModel.ASSOC_MEMBER);
            userProps.remove(ContentModel.ASSOC_MEMBER);
            if (!StringUtils.isEmpty(groups)) {
                user.getGroups().addAll(Arrays.asList(
                        StringUtils.delimitedListToStringArray(groups, ";")));
            }
        }

        return user;
    }

    public static class User {

        private final Map<QName, String> properties;
        private final List<String> groups = new ArrayList<>();

        public User(Map<QName, String> properties) {
            this.properties = properties;
        }

        public Map<QName, String> getProperties() {
            return properties;
        }

        public List<String> getGroups() {
            return groups;
        }
    }
}
