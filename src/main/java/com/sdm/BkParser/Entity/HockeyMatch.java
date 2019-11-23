package com.sdm.BkParser.Entity;

import com.sdm.BkParser.Service.Parsers.Fonbet.CoefIdList;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.HashMap;

@Data
@Entity
@Table(name = "hockeyMatch")
public class HockeyMatch extends Event {

    //весь матч
    private Double allMatchTeam1Win;         //1
    private Double allMatchTeam2Win;         //2
    private Double allMatchDraw;             //x
    private Double allMatchDrawOrTeam1Win;   //1x
    private Double allMatchDrawOrTeam2Win;   //2x
    private Double allMatchTeam1OrTeam2Win;  //12


    //тотал на весь матч общий
    private HashMap<Double, Double> allMatchTotalsU;

    private HashMap<Double, Double> allMatchTotalsO;

    private HashMap<Double, Double> allMatchTeam1Foras;

    private HashMap<Double, Double> allMatchTeam2Foras;


    public HockeyMatch(int Id, String team1, String team2, String kindOfSport, String bkName, long startTime,
                       HashMap<String, Double> coefs, HashMap<String, HashMap<Double, Double>> totalsOnThisEvent, HashMap<String, HashMap<Double, Double>> forasOnThisEvent) {
        super(Id, team1, team2, kindOfSport, bkName, startTime);

        this.allMatchTeam1Win = coefs.get(CoefIdList.ALL_MATCH_TEAM_1_WIN);
        this.allMatchTeam2Win = coefs.get(CoefIdList.ALL_MATCH_TEAM_2_WIN);
        this.allMatchDraw = coefs.get(CoefIdList.ALL_MATCH_DRAW);
        this.allMatchDrawOrTeam1Win = coefs.get(CoefIdList.ALL_MATCH_DRAW_OR_TEAM_1_WIN);
        this.allMatchDrawOrTeam2Win = coefs.get(CoefIdList.ALL_MATCH_DRAW_OR_TEAM_2_WIN);
        this.allMatchTeam1OrTeam2Win = coefs.get(CoefIdList.ALL_MATCH_TEAM_1_OR_TEAM_2_WIN);

        this.allMatchTotalsO = totalsOnThisEvent.get(CoefIdList.ALL_MATCH_TOTALS_O);
        this.allMatchTotalsU = totalsOnThisEvent.get(CoefIdList.ALL_MATCH_TOTALS_U);

        this.allMatchTeam1Foras = forasOnThisEvent.get(CoefIdList.ALL_MATCH_TEAM_1_FORAS);
        this.allMatchTeam2Foras = forasOnThisEvent.get(CoefIdList.ALL_MATCH_TEAM_2_FORAS);

    }


    public HockeyMatch() {
    }


}
