package com.carlife.utility;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EncodeUtility {
	
	public static String md5(String string) {

		byte[] hash;

		try {

			hash = MessageDigest.getInstance("MD5").digest(
					string.getBytes("UTF-8"));

		} catch (NoSuchAlgorithmException e) {

			throw new RuntimeException("Huh, MD5 should be supported?", e);

		} catch (UnsupportedEncodingException e) {

			throw new RuntimeException("Huh, UTF-8 should be supported?", e);

		}

		StringBuilder hex = new StringBuilder(hash.length * 2);

		for (byte b : hash) {

			if ((b & 0xFF) < 0x10)
				hex.append("0");

			hex.append(Integer.toHexString(b & 0xFF));

		}

		return hex.toString();

	}

	/*
	 * 拼接字符串
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String JoinUtil(Map<String, String> map) {
		StringBuilder sb = new StringBuilder();
	
		if (map.size() > 0) {
			List arrayList = new ArrayList(map.entrySet());
			if (arrayList != null) {
				Collections.sort(arrayList, new Comparator() {
					public int compare(Object arg1, Object arg2) {
						Map.Entry obj1 = (Map.Entry) arg1;
						Map.Entry obj2 = (Map.Entry) arg2;
						return (obj1.getKey()).toString().compareTo(
								obj2.getKey().toString());
					}
				});
				for (Iterator iter = arrayList.iterator(); iter.hasNext();) {
					Map.Entry entry = (Map.Entry) iter.next();
					String key = (String) entry.getKey();
					sb.append(key + map.get(key));
				//	System.out.println(map.get(key));
				}
			}
		}

		return sb.toString();

	}

}
