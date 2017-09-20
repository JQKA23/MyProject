package com.carlife.model;

import java.io.Serializable;

public class GiftCard  implements Serializable{

	
	
	private String CardNo;
	public void setCardNo(String cardNo){
		this.CardNo=cardNo;
	}
	public String getCardNo(){
		return this.CardNo;
	}
	
	private String Amount;
	public void setAmount(String amount){
		this.Amount=amount;
	}
	public String getAmount(){
		return this.Amount;
	}
	
	
}
