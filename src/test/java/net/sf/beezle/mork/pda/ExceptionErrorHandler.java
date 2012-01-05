package net.sf.beezle.mork.pda;

import net.sf.beezle.mork.mapping.ErrorHandler;
import net.sf.beezle.mork.scanner.Position;
import net.sf.beezle.sushi.util.IntBitSet;

import java.io.IOException;

public class ExceptionErrorHandler implements ErrorHandler {
    @Override
    public void lexicalError(Position pos) {
        throw new RuntimeException();
    }

    @Override
    public void syntaxError(Position pos, IntBitSet shiftable) {
        throw new RuntimeException();
    }

    @Override
    public void semanticError(Position pos, Exception e) {
        throw new RuntimeException();
    }

    @Override
    public void ioError(String position, String message, IOException e) {
        throw new RuntimeException();
    }
}
