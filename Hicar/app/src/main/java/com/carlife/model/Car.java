package com.carlife.model;

import java.io.Serializable;

public class Car  implements Serializable{

	private int Id;
	public void setId(int id){
		this.Id=id;
	}
	public int getId(){
		return this.Id;
	}
	
	private String CarNo;
	public void setCarNo(String carNo){
		this.CarNo=carNo;
	}
	public String getCarNo(){
		return this.CarNo;
	}
	
	private String CarColor;
	public void setCarColor(String carColor){
		this.CarColor=carColor;
	}
	public String getCarColor(){
		return this.CarColor;
	}
	
	private String CarModel;
	public void setCarModel(String model){
		this.CarModel=model;
	}
	public String getCarModel(){
		return this.CarModel;
	}
	
	private String CarframeNo;
	public void setCarframeNo(String carframeNo){
		this.CarframeNo=carframeNo;
	}
	public String getCarframeNo(){
		return this.CarframeNo;
	}
	
	private String EngineNo;
	public void setEngineNo(String engineNo){
		this.EngineNo=engineNo;
	}
	public String getEngineNo(){
		return this.EngineNo;
	}
	
	
}
