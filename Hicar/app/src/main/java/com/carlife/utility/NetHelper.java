package com.carlife.utility;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class NetHelper {
	//网络是否可用
	public static boolean isNetworkAvailable(Context con) {
		boolean result=GetNetWorkAvailable(con);
		if(result==false){			
			 //toggleWiFi(con, true);  
			result=toggleMobileData(con, true);  	       
		}
		return result;
	}
	
	private static boolean GetNetWorkAvailable(Context con){
		ConnectivityManager cm = (ConnectivityManager) con
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null)
			return false;
		NetworkInfo netinfo = cm.getActiveNetworkInfo();
		if (netinfo == null) {
			return false;
		}
		if (netinfo.isConnected()) {
			return true;
		}		
		return false;
	}
	
	// Wifi是否可用  
	public static boolean isWifiEnable(Context context) {  
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);  
        return wifiManager.isWifiEnabled();  
    }  
    
    // Gps是否可用  
	public static boolean isGpsEnable(Context context) {  
        LocationManager locationManager =   
                ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));  
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);  
    }  
	
	  //设置是否打开Wifi  
    private static void toggleWiFi(Context context, boolean enabled) {  
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);  
        wifiManager.setWifiEnabled(enabled);  
    }  
      
    /** 
     * 设置是否打开移动网络 
     *  
     * 但没有直接的API可调用,但是我们发现: 
     * 在ConnectivityManager中有一隐藏的方法setMobileDataEnabled() 
     * 源码如下: 
     * public void setMobileDataEnabled(boolean enabled) { 
     *   try { 
     *      mService.setMobileDataEnabled(enabled); 
     *   } catch (RemoteException e) { 
     *      } 
     * } 
     *  
     * 这里的重点就是mService,查看其声明: 
     * private IConnectivityManager mService; 
     * 继续查看源码可知IConnectivityManager为了一个AIDL(接口interface IConnectivityManager) 
     *  
     *  
     * 调用过程: 
     * ConnectivityManager中有一隐藏的方法setMobileDataEnabled() 
     * 在setMobileDataEnabled()中调用了IConnectivityManager中的setMobileDataEnabled(boolean) 
     *  
     * 所以我们首先需要反射出ConnectivityManager类的成员变量mService(IConnectivityManager类型) 
     */  
    private static boolean toggleMobileData(Context context, boolean enabled) {    
        ConnectivityManager connectivityManager =   
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
          
        //ConnectivityManager类    
        Class<?> connectivityManagerClass = null;  
        //ConnectivityManager类中的字段    
        Field connectivityManagerField = null;  
          
          
        //IConnectivityManager接口  
        Class<?> iConnectivityManagerClass = null;  
        //IConnectivityManager接口的对象  
        Object iConnectivityManagerObject = null;  
        //IConnectivityManager接口的对象的方法  
        Method setMobileDataEnabledMethod = null;  
          
        try {  
            //取得ConnectivityManager类  
            connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());  
            //取得ConnectivityManager类中的字段mService  
            connectivityManagerField = connectivityManagerClass.getDeclaredField("mService");  
            //取消访问私有字段的合法性检查   
            //该方法来自java.lang.reflect.AccessibleObject  
            connectivityManagerField.setAccessible(true);  
              
              
            //实例化mService  
            //该get()方法来自java.lang.reflect.Field  
            //一定要注意该get()方法的参数:  
            //它是mService所属类的对象  
            //完整例子请参见:  
            //http://blog.csdn.net/lfdfhl/article/details/13509839  
            iConnectivityManagerObject = connectivityManagerField.get(connectivityManager);  
            //得到mService所属接口的Class  
            iConnectivityManagerClass = Class.forName(iConnectivityManagerObject.getClass().getName());  
            //取得IConnectivityManager接口中的setMobileDataEnabled(boolean)方法  
            //该方法来自java.lang.Class.getDeclaredMethod  
            setMobileDataEnabledMethod =   
            iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);  
            //取消访问私有方法的合法性检查   
            //该方法来自java.lang.reflect.AccessibleObject  
            setMobileDataEnabledMethod.setAccessible(true);  
            //调用setMobileDataEnabled方法  
            setMobileDataEnabledMethod.invoke(iConnectivityManagerObject,enabled);  
            return true;
        } catch (ClassNotFoundException e) {     
            e.printStackTrace();    
        } catch (NoSuchFieldException e) {     
            e.printStackTrace();    
        } catch (SecurityException e) {     
            e.printStackTrace();    
        } catch (NoSuchMethodException e) {     
            e.printStackTrace();    
        } catch (IllegalArgumentException e) {     
            e.printStackTrace();    
        } catch (IllegalAccessException e) {     
            e.printStackTrace();    
        } catch (InvocationTargetException e) {     
            e.printStackTrace();    
        }   
        return false;
    }  
  
  
	
	
}