package com.carlife.model;

import java.io.Serializable;

public class Driver implements Serializable {

	private int Id;

	public void setId(int id) {
		this.Id = id;
	}

	public int getId() {
		return this.Id;
	}

	private String Mobile;

	public void setMobile(String mobile) {
		this.Mobile = mobile;
	}

	public String getMobile() {
		return this.Mobile;
	}

	private String Lat;

	public void setLat(String lat) {
		this.Lat = lat;
	}

	public String getLat() {
		return this.Lat;
	}

	private String Lon;

	public void setLon(String lat) {
		this.Lon = lat;
	}

	public String getLon() {
		return this.Lon;
	}

	private String RealName;

	public void setRealName(String realName) {
		this.RealName = realName;
	}

	public String getRealName() {
		return this.RealName;
	}

	private String Distance;

	public void setDistance(String distance) {
		this.Distance = distance;
	}

	public String getDistance() {
		return this.Distance;
	}

	private int Bid;

	public void setBid(int bid) {
		this.Bid = bid;
	}

	public int getBid() {
		return this.Bid;
	}

	private double star;
	private int drivingYears;
	private int orderCount;

	public double getStar() {
		return star;
	}

	public void setStar(double star) {
		this.star = star;
	}

	public int getDrivingYears() {
		return drivingYears;
	}

	public void setDrivingYears(int drivingYears) {
		this.drivingYears = drivingYears;
	}

	public int getOrderCount() {
		return orderCount;
	}

	public void setOrderCount(int orderCount) {
		this.orderCount = orderCount;
	}
}
