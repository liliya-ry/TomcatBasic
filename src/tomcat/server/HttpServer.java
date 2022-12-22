package tomcat.server;

import org.apache.commons.cli.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import tomcat.servlet_context.ServletContext;
import javax.xml.parsers.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class HttpServer {
    private static final String TOMCAT_ROOT = "D:/IdeaProjects/TomcatBasic";
    private static final String SERVER_XML_PATH = "src/tomcat/server.xml";
    private static final int DEFAULT_PORT = 80;
    private static final int DEFAULT_THREAD_POOL_SIZE = 1;

    private final Map<String, ServletContext> servletContexts;


    private HttpServer() {
        servletContexts = new HashMap<>();
        try {
            parseServletContexts();
        } catch (Exception e) {
            System.out.println("Error parsing Servlet Contexts - server can not start");
        }
    }

    private void startServer(int port, int poolSize) {
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                HttpServerTask serverTask = new HttpServerTask(clientSocket, servletContexts);
                pool.submit(serverTask);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
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

        HttpServer server = new HttpServer();
        String optionStr = cmd.getOptionValue("p");
        int port = optionStr != null ? Integer.parseInt(optionStr) : DEFAULT_PORT;
        printStart(port);

        optionStr = cmd.getOptionValue("t");
        int poolSize = optionStr != null ? Integer.parseInt(optionStr) : DEFAULT_THREAD_POOL_SIZE;

        try {
            server.startServer(port, poolSize);
        } catch (Exception e) {
            System.out.println("Server can't start: error parsing webapp web.xml");
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

    private static void printStart(int port) {
        System.out.println("  http://localhost:" + port);
        System.out.println("Hit CTRL-C to stop the server");
    }

    public void parseServletContexts() throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilder docBuilder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        Document document = docBuilder.parse(SERVER_XML_PATH);
        NodeList contextNodes = document.getElementsByTagName("Context");
        for (int i = 0; i < contextNodes.getLength(); i++) {
            Node contextNode = contextNodes.item(i);
            Element contextEl = (Element) contextNode;
            String path = "/" + contextEl.getAttribute("path");
            String docBase = contextEl.getAttribute("docBase");
            if (!docBase.startsWith(TOMCAT_ROOT)) {
                continue;
            }
            docBase = docBase.substring(TOMCAT_ROOT.length() + 1);
            ServletContext context = new ServletContext(docBase, path);
            servletContexts.put(path, context);
        }

    }
}