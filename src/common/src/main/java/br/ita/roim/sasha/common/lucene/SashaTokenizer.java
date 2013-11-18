package br.ita.roim.sasha.common.lucene;

import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.Version;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class SashaTokenizer extends CharTokenizer {

    private static Map<Character, Character> MAP_NORM;
    static {
        MAP_NORM = new HashMap<>();
        MAP_NORM.put('á', 'a');
        MAP_NORM.put('é', 'e');
        MAP_NORM.put('í', 'i');
        MAP_NORM.put('ό', 'ο');
        MAP_NORM.put('ú', 'u');
        MAP_NORM.put('à', 'a');
        MAP_NORM.put('è', 'e');
        MAP_NORM.put('ì', 'i');
        MAP_NORM.put('ò', 'o');
        MAP_NORM.put('ù', 'u');
        MAP_NORM.put('ã', 'a');
        MAP_NORM.put('ẽ', 'e');
        MAP_NORM.put('ĩ', 'i');
        MAP_NORM.put('õ', 'o');
        MAP_NORM.put('ũ', 'u');
        MAP_NORM.put('ä', 'a');
        MAP_NORM.put('ë', 'e');
        MAP_NORM.put('ï', 'i');
        MAP_NORM.put('ö', 'ο');
        MAP_NORM.put('ü', 'u');
        MAP_NORM.put('â', 'a');
        MAP_NORM.put('ê', 'e');
        MAP_NORM.put('î', 'i');
        MAP_NORM.put('ô', 'ο');
        MAP_NORM.put('û', 'u');
    }

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
        int lwc = Character.toLowerCase(c);

        if (MAP_NORM.containsKey((char) lwc)) {
            return MAP_NORM.get((char) lwc);
        }

        return lwc;
    }
}