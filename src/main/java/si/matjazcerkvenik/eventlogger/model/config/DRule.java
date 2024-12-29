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

import java.util.Map;

public class DRule {

    private String name;
    private boolean enabled = true;
    private Map<String, String> pattern;
    private Map<String, String> filter;
    private Map<String, String> action;
    private long hits = 0L;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, String> getPattern() {
        return pattern;
    }

    public void setPattern(Map<String, String> pattern) {
        this.pattern = pattern;
    }

    public Map<String, String> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, String> filter) {
        this.filter = filter;
    }

    public Map<String, String> getAction() {
        return action;
    }

    public void setAction(Map<String, String> action) {
        this.action = action;
    }

    public void increaseHits() {
        hits++;
    }

    public void resetHits() {
        hits = 0;
    }

    public long getHits() {
        return hits;
    }

    @Override
    public String toString() {
        return "DRule{" +
                "name='" + name + '\'' +
                ", enabled=" + enabled +
                ", pattern=" + pattern +
                ", filter=" + filter +
                ", action=" + action +
                '}';
    }
}
