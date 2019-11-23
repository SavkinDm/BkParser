package com.sdm.BkParser.Entity;


import com.sdm.BkParser.SupportClasses.SportIdList;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.MappedSuperclass;
import java.util.Objects;

@Data
@MappedSuperclass
public class Event {
    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"ID\"", unique = true, nullable = false)
    protected int id;
    protected String Name;
    protected String team1;
    protected String team2;
    protected String kindOfSport;
    protected String bkName;
    protected long startTime;


    public Event() {
    }


    /*
        Формируем название для события оно же название для таблицы
     */
    protected static String makeEventName(String kindOfSport, String team1, String team2, long startTime) {
        String finalName = "";
        try {
            finalName = kindOfSport + deleteAllBadSymbols(team1) + deleteAllBadSymbols(team2) + startTime;
            finalName = finalName.toLowerCase();
            finalName = finalName.replaceAll(" ", "");
        } catch (Exception ignored) {
        }

        return finalName;
    }

    private static String deleteAllBadSymbols(String str) {
        char[] s = str.toCharArray();

        StringBuilder response = new StringBuilder();
        for (char c : s) {

            if (Character.isLetterOrDigit(c))
                response.append(c);
        }

        return response.toString();

    }


    /*
    Ищем название вида спорта по его id
     */
    protected static String checkKindOfSport(int sportId) {
        SportIdList sportIdList = new SportIdList();

        return sportIdList.getSportName(sportId);
    }

    /*
    Конструктор чтобы забивы
     */

    public Event(int Id, String team1, String team2, String kindOfSport, String bkName, int startTime) {
        this.id = Id;
        this.team1 = team1;
        this.team2 = team2;
        this.kindOfSport = kindOfSport;
        Name = makeEventName(this.kindOfSport, team1, team2, startTime);
        this.bkName = bkName;
        this.startTime = startTime;

    }


    public Event(int id, String name, String team1, String team2, String kindOfSport, String bkName, long startTime) {
        this.id = id;
        Name = name;
        this.team1 = team1;
        this.team2 = team2;
        this.kindOfSport = kindOfSport;
        this.bkName = bkName;
        this.startTime = startTime;

    }

    public Event(int Id, String team1, String team2, String kindOfSport, String bkName, long startTime) {
        this.id = Id;
        this.team1 = team1;
        this.team2 = team2;
        this.kindOfSport = kindOfSport;
        Name = makeEventName(this.kindOfSport, team1, team2, startTime);
        this.bkName = bkName;
        this.startTime = startTime;


    }

    @Override
    public boolean equals(Object o) {
        Event event = (Event) o;
        String te11 = ((Event) o).getTeam1();
        String te12 = ((Event) o).getTeam2();
        String te21 = this.team1;
        String te22 = this.team2;
        te11 = te11.toLowerCase();
        te11 = te11.replaceAll(" ", "");
        te12 = te12.toLowerCase();
        te12 = te12.replaceAll(" ", "");
        te21 = te21.toLowerCase();
        te21 = te21.replaceAll(" ", "");
        te22 = te22.toLowerCase();
        te22 = te22.replaceAll(" ", "");
        boolean names = isEquals(this.Name, ((Event) o).getName(), 0.5);
        boolean team1 = false;
        boolean team2 = false;
        boolean check1 = false;
        boolean check2 = false;

        try {
            team1 = isEquals(te21, te11, 0.75);
            check1 = isEquals(te21, te11, 0.55);

        } catch (Exception ignored) {
        }
        try {
            team2 = isEquals(te22, te12, 0.75);
            check2 = isEquals(te22, te12, 0.55);
        } catch (Exception ignored) {
        }


        boolean startTime;

        startTime = this.startTime == event.startTime;
        return startTime && names && (team1 || team2) && (check1 && check2);
    }


    private boolean isEquals(String firstName, String secondName, double coef) {
        int equalSubtokensCount = 0;
        int SubtokenLength = 2;
        boolean[] usedTokens = new boolean[secondName.length() - SubtokenLength + 1];
        for (int i = 0; i < firstName.length() - SubtokenLength + 1; ++i) {
            String subtokenFirst = firstName.substring(i, SubtokenLength + i);
            for (int j = 0; j < secondName.length() - SubtokenLength + 1; ++j) {
                if (!usedTokens[j]) {
                    String subtokenSecond = secondName.substring(j, SubtokenLength + j);
                    if (subtokenFirst.equals(subtokenSecond)) {
                        equalSubtokensCount++;
                        usedTokens[j] = true;
                        break;
                    }
                }
            }
        }
        int subtokenFirstCount = firstName.length() - SubtokenLength + 1;
        int subtokenSecondCount = secondName.length() - SubtokenLength + 1;

        double tanimoto = (1.0 * equalSubtokensCount) / (subtokenFirstCount + subtokenSecondCount - equalSubtokensCount);

        return coef <= tanimoto;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Name);
    }


}

