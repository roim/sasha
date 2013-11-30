package im.rro.sasha.luna;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import im.rro.sasha.common.lucene.SashaAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//TODO, roim, it's all procedural and static for now. Will be refactored when I include unit tests.
public class Luna {

    public final static Logger L = Logger.getLogger(Luna.class.getName());

    private static IndexReader IR = null;

    public static void main(String[] args) {
        //
        // Process the input args
        //

        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(
                    "luna: sasha http handler\n" +
                            "--------------------------\n" +
                            "Usage:\tluna [-indexpath dir]\n");
            System.exit(0);
        }

        String tempIndexPath = "index";

        for (int i = 0; i < args.length; ++i) {
            switch (args[i]) {
                case "-indexpath":
                    tempIndexPath = args[i + 1];
                    ++i;
                    break;
                default:
                    System.err.println("(105) Invalid argument: " + args[i]);
                    System.exit(105);
            }
        }

        final String indexPath = tempIndexPath;

        //
        // Setting things up
        //

        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            FileHandler logFile = new FileHandler("luna_" + timestamp + ".log");
            logFile.setFormatter(new SimpleFormatter());
            L.addHandler(logFile);
        } catch (IOException ioe) {
            System.err.println("(101) Could not configure the logger: " + ioe);
            System.exit(101);
        }

        Analyzer analyzer = new SashaAnalyzer(Version.LUCENE_45);
        try {
            Directory indexDir = FSDirectory.open(new java.io.File(indexPath));
            IR = DirectoryReader.open(indexDir);
        } catch (IOException ioe) {
            L.log(Level.SEVERE, "Could not open Lucene index at" + indexPath + ": " + ioe);
            System.err.println("(103) Could not open the Lucene index! See the log for more information.");
            System.exit(103);
        }

        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(80), 0);
            server.createContext("/search", new SearchRequestHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException ioe) {
            L.log(Level.SEVERE, "Could not start the HTTP Server: " + ioe);
            System.err.println("(105) Could not start the HTTP Server! See the log for more information.");
            System.exit(105);
        }


        L.log(Level.INFO, "Search will be performed on index at " + indexPath);

        //
        // Shutting down
        //

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    IR.close();
                } catch (IOException ioe) {
                    L.log(Level.SEVERE, "Could not close the Index Reader: " + ioe);
                    System.err.println("(104) Could not close the Index Reader! See the log for more information.");
                    System.exit(104);
                }

                L.log(Level.INFO, "Server closed succesfully!");
            }
        });

    }

    private static class SearchRequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) {
            String response = "This is the response";

            try {
                t.sendResponseHeaders(200, response.length());
            } catch (IOException ioe) {
                L.log(Level.WARNING, "Error sending the response headers.");
                return;
            }

            try {
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (IOException ioe) {
                L.log(Level.WARNING, "Error sending the response.");
            }
        }
    }
}
