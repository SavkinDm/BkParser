package com.sdm.BkParser.Service.Parsers.LigaStavok;


import com.sdm.BkParser.Entity.*;
import com.sdm.BkParser.Service.Parsers.Fonbet.CoefIdList;
import com.sdm.BkParser.Service.Parsers.ParserInt;
import com.sdm.BkParser.SupportClasses.HashmapForRest;
import com.sdm.BkParser.SupportClasses.Rest.NewRestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service
public class LigaStavokRestTemplate  implements ParserInt {
    static LigaStavokSportIdLIst ligaStavokSportIdLIst = new LigaStavokSportIdLIst();
    public static final String BK_NAME = "LigaStavok";
    public final static String URL="https://lds-api.ligastavok.ru/rest/events/v1/groupingByProposedType";

   private static HashMap<String, HashMap<Double, Double>> foras;


   @Autowired
   NewRestTemplate newRestTemplate;


    /*
        достаем информацию по форам на событие
    */
    /*
    private static HashMap<String, HashMap<Double, Double>> getForasOnThisEvent(JSONObject event) {
        HashMap<String, HashMap<Double, Double>> response = new HashMap<>();
        HashMap<Double, Double> team1Foras = new HashMap<>();
        HashMap<Double, Double> team2Foras = new HashMap<>();
        try {
            HashMap totals = (HashMap) event.getJSONObject("outcomes").getJSONObject("main").getJSONObject("nsub").toMap();
            Set<String> keys = totals.keySet();
            for (String key : keys) {
                HashMap total = (HashMap) totals.get(key);
                String check = (String) total.get("title");
                if(check.equals("Фора")){
                    HashMap t = (HashMap) total.get("nsub");
                    HashMap team1 = (HashMap) t.get("1");
                    Double k = Double.valueOf(team1.get("adValue").toString());
                    Double coef = Double.valueOf(team1.get("value").toString());
                    team1Foras.put(k, coef);
                    HashMap team2 = (HashMap) t.get("2");
                    k = Double.valueOf(team2.get("adValue").toString());
                    coef = Double.valueOf(team2.get("value").toString());
                    team2Foras.put(k, coef);
                }
            }
        }catch (Exception e){}


        response.put(CoefIdList.ALL_MATCH_TEAM_1_FORAS, team1Foras);
        response.put(CoefIdList.ALL_MATCH_TEAM_2_FORAS, team2Foras);

        return response;
    }
    */

    /*
        достаем информацию по тоталам на событие
    */
    private  HashMap<String, HashMap<Double, Double>> getTotalsAndForasOnThisEvent(JSONObject event) {
        HashMap<String, HashMap<Double, Double>> response = new HashMap<>();
        HashMap<Double, Double> totalsO = new HashMap<>();
        HashMap<Double, Double> totalsU = new HashMap<>();
        HashMap<Double, Double> team1Foras = new HashMap<>();
        HashMap<Double, Double> team2Foras = new HashMap<>();
        foras = new HashMap<>();

            try {
                HashMap totals = (HashMap) event.getJSONObject("outcomes").getJSONObject("main").getJSONObject("nsub").toMap();
                Set<String> keys = totals.keySet();
                for (String key : keys) {
                    HashMap total = (HashMap) totals.get(key);
                    String check = (String) total.get("title");
                    switch (check){
                        case "Тотал":
                            HashMap t = (HashMap) total.get("nsub");
                            HashMap gross = (HashMap) t.get("gross");
                            Double k = Double.valueOf(gross.get("adValue").toString());
                            Double coef = Double.valueOf(gross.get("value").toString());
                            totalsO.put(k, coef);
                            gross = (HashMap) t.get("less");
                            k = Double.valueOf(gross.get("adValue").toString());
                            coef = Double.valueOf(gross.get("value").toString());
                            totalsU.put(k, coef);
                            break;

                        case "Фора":
                            HashMap t1 = (HashMap) total.get("nsub");
                            HashMap team1 = (HashMap) t1.get("1");
                            Double k1 = Double.valueOf(team1.get("adValue").toString());
                            Double coef1 = Double.valueOf(team1.get("value").toString());
                            team1Foras.put(k1, coef1);
                            HashMap team2 = (HashMap) t1.get("2");
                            k1 = Double.valueOf(team2.get("adValue").toString());
                            coef1 = Double.valueOf(team2.get("value").toString());
                            team2Foras.put(k1, coef1);
                            break;
                    }

                }
            }catch (Exception e){
                //logger.getLOGGER().log(Level.WARNING, "Error while getting totals and foras on event", e);
            }

        response.put(CoefIdList.ALL_MATCH_TOTALS_O, totalsO);
        response.put(CoefIdList.ALL_MATCH_TOTALS_U, totalsU);

        foras.put(CoefIdList.ALL_MATCH_TEAM_1_FORAS, team1Foras);
        foras.put(CoefIdList.ALL_MATCH_TEAM_2_FORAS, team2Foras);

        return response;
    }


