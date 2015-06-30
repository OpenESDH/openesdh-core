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
    ALICE("abeecher", "test", "Alice", "Beecher"), // Alias for default user
    MIKE_JACKSON("mjackson", "test", "Mike", "Jackson"), // Alias for default user
    BOB("bob", "test", "Bob", "Bygherren"), // Assign CUSTOMER, CUSTOMER_CASES, CUSTOMER_STAFF
    CAROL("carol", "carol", "Carol", "Doe"), // Assign JOB, JOB_CASES, JOB_STAFF
    DAVE("dave", "dave", "Dave", "Doe"), // Assign OPERATIONS, OPERATIONS_CASES, OPERATIONS_STAFF
    INVALID("invalid", "invalid", "", ""), // This user should *NOT* be made
    NONE("", "", "", "");	// This is a blank user for special cases

    private final String username;
    private final String password;
    private final String firstName;
    private final String lastName;

    private static final Map<String, User> fullnameStringToEnum = new HashMap<String, User>();

    static {
        for(User user : values()) {
            fullnameStringToEnum.put(user.fullName(), user);
        }
    }

    private User(final String newUsername,
                 final String newPassword,
                 final String newFirstName,
                 final String newLastName) {
        this.username = newUsername;
        this.password = newPassword;
        this.firstName = newFirstName;
        this.lastName = newLastName;
    }

    public String username() { return username; }
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
