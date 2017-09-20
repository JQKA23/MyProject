package com.carlife.model;

import java.io.Serializable;



public class RescueOrder implements Serializable{
	
	
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
	
	
	private String RescueType;
	public void setRescueType(String rescueType)
	{
		this.RescueType=rescueType;
	}
	public String getRescueType()
	{
		return this.RescueType;
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
	
	private String PricePlan;
	public void setPricePlan(String price)
	{
		this.PricePlan=price;
	}
	public String getPricePlan()
	{
		return this.PricePlan;
	}

	private String Status;
	public void setStatus(String status)
	{
		this.Status=status;
	}
	public String getStatus()
	{
		return this.Status;
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
	
	private String DriverId;
	public void setDriverId(String driverId){
		this.DriverId=driverId;
	}
	public String getDriverId(){
		return this.DriverId;
	}
	
	private String OrderType;
	public void setOrderType(String orderType){
		this.OrderType=orderType;
	}
	public String getOrderType(){
		return this.OrderType;
	}
	
	private String StartPlace;
	public void setStartPlace(String startPlace){
		this.StartPlace=startPlace;
	}
	public String getStartPlace(){
		return this.StartPlace;
	} 
	
	private String EndPlace;
	public void setEndPlace(String endPlace){
		this.EndPlace=endPlace;
	}
	public String getEndPlace(){
		return this.EndPlace;
	}
	
	private String KmReal;
	public void setKmReal(String kmReal){
		this.KmReal=kmReal;
	}
	public String getKmReal(){
		return this.KmReal;
	}
	
	private String CarType;
	public void setCarType(String carType){
		this.CarType=carType;
	}
	public String getCarType(){
		return this.CarType;
	}
	
	private String CarNo;
	public void setCarNo(String carNo){
		this.CarNo=carNo;
	}
	public String getCarNo(){
		return this.CarNo;
	}
	
	private String StartTime;
	public void setStartTime(String startTime){
		this.StartTime=startTime;
	}
	public String getStartTime(){
		return this.StartTime;
	}
	
	private int IsPay;
	public void setIsPay(int isPay){
		this.IsPay=isPay;
	}
	public int getIsPay(){
		return this.IsPay;
	}

}
