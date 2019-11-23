package com.sdm.BkParser.Service.Parsers;

import com.sdm.BkParser.Entity.Event;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public interface ParserInt {

    HashMap<String, List<Event>> startParsing();


}
