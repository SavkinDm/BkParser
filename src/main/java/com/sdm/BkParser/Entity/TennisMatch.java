package com.sdm.BkParser.Entity;

import com.sdm.BkParser.Service.Parsers.Fonbet.CoefIdList;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;

@Data
@Entity
@Table(name = "tennisMatch")
public class TennisMatch extends Event {

    //весь матч
    private Double allMatchPlayer1Win;         //1
    private Double allMatchPlayer2Win;         //2


    //тотал на весь матч общий
    private HashMap<Double, Double> allMatchTotalsU;

    private HashMap<Double, Double> allMatchTotalsO;

    private HashMap<Double, Double> allMatchTeam1Foras;

    private HashMap<Double, Double> allMatchTeam2Foras;


    //1ый сет
    @Transient
    private Double firstSetWin1Player;      // 1-й сет 1
    @Transient
    private Double firstSetWin2Player;      // 1-й сет 2

    public TennisMatch(int Id, String team1, String team2, String kindOfSport, String bkName, long startTime,
                       HashMap<String, Double> coefs, HashMap<String, HashMap<Double, Double>> totalsOnThisEvent, HashMap<String, HashMap<Double, Double>> forasOnThisEvent) {
        super(Id, team1, team2, kindOfSport, bkName, startTime);

        this.allMatchPlayer1Win = coefs.get(CoefIdList.ALL_MATCH_TEAM_1_WIN);
        this.allMatchPlayer2Win = coefs.get(CoefIdList.ALL_MATCH_TEAM_2_WIN);
        this.firstSetWin1Player = coefs.get(CoefIdList.FIRST_SET_TEAM_1_WIN);
        this.firstSetWin2Player = coefs.get(CoefIdList.FIRST_SET_TEAM_2_WIN);

        this.allMatchTotalsO = totalsOnThisEvent.get(CoefIdList.ALL_MATCH_TOTALS_O);
        this.allMatchTotalsU = totalsOnThisEvent.get(CoefIdList.ALL_MATCH_TOTALS_U);

        this.allMatchTeam1Foras = forasOnThisEvent.get(CoefIdList.ALL_MATCH_TEAM_1_FORAS);
        this.allMatchTeam2Foras = forasOnThisEvent.get(CoefIdList.ALL_MATCH_TEAM_2_FORAS);


    }


    public TennisMatch() {
    }


}
