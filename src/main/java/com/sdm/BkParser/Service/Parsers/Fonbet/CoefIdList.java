package com.sdm.BkParser.Service.Parsers.Fonbet;

import java.util.HashMap;

public class CoefIdList{


    private HashMap<Integer, String> CoefList;

    //общие
    //весь матч
    public static final String ALL_MATCH_TEAM_1_WIN = "ALL_MATCH_TEAM_1_WIN";
    public static final String ALL_MATCH_TEAM_2_WIN = "ALL_MATCH_TEAM_2_WIN";
    public static final String ALL_MATCH_DRAW = "ALL_MATCH_DRAW";
    public static final String ALL_MATCH_DRAW_OR_TEAM_1_WIN = "ALL_MATCH_DRAW_OR_TEAM_1_WIN";
    public static final String ALL_MATCH_DRAW_OR_TEAM_2_WIN = "ALL_MATCH_DRAW_OR_TEAM_2_WIN";
    public static final String ALL_MATCH_TEAM_1_OR_TEAM_2_WIN = "ALL_MATCH_TEAM_1_OR_TEAM_2_WIN";

    public static final String ALL_MATCH_TOTALS_O = "ALL_MATCH_TOTALS_O";
    public static final String ALL_MATCH_TOTALS_U = "ALL_MATCH_TOTALS_U";

    public static final String ALL_MATCH_TEAM_1_FORAS = "ALL_MATCH_TEAM_1_FORAS";
    public static final String ALL_MATCH_TEAM_2_FORAS = "ALL_MATCH_TEAM_2_FORAS";


    // 1ый тайм/период/сет
    public static final String FIRST_SET_TEAM_1_WIN = "FIRST_SET_TEAM_1_WIN";
    public static final String FIRST_SET_TEAM_2_WIN = "FIRST_SET_TEAM_2_WIN";

    // 2ой тайм/период/сет

    //Футбол
    public static final String TEAM_1_AND_TEAM_2_WILL_SCORE_A_GOAL_YES = "TEAM_1_AND_TEAM_2_WILL_SCORE_A_GOAL_YES";
    public static final String TEAM_1_AND_TEAM_2_WILL_SCORE_A_GOAL_NO = "TEAM_1_AND_TEAM_2_WILL_SCORE_A_GOAL_NO";
    public static final String TEAM_1_WILL_SCORE_A_GOAL_FIRST_YES = "TEAM_1_WILL_SCORE_A_GOAL_FIRST_YES";
    public static final String TEAM_1_WILL_SCORE_A_GOAL_FIRST_NO = "TEAM_1_WILL_SCORE_A_GOAL_FIRST_NO";
    public static final String TEAM_2_WILL_SCORE_A_GOAL_FIRST_YES = "TEAM_2_WILL_SCORE_A_GOAL_FIRST_YES";
    public static final String TEAM_2_WILL_SCORE_A_GOAL_FIRST_NO = "TEAM_2_WILL_SCORE_A_GOAL_FIRST_NO";

    //теннис

    //баскетбол

    //волейбол

    //хоккей



    public CoefIdList() {
        CoefList= new HashMap<>();

        //общие
        CoefList.put(921, ALL_MATCH_TEAM_1_WIN);
        CoefList.put(923, ALL_MATCH_TEAM_2_WIN);
        CoefList.put(970, FIRST_SET_TEAM_1_WIN);
        CoefList.put(971, FIRST_SET_TEAM_2_WIN);
        CoefList.put(922, ALL_MATCH_DRAW);
        CoefList.put(924, ALL_MATCH_DRAW_OR_TEAM_1_WIN);
        CoefList.put(925, ALL_MATCH_DRAW_OR_TEAM_2_WIN);
        CoefList.put(1571, ALL_MATCH_TEAM_1_OR_TEAM_2_WIN);

        //CoefList.put(927, ALL_MATCH_FORA_1);
        //CoefList.put(928, ALL_MATCH_FORA_2);

       // CoefList.put(930, ALL_MATCH_TOTAL_O);
        //CoefList.put(931, ALL_MATCH_TOTAL_U);




        //футбол специфические кэфы
        CoefList.put(1300, TEAM_1_AND_TEAM_2_WILL_SCORE_A_GOAL_YES);  // поменять айди
        CoefList.put(1301, TEAM_1_AND_TEAM_2_WILL_SCORE_A_GOAL_NO);
        CoefList.put(1302, TEAM_1_WILL_SCORE_A_GOAL_FIRST_YES);
        CoefList.put(1303, TEAM_1_WILL_SCORE_A_GOAL_FIRST_NO);
        CoefList.put(1304, TEAM_2_WILL_SCORE_A_GOAL_FIRST_YES);
        CoefList.put(1305, TEAM_2_WILL_SCORE_A_GOAL_FIRST_NO);

        //теннис специфические кэфы


        // баскетбол  специфические кэфы


        // волейбол специфические кэфы

        // хоккей специфические кэфы

    }

    public int getSize(){

        return CoefList.size();
    }


    public boolean containsKey(int key){

        if( CoefList.containsKey(key))
            return true;
        else
            return false;

    }



    public HashMap<Integer, String> getCoefList() {
        return CoefList;
    }

    public  String getValue(Integer key){
        String response;
        response = CoefList.get(key);
        if(response!=null)
            return response;
        else
            return "-1";
    }
}
