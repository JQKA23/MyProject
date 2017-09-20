package com.carlife.model;

import java.io.Serializable;



public class ChewuOrder implements Serializable{
	
	
	private int Id;
	public int getId() {
		return Id;
	}

	public void setId(int id) {
		this.Id = id;
	}
	
	private String OrderNo;
	public void setOrderNo(String orderNo)
	{
		this.OrderNo=orderNo;
	}
	public String getOrderNo()
	{
		return this.OrderNo;
	}
	
	
	private String BusinessType;
	public void setBusinessType(String businessType)
	{
		this.BusinessType=businessType;
	}
	public String getBusinessType()
	{
		return this.BusinessType;
	}

	
	private String AddTime;
	public void setAddTime(String addTime)
	{
		this.AddTime=addTime;
	}
	public String getAddTime()
	{
		return this.AddTime;
	}
	
	
}
