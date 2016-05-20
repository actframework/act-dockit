package dockit;

import act.boot.app.RunApp;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Application {
    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger("act");
        logger.setLevel(Level.WARNING);

        logger = Logger.getLogger("act.dockit");
        logger.setLevel(Level.INFO);

        logger = Logger.getLogger("act.job");
        logger.setLevel(Level.INFO);

        logger = Logger.getLogger("org.xnio");
        logger.setLevel(Level.SEVERE);

        RunApp.start("dockit", "0.1.2", Application.class);
    }
}
