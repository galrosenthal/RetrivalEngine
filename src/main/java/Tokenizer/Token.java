package Tokenizer;

import java.util.Objects;

public class Token {
    private String tokenText;

    public Token(String tokenText) {
        setTokenText(tokenText);
    }

    public String getTokenText() {
        return tokenText;
    }

    private void setTokenText(String tokenText) {
        this.tokenText = tokenText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token)) return false;
        Token token = (Token) o;
        return Objects.equals(getTokenText(), token.getTokenText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTokenText());
    }


    @Override
    public String toString() {
        return "Token{" +
                "tokenText='" + tokenText + '\'' +
                '}';
    }


}
