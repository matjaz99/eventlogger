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

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigReader {

    public static YamlConfig loadProvidersYamlConfig(String path) {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(new Constructor(YamlConfig.class), representer);
        File f = new File(path);
        try {
            if (!f.exists()) throw new FileNotFoundException();
            InputStream inputStream = new FileInputStream(f);
            YamlConfig config = yaml.load(inputStream);
            LogFactory.getLogger().info("event rules loaded: " + f.getAbsolutePath());
            if (config != null) LogFactory.getLogger().info("YAML config: " + config.toString());
            verifyConfigs(config.getGroups());
            return config;
        } catch (FileNotFoundException e) {
            LogFactory.getLogger().warn("ConfigReader: no rules file found at " +  path);
        } catch (ConfigException e) {
            LogFactory.getLogger().error("ConfigReader: Error loading rules file: " + e.getMessage());
        } catch (Exception e) {
            LogFactory.getLogger().error("ConfigReader: ", e);
        }
        return null;
    }

    /**
     * Check all parameters if they suit the selected provider type and set default values where needed.
     */
    public static void verifyConfigs(List<DRulesGroup> rulesGroups) throws ConfigException {

        if (rulesGroups == null) throw new ConfigException("groups config is null");

        if (rulesGroups.isEmpty()) throw new ConfigException("groups config is empty");

        for (DRulesGroup g : rulesGroups) {

            if (g.getRules() == null) throw new ConfigException("rules config is null");

            if (g.getRules().isEmpty()) throw new ConfigException("rules config is empty");

            LogFactory.getLogger().info("checking group: " + g.getName());

            for (DRule r : g.getRules()) {
                // check mandatory parameters
                LogFactory.getLogger().info("checking rule: " + r.getName());
                if (r.getName() == null) throw new ConfigException("missing rule name");
                if (r.getPattern() == null) throw new ConfigException("missing rule pattern");
            }

        }

    }

    /**
     * Check if param exists. If not, then fill it with default value.
     * @param params
     * @param defaultValue
     */
    private static void checkParam(Map<String, Object> params, String paramName, String defaultValue) {
        Object p = params.get(paramName);
        if (p == null) {
            params.put(paramName, defaultValue);
            LogFactory.getLogger().warn("param " + paramName + " is missing; default will be used: " + defaultValue);
        }
    }

}
