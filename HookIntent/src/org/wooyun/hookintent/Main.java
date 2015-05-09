package org.wooyun.hookintent;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public class Main implements IXposedHookLoadPackage {
	public static String mainTag = "IntentHook";

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		
		//系统 app 不监控
		if ((lpparam.appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
			return;
		
//		if(!lpparam.packageName.equals(""))
//			return;
//		for (int i = 0; i < systemXgoogle.length; i++) {
//			
//			if(lpparam.packageName.equals(systemXgoogle[i]))
//				return;
//			
//		}

		//hook 启动 activity android.app.Activity#startActivityForResult
        findAndHookMethod("android.app.Activity", lpparam.classLoader, "startActivityForResult",Intent.class , int.class , Bundle.class , new XC_MethodHook() {
//		findAndHookMethod("android.app.Instrumentation", lpparam.classLoader, "execStartActivity", Context.class, IBinder.class, IBinder.class, Activity.class,
//	            Intent.class, int.class, Bundle.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                Intent it = (Intent) param.args[0];
                Bundle bund = (Bundle) param.args[2];
                String tag = "startActivity";
                getIntentAndBundle(it,bund,tag);
                }
        }
        );
        
        //hook 即将启动 activity android.app.PendingIntent#getActivity
		findAndHookMethod("android.app.PendingIntent", lpparam.classLoader, "getActivity", Context.class,int.class, Intent.class,int.class, new XC_MethodHook() {

			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {

				Intent it = (Intent) param.args[2];
				String tag = "getActivity";
				getIntentAndBundle(it,null,tag);
				}
		}
		);
		
		
		//由android.content.Context改为ContextWrapper, 
		//hook sendBroadcast(Intent intent)  
		//hook sendBroadcast(Intent intent, String receiverPermission)
		//hook sendOrderedBroadcast(Intent intent,String receiverPermission)
		//sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver,Handler scheduler, int initialCode, String initialData, Bundle initialExtras)
		//hook sendStickyBroadcast(Intent intent)
		//sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver,Handler scheduler, int initialCode, String initialData,Bundle initialExtras)
		//removeStickyBroadcast(Intent intent) 
		//hook registerReceiver(BroadcastReceiver receiver, IntentFilter filter)
		//registerReceiver(BroadcastReceiver receiver, IntentFilter filter,String broadcastPermission, Handler scheduler)

		findAndHookMethod("android.content.ContextWrapper", lpparam.classLoader, "sendBroadcast", Intent.class, new XC_MethodHook() {

			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {

				Intent it = (Intent) param.args[0];
				String tag = "sendBroadcast";
				getIntentAndBundle(it,null,tag);
				
				}
			
		}
		);
		
		findAndHookMethod("android.content.ContextWrapper", lpparam.classLoader, "sendBroadcast", Intent.class,String.class, new XC_MethodHook() {

			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {

				Intent it = (Intent) param.args[0];
				String tag = "sendBroadcast";
				getIntentAndBundle(it,null,tag);
				
				String permission = (String) param.args[1];
				if( permission != null )
					Log.i(mainTag, tag +" permission:" + permission );
				
				}
			
		}
		);
		findAndHookMethod("android.content.ContextWrapper", lpparam.classLoader, "sendStickyBroadcast", Intent.class, new XC_MethodHook() {

			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {

				Intent it = (Intent) param.args[0];
				String tag = "sendStickyBroadcast";
				getIntentAndBundle(it,null,tag);
				
				}
		}
		);
		
		findAndHookMethod("android.content.ContextWrapper", lpparam.classLoader, "sendOrderedBroadcast", Intent.class,String.class, new XC_MethodHook() {

			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {

				Intent it = (Intent) param.args[0];
				String tag = "sendOrderedBroadcast";
				getIntentAndBundle(it,null,tag);
				String permission = (String) param.args[1];
				if( permission != null )
					Log.i(mainTag, tag +" permission:" + permission );
				}
		}
		);
		
		findAndHookMethod("android.content.ContextWrapper", lpparam.classLoader, "registerReceiver", BroadcastReceiver.class , IntentFilter.class , new XC_MethodHook() {

			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {

				IntentFilter filter = (IntentFilter) param.args[1];
				handleFilter(filter);
				
				BroadcastReceiver broad = (BroadcastReceiver) param.args[0];
				handleBroad(broad);
				
				}
			
		}
		);
		
		
		//由android.content.Context改为ContextWrapper
		//hook startService(Intent service)
		//stopService(Intent name)
		//hook bindService(Intent service, ServiceConnection conn,int flags)
		//unbindService(ServiceConnection conn) 
		findAndHookMethod("android.content.ContextWrapper", lpparam.classLoader, "startService", Intent.class, new XC_MethodHook() {

			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {

				Intent it = (Intent) param.args[0];
				String tag = "startService";
				getIntentAndBundle(it,null,tag);
				}
		}
		);
		findAndHookMethod("android.content.ContextWrapper", lpparam.classLoader, "bindService", Intent.class,ServiceConnection.class,int.class, new XC_MethodHook() {

			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {

				Intent it = (Intent) param.args[0];
				String tag = "bindService";
				getIntentAndBundle(it,null,tag);
				}
		}
		);
		
		
	}
	
	//处理 IntentFilter
	public void handleFilter (IntentFilter filter){
		
		String tag = "registerReceiver";
		
		String action = filter.getAction(0);
		if (action != null)
			Log.i(mainTag, tag + " action:" + action);
		
		String category = filter.getCategory(0);
		if (category != null)
			Log.i(mainTag, tag + " category:" + category);
		
		String scheme = filter.getDataScheme(0);
		if (scheme != null )
			Log.i(mainTag, tag + " scheme: " + scheme);
				
	}
	
	public void handleBroad(BroadcastReceiver broad){
		String tag = "registerReceiver";
		Log.i(mainTag,tag + " class: " + broad.getClass().toString());
	}
	
	
	//处理 intent 和 bundle
	public void getIntentAndBundle(Intent it,Bundle bund,String tag){

		//获取 bundle
		if(bund != null){
			String NEWLINE = "\n";
			StringBuilder stringBuilder = new StringBuilder();
			Bundle intentBundle = bund;
			Set<String> keySet = intentBundle.keySet();
			int count = 0;

			for (String key : keySet) {
				count++;
				Object thisObject = intentBundle.get(key);
				stringBuilder.append(tag+" Bundle EXTRA ").append(count)
						.append(":");
				String thisClass = thisObject.getClass().getName();
				if (thisClass != null) {
					stringBuilder.append(tag + "Bundle Class: ").append(thisClass)
							.append(NEWLINE);
				}
				stringBuilder.append(tag+" Bundle Key: ").append(key).append(NEWLINE);

				if (thisObject instanceof String || thisObject instanceof Long
						|| thisObject instanceof Integer
						|| thisObject instanceof Boolean) {
					stringBuilder.append(tag+" Bundle Value: " + thisObject.toString())
							.append(NEWLINE);
				} else if (thisObject instanceof ArrayList) {
					stringBuilder.append(tag+" Bundle Values:");
					ArrayList thisArrayList = (ArrayList) thisObject;
					for (Object thisArrayListObject : thisArrayList) {
						stringBuilder.append(thisArrayListObject.toString()
								+ NEWLINE);
					}
				}
			}
			Log.i(mainTag, tag+" Bundle EXTRA:" + stringBuilder);
		}
		
		//取得 action
		if(it.getAction()!=null){
//			for (int i = 0; i < actionFilter.length; i++) {
//				if(it.getAction() == actionFilter[i] )
//					return ;
//			}
			Log.i(mainTag,tag+" Action:" + it.getAction());
		}

		//取得 flag
		if(it.getFlags()!=0){
			String NEWLINE = "\n";
			StringBuilder stringBuilder = new StringBuilder();
			ArrayList<String> flagsStrings = getFlags(it);
			if (flagsStrings.size() > 0) {
				for (String thisFlagString : flagsStrings) {
					stringBuilder.append(thisFlagString).append(NEWLINE);
				}
			} else {
				stringBuilder.append("NONE").append(NEWLINE);
			}
			Log.i(mainTag, tag+"  Flags:" + stringBuilder);
		}

		//取得 data
		if(it.getDataString()!=null)
			Log.i(mainTag, tag+" Data:" + it.getDataString());

		//取得 type
		if(it.getType()!=null)
			Log.i(mainTag, tag+" Type:" + it.getType());

		//取得 Component
		if(it.getComponent()!=null){
			ComponentName cp = it.getComponent();
			Log.i(mainTag, tag+" Component:" + cp.getPackageName() + "/" + cp.getClassName());
		}

		if(it.getExtras()!=null){
			String NEWLINE = "\n";
			StringBuilder stringBuilder = new StringBuilder();
			try {
				Bundle intentBundle = it.getExtras();
				if (intentBundle != null) {
					Set<String> keySet = intentBundle.keySet();
					int count = 0;

					for (String key : keySet) {
						count++;
						Object thisObject = intentBundle.get(key);
						stringBuilder.append(NEWLINE).append(tag+" EXTRA ").append(count)
								.append(":").append(NEWLINE);
						String thisClass = thisObject.getClass().getName();
						if (thisClass != null) {
							stringBuilder.append(tag+" Class: ").append(thisClass)
									.append(NEWLINE);
						}
						stringBuilder.append(tag+" Key: ").append(key).append(NEWLINE);

						if (thisObject instanceof String || thisObject instanceof Long
								|| thisObject instanceof Integer
								|| thisObject instanceof Boolean) {
							stringBuilder.append(tag+" Value: " + thisObject.toString())
									.append(NEWLINE);
						} else if (thisObject instanceof ArrayList) {
							stringBuilder.append(tag+" Values:");
							ArrayList thisArrayList = (ArrayList) thisObject;
							for (Object thisArrayListObject : thisArrayList) {
								stringBuilder.append(thisArrayListObject.toString()
										+ NEWLINE);
							}
						}
					}
				}

				Log.i(mainTag, tag+" EXTRA: \n" + stringBuilder);
			} catch (Exception e) {
				stringBuilder.append(tag+" BUNDLE:");
				stringBuilder.append(tag+" Error extracting extras");
				e.printStackTrace();
			}
		}
	}

	//分离 flag
	private ArrayList<String> getFlags(Intent editableIntent) {
		ArrayList<String> flagsStrings = new ArrayList<String>();
		int flags = editableIntent.getFlags();
		Set<Entry<Integer, String>> set = FLAGS_MAP.entrySet();
		Iterator<Entry<Integer, String>> i = set.iterator();
		while (i.hasNext()) {
			Entry<Integer, String> thisFlag = (Entry<Integer, String>) i.next();
			if ((flags & thisFlag.getKey()) != 0) {
				flagsStrings.add(thisFlag.getValue());
			}
		}
		return flagsStrings;
	}


	// 映射 flag
	private static final Map<Integer, String> FLAGS_MAP = new HashMap<Integer, String>() {
		{
			put(new Integer(Intent.FLAG_GRANT_READ_URI_PERMISSION),
					"FLAG_GRANT_READ_URI_PERMISSION");
			put(new Integer(Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
					"FLAG_GRANT_WRITE_URI_PERMISSION");
			put(new Integer(Intent.FLAG_FROM_BACKGROUND),
					"FLAG_FROM_BACKGROUND");
			put(new Integer(Intent.FLAG_DEBUG_LOG_RESOLUTION),
					"FLAG_DEBUG_LOG_RESOLUTION");
			put(new Integer(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES),
					"FLAG_EXCLUDE_STOPPED_PACKAGES");
			put(new Integer(Intent.FLAG_INCLUDE_STOPPED_PACKAGES),
					"FLAG_INCLUDE_STOPPED_PACKAGES");
			put(new Integer(Intent.FLAG_ACTIVITY_NO_HISTORY),
					"FLAG_ACTIVITY_NO_HISTORY");
			put(new Integer(Intent.FLAG_ACTIVITY_SINGLE_TOP),
					"FLAG_ACTIVITY_SINGLE_TOP");
			put(new Integer(Intent.FLAG_ACTIVITY_NEW_TASK),
					"FLAG_ACTIVITY_NEW_TASK");
			put(new Integer(Intent.FLAG_ACTIVITY_MULTIPLE_TASK),
					"FLAG_ACTIVITY_MULTIPLE_TASK");
			put(new Integer(Intent.FLAG_ACTIVITY_CLEAR_TOP),
					"FLAG_ACTIVITY_CLEAR_TOP");
			put(new Integer(Intent.FLAG_ACTIVITY_FORWARD_RESULT),
					"FLAG_ACTIVITY_FORWARD_RESULT");
			put(new Integer(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP),
					"FLAG_ACTIVITY_PREVIOUS_IS_TOP");
			put(new Integer(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS),
					"FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS");
			put(new Integer(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT),
					"FLAG_ACTIVITY_BROUGHT_TO_FRONT");
			put(new Integer(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED),
					"FLAG_ACTIVITY_RESET_TASK_IF_NEEDED");
			put(new Integer(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY),
					"FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY");
			put(new Integer(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET),
					"FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET");
			put(new Integer(Intent.FLAG_ACTIVITY_NO_USER_ACTION),
					"FLAG_ACTIVITY_NO_USER_ACTION");
			put(new Integer(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
					"FLAG_ACTIVITY_REORDER_TO_FRONT");
			put(new Integer(Intent.FLAG_ACTIVITY_NO_ANIMATION),
					"FLAG_ACTIVITY_NO_ANIMATION");
			put(new Integer(Intent.FLAG_ACTIVITY_CLEAR_TASK),
					"FLAG_ACTIVITY_CLEAR_TASK");
			put(new Integer(Intent.FLAG_ACTIVITY_TASK_ON_HOME),
					"FLAG_ACTIVITY_TASK_ON_HOME");
			put(new Integer(Intent.FLAG_RECEIVER_REGISTERED_ONLY),
					"FLAG_RECEIVER_REGISTERED_ONLY");
			put(new Integer(Intent.FLAG_RECEIVER_REPLACE_PENDING),
					"FLAG_RECEIVER_REPLACE_PENDING");
			//put(new Integer(0x10000000),"FLAG_RECEIVER_FOREGROUND");
			//put(new Integer(0x08000000),"FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT");
			put(new Integer(0x04000000), 
					"FLAG_RECEIVER_BOOT_UPGRADE");
			//put(new Integer(0x00080000),"FLAG_ACTIVITY_NEW_DOCUMENT");
			put(new Integer(0x00002000),
					"FLAG_ACTIVITY_RETAIN_IN_RECENTS");
			put(new Integer(0x00000040),
					"FLAG_GRANT_PERSISTABLE_URI_PERMISSION");
			put(new Integer(0x00000080),
					"FLAG_GRANT_PREFIX_URI_PERMISSION");
			//put(new Integer(0x08000000),"FLAG_RECEIVER_NO_ABORT");
			
		}
	};
	
	public boolean isThird(String appname,Context context){
		
		PackageManager manager = context.getPackageManager();
		List<ApplicationInfo>  info = manager.getInstalledApplications(0);
		ArrayList<String> pname = new ArrayList<String>();
		Iterator<ApplicationInfo> iterator = info.iterator();
		while (iterator.hasNext()) {
			ApplicationInfo applicationInfo = (ApplicationInfo) iterator.next();
			if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
				String name = applicationInfo.packageName;
				pname.add(name);
			}
		}
		if(pname.contains(appname))
			return true;
		else
			return false;
	};
	
/**	
	//android sdk Broadcast Action 
	private static String[] actionFilter = 
		{"android.intent.action.AIRPLANE_MODE"  //system,飞行模式 
		//,"android.intent.action.ALL_APPS"  //列出所有可用app
		//,"android.intent.action.ANSWER"   //处理来电
		,"android.intent.action.APPLICATION_RESTRICTIONS_CHANGED" //system, Sent after application restrictions are changed. 
		//,"android.intent.action.APP_ERROR"    //应用 crash 后点击 report 后发送
		//,"android.intent.action.ASSIST"  //辅助动作
		//,"android.intent.action.ATTACH_DATA"  //
		,"android.intent.action.BATTERY_CHANGED"  //system,粘性广播动作,电池状态改变:充电状态,电池量
		,"android.intent.action.BATTERY_LOW" //system,低电量提醒
		,"android.intent.action.BATTERY_OKAY"  //system,电池脱离低电量状态
		,"android.intent.action.BOOT_COMPLETED" //system,广播动作,开机启动
		//,"android.intent.action.BUG_REPORT"  //Activity Action,开启bug反馈界面
		//,"android.intent.action.CALL"  //Activity Action 启动拨号界面
		//,"android.intent.action.CALL_BUTTON"  //Activity Action,启动拨号后的解密
		,"android.intent.action.CAMERA_BUTTON" //Camera被点击
		,"android.intent.action.CLOSE_SYSTEM_DIALOGS"  //
		,"android.intent.action.CONFIGURATION_CHANGED"  //system,静态接收器不能接收此广播,设备配置改变
		,"android.intent.action.DATE_CHANGED"  //日期改变
		,"android.intent.action.DEVICE_STORAGE_LOW"  // system,粘性广播,设备剩余存储低
		,"android.intent.action.DEVICE_STORAGE_OK"  //system,设备存储恢复正常
		,"android.intent.action.DOCK_EVENT"  //粘性广播,changes in the physical docking state
		,"android.intent.action.DREAMING_STARTED"  //设备休眠
		,"android.intent.action.DREAMING_STOPPED"  //休眠结束
		,"android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE"  //system, sdcard上的应用可用
		,"android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE"  //system
		,"android.intent.action.GTALK_CONNECTED"  //A GTalk connection has been established.
		,"android.intent.action.GTALK_DISCONNECTED"  //disconnected
		,"android.intent.action.HEADSET_PLUG"  //
		,"android.intent.action.INPUT_METHOD_CHANGED"  //输入法改变
		,"android.intent.action.LOCALE_CHANGED"  //system,位置改变
		,"android.intent.action.MANAGE_PACKAGE_STORAGE"  //	
		,"android.intent.action.MEDIA_BAD_REMOVAL"  //暴力移出 sdcard
		,"android.intent.action.MEDIA_BUTTON"  //
		,"android.intent.action.MEDIA_CHECKING"  //
		,"android.intent.action.MEDIA_EJECT"  //sdcard弹出前广播
		,"android.intent.action.MEDIA_MOUNTED" //
		,"android.intent.action.MEDIA_NOFS"
		,"android.intent.action.MEDIA_REMOVED"
		,"android.intent.action.MEDIA_SCANNER_FINISHED"
		,"android.intent.action.MEDIA_SCANNER_SCAN_FILE"
		,"android.intent.action.MEDIA_SCANNER_STARTED" 
		,"android.intent.action.MEDIA_SHARED"
		,"android.intent.action.MEDIA_UNMOUNTABLE"
		,"android.intent.action.MEDIA_UNMOUNTED"
		,"android.intent.action.MY_PACKAGE_REPLACED" //system,新版 app 安装替换旧版 app
		,"android.intent.action.NEW_OUTGOING_CALL"  //system,接收需要 PROCESS_OUTGOING_CALLS 权限, 呼出电话,EXTRA_PHONE_NUMBER 呼叫号码,有序广播可以设置接收priority
		,"android.intent.action.PACKAGE_CHANGED"    //system
		,"android.intent.action.PACKAGE_ADDED"  	//system,新 app 安装
		,"android.intent.action.PACKAGE_DATA_CLEARED"  //system
		,"android.intent.action.PACKAGE_FIRST_LAUNCH"  //system
		,"android.intent.action.PACKAGE_FULLY_REMOVED"  //system
		, "android.intent.action.PACKAGE_INSTALL"  //system  ,api 14 废弃
		, "android.intent.action.PACKAGE_NEEDS_VERIFICATION"  //system ,app 需要验证
		,"android.intent.action.PACKAGE_REMOVED"  //system
		, "android.intent.action.PACKAGE_REPLACED" //system
		,"android.intent.action.PACKAGE_RESTARTED" //system
		,"android.intent.action.PACKAGE_VERIFIED"  //system
		,"android.intent.action.ACTION_POWER_DISCONNECTED"  //system ,断开电源
		,"android.intent.action.PROVIDER_CHANGED"  //content providers 数据发生变化
		, "android.intent.action.REBOOT"  //system
		,"android.intent.action.SCREEN_OFF"  //system ,关闭屏幕
		,"android.intent.action.SCREEN_ON" //system ,点亮屏幕
		,"android.intent.action.ACTION_SHUTDOWN"  //关机
		,"android.intent.action.TIMEZONE_CHANGED" //system ,时区改变
		,"android.intent.action.TIME_SET"
		,"android.intent.action.TIME_TICK" //system,时间改变,每分钟发一次,静态接收器无法接收
		,"android.intent.action.UID_REMOVED"   // system 
		,"android.intent.action.UMS_CONNECTED" //api 14 废弃 替换成android.os.storage.StorageEventListener,The device has entered USB Mass Storage mode
		,"android.intent.action.UMS_DISCONNECTED" //The device has exited USB Mass Storage mode.
		, "android.intent.action.USER_PRESENT"  // Sent when the user is present after device wakes up (e.g when the keyguard is gone).
		, "android.intent.action.WALLPAPER_CHANGED" //api 16 废弃,
		// google action 
		,"com.google.android.gcm.DISCONNECTED"
		,"com.google.android.gms.icing.INDEX_SERVICE"
		,"com.google.android.gms.icing.START_STICKY"
		,"com.google.android.gms.icing.LIGHTWEIGHT_INDEX_SERVICE"
		,"com.google.android.gms.icing.LIGHTWEIGHT_WORKER_SERVICE"
		,"com.google.android.gms.icing.IME_NOTIFICATION"
		,"com.google.android.gms.auth.DATA_PROXY"
		,"com.google.android.location.internal.GMS_NLP"
		,"com.google.android.gms.usagereporting.service.START"
		,"com.google.android.gms.droidguard.service.START"
		,"com.google.android.gms.cast.service.DEVICE_SCANNER_INTENT"
		,"com.google.android.checkin.CHECKIN_COMPLETE"
		,"com.google.android.gms.playlog.service.START"
		,"com.google.android.gms.playlog.service.INTENT"
		,"com.google.android.gms.games.service.ASYNC"
		,"com.google.android.gms.INITIALIZE"
		,"com.google.android.gms.common.stats.START"
		,"com.google.android.gms.wearable.ACTION_WEARABLE_APP_PACKAGE_ADDED"
		,"com.google.android.gms.location.reporting.SETTINGS_CHANGED"
		,"com.google.android.gms.wearable.BIND"
		,"com.google.android.email.EXCHANGE_INTENT"
		};
	
		
	private static String[] systemXgoogle = {
		//"com.google.process.gapps"
		"com.google.android.GoogleCamera"
		,"com.google.android.apps.genie.geniewidget"
		,"com.google.android.apps.inputmethod.hindi"
		,"com.google.android.apps.walletnfcrel"
		,"com.google.android.backuptransport"
		,"com.google.android.calendar"
		,"com.google.android.configupdater"
		,"com.google.android.deskclock"
		,"com.google.android.dialer"
		,"com.google.android.email"
		,"com.google.android.exchange"
		,"com.google.android.feedback"
		,"com.google.android.gallery3d"
		,"com.google.android.gm"
		,"com.google.android.gms"
		,"com.google.android.googlequicksearchbox"
		,"com.google.android.gsf"
		,"com.google.android.gsf.login"
		,"com.google.android.inputmethod.latin"
		,"com.google.android.launcher"
		,"com.google.android.marvin.talkback"
		,"com.google.android.music"
		,"com.google.android.onetimeinitializer"
		,"com.google.android.partnersetup"
		,"com.google.android.setupwizard"
		,"com.google.android.street"
		,"com.google.android.syncadapters.contacts"
		,"com.google.android.tag"
		,"com.google.android.tts"
		,"com.google.android.videos"
		
		,"com.android.backupconfirm"
		,"com.android.bluetooth"
		,"com.android.browser.provider"
		,"com.android.calculator2"
		,"com.android.cellbroadcastreceiver"
		,"com.android.certinstaller"
		,"com.android.chrome"
		,"com.android.contacts"
		,"com.android.defcontainer"
		,"com.android.documentsui"
		,"com.android.dreams.basic"
		,"com.android.externalstorage"
		,"com.android.htmlviewer"
		,"com.android.inputdevices"
		,"com.android.keychain"
		,"com.android.keyguard"
		,"com.android.location.fused"
		,"com.android.musicfx"
		,"com.android.nfc"
		,"com.android.packageinstaller"
		,"com.android.pacprocessor"
		,"com.android.phasebeamorange"
		,"com.android.phone"
		,"com.android.providers.calendar"
		,"com.android.providers.contacts"
		,"com.android.providers.downloads"
		,"com.android.providers.downloads.ui"
		,"com.android.providers.media"
		,"com.android.providers.partnerbookmarks"
		,"com.android.providers.settings"
		,"com.android.providers.telephony"
		,"com.android.providers.userdictionary"
		,"com.android.proxyhandler"
		,"com.android.settings"
		,"com.android.sharedstoragebackup"
		,"com.android.shell"
		,"com.android.stk"
		,"com.android.systemui"
		,"com.android.vending"
		,"com.android.vpndialogs"
		,"com.android.wallpaper.livepicker"
		,"com.android.wallpapercropper"
	}; 
**/	
}
