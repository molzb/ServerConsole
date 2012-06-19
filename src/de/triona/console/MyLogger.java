package de.triona.console;

import java.util.logging.*;

/**
 *
 * @author Bernhard
 */
public class MyLogger {

    private static LogFormatter logFormatter = new LogFormatter();
    private static ConsoleHandler consoleHandler = new ConsoleHandler();
    
    static public void setup(Logger logger) {
        try {
            logger.setLevel(Level.FINE);
            logger.setUseParentHandlers(false);
            consoleHandler.setFormatter(logFormatter);
            logger.addHandler(consoleHandler);
        } catch (Exception ioe) {
            System.err.println(ioe.getMessage());
        }
    }
}
