package com.sdm.BkParser.Service.Parsers.FanSport;



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
public class FanSportRestTemplate implements ParserInt {

    private static final String URL_FOOTBALL = "https://fan-sport2.com/LineFeed/Get1x2_VZip?sports=1&count=10000&tf=10000&mode=4&cyberFlag=2&partner=110";
    private static final String URL_BASKETBALL = "https://fan-sport2.com/LineFeed/Get1x2_VZip?sports=3&count=10000&tf=10000&mode=4&cyberFlag=2&partner=94";
    private static final String URL_VOLLEYBALL = "https://fan-sport2.com/LineFeed/Get1x2_VZip?sports=6&count=10000&tf=10000&mode=4&cyberFlag=2&partner=94";
    private static final String URL_TENNIS = "https://fan-sport2.com/LineFeed/Get1x2_VZip?sports=4&count=10000&tf=10000&mode=4&cyberFlag=2&partner=94";
    private static final String URL_HOCKEY = "https://fan-sport2.com/LineFeed/Get1x2_VZip?sports=2&count=10000&tf=10000&mode=4&cyberFlag=2&partner=94";

    private static final String SECOND_URL_FIRST_PART = "https://fan-sport2.com/LineFeed/GetGameZip?id=";
    private static final String SECOND_URL_SECOND_PART = "&lng=ru&cfview=0&isSubGames=true&GroupEvents=true&allEventsGroupSubGames=true&countevents=250&grMode=2";
    private static final String BK_NAME = "fanSport";

    @Autowired
    NewRestTemplate restTemplate;


    /*
        основная функция парсинга
     */
    @Override
    public HashMap<String, List<Event>> startParsing() {
        HashMap<String, List<Event>> response = new HashMap<>();

        JSONObject footballResponse, hockeyResponse, volleyballResponse, basketballResponse, tennisResponse;
        /*
            делаем запрос к серверу по каждому виду спорта
         */
        footballResponse = restTemplate.getForJSON(URL_FOOTBALL);
        hockeyResponse = restTemplate.getForJSON(URL_HOCKEY);
        volleyballResponse = restTemplate.getForJSON(URL_VOLLEYBALL);
        basketballResponse = restTemplate.getForJSON(URL_BASKETBALL);
        tennisResponse = restTemplate.getForJSON(URL_TENNIS);


          /*
            разбираем объекты и сохраняем их
         */
        response.put(SportIdList.FOOTBALL, getAllEvents(footballResponse, SportIdList.FOOTBALL));
        response.put(SportIdList.HOCKEY, getAllEvents(hockeyResponse, SportIdList.HOCKEY));
        response.put(SportIdList.VOLLEYBALL, getAllEvents(volleyballResponse, SportIdList.VOLLEYBALL));
        response.put(SportIdList.BASKETBALL, getAllEvents(basketballResponse, SportIdList.BASKETBALL));
        response.put(SportIdList.TENNIS, getAllEvents(tennisResponse, SportIdList.TENNIS));


        return response;
    }

    /*
        получаем готовый список событий по каждому виду спорта
     */
    private List<Event> getAllEvents(JSONObject jsonObject, String sportId) {
        List<Event> resp = new LinkedList<>();
        List<Long> idList = getAllIds(jsonObject);

        for (Long id : idList) {
            try {
                Event event = downloadAndSaveEvent(id, sportId);
                if (event != null)
                    resp.add(event);
            } catch (Exception ignored) {
            }
        }

        return resp;
    }

