package com.zhaoyi.walker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jianjia Liu on 2017/3/28.
 */

public class ResultFromServer {
    private static List<Map<String, Object>> hosInfo;
    private static List<Map<String, Object>> allDoctor;
    private static List<Map<String, Object>> doctor;
    private static List<Map<String, Object>> department;
    private static List<Map<String, Object>> fixedCityHos;
    private static List<Map<String, Object>> homeInfo;
    private static List<Map<String, Object>> result;
    private static List<Map<String, Object>> region;

    public static void initHosInfo() {
        if(hosInfo == null) {
            hosInfo = new ArrayList<Map<String, Object>>();
        }
        else {
            hosInfo.clear();
        }
    }
    public static List<Map<String, Object>> getHosInfo() {
        return hosInfo;
    }

    public static void addHosInfo(Map<String, Object> m) {
        hosInfo.add(m);
    }

    public static void initDoctor() {
        if(doctor == null) {
            doctor = new ArrayList<Map<String, Object>>();
        }
        else {
            doctor.clear();
        }
    }
    public static List<Map<String, Object>> getDoctor() {
        return doctor;
    }

    public static void initAllDoctor() {
        if(allDoctor == null) {
            allDoctor = new ArrayList<Map<String, Object>>();
        }
        else {
            allDoctor.clear();
        }
    }
    public static List<Map<String, Object>> getAllDoctor() {
        return allDoctor;
    }

    public static void addAllDoctor(Map<String, Object> m) {
        allDoctor.add(m);
    }

    public static void initDepartment() {
        if(department == null) {
            department = new ArrayList<Map<String, Object>>();
        }
        else {
            department.clear();
        }
    }
    public static List<Map<String, Object>> getDepartment() {
        return department;
    }

    public static void initFixedCityHos() {
        if(fixedCityHos == null) {
            fixedCityHos = new ArrayList<Map<String, Object>>();
        }
    }
    public static List<Map<String, Object>> getFixedCityHos() {
        return fixedCityHos;
    }

    public static void addFixedCityHos(Map<String, Object> m) {
        fixedCityHos.add(m);
    }

    public static void initHomeInfo() {
        if(homeInfo == null) {
            homeInfo = new ArrayList<Map<String, Object>>();
        }
        else {
            homeInfo.clear();
        }
    }
    public static List<Map<String, Object>> getHomeInfo() {
        return homeInfo;
    }

    public static void addHomeInfo(Map<String, Object> m) {
        homeInfo.add(m);
    }

    public static void initResult() {
        if(result == null) {
            result = new ArrayList<Map<String, Object>>();
        }
    }

    public static List<Map<String, Object>> getResult() {
        return result;
    }

    public static List<Map<String, Object>> getRegion() {
        return region;
    }

    public static void addRegion(Map<String, Object> m) {
        region.add(m);
    }

    public static void initRegion() {
        if(region == null) {
            region = new ArrayList<Map<String, Object>>();
        }
    }
}
