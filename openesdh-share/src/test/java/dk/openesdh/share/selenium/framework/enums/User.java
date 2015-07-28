package dk.openesdh.share.selenium.framework.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum class for making picking a user
 * more simple from the testcases. Users should
 * be present in the share.
 * CAVEAT! Read the comments!
 * @author Søren Kirkegård
 *
 */
public enum User {
    ADMIN("admin", "admin", "admin", "admin"),
    ALICE("abeecher", "test", "Alice", "Beecher"), // Assign to CaseSimpleCreator Group
    MIKE_JACKSON("mjackson", "test", "Mike", "Jackson"), // Assign to CaseSimpleReader Group
    BOB("bob", "test", "Bob", "Bygherren"), // Assign to CaseSimpleWriter Group
    CAROL("carol", "test", "Carol", "Doe"), // Normal user
    HELENA("helena", "test", "Helena", "Christensen"), // Normal user
    BRIGITTE("brigitte", "test", "Brigitte", "Nielsen"), // Normal user
    INVALID("invalid", "invalid", "", ""), // This user should *NOT* be made
    NONE("", "", "", "");	// This is a blank user for special cases

    private final String userName;
    private final String password;
    private final String firstName;
    private final String lastName;

    private static final Map<String, User> fullnameStringToEnum = new HashMap<String, User>();
    static {
        for(User user : values()) {
            fullnameStringToEnum.put(user.fullName(), user);
        }
    }

    private User(final String newUserName,
                 final String newPassword,
                 final String newFirstName,
                 final String newLastName) {
        this.userName = newUserName;
        this.password = newPassword;
        this.firstName = newFirstName;
        this.lastName = newLastName;
    }

    public String userName() { return userName; }
    public String password() { return password; }
    public String firstName() { return firstName; }
    public String lastName() { return lastName; }
    public String fullName() { return firstName + " " + lastName; }

    /**
     * Convert fullname String to User
     * @param fullName String with the users fullname
     * @return
     */
    public static User fromFullNameString(String fullName) {
        return fullnameStringToEnum.get(fullName);
    }
}
