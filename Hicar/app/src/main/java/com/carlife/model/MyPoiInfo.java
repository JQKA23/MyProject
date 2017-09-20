package com.carlife.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class MyPoiInfo implements Serializable {
	private List<Map<String, String>> adpterList;

	public List<Map<String, String>> getAdpterList() {
		return adpterList;
	}

	public void setAdpterList(List<Map<String, String>> adpterList) {
		this.adpterList = adpterList;
	}

}
