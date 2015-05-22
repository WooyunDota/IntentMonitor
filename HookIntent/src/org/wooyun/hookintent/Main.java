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
import android.content.ContextWrapper;


public class Main implements IXposedHookLoadPackage {
    public static String mainTag = "IHook";

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

        mainTag = mainTag + "-" + lpparam.packageName;
        //系统 app 不监控
        if ((lpparam.appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
            return;

        //hook 启动 activity android.app.Activity#startActivityForResult
        findAndHookMethod("android.app.Activity", lpparam.classLoader, "startActivityForResult", Intent.class, int.class, Bundle.class, new XC_MethodHook() {
                    //		findAndHookMethod("android.app.Instrumentation", lpparam.classLoader, "execStartActivity", Context.class, IBinder.class, IBinder.class, Activity.class,
//	            Intent.class, int.class, Bundle.class, new XC_MethodHook() {
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                        Intent it = (Intent) param.args[0];
                        Bundle bund = (Bundle) param.args[2];
                        String tag = "startActivity";
                        getIntentAndBundle(it, bund, tag);
                    }
                }
        );

        //hook 即将启动 activity android.app.PendingIntent#getActivity
        findAndHookMethod("android.app.PendingIntent", lpparam.classLoader, "getActivity", Context.class, int.class, Intent.class, int.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {

                        Intent it = (Intent) param.args[2];
                        String tag = "getActivity";
                        getIntentAndBundle(it, null, tag);
                    }
                }
        );


        //由android.content.Context改为ContextWrapper,抽象方法不能 hook
        //hook sendBroadcast(Intent intent)
        //hook sendBroadcast(Intent intent, String receiverPermission)
        //hook sendOrderedBroadcast(Intent intent,String receiverPermission)
        //sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver,Handler scheduler, int initialCode, String initialData, Bundle initialExtras)
        //hook sendStickyBroadcast(Intent intent)
        //sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver,Handler scheduler, int initialCode, String initialData,Bundle initialExtras)
        //removeStickyBroadcast(Intent intent)
        //hook registerReceiver(BroadcastReceiver receiver, IntentFilter filter)
        //hook registerReceiver(BroadcastReceiver receiver, IntentFilter filter,String broadcastPermission, Handler scheduler)

