package com.carlife.model;

import java.io.Serializable;

public class Comment  implements Serializable{

	
	private int Star;
	public void setStar(int star){
		this.Star=star;
	}
	public int getStar(){
		return this.Star;
	}
	
	private String AddTime;
	public void setAddTime(String time){
		this.AddTime=time;
	}
	public String getAddTime(){
		return this.AddTime;
	}
	
	private String Content;
	public void setContent(String content){
		this.Content=content;
	}
	public String getContent(){
		return this.Content;
	}
}
