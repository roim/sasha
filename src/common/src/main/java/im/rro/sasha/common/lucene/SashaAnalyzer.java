package im.rro.sasha.common.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;

import java.io.Reader;

public final class SashaAnalyzer extends Analyzer {

    private final Version matchVersion;

    public SashaAnalyzer(Version matchVersion) {
        this.matchVersion = matchVersion;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName,
                                                     final Reader reader) {

        return new TokenStreamComponents(new SashaTokenizer(matchVersion, reader));
    }
}