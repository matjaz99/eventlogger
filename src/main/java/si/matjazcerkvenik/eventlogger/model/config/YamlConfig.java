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

public class YamlConfig {

    private String version;

    private List<DRulesGroup> groups;


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<DRulesGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<DRulesGroup> groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        return "YamlConfig{" +
                "version='" + version + '\'' +
                ", groups=" + groups +
                '}';
    }
}
