package com.sdm.BkParser.Service.Parsers.LigaStavok;




import com.sdm.BkParser.SupportClasses.SportIdList;

import java.util.HashMap;

public class LigaStavokSportIdLIst {


    public static final String FOOTBALL = SportIdList.FOOTBALL;
    public static final String BASKETBALL =  SportIdList.BASKETBALL;
    public static final String VOLLEYBALL = SportIdList.VOLLEYBALL;
    public static final String TENNIS =  SportIdList.TENNIS;
    public static final String HOCKEY = SportIdList.HOCKEY;

    private static HashMap<Integer, Integer> SportList;
    private   HashMap<Integer, String> SpList = new HashMap<>();


    public LigaStavokSportIdLIst() {


        SpList = new HashMap<>();
        SpList.put(33, FOOTBALL);
        SpList.put(25, BASKETBALL);
        SpList.put(128, VOLLEYBALL);
        SpList.put(34, TENNIS);
        SpList.put(31, HOCKEY);


    }


    public HashMap<Integer, Integer> getSportIdList() {
        return SportList;
    }

    public  void setSportIdList(HashMap<Integer, Integer> sportIdList) {
        SportList = sportIdList;
    }

    public  HashMap<Integer, String> getSpList() {
        return SpList;
    }


    public    String getSportName(Integer id){
        String response;

        response =  SpList.get(id);
        if(response!=null)
            return response;
        else
            return "";

    }


}
