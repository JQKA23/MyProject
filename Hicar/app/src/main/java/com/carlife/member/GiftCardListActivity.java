package com.carlife.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.model.GiftCard;
import com.carlife.utility.CustomDialog;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Utili;

public class GiftCardListActivity extends Activity implements OnClickListener {

	private Button btn_back;
	private final static int Load_fail = 101;
	private final static int Load_success = 102;
	private CustomProgressDialog cpd;
	private List<GiftCard> giftcardList = new ArrayList<GiftCard>();
	private List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	private ListView lv_giftCard;

	private String mobile = "";
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.giftcardlist);

		lv_giftCard = (ListView) findViewById(R.id.lv_giftCard);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		mobile = sp.getString(Const.BindMobile, "");
		context = this;
	}

	@Override
	protected void onResume() {
		getGiftCards();
		super.onResume();
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btn_back:
			this.finish();
			break;

		}
	}

	private void getGiftCards() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("mobile", mobile);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("mobile", mobile);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/GetGiftCardList", params,
				new AjaxCallBack<Object>() {

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(Load_fail);
					}

					@Override
					public void onLoading(long count, long current) {

						super.onLoading(count, current);

					}

					@Override
					public void onSuccess(Object t) {
						super.onSuccess(t);
						String jsonMessage = Utili.GetJson("" + t);
						try {

							giftcardList = new ArrayList<GiftCard>();
							JSONArray jsonArray = new JSONArray(jsonMessage);
							for (int i = 0; i < jsonArray.length(); i++) {
								GiftCard card = new GiftCard();
								JSONObject obj = (JSONObject) jsonArray.opt(i);
								card.setCardNo(obj.getString("CardNo"));
								card.setAmount(obj.getString("Amount"));
								giftcardList.add(card);
							}

							handler.sendEmptyMessage(Load_success);

						} catch (JSONException e) {
							System.out.print(e);
							handler.sendEmptyMessage(Load_fail);

						}

					}

					@Override
					public AjaxCallBack<Object> progress(boolean progress,
							int rate) {

						return super.progress(progress, rate);
					}

				});

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (cpd != null && cpd.isShowing()) {
				cpd.dismiss();
			}
			switch (msg.what) {
			case Load_fail:
				Toast.makeText(context, "获取失败，请稍后重试", Toast.LENGTH_SHORT)
						.show();
				break;
			case Load_success:
				if (giftcardList.size() > 0) {
					prepareData();
					SimpleAdapter listAdapter = new SimpleAdapter(context,
							data, R.layout.giftcarditem, new String[] {
									"CardNo", "Amount" }, new int[] {
									R.id.txt_cardNo, R.id.txt_cardAmount });
					lv_giftCard.setAdapter(listAdapter);
					lv_giftCard
							.setOnItemClickListener(new OnItemClickListener() {
								@Override
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id) {
									GiftCard card = giftcardList.get(position);
									Intent i = new Intent(context,
											GiftCardActivity.class);
									i.putExtra("cardNo", card.getCardNo());
									i.putExtra("amount", card.getAmount());
									startActivity(i);

								}
							});
				} else {
					alertInfo();
				}
				break;
			}

		}
	};

	private void alertInfo() {
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setMessage("您还没有礼品卡，如需购买，请拨打400-800-1018");
		builder.setTitle("提示");
		builder.setPositiveButton("立即购买",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Intent.ACTION_DIAL, Uri
								.parse("tel:4008001018"));
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
						dialog.dismiss();
					}
				});

		builder.setNegativeButton("取消",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	private void prepareData() {
		data = new ArrayList<Map<String, String>>();
		for (int i = 0; i < giftcardList.size(); i++) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("CardNo", giftcardList.get(i).getCardNo());
			item.put("Amount", giftcardList.get(i).getAmount());
			data.add(item);
		}
	}
}
