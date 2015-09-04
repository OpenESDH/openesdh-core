package dk.openesdh.repo.model;

import java.util.List;

/**
 * Created by syastrov on 9/2/15.
 */
public class CaseStatus {
    public static final String ACTIVE = "active";
    public static final String PASSIVE = "passive";
    public static final String CLOSED = "closed";
    public static final String ARCHIVED = "archived";

    public static String[] getStatuses() {
        return new String[] {ACTIVE, PASSIVE, CLOSED, ARCHIVED};
    }

    public static boolean isValidTransition(String before,
                                            String after) {
        if (before == null || before.equals(after)) {
            return true;
        }
        switch (before) {
            case ACTIVE:
                return after.equals(PASSIVE) || after.equals(CLOSED);
            case PASSIVE:
                return after.equals(ACTIVE) || after.equals(CLOSED);
            case CLOSED:
                // Cases must be closed before being archived
                return after.equals(ACTIVE) || after.equals(PASSIVE) || after.equals(ARCHIVED);
            default:
                // Archived cases cannot transition to any other status
                return false;
        }
    }
}