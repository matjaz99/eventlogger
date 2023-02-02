package si.matjazcerkvenik.eventlogger.util;

import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    public static void main(String[] args) {

        tryRegex();
        tryGrok();

    }

    private static void tryGrok() {

        GrokCompiler grokCompiler = GrokCompiler.newInstance();
        grokCompiler.registerDefaultPatterns();

//        String line = "time=\"2022-08-06T15:04:49.297937545+02:00\" level=error msg=\"error sending message to peer\" error=\"rpc error: code = Unavailable desc = connection error: desc = \\\"transport: Error while dialing dial tcp 192.168.0.191:2377: connect: no route to host\\\"\"";
//        String pattern = "error"; // not found
//        String pattern = "%{GREEDYDATA}error%{GREEDYDATA}"; // found
//        String pattern = "(.*)\\{error\\}(.*)"; // not found
//        String pattern = "^(.*)\b(error)\b(.*)$"; // not found
//        String pattern = ".*(?>error).*"; // not found
//        String pattern = "(?<name0>.*)error(?<name1>.*)"; // found; the same as GREEDYDATA

        String line = "New session 7 of user root.";
        String pattern = "New session %{INT} of user root."; // found

        final Grok grok = grokCompiler.compile(pattern);
        Match gm = grok.match(line);
        System.out.println("GROK PATTERN: " + grok.getNamedRegex());
        final Map<String, Object> capture = gm.capture();
        if (capture.size() == 0) {
            System.out.println("grok nothing found");
            return;
        }
        for (String s : capture.keySet()) {
            System.out.println("GROK RESULT: " + capture.get(s).toString());
        }

    }

    private static void tryRegex() {

        String line = "CMDOUT ERROR (/opt/monis/integrations/file_collector/scripts/monitor_file_checksum.sh: line 12: md5: command not found)";
        String pattern = "(?i)error"; // found

//        String line = "New session 7 of user root.";
//        String pattern = "New session \\d+ of user root"; // found

//        String line = "(root) CMDOUT (/opt/monis/integrations/file_collector/scripts/monitor_file_checksum.sh: line 12: md5: command not found)";
//        String pattern = "command not found"; // found

//        String line = "(root) CMDOUT (/opt/monis/integrations/file_collector/scripts/monitor_file_checksum.sh: line 12: md5: command not found)";
//        String pattern = "command not found"; // found

        Pattern patternObj = Pattern.compile(pattern);
        System.out.println("REGEX PATTERN: " + patternObj.toString());
        Matcher matcher = patternObj.matcher(line);
        boolean matchFound = matcher.find();
        if (matchFound) {
            System.out.println("regex match found");
        } else {
            System.out.println("regex match not found");
        }

    }

}
