package salsa_lite.compiler.definitions;

import salsa_lite.compiler.Token;

public class CErrorInformation {
    public int beginLine = -1;
    public int beginColumn = -1;
    public int endLine = -1;
    public int endColumn = -1;

    
    public void initLineAndColumnBegin(Token begin) {
        this.beginLine = begin.beginLine;
        this.beginColumn = begin.beginColumn;
    }

    public void initLineAndColumnEnd(Token end) {
        this.endLine = end.endLine;
        this.endColumn = end.endColumn;
    }

    public void initLineAndColumnBegin(CErrorInformation begin) {
        this.beginLine = begin.beginLine;
        this.beginColumn = begin.beginColumn;
    }

    public void initLineAndColumnEnd(CErrorInformation end) {
        this.endLine = end.endLine;
        this.endColumn = end.endColumn;
    }


    public void initLineAndColumn(CErrorInformation ei) {
        this.beginLine = ei.beginLine;
        this.beginColumn = ei.beginColumn;

        this.endLine = ei.endLine;
        this.endColumn = ei.endColumn;
    }

    public void initLineAndColumn(Token t) {
        beginLine = t.beginLine;
        beginColumn= t.beginColumn;

        endLine = t.endLine;
        endColumn = t.endColumn;
    }

    public void initLineAndColumn(CErrorInformation begin, CErrorInformation end) {
        this.beginLine = begin.beginLine;
        this.beginColumn = begin.beginColumn;

        this.endLine = end.endLine;
        this.endColumn = end.endColumn;
    }

    public void initLineAndColumn(Token begin, Token end) {
        this.beginLine = begin.beginLine;
        this.beginColumn = begin.beginColumn;

        this.endLine = end.endLine;
        this.endColumn = end.endColumn;
    }

    public void initLineAndColumn(Token begin, CErrorInformation end) {
        this.beginLine = begin.beginLine;
        this.beginColumn = begin.beginColumn;

        this.endLine = end.endLine;
        this.endColumn = end.endColumn;
    }

    public void initLineAndColumn(CErrorInformation begin, Token end) {
        this.beginLine = begin.beginLine;
        this.beginColumn = begin.beginColumn;

        this.endLine = end.endLine;
        this.endColumn = end.endColumn;
    }
}
