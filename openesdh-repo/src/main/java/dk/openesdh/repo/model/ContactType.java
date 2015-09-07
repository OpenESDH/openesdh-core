package dk.openesdh.repo.model;

import dk.openesdh.repo.services.contacts.ContactService;
import org.apache.commons.lang.StringUtils;

/**
 * @author lanre.
 */
public enum ContactType {

    ORGANIZATION(true, ContactService.CONTACT_PREFIX, 9),
    PERSON(true, ContactService.CONTACT_PREFIX, 10),
    UNSUPPORTED(false, "", 11);

    private final boolean fixed = false;
    private final String fixedString = "";
    private final boolean prefixed;
    private final String prefixString;
    private final int orderPosition;

    private ContactType(boolean prefixed, String prefixString, int orderPosition) {
        this.prefixed = prefixed;
        this.prefixString = prefixString;
        this.orderPosition = orderPosition;
    }

    public boolean isFixedString() {
        return fixed;
    }

    public String getFixedString() {
        return fixedString;
    }

    public boolean isPrefixed() {
        return prefixed;
    }

    public String getPrefixString() {
        return prefixString;
    }

    public int getOrderPosition() {
        return orderPosition;
    }

    public boolean equals(String authority) {
        return equals(getContactType(authority));
    }

    public static ContactType getContactType(String contact) {
        contact = StringUtils.upperCase(contact);
        if (ContactType.ORGANIZATION.name().equals(contact)) {
            return ContactType.ORGANIZATION;
        }
        if (ContactType.PERSON.name().equals(contact)) {
            return ContactType.PERSON;
        }
        return ContactType.UNSUPPORTED;
    }
}