    /*
        вытаскиваем основные коэфициенты на событие 1 х 2 1х 12 х2
    */
    private  HashMap<String, Double> getMaincoefsOnThisEvent(JSONObject event) {
        HashMap<String, Double> response = new HashMap<>();
        // 1, x ,2
        try {
            JSONObject eventMainCoefs1 = event.getJSONObject("outcomesWinner").getJSONObject("main").getJSONObject("outcomes");
            try {
                Double One =  eventMainCoefs1.getJSONObject("_1").getDouble("value");
                response.put(CoefIdList.ALL_MATCH_TEAM_1_WIN, One);
            }catch (Exception e){}
            try {
                Double Draw =  eventMainCoefs1.getJSONObject("x").getDouble("value");
                response.put(CoefIdList.ALL_MATCH_DRAW, Draw);
            }catch (Exception e){}
            try {
                Double Two =  eventMainCoefs1.getJSONObject("_2").getDouble("value");
                response.put(CoefIdList.ALL_MATCH_TEAM_2_WIN, Two);
            }catch (Exception e){}
        }catch (Exception e){
          //  logger.getLOGGER().log(Level.WARNING, "Error while getting 1 x 2 on event", e);
        }
        // 1x, 12 ,x2
        try{
            JSONObject eventMainCoefs2 = event.getJSONObject("outcomesDouble").getJSONObject("main").getJSONObject("outcomes");
            try {
                Double drawOrOne =  eventMainCoefs2.getJSONObject("1x").getDouble("value");
                response.put(CoefIdList.ALL_MATCH_DRAW_OR_TEAM_1_WIN, drawOrOne);
            }catch (Exception e){}
            try {
                Double Team1OrTeam2 =  eventMainCoefs2.getJSONObject("_12").getDouble("value");
                response.put(CoefIdList.ALL_MATCH_TEAM_1_OR_TEAM_2_WIN, Team1OrTeam2);
            }catch (Exception e){}
            try {
                Double drawOrTwo =  eventMainCoefs2.getJSONObject("x2").getDouble("value");
                response.put(CoefIdList.ALL_MATCH_DRAW_OR_TEAM_2_WIN, drawOrTwo);
            }catch (Exception e){}
        }catch (Exception e){
           // logger.getLOGGER().log(Level.WARNING, "Error while getting 1x 2x 12 on event", e);
        }



        return response;
    }


