package com.zhaoyi.walker.model;

/**
 * Created by jianjia Liu on 2017/4/1.
 */

public class CurrentHosInfo {
    private static String curHos;
    private static String curDepartment;
    private static String curDoctor;
    private static String curCity;
    private static String curCost;
    private static String curDoctorRole;
    private static String curHosAddress;

    public static String getCurHos() { return curHos; }
    public static String getCurDepartment() { return curDepartment; }
    public static String getCurDoctor() { return curDoctor; }
    public static String getCurCity() { return curCity; }
    public static String getCurCost() { return curCost; }
    public static String getCurDoctorRole() { return curDoctorRole; }
    public static String getCurHosAddress() { return curHosAddress; }

    public static void setCurHos(String hos) { curHos = hos; }
    public static void setCurDepartment(String department) { curDepartment = department; }
    public static void setCurDoctor(String doctor) { curDoctor = doctor; }
    public static void setCurCity(String city) { curCity = city; }
    public static void setCurCost(String cost) { curCost = cost; }
    public static void setCurDoctorRole(String role) { curDoctorRole = role; }
    public static void setCurHosAddress(String address) { curHosAddress = address; }
}
