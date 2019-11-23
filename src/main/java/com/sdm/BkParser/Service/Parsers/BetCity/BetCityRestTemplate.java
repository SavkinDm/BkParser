package com.sdm.BkParser.Service.Parsers.BetCity;


import com.sdm.BkParser.Entity.*;
import com.sdm.BkParser.Service.Parsers.Fonbet.CoefIdList;
import com.sdm.BkParser.Service.Parsers.ParserInt;
import com.sdm.BkParser.SupportClasses.Rest.NewRestTemplate;
import com.sdm.BkParser.SupportClasses.SportIdList;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BetCityRestTemplate implements ParserInt {

    private static final String URL = "https://ad.betcity.ru/d/off/events";
    private static final String SECOND_URL = "https://ad.betcity.ru/d/off/ext?rev=";
    private static final String BK_NAME = "betcity";


    @Autowired
    NewRestTemplate newRestTemplate;

    /*
        разбираем и сохраняем основные коэфициенты на событие
     */
    private static HashMap<String, Double> getcoefsOnThisEventToSave(Map sportInf, int id) {
        HashMap<String, Double> coefs = new HashMap<>();
        try {
            Set<String> keys = sportInf.keySet();
            for (String key : keys) {
                Map mainCoefs = (Map) sportInf.get(key);
                Map data = (Map) mainCoefs.get("data");
                Map dataId = (Map) data.get(String.valueOf(id));
                Map blocks = (Map) dataId.get("blocks");
                switch (key) {
                    case "69": // 1 x 2
                        Map Wm = null;

                        try {
                            Wm = (Map) blocks.get("Wm");
                            Map p1 = (Map) Wm.get("P1");
                            Double p1coef = Double.valueOf(String.valueOf(p1.get("kf")));
                            coefs.put(CoefIdList.ALL_MATCH_TEAM_1_WIN, p1coef);
                        } catch (Exception ignored) {
                        }
                        try {
                            Map X = (Map) Wm.get("X");
                            Double xcoef = Double.valueOf(String.valueOf(X.get("kf")));
                            coefs.put(CoefIdList.ALL_MATCH_DRAW, xcoef);
                        } catch (Exception ignored) {
                        }
                        try {
                            Map p2 = (Map) Wm.get("P2");
                            Double p2coef = Double.valueOf(String.valueOf(p2.get("kf")));
                            coefs.put(CoefIdList.ALL_MATCH_TEAM_2_WIN, p2coef);
                        } catch (Exception ignored) {
                        }


                        break;

                    case "70":  // 1x 12 2x
                        try {
                            Map WXm = (Map) blocks.get("WXm");
                            Map p1x = (Map) WXm.get("1X");
                            Double p1xCoef = Double.valueOf(String.valueOf(p1x.get("kf")));
                            Map oneTwo = (Map) WXm.get("12");
                            Double oneTwoCoef = Double.valueOf(String.valueOf(oneTwo.get("kf")));
                            Map p2x = (Map) WXm.get("X2");
                            Double p2xCoef = Double.valueOf(String.valueOf(p2x.get("kf")));
                            coefs.put(CoefIdList.ALL_MATCH_DRAW_OR_TEAM_1_WIN, p1xCoef);
                            coefs.put(CoefIdList.ALL_MATCH_TEAM_1_OR_TEAM_2_WIN, oneTwoCoef);
                            coefs.put(CoefIdList.ALL_MATCH_DRAW_OR_TEAM_2_WIN, p2xCoef);
                        } catch (Exception ignored) {
                        }

                        break;

                }
            }


        } catch (Exception e) {

        }

        return coefs;
    }

    /*
        основная функция парсинга
     */
    @Override
    public HashMap<String, List<Event>> startParsing() {
        HashMap<String, List<Event>> events;

        JSONObject mainResponse = newRestTemplate.getForJSON(URL);
        events = getAllEventsMainInfo(mainResponse);
        return events;
    }

    /*
        разбираем и сохраняем основные форы на событие
     */
    private HashMap<String, HashMap<Double, Double>> getForasOnthisEvent(Map coefs, int id) {
        HashMap<String, HashMap<Double, Double>> response = new HashMap<>();
        HashMap<Double, Double> team1foras = new HashMap<>();
        HashMap<Double, Double> team2foras = new HashMap<>();
        try {
            Map foras = (Map) coefs.get("71");
            Map data = (Map) foras.get("data");
            Map idK = (Map) data.get(String.valueOf(id));
            Map blocks = (Map) idK.get("blocks");
            Map f1m = (Map) blocks.get("F1m");
            Map re;
            Double val;
            Double coef;
            try {
                re = (Map) f1m.get("Kf_F1");
                val = Double.valueOf(String.valueOf(re.get("lv")));
                coef = Double.valueOf(String.valueOf(re.get("kf")));
                team1foras.put(val, coef);
            } catch (Exception ignored) {
            }

            try {
                re = (Map) f1m.get("Kf_F2");
                val = Double.valueOf(String.valueOf(re.get("lv")));
                coef = Double.valueOf(String.valueOf(re.get("kf")));
                team2foras.put(val, coef);
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        }


        response.put(CoefIdList.ALL_MATCH_TEAM_1_FORAS, team1foras);
        response.put(CoefIdList.ALL_MATCH_TEAM_2_FORAS, team2foras);

        return response;
    }

    /*
        разбираем и сохраняем основные тоталы на событие
     */
    private HashMap<String, HashMap<Double, Double>> getTotalsOnthisEvent(Map sportInf, int eventId) {
        HashMap<String, HashMap<Double, Double>> response = new HashMap<>();
        HashMap<Double, Double> totalsO = new HashMap<>();
        HashMap<Double, Double> totalsU = new HashMap<>();
        // взяли основной тотал из основного запроса
        try {
            Map mainCoefs = (Map) sportInf.get("72");
            Map data = (Map) mainCoefs.get("data");
            Map dataId = (Map) data.get(String.valueOf(eventId));
            Map blocks = (Map) dataId.get("blocks");
            Map WXm = (Map) blocks.get("T1m");
            Map totalU = (Map) WXm.get("Tm");
            Double totalVal = Double.valueOf(String.valueOf(totalU.get("lv")));
            Double totalUcoef = Double.valueOf(String.valueOf(totalU.get("kf")));
            Map totalO = (Map) WXm.get("Tb");
            Double totalOcoef = Double.valueOf(String.valueOf(totalO.get("kf")));
            totalsU.put(totalVal, totalUcoef);
            totalsO.put(totalVal, totalOcoef);
        } catch (Exception ignored) {
        }

        // грузим джейсон по второму юрлу для загрузки оставшихся тоталов
        /*
        try{
            response = downloadExternalTotals(eventId);
        }catch (Exception e){}
        response.get(CoefIdList.ALL_MATCH_TOTALS_O).putAll(totalsO);
        response.get(CoefIdList.ALL_MATCH_TOTALS_U).putAll(totalsU);
        */


        response.put(CoefIdList.ALL_MATCH_TOTALS_O, totalsO);
        response.put(CoefIdList.ALL_MATCH_TOTALS_U, totalsU);
        return response;
    }

    /*
        делаем запрос по второму юрлу для подгрузки и сохранения тоталов
     */
    private HashMap<String, HashMap<Double, Double>> downloadExternalTotals(int eventId) {
        HashMap<String, HashMap<Double, Double>> response = new HashMap<>();
        HashMap<Double, Double> totalsO = new HashMap<>();
        HashMap<Double, Double> totalsU = new HashMap<>();
        JSONObject resp;

        try {
            resp = newRestTemplate.getForJSON(SECOND_URL + 3 + "&ids=" + eventId);
            Map sports = resp.getJSONObject("reply").getJSONObject("sports").toMap();
            Set<String> keysSport = sports.keySet();
            for (String key : keysSport) {
                Map chmps = (Map) sports.get(key);
                Map chemp = (Map) chmps.get("chmps");
                Set<String> keysChmps = chemp.keySet();
                for (String keyChmps : keysChmps) {
                    Map num = (Map) chemp.get(keyChmps);
                    Map evts = (Map) num.get("evts");
                    Map ext = (Map) evts.get(String.valueOf(eventId));
                    Map ext1 = (Map) ext.get("ext");
                    Map totals = (Map) ext1.get("112");
                    Map data = (Map) totals.get("data");
                    Set<String> keysData = data.keySet();
                    for (String keyData : keysData) {
                        Map total = (Map) data.get(keyData);
                        Map blocks = (Map) total.get("blocks");
                        Map t = (Map) blocks.get("T");
                        Double value = Double.valueOf(String.valueOf(t.get("Tot")));
                        Map Tm = (Map) t.get("Tm");
                        Double tmCoef = Double.valueOf(String.valueOf(Tm.get("kf")));
                        Map Tb = (Map) t.get("Tb");
                        Double tbCoef = Double.valueOf(String.valueOf(Tb.get("kf")));
                        totalsO.put(value, tbCoef);
                        totalsU.put(value, tmCoef);

                    }
                }
            }

        } catch (Exception e) {
            //logger.getLOGGER().log(Level.WARNING, "Betcity error while parsing second url " + SECOND_URL+eventId, e);
        }


        response.put(CoefIdList.ALL_MATCH_TOTALS_O, totalsO);
        response.put(CoefIdList.ALL_MATCH_TOTALS_U, totalsU);
        return response;
    }

    /*
        разбираем основной ответ от 1го юрла и сохраняем всю информацию
     */
    private HashMap<String, List<Event>> getAllEventsMainInfo(JSONObject mainResponse) {
        HashMap<String, List<Event>> allEventsFromBetCity = new HashMap<>();
        List<Event> footballMatches = new ArrayList<>();
        List<Event> basketballMatches = new ArrayList<>();
        List<Event> volleyballMatches = new ArrayList<>();
        List<Event> tennisMatches = new ArrayList<>();
        List<Event> hockeyMatches = new ArrayList<>();

        JSONObject sports = mainResponse.getJSONObject("reply").getJSONObject("sports");
        Map sportsList = sports.toMap();
        Set<String> keys = sportsList.keySet();
        for (String key : keys) {
            Map sport = (Map) sportsList.get(key);
            int sport_id = (int) sport.get("id_sp");
            if (sport_id == 1 || sport_id == 2 || sport_id == 3 || sport_id == 7 || sport_id == 12) {
                Map chempsList = (Map) sport.get("chmps");
                Set<String> chempsKeys = chempsList.keySet();
                for (int i = 70868; i < 70910; i++) {
                    try {
                        chempsKeys.remove(String.valueOf(i));
                    } catch (Exception ignored) {
                    }
                }
                for (String chempId : chempsKeys) {
                    Map chemp = (Map) chempsList.get(chempId);
                    Map eventsList = (Map) chemp.get("evts");
                    Set<String> eventsKeys = eventsList.keySet();
                    for (String eventId : eventsKeys) {
                        Map event = (Map) eventsList.get(eventId);
                        int id = (int) event.get("id_ev");
                        String team1 = null;
                        String team2 = null;
                        try {
                            team1 = (String) event.get("name_ht");

                        } catch (NullPointerException ignored) {
                        }
                        try {
                            team2 = (String) event.get("name_at");

                        } catch (NullPointerException ignored) {
                        }

                        long startTime = Long.parseLong(String.valueOf(event.get("date_ev")));
                        BetCitySportIdList betCitySportIdList = new BetCitySportIdList();
                        if (team1 != null && team2 != null) {
                            if (!team1.contains("УГЛ") && !team2.contains("УГЛ") && !team1.contains("ЖК") && !team2.contains("ЖК") && !team1.contains("фолы") && !team2.contains("фолы")
                                    && !team1.contains("офсайды") && !team2.contains("офсайды") && !team1.contains("(стат)") && !team2.contains("(стат)")) {


                                Map coefs = (Map) event.get("main");
                                // разбираем основные коэфициенты на событие 1 х 2 1х 12 2х
                                HashMap<String, Double> coefsOnThisEventToSave = getcoefsOnThisEventToSave(coefs, id);
                                // разбираем тоталы на событие
                                HashMap<String, HashMap<Double, Double>> totalsOnEvent;
                                totalsOnEvent = getTotalsOnthisEvent(coefs, id);
                                // разбираем форы на событие
                                HashMap<String, HashMap<Double, Double>> forasOnEvent;
                                forasOnEvent = getForasOnthisEvent(coefs, id);
                                Event event1;
                                switch (betCitySportIdList.getSportName(sport_id)) {

                                    case BetCitySportIdList.FOOTBALL:
                                        event1 = new FootballMatch(id, team1, team2, betCitySportIdList.getSportName(sport_id), BK_NAME, startTime, coefsOnThisEventToSave, totalsOnEvent, forasOnEvent);
                                        footballMatches.add(event1);
                                        break;

                                    case BetCitySportIdList.BASKETBALL:
                                        event1 = new BasketballMatch(id, team1, team2, betCitySportIdList.getSportName(sport_id), BK_NAME, startTime, coefsOnThisEventToSave, totalsOnEvent, forasOnEvent);
                                        basketballMatches.add(event1);
                                        break;

                                    case BetCitySportIdList.VOLLEYBALL:
                                        event1 = new VolleyballMatch(id, team1, team2, betCitySportIdList.getSportName(sport_id), BK_NAME, startTime, coefsOnThisEventToSave, totalsOnEvent, forasOnEvent);
                                        volleyballMatches.add(event1);
                                        break;

                                    case BetCitySportIdList.HOCKEY:
                                        event1 = new HockeyMatch(id, team1, team2, betCitySportIdList.getSportName(sport_id), BK_NAME, startTime, coefsOnThisEventToSave, totalsOnEvent, forasOnEvent);
                                        hockeyMatches.add(event1);
                                        break;

                                    case BetCitySportIdList.TENNIS:
                                        event1 = new TennisMatch(id, team1, team2, betCitySportIdList.getSportName(sport_id), BK_NAME, startTime, coefsOnThisEventToSave, totalsOnEvent, forasOnEvent);
                                        tennisMatches.add(event1);
                                        break;


                                }
                            }
                        }


                    }
                }
            }
        }


        allEventsFromBetCity.put(SportIdList.FOOTBALL, footballMatches);
        allEventsFromBetCity.put(SportIdList.BASKETBALL, basketballMatches);
        allEventsFromBetCity.put(SportIdList.VOLLEYBALL, volleyballMatches);
        allEventsFromBetCity.put(SportIdList.TENNIS, tennisMatches);
        allEventsFromBetCity.put(SportIdList.HOCKEY, hockeyMatches);

        return allEventsFromBetCity;
    }


}
