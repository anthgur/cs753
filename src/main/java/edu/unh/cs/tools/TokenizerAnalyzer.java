package edu.unh.cs.tools;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class TokenizerAnalyzer {
    private TokenizerAnalyzer() {}

    public static List<String> tokenizeString(Analyzer analyzer, String str) {
        List<String> result = new ArrayList<>();
        try {
            TokenStream stream = analyzer.tokenStream(null, new StringReader(str));
            stream.reset();
            while (stream.incrementToken()) {
                result.add(stream.getAttribute(CharTermAttribute.class).toString());
            }
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