        findAndHookMethod("android.content.ContextWrapper", lpparam.classLoader, "sendBroadcast", Intent.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {

                        Intent it = (Intent) param.args[0];
                        String tag = "sendBroadcast";
                        getIntentAndBundle(it, null, tag);

                    }

                }
        );

        findAndHookMethod("android.content.ContextWrapper", lpparam.classLoader, "sendBroadcast", Intent.class, String.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {

                        Intent it = (Intent) param.args[0];
                        String tag = "sendBroadcast";
                        getIntentAndBundle(it, null, tag);

                        String permission = (String) param.args[1];
                        if (permission != null)
                            Log.i(mainTag, tag + " permission:" + permission);

                    }

                }
        );
        findAndHookMethod("android.content.ContextWrapper", lpparam.classLoader, "sendStickyBroadcast", Intent.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {

                        Intent it = (Intent) param.args[0];
                        String tag = "sendStickyBroadcast";
                        getIntentAndBundle(it, null, tag);

                    }
                }
        );

        findAndHookMethod("android.content.ContextWrapper", lpparam.classLoader, "sendOrderedBroadcast", Intent.class, String.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {

                        Intent it = (Intent) param.args[0];
                        String tag = "sendOrderedBroadcast";
                        getIntentAndBundle(it, null, tag);
                        String permission = (String) param.args[1];
                        if (permission != null)
                            Log.i(mainTag, tag + " permission:" + permission);
                    }
                }
        );

        findAndHookMethod("android.content.ContextWrapper", lpparam.classLoader, "registerReceiver", BroadcastReceiver.class, IntentFilter.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {



                        BroadcastReceiver broad = (BroadcastReceiver) param.args[0];
                        if(broad != null)
                            handleBroad(broad);

                        IntentFilter filter = (IntentFilter) param.args[1];
                        if(filter != null)
                            handleFilter(filter);
                    }

                }
        );


        findAndHookMethod("android.content.ContextWrapper", lpparam.classLoader, "registerReceiver", BroadcastReceiver.class, IntentFilter.class, String.class, Handler.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {



                        BroadcastReceiver broad = (BroadcastReceiver) param.args[0];
                        if(broad != null)
                            handleBroad(broad);

                        IntentFilter filter = (IntentFilter) param.args[1];
                        if(filter != null)
                            handleFilter(filter);

                        String per = (String) param.args[2];

                        if (per != null)
                            Log.i(mainTag, "registerReceiver permission : " + per);
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
                        getIntentAndBundle(it, null, tag);
                    }
                }
        );
        findAndHookMethod("android.content.ContextWrapper", lpparam.classLoader, "bindService", Intent.class, ServiceConnection.class, int.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {

                        Intent it = (Intent) param.args[0];
                        String tag = "bindService";
                        getIntentAndBundle(it, null, tag);
                    }
                }
        );

    }

    //处理 IntentFilter ,filter 中可能有多个 action/scheme ....
    public void handleFilter(IntentFilter filter) {

        String tag = "registerReceiver";


            StringBuilder sb = new StringBuilder();
            Iterator<String> actions = filter.actionsIterator();
            String action = null;
            int i = 0;
            while (actions.hasNext())
            {
                action = actions.next();
                Log.i(mainTag, tag + " action" + i + ":" + action);
                i++;
            }

//            String category = filter.getCategory(i);
//            if (category != null)
//                Log.i(mainTag, tag + " category"+i+":" + category);

//            String scheme = filter.getDataScheme(0);
//            if (scheme != null)
//                Log.i(mainTag, tag + " scheme"+i+":" + scheme);



    }


    public void handleFilter(IntentFilter filter,String tag) {


        String action = filter.getAction(0);
        if (action != null)
            Log.i(mainTag, tag + " action:" + action);

        String category = filter.getCategory(0);
        if (category != null)
            Log.i(mainTag, tag + " category:" + category);

        String scheme = filter.getDataScheme(0);
        if (scheme != null)
            Log.i(mainTag, tag + " scheme: " + scheme);

    }

    public void handleBroad(BroadcastReceiver broad) {
        String tag = "registerReceiver";
        Log.i(mainTag, tag + " class: " + broad.getClass().toString());
    }


    //处理 intent 和 bundle
    public void getIntentAndBundle(Intent it, Bundle bund, String tag) {

        //获取 bundle
        if (bund != null) {
            String NEWLINE = "\n";
            StringBuilder stringBuilder = new StringBuilder();
            Bundle intentBundle = bund;
            Set<String> keySet = intentBundle.keySet();
            int count = 0;

            for (String key : keySet) {
                count++;
                Object thisObject = intentBundle.get(key);
                stringBuilder.append(tag + " Bundle EXTRA ").append(count)
                        .append(":");
                String thisClass = thisObject.getClass().getName();
                if (thisClass != null) {
                    stringBuilder.append(tag + "Bundle Class: ").append(thisClass)
                            .append(NEWLINE);
                }
                stringBuilder.append(tag + " Bundle Key: ").append(key).append(NEWLINE);

                if (thisObject instanceof String || thisObject instanceof Long
                        || thisObject instanceof Integer
                        || thisObject instanceof Boolean) {
                    stringBuilder.append(tag + " Bundle Value: " + thisObject.toString())
                            .append(NEWLINE);
                } else if (thisObject instanceof ArrayList) {
                    stringBuilder.append(tag + " Bundle Values:");
                    ArrayList thisArrayList = (ArrayList) thisObject;
                    for (Object thisArrayListObject : thisArrayList) {
                        stringBuilder.append(thisArrayListObject.toString()
                                + NEWLINE);
                    }
                }
            }
            Log.i(mainTag, tag + " Bundle EXTRA:" + stringBuilder);
        }

        //取得 action
        if (it.getAction() != null) {
            Log.i(mainTag, tag + " Action:" + it.getAction());
        }

        //取得 flag
        if (it.getFlags() != 0) {
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
            Log.i(mainTag, tag + "  Flags:" + stringBuilder);
        }

        //取得 data
        if (it.getDataString() != null)
            Log.i(mainTag, tag + " Data:" + it.getDataString());

        //取得 type
        if (it.getType() != null)
            Log.i(mainTag, tag + " Type:" + it.getType());

        //取得 Component
        if (it.getComponent() != null) {
            ComponentName cp = it.getComponent();
            Log.i(mainTag, tag + " Component:" + cp.getPackageName() + "/" + cp.getClassName());
        }

        if (it.getExtras() != null) {
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
                        stringBuilder.append(NEWLINE).append(tag + " EXTRA ").append(count)
                                .append(":").append(NEWLINE);
                        String thisClass = thisObject.getClass().getName();
                        if (thisClass != null) {
                            stringBuilder.append(tag + " Class: ").append(thisClass)
                                    .append(NEWLINE);
                        }
                        stringBuilder.append(tag + " Key: ").append(key).append(NEWLINE);

                        if (thisObject instanceof String || thisObject instanceof Long
                                || thisObject instanceof Integer
                                || thisObject instanceof Boolean) {
                            stringBuilder.append(tag + " Value: " + thisObject.toString())
                                    .append(NEWLINE);
                        } else if (thisObject instanceof ArrayList) {
                            stringBuilder.append(tag + " Values:");
                            ArrayList thisArrayList = (ArrayList) thisObject;
                            for (Object thisArrayListObject : thisArrayList) {
                                stringBuilder.append(thisArrayListObject.toString()
                                        + NEWLINE);
                            }
                        }
                    }
                }

                Log.i(mainTag, tag + " EXTRA: \n" + stringBuilder);
            } catch (Exception e) {
                stringBuilder.append(tag + " BUNDLE:");
                stringBuilder.append(tag + " Error extracting extras");
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

}
