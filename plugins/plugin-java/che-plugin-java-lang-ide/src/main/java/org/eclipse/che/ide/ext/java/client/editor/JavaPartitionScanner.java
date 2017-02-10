/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.client.editor;


import java.util.List;

import javax.inject.Inject;

import org.eclipse.che.ide.api.editor.text.rules.Token;
import org.eclipse.che.ide.api.editor.text.rules.TokenImpl;
import org.eclipse.che.ide.ext.java.jdt.JavaPartitions;
import org.eclipse.che.ide.api.editor.partition.PartitionScanner;
import org.eclipse.che.ide.api.editor.partition.StringCharacterScanner;
import org.eclipse.che.ide.ext.java.jdt.text.rules.CharacterScanner;

/**
 * This scanner recognizes the JavaDoc comments, Java multi line comments, Java single line comments,
 * Java strings and Java characters.
 */
public class JavaPartitionScanner implements PartitionScanner, JavaPartitions {

    // states
    private static final int JAVA                = 0;
    private static final int SINGLE_LINE_COMMENT = 1;
    private static final int MULTI_LINE_COMMENT  = 2;
    private static final int JAVADOC             = 3;
    private static final int CHARACTER           = 4;
    private static final int STRING              = 5;

    // beginning of prefixes and postfixes
    private static final int NONE            = 0;
    private static final int BACKSLASH       = 1; // postfix for STRING and CHARACTER
    private static final int SLASH           = 2; // prefix for SINGLE_LINE or MULTI_LINE or JAVADOC
    private static final int SLASH_STAR      = 3; // prefix for MULTI_LINE_COMMENT or JAVADOC
    private static final int SLASH_STAR_STAR = 4; // prefix for MULTI_LINE_COMMENT or JAVADOC
    private static final int STAR            = 5; // postfix for MULTI_LINE_COMMENT or JAVADOC
    private static final int CARRIAGE_RETURN = 6; // postfix for STRING, CHARACTER and SINGLE_LINE_COMMENT

    /** The content. */
    private final StringCharacterScanner fScanner = new StringCharacterScanner();

    /** The offset of the last returned token. */
    private int fTokenOffset;
    /** The length of the last returned token. */
    private int fTokenLength;

    /** The state of the scanner. */
    private int fState;
    /** The last significant characters read. */
    private int fLast;
    /** The amount of characters already read on first call to nextToken(). */
    private int fPrefixLength;

    // emulate JavaPartitionScanner
    private boolean fEmulate = false;
    private int fJavaOffset;
    private int fJavaLength;

    private final Token[] fTokens = new Token[]{
            new TokenImpl(null),
            new TokenImpl(JAVA_SINGLE_LINE_COMMENT),
            new TokenImpl(JAVA_MULTI_LINE_COMMENT),
            new TokenImpl(JAVA_DOC),
            new TokenImpl(JAVA_CHARACTER),
            new TokenImpl(JAVA_STRING)
    };

    public JavaPartitionScanner(boolean emulate) {
        fEmulate = emulate;
    }

    @Inject
    public JavaPartitionScanner() {
        this(false);
    }

