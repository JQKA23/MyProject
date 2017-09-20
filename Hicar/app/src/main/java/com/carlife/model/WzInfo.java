package com.carlife.model;

import java.io.Serializable;

public class WzInfo implements Serializable {
	
	private String CarNo;

	public void setCarNo(String carNo) {
		this.CarNo = carNo;
	}

	public String getCarNo() {
		return this.CarNo;
	}

	private String EngineNo; 

	public void setEngineNo(String engineNo) {
		this.EngineNo = engineNo;
	}

	public String getEngineNo() {
		return this.EngineNo;
	}

	private String Fen;

	public void setFen(String fen) {
		this.Fen = fen;
	}

	public String getFen() {
		return this.Fen;
	}

	private String Officer;

	public void setOfficer(String officer) {
		this.Officer = officer;
	}

	public String getOfficer() {
		return this.Officer;
	}

	private String OccurTime;

	public void setOccurTime(String occurTime) {
		this.OccurTime = occurTime;
	}

	public String getOccurTime() {
		return this.OccurTime;
	}
	
	private String OccurArea;

	public void setOccurArea(String occurArea) {
		this.OccurArea = occurArea;
	}

	public String getOccurArea() {
		return this.OccurArea;
	}
	
	private String Code;

	public void setCode(String code) {
		this.Code = code;
	}

	public String getCode() {
		return this.Code;
	}
	
	
	private String Info;

	public void setInfo(String info) {
		this.Info = info;
	}

	public String getInfo() {
		return this.Info;
	}
	
	private String Money;

	public void setMoney(String money) {
		this.Money = money;
	}

	public String getMoney() {
		return this.Money;
	}
	
	
	
}
