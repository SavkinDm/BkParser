package com.sdm.BkParser.Service.Parsers.MostBet;

import com.sdm.BkParser.Entity.*;
import com.sdm.BkParser.Service.Parsers.Fonbet.CoefIdList;
import com.sdm.BkParser.Service.Parsers.ParserInt;
import com.sdm.BkParser.SupportClasses.Rest.NewRestTemplate;
import com.sdm.BkParser.SupportClasses.SportIdList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Service
public class MostBetRestTemplate implements ParserInt {

    private static final String URL_FOOTBALL = "https://mostbet.ru/line/list?t[]=2&t[]=1&l=100&lc[]=1&of=";
    private static final String URL_BASKETBALL = "https://mostbet.ru/line/list?t[]=2&t[]=1&l=100&lc[]=7&of=";
    private static final String URL_VOLLEYBALL = "https://mostbet.ru/line/list?t[]=2&t[]=1&l=100&lc[]=13&of=";
    private static final String URL_TENNIS = "https://mostbet.ru/line/list?t[]=2&t[]=1&l=100&lc[]=3&of=";
    private static final String URL_HOCKEY = "https://mostbet.ru/line/list?t[]=2&t[]=1&l=100&lc[]=5&of=";

    private static final String URL_SECOND = "https://mostbet.ru/line/";
    private static final String BK_NAME = "MostBet";


    @Autowired
    NewRestTemplate newRestTemplate;


    public  HashMap<String, List<Event>> startParsing(){
        HashMap<String, List<Event>> response = new HashMap<>();

        List<Event> volEvents = new LinkedList<>();

        response.put(SportIdList.FOOTBALL, kuchaZaprosov(URL_FOOTBALL, SportIdList.FOOTBALL));
        response.put(SportIdList.HOCKEY,  kuchaZaprosov(URL_HOCKEY, SportIdList.HOCKEY));
        response.put(SportIdList.BASKETBALL,  kuchaZaprosov(URL_BASKETBALL, SportIdList.BASKETBALL));
        response.put(SportIdList.TENNIS,  kuchaZaprosov(URL_TENNIS, SportIdList.TENNIS));
        response.put(SportIdList.VOLLEYBALL, volEvents);




        return response;
    }

    private  List<Event> kuchaZaprosov(String url, String kindOfSport){
        int i=0;
        int t=333;
        List<Event> footballMatches=null;
        JSONObject footballResponse;
        List<Event> allMatchesFootball = new LinkedList<>();

        do{
            footballResponse = newRestTemplate.getForJSON(url+i);
            try{
                footballMatches = getAllEvents(footballResponse, kindOfSport);
                for (Event event : footballMatches) {
                    if(!allMatchesFootball.contains(event)){
                        allMatchesFootball.add(event);
                    }
                }
            }catch (Exception e){
                t = footballResponse.getInt("line_count");
            }
        i++;
        }while (i!=1);

        return  footballMatches;
    }


    private  List<Event> getAllEvents(JSONObject mainResponce, String kindOfSport) {
        List<Event> responce = new ArrayList<>();


        JSONArray lines =  mainResponce.getJSONArray("lines_hierarchy");
        JSONObject line=null;
        for (Object o: lines) {
            line = (JSONObject) o;
            if(line.getInt("type")==1){
                break;
            }
        }
        JSONObject ev= (JSONObject) line.getJSONArray("line_category_dto_collection").get(0);
        JSONArray evList = ev.getJSONArray("line_supercategory_dto_collection");
        List<JSONObject> resp = new ArrayList<>();
        for (Object or : evList) {
            JSONObject jsonObject = (JSONObject) or;
            JSONObject er = (JSONObject) jsonObject.getJSONArray("line_subcategory_dto_collection").get(0);
            JSONObject re = (JSONObject) er.getJSONArray("line_dto_collection").get(0);
            resp.add(re);
        }
        int i=0;
        for (JSONObject object : resp) {
            try {
                i++;
                Event event = downloadAndSaveEvent(object, kindOfSport);

                responce.add(event);
            } catch (Exception ignored) {
            }
        }

        return responce;
    }

    private  Event downloadAndSaveEvent(JSONObject object, String kindOfSport) {
        JSONObject ev = object.getJSONObject("match");
        String team1 = ev.getJSONObject("team1").getString("title");
        String team2 = ev.getJSONObject("team2").getString("title");
        Long startTime = Long.valueOf(String.valueOf(ev.getInt("begin_at")));
        int id = object.getInt("id");
        JSONArray outcomes = object.getJSONArray("outcomes");
        // достаем основные коэфициенты 1 х 2 1х 12 х2
        HashMap<String, Double> coefsOnEvent = getMainCoefs(outcomes);
        // достаем тоталы на это событие
        HashMap<String, HashMap<Double, Double>> totalsOnThisEvent = getTotals(outcomes, id);
        //достаем форы на это событие
        HashMap<String, HashMap<Double, Double>> forasOnThisEvent = getForas(outcomes, id);
        Event event = null;
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

        return event;
    }

