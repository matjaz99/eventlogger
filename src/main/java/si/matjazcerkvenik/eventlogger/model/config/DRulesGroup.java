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
package si.matjazcerkvenik.eventlogger.model.config;

import java.util.List;

public class DRulesGroup {

    private String name;

    private String endpoint;

    private List<DRule> rules;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public List<DRule> getRules() {
        return rules;
    }

    public void setRules(List<DRule> rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        return "DRulesGroup{" +
                "name='" + name + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", rules=" + rules +
                '}';
    }
}
