package im.rro.sasha.luna;

import com.sun.net.httpserver.HttpServer;
import im.rro.sasha.common.lucene.SashaAnalyzer;
import im.rro.sasha.luna.handlers.SearchRequestHandler;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//TODO, roim, it's all procedural and static for now. Will be refactored when I include unit tests.
public class Luna {

    public final static Logger L = Logger.getLogger(Luna.class.getName());

    public static IndexSearcher IS = null;

    public static final Object IndexSearcherLock = new Object();

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

        // Logger
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

        // Index Searcher refresher
        //
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (IndexSearcherLock) {
                    try {
                        Directory indexDir = FSDirectory.open(new java.io.File(indexPath));

                        // Create an empty index if none exists
                        if (!DirectoryReader.indexExists(indexDir)) {
                            Analyzer analyzer = new SashaAnalyzer(Version.LUCENE_46);
                            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46, analyzer);
                            new IndexWriter(indexDir, iwc).close();
                        }

                        if (IS != null) IS.getIndexReader().close();
                        IS = new IndexSearcher(DirectoryReader.open(indexDir));
                    } catch (IOException ioe) {
                        L.log(Level.SEVERE, "Could not open Lucene index at" + indexPath + ": " + ioe);
                        System.err.println("(103) Could not open the Lucene index! See the log for more information.");
                        System.exit(103);
                    }
                }
            }
        }, 0, 1000*60*5);

        // Http Server
        //
        HttpServer tempServer = null;
        try {
            tempServer = HttpServer.create(new InetSocketAddress(8080), 0);
            tempServer.createContext("/search", new SearchRequestHandler());
            tempServer.setExecutor(null); // creates a default executor
            tempServer.start();
        } catch (IOException ioe) {
            L.log(Level.SEVERE, "Could not start the HTTP Server: " + ioe);
            System.err.println("(105) Could not start the HTTP Server! See the log for more information.");
            System.exit(105);
        }
        final HttpServer server = tempServer;

        L.log(Level.INFO, "Search will be performed on index at " + indexPath);

        // Shutdown hook
        //
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.stop(1);

                try {
                    IS.getIndexReader().close();
                } catch (IOException ioe) {
                    L.log(Level.SEVERE, "Could not close the Index Reader: " + ioe);
                    return;
                }

                L.log(Level.INFO, "Server closed successfully!");
            }
        });

    }
}
