package tomcat.server;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;
import tomcat.servlet_context.ServletContext;
import webapps.blogApp.src.servlets.*;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;

public class HttpServer {
    private static final Logger LOGGER = LogManager.getLogger(HttpServer.class);
    private static final String WEB_APP_DIR = "src/webapps/blogApp/src";
    private static final String WEB_XML_URL = "/main/webapp/WEB-INF/web.xml";
    private static final int DEFAULT_PORT = 80;
    private static final int DEFAULT_THREAD_POOL_SIZE = 1;
    private static final String DEFAULT_DIRECTORY = "/blogApp";

    private final ServletContext servletContext;


    private HttpServer(String contextPath) throws Exception {
        this.servletContext = new ServletContext(WEB_APP_DIR, contextPath, WEB_XML_URL);
    }

    private void startServer(int port, int poolSize) {
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                HttpServerTask serverTask = new HttpServerTask(clientSocket, servletContext);
                pool.submit(serverTask);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args == null) {
            printUsage();
            return;
        }

        Options options = createOptions();
        CommandLine cmd = parseOptions(args, options);

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("http-clientSocket", options);
            return;
        }

        String[] arguments = cmd.getArgs();

        if (arguments.length > 1) {
            printUsage();
            return;
        }

        String contextPath = arguments.length == 1 ? arguments[0] : DEFAULT_DIRECTORY;
        String optionStr = cmd.getOptionValue("p");
        int port = optionStr != null ? Integer.parseInt(optionStr) : DEFAULT_PORT;
        printStart(port, contextPath);

        optionStr = cmd.getOptionValue("t");
        int poolSize = optionStr != null ? Integer.parseInt(optionStr) : DEFAULT_THREAD_POOL_SIZE;

        try {
            HttpServer server = new HttpServer(contextPath);
            server.startServer(port, poolSize);
        } catch (Exception e) {
            LOGGER.error("Server can't start: error parsing webapp web.xml");
        }
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption("p", "port", true, "порт");
        options.addOption("t", "threads", true, "брой нишки");
        options.addOption("h", "help", false, "показва описание на опциите");
        return options;
    }

    private static CommandLine parseOptions(String[] args, Options options) {
        try {
            CommandLineParser parser = new DefaultParser();
            return parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("http-clientSocket: unknown option --");
        }
        return null;
    }

    private static void printUsage() {
        System.out.println("Usage: http-server [path] [options]");
    }

    private static void printStart(int port, String contextPath) {
        System.out.println("Starting up http-server, serving " + contextPath);
        System.out.println("Available on:");
        System.out.println("  http://192.168.8.151:" + port + contextPath);
        System.out.println("  http://127.0.0.1:" + port + contextPath);
        System.out.println("  http://localhost:" + port + contextPath);
        System.out.println("Hit CTRL-C to stop the server");
    }
}