package de.triona.console;

/**
 *
 * @author Bernhard
 */
public class FindModel {
    private String term;
    private boolean regex;
    private boolean matchCase;

    public FindModel() {
    }

    public FindModel(String term, boolean regex, boolean matchCase) {
        this.term = term;
        this.regex = regex;
        this.matchCase = matchCase;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public boolean isRegex() {
        return regex;
    }

    public void setRegex(boolean regex) {
        this.regex = regex;
    }

    public boolean isMatchCase() {
        return matchCase;
    }

    public void setMatchCase(boolean matchCase) {
        this.matchCase = matchCase;
    }
}