    /*
         подгружаем полную информацию о каждом ивенте
    */
    private Event downloadInformationAboutSingleEvent(Long id){
        Event response =null;
        String url = "https://lds-api.ligastavok.ru/rest/events/v1/actionLines";
        HashmapForRest<String, String> parameters = new HashmapForRest<>();
        parameters.put("ids","["+ id +"]");
        JSONObject downloadedEvent= newRestTemplate.requestForLigaStavok(url, parameters.toString());

        try {
            JSONObject event = (JSONObject) downloadedEvent.getJSONArray("result").get(0);
            JSONObject eventInf = event.getJSONObject("event");
            String team1 =  eventInf.getString("team1");
            String team2 = "";
            try {
                team2 =  eventInf.getString("team2");

            } catch (Exception e) { }


                long startTime = event.getLong("gameTs") / 1000;
                String kindOfSport = ligaStavokSportIdLIst.getSportName(event.getInt("gameId"));

                // достаем основные коэфициенты на событие
                HashMap<String, Double> coefsOnEvent = getMaincoefsOnThisEvent(event);
                // достаем информацию по тоталам на событие
                HashMap<String, HashMap<Double, Double>> totalsOnThisEvent = getTotalsAndForasOnThisEvent(event);
                // достаем информацию по форам на событие
                HashMap<String, HashMap<Double, Double>> forasOnThisEvent = foras;

                switch (kindOfSport) {
                    case LigaStavokSportIdLIst.FOOTBALL:
                        response = new FootballMatch(Math.toIntExact(id), team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent,forasOnThisEvent);
                        break;

                    case LigaStavokSportIdLIst.BASKETBALL:
                        response = new BasketballMatch(Math.toIntExact(id), team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                        break;

                    case LigaStavokSportIdLIst.VOLLEYBALL:
                        response = new VolleyballMatch(Math.toIntExact(id), team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                        break;

                    case LigaStavokSportIdLIst.HOCKEY:
                        response = new HockeyMatch(Math.toIntExact(id), team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                        break;

                    case LigaStavokSportIdLIst.TENNIS:
                        response = new TennisMatch(Math.toIntExact(id), team1, team2, kindOfSport, BK_NAME, startTime, coefsOnEvent, totalsOnThisEvent, forasOnThisEvent);
                        break;
                }

        }catch (Exception e){
           // logger.getLOGGER().log(Level.WARNING, "Error while creating event object", e);
        }




        return response;}


    /*
       из основного ответа вытаскиваем id событий
   */
    private  List<Long> getAllEventsIds(JSONObject mainResponse){

        JSONArray tournaments = mainResponse.getJSONArray("result");

        List<Long> eventIdsList = new LinkedList<>();
        for (Object tournament : tournaments) {
            JSONObject tourn = (JSONObject) tournament;

            if (!tourn.getString("title").equalsIgnoreCase("Итоги") && !tourn.getString("title").equalsIgnoreCase("Статистика")
                    && !tourn.getString("title").equalsIgnoreCase("Специальное")){

                JSONArray events = tourn.getJSONArray("events");
                for (Object event : events) {
                    JSONObject ev = (JSONObject) event;
                    Long id = ev.getLong("id");
                    eventIdsList.add(id);
                }
            }


        }
        return eventIdsList;}


    /*
       Разбираем полученные данные по ивентам спортивных событий
    */
    private  HashMap<String, List<Event>> getAllEvents(JSONObject mainResponse){
        HashMap<String, List<Event>> response = new HashMap<>();
        List<Event> footballMatches = new LinkedList<>();
        List<Event> basketballMatches = new LinkedList<>();
        List<Event> volleyballMatches = new LinkedList<>();
        List<Event> tennisMatches = new LinkedList<>();
        List<Event> hockeyMatches = new LinkedList<>();


        //из основного ответа вытаскиваем id событий
        List<Long> eventIdsList = getAllEventsIds(mainResponse);

        for (Long id : eventIdsList) {
            // загружаем полную ифнормацию по каждому ивенту и потом сохраняем их по спискам
            Event event = downloadInformationAboutSingleEvent(id);


                try {
                    if(!event.getTeam2().equals("")) {
                        switch (event.getKindOfSport()) {

                            case LigaStavokSportIdLIst.FOOTBALL:
                                footballMatches.add(event);
                                break;

                            case LigaStavokSportIdLIst.BASKETBALL:
                                basketballMatches.add(event);
                                break;

                            case LigaStavokSportIdLIst.VOLLEYBALL:
                                volleyballMatches.add(event);
                                break;

                            case LigaStavokSportIdLIst.HOCKEY:
                                hockeyMatches.add(event);
                                break;

                            case LigaStavokSportIdLIst.TENNIS:
                                tennisMatches.add(event);
                                break;
                        }
                    }
                }catch (Exception e){ }



        }

        response.put(LigaStavokSportIdLIst.FOOTBALL, footballMatches);
        response.put(LigaStavokSportIdLIst.BASKETBALL, basketballMatches);
        response.put(LigaStavokSportIdLIst.VOLLEYBALL, volleyballMatches);
        response.put(LigaStavokSportIdLIst.TENNIS, tennisMatches);
        response.put(LigaStavokSportIdLIst.HOCKEY, hockeyMatches);
    return response;}



    /*
        основная функция парсинга
     */
    public  HashMap<String, List<Event>> startParsing(){

        HashMap<String, List<Event>> EventList = new HashMap<>();


        try{
            /*
           совершаем POST запрос к серверу Лиги ставок и получаем полный json
            */
            String par = "{\"limit\":10,\"sportIds\":[31,33,34,128,25],\"categoryIds\":[],\"tournamentIds\":[]}";
            HashmapForRest<String, String> params = new HashmapForRest<>();
            params.put("limit", "10");
            params.put("sportIds", "[31,33,34,128,25]");
            params.put("categoryIds", "[]");
            params.put("tournamentIds", "[]");
            JSONObject mainResponse = newRestTemplate.requestForLigaStavok(URL, params.toString());
            /*
            Разбираем полученные данные по ивентам спортивных событий
             */
            EventList = getAllEvents(mainResponse);

        }catch (Exception e){
            e.printStackTrace();
           // logger.getLOGGER().log(Level.WARNING, "Error, no events from Liga stavok will be saved", e);
        }




        return EventList;
    }


}
