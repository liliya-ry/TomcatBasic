import org.apache.commons.cli.*;
import server.HttpServerTask;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private static final int DEFAULT_PORT = 80;
    private static final int DEFAULT_THREAD_POOL_SIZE = 1;
    private static final String DEFAULT_DIRECTORY = System.getProperty("user.dir");

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

        String webRoot = arguments.length == 1 ? arguments[0] : DEFAULT_DIRECTORY;
        String optionStr = cmd.getOptionValue("p");
        int port = optionStr != null ? Integer.parseInt(optionStr) : DEFAULT_PORT;
        printStart(port, webRoot);

        optionStr = cmd.getOptionValue("t");
        int poolSize = optionStr != null ? Integer.parseInt(optionStr) : DEFAULT_THREAD_POOL_SIZE;


        startServer(port, poolSize, webRoot);
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

    private static void printStart(int port, String webRoot) {
        System.out.println("Starting up http-server, serving " + webRoot);
        System.out.println("Available on:");
        System.out.println("  http://192.168.8.151:" + port);
        System.out.println("  http://127.0.0.1:" + port);
        System.out.println("Hit CTRL-C to stop the server");
    }

    private static void startServer(int port, int poolSize, String webRoot) {
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                HttpServerTask serverTask = new HttpServerTask(clientSocket, webRoot, port);
                pool.submit(serverTask);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}