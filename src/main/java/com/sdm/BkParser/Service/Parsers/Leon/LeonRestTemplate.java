package com.sdm.BkParser.Service.Parsers.Leon;


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
public class LeonRestTemplate implements ParserInt {
    public static final String BK_NAME = "Leon";

    public final static String URL_FOOTBALL="https://www.leon.ru/rest/betline/events/prematch?ctag=ru-RU&family=Soccer&limit=10000";
    public final static String URL_HOCKEY="https://www.leon.ru/rest/betline/events/prematch?ctag=ru-RU&family=IceHockey&limit=10000";
    public final static String URL_VOLLEYBALL="https://www.leon.ru/rest/betline/events/prematch?ctag=ru-RU&family=Volleyball&limit=10000";
    public final static String URL_BASKETBALL="https://www.leon.ru/rest/betline/events/prematch?ctag=ru-RU&family=Basketball&limit=10000";
    public final static String URL_TENNIS="https://www.leon.ru/rest/betline/events/prematch?ctag=ru-RU&family=Tennis&limit=10000";
    public final static String SECOND_URL = "https://www.leon.ru/rest/betline/event/prematch?ctag=ru-RU&eventId=";

   private static HashMap<String, HashMap<Double, Double>> foras;
    @Autowired
    NewRestTemplate restTemplate;
    /*
     основная функция парсинга
  */
    public  HashMap<String, List<Event>> startParsing(){
        HashMap<String, List<Event>> EventList = new HashMap<>();

        JSONObject footballResponse, hockeyResponse, volleybollResponse, basketballResponse, tennisResponse;

        /*
            делаем запрос к серверу по каждому виду спорта
         */





        footballResponse = restTemplate.getForJSON(URL_FOOTBALL);
        hockeyResponse = restTemplate.getForJSON(URL_HOCKEY);
        volleybollResponse = restTemplate.getForJSON(URL_VOLLEYBALL);
        basketballResponse = restTemplate.getForJSON(URL_BASKETBALL);
        tennisResponse = restTemplate.getForJSON(URL_TENNIS);



        /*
            разбираем объекты и сохраняем их
         */

        EventList.put(SportIdList.FOOTBALL, getAllEvents(footballResponse, SportIdList.FOOTBALL));
        EventList.put(SportIdList.HOCKEY, getAllEvents(hockeyResponse, SportIdList.HOCKEY));
        EventList.put(SportIdList.VOLLEYBALL, getAllEvents(volleybollResponse, SportIdList.VOLLEYBALL));
        EventList.put(SportIdList.BASKETBALL, getAllEvents(basketballResponse, SportIdList.BASKETBALL));
        EventList.put(SportIdList.TENNIS, getAllEvents(tennisResponse, SportIdList.TENNIS));



        return EventList;
    }



    /*
        достаем айди события и относительно его делаем второй запрос
     */

    private  List<Event> getAllEvents(JSONObject jsonObject, String kindOfSport) {
        List<Event> response = new LinkedList<>();
        List<Long> idList = getAllIds(jsonObject);
        for (Long id : idList) {
            try{
                Event event = downloadAndSaveEvent(id, kindOfSport);
                response.add(event);
            }catch (Exception e){
                //logger.getLOGGER().log(Level.WARNING, "Error while downloading and saving single event", e);
            }

        }
        return response;
    }




    /*
        разбираем ответ от второго запроса и сохраняем все в объект типа евент по конкретному виду спорта
     */

