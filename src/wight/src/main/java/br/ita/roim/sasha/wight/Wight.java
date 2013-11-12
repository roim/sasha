package br.ita.roim.sasha.wight;

import br.ita.roim.sasha.common.FileInfo;
import br.ita.roim.sasha.common.lucene.SashaAnalyzer;
import br.ita.roim.sasha.wight.utils.FileScanner;
import br.ita.roim.sasha.wight.utils.Parallel;
import javafx.util.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//TODO, roim, it's all procedural and static for now. Will be refactored when I include unit tests.
public class Wight {

    public final static Logger L = Logger.getLogger(Wight.class.getName());

    private final static int SCANNING_THREADS = 8;
    private static IndexWriter IW = null;

    public static void main(String[] args) {
        //
        // Process the input args
        //

        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(
                    "wight: a crawler for sasha\n" +
                            "--------------------------\n" +
                            "Usage:\twight [-scanpath dir] [-indexpath dir] [-clean timeoutHours]\n");
            System.exit(0);
        }

        Path scanPath = Paths.get("scan");
        String indexPath = "index";
        long timeoutHours = 24*7;
        boolean shouldClean = false;

        for (int i = 0; i < args.length; ++i) {
            switch (args[i]) {
                case "-scanpath":
                    scanPath = Paths.get(args[i + 1]);
                    ++i;
                    break;
                case "-indexpath":
                    indexPath = args[i + 1];
                    ++i;
                    break;
                case "-clean":
                    shouldClean = true;
                    try {
                        timeoutHours = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException nfe) {
                        System.err.println("(105) Invalid timeout argument: " + args[i + 1]);
                        System.exit(105);
                    }
                    ++i;
                    break;
                default:
                    System.err.println("(105) Invalid argument: " + args[i]);
                    System.exit(105);
            }
        }

        //
        // Setting things up
        //

        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            FileHandler logFile = new FileHandler("wight_" + timestamp + ".log");
            logFile.setFormatter(new SimpleFormatter());
            L.addHandler(logFile);
        } catch (IOException ioe) {
            System.err.println("(101) Could not configure the logger: " + ioe);
            System.exit(101);
        }

        try {
            Directory indexDir = FSDirectory.open(new java.io.File(indexPath));
            Analyzer analyzer = new SashaAnalyzer(Version.LUCENE_45);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_45, analyzer);
            IW = new IndexWriter(indexDir, iwc);
        } catch (IOException ioe) {
            L.log(Level.SEVERE, "Could not open Lucene index at" + indexPath + ": " + ioe);
            System.err.println("(103) Could not open the Lucene index! See the log for more information.");
            System.exit(103);
        }

        //
        // Scan
        //

        L.log(Level.INFO, "Scanning on: " + scanPath.toAbsolutePath());

        DirectoryStream<Path> shares = null;
        try {
            shares = new FileScanner(scanPath).getSubFolders();
        } catch (IOException ioe) {
            L.log(Level.SEVERE, "Could not find the shares to scan: " + ioe);
            System.err.println("(102) Could not find the shares to scan! See the log for more information.");
            System.exit(102);
        }

        Parallel.blockingFor(SCANNING_THREADS, shares, (share) -> {
            L.log(Level.INFO, "Starting scan on share: " + share);

            final long[] filesFound = {0};
            final FileScanner shareScanner = new FileScanner(share);
            try {
                shareScanner.fileWalker(p -> {
                    processFile(p);
                    filesFound[0]++;
                });
            } catch (IOException ioe) {
                L.log(Level.WARNING, share + " had exception on fileWalker: " + ioe);
            }

            L.log(Level.INFO, "Found " + filesFound[0] + " files on share: " + share);
        });

        //
        // Clean
        //

        if (shouldClean) {
            L.log(Level.INFO, "Cleaning old files from index, timeout is " + timeoutHours + " hours.");

            final long millisecondsInAnHour = 1000*60*60;
            final long timeoutMs = System.currentTimeMillis() - timeoutHours*millisecondsInAnHour;

            try {
                IW.deleteDocuments(NumericRangeQuery.newLongRange(FileInfo.ROW_LAST_SEEN, 0L, timeoutMs, true, true));
            } catch (IOException ioe) {
                L.log(Level.SEVERE, "Could not delete old files from index: " + ioe);
            }
        }

        //
        // Shutting down
        //

        try {
            IW.close();
        } catch (IOException ioe) {
            L.log(Level.SEVERE, "Could not close the Index Writer: " + ioe);
            System.err.println("(104) Could not close the Index Writer! See the log for more information.");
            System.exit(104);
        }

        L.log(Level.INFO, "Done!");
    }

    static boolean processFile(final Pair<Path, BasicFileAttributes> p) {
        final FileInfo fi = new FileInfo(p.getKey(), p.getValue());

        try {
            IW.updateDocument(new Term(FileInfo.ROW_COMPLETE_PATH, fi.getCompletePath()), fi.createDocument());
        } catch (IOException ioe) {
            //TODO, roim, this might fill the log really fast. Maybe only log the first per share?
            L.log(Level.WARNING, "Could not add/update " + fi.getCompletePath() + ": " + ioe);
            return false;
        }

        return true;
    }

}
