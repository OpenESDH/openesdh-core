package dk.openesdh.repo.model;

/**
 * Created by syastrov on 9/2/15.
 */
public class DocumentStatus {
    public static final String DRAFT = "draft";
    public static final String FINAL = "final";

    public static boolean isValidTransition(String before,
                                            String after) {
        if (before == null || before.equals(after)) {
            return true;
        }
        // Currently, all transitions are valid.
        switch (before) {
            case DRAFT:
                return after.equals(FINAL);
            case FINAL:
                return after.equals(DRAFT);
            default:
                return false;
        }
    }
}