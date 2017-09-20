package com.carlife.model;

import java.io.Serializable;



public class DaijiaOrder implements Serializable{
	private String orderId;
	private String orderNo;
	private String name;
	private String comment_count;
	private String ordertime;
	private String comment_state;
	private String comment_content;
	private String id;
	private String number;
	private String driverId;
	private String ReceiveOrderTime;	
	private String OrderStatus;
	private String OrderAmountReal;
	private String DeparturePlaceReal;	
	private String DestinationReal;	
	private String Star;
	public void setStar(String p_Star)
	{
		this.Star=p_Star;
	}
	public String getStar()
	{
		return this.Star;
	}
	
	
	public void setOrderStatus(String p_OrderStatus)
	{
		this.OrderStatus=p_OrderStatus;
	}
	public String getOrderStatus()
	{
		return this.OrderStatus;
	}
	
	public void setOrderAmountReal(String orderAmountReal)
	{
		this.OrderAmountReal=orderAmountReal;
	}
	public String getOrderAmountReal()
	{
		return this.OrderAmountReal;
	}
	
	public void setDeparturePlaceReal(String p_DeparturePlaceReal)
	{
		this.DeparturePlaceReal=p_DeparturePlaceReal;
	}
	public String getDeparturePlaceReal()
	{
		return this.DeparturePlaceReal;
	}
	
	
	
	public void setDestinationReal(String p_DestinationReal)
	{
		this.DestinationReal=p_DestinationReal;
	}
	public String getDestinationReal()
	{
		return this.DestinationReal;
	}
	
	
	
	public String getReceiveOrderTime() {
		return ReceiveOrderTime;
	}

	public void setReceiveOrderTime(String receiveOrderTime) {
		this.ReceiveOrderTime = receiveOrderTime;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
	public void setOrderNo(String p_orderNo)
	{
		this.orderNo=p_orderNo;
	}
	public String getOrderNo()
	{
		return this.orderNo;
	}
	

	public String getDriverId() {
		return driverId;
	}

	public void setDriverId(String driverId) {
		this.driverId = driverId;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComment_count() {
		return comment_count;
	}

	public void setComment_count(String comment_count) {
		this.comment_count = comment_count;
	}

	public String getOrdertime() {
		return ordertime;
	}

	public void setOrdertime(String ordertime) {
		this.ordertime = ordertime;
	}

	public String getComment_state() {
		return comment_state;
	}

	public void setComment_state(String comment_state) {
		this.comment_state = comment_state;
	}

	public String getComment_content() {
		return comment_content;
	}

	public void setComment_content(String comment_content) {
		this.comment_content = comment_content;
	}

}
