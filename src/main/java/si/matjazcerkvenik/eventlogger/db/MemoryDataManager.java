/*
   Copyright 2021 Matjaž Cerkvenik

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package si.matjazcerkvenik.eventlogger.db;

import si.matjazcerkvenik.eventlogger.model.DEvent;
import si.matjazcerkvenik.eventlogger.model.DFilter;
import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;
import si.matjazcerkvenik.eventlogger.model.DRequest;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import java.util.*;
import java.util.stream.Collectors;

public class MemoryDataManager implements IDataManager {

    private static SimpleLogger logger = LogFactory.getLogger();

    private int clientId = 0;
    private String clientName;
    public static List<DEvent> eventMessages = new LinkedList<>();

    public MemoryDataManager(int id) {
        clientId = id;
        clientName = "Memory[" + clientId + "]";
        logger.info(getClientName() + " initialized");
    }

    @Override
    public String getClientName() {
        return clientName;
    }

    @Override
    public void addEvents(List<DEvent> eventList) {
        logger.info(getClientName() + " addEvents: size=" + eventList.size());
        while (eventMessages.size() + eventList.size() > DProps.EVENTLOGGER_MEMORY_BUFFER_SIZE) {
            eventMessages.remove(0);
        }
        eventMessages.addAll(eventList);
    }

    @Override
    public List<DEvent> getEvents(DFilter filter) {
        logger.info(getClientName() + " getEvents: filter=" + filter);
        // WARNING order is not guaranteed!!!
        if (filter != null) {
            List<DEvent> result = new ArrayList<>();
            if (filter.getHosts() != null && filter.getHosts().length > 0) {
                for (int i = 0; i < filter.getHosts().length; i++) {
                    String h = filter.getHosts()[i];
                    result.addAll(eventMessages.stream()
                            .filter(e -> e.getHost().equals(h))
                            .collect(Collectors.toList()));
                }
            }
            if (filter.getIdents() != null && filter.getIdents().length > 0) {
                for (int i = 0; i < filter.getIdents().length; i++) {
                    String h = filter.getIdents()[i];
                    result.addAll(eventMessages.stream()
                            .filter(e -> e.getIdent().equals(h))
                            .collect(Collectors.toList()));
                }
            }
            return result;
        }
        return eventMessages;
    }

    @Override
    public Map<String, Integer> getTopEventsByHosts(int limit) {
        // not aplicable
        return null;
    }

    @Override
    public Map<String, Integer> getTopEventsByIdent(int limit) {
        // not aplicable
        return null;
    }

    @Override
    public List<String> getDistinctKeys(String key) {
        logger.info(getClientName() + " getDistinctKeys");
        Map<String, String> map = new HashMap<>();
        if (key.equalsIgnoreCase("host")) {
            for (DEvent e : eventMessages) {
                map.put(e.getHost(), null);
            }
            return new ArrayList<String>(map.keySet());
        }
        if (key.equalsIgnoreCase("ident")) {
            for (DEvent e : eventMessages) {
                map.put(e.getIdent(), null);
            }
            return new ArrayList<String>(map.keySet());
        }
        return null;
    }

    @Override
    public void cleanDB() {
        // not applicable
    }

    @Override
    public void close() {
        // not applicable
    }



}
