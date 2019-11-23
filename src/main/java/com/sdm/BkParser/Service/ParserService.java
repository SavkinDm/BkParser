package com.sdm.BkParser.Service;


import com.sdm.BkParser.Repositories.EventsRepo;
import com.sdm.BkParser.Service.Parsers.BetCity.BetCityRestTemplate;
import com.sdm.BkParser.Service.Parsers.FanSport.FanSportRestTemplate;
import com.sdm.BkParser.Service.Parsers.Fonbet.FonbetRestTemplate;
import com.sdm.BkParser.Service.Parsers.Leon.LeonRestTemplate;
import com.sdm.BkParser.Service.Parsers.LigaStavok.LigaStavokRestTemplate;
import com.sdm.BkParser.Service.Parsers.Melbet.MelBetRestTemplate;
import com.sdm.BkParser.Service.Parsers.MostBet.MostBetRestTemplate;
import com.sdm.BkParser.Service.Parsers.OlimpBet.OlimpBetRestTemplate;
import com.sdm.BkParser.SupportClasses.ParsingInMultiThreadsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ParserService {

    @Autowired
    private FonbetRestTemplate fonbetRestTemplate;

    @Autowired
    private LeonRestTemplate leonRestTemplate;

    @Autowired
    private FanSportRestTemplate fanSportRestTemplate;

    @Autowired
    private BetCityRestTemplate betCityRestTemplate;

    @Autowired
    private LigaStavokRestTemplate ligaStavokRestTemplate;

    @Autowired
    private MelBetRestTemplate melBetRestTemplate;

    @Autowired
    private MostBetRestTemplate mostBetRestTemplate;

    @Autowired
    private OlimpBetRestTemplate olimpBetRestTemplate;

    @Autowired
    EventsRepo eventsRepo;

    @Autowired
    ParsingInMultiThreadsManager threadsManager;


    public ParserService() {

    }

    @Scheduled
    public void startWorking() {

        threadsManager.parse(fonbetRestTemplate);
        threadsManager.parse(leonRestTemplate);
        threadsManager.parse(fanSportRestTemplate);
        threadsManager.parse(betCityRestTemplate);
        threadsManager.parse(ligaStavokRestTemplate);
        threadsManager.parse(melBetRestTemplate);
        threadsManager.parse(mostBetRestTemplate);
        threadsManager.parse(olimpBetRestTemplate);

    }



}
