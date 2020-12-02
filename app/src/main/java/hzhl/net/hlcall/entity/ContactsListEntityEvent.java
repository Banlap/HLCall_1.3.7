package hzhl.net.hlcall.entity;

import java.util.ArrayList;

public class ContactsListEntityEvent {
    private ArrayList<ContactsListEntity> contactsListEntities;

    public ContactsListEntityEvent(ArrayList<ContactsListEntity> contactsListEntities) {
        this.contactsListEntities = contactsListEntities;
    }

    public ArrayList<ContactsListEntity> getContactsListEntities() {
        return contactsListEntities;
    }

    public void setContactsListEntities(ArrayList<ContactsListEntity> contactsListEntities) {
        this.contactsListEntities = contactsListEntities;
    }
}