    private  HashMap<String, HashMap<Double, Double>> getForas(JSONArray outcomes, int id) {
        HashMap<String, HashMap<Double, Double>> resp = new HashMap<>();
        HashMap<String, HashMap<Double, Double>> resp2 = new HashMap<>();
        HashMap<Double, Double> team1Foras = new HashMap<>();
        HashMap<Double, Double> team2Foras = new HashMap<>();
        double tit = -1000000;
        double tit2 = -1000000;
        try{
            // берем 1 основную фору
            for (Object ob : outcomes) {
                JSONObject object = (JSONObject) ob;
                int pId = object.getInt("type_id");
                String title = object.getString("alias");
                double coef=0;
                if(pId == 0 && title.equals("fora_title")){
                    String v = object.getString("title");
                    char[] arr = v.toCharArray();
                    char a = arr[arr.length-1];
                    if(Character.isDigit(a)){
                        tit = Double.parseDouble(v);
                        if(tit!=0){
                            tit2 = Double.parseDouble(v)*(-1);
                        }else{
                            tit2 = Double.parseDouble(v);
                        }

                    }else{
                        String k = v.substring(0, v.length()-1);
                        tit = Double.parseDouble(k);
                        if(tit!=0){
                            tit2 = Double.parseDouble(k)*(-1);
                        }else{
                            tit2 = Double.parseDouble(v);
                        }
                    }

                }
            }
            if(tit!= -1000000){
                for (Object ob : outcomes) {
                    JSONObject object = (JSONObject) ob;
                    int pId = object.getInt("type_id");
                    double coef=0;
                    switch (pId){
                        case 453:
                            coef =  object.getDouble("odd");
                            team1Foras.put(tit, coef);
                            break;

                        case 455:
                            coef =  object.getDouble("odd");
                            team2Foras.put(tit2, coef);
                            break;
                    }
                }
                //берем все остальные
                resp2 = getOtherForas(id);
                team1Foras.putAll(resp2.get(CoefIdList.ALL_MATCH_TEAM_1_FORAS));
                team2Foras.putAll(resp2.get(CoefIdList.ALL_MATCH_TEAM_2_FORAS));
            }
        }catch (Exception e){
            //logger.getLOGGER().log(Level.WARNING, "Error while getting foras p 1", e);
        }


        resp.put(CoefIdList.ALL_MATCH_TEAM_1_FORAS, team1Foras);
        resp.put(CoefIdList.ALL_MATCH_TEAM_2_FORAS, team2Foras);
        return resp;
    }

    private  HashMap<String, HashMap<Double, Double>> getOtherForas(int id) {
        HashMap<String, HashMap<Double, Double>> resp = new HashMap<>();
        HashMap<Double, Double> team1Foras = new HashMap<>();
        HashMap<Double, Double> team2Foras = new HashMap<>();

        StringBuilder html = newRestTemplate.getForHTMLString(URL_SECOND+id);
        String[] arr = html.toString().split("data-line-id=");
        for (int i = 0; i < arr.length; i++) {
            String line = arr[i];
            String[] c = line.split("\n");
            for (int j = 0; j < c.length; j++) {
                String kek = c[j];
                try{
                    if(kek.contains("data-group-id=")){
                        String pId =  kek.substring(kek.indexOf("\"")+1, kek.lastIndexOf("\""));
                        int er = Integer.valueOf(pId);
                        if(er == 31){
                            double coef=0;
                            for (int k = 0; k < c.length; k++) {
                                String lol = c[k];

                                if (lol.contains("data-odd=")){
                                    String o = lol.substring(lol.indexOf("\"")+1, lol.lastIndexOf("\""));
                                    coef = Double.parseDouble(o);
                                }
                                try{
                                    String[] str = lol.split("\">");
                                    String[] str2 = str[1].split("</span>");
                                    String type = str2[0];
                                    double her = Double.parseDouble(type.substring(type.indexOf("(")+1, type.indexOf(")")));
                                    String t = type.substring(type.indexOf(" ")+1, type.lastIndexOf(" "));
                                    switch (t){
                                        case "1":
                                            team1Foras.put(her, coef);
                                            break;

                                        case "2":
                                            team2Foras.put(her, coef);
                                            break;
                                    }

                                }catch (Exception e){}
                            }
                        }
                    }
                }catch (Exception e){}



            }


        }


        resp.put(CoefIdList.ALL_MATCH_TEAM_1_FORAS, team1Foras);
        resp.put(CoefIdList.ALL_MATCH_TEAM_2_FORAS, team2Foras);
        return resp;
    }

