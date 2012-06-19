package de.triona.console;

/*
 * RunCommand.java
 */
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunCommand {

    private static final Logger logger = Logger.getLogger(RunCommand.class.getName());
    private String prefix = "cmd /c ";
    public StringBuilder output = new StringBuilder();
    public static final String EXAMPLE_TEST = "This is my small example string which I'm going to use for pattern matching.";

    public static void main2(String[] args) {
        Pattern pattern = Pattern.compile("\\w+");
        // In case you would like to ignore case sensitivity you could use this statement
        // Pattern pattern = Pattern.compile("\\s+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(EXAMPLE_TEST);
        // Check all occurance
        while (matcher.find()) {
            System.out.print("Start index: " + matcher.start());
            System.out.print(" End index: " + matcher.end() + " ");
            System.out.println(matcher.group());
        }
        // Now create a new pattern and matcher to replace whitespace with tabs
        Pattern replace = Pattern.compile("\\s+");
        Matcher matcher2 = replace.matcher(EXAMPLE_TEST);
        System.out.println(matcher2.replaceAll("\t"));
    }

    public static void main(String[] args) throws IOException {
        String seq = "01:39:22,665 INFO  [org.jboss.web] JBAS018210: Registering web context: /zonk";
        System.out.println(Pattern.matches(".*J.*S.*", seq));
        if (true) {
            return;
        }
        String regex = "[a-z\\.]{1,}";
        System.out.println(Pattern.matches(regex, "abc"));
        System.out.println(Pattern.matches(regex, "abc.def"));
        System.out.println(Pattern.matches(regex, "abc,def"));
        System.out.println(Pattern.matches(regex, "A,def"));
        if (true) {
            return;
        }
        RunCommand runCommand = new RunCommand();
//        Process pr = runCommand.runCommand("dir");
        Process pr = runCommand.runCommand(ConsoleFrame.SERVER_DIR + ConsoleFrame.START_COMMAND);
        runCommand.catchOutput(pr);
        String outputStr = runCommand.output.toString();
//        System.out.println("output=" + outputStr);
//        Matcher m = Pattern.compile("I.*?O").matcher("INFOFO");
        Matcher m2 = Pattern.compile(".*INFO.*", Pattern.MULTILINE).matcher(outputStr);
        while (m2.find()) {
            System.out.println(outputStr.substring(m2.start(), m2.end()));
        }
    }

    public Process runCommand(String cmd) throws IOException {
        Runtime rt = Runtime.getRuntime();
        String cmdWithPrefix = prefix + cmd;
        logger.info("Running " + cmdWithPrefix);
        Process pr = rt.exec(cmdWithPrefix);
        logger.info("catching output");
//        catchOutput(pr);
        return pr;
    }

    private void catchOutput(Process pr) {
        try {
//            FileInputStream fis = new FileInputStream("C:/java/jboss-as-7.1.1.Final/standalone/log/boot.log"); //TEST
//            BufferedReader br = new BufferedReader(new InputStreamReader(fis)); //TEST
            BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
//                System.out.println("  OUT> " + line);
                output.append(line).append("\n");
            }
            System.out.println("done, return value is " + pr.exitValue());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IllegalThreadStateException e) {
            logger.info("ok, process is running asynchronously");
        }
    }
}