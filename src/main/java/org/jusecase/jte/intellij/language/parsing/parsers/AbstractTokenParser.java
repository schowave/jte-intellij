package org.jusecase.jte.intellij.language.parsing.parsers;

import com.intellij.ide.highlighter.custom.tokens.TokenParser;
import com.intellij.psi.CustomHighlighterTokenType;
import com.intellij.psi.tree.IElementType;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public abstract class AbstractTokenParser extends TokenParser {
    protected final boolean isBeginOf(int position, String token) {
        int endPosition = position + token.length();
        if (endPosition > myEndOffset) {
            return false;
        }

        for (int i = position; i < endPosition; ++i) {
            if (myBuffer.charAt(i) != token.charAt(i - position)) {
                return false;
            }
        }

        return true;
    }

    protected final boolean hasToken(int position, String keyword, IElementType type) {
        if (isBeginOf(position, keyword)) {
            myTokenInfo.updateData(position, position + keyword.length(), type);
            return true;
        }
        return false;
    }

    protected final boolean skipWhitespaces(int position) {
        if (!isWhitespace(position)) return false;
        int start = position;
        //noinspection StatementWithEmptyBody
        for (position++; position < myEndOffset && isWhitespace(position); position++) ;
        myTokenInfo.updateData(start, position, JteTokenTypes.WHITESPACE);
        return true;
    }

    protected boolean isWhitespace(int position) {
        return Character.isWhitespace(myBuffer.charAt(position));
    }
}
