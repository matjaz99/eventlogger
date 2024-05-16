package si.matjazcerkvenik.eventlogger.web;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import si.matjazcerkvenik.eventlogger.util.LogFactory;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@ManagedBean
@ApplicationScoped
public class AppBean {

    public String getHelp() {

        // resources directory
        String filePath = "HELP.md";

        try {
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
            Parser parser = Parser.builder().build();
            Node document = parser.parseReader(new InputStreamReader(input));
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            return renderer.render(document);
        } catch (IOException e) {
            LogFactory.getLogger().error("getHelp(): IOException: ", e);
        }
        return null;
    }

}
