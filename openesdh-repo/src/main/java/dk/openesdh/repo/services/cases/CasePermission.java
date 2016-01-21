package dk.openesdh.repo.services.cases;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;

import dk.openesdh.repo.utils.Utils;

public enum CasePermission {
    CREATOR("Creator"),
    OWNER("Owner"),
    READER("Reader"),
    WRITER("Writer");
    public static final String PREFIX = "Case";

    /**
     * REGEXP: \A(Case).+(Reader|Writer|Owners)\Z
     */
    public static String REGEXP_ANY = "\\A(" + PREFIX + ").+("
            + Joiner.on("|").join(values()) + ")\\Z";


    private final String name;
    private final String regexp;

    private CasePermission(String name) {
        this.name = name;
        this.regexp = "(" + PREFIX + ").*(" + name + ")";
    }

    /**
     * @param caseType excepts "case:typeName" and "typeName"
     * @return "Typename"
     */
    public String getFullName(String caseType) {
        return PREFIX + extractCaseType(caseType) + name;
    }

    private String extractCaseType(String caseType) {
        return StringUtils.capitalize(Utils.extractCaseType(caseType));
    }

    public String getRegExp(boolean fullMatch) {
        return fullMatch ? "\\A" + regexp + "\\Z" : regexp;
    }

    @Override
    public String toString() {
        return name;
    }
}
