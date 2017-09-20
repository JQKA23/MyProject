package com.carlife.model;

import java.io.Serializable;

public class Water  implements Serializable{
	
	private String CardNo;
	public void setCardNo(String cardNo){
		this.CardNo=cardNo;
	}
	public String getCardNo(){
		return this.CardNo;
	}
	
	private String WaterType;
	public void setWaterType(String w){
		this.WaterType=w;
	}
	public String getWaterType(){
		return this.WaterType;
	}
	
	private String WaterValue;
	public void setWaterValue(String amount){
		this.WaterValue=amount;
	}
	public String getWaterValue(){
		return this.WaterValue;
	}
	
	private String OrderNo;
	public void setOrderNo(String orderNo){
		this.OrderNo=orderNo;
	}
	public String getOrderNo(){
		return this.OrderNo;
	}
	
	private String AllAmount;
	public void setAllAmount(String allAmount){
		this.AllAmount=allAmount;
	}
	public String getAllAmount(){
		return this.AllAmount;
	} 
	
	private String Remark;
	public void setRemark(String remark){
		this.Remark=remark;
	}
	public String getRemark(){
		return this.Remark;
	} 
	
}
