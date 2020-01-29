package com.example.im_chat.other;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

public class ActivityManager {
    private List<Activity> activities = null;
    private static ActivityManager instance = new ActivityManager();

    private ActivityManager() {
        activities = new LinkedList<Activity>();
    }

    /**
     * 单例模式中获取唯一的MyApplication实例     *     * @return
     */
    public static ActivityManager getInstance() {
        return instance;
    }

    // 添加Activity到容器中
    public void addActivity(Activity activity) {
        if (activities != null && activities.size() > 0) {
            if (!activities.contains(activity)) {
                activities.add(activity);
            }
        } else {
            activities.add(activity);
        }
    }

    // 遍历所有Activity并finish
    public void finishActivity() {
        if (activities != null && activities.size() > 0) {
            for (Activity activity : activities) {
                if (!activity.isFinishing()) {
                    activity.finish();
                }
            }
        }
        activities.clear();
    }

    /**
     * 获得当前显示的Activity
     *
     * @return
     */
    public String getRunningActivityName() {
        if (null != activities && activities.size() > 0) {
            return activities.get(activities.size() - 1).toString();
        }
        return null;
    }

    //获得当前正在显示的activity
    public Activity getRunningActivity() {
        if (null != activities && activities.size() > 0) {
            return activities.get(activities.size() - 1);
        }
        return null;
    }

    public void finishActivityByName(String name){
        if (activities != null && activities.size() > 0) {
            for (Activity activity : activities){
                if(null != activity){
                    String activityName = activity.toString();
                    if(activityName.contains(name) && !activity.isFinishing()){
                        activity.finish();
                    }
                }
            }
        }
    }
}