package dk.openesdh.repo.model;

/**
 * Created by syastrov on 9/2/15.
 */
public enum CaseStatus {

    ACTIVE,
    PASSIVE,
    CLOSED,
    ARCHIVED;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static boolean isValidTransition(CaseStatus before, CaseStatus after) {
        if (before == null || before == after) {
            return true;
        }
        switch (before) {
            case ACTIVE:
                return after == PASSIVE || after == CLOSED;
            case PASSIVE:
                return after == ACTIVE || after == CLOSED;
            case CLOSED:
                // Cases must be closed before being archived
                return after == ACTIVE || after == PASSIVE || after == ARCHIVED;
            default:
                // Archived cases cannot transition to any other status
                return false;
        }
    }
}
