package com.sdm.BkParser.Service.Parsers.BetCity;

import java.util.HashMap;

public class BetCitySportIdList {


    public static final String FOOTBALL = "football";
    public static final String BASKETBALL = "basketball";
    public static final String VOLLEYBALL = "volleyball";
    public static final String TENNIS = "tennis";
    public static final String HOCKEY = "hockey";

    private HashMap<Integer, Integer> SportIdList;
    private   HashMap<Integer, String> SportList= new HashMap<>();


    public BetCitySportIdList() {


        SportIdList = new HashMap<>();
        SportList.put(1, FOOTBALL);
        SportList.put(3, BASKETBALL);
        SportList.put(12, VOLLEYBALL);
        SportList.put(2, TENNIS);
        SportList.put(7, HOCKEY);


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


    public    String getSportName(Integer id){
        String response;

        response =  SportList.get(id);
        if(response!=null)
            return response;
        else
            return "";

    }


}