    /*
     * @see org.eclipse.jface.text.rules.ITokenScanner#nextToken()
     */
    public Token nextToken() {

        // emulate JavaPartitionScanner
        if (fEmulate) {
            if (fJavaOffset != -1 && fTokenOffset + fTokenLength != fJavaOffset + fJavaLength) {
                fTokenOffset += fTokenLength;
                return fTokens[JAVA];
            } else {
                fJavaOffset = -1;
                fJavaLength = 0;
            }
        }

        fTokenOffset += fTokenLength;
        fTokenLength = fPrefixLength;

        while (true) {
            final int ch = fScanner.read();

            // characters
            switch (ch) {
                case CharacterScanner.EOF:
                    if (fTokenLength > 0) {
                        fLast = NONE; // ignore last
                        return preFix(fState, JAVA, NONE, 0);

                    } else {
                        fLast = NONE;
                        fPrefixLength = 0;
                        return TokenImpl.EOF;
                    }

                case '\r':
                    // emulate JavaPartitionScanner
                    if (!fEmulate && fLast != CARRIAGE_RETURN) {
                        fLast = CARRIAGE_RETURN;
                        fTokenLength++;
                        continue;

                    } else {

                        switch (fState) {
                            case SINGLE_LINE_COMMENT:
                            case CHARACTER:
                            case STRING:
                                if (fTokenLength > 0) {
                                    final Token token = fTokens[fState];

                                    // emulate JavaPartitionScanner
                                    if (fEmulate) {
                                        fTokenLength++;
                                        fLast = NONE;
                                        fPrefixLength = 0;
                                    } else {
                                        fLast = CARRIAGE_RETURN;
                                        fPrefixLength = 1;
                                    }

                                    fState = JAVA;
                                    return token;

                                } else {
                                    consume();
                                    continue;
                                }

                            default:
                                consume();
                                continue;
                        }
                    }

                case '\n':
                    switch (fState) {
                        case SINGLE_LINE_COMMENT:
                        case CHARACTER:
                        case STRING:
                            // assert(fTokenLength > 0);
                            return postFix(fState);

                        default:
                            consume();
                            continue;
                    }

                default:
                    if (!fEmulate && fLast == CARRIAGE_RETURN) {
                        switch (fState) {
                            case SINGLE_LINE_COMMENT:
                            case CHARACTER:
                            case STRING:

                                int last;
                                int newState;
                                switch (ch) {
                                    case '/':
                                        last = SLASH;
                                        newState = JAVA;
                                        break;

                                    case '*':
                                        last = STAR;
                                        newState = JAVA;
                                        break;

                                    case '\'':
                                        last = NONE;
                                        newState = CHARACTER;
                                        break;

                                    case '"':
                                        last = NONE;
                                        newState = STRING;
                                        break;

                                    case '\r':
                                        last = CARRIAGE_RETURN;
                                        newState = JAVA;
                                        break;

                                    case '\\':
                                        last = BACKSLASH;
                                        newState = JAVA;
                                        break;

                                    default:
                                        last = NONE;
                                        newState = JAVA;
                                        break;
                                }

                                fLast = NONE; // ignore fLast
                                return preFix(fState, newState, last, 1);

                            default:
                                break;
                        }
                    }
            }

            // states
            switch (fState) {
                case JAVA:
                    switch (ch) {
                        case '/':
                            if (fLast == SLASH) {
                                if (fTokenLength - getLastLength(fLast) > 0) {
                                    return preFix(JAVA, SINGLE_LINE_COMMENT, NONE, 2);
                                } else {
                                    preFix(JAVA, SINGLE_LINE_COMMENT, NONE, 2);
                                    fTokenOffset += fTokenLength;
                                    fTokenLength = fPrefixLength;
                                    break;
                                }

                            } else {
                                fTokenLength++;
                                fLast = SLASH;
                                break;
                            }

                        case '*':
                            if (fLast == SLASH) {
                                if (fTokenLength - getLastLength(fLast) > 0) {
                                    return preFix(JAVA, MULTI_LINE_COMMENT, SLASH_STAR, 2);
                                } else {
                                    preFix(JAVA, MULTI_LINE_COMMENT, SLASH_STAR, 2);
                                    fTokenOffset += fTokenLength;
                                    fTokenLength = fPrefixLength;
                                    break;
                                }

                            } else {
                                consume();
                                break;
                            }

                        case '\'':
                            fLast = NONE; // ignore fLast
                            if (fTokenLength > 0) {
                                return preFix(JAVA, CHARACTER, NONE, 1);
                            } else {
                                preFix(JAVA, CHARACTER, NONE, 1);
                                fTokenOffset += fTokenLength;
                                fTokenLength = fPrefixLength;
                                break;
                            }

                        case '"':
                            fLast = NONE; // ignore fLast
                            if (fTokenLength > 0) {
                                return preFix(JAVA, STRING, NONE, 1);
                            } else {
                                preFix(JAVA, STRING, NONE, 1);
                                fTokenOffset += fTokenLength;
                                fTokenLength = fPrefixLength;
                                break;
                            }

                        default:
                            consume();
                            break;
                    }
                    break;

                case SINGLE_LINE_COMMENT:
                    consume();
                    break;

                case JAVADOC:
                    switch (ch) {
                        case '/':
                            switch (fLast) {
                                case SLASH_STAR_STAR:
                                    return postFix(MULTI_LINE_COMMENT);

                                case STAR:
                                    return postFix(JAVADOC);

                                default:
                                    consume();
                                    break;
                            }
                            break;

                        case '*':
                            fTokenLength++;
                            fLast = STAR;
                            break;

                        default:
                            consume();
                            break;
                    }
                    break;

                case MULTI_LINE_COMMENT:
                    switch (ch) {
                        case '*':
                            if (fLast == SLASH_STAR) {
                                fLast = SLASH_STAR_STAR;
                                fTokenLength++;
                                fState = JAVADOC;
                            } else {
                                fTokenLength++;
                                fLast = STAR;
                            }
                            break;

                        case '/':
                            if (fLast == STAR) {
                                return postFix(MULTI_LINE_COMMENT);
                            } else {
                                consume();
                                break;
                            }

                        default:
                            consume();
                            break;
                    }
                    break;

                case STRING:
                    switch (ch) {
                        case '\\':
                            fLast = (fLast == BACKSLASH) ? NONE : BACKSLASH;
                            fTokenLength++;
                            break;

                        case '\"':
                            if (fLast != BACKSLASH) {
                                return postFix(STRING);

                            } else {
                                consume();
                                break;
                            }

                        default:
                            consume();
                            break;
                    }
                    break;

                case CHARACTER:
                    switch (ch) {
                        case '\\':
                            fLast = (fLast == BACKSLASH) ? NONE : BACKSLASH;
                            fTokenLength++;
                            break;

                        case '\'':
                            if (fLast != BACKSLASH) {
                                return postFix(CHARACTER);

                            } else {
                                consume();
                                break;
                            }

                        default:
                            consume();
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static final int getLastLength(int last) {
        switch (last) {
            default:
                return -1;

            case NONE:
                return 0;

            case CARRIAGE_RETURN:
            case BACKSLASH:
            case SLASH:
            case STAR:
                return 1;

            case SLASH_STAR:
                return 2;

            case SLASH_STAR_STAR:
                return 3;
        }
    }

    private final void consume() {
        fTokenLength++;
        fLast = NONE;
    }

    private final Token postFix(int state) {
        fTokenLength++;
        fLast = NONE;
        fState = JAVA;
        fPrefixLength = 0;
        return fTokens[state];
    }

    private final Token preFix(int state, int newState, int last, int prefixLength) {
        // emulate JavaPartitionScanner
        if (fEmulate && state == JAVA && (fTokenLength - getLastLength(fLast) > 0)) {
            fTokenLength -= getLastLength(fLast);
            fJavaOffset = fTokenOffset;
            fJavaLength = fTokenLength;
            fTokenLength = 1;
            fState = newState;
            fPrefixLength = prefixLength;
            fLast = last;
            return fTokens[state];

        } else {
            fTokenLength -= getLastLength(fLast);
            fLast = last;
            fPrefixLength = prefixLength;
            final Token token = fTokens[state];
            fState = newState;
            return token;
        }
    }

    public void setScannedString(String content) {

        fScanner.setScannedString(content);

        fTokenOffset = 0;
        fTokenLength = 0;
        fPrefixLength = 0;
        fLast = NONE;
        fState = JAVA;

        // emulate JavaPartitionScanner
        if (fEmulate) {
            fJavaOffset = -1;
            fJavaLength = 0;
        }
    }

    @Override
    public void setLegalLineDelimiters(final List<String> delimiters) {
        this.fScanner.setLegalLineDelimiters(delimiters);
    }

    /*
     * @see ITokenScanner#getTokenLength()
     */
    public int getTokenLength() {
        return fTokenLength;
    }

    /*
     * @see ITokenScanner#getTokenOffset()
     */
    public int getTokenOffset() {
        return fTokenOffset;
    }

}
