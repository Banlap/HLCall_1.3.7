package hzhl.net.hlcall.fragment;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import hzhl.net.hlcall.App;
import hzhl.net.hlcall.entity.ContactsListEntity;
import hzhl.net.hlcall.utils.CharacterParser;
import hzhl.net.hlcall.utils.ContactsUtil;
import hzhl.net.hlcall.utils.PinyinComparator;
import hzhl.net.hlcall.utils.StringUtil;

public class ContactsListViewModel extends ViewModel {
    private ArrayList<ContactsListEntity> contactsList = new ArrayList<>();//获取系统联系人的数据
    private MutableLiveData<ContactsListEntity> contact = new MutableLiveData<>();
    private MutableLiveData<ArrayList<ContactsListEntity>> list = new MutableLiveData<>();
    public MutableLiveData<ContactsListEntity> getContactsListEntityForId(String id){
        contact = new MutableLiveData<>();
        App.runAsync(() -> {
            contact.postValue(ContactsUtil.searchContactInId(App.sContext,id));
        });
        return contact;
    }

    public void getContactsListTask(){
        if (contactsList.size()>0) list.postValue(contactsList);
        else {
            App.runAsync(() -> updateContacts(ContactsUtil.queryContacts(App.sContext)));

        }
    }

    public List<ContactsListEntity> getContactsList() {
        return contactsList;
    }
    public void setContactsList(ArrayList<ContactsListEntity> contactsList) {
        this.contactsList = contactsList;
    }


    public ContactsListEntity getContactsListEntity(int index){
        return contactsList.get(index);
    }

    public MutableLiveData<ContactsListEntity> getContact() {
        return contact;
    }

    public MutableLiveData<ArrayList<ContactsListEntity>> getList() {
        return list;
    }

    //设置数据拼音的首字母
    private void setSortedLetters(ArrayList<ContactsListEntity> dataList) {
        // 实例化汉字转拼音类
        CharacterParser characterParser = CharacterParser.getInstance();
        for (int i = 0; i < dataList.size(); i++) {
            ContactsListEntity entity = dataList.get(i);
            if (StringUtil.isEmpty(entity.getName())
                    || StringUtil.isEmpty(entity.getName().trim())) {
                entity.setName("#");
            }
            // 汉字转换成拼音
            Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
            if (p.matcher(entity.getName().trim().substring(0, 1)).matches()) {
                // 中文开头
                String pinyin = characterParser.getSelling(entity.getName());
                String sortString = pinyin.substring(0, 1).toUpperCase(
                        Locale.CHINESE);
                // 正则表达式，判断首字母是否是英文字母
                if (sortString.matches("[A-Z]")) {
                    entity.setSortLetters(sortString
                            .toUpperCase(Locale.CHINESE));
                } else {
                    entity.setSortLetters("#");
                }
            } else {
                // 英文
                String sortString = entity.getName().trim().substring(0, 1)
                        .toUpperCase(Locale.CHINESE);
                if (sortString.matches("[A-Z]")) {
                    entity.setSortLetters(sortString
                            .toUpperCase(Locale.CHINESE));
                } else {
                    entity.setSortLetters("#");
                }
            }
        }
    }

    public void updateContacts(ArrayList<ContactsListEntity> contactsList) {
        //设置数据拼音的首字母
        setSortedLetters(contactsList);
        //拼音拼音比对类
        PinyinComparator pinyinComparator = new PinyinComparator();
        // 根据a-z进行排序源数据
        Collections.sort(contactsList, pinyinComparator);
        list.postValue(contactsList);
        this.contactsList = contactsList;
    }
}
