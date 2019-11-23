package com.sdm.BkParser.Service.Parsers.Melbet;

import com.sdm.BkParser.Entity.*;
import com.sdm.BkParser.Service.Parsers.Fonbet.CoefIdList;
import com.sdm.BkParser.Service.Parsers.ParserInt;
import com.sdm.BkParser.SupportClasses.Rest.NewRestTemplate;
import com.sdm.BkParser.SupportClasses.SportIdList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Service
public class MelBetRestTemplate implements ParserInt {
    public static final String BK_NAME = "MelBet365";
    private static final String URL_FOOTBALL = "https://melbet365.com/LineFeed/Get1x2_VZip?sports=1&count=10000&tf=10000&mode=4&cyberFlag=2&partner=94";
    private static final String URL_BASKETBALL = "https://melbet365.com/LineFeed/Get1x2_VZip?sports=3&count=10000&tf=10000&mode=4&cyberFlag=2&partner=94";
    private static final String URL_VOLLEYBALL = "https://melbet365.com/LineFeed/Get1x2_VZip?sports=6&count=10000&tf=10000&mode=4&cyberFlag=2&partner=94";
    private static final String URL_TENNIS = "https://melbet365.com/LineFeed/Get1x2_VZip?sports=4&count=10000&tf=10000&mode=4&cyberFlag=2&partner=94";
    private static final String URL_HOCKEY = "https://melbet365.com/LineFeed/Get1x2_VZip?sports=2&count=10000&tf=10000&mode=4&cyberFlag=2&partner=94";
    private final static String SECOND_URL = "https://melbet365.com/LineFeed/GetGameZip?id=";
    private static HashMap<String, HashMap<Double, Double>> foras;


    @Autowired
    NewRestTemplate newRestTemplate;
    /*
        основная функция парсинга
    */
    @Override
    public  HashMap<String, List<Event>> startParsing() {
        HashMap<String, List<Event>> EventList = new HashMap<>();
        JSONObject footballResponse, hockeyResponse, volleybollResponse, basketballResponse, tennisResponse;

        footballResponse = newRestTemplate.getForJSON(URL_FOOTBALL);
        hockeyResponse = newRestTemplate.getForJSON(URL_HOCKEY);
        volleybollResponse = newRestTemplate.getForJSON(URL_VOLLEYBALL);
        basketballResponse = newRestTemplate.getForJSON(URL_BASKETBALL);
        tennisResponse = newRestTemplate.getForJSON(URL_TENNIS);

        EventList.put(SportIdList.FOOTBALL, getAllEvents(footballResponse, SportIdList.FOOTBALL));
        EventList.put(SportIdList.HOCKEY, getAllEvents(hockeyResponse, SportIdList.HOCKEY));
        EventList.put(SportIdList.VOLLEYBALL, getAllEvents(volleybollResponse, SportIdList.VOLLEYBALL));
        EventList.put(SportIdList.BASKETBALL, getAllEvents(basketballResponse, SportIdList.BASKETBALL));
        EventList.put(SportIdList.TENNIS, getAllEvents(tennisResponse, SportIdList.TENNIS));

        return EventList;


    }


    private  List<Long> getAllIds(JSONObject footballResponse) {
        List<Long> ids = new LinkedList<>();

        JSONArray events = footballResponse.getJSONArray("Value");
        for (Object ev : events) {
            JSONObject event = (JSONObject) ev;
            ids.add(Long.valueOf(String.valueOf(event.getInt("CI"))));
        }

        return ids;

    }

    private  List<Event> getAllEvents(JSONObject jsonObject, String kindOfSort) {
        List<Event> response = new LinkedList<>();
        List<Long> idList = getAllIds(jsonObject);
        for (Long id : idList) {
            try {
                Event event = downloadAndSaveEvent(id, kindOfSort);
                response.add(event);
            } catch (Exception e) {
                //logger.getLOGGER().log(Level.WARNING, "Error while downloading and saving events", e);
            }

        }

        return response;
    }

