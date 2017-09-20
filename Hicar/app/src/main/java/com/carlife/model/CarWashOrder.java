package com.carlife.model;

import java.io.Serializable;



public class CarWashOrder implements Serializable{
	
	
	private String Id;
	public String getId() {
		return Id;
	}

	public void setId(String orderId) {
		this.Id = orderId;
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
	
	
	private String Price;
	public void setPrice(String price)
	{
		this.Price=price;
	}
	public String getPrice()
	{
		return this.Price;
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
	
	private String Company;
	public void setCompany(String company){
		this.Company=company;
	}
	public String getCompany(){
		return this.Company;
	}
	
	
	
	private String CarType;
	public void setCarType(String carType){
		this.CarType=carType;
	}
	public String getCarType(){
		return this.CarType;
	}
	
	

}
