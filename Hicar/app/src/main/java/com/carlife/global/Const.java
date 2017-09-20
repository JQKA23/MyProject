package com.carlife.global;

public class Const {
	// bugly
	public static final String BuglyAppID = "900009465";
	public static final String APPKEY = "appKey";
	public static final String APPKEY_STR = "CarLifeAndroid";
	public static final String APPSECRET_STR = "934yh2y*&%63yyEHw";

	public static final long startDelay = 1000; // 进入程序延迟1秒

	public static final String spFirstPref = "first_pref";// 第一次进入
	public static final String isFirstIn = "isFirstIn";
	// public static final LatLng BEIJING = new LatLng(39.90403, 116.407525);//
	// 北京市经纬度

	public static final String spBindMobile = "spBindMobile"; // 绑定的手机号
	public static final String BindMobile = "BindMobile";

	public static final String spCallMobile = "spConsumerMobile"; // 使用人手机号
	public static final String CallMobile = "ConsumerMobile";

	public static final String spLocation = "spLocation";
	public static final String Location = "Location";

	public static final String spOrderCount = "spOrderCount";
	public static final String OrderCount = "OrderCount";

	public static final String spValidCode = "spValidCode";// 验证码
	public static final String validCode = "validCode";// 验证码

	public static final String spPushMessageList = "spPushMessageList";
	public static final String PushMessage = "PushMessage";

	public static final String spFeedback = "spfeedback";// 意见反馈
	public static final String feedback = "feedbackContent";// 内容

	public static final String API_SERVICES_ADDRESS = "http://appservice.1018a.com/HiCar.svc";// 正式
	// public static final String API_SERVICES_ADDRESS =
	// "http://116.90.87.31:121/HiCar.svc";// 测试

	public static final String cache_ads = "cache_ads";

	// 当前城市
	public static final String spCity = "spCity";
	public static final String City = "City";

	// weixin
	public static final String wxAPPID = "wx4ad756963e87e30a";
	public static final String wxAPP_SECRET = "08603055b8a76f0c7719b3f9604fe6cb";
	public static final String wxMCH_ID = "1261624801";
	public static final String wxKey = "123456789wYujymj6bnvcfsqplkj8765";

	// 微信签名 dfb1aa1196227d5d8462102c531d9696
	// debug下签名：55c982a0f5e24282ef273367311dc81e

	// 签名后的SHA1：
	// 83:7B:9A:8D:C0:1E:92:A8:69:46:A2:2A:07:A8:53:B4:BC:84:A0:9A

	// =================记得更改AndroidManifest.xml中的字符串
	// 百度地图
	// 测试：VC11kBnqDNo7NGBMA4KI2LfL
	// 签名后：YtnofcRQqmnjDFEn8HxI946k

	// 快钱

	// 获取司机状态
	public static String getDriverState(String state) {
		switch (Integer.parseInt(state)) {
		case 0:
			return "下班";
		case 1:
			return "空闲";
		default:
			return "忙碌";
		case 4:
			return "结伴返程";
		}
	}

	// 存储用过的地址
	public static final String spAddressList = "spAddressList";
	public static final String AddressList = "spAddressList"; // 地址@维度@经度
	// 存储手工搜索的地址和经纬度
	public static final String spLocatedHand = "spLocatedHand";
	public static final String LoatedHandAddress = "LoatedHandAddress";
	public static final String longitudeByHand = "longitudeByHand";
	public static final String latitudeByHand = "latitudeByHand";

	/* 支付宝参数 */
	public static final String alipayPId = "2088201325821482";
	public static final String alipaySeller = "zhifuba1018@126.com";
	// 商户私钥，pkcs8格式
	public static final String alipay_RSA_PRIVATE = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBANyG131UWTHXOz2BdUgrbsu/DMDLourM9ILmlIF12H2/zzulT7XWVzdvDUf+v0//w3MR3n+U+5HMaEm2voFqwtbx4E2+/a/QiVOfgHCEnrXLoZRW2h5/3iUDDAaIMHFG76qY6t9Kwie2VG/7pCFL70PU5CE9Z9e85myP9ENyXk7jAgMBAAECgYANgOA/F2Qizigdewjgc3BkX317bLy8HfK29UGQr9Oi15Tr9RJtcwOCLHWmu+2hC2s/Z4+1Rj0lT1vvb5Y4vH3TqH+TAmxrgRWM8jK54Ss5GYA/be/ic4GYgz15jFyh/OrHcWxkhhtTsiFRO/SXkEA9p8zmZ/iD4qBgUsU/tF8Z+QJBAPHWm2G7MM1wArZX52XgKv+pTEhbzoabtHgT8749ESh7gV5ItxXFoKNytELrN+2mJp9rOr7TPfPPPOu7UCQ/BmUCQQDpcMDYan+ugZ16XxLKmGCFEvOFAAq70v4b+Hg6ydL5eAeImTq9ISg/qdPhsDPjXp5CQlguZenmayP1RDYezOenAkB4ptCuWW1nkNJJlmFjegr5scTU9Lh8f4HxxkJ8TosY30UTfMOaoRYbdUZpfGBSt+nc7upH+auWz3VdRf4dlzPhAkAbkOgE5labiczJ1Y5HhETpkfpVu1KJKXo+XVH9RzX1pZnmJIoOKWsyUWCm5wLJl7PCiUkWeYxsXDyLyvlFG9dDAkBns7nYBl0pG7X6IlIR4PThsAII30dc4YH4Rwfl+y7DR4YGcG4mu5i5I99RRF17UqY7l9i+j+Qy4W/9Q+k1f/dR";
	// 支付宝公钥
	public static final String alipay_RSA_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

}
