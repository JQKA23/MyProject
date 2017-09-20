package com.carlife.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.widget.Toast;

import com.baidu.mapapi.utils.OpenClientUtil;
import com.carlife.R;

public class Utili {

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
		case 5:
			return "回单中";
		}
	}

	// 获取司机驾龄
	public static String getDriverYears(String startDate) {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		startDate = startDate.replace('/', '-');
		Date issueDate;
		int driverYear = 5;
		try {
			issueDate = fmt.parse(startDate);
			Date now = new Date();
			driverYear = now.getYear() - issueDate.getYear();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return driverYear + "";
	}

	// 获取司机身份证号
	public static String getDriverIdNo(String str) {
		String strTemp = "";
		if (!str.equals("") && str.length() > 6)

			strTemp = str.substring(0, 6) + "*****"
					+ str.substring(str.length() - 4);
		return strTemp;
	}

	// 获取当前时间
	public static String getCurrentTime() {
		Date now = new Date();
		return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(now);
	}

	public static String getTimeSpan(String start, String end) {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startTime = null;
		Date endTime = null;
		try {
			startTime = fmt.parse(start);
			endTime = fmt.parse(end);
			long diff = endTime.getTime() - startTime.getTime();
			long minute = diff / (1000 * 60);
			return minute + "";
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "0";
	}

	private final static double EARTH_RADIUS = 6378137; // 地球半径(米)

	public static double GetDistance(String lng1, String lat1, String lng2,
			String lat2) {
		double radLat1 = rad(Double.parseDouble(lat1));
		double radLat2 = rad(Double.parseDouble(lat2));
		double a = radLat1 - radLat2;
		double b = rad(Double.parseDouble(lng1))
				- rad(Double.parseDouble(lng2));

		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		// s = Math.Round(s * 10000) / 10000;
		return s;
	}

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	// 订单状态
	public static String getOrderStatus(int orderStatus) {
		switch (orderStatus) {
		case 1:
			return "进行中";
		case 4:
			return "完成";
		case 5:
			return "作废";
		case 3:
			return "取消";
		case 6:
			return "完成";
		case 2:
			return "预约";
		case 7:
			return "进行中";
		default:
			return "未知状态";
		}
	}

	// 产生验证码
	public static String generateValid() {
		int temp = (int) (Math.random() * 9000);
		temp += 1000;
		return temp + "";
	}

	public static void ToastInfo(Context context, String info) {
		// Toast toast=Toast.makeText(context, info, Toast.LENGTH_SHORT);
		// toast.setGravity(Gravity.CENTER, 0, 0);
		// //创建图片视图对象
		// ImageView imageView= new ImageView(context);
		// //设置图片
		// imageView.setImageResource(R.drawable.icon);
		// LinearLayout.LayoutParams lp = new
		// LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
		// LinearLayout.LayoutParams.WRAP_CONTENT);
		// lp.setMargins(0, 0, 10, 0);
		// imageView.setLayoutParams(lp);
		// LinearLayout toastView = (LinearLayout) toast.getView();
		// //设置此布局为横向的
		// toastView.setOrientation(LinearLayout.HORIZONTAL);
		//
		// //将ImageView在加入到此布局中的第一个位置
		// toastView.addView(imageView, 0);
		// toast.show();

		// LayoutInflater inflater = (LayoutInflater) context
		// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// View layout = inflater.inflate(R.layout.toast, null);
		//
		// TextView title = (TextView) layout.findViewById(R.id.message);
		// title.setText(info);
		// Toast toast = new Toast(context.getApplicationContext());
		// toast.setGravity(Gravity.CENTER, 0, 0);
		// toast.setDuration(Toast.LENGTH_SHORT);
		// toast.setView(layout);
		// toast.show();

		Toast toast = Toast.makeText(context.getApplicationContext(), info,
				Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();

	}

	public static String GetJson(String str) {
		str = str.substring(1);
		str = str.substring(0, str.length() - 1);
		str = str.replace("\\", "");
		return str;
	}

	public static String getCurrentDate() {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		String mYear = String.valueOf(c.get(Calendar.YEAR)); // 获取当前年份
		String mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取当前月份
		String mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));// 获取当前月份的日期号码
		String mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
		if ("1".equals(mWay)) {
			mWay = "天";
		} else if ("2".equals(mWay)) {
			mWay = "一";
		} else if ("3".equals(mWay)) {
			mWay = "二";
		} else if ("4".equals(mWay)) {
			mWay = "三";
		} else if ("5".equals(mWay)) {
			mWay = "四";
		} else if ("6".equals(mWay)) {
			mWay = "五";
		} else if ("7".equals(mWay)) {
			mWay = "六";
		}
		return mMonth + "月" + mDay + "日" + " 星期" + mWay;
	}

	public static void showDialog(final Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("您尚未安装百度地图app或app版本过低，点击确认安装？");
		builder.setTitle("提示");
		builder.setPositiveButton(R.string.btn_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						OpenClientUtil.getLatestBaiduMapApp(context);
					}
				});

		builder.setNegativeButton(R.string.btn_cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		builder.create().show();

	}

}
