package dk.openesdh.repo.selenium.framework.enums;

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
    ALICE("alice", "alice", "Alice", "Doe"), // Alias for default user
    BOB("bob", "bob", "Bob", "Doe"), // Assign CUSTOMER, CUSTOMER_CASES, CUSTOMER_STAFF
    CAROL("carol", "carol", "Carol", "Doe"), // Assign JOB, JOB_CASES, JOB_STAFF
    DAVE("dave", "dave", "Dave", "Doe"), // Assign OPERATIONS, OPERATIONS_CASES, OPERATIONS_STAFF
    INVALID("invalid", "invalid", "", ""), // This user should *NOT* be made
    NONE("", "", "", "");	// This is a blank user for special cases

    private final String username;
    private final String password;
    private final String firstname;
    private final String lastname;

    private static final Map<String, User> fullnameStringToEnum = new HashMap<String, User>();

    static {
        for(User user : values()) {
            fullnameStringToEnum.put(user.fullname(), user);
        }
    }

    private User(final String newUsername,
                 final String newPassword,
                 final String newFirstname,
                 final String newLastname) {
        this.username = newUsername;
        this.password = newPassword;
        this.firstname = newFirstname;
        this.lastname = newLastname;
    }

    public String username() { return username; }
    public String password() { return password; }
    public String firstname() { return firstname; }
    public String lastname() { return lastname; }
    public String fullname() { return firstname + " " + lastname; }

    /**
     * Convert fullname String to User
     * @param fullname String with the users fullname
     * @return
     */
    public static User fromFullnameString(String fullname) {
        return fullnameStringToEnum.get(fullname);
    }
}
