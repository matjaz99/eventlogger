package si.matjazcerkvenik.eventlogger.db;

import si.matjazcerkvenik.eventlogger.util.DProps;
import si.matjazcerkvenik.eventlogger.util.LogFactory;

public class RequestsProcessorThread extends Thread {

    public RequestsProcessorThread(String name) {
        super(name);
    }

    @Override
    public void run() {

        while (true) {

            try {
                Thread.sleep(444);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            EventQueue.getInstance().processRequestsQueue();

        }

    }

}
