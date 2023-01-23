package si.matjazcerkvenik.eventlogger.model.config;

import java.util.Map;

public class DRule {

    private String name;
    private Map<String, String> pattern;
    private Map<String, String> filter;
    private Map<String, String> action;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return "DRule{" +
                "name='" + name + '\'' +
                ", pattern=" + pattern +
                ", filter=" + filter +
                ", action=" + action +
                '}';
    }
}
