package com.carlife.model;

import java.io.Serializable;

public class Ad  implements Serializable{

	
	private String Image;
	public void setImage(String img){
		this.Image=img;
	}
	public String getImage(){
		return this.Image;
	}
	
	private String Url;
	public void setUrl(String url){
		this.Url=url;
	}
	public String getUrl(){
		return this.Url;
	}
}
