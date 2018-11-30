package lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Scanner {
    // return tokens as an Iterator
    public static Iterator<Token> scan(String list) throws FileNotFoundException {
        ScanContext context = new ScanContext(list);
        return new TokenIterator(context);
    }

    // return tokens as a Stream 
    public static Stream<Token> stream(String list) throws FileNotFoundException {
        Iterator<Token> tokens = scan(list);
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(tokens, Spliterator.ORDERED), false);
    }
}