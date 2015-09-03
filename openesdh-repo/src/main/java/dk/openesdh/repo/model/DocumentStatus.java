package dk.openesdh.repo.model;

/**
 * Created by syastrov on 9/2/15.
 */
public class DocumentStatus {
    public static final String EDITABLE = "editable";
    public static final String FINAL = "final";

    public static boolean isValidTransition(String before,
                                            String after) {
        if (before == null || before.equals(after)) {
            return true;
        }
        // Currently, all transitions are valid.
        switch (before) {
            case EDITABLE:
                return after.equals(FINAL);
            case FINAL:
                return after.equals(EDITABLE);
            default:
                return false;
        }
    }
}