    private  Event downloadAndSaveEvent(Long id, String kindOfSport) {

        JSONObject res =  restTemplate.getForJSON(SECOND_URL+ id);

        JSONArray names = res.getJSONArray("competitors");
        JSONObject t1 =  names.getJSONObject(0);
        String team1 = t1.getString("name");
        t1 = names.getJSONObject(1);
        String team2 = t1.getString("name");
        Long startTime = res.getLong("kickoff")/1000;
        // достаем основные коэфициенты 1 х 2 1х 12 х2
        HashMap<String, Double> coefsOnEvent = getMaincoefsOnThisEvent(res.getJSONArray("markets"));
        // достаем тоталы на это событие
        HashMap<String, HashMap<Double, Double>> totalsOnThisEvent = getTotalsAndForasOnThisEvent(res.getJSONArray("markets"));
        // достаем форы на это событие
        HashMap<String, HashMap<Double, Double>> forasOnThisEvent = foras;
        Event event=null;
        switch (kindOfSport){
            case SportIdList.FOOTBALL:
                event = new FootballMatch(32, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                break;
            case SportIdList.BASKETBALL:
                event = new BasketballMatch(32, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                break;

            case SportIdList.VOLLEYBALL:
                event = new VolleyballMatch(32, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                break;

            case SportIdList.HOCKEY:
                event = new HockeyMatch(32, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                break;

            case SportIdList.TENNIS:
                event = new TennisMatch(32, team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                break;

        }

        return event;
    }



    private static HashMap<String, HashMap<Double, Double>> getForasOnThisEvent(JSONArray markets) {
        HashMap<String, HashMap<Double, Double>> response = new HashMap<>();
        HashMap<Double, Double> forasTeam1 = new HashMap<>();
        HashMap<Double, Double> forasTeam2 = new HashMap<>();
        for (Object ob : markets) {
            JSONObject obj = (JSONObject) ob;
            String name1 = obj.getString("name");
            switch (name1){

            }


        }
        response.put(CoefIdList.ALL_MATCH_TEAM_1_FORAS, forasTeam1);
        response.put(CoefIdList.ALL_MATCH_TEAM_2_FORAS, forasTeam2);
        return response;
    }





    /*
        разбираем JSON и достаем тоталы на событие
     */

    private static HashMap<String, HashMap<Double, Double>> getTotalsAndForasOnThisEvent(JSONArray markets) {
        foras = new HashMap<>();
        HashMap<String, HashMap<Double, Double>> response = new HashMap<>();
        HashMap<Double, Double> totalsO = new HashMap<>();
        HashMap<Double, Double> totalsU = new HashMap<>();
        HashMap<Double, Double> forasTeam1 = new HashMap<>();
        HashMap<Double, Double> forasTeam2 = new HashMap<>();
        for (Object ob : markets) {
            JSONObject obj = (JSONObject) ob;

            String name1 = obj.getString("name");
            switch (name1){
                case "Тотал":
                    JSONArray ar = obj.getJSONArray("runners");
                    for (Object o : ar) {
                        JSONObject o1 = (JSONObject) o;
                        switch (o1.getJSONArray("tags").get(0).toString()){

                            case "UNDER":
                                String name = o1.getString("name");

                                Double key = Double.valueOf(name.substring(name.indexOf("(")+1, name.indexOf(")")));
                                Double coef = o1.getDouble("price");
                                totalsU.put(key, coef);
                                break;

                            case "OVER":
                                name = o1.getString("name");
                                key = Double.valueOf(name.substring(name.indexOf("(")+1, name.indexOf(")")));
                                coef = o1.getDouble("price");
                                totalsO.put(key, coef);
                                break;


                        }
                    }
                    break;

                case "Фора":
                    JSONArray arr = obj.getJSONArray("runners");
                    for (Object o : arr) {
                        JSONObject o1 = (JSONObject) o;
                        switch (o1.getJSONArray("tags").get(0).toString()){

                            case "HOME":
                                String name = o1.getString("name");

                                Double key = Double.valueOf(name.substring(name.indexOf("(")+1, name.indexOf(")")));
                                Double coef = o1.getDouble("price");
                                forasTeam1.put(key, coef);
                                break;

                            case "AWAY":
                                name = o1.getString("name");
                                key = Double.valueOf(name.substring(name.indexOf("(")+1, name.indexOf(")")));
                                coef = o1.getDouble("price");
                                forasTeam2.put(key, coef);
                                break;


                        }
                    }
                    break;
            }
        }
        response.put(CoefIdList.ALL_MATCH_TOTALS_O, totalsO);
        response.put(CoefIdList.ALL_MATCH_TOTALS_U, totalsU);
        foras.put(CoefIdList.ALL_MATCH_TEAM_1_FORAS, forasTeam1);
        foras.put(CoefIdList.ALL_MATCH_TEAM_2_FORAS, forasTeam2);
        return response;
    }


    /*
        разбираем JSON и достаем основные кэфы на событие
     */

    private static HashMap<String, Double> getMaincoefsOnThisEvent(JSONArray markets) {
        HashMap<String, Double> coefs= new HashMap<>();
        boolean OneXTwo = false;
        boolean OneXTwo2 = false;
        for (Object ob : markets) {
            JSONObject obj = (JSONObject) ob;

            switch (obj.getString("name")){
                case "1X2":
                    OneXTwo = true;
                    JSONArray ar = obj.getJSONArray("runners");
                    for (Object o : ar) {
                        JSONObject o1 = (JSONObject) o;
                        switch (o1.getString("name")){

                            case "1":
                                coefs.put(CoefIdList.ALL_MATCH_TEAM_1_WIN, o1.getDouble("price"));
                                break;

                            case "X":
                                coefs.put(CoefIdList.ALL_MATCH_DRAW, o1.getDouble("price"));
                                break;

                            case "2":
                                coefs.put(CoefIdList.ALL_MATCH_TEAM_2_WIN, o1.getDouble("price"));
                                break;
                        }
                    }
                    break;

                case "Двойной исход":
                    OneXTwo2 = true;
                     ar = obj.getJSONArray("runners");
                    for (Object o : ar) {
                        JSONObject o1 = (JSONObject) o;
                        switch (o1.getString("name")){

                            case "1X":
                                coefs.put(CoefIdList.ALL_MATCH_DRAW_OR_TEAM_1_WIN, o1.getDouble("price"));
                                break;

                            case "12":
                                coefs.put(CoefIdList.ALL_MATCH_TEAM_1_OR_TEAM_2_WIN, o1.getDouble("price"));
                                break;

                            case "X2":
                                coefs.put(CoefIdList.ALL_MATCH_DRAW_OR_TEAM_2_WIN, o1.getDouble("price"));
                                break;
                        }

                    }
                    break;
            }
            if(OneXTwo && OneXTwo2){
                break;
            }


        }

        return coefs;
    }



    /*
        разбираем JSON и достаем айдишники событий
     */

    private static List<Long> getAllIds(JSONObject response) {
        List<Long> ids = new LinkedList<>();

        JSONArray events = response.getJSONArray("events");
        for (Object ev : events) {
            JSONObject event = (JSONObject) ev;
            ids.add(event.getLong("id"));
        }

        return ids;
    }

}
