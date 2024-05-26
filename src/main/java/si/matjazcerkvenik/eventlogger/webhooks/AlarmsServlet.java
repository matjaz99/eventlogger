package si.matjazcerkvenik.eventlogger.webhooks;

import si.matjazcerkvenik.eventlogger.util.AlarmMananger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(
        name = "AlarmsServlet",
        description = "Serving alerts",
        urlPatterns = "/alarms"
)
public class AlarmsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String json = AlarmMananger.getInstance().toJsonStringAllActiveAlarms();
        resp.getWriter().println(json);

    }

}
