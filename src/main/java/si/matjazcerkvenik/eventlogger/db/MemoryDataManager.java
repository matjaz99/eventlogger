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
import si.matjazcerkvenik.eventlogger.model.DataFilter;
import si.matjazcerkvenik.eventlogger.util.LogFactory;
import si.matjazcerkvenik.eventlogger.webhooks.WebhookMessage;
import si.matjazcerkvenik.simplelogger.SimpleLogger;

import java.util.LinkedList;
import java.util.List;

public class MemoryDataManager implements IDataManager {

    private static SimpleLogger logger = LogFactory.getLogger();

    private int clientId = 0;

    public static List<WebhookMessage> webhookMessages = new LinkedList<>();
    public static List<DEvent> eventMessages = new LinkedList<>();

    public MemoryDataManager(int id) {
        clientId = id;
        logger.info("MemoryDataManager: " + getClientName() + " initialized");
    }

    @Override
    public String getClientName() {
        return "Memory[" + clientId + "]";
    }

    @Override
    public void addWebhookMessage(WebhookMessage webhookMessage) {
        webhookMessages.add(webhookMessage);
        if (webhookMessages.size() > 100) {
            webhookMessages.remove(0);
        }
    }

    @Override
    public List<WebhookMessage> getWebhookMessages() {
        return webhookMessages;
    }

    @Override
    public void addEvents(List<DEvent> eventList) {
        if (eventMessages.size() > 1000) {
            for (int i = 0; i < eventList.size(); i++) {
                eventMessages.remove(0);
            }
        }
        eventMessages.addAll(eventList);
        logger.info("MemoryDataManager: " + getClientName() + " new events added");
    }

    @Override
    public List<DEvent> getEvents(DataFilter filter) {
        logger.info("MemoryDataManager: " + getClientName() + " get events list");
        // TODO
        return eventMessages;
    }

    @Override
    public List<String> getAvailableHosts() {
        // TODO
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
