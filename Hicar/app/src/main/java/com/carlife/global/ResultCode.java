package com.carlife.global;

public class ResultCode {

	public static String GetResult(int code){
		switch(code){
		case 0:return "成功";
		case 1:return "系统错误";
		case 10:return "无签名";
		case 11:return "签名错误";
		case 100:return "用户不存在";
		case 101:return "无法获取手机序列号";
		case 102:return "序列号不匹配";
		case 103:return "用户信息错误";
		case 104:return "违规被屏蔽";
		case 105:return "余额不足，请及时充值";
		case 201:return "上传定位失败";
		case 202:return "更改状态失败";
		case 203:return "上传经纬度错误";
		case 301:return "提交推送时间失败";
		case 302:return "订单不存在";
		case 303:return "更新呼叫客户/组长时间失败";
		case 304:return "没有查询到组长电话";
		case 305:return "更新到达时间失败";
		case 501:return "订单已回过";
		case 502:return "回单金额不能为空";
		case 503:return "回单金额不能为0";
		case 504:return "回单金额过大,请联系客服";
		case 505:return "回单失败";
		case 601:return "获取司机列表失败";
		case 602:return "获取会员卡号失败";
		case 603:return "提交意见反馈失败";
		case 701:return "当前有订单不能注销";
		case 801:return "手机号码不正确";
		case 802:return "售卡金额不正确";
		case 803:return "礼品卡不存在";
		case 804:return "此卡已售出";
		case 805:return "没有售此卡权限";
		case 806:return "已有订单不能创建新订单";
		}
		return "系统错误";
	}

}
