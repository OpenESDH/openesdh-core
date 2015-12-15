package dk.openesdh.repo.services.authorities;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVStrategy;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.StringUtils;

public class GroupsCsvParser {

    private static final int GROUP_NAME_COLUMN = 0;
    private static final int GROUP_DISPLAY_NAME_COLUMN = 1;
    private static final int GROUP_MEMBER_OF_COLUMN = 2;
    private static final String READ = "read";
    private static final String WRITE = "write";
    private static final String CREATE = "create";

    private List<String> validCaseTypes = new ArrayList<String>();

    private List<Map<String, String>> caseTypePermissionsMap = new ArrayList<>();

    public GroupsCsvParser(List<String> validCaseTypes) {
        this.validCaseTypes = validCaseTypes;
    }

    public List<Group> parse(InputStream groupsCsv) throws IOException {
        InputStreamReader reader = new InputStreamReader(groupsCsv, Charset.forName("UTF-8"));
        CSVParser csv = new CSVParser(reader, CSVStrategy.EXCEL_STRATEGY);
        String[][] data = csv.getAllValues();
        if (data.length == 0) {
            return Collections.emptyList();
        }
        int lineInFile = 0;
        try {
            parseHeaderLine(data[0]);
            data = (String[][]) ArrayUtils.remove(data, 0);
            List<Group> groups = new ArrayList<Group>();
            for (int lineIndex = 0; lineIndex < data.length; lineIndex++) {
                lineInFile++;
                String[] line = data[lineIndex];
                if (!isEmptyLine(line)) {
                    groups.add(parseGroup(line));
                }
            }
            return groups;
        } catch (Exception ex) {
            throw new RuntimeException("Error parsing csv file on line: " + lineInFile + " " + ex.getMessage(), ex);
        }
    }

    private boolean isEmptyLine(String[] line) {
        return line == null || line.length == 0 || (line.length == 1 && line[0].trim().length() == 0);
    }

    private void parseHeaderLine(String[] headerLine) throws Exception {
        caseTypePermissionsMap.clear();
        if (headerLine.length < 4) {
            return;
        }
        for (int columnIndex = 3; columnIndex < headerLine.length; columnIndex++) {
            parseCaseType(headerLine[columnIndex]);
        }
    }

    private void parseCaseType(String caseTypeName) throws Exception {
        String caseType = caseTypeName.toLowerCase().replace("case", "").trim();
        if (!validCaseTypes.contains(caseType)) {
            throw new Exception("Error parsing csv file header. Invalid case type provided: " + caseType);
        }
        String groupPrefix = "Case" + StringUtils.capitalize(caseType);
        Map<String, String> permissionGroups = new HashMap<String, String>();
        permissionGroups.put(CREATE, groupPrefix + "Creator");
        permissionGroups.put(READ, groupPrefix + "Reader");
        permissionGroups.put(WRITE, groupPrefix + "Writer");
        caseTypePermissionsMap.add(permissionGroups);
    }

    private Group parseGroup(String[] line) throws Exception {

        if (line.length == 1 || line[GROUP_DISPLAY_NAME_COLUMN] == null
                || line[GROUP_DISPLAY_NAME_COLUMN].trim().length() == 0) {
            throw new Exception("Group Display Name not provided");
        }

        Group group = new Group(line[GROUP_NAME_COLUMN], line[GROUP_DISPLAY_NAME_COLUMN]);
        if (line.length < 3) {
            return group;
        }

        List<String> memberOfGroups = group.getMemberOfGroups();
        memberOfGroups.addAll(
                parseMemberOfGroups(line[GROUP_MEMBER_OF_COLUMN]));

        int caseTypeIndex = 0;
        for (int permissionsColIndex = GROUP_MEMBER_OF_COLUMN + 1; permissionsColIndex < line.length; permissionsColIndex++) {
            Map<String, String> caseTypePermissions = this.caseTypePermissionsMap.get(caseTypeIndex);
            List<String> permissionGroups = parsePermissions(line[permissionsColIndex], caseTypePermissions);
            memberOfGroups.addAll(permissionGroups);
            caseTypeIndex++;
        }

        return group;
    }

    private List<String> parseMemberOfGroups(String memberOfGroups) {
        if (StringUtils.isEmpty(memberOfGroups)) {
            return Collections.emptyList();
        }
        return Arrays.asList(StringUtils.delimitedListToStringArray(memberOfGroups, ";"));
    }

    private List<String> parsePermissions(String permissions, Map<String, String> caseTypePermissions) throws Exception {
        if (StringUtils.isEmpty(permissions)) {
            return Collections.emptyList();
        }
        
        List<String> permissionGroups = new ArrayList<String>();
        for(String permission : permissions.split(";")){
            String permissionGroup = caseTypePermissions.get(permission.toLowerCase().trim());
            if(StringUtils.isEmpty(permissionGroup)){
                throw new Exception("Invalid permission: " + permission);
            }
            permissionGroups.add(permissionGroup);
        }
        return permissionGroups;
    }

    static class Group {
        private String shortName;
        private String displayName;
        private List<String> memberOfGroups = new ArrayList<String>();

        public Group(String shortName, String displayName) {
            super();
            this.shortName = shortName;
            this.displayName = displayName;
        }

        public String getShortName() {
            return shortName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public List<String> getMemberOfGroups() {
            return memberOfGroups;
        }
    }
}
