package hzhl.net.hlcall.utils;

import java.util.Comparator;

import hzhl.net.hlcall.entity.ContactsListEntity;

/**  
 * @Title: PinyinComparator
 * @Description: 拼音拼音比对
 * @version V1.0
 */
public class PinyinComparator implements Comparator<ContactsListEntity> {

	public int compare(ContactsListEntity o1, ContactsListEntity o2) {
		if (o1.getSortLetters().equals("@")
				|| o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")
				|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			return o1.getSortLetters().compareTo(o2.getSortLetters());
		}
	}

}
