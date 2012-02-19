package net.sf.beezle.mork.pda;

import net.sf.beezle.mork.grammar.Grammar;

public interface PDABuilder {
    Grammar getGrammar();
    int addIfNew(State state);
    int size();
}
