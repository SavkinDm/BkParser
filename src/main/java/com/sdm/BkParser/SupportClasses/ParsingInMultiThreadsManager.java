package com.sdm.BkParser.SupportClasses;


import com.sdm.BkParser.Entity.Event;
import com.sdm.BkParser.Repositories.EventsRepo;
import com.sdm.BkParser.Service.Parsers.ParserInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class ParsingInMultiThreadsManager {

    @Autowired
    EventsRepo eventsRepo;


    @Async
    public void parse(ParserInt parser) {
        long start = System.currentTimeMillis();
        System.out.println("Start parsing " + parser.getClass().getName());
        HashMap<String, List<Event>> events = parser.startParsing();
        eventsRepo.saveAllEvents(events);
        long finish = System.currentTimeMillis();
        System.out.println(parser.getClass().getName() + " was parsed in " + (finish - start) / 1000 + " seconds");

    }
}
