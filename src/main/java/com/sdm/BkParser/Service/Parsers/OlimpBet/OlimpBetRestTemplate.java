package com.sdm.BkParser.Service.Parsers.OlimpBet;


import com.sdm.BkParser.Entity.*;
import com.sdm.BkParser.Service.Parsers.Fonbet.CoefIdList;
import com.sdm.BkParser.Service.Parsers.ParserInt;
import com.sdm.BkParser.SupportClasses.Rest.NewRestTemplate;
import com.sdm.BkParser.SupportClasses.SportIdList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


@Service
public class OlimpBetRestTemplate implements ParserInt {

    private static final String FIRST_URL = "https://www.olimp.bet/api/champs";

    private static final String BK_NAME = "OlimpBet";

    private  static  HashMap<String, HashMap<Double, Double>> foras;
    private static final String SECOND_URL = "https://www.olimp.bet/apiru/prematch/champ/?id=";

    @Autowired
    NewRestTemplate newRestTemplate;


    public  HashMap<String, List<Event>> startParsing(){
        HashMap<String, List<Event>> response = new HashMap<>();

        List<Integer> footballChempsIds = getAllChempIds(1, "237e51cfe32e79c51c1e0a9297c8de5d");
        List<Integer> basketballChempsIds = getAllChempIds(5, "1689c5993aecefb3a96ea021d77e55d6");
        List<Integer> hockeyChempsIds = getAllChempIds(2,"9d04fd1493f7226cf303aa5f7fa57a3c");
        List<Integer> tennisChempsIds = getAllChempIds(3, "71679eff92c8a1ced8a6ee79dce93735");
        List<Integer> volleyballChempsIds = getAllChempIds(10, "605ea24e3e948d80231425c8862a3676");

        List<Event> footballEvents = getAllEvents(footballChempsIds, SportIdList.FOOTBALL);
        List<Event> basketballEvents = getAllEvents(basketballChempsIds, SportIdList.BASKETBALL);
        List<Event> hockeyEvents = getAllEvents(hockeyChempsIds, SportIdList.HOCKEY);
        List<Event> tennisEvents = getAllEvents(tennisChempsIds, SportIdList.TENNIS);
        List<Event> volleyballEvents = getAllEvents(volleyballChempsIds, SportIdList.VOLLEYBALL);

        response.put(SportIdList.FOOTBALL, footballEvents);
        response.put(SportIdList.BASKETBALL, basketballEvents);
        response.put(SportIdList.HOCKEY, hockeyEvents);
        response.put(SportIdList.TENNIS, tennisEvents);
        response.put(SportIdList.VOLLEYBALL, volleyballEvents);


        return response;
    }

    private  List<Event> getAllEvents(List<Integer> chempsIds, String kindOfSport) {
        List<Event> eventsFromChemp = new LinkedList<>();

        for (Integer chempId : chempsIds) {

            JSONObject response = newRestTemplate.getForJSON(SECOND_URL+ chempId);
            eventsFromChemp.addAll(downloadAndSaveEvents(response, kindOfSport));
        }


        return eventsFromChemp;
    }

    private List<Event> downloadAndSaveEvents(JSONObject resp, String kindOfSport) {
        List<Event> eventList = new LinkedList<>();

        JSONArray events = resp.getJSONArray("events");
        for (Object ob : events) {
            JSONObject object = (JSONObject) ob;
            Event event = getSingleEvent(object, kindOfSport);
            try{
                String team1 = event.getTeam1();
                String team2 = event.getTeam2();
                if(team1!=null && team2!=null && !team1.equals("")&& !team2.equals("")){
                    eventList.add(event);
                }
            }catch (Exception e){
                //logger.getLOGGER().log(Level.WARNING, "Error while downloading and saving events", e);
            }


        }



        return eventList;
    }

