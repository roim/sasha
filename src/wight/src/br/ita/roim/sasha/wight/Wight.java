package br.ita.roim.sasha.wight;

import br.ita.roim.sasha.utils.FileScanner;
import br.ita.roim.sasha.utils.Parallel;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Wight {

    private final static int SCANNING_THREADS = 8;
    public final static Logger L = Logger.getLogger(Wight.class.getName());

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--help")) {
            System.out.println("wight: a crawler for sasha\n" +
                "--------------------------\n" +
                "wight will index all folders mounted on the /scan folder on the CWD.");
            System.exit(0);
        }

        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            FileHandler logFile = new FileHandler("wight_" + timestamp + ".log");
            logFile.setFormatter(new SimpleFormatter());
            L.addHandler(logFile);
        } catch (IOException e) {
            System.err.println("(101) Could configure the logger!");
            System.exit(101);
        }

        Path scanPath = Paths.get("scan");

        L.log(Level.INFO, "Scanning on: " + scanPath.toAbsolutePath());

        DirectoryStream<Path> shares = null;
        try {
            shares = new FileScanner(scanPath).getSubFolders();
        } catch (IOException ioe) {
            L.log(Level.SEVERE, "Could not find the shares to scan: " + ioe.getStackTrace());
            System.err.println("(102) Could not find the shares to scan. See the log for more information.");
            System.err.println(ioe.getStackTrace());
            System.exit(102);
        }

        Parallel.For(SCANNING_THREADS, shares, (share) -> {
            L.log(Level.INFO, "Starting scan on share: " + share);

            FileScanner shareScanner = new FileScanner(share);
            try {
                shareScanner.fileWalker(p -> System.out.println(p.getKey()));
            } catch (IOException e) {
                L.log(Level.WARNING, "Exception on fileWalker: " + e.getStackTrace());
            }

            L.log(Level.INFO, "Completed scan on share: " + share);
        });

    }

}
