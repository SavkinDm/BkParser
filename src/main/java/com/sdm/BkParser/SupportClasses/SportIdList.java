package com.sdm.BkParser.SupportClasses;


import java.util.HashMap;

public class SportIdList {

    public static final String FOOTBALL = "football";
    public static final String BASKETBALL = "basketball";
    public static final String VOLLEYBALL = "volleyball";
    public static final String TENNIS = "tennis";
    public static final String HOCKEY = "hockey";

    private  HashMap<Integer, Integer> SportIdList;
    private   HashMap<Integer, String> SportList= new HashMap<>();



    public SportIdList() {
        SportIdList = new HashMap<>();
        SportList.put(1, FOOTBALL);
        SportList.put(3, BASKETBALL);
        SportList.put(9, VOLLEYBALL);
        SportList.put(4, TENNIS);
        SportList.put(2, HOCKEY);
    }

    public HashMap<Integer, Integer> getSportIdList() {
        return SportIdList;
    }

    public  void setSportIdList(HashMap<Integer, Integer> sportIdList) {
        SportIdList = sportIdList;
    }

    public  HashMap<Integer, String>  getSportList() {
        return SportList;
    }


    public   String getSportName(Integer id){
        String response;

        response =  SportList.get(SportIdList.get(id));
        if(response!=null)
            return response;
        else
            return "";

    }



}

