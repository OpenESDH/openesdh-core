package dk.openesdh.repo.model;

import dk.openesdh.repo.services.contacts.ContactService;

/**
 * @author lanre.
 */
public enum ContactType {

    ORGANIZATION
            {
                public boolean isFixedString()
                {
                    return false;
                }

                public String getFixedString()
                {
                    return "";
                }

                public boolean isPrefixed()
                {
                    return true;
                }

                public String getPrefixString()
                {
                    return ContactService.CONTACT_PREFIX;
                }

                public int getOrderPosition()
                {
                    return 9;
                }
            },
    PERSON
            {
                public boolean isFixedString()
                {
                    return false;
                }

                public String getFixedString()
                {
                    return "";
                }

                public boolean isPrefixed()
                {
                    return true;
                }

                public String getPrefixString()
                {
                    return ContactService.CONTACT_PREFIX;
                }

                public int getOrderPosition()
                {
                    return 10;
                }
            },
    UNSUPPORTED
            {
                public boolean isFixedString()
                {
                    return false;
                }

                public String getFixedString()
                {
                    return "";
                }

                public boolean isPrefixed()
                {
                    return false;
                }

                public String getPrefixString()
                {
                    return "";
                }

                public int getOrderPosition()
                {
                    return 11;
                }
            };

    public abstract boolean isFixedString();

    public abstract String getFixedString();

    public abstract boolean isPrefixed();

    public abstract String getPrefixString();

    public abstract int getOrderPosition();

    public boolean equals(String authority)
    {
        return equals(getContactType(authority));
    }

    public static ContactType getContactType(String contact){
        ContactType contactType;

            if (contact.equals(ContactType.ORGANIZATION.name())) {
                contactType = ContactType.ORGANIZATION;
            }
            else if (contact.equals(ContactType.PERSON.name())){
                contactType = ContactType.PERSON;
            }
        else
            contactType = ContactType.UNSUPPORTED;

        return contactType;
    }
}
