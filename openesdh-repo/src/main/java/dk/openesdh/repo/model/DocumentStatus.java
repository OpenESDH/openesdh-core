package dk.openesdh.repo.model;

/**
 * Created by syastrov on 9/2/15.
 */
public enum DocumentStatus {
    DRAFT,
    FINAL;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static boolean isValidTransition(DocumentStatus before, DocumentStatus after) {
        if (before == null || before == after) {
            return true;
        }
        // Currently, all transitions are valid.
        switch (before) {
            case DRAFT:
                return after == FINAL;
            case FINAL:
                return after == DRAFT;
            default:
                return false;
        }
    }

}
