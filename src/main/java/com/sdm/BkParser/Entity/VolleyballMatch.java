package com.sdm.BkParser.Entity;

import com.sdm.BkParser.Service.Parsers.Fonbet.CoefIdList;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;

@Data
@Entity
@Table(name = "volleyballMatch")
public class VolleyballMatch extends Event {

    private Double allMatchTeam1Win;
    private Double allMatchTeam2Win;

    @Transient
    private Double firstSetTeam1Win;      // 1-й сет 1
    @Transient
    private Double firstSetTeam2Win;      // 1-й сет 2


    //тотал на весь матч общий
    private HashMap<Double, Double> allMatchTotalsU;

    private HashMap<Double, Double> allMatchTotalsO;

    private HashMap<Double, Double> allMatchTeam1Foras;

    private HashMap<Double, Double> allMatchTeam2Foras;


    public VolleyballMatch(int Id, String team1, String team2, String kindOfSport, String bkName, long startTime, HashMap<String, Double> coefs,
                           HashMap<String, HashMap<Double, Double>> totalsOnThisEvent, HashMap<String, HashMap<Double, Double>> forasOnThisEvent) {

        super(Id, team1, team2, kindOfSport, bkName, startTime);

        this.allMatchTeam1Win = coefs.get(CoefIdList.ALL_MATCH_TEAM_1_WIN);
        this.allMatchTeam2Win = coefs.get(CoefIdList.ALL_MATCH_TEAM_2_WIN);
        this.firstSetTeam1Win = coefs.get(CoefIdList.FIRST_SET_TEAM_1_WIN);
        this.firstSetTeam2Win = coefs.get(CoefIdList.FIRST_SET_TEAM_2_WIN);


        this.allMatchTotalsO = totalsOnThisEvent.get(CoefIdList.ALL_MATCH_TOTALS_O);
        this.allMatchTotalsU = totalsOnThisEvent.get(CoefIdList.ALL_MATCH_TOTALS_U);

        this.allMatchTeam1Foras = forasOnThisEvent.get(CoefIdList.ALL_MATCH_TEAM_1_FORAS);
        this.allMatchTeam2Foras = forasOnThisEvent.get(CoefIdList.ALL_MATCH_TEAM_2_FORAS);


    }


    public VolleyballMatch(int Id, String team1, String team2, String kindOfSport, String bkName, int startTime) {
        super(Id, team1, team2, kindOfSport, bkName, startTime);
    }

    public VolleyballMatch(int id, String name, String team1, String team2, String kindOfSport, String bkName, long startTime) {
        super(id, name, team1, team2, kindOfSport, bkName, startTime);
    }

    public VolleyballMatch(int Id, String team1, String team2, String kindOfSport, String bkName, long startTime, HashMap<String, HashMap<Double, Double>> totalsOnThisEvent) {
        super(Id, team1, team2, kindOfSport, bkName, startTime);
    }

    public VolleyballMatch() {
    }


}
