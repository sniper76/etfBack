package com.etf.rest.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
	
	public static String toDate(String fmt) {
		SimpleDateFormat sdf = new SimpleDateFormat(fmt);
		return sdf.format(new Date());
	}
	
	public static int parseInt(Object obj) {
		if(obj == null) {
			return 0;
		}
		return Integer.parseInt(obj.toString().replaceAll(",", ""));
	}

//	public static void main(String[] args) {
//
//		List<Item> items = new ArrayList<Item>();
//		Item i1 = new Item();
//		i1.setSTK_NM_KOR("AAA");
//		Item i2 = new Item();
//		i2.setSTK_NM_KOR("AAA");
//		Item i3 = new Item();
//		i3.setSTK_NM_KOR("AAA");
//		Item i4 = new Item();
//		i4.setSTK_NM_KOR("BBB");
//		Item i5 = new Item();
//		i5.setSTK_NM_KOR("BBB");
//		Item i6 = new Item();
//		i6.setSTK_NM_KOR("CCC");
//		Item i7 = new Item();
//		i7.setSTK_NM_KOR("DDD");
//		Item i8 = new Item();
//		i8.setSTK_NM_KOR("EEE");
//		Item i9 = new Item();
//		i9.setSTK_NM_KOR("DDD");
//
//		items.add(i1);
//		items.add(i9);
//		items.add(i2);
//		items.add(i8);
//		items.add(i3);
//		items.add(i7);
//		items.add(i4);
//		items.add(i6);
//		items.add(i5);
//
//		List<Item> returnItems = new ArrayList<Item>();
//
//		for (Item r : items) {
//			Item firstElement = returnItems.stream()
//			        .filter(s -> r.getSTK_NM_KOR().equals(s.getSTK_NM_KOR())).findFirst().orElse(null);
//			if(firstElement != null) {
//				Optional<Item> element = returnItems.stream()
//					.filter(s -> r.getSTK_NM_KOR().equals(s.getSTK_NM_KOR())).findFirst();
//
//				element.get().setSTK_NM_CNT(element.get().getSTK_NM_CNT()+1);
//			}
//			else {
//				r.setSTK_NM_CNT(1);
//				returnItems.add(r);
//			}
//		}
//
//		Collections.sort(returnItems);
//
//		for (Item item : returnItems) {
//			System.out.println(item);
//		}
//	}
}