    private  Event downloadAndSaveEvent(Long id, String kindOfSport) {

        JSONObject response = newRestTemplate.getForJSON(SECOND_URL+id);
        JSONObject ev = response.getJSONObject("Value");
        String team1 = ev.getString("O1");
        String team2 = ev.getString("O2");
        long startTime = Long.parseLong(String.valueOf(ev.getInt("S")));
        HashMap<String, Double> coefsOnEvent = getMainCoefsOnThisEvent(ev);
        HashMap<String, HashMap<Double, Double>> totalsOnThisEvent = getTotalsAndForasOnThisEvent(ev);
        HashMap<String, HashMap<Double, Double>> forasOnThisEvent = foras;
        Event event = null;
         switch (kindOfSport){
             case SportIdList.FOOTBALL:
                 event = new FootballMatch(1, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent,forasOnThisEvent);
                 break;
             case SportIdList.BASKETBALL:
                 event = new BasketballMatch(1, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                 break;

             case SportIdList.VOLLEYBALL:
                 event = new VolleyballMatch(1, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                 break;

             case SportIdList.HOCKEY:
                 event = new HockeyMatch(1, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                 break;

             case SportIdList.TENNIS:
                 event = new TennisMatch(1, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                 break;
         }
        return event;
    }

    private  HashMap<String, Double> getMainCoefsOnThisEvent(JSONObject ev) {
        HashMap<String, Double> mainCoefs = new HashMap<>();
        //  JSONArray Value = ev.getJSONArray("C");
        //  for (Object ob : Value) {

        //JSONObject coefBlock = (JSONObject) ev;
        JSONArray coefss = ev.getJSONArray("E");
        // JSONObject coefs = ev.getJSONObject("E");
        for (Object ob : coefss) {
            JSONObject ee = (JSONObject) ob;
            int check = ee.getInt("G");
            if (check == 1 || check == 8) {

                    int type = ee.getInt("T");
                    double coef = ee.getDouble("C");
                    switch (type) {
                        case 1:
                            mainCoefs.put(CoefIdList.ALL_MATCH_TEAM_1_WIN, coef);
                            break;
                        case 2:
                            mainCoefs.put(CoefIdList.ALL_MATCH_DRAW, coef);
                            break;
                        case 3:
                            mainCoefs.put(CoefIdList.ALL_MATCH_TEAM_2_WIN, coef);
                            break;
                        case 4:
                            mainCoefs.put(CoefIdList.ALL_MATCH_DRAW_OR_TEAM_1_WIN, coef);
                            break;
                        case 5:
                            mainCoefs.put(CoefIdList.ALL_MATCH_TEAM_1_OR_TEAM_2_WIN, coef);
                            break;
                        case 6:
                            mainCoefs.put(CoefIdList.ALL_MATCH_DRAW_OR_TEAM_2_WIN, coef);
                            break;
                    }
                }
            }


        return mainCoefs;
    }

    private HashMap<String, HashMap<Double, Double>> getTotalsAndForasOnThisEvent(JSONObject ev) {
        HashMap<String, HashMap<Double, Double>> totals = new HashMap<>();
        HashMap<Double, Double> totalsO = new HashMap<>();
        HashMap<Double, Double> totalsU = new HashMap<>();
        HashMap<Double, Double> forasTeam1 = new HashMap<>();
        HashMap<Double, Double> forasTeam2 = new HashMap<>();

        foras=new HashMap<>();

        JSONArray coefss = ev.getJSONArray("E");

        for (Object ob : coefss) {
            JSONObject ee = (JSONObject) ob;
            int check = ee.getInt("G");

            switch (check) {
                case 17:
                    switch (ee.getInt("T")) {
                        case 9:
                            try {
                                totalsO.put(ee.getDouble("P"), ee.getDouble("C"));
                            } catch (Exception e) {
                            }
                            break;

                        case 10:
                            try {
                                totalsU.put(ee.getDouble("P"), ee.getDouble("C"));
                            } catch (Exception e) {
                            }
                            break;
                    }
                    break;

                case 2:
                    switch (ee.getInt("T")) {
                        case 7:
                            try {
                                forasTeam1.put(ee.getDouble("P"), ee.getDouble("C"));
                            } catch (Exception e) {
                            }
                            break;

                        case 8:
                            try {
                                forasTeam2.put(ee.getDouble("P"), ee.getDouble("C"));
                            } catch (Exception e) {
                            }
                            break;
                    }
                    break;

            }
        }


        totals.put(CoefIdList.ALL_MATCH_TOTALS_O, totalsO);
        totals.put(CoefIdList.ALL_MATCH_TOTALS_U, totalsU);

        foras.put(CoefIdList.ALL_MATCH_TEAM_1_FORAS, forasTeam1);
        foras.put(CoefIdList.ALL_MATCH_TEAM_2_FORAS, forasTeam2);

        return totals;
    }

        /*
    private static HashMap<String, HashMap<Double, Double>> getForasOnThisEvent(JSONObject ev) {
        HashMap<String, HashMap<Double, Double>> foras = new HashMap<>();
        HashMap<Double, Double> forasTeam1 = new HashMap<>();
        HashMap<Double, Double> forasTeam2 = new HashMap<>();
        JSONArray coefss = ev.getJSONArray("E");
        // JSONObject coefs = ev.getJSONObject("E");
        for (Object ob : coefss) {
            JSONObject ee = (JSONObject) ob;
            int check = ee.getInt("G");
            if (check == 2 && ee.getInt("T") == 7) {
                try {
                    forasTeam1.put(ee.getDouble("P"), ee.getDouble("C"));
                } catch (Exception e) { }
            }

            if (check == 2 && ee.getInt("T") == 8) {
                try {
                    forasTeam2.put(ee.getDouble("P"), ee.getDouble("C"));
                } catch (Exception e) { }
                }
        }

        foras.put(CoefIdList.ALL_MATCH_TEAM_1_FORAS, forasTeam1);
        foras.put(CoefIdList.ALL_MATCH_TEAM_2_FORAS, forasTeam2);

        return foras;
    }
        */
}