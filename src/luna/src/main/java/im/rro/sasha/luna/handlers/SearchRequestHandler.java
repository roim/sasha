package im.rro.sasha.luna.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import im.rro.sasha.common.FileInfo;
import im.rro.sasha.common.lucene.SashaAnalyzer;
import im.rro.sasha.luna.Luna;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
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

        // The user input query is not tokenized nor normalized.
        //   i.e. "foo bar" is a single term, also "foo" and "FOO" are not the same.
        // However, we tokenized the data before adding it to the index, so now we should tokenize the input
        //   in the same way before sending the query to lucene.
        LinkedList<String> terms;
        try {
            terms = getAnalyzedTerms(parameters.get("q"));
        } catch (IOException ioe) {
            Luna.L.log(Level.SEVERE, "Error analyzing input terms:" +
                            "\nURI: " + t.getRequestURI() +
                            "\nTerms: " + parameters.get("q") +
                            "\nException: " + ioe);
            sendStringResponse(t, 500, "500: Internal server error.");
            return;
        }

        // Each token will generate a fuzzy query
        // Fuzzy queries match similar words. e.g. 'grey' to 'gray'
        BooleanQuery nameQuery = new BooleanQuery();

        for ( String term : terms ) {
            nameQuery.add(new FuzzyQuery(new Term(FileInfo.ROW_FILE_NAME, term)), BooleanClause.Occur.SHOULD);
        }

        // Join the query for filename with the query for file extension, if it exists
        // Note that this will result in a new boolean query that include the previous nameQuery
        //   this is done to ensure that nameQuery must happen.
        //   (otherwise if the filename didn't match but the extension did, the result would still be valid)
        String extension = parameters.containsKey("ext") ? parameters.get("ext") : "";
        extension = extension.toLowerCase();

        Query query;
        if (extension.equals("")) {
            query = nameQuery;
        } else {
            BooleanQuery bQuery = new BooleanQuery();
            bQuery.add(nameQuery, BooleanClause.Occur.MUST);
            bQuery.add(new TermQuery(new Term(FileInfo.ROW_EXTENSION, extension)), BooleanClause.Occur.MUST);

            query = bQuery;
        }

        // Search the index
        //
        TopDocs searchResults;

        try {
            searchResults = Luna.IS.search(query, 1000, Sort.RELEVANCE);
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
            Path filePath;

            try {
                Document hit = Luna.IS.doc(sd.doc);
                filePath = Paths.get(hit.getField(FileInfo.ROW_COMPLETE_PATH).stringValue());
                long size = hit.getField(FileInfo.ROW_SIZE).numericValue().longValue();

                results.add(new FileInfo(filePath, size));
            } catch (IOException ioe) {
                Luna.L.log(Level.SEVERE, "Error querying the index." +
                        "\nURI: " + t.getRequestURI() +
                        "\nQuery: " + parameters.get("q") +
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
            t.sendResponseHeaders(statusCode, response.getBytes().length);
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

    private LinkedList<String> getAnalyzedTerms(String rawTerms) throws IOException {
        LinkedList<String> results = new LinkedList<>();
        Analyzer analyzer = new SashaAnalyzer(Version.LUCENE_46);
        TokenStream stream = analyzer.tokenStream(null, new StringReader(rawTerms));
        CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);

        stream.reset();
        while (stream.incrementToken()) {
            results.add(cattr.toString());
        }
        stream.end();
        stream.close();

        return results;
    }
}
