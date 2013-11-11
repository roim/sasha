package br.ita.roim.sasha.common.lucene;

import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.Version;

import java.io.Reader;

public class SashaTokenizer extends CharTokenizer {

    public SashaTokenizer(Version matchVersion, Reader in) {
        super(matchVersion, in);
    }

    public SashaTokenizer(Version matchVersion, AttributeFactory factory, Reader in) {
        super(matchVersion, factory, in);
    }

    @Override
    protected boolean isTokenChar(int c) {
        return Character.isLetter(c);
    }

    @Override
    protected int normalize(int c) {
        return Character.toLowerCase(c);
    }
}