    private  HashMap<String, HashMap<Double, Double>> getTotals(JSONArray outcomes, int id) {
        HashMap<String, HashMap<Double, Double>> resp = new HashMap<>();
        HashMap<String, HashMap<Double, Double>> resp2 = new HashMap<>();
        HashMap<Double, Double> totalsO = new HashMap<>();
        HashMap<Double, Double> totalsU = new HashMap<>();
        try{
            double tit = -1;
            // берем 1 основной тотал
            for (Object ob : outcomes) {
                JSONObject object = (JSONObject) ob;
                int pId = object.getInt("type_id");
                String title = object.getString("alias");
                double coef=0;
                if(pId == 0 && title.equals("total_title")){
                    try{
                        tit = object.getDouble("title");
                    }catch (Exception ignored){}

                }
            }
            if(tit!= -1){
                for (Object ob : outcomes) {
                    JSONObject object = (JSONObject) ob;
                    int pId = object.getInt("type_id");
                    double coef=0;
                    switch (pId){
                        case 1001:
                            coef =  object.getDouble("odd");
                            totalsO.put(tit, coef);
                            break;

                        case 1003:
                            coef =  object.getDouble("odd");
                            totalsU.put(tit, coef);
                            break;
                    }
                }
                //берем все остальные
                resp2 = getOtherTotals(id);

                totalsO.putAll(resp2.get(CoefIdList.ALL_MATCH_TOTALS_O));
                totalsU.putAll(resp2.get(CoefIdList.ALL_MATCH_TOTALS_U));
            }

        }catch (Exception e){
            //logger.getLOGGER().log(Level.WARNING, "Error while getting totals p 1", e);

        }


       resp.put(CoefIdList.ALL_MATCH_TOTALS_O, totalsO);
       resp.put(CoefIdList.ALL_MATCH_TOTALS_U, totalsU);
        return resp;
    }

    private  HashMap<String, HashMap<Double, Double>> getOtherTotals(int id) {
        HashMap<String, HashMap<Double, Double>> resp = new HashMap<>();
        HashMap<Double, Double> totalsO = new HashMap<>();
        HashMap<Double, Double> totalsU = new HashMap<>();

        StringBuilder html = newRestTemplate.getForHTMLString(URL_SECOND+id);
        String[] arr = html.toString().split("data-line-id=");
        for (int i = 0; i < arr.length; i++) {
            String line = arr[i];
            String[] c = line.split("\n");
            for (int j = 0; j < c.length; j++) {
                String kek = c[j];
                try{
                    if(kek.contains("data-group-id=")){
                        String pId =  kek.substring(kek.indexOf("\"")+1, kek.lastIndexOf("\""));
                        int er = Integer.valueOf(pId);
                        if(er == 7){
                            double coef=0;
                            for (int k = 0; k < c.length; k++) {
                                String lol = c[k];

                                if (lol.contains("data-odd=")){
                                    String o = lol.substring(lol.indexOf("\"")+1, lol.lastIndexOf("\""));
                                    coef = Double.parseDouble(o);
                                }
                                try{
                                    String[] str = lol.split("\">");
                                    String[] str2 = str[1].split("</span>");
                                    String type = str2[0];
                                    double her = Double.parseDouble(type.substring(type.indexOf("(")+1, type.indexOf(")")));
                                    String t = type.substring(type.length()-1);
                                    switch (t){
                                        case "Б":
                                            totalsO.put(her, coef);
                                            break;

                                        case "М":
                                            totalsU.put(her, coef);
                                            break;
                                    }
                                }catch (Exception e){}
                            }
                        }
                    }
                }catch (Exception e){}



            }


        }


        resp.put(CoefIdList.ALL_MATCH_TOTALS_O, totalsO);
        resp.put(CoefIdList.ALL_MATCH_TOTALS_U, totalsU);
        return resp;
    }


    private  HashMap<String, Double> getMainCoefs(JSONArray outcomes) {
        HashMap<String, Double> coefs = new HashMap<>();
        for (Object ob : outcomes) {
            JSONObject object = (JSONObject) ob;
            String id = object.getString("alias");
            double coef=0;
            switch (id){
                case "1":
                      coef =  object.getDouble("odd");
                      coefs.put(CoefIdList.ALL_MATCH_TEAM_1_WIN, coef);
                  break;

                case "x":
                    coef =  object.getDouble("odd");
                    coefs.put(CoefIdList.ALL_MATCH_DRAW, coef);
                break;

                case "2":
                    coef =  object.getDouble("odd");
                    coefs.put(CoefIdList.ALL_MATCH_TEAM_2_WIN, coef);
                    break;

                case "1x":
                    coef =  object.getDouble("odd");
                    coefs.put(CoefIdList.ALL_MATCH_DRAW_OR_TEAM_1_WIN, coef);
                    break;

                case "12":
                    coef =  object.getDouble("odd");
                    coefs.put(CoefIdList.ALL_MATCH_TEAM_1_OR_TEAM_2_WIN, coef);
                    break;

                case "2x":
                    coef =  object.getDouble("odd");
                    coefs.put(CoefIdList.ALL_MATCH_DRAW_OR_TEAM_2_WIN, coef);
                    break;
            }

        }

        return coefs;
    }


}
