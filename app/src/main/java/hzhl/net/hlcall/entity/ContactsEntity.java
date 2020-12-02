package hzhl.net.hlcall.entity;

import java.io.Serializable;
import java.util.Comparator;

public class ContactsEntity implements Serializable, Comparator<ContactsEntity> {

    private String email;
    private String emailType;
    private String id;
    private String name;
    private String number;
    private String numberType;

    public ContactsEntity(){
    }

    public ContactsEntity(ContactsEntity contact){
        this.name = contact.getName();
        this.number = contact.getNumber();
        this.numberType = contact.getNumberType();
        this.email = contact.getEmail();
        this.emailType = contact.getEmailType();
    }

    public ContactsEntity(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public String getEmail() {
        return email;
    }
    public String getEmailType() {
        return emailType;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getNumber() {
        return number;
    }
    public String getNumberType() {
        return numberType;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setEmailType(String emailType) {
        this.emailType = emailType;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public void setNumberType(String numberType) {
        this.numberType = numberType;
    }


    @Override
    public int compare(ContactsEntity o1, ContactsEntity o2) {
        if (o1.getNumber().equals(o2.getNumber()))return 0;
        else return 1;
    }
}