    /*
        загружаем и сохраняем полную информацию о каждом событии
     */
    private Event downloadAndSaveEvent(Long id, String kindOfSport) {

        JSONObject resp = restTemplate.getForJSON(SECOND_URL_FIRST_PART + id + SECOND_URL_SECOND_PART);
        JSONObject ev = resp.getJSONObject("Value");
        String team1 = ev.getString("O1");
        String team2 = ev.getString("O2");
        Event event = null;
        if (!team1.equals("Хозяева (очки)")) {
            long startTime = Long.parseLong(String.valueOf(ev.getInt("S")));
            // достаем основные коэфициенты 1 х 2 1х 12 х2
            HashMap<String, Double> coefsOnEvent = getMaincoefsOnThisEvent(ev);
            // достаем тоталы на это событие
            HashMap<String, HashMap<Double, Double>> totalsOnThisEvent = getTotalsOnThisEvent(ev);
            //достаем форы на это событие
            HashMap<String, HashMap<Double, Double>> forasOnThisEvent = getForasOnThisEvent(ev);

            switch (kindOfSport) {
                case SportIdList.FOOTBALL:
                    event = new FootballMatch(31, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                    break;
                case SportIdList.BASKETBALL:
                    event = new BasketballMatch(31, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                    break;

                case SportIdList.VOLLEYBALL:
                    event = new VolleyballMatch(31, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                    break;

                case SportIdList.HOCKEY:
                    event = new HockeyMatch(31, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                    break;

                case SportIdList.TENNIS:
                    event = new TennisMatch(31, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                    break;

            }
        }


        return event;
    }

    /*
        достаем форы на событие
     */
    private HashMap<String, HashMap<Double, Double>> getForasOnThisEvent(JSONObject ev) {
        HashMap<String, HashMap<Double, Double>> foras = new HashMap<>();
        HashMap<Double, Double> forasTeam1 = new HashMap<>();
        HashMap<Double, Double> forasTeam2 = new HashMap<>();
        JSONArray Ge = ev.getJSONArray("GE");
        for (Object ob : Ge) {
            JSONObject coefBlock = (JSONObject) ob;
            JSONArray coefs = coefBlock.getJSONArray("E");
            int check = coefBlock.getInt("G");
            if (check == 3) {
                JSONArray forasT1 = (JSONArray) coefs.get(0);
                JSONArray forasT2 = (JSONArray) coefs.get(1);
                for (Object totO : forasT1) {
                    JSONObject forat1 = (JSONObject) totO;
                    if (forat1.getInt("T") == 7) {
                        forasTeam1.put(forat1.getDouble("P"), forat1.getDouble("C"));
                    }
                }
                for (Object totU : forasT2) {
                    JSONObject forat2 = (JSONObject) totU;
                    if (forat2.getInt("T") == 8) {
                        forasTeam2.put(forat2.getDouble("P"), forat2.getDouble("C"));
                    }
                }
            }
        }
        foras.put(CoefIdList.ALL_MATCH_TEAM_1_FORAS, forasTeam1);
        foras.put(CoefIdList.ALL_MATCH_TEAM_2_FORAS, forasTeam2);

        return foras;
    }

    /*
        достаем тоталы на событие
     */
    private HashMap<String, HashMap<Double, Double>> getTotalsOnThisEvent(JSONObject ev) {
        HashMap<String, HashMap<Double, Double>> totals = new HashMap<>();
        HashMap<Double, Double> totalsO = new HashMap<>();
        HashMap<Double, Double> totalsU = new HashMap<>();
        JSONArray Ge = ev.getJSONArray("GE");
        for (Object ob : Ge) {
            JSONObject coefBlock = (JSONObject) ob;
            JSONArray coefs = coefBlock.getJSONArray("E");
            int check = coefBlock.getInt("G");
            if (check == 4) {
                JSONArray totalsO1 = (JSONArray) coefs.get(0);
                JSONArray totalsU1 = (JSONArray) coefs.get(1);
                for (Object totO : totalsO1) {
                    JSONObject totalO = (JSONObject) totO;
                    if (totalO.getInt("T") == 9) {
                        totalsO.put(totalO.getDouble("P"), totalO.getDouble("C"));
                    }
                }
                for (Object totU : totalsU1) {
                    JSONObject totalU = (JSONObject) totU;
                    if (totalU.getInt("T") == 10) {
                        totalsU.put(totalU.getDouble("P"), totalU.getDouble("C"));
                    }
                }
            }
        }
        totals.put(CoefIdList.ALL_MATCH_TOTALS_O, totalsO);
        totals.put(CoefIdList.ALL_MATCH_TOTALS_U, totalsU);

        return totals;
    }

    /*
        достаем основные коэфициенты на событие 1 х 2 1х 12 2х
     */
    private HashMap<String, Double> getMaincoefsOnThisEvent(JSONObject ev) {
        HashMap<String, Double> mainCoefs = new HashMap<>();
        JSONArray Ge = ev.getJSONArray("GE");
        for (Object ob : Ge) {
            JSONObject coefBlock = (JSONObject) ob;
            JSONArray coefs = coefBlock.getJSONArray("E");
            int check = coefBlock.getInt("G");
            if (check == 1 || check == 2) {
                for (Object o : coefs) {
                    JSONArray co = (JSONArray) o;
                    JSONObject c = (JSONObject) co.get(0);
                    int type = c.getInt("T");
                    double coef = c.getDouble("C");
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
        }


        return mainCoefs;
    }

    /*
        парсим по 1ому юрлу все айди событий
     */
    private List<Long> getAllIds(JSONObject jsonObject) {
        List<Long> ids = new LinkedList<>();
        JSONArray events = jsonObject.getJSONArray("Value");
        for (Object obj : events) {
            JSONObject event = (JSONObject) obj;
            Long id = Long.valueOf(String.valueOf(event.getInt("CI")));
            ids.add(id);
        }
        return ids;
    }


}
