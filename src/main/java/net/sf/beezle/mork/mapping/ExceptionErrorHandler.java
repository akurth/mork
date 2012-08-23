package net.sf.beezle.mork.mapping;

import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.scanner.Position;
import net.sf.beezle.sushi.util.IntBitSet;

import java.io.IOException;

public class ExceptionErrorHandler implements ErrorHandler {
    public ExceptionErrorHandler() {
    }

    public void lexicalError(Position pos) throws IOException {
        report(pos.toString(), "illegal token");
    }

    public void syntaxError(Position pos, IntBitSet shiftable) throws IOException {
        report(pos.toString(), "syntax error");
    }

    public void semanticError(Position pos, Exception e) throws IOException {
        report(pos.toString(), e.getMessage());
    }

    public void close() throws IOException {
        // no deferred exceptions
    }

    //--

    protected void report(String pos, String message) throws IOException {
        throw new IOException(pos + ": " + message);
    }
}
