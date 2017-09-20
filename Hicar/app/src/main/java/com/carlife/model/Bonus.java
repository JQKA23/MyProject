package com.carlife.model;

import java.io.Serializable;

public class Bonus  implements Serializable{
	
	private String Amount;
	public void setAmount(String amount){
		this.Amount=amount;
	}
	public String getAmount(){
		return this.Amount;
	}
	
	private String Period;
	public void setPeriod(String w){
		this.Period=w;
	}
	public String getPeriod(){
		return this.Period;
	}
	
	private String Remark;
	public void setRemark(String remark){
		this.Remark=remark;
	}
	public String getRemark(){
		return this.Remark;
	}
	
}
