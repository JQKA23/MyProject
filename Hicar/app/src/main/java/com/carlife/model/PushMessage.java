package com.carlife.model;

import java.io.Serializable;



public class PushMessage implements Serializable{
	
	
	private int Id;
	public void setId(int id)
	{
		this.Id=id;
	}
	public int getId()
	{
		return this.Id;
	}
	
	private String Title;
	public void setTitle(String title)
	{
		this.Title=title;
	}
	public String getTitle()
	{
		return this.Title;
	}
	
	private String Content;
	public void setContent(String content)
	{
		this.Content=content;
	}
	public String getContent()
	{
		return this.Content;
	}

}