    private  Event getSingleEvent(JSONObject object, String kindOfSport) {
        int id = object.getInt("id");
        long startTime = object.getLong("start")/1000;
        String name = object.getString("name");
        Event event = null;
        try {
            String[] nam = name.split(" - ");
            String team1;
            String team2;
            team1 = nam[0];
            team2 = nam[1];

            JSONArray markets = object.getJSONArray("markets");
            // достаем основные коэфициенты 1 х 2 1х 12 х2
            HashMap<String, Double> coefsOnEvent = getMainCoefsOnThisEvent(markets);
            JSONObject allCoefs = newRestTemplate.getForJSON("https://www.olimp.bet/apiru/prematch/event/?id="+id);
            // достаем тоталы на это событие
            HashMap<String, HashMap<Double, Double>> totalsOnThisEvent = getTotalsAndForasOnThisEvent(markets, id, allCoefs);
            //достаем форы на это событие
            HashMap<String, HashMap<Double, Double>> forasOnThisEvent = foras;

            switch (kindOfSport) {
                case SportIdList.FOOTBALL:
                    event = new FootballMatch(id, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                    break;
                case SportIdList.BASKETBALL:
                    event = new BasketballMatch(id, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                    break;

                case SportIdList.VOLLEYBALL:
                    event = new VolleyballMatch(id, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                    break;

                case SportIdList.HOCKEY:
                    event = new HockeyMatch(id, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                    break;

                case SportIdList.TENNIS:
                    event = new TennisMatch(id, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                    break;

            }
        }catch (Exception e){
           // logger.getLOGGER().log(Level.WARNING, "Error while downloading and saving single event", e);

        }


        return event;
    }

    /*
    private static HashMap<String, HashMap<Double, Double>> getForasOnThisEvent(JSONArray markets, int id, JSONObject dopCoefs) {
        HashMap<String, HashMap<Double, Double>> foras = new HashMap<>();
        HashMap<Double, Double> team1Foras = new HashMap<>();
        HashMap<Double, Double> team2Foras = new HashMap<>();

        for (Object ob : markets) {
            JSONObject object = (JSONObject) ob;
            String check = object.getString("name");
            Double coef = Double.valueOf(object.getString("value"));
            Double param;
            switch (check){
                case "К1":
                    param = Double.valueOf(object.getString("param"));
                    team1Foras.put(param, coef);
                    break;
                case "К2":
                    param = Double.valueOf(object.getString("param"));
                    team2Foras.put(param, coef);
                    break;
            }
        }

        JSONArray mark = dopCoefs.getJSONArray("markets");
        for (Object ob : mark) {
            JSONObject object = (JSONObject) ob;
            int check = object.getInt("selection");
            int mar = object.getInt("market");
            if(mar ==4){
                Double coef = Double.valueOf(object.getString("value"));
                Double param;
                switch (check){
                    case 1:
                        param = Double.valueOf(object.getString("param"));
                        team1Foras.put(param, coef);
                        break;
                    case 2:
                        param = Double.valueOf(object.getString("param"));
                        team2Foras.put(param, coef);
                        break;
                }
            }

        }


        foras.put(CoefIdList.ALL_MATCH_TEAM_1_FORAS, team1Foras);
        foras.put(CoefIdList.ALL_MATCH_TEAM_2_FORAS, team2Foras);


        return foras;
    }
    */

    private static HashMap<String, HashMap<Double, Double>> getTotalsAndForasOnThisEvent(JSONArray markets, int id, JSONObject dopCoefs) {
        HashMap<String, HashMap<Double, Double>> totals = new HashMap<>();
        foras = new HashMap<>();
        HashMap<Double, Double> team1Foras = new HashMap<>();
        HashMap<Double, Double> team2Foras = new HashMap<>();

        HashMap<Double, Double> totalsO = new HashMap<>();
        HashMap<Double, Double> totalsU = new HashMap<>();

        for (Object ob : markets) {
            JSONObject object = (JSONObject) ob;
            String check = object.getString("name");
            Double coef = Double.valueOf(object.getString("value"));
            Double param;
            switch (check){
                case "ТотБ":
                     param = Double.valueOf(object.getString("param"));
                    totalsO.put(param, coef);
                    break;
                case "ТотМ":
                     param = Double.valueOf(object.getString("param"));
                    totalsU.put(param, coef);
                    break;
                case "К1":
                    param = Double.valueOf(object.getString("param"));
                    team1Foras.put(param, coef);
                    break;
                case "К2":
                    param = Double.valueOf(object.getString("param"));
                    team2Foras.put(param, coef);
                    break;
            }
        }

        JSONArray mark = dopCoefs.getJSONArray("markets");
        for (Object ob : mark) {
            JSONObject object = (JSONObject) ob;
            int check = object.getInt("outcomeType");
            int mar = object.getInt("market");
            int check1 = object.getInt("selection");
            switch (mar){
                case 5:
                    Double coef = Double.valueOf(object.getString("value"));
                    Double param;
                    switch (check){
                        case 3:
                            param = Double.valueOf(object.getString("param"));
                            totalsO.put(param, coef);
                            break;
                        case 2:
                            param = Double.valueOf(object.getString("param"));
                            totalsU.put(param, coef);
                            break;
                    }
                    break;

                case 4:
                    Double coef1 = Double.valueOf(object.getString("value"));
                    Double param1;
                    switch (check1){
                        case 1:
                            param1 = Double.valueOf(object.getString("param"));
                            team1Foras.put(param1, coef1);
                            break;
                        case 2:
                            param1 = Double.valueOf(object.getString("param"));
                            team2Foras.put(param1, coef1);
                            break;
                    }
                    break;
            }
        }

        foras.put(CoefIdList.ALL_MATCH_TEAM_1_FORAS, team1Foras);
        foras.put(CoefIdList.ALL_MATCH_TEAM_2_FORAS, team2Foras);
        totals.put(CoefIdList.ALL_MATCH_TOTALS_O, totalsO);
        totals.put(CoefIdList.ALL_MATCH_TOTALS_U, totalsU);

        return totals;
    }

    private  HashMap<String, Double> getMainCoefsOnThisEvent(JSONArray markets) {
        HashMap<String, Double> coefs = new HashMap<>();
        for (Object ob : markets) {
            JSONObject object = (JSONObject) ob;
            String check = object.getString("name");
            Double coef = Double.valueOf(object.getString("value"));
            switch (check){
                case "П1":
                    coefs.put(CoefIdList.ALL_MATCH_TEAM_1_WIN, coef);
                    break;

                case "Х":
                    coefs.put(CoefIdList.ALL_MATCH_DRAW, coef);
                    break;

                case "П2":
                    coefs.put(CoefIdList.ALL_MATCH_TEAM_2_WIN, coef);
                    break;

                case "12":
                    coefs.put(CoefIdList.ALL_MATCH_TEAM_1_OR_TEAM_2_WIN, coef);
                    break;

                case "Х2":
                    coefs.put(CoefIdList.ALL_MATCH_DRAW_OR_TEAM_2_WIN, coef);
                    break;

                case "1Х":
                    coefs.put(CoefIdList.ALL_MATCH_DRAW_OR_TEAM_1_WIN, coef);
                    break;

            }
        }
        return coefs;
    }

    private  List<Integer> getAllChempIds(int i, String key) {
        List<Integer> responce= new ArrayList<>();


        String params = "{\"lang_id\":\"0\",\"platforma\":\"SITE_CUPIS\",\"sport_id\":\"" + i + "\",\"live\":0}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(params.length());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-bf", "7b3874512c00963561d144a0feceec12");
        headers.set("x-token", key);

        JSONObject mainresp = newRestTemplate.postForJSON( FIRST_URL, headers, params);
        JSONArray arr = mainresp.getJSONArray("data");
        for (Object ob : arr) {
            JSONObject object = (JSONObject) ob;
            int io = object.getInt("io");
            if(io==0){
                int id = object.getInt("cid");
                responce.add(id);
            }
        }



        return responce;
    }


}
