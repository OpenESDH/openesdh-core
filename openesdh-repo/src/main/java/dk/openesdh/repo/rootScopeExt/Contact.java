package dk.openesdh.repo.rootScopeExt;

import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.services.contacts.ContactService;
import org.alfresco.repo.jscript.*;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.List;

public class Contact extends BaseScopableProcessorExtension{
    private ContactService contactService;

    /**
     * See org.alfresco.repo.jscript#getPeoplePaging(String filter, ScriptPagingDetails pagingRequest, String sortBy, Boolean sortAsc)
     * @param id the email id
     * @param type allowed types are {PERSON/ORGANIZATION}
     * @return org.mozilla.javascript.Scriptable object (See mozilla docs)
     */
    public Scriptable getContactsById(String id, String type ){
        List<ContactInfo> contacts = this.contactService.getContactByFilter(id, type.toUpperCase());

        Object[] contactRefs = new Object[contacts.size()];
        for (int i = 0; i < contactRefs.length; i++){
            contactRefs[i] = contacts.get(i).getNodeRef();
        }

        return Context.getCurrentContext().newArray(getScope(), contactRefs);
    }

    //<editor-fold desc="injected bean setters">
    public void setContactService(ContactService contactService) {
        this.contactService = contactService;
    }
    //</editor-fold>

}
