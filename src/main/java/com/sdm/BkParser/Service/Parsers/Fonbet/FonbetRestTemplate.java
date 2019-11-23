package com.sdm.BkParser.Service.Parsers.Fonbet;


import com.sdm.BkParser.Entity.*;
import com.sdm.BkParser.Service.Parsers.ParserInt;
import com.sdm.BkParser.SupportClasses.Rest.JSONParser;
import com.sdm.BkParser.SupportClasses.SportIdList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class FonbetRestTemplate  implements ParserInt {
    public  static final String BK_NAME = "fonbet";

   @Autowired
    private JSONParser jsonParser;

    private final String URL = "http://line16.bkfon-resource.ru/line/currentLine/ru";

    private SportIdList sportIdList;


    public FonbetRestTemplate() {
    }





    /*
        вытасикаваем все события
     */

    private static JSONArray getEvents(JSONObject response){
        JSONArray Events=null;
        try{
            Events = response.getJSONArray("events");

        }catch (Exception e){
          // logger.getLOGGER().log(Level.WARNING, "Error while getting all events from JSON", e);
        }
        return Events;
    }



    private static JSONArray getCoef(JSONObject response){
        JSONArray Coefs=null;
        try{
            Coefs = response.getJSONArray("customFactors");

        }catch (Exception e){
          //  logger.getLOGGER().log(Level.WARNING, "Error while getting all coefs from JSON", e);

        }
        return Coefs;
    }


    /*
        вытаскиваем все коэфициенты на кокретное событие
     */

    private static List<HashMap> getAllCoefsOnSingleEvent(JSONArray coefs, int eventsId) throws JSONException, IOException {
        JSONObject coefsInformation;
        List<HashMap> coefsOnThisEvent= new LinkedList<>();
        for (int i = 0; i < coefs.length(); i++) {
            coefsInformation = coefs.getJSONObject(i);
            HashMap coefInf = (HashMap) coefsInformation.toMap();
            Integer id = (Integer) coefInf.get("e");
            if(id == eventsId){
                coefsOnThisEvent.add(coefInf);

            }

        }
    return  coefsOnThisEvent;
    }




    /*
        осмысленно сохраняем коэфициенты в объект ивент
     */
    private static HashMap<String, Double> getcoefsOnThisEventToSave(List<HashMap> allCoefsOnSingleEvent){
        HashMap<String, Double> response= new HashMap<>();
        CoefIdList coefIdList = new CoefIdList();
        for (HashMap coeficient : allCoefsOnSingleEvent) {
            if (coefIdList.containsKey((Integer) coeficient.get("f"))) {
                String key = String.valueOf(coefIdList.getValue((Integer) coeficient.get("f")));
                response.put(key, Double.parseDouble(String.valueOf(coeficient.get("v"))));
            }
        }
    return response;
    }


    /*
        выясняем какой вид спорта у нашего объекта
     */
    private static HashMap<Integer, Integer> getSportId(JSONObject sports){
        JSONArray Sports=null;

        try{
            Sports = sports.getJSONArray("sports");

        }catch (Exception e){
            e.printStackTrace();
        }

        JSONObject sportInformation;
        HashMap<Integer, Integer> response = new HashMap<>();
        for (int i = 0; i < Sports.length(); i++) {



            try {
                sportInformation =  Sports.getJSONObject(i);
                Integer parentId = sportInformation.getInt("parentId");
                Integer id = sportInformation.getInt("id");
                response.put( id, parentId );
            } catch (Exception ignored) {

            }


        }

    return response;
    }


    /*
        формируем окончательный объект какого-то вида спорта и записываем их все в определенные списки
     */

    private  HashMap<String, List<Event>> getAndSaveAllEvents(JSONArray events, JSONArray coefs) throws IOException {
        HashMap<String, List<Event>> response = new HashMap<>();
        JSONObject mainInformation = null;


        List<Event> footballMatches = new LinkedList<>();
        List<Event> basketballMatches = new LinkedList<>();
        List<Event> volleyballMatches = new LinkedList<>();
        List<Event> tennisMatches = new LinkedList<>();
        List<Event> hockeyMatches = new LinkedList<>();

        for (int i = 0; i < events.length(); i++) {
            //основная информация по событию
            try {
                mainInformation = events.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            HashMap inf = (HashMap) mainInformation.toMap();

            int check = (int) inf.get("level");
            String namePrefix = String.valueOf(inf.get("namePrefix"));
            String eventName = String.valueOf(inf.get("name"));
            if (check == 1 && namePrefix.equals("") && eventName.equals("")) {

                Integer id = (Integer) inf.get("id");
                String team1 = String.valueOf(inf.get("team1"));
                String team2 = String.valueOf(inf.get("team2"));

                Integer sportId = (Integer) inf.get("sportId");
                long startTime = Long.parseLong(String.valueOf(inf.get("startTime")));
                // отметаем события где нет названий команд
                if (team1.equals("null") || team2.equals("null"))
                    continue;

                // информаця по ставкам на это событие
                List<HashMap> allCoefsOnSingleEvent = null;
                try {
                    allCoefsOnSingleEvent = getAllCoefsOnSingleEvent(coefs, id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // осмысленно сохраняем коэфициенты в объект ивент

                HashMap<String, Double> coefsOnThisEventToSave = getcoefsOnThisEventToSave(allCoefsOnSingleEvent);

                // вытаскиваем тоталы на событие и сохраняем их
                HashMap<String, HashMap<Double, Double>> totalsOnEvent = getTotalsOnthisEvent(allCoefsOnSingleEvent, sportIdList.getSportName(sportId));

                // вытаскиваем форы на событие и сохраняем их
                HashMap<String, HashMap<Double, Double>> forasOnEvent = getForasOnthisEvent(allCoefsOnSingleEvent, sportIdList.getSportName(sportId));

                //итоговый объект события
                Event event;
                if(coefsOnThisEventToSave.size()==0 &&totalsOnEvent.get(CoefIdList.ALL_MATCH_TOTALS_O).size()==0 &&totalsOnEvent.get(CoefIdList.ALL_MATCH_TOTALS_U).size()==0
                        && forasOnEvent.get(CoefIdList.ALL_MATCH_TEAM_1_FORAS).size()==0 && forasOnEvent.get(CoefIdList.ALL_MATCH_TEAM_2_FORAS).size()==0){

                }else{
                    switch (sportIdList.getSportName(sportId)) {
                        case SportIdList.FOOTBALL:
                            event = new FootballMatch(id, team1, team2, SportIdList.FOOTBALL, BK_NAME, startTime, coefsOnThisEventToSave, totalsOnEvent, forasOnEvent);
                            footballMatches.add(event);
                            break;

                        case SportIdList.BASKETBALL:
                            event = new BasketballMatch(id, team1, team2, SportIdList.BASKETBALL, BK_NAME, startTime, coefsOnThisEventToSave, totalsOnEvent, forasOnEvent);
                            basketballMatches.add(event);
                            break;

                        case SportIdList.VOLLEYBALL:
                            event = new VolleyballMatch(id, team1, team2, SportIdList.VOLLEYBALL, BK_NAME, startTime, coefsOnThisEventToSave, totalsOnEvent, forasOnEvent);
                            volleyballMatches.add(event);
                            break;

                        case SportIdList.TENNIS:
                            event = new TennisMatch(id, team1, team2, SportIdList.TENNIS, BK_NAME, startTime, coefsOnThisEventToSave, totalsOnEvent, forasOnEvent);
                            tennisMatches.add(event);
                            break;
                        case SportIdList.HOCKEY:
                            event = new HockeyMatch(id, team1, team2, SportIdList.HOCKEY, BK_NAME, startTime, coefsOnThisEventToSave, totalsOnEvent, forasOnEvent);
                            hockeyMatches.add(event);
                            break;

                    }
                }


            }
        }

        response.put(SportIdList.FOOTBALL, footballMatches);
        response.put(SportIdList.BASKETBALL, basketballMatches);
        response.put(SportIdList.VOLLEYBALL, volleyballMatches);
        response.put(SportIdList.TENNIS, tennisMatches);
        response.put(SportIdList.HOCKEY, hockeyMatches);
    return response;
    }



    private static HashMap<String, HashMap<Double, Double>> getForasOnthisEvent(List<HashMap> allCoefsOnSingleEvent, String sportId) {
        HashMap<String, HashMap<Double, Double>> response = new HashMap<>();
        HashMap<Double, Double> team1Foras = new HashMap<>();
        HashMap<Double, Double> team2Foras = new HashMap<>();
        List<HashMap> totalsandForas = new ArrayList<>();
            for (HashMap tAndF : allCoefsOnSingleEvent) {
                if(tAndF.size() == 5){
                    totalsandForas.add(tAndF);
                }
            }
            for (HashMap total : totalsandForas) {
                Double key;
                Double coef;
                int id = (int) total.get("f");
                //для фор 1ой команды
                if (id == 927|| id == 989|| id == 910|| id == 1569|| id == 1672 || id == 1680 || id == 1677){
                    key = Double.parseDouble( String.valueOf(total.get("pt")));
                    coef = Double.parseDouble( String.valueOf(total.get("v")));
                    team1Foras.put(key, coef);
                }

                id = (int) total.get("f");
                //для фор 2ой команды
                if (id == 928|| id == 912 || id == 991|| id == 1572|| id == 1675 || id == 1681 || id == 1678){
                    key = Double.parseDouble( String.valueOf(total.get("pt")));
                    coef = Double.parseDouble( String.valueOf(total.get("v")));
                    team2Foras.put(key, coef);
                }

            }
        if(sportId.equals(SportIdList.BASKETBALL)) {
            for (HashMap tAndF : allCoefsOnSingleEvent) {
                int id = (int) tAndF.get("f");
                if(id == 911|| id== 1074){
                    List list = (List) tAndF.get("variants");
                    for (Object tot : list) {
                        Map total = (Map) tot;
                        double key, coef;
                        switch (id){
                            case 911:
                                key = Double.parseDouble(String.valueOf(total.get("pt")));
                                coef = Double.parseDouble(String.valueOf(total.get("v")));
                                team1Foras.put(key, coef);
                                break;

                            case 1074:
                                key = Double.parseDouble(String.valueOf(total.get("pt")));
                                coef = Double.parseDouble(String.valueOf(total.get("v")));
                                team2Foras.put(key, coef);
                                break;
                        }
                    }
                }
            }
        }

        response.put(CoefIdList.ALL_MATCH_TEAM_1_FORAS, team1Foras);
        response.put(CoefIdList.ALL_MATCH_TEAM_2_FORAS, team2Foras);
        return response;
    }




    /*
        вытаскивание тоталов на это событие
     */

    private static HashMap<String, HashMap<Double, Double>> getTotalsOnthisEvent(List<HashMap> allCoefsOnSingleEvent, String sportId) {
        HashMap<String, HashMap<Double, Double>> response = new HashMap<>();
        HashMap<Double, Double> totalsO = new HashMap<>();
        HashMap<Double, Double> totalsU = new HashMap<>();
        List<HashMap> totalsandForas = new ArrayList<>();

        for (HashMap tAndF : allCoefsOnSingleEvent) {
            if (tAndF.size() == 5) {
                totalsandForas.add(tAndF);
            }
        }
        for (HashMap total : totalsandForas) {
            Double key;
            Double coef;
            int id = (int) total.get("f");
            //для тоталО
            if (id == 930 || id == 1696 || id == 1727 || id == 1730 || id == 1733 || id == 1739 || id == 1736) {
                key = Double.parseDouble(String.valueOf(total.get("pt")));
                coef = Double.parseDouble(String.valueOf(total.get("v")));
                totalsO.put(key, coef);
            }
            id = (int) total.get("f");
            //для тоталU
            if (id == 931 || id == 1697 || id == 1728 || id == 1791 || id == 1737 || id == 1734 || id == 1731) {
                key = Double.parseDouble(String.valueOf(total.get("pt")));
                coef = Double.parseDouble(String.valueOf(total.get("v")));
                totalsU.put(key, coef);
            }
        }
        if(sportId.equals(SportIdList.BASKETBALL)) {
            for (HashMap tAndF : allCoefsOnSingleEvent) {
                int id = (int) tAndF.get("f");
                if(id == 680|| id== 682){
                    List list = (List) tAndF.get("variants");
                    for (Object tot : list) {
                        Map total = (Map) tot;
                        double key, coef;
                        switch (id){
                            case 680:
                                key = Double.parseDouble(String.valueOf(total.get("pt")));
                                coef = Double.parseDouble(String.valueOf(total.get("v")));
                                totalsO.put(key, coef);
                                break;

                            case 682:
                                key = Double.parseDouble(String.valueOf(total.get("pt")));
                                coef = Double.parseDouble(String.valueOf(total.get("v")));
                                totalsU.put(key, coef);
                                break;
                        }
                    }
                }
            }
        }
        response.put(CoefIdList.ALL_MATCH_TOTALS_O, totalsO);
        response.put(CoefIdList.ALL_MATCH_TOTALS_U, totalsU);

        return response;
    }





    /*
       основная функция парсинга
    */
    @Override
    public HashMap<String, List<Event>> startParsing() {
        JSONObject mainResponse;

        mainResponse = jsonParser.httpGetWithGZIP(URL);

        /*
        формируем список спорт айди что чему соотностится
         */
        sportIdList = new SportIdList();
        sportIdList.setSportIdList(getSportId(mainResponse));


        /*
        Достаем оттуда информацию по событиям
         */
        JSONArray events = getEvents(mainResponse);

        /*
        достаем информацию по коэфициентам на эти события
         */
        JSONArray  coefs = getCoef(mainResponse);

        /*
        Создаем список объектов класса Event для каждого события и заполняем их данными из JSON
         */
        HashMap<String, List<Event>> EventList=new HashMap<>();
        try {
            EventList = getAndSaveAllEvents(events, coefs);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return EventList;
    }




}
