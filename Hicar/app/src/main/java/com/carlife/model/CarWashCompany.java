package com.carlife.model;

import java.io.Serializable;

public class CarWashCompany  implements Serializable{

	private int Id;
	public void setId(int id){
		this.Id=id;
	}
	public int getId(){
		return this.Id;
	}
	
	private String CompanyName;
	public void setCompanyName(String companyName){
		this.CompanyName=companyName;
	}
	public String getCompanyName(){
		return this.CompanyName;
	}
	
	private String CompanyPhone;
	public void setCompanyPhone(String companyPhone){
		this.CompanyPhone=companyPhone;
	}
	public String getCompanyPhone(){
		return this.CompanyPhone;
	}
	
	private String Address;
	public void setAddress(String address){
		this.Address=address;
	}
	public String getAddress(){
		return this.Address;
	}
	
	
	private String Latitude;
	public void setLatitude(String latitude){
		this.Latitude=latitude;
	}
	public String getLatitude(){
		return this.Latitude;
	}

	private String Longitude;
	public void setLongitude(String longitude){
		this.Longitude=longitude;
	}
	public String getLongitude(){
		return this.Longitude;
	}


	private String MobilePhone;
	public void setMobilePhone(String mobilePhone){
		this.MobilePhone=mobilePhone;
	}
	public String getMobilePhone(){
		return this.MobilePhone;
	}

	private int PriceBigCar;
	public void setPriceBigCar(int priceBigCar){
		this.PriceBigCar=priceBigCar;
	}
	public int getPriceBigCar(){
		return this.PriceBigCar;
	}
   
	private int PriceSmallCar;
	public void setPriceSmallCar(int priceSmallCar){
		this.PriceSmallCar=priceSmallCar;
	}
	public int getPriceSmallCar(){
		return this.PriceSmallCar;
	}

	private int PriceBigDiscount;
	public void setPriceBigDiscount(int priceBigDiscount){
		this.PriceBigDiscount=priceBigDiscount;
	}
	public int getPriceBigDiscount(){
		return this.PriceBigDiscount;
	}

	private int PriceSmallDiscount;
	public void setPriceSmallDiscount(int priceSmallDiscount){
		this.PriceSmallDiscount=priceSmallDiscount;
	}
	public int getPriceSmallDiscount(){
		return this.PriceSmallDiscount;
	}

	private String Remark;
	public void setRemark(String priceSmallDiscount){
		this.Remark=priceSmallDiscount;
	}
	public String getRemark(){
		return this.Remark;
	}

	private String Distance ;
	public void setDistance(String distance){
		this.Distance=distance;
	}
	public String getDistance(){
		return this.Distance;
	}
	
	private int Star;
	public void setStar(int star){
		this.Star=star;
	}
	public int getStar(){
		return this.Star;
	}
	private int OrderCount;
	public void setOrderCount(int orderCount){
		this.OrderCount=orderCount;
	}
	public int getOrderCount(){
		return this.OrderCount;
	}
	
	private String PicUrl ;
	public void setPicUrl(String picUrl){
		this.PicUrl=picUrl;
	}
	public String getPicUrl(){
		return this.PicUrl;
	}
}
