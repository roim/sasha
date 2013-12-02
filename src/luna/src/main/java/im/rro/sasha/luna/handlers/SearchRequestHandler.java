package im.rro.sasha.luna.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import im.rro.sasha.common.FileInfo;
import im.rro.sasha.luna.Luna;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

public class SearchRequestHandler implements  HttpHandler{
    private static Gson GSON = new Gson();

    @Override
    public void handle(HttpExchange t) {
        if (!t.getRequestMethod().equals("GET")) {
            Luna.L.log(Level.INFO, "Invalid request method received: " + t.getRequestMethod());
            sendStringResponse(t, 405, "405: Method not allowed. Only GET is accepted at this URL.");
            return;
        }

        // Find out the desired query
        //
        Map<String, String> parameters = queryToMap(t.getRequestURI().getQuery());

        if (!parameters.containsKey("q")) {
            Luna.L.log(Level.INFO, "Invalid request URI received: " + t.getRequestURI());
            sendStringResponse(t, 400, "400: Bad request. No 'q' parameter.");
            return;
        }

        String[] terms = parameters.get("q").split("\\s+");
        SpanQuery[] clauses = new SpanQuery[terms.length];

        {
            int i = 0;
            for ( String term : terms ) {
                // Fuzzy query matches similar words. e.g. 'grey' to 'gray'
                clauses[i++] = new SpanMultiTermQueryWrapper(new FuzzyQuery(new Term(FileInfo.ROW_FILE_NAME, term)));
            }
        }

        // SpanNearQuery finds occurrences of the clauses in close position to each other.
        SpanNearQuery query = new SpanNearQuery(clauses, 1, false);

        // Search the index
        //
        TopDocs searchResults;

        try {
            searchResults = Luna.IS.search(query, 10);
        } catch (IOException ioe) {
            Luna.L.log(Level.SEVERE, "Error querying the index." +
                    "\nURI: " + t.getRequestURI() +
                    "\nQuery: " + parameters.get("q") +
                    "\nException: " + ioe);
            sendStringResponse(t, 500, "500: Internal server error.");
            return;
        }

        // Build the response from the received hits
        //
        LinkedList<FileInfo> results = new LinkedList<>();
        for ( ScoreDoc sd : searchResults.scoreDocs) {
            Path filePath = null;

            try {
                Document hit = Luna.IS.doc(sd.doc);
                filePath = Paths.get(hit.getField(FileInfo.ROW_COMPLETE_PATH).stringValue());
                long size = hit.getField(FileInfo.ROW_SIZE).numericValue().longValue();

                results.add(new FileInfo(filePath, size));
            } catch (IOException ioe) {
                Luna.L.log(Level.SEVERE, "Error querying the index." +
                        "\nURI: " + t.getRequestURI() +
                        "\nQuery: " + parameters.get("q") +
                        "\nResult path: " + filePath +
                        "\nException: " + ioe);
                sendStringResponse(t, 500, "500: Internal server error.");
                return;
            }
        }

        // Send the response
        //
        sendStringResponse(t, 200, GSON.toJson(results));
    }

    private void sendStringResponse(HttpExchange t, int statusCode, String response) {
        try {
            t.sendResponseHeaders(statusCode, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (IOException ioe) {
            //TODO, define what to log
            Luna.L.log(Level.WARNING, "Could not notify " + statusCode + " to client: " + ioe);
        }
    }

    private Map<String, String> queryToMap(String query){
        Map<String, String> result = new HashMap<>();

        for (String param : query.split("&")) {
            String pair[] = param.split("=");

            if (pair.length > 1) {
                result.put(pair[0], pair[1]);
            } else {
                result.put(pair[0], "");
            }
        }

        return result;
    }
}
