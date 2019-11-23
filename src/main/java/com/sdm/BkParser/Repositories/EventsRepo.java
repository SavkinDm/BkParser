package com.sdm.BkParser.Repositories;


import com.sdm.BkParser.Entity.*;
import com.sdm.BkParser.SupportClasses.SportIdList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
@Scope("singleton")
public class EventsRepo {

    @Autowired
    footballMatchRepo footballMatchrepo;

    @Autowired
    basketballMatchRepo basketballMatchRepo;

    @Autowired
    volleyballMatchRepo volleyballMatchRepo;

    @Autowired
    tennisMatchRepo tennisMatchRepo;

    @Autowired
    hockeyMatchRepo hockeyMatchRepo;

    public EventsRepo() {
    }

    @Transactional
    public void saveAllEvents(HashMap<String, List<Event>> events) {

        List<FootballMatch> footballEvents = casting(SportIdList.FOOTBALL, events.get(SportIdList.FOOTBALL));
        footballMatchrepo.saveAll(footballEvents);

        List<VolleyballMatch> volleyballEvents = casting(SportIdList.VOLLEYBALL, events.get(SportIdList.VOLLEYBALL));
        volleyballMatchRepo.saveAll(volleyballEvents);

        List<BasketballMatch> basketballEvents = casting(SportIdList.BASKETBALL, events.get(SportIdList.BASKETBALL));
        basketballMatchRepo.saveAll(basketballEvents);

        List<TennisMatch> tennisEvents = casting(SportIdList.TENNIS, events.get(SportIdList.TENNIS));
        tennisMatchRepo.saveAll(tennisEvents);

        List<HockeyMatch> hockeyEvents = casting(SportIdList.HOCKEY, events.get(SportIdList.HOCKEY));
        hockeyMatchRepo.saveAll(hockeyEvents);

    }

    private <classname> List<classname> casting(String kindOfSport, List<Event> events) {

        List<classname> matches = new ArrayList<>();
        for (Event ev : events) {
            matches.add((classname) ev);

        }
        return matches;

    }

}
