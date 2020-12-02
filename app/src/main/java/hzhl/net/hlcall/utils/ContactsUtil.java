package hzhl.net.hlcall.utils;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hzhl.net.hlcall.entity.ContactsEntity;
import hzhl.net.hlcall.entity.ContactsListEntity;
import hzhl.net.hlcall.entity.NumberEntity;

public class ContactsUtil {
    public static ArrayList<ContactsListEntity> queryContacts(Context context) {
        ArrayList<ContactsListEntity> dataList = new ArrayList<>();
        ArrayMap<String,ContactsListEntity> arrayMap = new ArrayMap<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.RawContacts.CONTACT_ID
                }
                , null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                StringBuilder buf = new StringBuilder();
                ContactsListEntity entity = new ContactsListEntity();
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
                String idContact = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
                entity.setName(name);
                entity.setId(id);
                buf.append("raw_contacts_id=" + id);
                buf.append("contacts_id=" + idContact);
                buf.append(" ,name=" + name);
                //Logger.d(buf.toString());
                if (!StringUtil.isEmpty(idContact)&&!StringUtil.isEmpty(entity.getName())) {//过滤掉没名字的
                    dataList.add(entity);
                    arrayMap.put(name,entity);
                }
            }
            cursor.close();
        }
        Logger.d(" size=" + dataList.size());
        dataList.clear();
        dataList.addAll(arrayMap.values());
        //return dataList;
        return dataList;
    }

    /**
     * 根据rawContactId获取该联系人数据
     *
     * @return 单个联系人数据
     */
    public static ContactsListEntity searchContactInId(Context context, String rawContactId) {
        ContactsListEntity entity = new ContactsListEntity();
        ArrayList<NumberEntity> numberList = new ArrayList<>();
        if (!StringUtil.isEmpty(rawContactId)) {
            entity.setId(rawContactId);//保存rawContactId
            Cursor cursor = context.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    new String[]{ContactsContract.Data._ID, ContactsContract.Data.MIMETYPE
                            , ContactsContract.Data.DATA1, ContactsContract.Data.DATA2
                            , ContactsContract.Data.DATA4, ContactsContract.Data.DATA15},
                    ContactsContract.Data.RAW_CONTACT_ID + "='" + rawContactId + "'"
                    , null, null);
            StringBuilder buf = new StringBuilder();
            buf.append(" ,rawContactId=" + rawContactId);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    NumberEntity numberEntity = new NumberEntity();
                    String dataId = cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID));
                    numberEntity.setId(dataId);
                    // buf.append(" ,dataId=" + dataId);
                    String data = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1));
                    if (cursor.getString(cursor.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/name")) {//如果是名字
                        entity.setName(data);
                        buf.append(" ,name=" + data);
                    } else if (cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE)).equals("vnd.android.cursor.item/phone_v2")) {  //如果是电话
                        numberEntity.setNumber(data);
                        numberEntity.setType(cursor.getString(cursor.getColumnIndex(COLUMN_NUMBER_TYPE)));
                        numberList.add(numberEntity);
                        buf.append(" ,Phone=" + data + " ,类型type=" + cursor.getString(cursor.getColumnIndex(COLUMN_NUMBER_TYPE)));
                    } else if (cursor.getString(cursor.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/email_v2")) {  //如果是email
                        buf.append(" ,email=" + data);
                    } else if (cursor.getString(cursor.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/postal-address_v2")) { //如果是地址
                        buf.append(" ,address=" + data);
                    } else if (cursor.getString(cursor.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/photo")) {//图片
                        buf.append(" ,photo=" + Arrays.toString(cursor.getBlob(cursor.getColumnIndex(ContactsContract.Data.DATA15))));
                        entity.setBytes(cursor.getBlob(cursor.getColumnIndex(ContactsContract.Data.DATA15)));
                    } else if (cursor.getString(cursor.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/organization")) {
                        //如果是组织
                        entity.setCompany(data);
                        entity.setJob(cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA4)));
                        buf.append(" ,organization=" + data + " ,职位=" + cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA4)));
                    } else {
                        buf.append(" ,other=" + data);
                    }

                }
                cursor.close();
                entity.setNumberList(numberList);
                Logger.d(buf.toString());
            }
        }
        return entity;
    }

    public static ArrayList<ContactsListEntity> readContacts(Context context) {
        ArrayList<ContactsListEntity> dataList = new ArrayList<>();
        //uri = content://com.android.contacts/contacts
        Uri uri = Uri.parse("content://com.android.contacts/contacts"); //访问raw_contacts表
        ContentResolver resolver = context.getContentResolver();
        //获得_id属性
        Cursor cursor = resolver.query(ContactsContract.RawContacts.CONTENT_URI, new String[]{ContactsContract.Data._ID}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ContactsListEntity entity = new ContactsListEntity();
                ArrayList<NumberEntity> numberList = new ArrayList<>();
                StringBuilder buf = new StringBuilder();
                //获得id并且在data中寻找数据
                int id = cursor.getInt(0);
                entity.setId(id + "");
                buf.append("raw_contacts_id=" + id);
                // buf.append(" ,raw_contact_id2=" + cursor.getString(cursor.getColumnIndex("version")));
                uri = Uri.parse("content://com.android.contacts/raw_contacts/" + id + "/data");
                //data1存储各个记录的总数据，mimetype存放记录的类型，如电话、email等
                Cursor cursor2 = resolver.query(uri, new String[]{ContactsContract.Data._ID, ContactsContract.Data.MIMETYPE
                        , ContactsContract.Data.DATA1, ContactsContract.Data.DATA2, ContactsContract.Data.DATA4
                        , ContactsContract.Data.DATA15}, null, null, null);
                // Logger.d("总列数:"+cursor2.getColumnCount());
                // Logger.d("游标中的行数:"+cursor2.getCount());

                if (cursor2 != null) {
                    while (cursor2.moveToNext()) {
                        NumberEntity numberEntity = new NumberEntity();
                        int dataId = cursor2.getInt(0);
                        numberEntity.setId(dataId + "");
                        //  buf.append(" ,dataId=" + dataId);
                        String data = cursor2.getString(cursor2.getColumnIndex(ContactsContract.Data.DATA1));
                        if (cursor2.getString(cursor2.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/name")) {//如果是名字
                            entity.setName(data);
                            buf.append(" ,name=" + data);
                        } else if (cursor2.getString(cursor2.getColumnIndex(ContactsContract.Data.MIMETYPE)).equals("vnd.android.cursor.item/phone_v2")) {  //如果是电话
                            numberEntity.setNumber(data);
                            numberEntity.setType(cursor2.getString(cursor2.getColumnIndex(COLUMN_NUMBER_TYPE)));
                            numberList.add(numberEntity);
                            //    buf.append(" ,Phone=" + data + " ,类型type=" + cursor2.getString(cursor2.getColumnIndex(COLUMN_NUMBER_TYPE)));
                        } else if (cursor2.getString(cursor2.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/email_v2")) {  //如果是email
                            buf.append(" ,email=" + data);
                        } else if (cursor2.getString(cursor2.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/postal-address_v2")) { //如果是地址
                            buf.append(" ,address=" + data);
                        } else if (cursor2.getString(cursor2.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/photo")) {//图片
                            buf.append(" ,photo=" + Arrays.toString(cursor2.getBlob(cursor2.getColumnIndex(ContactsContract.Data.DATA15))));
                            entity.setBytes(cursor2.getBlob(cursor2.getColumnIndex(ContactsContract.Data.DATA15)));
                        } else if (cursor2.getString(cursor2.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/organization")) {
                            //如果是组织
                            entity.setCompany(data);
                            entity.setJob(cursor2.getString(cursor2.getColumnIndex(ContactsContract.Data.DATA4)));
                            buf.append(" ,organization=" + data + " ,职位=" + cursor2.getString(cursor2.getColumnIndex(ContactsContract.Data.DATA4)));
                        } else {
                            buf.append(" ,other=" + data);
                        }
                    }
                    cursor2.close();//防止内存溢出，cursor关掉
                }
                entity.setNumberList(numberList);
                if (!StringUtil.isEmpty(entity.getName())) {//过滤掉没名字的
                    dataList.add(entity);
                }
                String str = buf.toString();
                //  Logger.d(str);
            }
            cursor.close();//防止内存溢出，cursor关掉
        }
        return dataList;
    }


    public static List<ContactsListEntity> printContacts(Context context) {
        List<ContactsListEntity> dataList = new ArrayList<>();
        Cursor phoneCursor = null;
        //生成ContentResolver对象
        ContentResolver contentResolver = context.getContentResolver();
        // 获得所有的联系人
        //这段代码和上面代码是等价的，使用两种方式获得联系人的Uri
        Cursor cursor = contentResolver.query(Uri.parse("content://com.android.contacts/contacts"), null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ContactsListEntity entity = new ContactsListEntity();
                ArrayList<NumberEntity> numberList = new ArrayList<>();
                int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                int displayNameColumn = cursor
                        .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

                // 获得联系人的ID
                String contactId = cursor.getString(idColumn);
                // 获得联系人姓名
                String displayName = cursor.getString(displayNameColumn);
                if (TextUtils.isEmpty(displayName)) {/*姓名为空 跳过 继续查找*/
                    continue;
                }
                entity.setName(displayName);
                /*       if (containsEmoji(displayName)) {*//*姓名包含 emoji 跳过 继续查找*//*
                        continue;
                    }*/

                /*设置联系人名字*/
                // todo  拿到数据处理
                // 查看联系人有多少个号码，如果没有号码，返回0
                int phoneCount = cursor.getInt(cursor
                        .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                if (phoneCount > 0) {
                    // 获得联系人的电话号码列表
                    phoneCursor = context.getContentResolver().query(
                            Phone.CONTENT_URI,
                            null,
                            Phone.CONTACT_ID
                                    + "=" + contactId, null, null);
                    /* 定义一个数 来获取不同的 phone*/
                    StringBuilder sb = new StringBuilder();
//                        /*计数*/
                    /*限制导入 5个以内*/
                    int i = 0;
                    if (phoneCursor != null) {
                        while (phoneCursor.moveToNext()) {
                            NumberEntity numberEntity = new NumberEntity();
                            //遍历所有的联系人下面所有的电话号码
                            String phoneNum = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER));
                            if (!TextUtils.isEmpty(phoneNum) && phoneNum.length() < 20) {
                                /*限制 导入 5个 电话号码*/
                                i++;
                                if (i >= 6) {
                                    continue;
                                }
                                // 20170929过滤 包含+86 和 "-" 的字段  将其替换为 "" 空串
                                phoneNum = phoneNum.replace("+86", "").replace("-", "");
//                                    if(i<phoneCount){
                                sb.append(phoneNum).append(",");
                                numberEntity.setNumber(phoneNum);
                                numberList.add(numberEntity);
                            }
                        }
                        phoneCursor.close();
                    }
                    entity.setNumberList(numberList);
                    dataList.add(entity);
                }
            }
            cursor.close();
        }
        return dataList;
    }



    public static boolean updateContacts(Context context, ContactsListEntity contactsListEntity, String rawContactId) {
        if (contactsListEntity != null && !StringUtil.isEmpty(rawContactId)) {
            ArrayList<NumberEntity> entity = new ArrayList<>();
            entity = contactsListEntity.getNumberList();
            Uri uri = Uri.parse("content://com.android.contacts/data");//对data表的所有数据操作
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            //更新名字
            String dataIdName = isExitMimeType(context, contactsListEntity.getId(), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            if (dataIdName.equals("0")) {
                values.put("raw_contact_id", contactsListEntity.getId());
                values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
                values.put("data1", contactsListEntity.getName());
                //wenyeyang
                values.put("data2", "");
                values.put("data3", "");
                values.put("data5", "");
                //
                Uri resultAddNameUri = resolver.insert(uri, values);
                Logger.d("resultAddNameUri=" + resultAddNameUri);
                values.clear();
            } else {
                if (!StringUtil.isEmpty(contactsListEntity.getName())) {
                    values.put("data1", contactsListEntity.getName());
                    //wenyeyang
                    values.put("data2", "");
                    values.put("data3", "");
                    values.put("data5", "");
                    //
                    int result = resolver.update(uri, values, "mimetype=? and raw_contact_id=?"
                            , new String[]{ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, contactsListEntity.getId()});
                    values.clear();
                    Logger.d("名字更新结果:" + result);
                }
            }
            //add organization
            String dataId = isExitMimeType(context, contactsListEntity.getId(), "vnd.android.cursor.item/organization");
            if (dataId.equals("0")) {
                //add organization
                if (!StringUtil.isEmpty(contactsListEntity.getCompany())) {
                    values.put("raw_contact_id", contactsListEntity.getId());
                    values.put(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/organization");
                    values.put("data1", contactsListEntity.getCompany());   //公司
                    values.put("data4", contactsListEntity.getJob());
                    Uri resultAddCompanyUri = resolver.insert(uri, values);
                    Logger.d("resultAddCompanyUri=" + resultAddCompanyUri);
                    values.clear();
                }
            } else {
                values.put("data1", contactsListEntity.getCompany());
                values.put("data4", contactsListEntity.getJob());
                int result = resolver.update(uri, values, "mimetype=? and raw_contact_id=?"
                        , new String[]{"vnd.android.cursor.item/organization", contactsListEntity.getId()});
                values.clear();
                Logger.d("updateCompany:" + result);
            }


        /*    if (!contactNew.getEmail().trim().equals("")) {
                ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(COLUMN_CONTACT_ID + "=? AND " + COLUMN_MIMETYPE + "=?",
                                new String[]{id, MIMETYPE_STRING_EMAIL})
                        .withValue(COLUMN_EMAIL, contactNew.getEmail())
                        .withValue(COLUMN_EMAIL_TYPE, contactNew.getEmailType())
                        .build());
                Logger.d(TAG, "update email: " + contactNew.getEmail());
            }*/

            //updatePhoto
            String dataIdPhoto = isExitMimeType(context, contactsListEntity.getId(), "vnd.android.cursor.item/photo");
            if (dataIdPhoto.equals("0")) {
                if (contactsListEntity.getBytes() != null) {
                    values.put("raw_contact_id", contactsListEntity.getId());
                    values.put(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/photo");
                    values.put("data15", contactsListEntity.getBytes());
                    Uri resultAddPhotoUri = resolver.insert(uri, values);
                    Logger.d("resultAddPhotoUri=" + resultAddPhotoUri);
                    values.clear();
                }
            } else {
                if (contactsListEntity.getBytes() != null) {
                    values.put("data15", contactsListEntity.getBytes());
                    int result2 = resolver.update(uri, values, "mimetype=? and raw_contact_id=?"
                            , new String[]{"vnd.android.cursor.item/photo", contactsListEntity.getId()});
                    values.clear();
                    Logger.d("updatePhoto:" + result2);
                }
            }
            //update number
            String dataIdNumber = isExitMimeType(context, contactsListEntity.getId(), Phone.CONTENT_ITEM_TYPE);
            if (dataIdNumber.equals("0")) {
                insertNumber(context, contactsListEntity.getId(), entity);
            } else {
                //删除所有号码，再插入新值
                int resultDelNumber = deleteNumber(context, contactsListEntity.getId());
                Logger.d("删除号码结果==" + resultDelNumber);
                if (resultDelNumber != 0) {
                    insertNumber(context, contactsListEntity.getId(), entity);
                }
            }
            return true;
        }
        return false;
    }

    public static void insertNumber(Context context, String rawContactId, ArrayList<NumberEntity> entity) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        Uri uri = Uri.parse("content://com.android.contacts/data");//对data表的所有数据操作
        if (!StringUtil.isEmpty(rawContactId) && entity != null) {
            for (int i = 0; i < entity.size(); i++) {
                //add Phone
                values.put("raw_contact_id", rawContactId);
                values.put(ContactsContract.Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                values.put(Phone.NUMBER, entity.get(i).getNumber());
                if (entity.get(i).getType() == null) {//新增号码，默认手机类型
                    values.put(Phone.TYPE, Phone.TYPE_MOBILE);
                } else {
                    values.put(Phone.TYPE, entity.get(i).getType());
                }
                Uri resultAddNumberUri = resolver.insert(uri, values);
                Logger.d("resultAddNumberUri=" + resultAddNumberUri);
                values.clear();
            }
        }
    }


    public static int deleteNumber(Context context, String raw_contact_id) {
        int result = 0;
        if (!StringUtil.isEmpty(raw_contact_id)) {
            Uri uri = Uri.parse("content://com.android.contacts/data");
            ContentResolver resolver = context.getContentResolver();
            if (resolver != null) {
                //根据raw_contact_id删除data中的号码数据
                result = resolver.delete(uri, "mimetype=? and raw_contact_id=?", new String[]{"vnd.android.cursor.item/phone_v2", raw_contact_id});
                return result;
            }
        }
        return result;
    }

    public static int deletePhoto(Context context, String raw_contact_id) {
        int result = 0;
        if (!StringUtil.isEmpty(raw_contact_id)) {
            Uri uri = Uri.parse("content://com.android.contacts/data");
            ContentResolver resolver = context.getContentResolver();
            if (resolver != null) {
                //根据raw_contact_id删除data中的号码数据
                result = resolver.delete(uri, "mimetype=? and raw_contact_id=?", new String[]{"vnd.android.cursor.item/photo", raw_contact_id});
                return result;
            }
        }
        return result;
    }

    /**
     * @param contactOld The contact wants to be updated. The name should exists.
     * @param contactNew
     */
    public static void updateContact(Context context, ContactsEntity contactOld, ContactsEntity contactNew) {
        Logger.w(TAG, "**update start**");
        String id = getContactID(context, contactOld.getName());
        if (id.equals("0")) {
            Logger.d(TAG, contactOld.getName() + " not exist.");
        } else if (contactNew.getName().trim().equals("")) {
            Logger.d(TAG, "contact name is empty. exit.");
        } else if (!getContactID(context, contactNew.getName()).equals("0")) {
            Logger.d(TAG, "new contact name already exist. exit.");
        } else {

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            //update name
            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(COLUMN_CONTACT_ID + "=? AND " + COLUMN_MIMETYPE + "=?",
                            new String[]{id, MIMETYPE_STRING_NAME})
                    .withValue(COLUMN_NAME, contactNew.getName())
                    .build());
            Logger.d(TAG, "update name: " + contactNew.getName());

            //update number
            if (!contactNew.getNumber().trim().equals("")) {
                ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(COLUMN_CONTACT_ID + "=? AND " + COLUMN_MIMETYPE + "=?",
                                new String[]{id, MIMETYPE_STRING_PHONE})
                        .withValue(COLUMN_NUMBER, contactNew.getNumber())
                        .withValue(COLUMN_NUMBER_TYPE, contactNew.getNumberType())
                        .build());
                Logger.d(TAG, "update number: " + contactNew.getNumber());
            }

            //update email if mail
            if (!contactNew.getEmail().trim().equals("")) {
                ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(COLUMN_CONTACT_ID + "=? AND " + COLUMN_MIMETYPE + "=?",
                                new String[]{id, MIMETYPE_STRING_EMAIL})
                        .withValue(COLUMN_EMAIL, contactNew.getEmail())
                        .withValue(COLUMN_EMAIL_TYPE, contactNew.getEmailType())
                        .build());
                Logger.d(TAG, "update email: " + contactNew.getEmail());
            }

            try {
                context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                Logger.d(TAG, "update success");
            } catch (Exception e) {
                Logger.d(TAG, "update failed");
                Logger.e(TAG, e.getMessage());
            }
        }
        Logger.w(TAG, "**update end**");
    }

    /**
     * 写入手机联系人
     */
    public static boolean writeContact(Context context, ContactsListEntity entity, String number1, String number2) {
        //先查询要添加的号码是否已存在通讯录中, 不存在则添加. 存在则提示用户
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentValues values = new ContentValues();
        long contact_id = ContentUris.parseId(resolver.insert(uri, values));
        //插入data表
        uri = Uri.parse("content://com.android.contacts/data");
        //add Name
        if (!StringUtil.isEmpty(entity.getName())) {
            values.put("raw_contact_id", contact_id);
            values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            values.put("data1", entity.getName());
            //values.put("data2", "qq");
            //wenyeyang
            values.put("data2", "");
            values.put("data3", "");
            values.put("data5", "");
            //
            Uri resultAddNameUri = resolver.insert(uri, values);
            Logger.d("resultAddNumberUri=" + resultAddNameUri);
            values.clear();
        }

        //add Phone
        if (!StringUtil.isEmpty(number1)) {
            values.put("raw_contact_id", contact_id);
            values.put(ContactsContract.Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
            values.put(Phone.NUMBER, number1);
            values.put(Phone.TYPE, Phone.TYPE_MOBILE);
            Uri resultAddNumber1Uri = resolver.insert(uri, values);
            Logger.d("resultAddNumberUri=" + resultAddNumber1Uri);
            values.clear();
        }

        //add Phone2
        if (!StringUtil.isEmpty(number2)) {
            values.put("raw_contact_id", contact_id);
            values.put(ContactsContract.Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
            values.put("data1", number2);//手机
            values.put(Phone.TYPE, Phone.TYPE_MOBILE);
            Uri resultAddNumber2Uri = resolver.insert(uri, values);
            Logger.d("resultAddNumberUri=" + resultAddNumber2Uri);
            values.clear();
        }

        //add photo
        if (entity.getBytes() != null) {
            values.put(ContactsContract.Data.RAW_CONTACT_ID, contact_id);
            values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, entity.getBytes());
            resolver.insert(uri, values);
            values.clear();
        }
        //add email
/*            values.put("raw_contact_id", contact_id);
            values.put(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/email_v2");
            values.put("data2", "1");   //邮箱
            values.put("data1", "xxxx@qq.com");
            resolver.insert(uri, values);
            values.clear();*/

        //add organization
        if (!StringUtil.isEmpty(entity.getCompany()) || !StringUtil.isEmpty(entity.getJob())) {
            values.put("raw_contact_id", contact_id);
            values.put(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/organization");
            if (entity.getCompany() != null) {
                values.put("data1", entity.getCompany());   //公司
            }
            if (entity.getJob() != null) {
                values.put("data4", entity.getJob());   //职务
            }
            Uri resultAddCompanyUri = resolver.insert(uri, values);
            Logger.d("resultAddCompanyUri=" + resultAddCompanyUri);
            values.clear();
        }
        return true;
    }


    private ContentResolver contentResolver;
    private static final String TAG = "ContactsManager";

    /**
     * Use a simple string represents the long.
     */
    private static final String COLUMN_CONTACT_ID =
            ContactsContract.Data.CONTACT_ID;
    private static final String COLUMN_RAW_CONTACT_ID =
            ContactsContract.Data.RAW_CONTACT_ID;
    private static final String COLUMN_DATA_ID =
            ContactsContract.Data._ID;
    private static final String COLUMN_MIMETYPE =
            ContactsContract.Data.MIMETYPE;
    private static final String COLUMN_NAME =
            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME;
    private static final String COLUMN_NUMBER =
            Phone.NUMBER;
    private static final String COLUMN_NUMBER_TYPE =
            Phone.TYPE;
    private static final String COLUMN_EMAIL =
            ContactsContract.CommonDataKinds.Email.DATA;
    private static final String COLUMN_EMAIL_TYPE =
            ContactsContract.CommonDataKinds.Email.TYPE;
    private static final String MIMETYPE_STRING_NAME =
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;
    private static final String MIMETYPE_STRING_PHONE =
            Phone.CONTENT_ITEM_TYPE;
    private static final String MIMETYPE_STRING_EMAIL =
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE;

    public ContactsUtil(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    /**
     * Search and fill the contact information by the contact name given.
     */
    public static ContactsEntity searchContact(Context context, String name) {

        ContactsEntity contact = new ContactsEntity();
        contact.setName(name);
        Logger.d("search name: " + contact.getName());
        String id = getContactID(context, contact.getName());
        contact.setId(id);

        if (id.equals("0")) {
            Logger.d(contact.getName() + " not exist. exit.");
            return null;
        } else {
            Logger.d("find id: " + id);
            //Fetch Phone Number
            Cursor cursor = context.getContentResolver().query(
                    Phone.CONTENT_URI,
                    new String[]{COLUMN_NUMBER, COLUMN_NUMBER_TYPE},
                    COLUMN_CONTACT_ID + "='" + id + "'", null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    contact.setNumber(cursor.getString(cursor.getColumnIndex(COLUMN_NUMBER)));
                    contact.setNumberType(cursor.getString(cursor.getColumnIndex(COLUMN_NUMBER_TYPE)));
                    Logger.d(TAG, "find number: " + contact.getNumber());
                    Logger.d(TAG, "find numberType: " + contact.getNumberType());
                }
            }
            //cursor.close();

            //Fetch email
            cursor = context.getContentResolver().query(
                    android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    new String[]{COLUMN_EMAIL, COLUMN_EMAIL_TYPE},
                    COLUMN_CONTACT_ID + "='" + id + "'", null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    contact.setEmail(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
                    contact.setEmailType(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL_TYPE)));
                    Logger.d(TAG, "find email: " + contact.getEmail());
                    Logger.d(TAG, "find emailType: " + contact.getEmailType());
                }
                cursor.close();
            }

        }
        Logger.w(TAG, "**search end**");
        return contact;
    }

    /**
     * @return 0 if contact not exist in contacts list. Otherwise return
     * the id of the contact.  raw_contact_id
     */
    public static String getRawContactID(Context context, String name) {
        String id = "0";
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID},
                ContactsContract.Contacts.DISPLAY_NAME +
                        "='" + name + "'", null, null);
        if (cursor != null && cursor.moveToNext()) {
            id = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.RawContacts._ID));
            cursor.close();
        }
        return id;
    }

    public static String getContactID(Context context, String name) {
        String id = "0";
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts._ID},
                ContactsContract.Contacts.DISPLAY_NAME +
                        "='" + name + "'", null, null);
        if (cursor != null && cursor.moveToNext()) {
            id = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.Contacts._ID));
            cursor.close();
        }
        return id;
    }

    /**
     * @param context
     * @param raw_contact_id
     * @return data_id
     */
    public static String isExitMimeType(Context context, String raw_contact_id, String mimetype) {
        String id = "0";
        if (StringUtil.isEmpty(raw_contact_id) || StringUtil.isEmpty(mimetype)) {
            return id;
        }
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data._ID},
                "mimetype=? and raw_contact_id=?", new String[]{mimetype, raw_contact_id}
                , null);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                id = cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID));
            }

            cursor.close();
        }
        return id;
    }

    public static boolean isExitNumber(Context context, String number) {
        //先查询要添加的号码是否已存在通讯录中, 不存在则添加. 存在则提示用户
        //根据电话号码对data表查询
        Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + number);
        ContentResolver resolver = context.getContentResolver();
        //从data表中phones 返回number的display_name
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);
        if (cursor == null)
            return false;
        if (cursor.moveToFirst()) {
            Logger.d("name=" + cursor.getString(0));
            Toast.makeText(context, "存在相同号码", Toast.LENGTH_SHORT).show();
            cursor.close();
            return false;
        } else {
            return true;
        }
    }

    public static String getNameFormNumber(Context context, String number) {
        //根据电话号码对data表查询
        Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + number);
        ContentResolver resolver = context.getContentResolver();
        //从data表中phones 返回number的display_name
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME, Phone.NUMBER
        }, null, null, null);
        if (cursor == null)
            return number;
        cursor.moveToFirst();
        do {
            try {
                Logger.d("name=" + cursor.getString(0));
                String name = cursor.getString(0);
                String numberC = cursor.getString(1);
                if (number.equals(numberC)) {
                    cursor.close();
                    return name;
                }
            }catch (Exception e){
                e.printStackTrace();
                return number;
            }
        } while (cursor.moveToNext());
        return number;
    }

    public static List<ContactsEntity> getListFormNumber(Context context, String number) {
        List<ContactsEntity> list = new ArrayList<>();
        ContactsEntity contacts = new ContactsEntity(number,number);
        list.add(contacts);
        Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + number);
        ContentResolver resolver = context.getContentResolver();
        //从data表中phones 返回number的display_name
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME, Phone.NUMBER
        }, null, null, null);
        if (cursor == null)
            return list;
        cursor.moveToFirst();
        do {
            try {
                Logger.d("name=" + cursor.getString(0));
                //Toast.makeText(context, "存在相同号码", Toast.LENGTH_SHORT).show();
                String name = cursor.getString(0);
                String numberC = cursor.getString(1);
                if (contacts.getNumber().equals(numberC))list.remove(contacts);
                ContactsEntity entity = new ContactsEntity();
                entity.setName(name);
                entity.setNumber(numberC);
                list.add(entity);
            }catch (Exception e){
                return list;
            }
        } while (cursor.moveToNext());
        cursor.close();
        return list;
    }

    public static List<ContactsEntity> getList(Context context) {
        List<ContactsEntity> dataList = new ArrayList<>();
        Cursor phoneCursor = null;
        //生成ContentResolver对象
        ContentResolver contentResolver = context.getContentResolver();
        // 获得所有的联系人
        //这段代码和上面代码是等价的，使用两种方式获得联系人的Uri
        Cursor cursor = contentResolver.query(Uri.parse("content://com.android.contacts/contacts"), null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {

                ArrayList<NumberEntity> numberList = new ArrayList<>();
                int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                int displayNameColumn = cursor
                        .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

                // 获得联系人的ID
                String contactId = cursor.getString(idColumn);
                // 获得联系人姓名
                String displayName = cursor.getString(displayNameColumn);
                if (TextUtils.isEmpty(displayName)) {/*姓名为空 跳过 继续查找*/
                    continue;
                }
                /*       if (containsEmoji(displayName)) {*//*姓名包含 emoji 跳过 继续查找*//*
                        continue;
                    }*/
                /*设置联系人名字*/
                // todo  拿到数据处理
                // 查看联系人有多少个号码，如果没有号码，返回0
                int phoneCount = cursor.getInt(cursor
                        .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                if (phoneCount > 0) {
                    // 获得联系人的电话号码列表
                    phoneCursor = context.getContentResolver().query(
                            Phone.CONTENT_URI,
                            null,
                            Phone.CONTACT_ID
                                    + "=" + contactId, null, null);
                    /* 定义一个数 来获取不同的 phone*/
                    /*计数*/
                    /*限制导入 5个以内*/
                    int i = 0;
                    if (phoneCursor != null) {
                        while (phoneCursor.moveToNext()) {
                            //遍历所有的联系人下面所有的电话号码
                            String phoneNum = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER));
                            if (!TextUtils.isEmpty(phoneNum) && phoneNum.length() < 20) {
                                /*限制 导入 5个 电话号码*/
                                i++;
                                if (i >= 6) {
                                    break;
                                }
                                // 20170929过滤 包含+86 和 "-" 的字段  将其替换为 "" 空串
                                phoneNum = phoneNum.replace("+86", "").replace("-", "");
//                                    if(i<phoneCount){
                                ContactsEntity entity = new ContactsEntity();
                                entity.setNumber(phoneNum);
                                entity.setName(displayName);
                                dataList.add(entity);
                            }
                        }
                        phoneCursor.close();
                    }
                }
            }
            cursor.close();
        }
        return dataList;
    }
    /**
     * You must specify the contact's ID.
     *
     * @param contact
     * @throws Exception The contact's name should not be empty.
     */
    public static void addContact(Context context, ContactsEntity contact) {
        Logger.w(TAG, "**add start**");
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        String id = getContactID(context, contact.getName());
        if (!id.equals("0")) {
            Logger.d(TAG, "contact already exist. exit.");
        } else if (contact.getName().trim().equals("")) {
            Logger.d(TAG, "contact name is empty. exit.");
        } else {
            //content://com.android.contacts/raw_contacts
            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(COLUMN_RAW_CONTACT_ID, 0)
                    .withValue(COLUMN_MIMETYPE, MIMETYPE_STRING_NAME)
                    .withValue(COLUMN_NAME, contact.getName())
                    .build());
            Logger.d(TAG, "add name: " + contact.getName());

            if (!contact.getNumber().trim().equals("")) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(COLUMN_RAW_CONTACT_ID, 0)
                        .withValue(COLUMN_MIMETYPE, MIMETYPE_STRING_PHONE)
                        .withValue(COLUMN_NUMBER, contact.getNumber())
                        .withValue(COLUMN_NUMBER_TYPE, contact.getNumberType())
                        .build());
                Logger.d(TAG, "add number: " + contact.getNumber());
            }

            if (!contact.getEmail().trim().equals("")) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(COLUMN_RAW_CONTACT_ID, 0)
                        .withValue(COLUMN_MIMETYPE, MIMETYPE_STRING_EMAIL)
                        .withValue(COLUMN_EMAIL, contact.getEmail())
                        .withValue(COLUMN_EMAIL_TYPE, contact.getEmailType())
                        .build());
                Logger.d(TAG, "add email: " + contact.getEmail());
            }

            try {
                context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                Logger.d(TAG, "add contact success.");
            } catch (Exception e) {
                Logger.d(TAG, "add contact failed.");
                Logger.e(TAG, e.getMessage());
            }
        }
        Logger.w(TAG, "**add end**");

    }

    /**
     * Delete
     */
    public static boolean deleteContact(Context context, String rawContactId) {
        if (!StringUtil.isEmpty(rawContactId)) {
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            //delete contact information such as phone number,email
            ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection("raw_contact_id" + "=" + rawContactId, null)
                    .build());
            //delete RawContacts
            ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                    .withSelection(ContactsContract.RawContacts._ID + "=" + rawContactId
                            , null)
                    .build());
            try {
                context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                Logger.d("delete contact success");
                return true;
            } catch (Exception e) {
                Logger.d("delete contact failed");
                Logger.e(e.getMessage());
            }
        }
        return false;
    }
}
