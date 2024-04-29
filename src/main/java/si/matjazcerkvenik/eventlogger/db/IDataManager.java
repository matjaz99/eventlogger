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
import si.matjazcerkvenik.eventlogger.model.DRequest;

import java.util.List;
import java.util.Map;

public interface IDataManager {

    public String getClientName();

    public void addEvents(List<DEvent> eventList);

    public List<DEvent> getEvents(DFilter filter);

    public Map<String, Integer> getTopEventsByHosts(int limit);

    public Map<String, Integer> getTopEventsByIdent(int limit);

    public List<String> getDistinctKeys(String key);

    public void cleanDB();

    public void close();

}
