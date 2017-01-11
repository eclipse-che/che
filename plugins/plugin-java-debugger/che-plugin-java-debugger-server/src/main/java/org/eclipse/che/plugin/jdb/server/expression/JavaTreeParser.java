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
// $ANTLR 3.3 Nov 30, 2010 12:50:56 org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g 2013-02-07 15:54:13

package org.eclipse.che.plugin.jdb.server.expression;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.TreeNodeStream;
import org.antlr.runtime.tree.TreeParser;
import org.antlr.runtime.tree.TreeRuleReturnScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * For more information see the head comment within the 'java.g' grammar file
 * that defines the input for this tree grammar.
 * <p/>
 * BSD licence
 * <p/>
 * Copyright (c) 2007-2008 by HABELITZ Software Developments
 * <p/>
 * All rights reserved.
 * <p/>
 * http://www.habelitz.com
 * <p/>
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY HABELITZ SOFTWARE DEVELOPMENTS ('HSD') ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL 'HSD' BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class JavaTreeParser extends TreeParser {
    public static final String[] tokenNames                    = new String[]{
            "<invalid>", "<EOR>", "<DOWN>", "<UP>", "AND", "AND_ASSIGN", "ASSIGN", "AT", "BIT_SHIFT_RIGHT", "BIT_SHIFT_RIGHT_ASSIGN",
            "COLON", "COMMA", "DEC", "DIV", "DIV_ASSIGN", "DOT", "DOTSTAR", "ELLIPSIS", "EQUAL", "GREATER_OR_EQUAL", "GREATER_THAN", "INC",
            "LBRACK", "LCURLY", "LESS_OR_EQUAL", "LESS_THAN", "LOGICAL_AND", "LOGICAL_NOT", "LOGICAL_OR", "LPAREN", "MINUS", "MINUS_ASSIGN",
            "MOD", "MOD_ASSIGN", "NOT", "NOT_EQUAL", "OR", "OR_ASSIGN", "PLUS", "PLUS_ASSIGN", "QUESTION", "RBRACK", "RCURLY", "RPAREN",
            "SEMI", "SHIFT_LEFT", "SHIFT_LEFT_ASSIGN", "SHIFT_RIGHT", "SHIFT_RIGHT_ASSIGN", "STAR", "STAR_ASSIGN", "XOR", "XOR_ASSIGN",
            "ABSTRACT", "ASSERT", "BOOLEAN", "BREAK", "BYTE", "CASE", "CATCH", "CHAR", "CLASS", "CONTINUE", "DEFAULT", "DO", "DOUBLE",
            "ELSE", "ENUM", "EXTENDS", "FALSE", "FINAL", "FINALLY", "FLOAT", "FOR", "IF", "IMPLEMENTS", "INSTANCEOF", "INTERFACE", "IMPORT",
            "INT", "LONG", "NATIVE", "NEW", "NULL", "PACKAGE", "PRIVATE", "PROTECTED", "PUBLIC", "RETURN", "SHORT", "STATIC", "STRICTFP",
            "SUPER", "SWITCH", "SYNCHRONIZED", "THIS", "THROW", "THROWS", "TRANSIENT", "TRUE", "TRY", "VOID", "VOLATILE", "WHILE",
            "ANNOTATION_INIT_ARRAY_ELEMENT", "ANNOTATION_INIT_BLOCK", "ANNOTATION_INIT_DEFAULT_KEY", "ANNOTATION_INIT_KEY_LIST",
            "ANNOTATION_LIST", "ANNOTATION_METHOD_DECL", "ANNOTATION_SCOPE", "ANNOTATION_TOP_LEVEL_SCOPE", "ARGUMENT_LIST",
            "ARRAY_DECLARATOR", "ARRAY_DECLARATOR_LIST", "ARRAY_ELEMENT_ACCESS", "ARRAY_INITIALIZER", "BLOCK_SCOPE", "CAST_EXPR",
            "CATCH_CLAUSE_LIST", "CLASS_CONSTRUCTOR_CALL", "CLASS_INSTANCE_INITIALIZER", "CLASS_STATIC_INITIALIZER",
            "CLASS_TOP_LEVEL_SCOPE", "CONSTRUCTOR_DECL", "ENUM_TOP_LEVEL_SCOPE", "EXPR", "EXTENDS_BOUND_LIST", "EXTENDS_CLAUSE",
            "FOR_CONDITION", "FOR_EACH", "FOR_INIT", "FOR_UPDATE", "FORMAL_PARAM_LIST", "FORMAL_PARAM_STD_DECL", "FORMAL_PARAM_VARARG_DECL",
            "FUNCTION_METHOD_DECL", "GENERIC_TYPE_ARG_LIST", "GENERIC_TYPE_PARAM_LIST", "INTERFACE_TOP_LEVEL_SCOPE", "IMPLEMENTS_CLAUSE",
            "LABELED_STATEMENT", "LOCAL_MODIFIER_LIST", "JAVA_SOURCE", "METHOD_CALL", "MODIFIER_LIST", "PARENTESIZED_EXPR", "POST_DEC",
            "POST_INC", "PRE_DEC", "PRE_INC", "QUALIFIED_TYPE_IDENT", "STATIC_ARRAY_CREATOR", "SUPER_CONSTRUCTOR_CALL",
            "SWITCH_BLOCK_LABEL_LIST", "THIS_CONSTRUCTOR_CALL", "THROWS_CLAUSE", "TYPE", "UNARY_MINUS", "UNARY_PLUS", "VAR_DECLARATION",
            "VAR_DECLARATOR", "VAR_DECLARATOR_LIST", "VOID_METHOD_DECL", "IDENT", "HEX_LITERAL", "OCTAL_LITERAL", "DECIMAL_LITERAL",
            "FLOATING_POINT_LITERAL", "CHARACTER_LITERAL", "STRING_LITERAL", "HEX_DIGIT", "INTEGER_TYPE_SUFFIX", "EXPONENT",
            "FLOAT_TYPE_SUFFIX", "ESCAPE_SEQUENCE", "UNICODE_ESCAPE", "OCTAL_ESCAPE", "JAVA_ID_START", "JAVA_ID_PART", "WS", "COMMENT",
            "LINE_COMMENT"
    };
    public static final int      EOF                           = -1;
    public static final int      AND                           = 4;
    public static final int      AND_ASSIGN                    = 5;
    public static final int      ASSIGN                        = 6;
    public static final int      AT                            = 7;
    public static final int      BIT_SHIFT_RIGHT               = 8;
    public static final int      BIT_SHIFT_RIGHT_ASSIGN        = 9;
    public static final int      COLON                         = 10;
    public static final int      COMMA                         = 11;
    public static final int      DEC                           = 12;
    public static final int      DIV                           = 13;
    public static final int      DIV_ASSIGN                    = 14;
    public static final int      DOT                           = 15;
    public static final int      DOTSTAR                       = 16;
    public static final int      ELLIPSIS                      = 17;
    public static final int      EQUAL                         = 18;
    public static final int      GREATER_OR_EQUAL              = 19;
    public static final int      GREATER_THAN                  = 20;
    public static final int      INC                           = 21;
    public static final int      LBRACK                        = 22;
    public static final int      LCURLY                        = 23;
    public static final int      LESS_OR_EQUAL                 = 24;
    public static final int      LESS_THAN                     = 25;
    public static final int      LOGICAL_AND                   = 26;
    public static final int      LOGICAL_NOT                   = 27;
    public static final int      LOGICAL_OR                    = 28;
    public static final int      LPAREN                        = 29;
    public static final int      MINUS                         = 30;
    public static final int      MINUS_ASSIGN                  = 31;
    public static final int      MOD                           = 32;
    public static final int      MOD_ASSIGN                    = 33;
    public static final int      NOT                           = 34;
    public static final int      NOT_EQUAL                     = 35;
    public static final int      OR                            = 36;
    public static final int      OR_ASSIGN                     = 37;
    public static final int      PLUS                          = 38;
    public static final int      PLUS_ASSIGN                   = 39;
    public static final int      QUESTION                      = 40;
    public static final int      RBRACK                        = 41;
    public static final int      RCURLY                        = 42;
    public static final int      RPAREN                        = 43;
    public static final int      SEMI                          = 44;
    public static final int      SHIFT_LEFT                    = 45;
    public static final int      SHIFT_LEFT_ASSIGN             = 46;
    public static final int      SHIFT_RIGHT                   = 47;
    public static final int      SHIFT_RIGHT_ASSIGN            = 48;
    public static final int      STAR                          = 49;
    public static final int      STAR_ASSIGN                   = 50;
    public static final int      XOR                           = 51;
    public static final int      XOR_ASSIGN                    = 52;
    public static final int      ABSTRACT                      = 53;
    public static final int      ASSERT                        = 54;
    public static final int      BOOLEAN                       = 55;
    public static final int      BREAK                         = 56;
    public static final int      BYTE                          = 57;
    public static final int      CASE                          = 58;
    public static final int      CATCH                         = 59;
    public static final int      CHAR                          = 60;
    public static final int      CLASS                         = 61;
    public static final int      CONTINUE                      = 62;
    public static final int      DEFAULT                       = 63;
    public static final int      DO                            = 64;
    public static final int      DOUBLE                        = 65;
    public static final int      ELSE                          = 66;
    public static final int      ENUM                          = 67;
    public static final int      EXTENDS                       = 68;
    public static final int      FALSE                         = 69;
    public static final int      FINAL                         = 70;
    public static final int      FINALLY                       = 71;
    public static final int      FLOAT                         = 72;
    public static final int      FOR                           = 73;
    public static final int      IF                            = 74;
    public static final int      IMPLEMENTS                    = 75;
    public static final int      INSTANCEOF                    = 76;
    public static final int      INTERFACE                     = 77;
    public static final int      IMPORT                        = 78;
    public static final int      INT                           = 79;
    public static final int      LONG                          = 80;
    public static final int      NATIVE                        = 81;
    public static final int      NEW                           = 82;
    public static final int      NULL                          = 83;
    public static final int      PACKAGE                       = 84;
    public static final int      PRIVATE                       = 85;
    public static final int      PROTECTED                     = 86;
    public static final int      PUBLIC                        = 87;
    public static final int      RETURN                        = 88;
    public static final int      SHORT                         = 89;
    public static final int      STATIC                        = 90;
    public static final int      STRICTFP                      = 91;
    public static final int      SUPER                         = 92;
    public static final int      SWITCH                        = 93;
    public static final int      SYNCHRONIZED                  = 94;
    public static final int      THIS                          = 95;
    public static final int      THROW                         = 96;
    public static final int      THROWS                        = 97;
    public static final int      TRANSIENT                     = 98;
    public static final int      TRUE                          = 99;
    public static final int      TRY                           = 100;
    public static final int      VOID                          = 101;
    public static final int      VOLATILE                      = 102;
    public static final int      WHILE                         = 103;
    public static final int      ANNOTATION_INIT_ARRAY_ELEMENT = 104;
    public static final int      ANNOTATION_INIT_BLOCK         = 105;
    public static final int      ANNOTATION_INIT_DEFAULT_KEY   = 106;
    public static final int      ANNOTATION_INIT_KEY_LIST      = 107;
    public static final int      ANNOTATION_LIST               = 108;
    public static final int      ANNOTATION_METHOD_DECL        = 109;
    public static final int      ANNOTATION_SCOPE              = 110;
    public static final int      ANNOTATION_TOP_LEVEL_SCOPE    = 111;
    public static final int      ARGUMENT_LIST                 = 112;
    public static final int      ARRAY_DECLARATOR              = 113;
    public static final int      ARRAY_DECLARATOR_LIST         = 114;
    public static final int      ARRAY_ELEMENT_ACCESS          = 115;
    public static final int      ARRAY_INITIALIZER             = 116;
    public static final int      BLOCK_SCOPE                   = 117;
    public static final int      CAST_EXPR                     = 118;
    public static final int      CATCH_CLAUSE_LIST             = 119;
    public static final int      CLASS_CONSTRUCTOR_CALL        = 120;
    public static final int      CLASS_INSTANCE_INITIALIZER    = 121;
    public static final int      CLASS_STATIC_INITIALIZER      = 122;
    public static final int      CLASS_TOP_LEVEL_SCOPE         = 123;
    public static final int      CONSTRUCTOR_DECL              = 124;
    public static final int      ENUM_TOP_LEVEL_SCOPE          = 125;
    public static final int      EXPR                          = 126;
    public static final int      EXTENDS_BOUND_LIST            = 127;
    public static final int      EXTENDS_CLAUSE                = 128;
    public static final int      FOR_CONDITION                 = 129;
    public static final int      FOR_EACH                      = 130;
    public static final int      FOR_INIT                      = 131;
    public static final int      FOR_UPDATE                    = 132;
    public static final int      FORMAL_PARAM_LIST             = 133;
    public static final int      FORMAL_PARAM_STD_DECL         = 134;
    public static final int      FORMAL_PARAM_VARARG_DECL      = 135;
    public static final int      FUNCTION_METHOD_DECL          = 136;
    public static final int      GENERIC_TYPE_ARG_LIST         = 137;
    public static final int      GENERIC_TYPE_PARAM_LIST       = 138;
    public static final int      INTERFACE_TOP_LEVEL_SCOPE     = 139;
    public static final int      IMPLEMENTS_CLAUSE             = 140;
    public static final int      LABELED_STATEMENT             = 141;
    public static final int      LOCAL_MODIFIER_LIST           = 142;
    public static final int      JAVA_SOURCE                   = 143;
    public static final int      METHOD_CALL                   = 144;
    public static final int      MODIFIER_LIST                 = 145;
    public static final int      PARENTESIZED_EXPR             = 146;
    public static final int      POST_DEC                      = 147;
    public static final int      POST_INC                      = 148;
    public static final int      PRE_DEC                       = 149;
    public static final int      PRE_INC                       = 150;
    public static final int      QUALIFIED_TYPE_IDENT          = 151;
    public static final int      STATIC_ARRAY_CREATOR          = 152;
    public static final int      SUPER_CONSTRUCTOR_CALL        = 153;
    public static final int      SWITCH_BLOCK_LABEL_LIST       = 154;
    public static final int      THIS_CONSTRUCTOR_CALL         = 155;
    public static final int      THROWS_CLAUSE                 = 156;
    public static final int      TYPE                          = 157;
    public static final int      UNARY_MINUS                   = 158;
    public static final int      UNARY_PLUS                    = 159;
    public static final int      VAR_DECLARATION               = 160;
    public static final int      VAR_DECLARATOR                = 161;
    public static final int      VAR_DECLARATOR_LIST           = 162;
    public static final int      VOID_METHOD_DECL              = 163;
    public static final int      IDENT                         = 164;
    public static final int      HEX_LITERAL                   = 165;
    public static final int      OCTAL_LITERAL                 = 166;
    public static final int      DECIMAL_LITERAL               = 167;
    public static final int      FLOATING_POINT_LITERAL        = 168;
    public static final int      CHARACTER_LITERAL             = 169;
    public static final int      STRING_LITERAL                = 170;
    public static final int      HEX_DIGIT                     = 171;
    public static final int      INTEGER_TYPE_SUFFIX           = 172;
    public static final int      EXPONENT                      = 173;
    public static final int      FLOAT_TYPE_SUFFIX             = 174;
    public static final int      ESCAPE_SEQUENCE               = 175;
    public static final int      UNICODE_ESCAPE                = 176;
    public static final int      OCTAL_ESCAPE                  = 177;
    public static final int      JAVA_ID_START                 = 178;
    public static final int      JAVA_ID_PART                  = 179;
    public static final int      WS                            = 180;
    public static final int      COMMENT                       = 181;
    public static final int      LINE_COMMENT                  = 182;

    // delegates
    // delegators


    public JavaTreeParser(TreeNodeStream input) {
        this(input, new RecognizerSharedState());
    }

    public JavaTreeParser(TreeNodeStream input, RecognizerSharedState state) {
        super(input, state);
        this.state.ruleMemo = new HashMap[289 + 1];


    }


    public String[] getTokenNames() {
        return JavaTreeParser.tokenNames;
    }

    public String getGrammarFileName() {
        return "org/eclipse/che/plugin/jdb/server/expression/JavaTreeParser.g";
    }


    /*
    public static com.sun.jdi.Value evaluate(String expr, Evaluator ev) {
       try
       {
          JavaLexer lex = new JavaLexer(new ANTLRStringStream(expr));
          CommonTokenStream tokens = new CommonTokenStream(lex);
          JavaParser parser = new JavaParser(tokens);
          CommonTreeNodeStream nodes = new CommonTreeNodeStream(parser.expression().getTree());
          JavaTreeParser walker = new JavaTreeParser(nodes, ev);
          return walker.evaluate();
       }
       catch (RecognitionException e)
       {
          throw new ExpressionException(e.getMessage(), e);
       }
    }
    */
    boolean mMessageCollectionEnabled = false;
    private boolean mHasErrors = false;
    List<String> mMessages;

    private Evaluator       ev;
    private ExpressionValue latest;

    public JavaTreeParser(TreeNodeStream input, Evaluator ev) {
        this(input);
        this.ev = ev;
    }

    /**
     * Switches error message collection on or of.
     * <p/>
     * The standard destination for parser error messages is <code>System.err</code>.
     * However, if <code>true</code> gets passed to this method this default
     * behaviour will be switched off and all error messages will be collected
     * instead of written to anywhere.
     * <p/>
     * The default value is <code>false</code>.
     *
     * @param pNewState
     *         <code>true</code> if error messages should be collected.
     */
    public void enableErrorMessageCollection(boolean pNewState) {
        mMessageCollectionEnabled = pNewState;
        if (mMessages == null && mMessageCollectionEnabled) {
            mMessages = new ArrayList<String>();
        }
    }

    /**
     * Collects an error message or passes the error message to <code>
     * super.emitErrorMessage(...)</code>.
     * <p/>
     * The actual behaviour depends on whether collecting error messages
     * has been enabled or not.
     *
     * @param pMessage
     *         The error message.
     */
    @Override
    public void emitErrorMessage(String pMessage) {
        if (mMessageCollectionEnabled) {
            mMessages.add(pMessage);
        } else {
            super.emitErrorMessage(pMessage);
        }
    }

    /**
     * Returns collected error messages.
     *
     * @return A list holding collected error messages or <code>null</code> if
     *         collecting error messages hasn't been enabled. Of course, this
     *         list may be empty if no error message has been emited.
     */
    public List<String> getMessages() {
        return mMessages;
    }

    /**
     * Tells if parsing a Java source has caused any error messages.
     *
     * @return <code>true</code> if parsing a Java source has caused at least one error message.
     */
    public boolean hasErrors() {
        return mHasErrors;
    }


    // $ANTLR start "javaSource"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:141:1: javaSource : ^( JAVA_SOURCE annotationList (
    // packageDeclaration )? ( importDeclaration )* ( typeDeclaration )* ) ;
    public final void javaSource() throws RecognitionException {
        int javaSource_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 1)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:142:3: ( ^( JAVA_SOURCE annotationList (
            // packageDeclaration )? ( importDeclaration )* ( typeDeclaration )* ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:143:3: ^( JAVA_SOURCE annotationList (
            // packageDeclaration )? ( importDeclaration )* ( typeDeclaration )* )
            {
                match(input, JAVA_SOURCE, FOLLOW_JAVA_SOURCE_in_javaSource90);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                pushFollow(FOLLOW_annotationList_in_javaSource92);
                annotationList();

                state._fsp--;
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:143:32: ( packageDeclaration )?
                int alt1 = 2;
                int LA1_0 = input.LA(1);

                if ((LA1_0 == PACKAGE)) {
                    alt1 = 1;
                }
                switch (alt1) {
                    case 1:
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: packageDeclaration
                    {
                        pushFollow(FOLLOW_packageDeclaration_in_javaSource94);
                        packageDeclaration();

                        state._fsp--;
                        if (state.failed) return;

                    }
                    break;

                }

                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:143:52: ( importDeclaration )*
                loop2:
                do {
                    int alt2 = 2;
                    int LA2_0 = input.LA(1);

                    if ((LA2_0 == IMPORT)) {
                        alt2 = 1;
                    }


                    switch (alt2) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: importDeclaration
                        {
                            pushFollow(FOLLOW_importDeclaration_in_javaSource97);
                            importDeclaration();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                        default:
                            break loop2;
                    }
                } while (true);

                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:143:71: ( typeDeclaration )*
                loop3:
                do {
                    int alt3 = 2;
                    int LA3_0 = input.LA(1);

                    if ((LA3_0 == AT || LA3_0 == CLASS || LA3_0 == ENUM || LA3_0 == INTERFACE)) {
                        alt3 = 1;
                    }


                    switch (alt3) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: typeDeclaration
                        {
                            pushFollow(FOLLOW_typeDeclaration_in_javaSource100);
                            typeDeclaration();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                        default:
                            break loop3;
                    }
                } while (true);


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 1, javaSource_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "javaSource"


    // $ANTLR start "packageDeclaration"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:146:1: packageDeclaration : ^( PACKAGE
    // qualifiedIdentifier ) ;
    public final void packageDeclaration() throws RecognitionException {
        int packageDeclaration_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 2)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:147:3: ( ^( PACKAGE qualifiedIdentifier ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:148:3: ^( PACKAGE qualifiedIdentifier )
            {
                match(input, PACKAGE, FOLLOW_PACKAGE_in_packageDeclaration118);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                pushFollow(FOLLOW_qualifiedIdentifier_in_packageDeclaration120);
                qualifiedIdentifier();

                state._fsp--;
                if (state.failed) return;

                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 2, packageDeclaration_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "packageDeclaration"


    // $ANTLR start "importDeclaration"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:151:1: importDeclaration : ^( IMPORT ( STATIC )?
    // qualifiedIdentifier ( DOTSTAR )? ) ;
    public final void importDeclaration() throws RecognitionException {
        int importDeclaration_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 3)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:152:3: ( ^( IMPORT ( STATIC )?
            // qualifiedIdentifier ( DOTSTAR )? ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:153:3: ^( IMPORT ( STATIC )? qualifiedIdentifier
            // ( DOTSTAR )? )
            {
                match(input, IMPORT, FOLLOW_IMPORT_in_importDeclaration137);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:153:12: ( STATIC )?
                int alt4 = 2;
                int LA4_0 = input.LA(1);

                if ((LA4_0 == STATIC)) {
                    alt4 = 1;
                }
                switch (alt4) {
                    case 1:
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: STATIC
                    {
                        match(input, STATIC, FOLLOW_STATIC_in_importDeclaration139);
                        if (state.failed) return;

                    }
                    break;

                }

                pushFollow(FOLLOW_qualifiedIdentifier_in_importDeclaration142);
                qualifiedIdentifier();

                state._fsp--;
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:153:40: ( DOTSTAR )?
                int alt5 = 2;
                int LA5_0 = input.LA(1);

                if ((LA5_0 == DOTSTAR)) {
                    alt5 = 1;
                }
                switch (alt5) {
                    case 1:
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: DOTSTAR
                    {
                        match(input, DOTSTAR, FOLLOW_DOTSTAR_in_importDeclaration144);
                        if (state.failed) return;

                    }
                    break;

                }


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 3, importDeclaration_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "importDeclaration"


    // $ANTLR start "typeDeclaration"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:156:1: typeDeclaration : ( ^( CLASS modifierList IDENT (
    // genericTypeParameterList )? ( extendsClause )? ( implementsClause )? classTopLevelScope ) | ^( INTERFACE modifierList IDENT (
    // genericTypeParameterList )? ( extendsClause )? interfaceTopLevelScope ) | ^( ENUM modifierList IDENT ( implementsClause )?
    // enumTopLevelScope ) | ^( AT modifierList IDENT annotationTopLevelScope ) );
    public final void typeDeclaration() throws RecognitionException {
        int typeDeclaration_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 4)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:157:3: ( ^( CLASS modifierList IDENT (
            // genericTypeParameterList )? ( extendsClause )? ( implementsClause )? classTopLevelScope ) | ^( INTERFACE modifierList
            // IDENT ( genericTypeParameterList )? ( extendsClause )? interfaceTopLevelScope ) | ^( ENUM modifierList IDENT (
            // implementsClause )? enumTopLevelScope ) | ^( AT modifierList IDENT annotationTopLevelScope ) )
            int alt12 = 4;
            switch (input.LA(1)) {
                case CLASS: {
                    alt12 = 1;
                }
                break;
                case INTERFACE: {
                    alt12 = 2;
                }
                break;
                case ENUM: {
                    alt12 = 3;
                }
                break;
                case AT: {
                    alt12 = 4;
                }
                break;
                default:
                    if (state.backtracking > 0) {
                        state.failed = true;
                        return;
                    }
                    NoViableAltException nvae =
                            new NoViableAltException("", 12, 0, input);

                    throw nvae;
            }

            switch (alt12) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:158:3: ^( CLASS modifierList IDENT (
                    // genericTypeParameterList )? ( extendsClause )? ( implementsClause )? classTopLevelScope )
                {
                    match(input, CLASS, FOLLOW_CLASS_in_typeDeclaration162);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_modifierList_in_typeDeclaration164);
                    modifierList();

                    state._fsp--;
                    if (state.failed) return;
                    match(input, IDENT, FOLLOW_IDENT_in_typeDeclaration166);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:158:30: ( genericTypeParameterList )?
                    int alt6 = 2;
                    int LA6_0 = input.LA(1);

                    if ((LA6_0 == GENERIC_TYPE_PARAM_LIST)) {
                        alt6 = 1;
                    }
                    switch (alt6) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: genericTypeParameterList
                        {
                            pushFollow(FOLLOW_genericTypeParameterList_in_typeDeclaration168);
                            genericTypeParameterList();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:158:56: ( extendsClause )?
                    int alt7 = 2;
                    int LA7_0 = input.LA(1);

                    if ((LA7_0 == EXTENDS_CLAUSE)) {
                        alt7 = 1;
                    }
                    switch (alt7) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: extendsClause
                        {
                            pushFollow(FOLLOW_extendsClause_in_typeDeclaration171);
                            extendsClause();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:158:71: ( implementsClause )?
                    int alt8 = 2;
                    int LA8_0 = input.LA(1);

                    if ((LA8_0 == IMPLEMENTS_CLAUSE)) {
                        alt8 = 1;
                    }
                    switch (alt8) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: implementsClause
                        {
                            pushFollow(FOLLOW_implementsClause_in_typeDeclaration174);
                            implementsClause();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    pushFollow(FOLLOW_classTopLevelScope_in_typeDeclaration177);
                    classTopLevelScope();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:160:3: ^( INTERFACE modifierList IDENT (
                    // genericTypeParameterList )? ( extendsClause )? interfaceTopLevelScope )
                {
                    match(input, INTERFACE, FOLLOW_INTERFACE_in_typeDeclaration187);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_modifierList_in_typeDeclaration189);
                    modifierList();

                    state._fsp--;
                    if (state.failed) return;
                    match(input, IDENT, FOLLOW_IDENT_in_typeDeclaration191);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:160:34: ( genericTypeParameterList )?
                    int alt9 = 2;
                    int LA9_0 = input.LA(1);

                    if ((LA9_0 == GENERIC_TYPE_PARAM_LIST)) {
                        alt9 = 1;
                    }
                    switch (alt9) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: genericTypeParameterList
                        {
                            pushFollow(FOLLOW_genericTypeParameterList_in_typeDeclaration193);
                            genericTypeParameterList();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:160:60: ( extendsClause )?
                    int alt10 = 2;
                    int LA10_0 = input.LA(1);

                    if ((LA10_0 == EXTENDS_CLAUSE)) {
                        alt10 = 1;
                    }
                    switch (alt10) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: extendsClause
                        {
                            pushFollow(FOLLOW_extendsClause_in_typeDeclaration196);
                            extendsClause();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    pushFollow(FOLLOW_interfaceTopLevelScope_in_typeDeclaration199);
                    interfaceTopLevelScope();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 3:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:162:3: ^( ENUM modifierList IDENT (
                    // implementsClause )? enumTopLevelScope )
                {
                    match(input, ENUM, FOLLOW_ENUM_in_typeDeclaration209);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_modifierList_in_typeDeclaration211);
                    modifierList();

                    state._fsp--;
                    if (state.failed) return;
                    match(input, IDENT, FOLLOW_IDENT_in_typeDeclaration213);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:162:29: ( implementsClause )?
                    int alt11 = 2;
                    int LA11_0 = input.LA(1);

                    if ((LA11_0 == IMPLEMENTS_CLAUSE)) {
                        alt11 = 1;
                    }
                    switch (alt11) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: implementsClause
                        {
                            pushFollow(FOLLOW_implementsClause_in_typeDeclaration215);
                            implementsClause();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    pushFollow(FOLLOW_enumTopLevelScope_in_typeDeclaration218);
                    enumTopLevelScope();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 4:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:164:3: ^( AT modifierList IDENT
                    // annotationTopLevelScope )
                {
                    match(input, AT, FOLLOW_AT_in_typeDeclaration228);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_modifierList_in_typeDeclaration230);
                    modifierList();

                    state._fsp--;
                    if (state.failed) return;
                    match(input, IDENT, FOLLOW_IDENT_in_typeDeclaration232);
                    if (state.failed) return;
                    pushFollow(FOLLOW_annotationTopLevelScope_in_typeDeclaration234);
                    annotationTopLevelScope();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 4, typeDeclaration_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "typeDeclaration"


    // $ANTLR start "extendsClause"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:167:1: extendsClause : ^( EXTENDS_CLAUSE ( type )+ ) ;
    public final void extendsClause() throws RecognitionException {
        int extendsClause_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 5)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:169:3: ( ^( EXTENDS_CLAUSE ( type )+ ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:170:3: ^( EXTENDS_CLAUSE ( type )+ )
            {
                match(input, EXTENDS_CLAUSE, FOLLOW_EXTENDS_CLAUSE_in_extendsClause255);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:170:20: ( type )+
                int cnt13 = 0;
                loop13:
                do {
                    int alt13 = 2;
                    int LA13_0 = input.LA(1);

                    if ((LA13_0 == TYPE)) {
                        alt13 = 1;
                    }


                    switch (alt13) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: type
                        {
                            pushFollow(FOLLOW_type_in_extendsClause257);
                            type();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                        default:
                            if (cnt13 >= 1) break loop13;
                            if (state.backtracking > 0) {
                                state.failed = true;
                                return;
                            }
                            EarlyExitException eee =
                                    new EarlyExitException(13, input);
                            throw eee;
                    }
                    cnt13++;
                } while (true);


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 5, extendsClause_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "extendsClause"


    // $ANTLR start "implementsClause"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:173:1: implementsClause : ^( IMPLEMENTS_CLAUSE ( type )+
    // ) ;
    public final void implementsClause() throws RecognitionException {
        int implementsClause_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 6)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:174:3: ( ^( IMPLEMENTS_CLAUSE ( type )+ ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:175:3: ^( IMPLEMENTS_CLAUSE ( type )+ )
            {
                match(input, IMPLEMENTS_CLAUSE, FOLLOW_IMPLEMENTS_CLAUSE_in_implementsClause275);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:175:23: ( type )+
                int cnt14 = 0;
                loop14:
                do {
                    int alt14 = 2;
                    int LA14_0 = input.LA(1);

                    if ((LA14_0 == TYPE)) {
                        alt14 = 1;
                    }


                    switch (alt14) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: type
                        {
                            pushFollow(FOLLOW_type_in_implementsClause277);
                            type();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                        default:
                            if (cnt14 >= 1) break loop14;
                            if (state.backtracking > 0) {
                                state.failed = true;
                                return;
                            }
                            EarlyExitException eee =
                                    new EarlyExitException(14, input);
                            throw eee;
                    }
                    cnt14++;
                } while (true);


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 6, implementsClause_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "implementsClause"


    // $ANTLR start "genericTypeParameterList"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:178:1: genericTypeParameterList : ^(
    // GENERIC_TYPE_PARAM_LIST ( genericTypeParameter )+ ) ;
    public final void genericTypeParameterList() throws RecognitionException {
        int genericTypeParameterList_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 7)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:179:3: ( ^( GENERIC_TYPE_PARAM_LIST (
            // genericTypeParameter )+ ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:180:3: ^( GENERIC_TYPE_PARAM_LIST (
            // genericTypeParameter )+ )
            {
                match(input, GENERIC_TYPE_PARAM_LIST, FOLLOW_GENERIC_TYPE_PARAM_LIST_in_genericTypeParameterList295);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:180:29: ( genericTypeParameter )+
                int cnt15 = 0;
                loop15:
                do {
                    int alt15 = 2;
                    int LA15_0 = input.LA(1);

                    if ((LA15_0 == IDENT)) {
                        alt15 = 1;
                    }


                    switch (alt15) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: genericTypeParameter
                        {
                            pushFollow(FOLLOW_genericTypeParameter_in_genericTypeParameterList297);
                            genericTypeParameter();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                        default:
                            if (cnt15 >= 1) break loop15;
                            if (state.backtracking > 0) {
                                state.failed = true;
                                return;
                            }
                            EarlyExitException eee =
                                    new EarlyExitException(15, input);
                            throw eee;
                    }
                    cnt15++;
                } while (true);


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 7, genericTypeParameterList_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "genericTypeParameterList"


    // $ANTLR start "genericTypeParameter"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:183:1: genericTypeParameter : ^( IDENT ( bound )? ) ;
    public final void genericTypeParameter() throws RecognitionException {
        int genericTypeParameter_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 8)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:184:3: ( ^( IDENT ( bound )? ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:185:3: ^( IDENT ( bound )? )
            {
                match(input, IDENT, FOLLOW_IDENT_in_genericTypeParameter315);
                if (state.failed) return;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:185:11: ( bound )?
                    int alt16 = 2;
                    int LA16_0 = input.LA(1);

                    if ((LA16_0 == EXTENDS_BOUND_LIST)) {
                        alt16 = 1;
                    }
                    switch (alt16) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: bound
                        {
                            pushFollow(FOLLOW_bound_in_genericTypeParameter317);
                            bound();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }


                    match(input, Token.UP, null);
                    if (state.failed) return;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 8, genericTypeParameter_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "genericTypeParameter"


    // $ANTLR start "bound"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:188:1: bound : ^( EXTENDS_BOUND_LIST ( type )+ ) ;
    public final void bound() throws RecognitionException {
        int bound_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 9)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:189:3: ( ^( EXTENDS_BOUND_LIST ( type )+ ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:190:3: ^( EXTENDS_BOUND_LIST ( type )+ )
            {
                match(input, EXTENDS_BOUND_LIST, FOLLOW_EXTENDS_BOUND_LIST_in_bound335);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:190:24: ( type )+
                int cnt17 = 0;
                loop17:
                do {
                    int alt17 = 2;
                    int LA17_0 = input.LA(1);

                    if ((LA17_0 == TYPE)) {
                        alt17 = 1;
                    }


                    switch (alt17) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: type
                        {
                            pushFollow(FOLLOW_type_in_bound337);
                            type();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                        default:
                            if (cnt17 >= 1) break loop17;
                            if (state.backtracking > 0) {
                                state.failed = true;
                                return;
                            }
                            EarlyExitException eee =
                                    new EarlyExitException(17, input);
                            throw eee;
                    }
                    cnt17++;
                } while (true);


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 9, bound_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "bound"


    // $ANTLR start "enumTopLevelScope"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:193:1: enumTopLevelScope : ^( ENUM_TOP_LEVEL_SCOPE (
    // enumConstant )+ ( classTopLevelScope )? ) ;
    public final void enumTopLevelScope() throws RecognitionException {
        int enumTopLevelScope_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 10)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:194:3: ( ^( ENUM_TOP_LEVEL_SCOPE ( enumConstant
            // )+ ( classTopLevelScope )? ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:195:3: ^( ENUM_TOP_LEVEL_SCOPE ( enumConstant )+
            // ( classTopLevelScope )? )
            {
                match(input, ENUM_TOP_LEVEL_SCOPE, FOLLOW_ENUM_TOP_LEVEL_SCOPE_in_enumTopLevelScope355);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:195:26: ( enumConstant )+
                int cnt18 = 0;
                loop18:
                do {
                    int alt18 = 2;
                    int LA18_0 = input.LA(1);

                    if ((LA18_0 == IDENT)) {
                        alt18 = 1;
                    }


                    switch (alt18) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: enumConstant
                        {
                            pushFollow(FOLLOW_enumConstant_in_enumTopLevelScope357);
                            enumConstant();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                        default:
                            if (cnt18 >= 1) break loop18;
                            if (state.backtracking > 0) {
                                state.failed = true;
                                return;
                            }
                            EarlyExitException eee =
                                    new EarlyExitException(18, input);
                            throw eee;
                    }
                    cnt18++;
                } while (true);

                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:195:40: ( classTopLevelScope )?
                int alt19 = 2;
                int LA19_0 = input.LA(1);

                if ((LA19_0 == CLASS_TOP_LEVEL_SCOPE)) {
                    alt19 = 1;
                }
                switch (alt19) {
                    case 1:
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: classTopLevelScope
                    {
                        pushFollow(FOLLOW_classTopLevelScope_in_enumTopLevelScope360);
                        classTopLevelScope();

                        state._fsp--;
                        if (state.failed) return;

                    }
                    break;

                }


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 10, enumTopLevelScope_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "enumTopLevelScope"


    // $ANTLR start "enumConstant"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:198:1: enumConstant : ^( IDENT annotationList (
    // arguments )? ( classTopLevelScope )? ) ;
    public final void enumConstant() throws RecognitionException {
        int enumConstant_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 11)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:199:3: ( ^( IDENT annotationList ( arguments )?
            // ( classTopLevelScope )? ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:200:3: ^( IDENT annotationList ( arguments )? (
            // classTopLevelScope )? )
            {
                match(input, IDENT, FOLLOW_IDENT_in_enumConstant378);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                pushFollow(FOLLOW_annotationList_in_enumConstant380);
                annotationList();

                state._fsp--;
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:200:26: ( arguments )?
                int alt20 = 2;
                int LA20_0 = input.LA(1);

                if ((LA20_0 == ARGUMENT_LIST)) {
                    alt20 = 1;
                }
                switch (alt20) {
                    case 1:
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: arguments
                    {
                        pushFollow(FOLLOW_arguments_in_enumConstant382);
                        arguments();

                        state._fsp--;
                        if (state.failed) return;

                    }
                    break;

                }

                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:200:37: ( classTopLevelScope )?
                int alt21 = 2;
                int LA21_0 = input.LA(1);

                if ((LA21_0 == CLASS_TOP_LEVEL_SCOPE)) {
                    alt21 = 1;
                }
                switch (alt21) {
                    case 1:
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: classTopLevelScope
                    {
                        pushFollow(FOLLOW_classTopLevelScope_in_enumConstant385);
                        classTopLevelScope();

                        state._fsp--;
                        if (state.failed) return;

                    }
                    break;

                }


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 11, enumConstant_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "enumConstant"


    // $ANTLR start "classTopLevelScope"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:203:1: classTopLevelScope : ^( CLASS_TOP_LEVEL_SCOPE (
    // classScopeDeclarations )* ) ;
    public final void classTopLevelScope() throws RecognitionException {
        int classTopLevelScope_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 12)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:204:3: ( ^( CLASS_TOP_LEVEL_SCOPE (
            // classScopeDeclarations )* ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:205:3: ^( CLASS_TOP_LEVEL_SCOPE (
            // classScopeDeclarations )* )
            {
                match(input, CLASS_TOP_LEVEL_SCOPE, FOLLOW_CLASS_TOP_LEVEL_SCOPE_in_classTopLevelScope403);
                if (state.failed) return;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:205:27: ( classScopeDeclarations )*
                    loop22:
                    do {
                        int alt22 = 2;
                        int LA22_0 = input.LA(1);

                        if ((LA22_0 == AT || LA22_0 == CLASS || LA22_0 == ENUM || LA22_0 == INTERFACE ||
                             (LA22_0 >= CLASS_INSTANCE_INITIALIZER && LA22_0 <= CLASS_STATIC_INITIALIZER) || LA22_0 == CONSTRUCTOR_DECL ||
                             LA22_0 == FUNCTION_METHOD_DECL || LA22_0 == VAR_DECLARATION || LA22_0 == VOID_METHOD_DECL)) {
                            alt22 = 1;
                        }


                        switch (alt22) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: classScopeDeclarations
                            {
                                pushFollow(FOLLOW_classScopeDeclarations_in_classTopLevelScope405);
                                classScopeDeclarations();

                                state._fsp--;
                                if (state.failed) return;

                            }
                            break;

                            default:
                                break loop22;
                        }
                    } while (true);


                    match(input, Token.UP, null);
                    if (state.failed) return;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 12, classTopLevelScope_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "classTopLevelScope"


    // $ANTLR start "classScopeDeclarations"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:208:1: classScopeDeclarations : ( ^(
    // CLASS_INSTANCE_INITIALIZER block ) | ^( CLASS_STATIC_INITIALIZER block ) | ^( FUNCTION_METHOD_DECL modifierList (
    // genericTypeParameterList )? type IDENT formalParameterList ( arrayDeclaratorList )? ( throwsClause )? ( block )? ) | ^(
    // VOID_METHOD_DECL modifierList ( genericTypeParameterList )? IDENT formalParameterList ( throwsClause )? ( block )? ) | ^(
    // VAR_DECLARATION modifierList type variableDeclaratorList ) | ^( CONSTRUCTOR_DECL modifierList ( genericTypeParameterList )?
    // formalParameterList ( throwsClause )? block ) | typeDeclaration );
    public final void classScopeDeclarations() throws RecognitionException {
        int classScopeDeclarations_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 13)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:209:3: ( ^( CLASS_INSTANCE_INITIALIZER block ) |
            // ^( CLASS_STATIC_INITIALIZER block ) | ^( FUNCTION_METHOD_DECL modifierList ( genericTypeParameterList )? type IDENT
            // formalParameterList ( arrayDeclaratorList )? ( throwsClause )? ( block )? ) | ^( VOID_METHOD_DECL modifierList (
            // genericTypeParameterList )? IDENT formalParameterList ( throwsClause )? ( block )? ) | ^( VAR_DECLARATION modifierList
            // type variableDeclaratorList ) | ^( CONSTRUCTOR_DECL modifierList ( genericTypeParameterList )? formalParameterList (
            // throwsClause )? block ) | typeDeclaration )
            int alt32 = 7;
            switch (input.LA(1)) {
                case CLASS_INSTANCE_INITIALIZER: {
                    alt32 = 1;
                }
                break;
                case CLASS_STATIC_INITIALIZER: {
                    alt32 = 2;
                }
                break;
                case FUNCTION_METHOD_DECL: {
                    alt32 = 3;
                }
                break;
                case VOID_METHOD_DECL: {
                    alt32 = 4;
                }
                break;
                case VAR_DECLARATION: {
                    alt32 = 5;
                }
                break;
                case CONSTRUCTOR_DECL: {
                    alt32 = 6;
                }
                break;
                case AT:
                case CLASS:
                case ENUM:
                case INTERFACE: {
                    alt32 = 7;
                }
                break;
                default:
                    if (state.backtracking > 0) {
                        state.failed = true;
                        return;
                    }
                    NoViableAltException nvae =
                            new NoViableAltException("", 32, 0, input);

                    throw nvae;
            }

            switch (alt32) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:210:3: ^( CLASS_INSTANCE_INITIALIZER
                    // block )
                {
                    match(input, CLASS_INSTANCE_INITIALIZER, FOLLOW_CLASS_INSTANCE_INITIALIZER_in_classScopeDeclarations423);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_block_in_classScopeDeclarations425);
                    block();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:212:3: ^( CLASS_STATIC_INITIALIZER block )
                {
                    match(input, CLASS_STATIC_INITIALIZER, FOLLOW_CLASS_STATIC_INITIALIZER_in_classScopeDeclarations435);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_block_in_classScopeDeclarations437);
                    block();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 3:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:214:3: ^( FUNCTION_METHOD_DECL
                    // modifierList ( genericTypeParameterList )? type IDENT formalParameterList ( arrayDeclaratorList )? ( throwsClause
                    // )? ( block )? )
                {
                    match(input, FUNCTION_METHOD_DECL, FOLLOW_FUNCTION_METHOD_DECL_in_classScopeDeclarations447);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_modifierList_in_classScopeDeclarations449);
                    modifierList();

                    state._fsp--;
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:214:39: ( genericTypeParameterList )?
                    int alt23 = 2;
                    int LA23_0 = input.LA(1);

                    if ((LA23_0 == GENERIC_TYPE_PARAM_LIST)) {
                        alt23 = 1;
                    }
                    switch (alt23) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: genericTypeParameterList
                        {
                            pushFollow(FOLLOW_genericTypeParameterList_in_classScopeDeclarations451);
                            genericTypeParameterList();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    pushFollow(FOLLOW_type_in_classScopeDeclarations454);
                    type();

                    state._fsp--;
                    if (state.failed) return;
                    match(input, IDENT, FOLLOW_IDENT_in_classScopeDeclarations456);
                    if (state.failed) return;
                    pushFollow(FOLLOW_formalParameterList_in_classScopeDeclarations458);
                    formalParameterList();

                    state._fsp--;
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:214:96: ( arrayDeclaratorList )?
                    int alt24 = 2;
                    int LA24_0 = input.LA(1);

                    if ((LA24_0 == ARRAY_DECLARATOR_LIST)) {
                        alt24 = 1;
                    }
                    switch (alt24) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: arrayDeclaratorList
                        {
                            pushFollow(FOLLOW_arrayDeclaratorList_in_classScopeDeclarations460);
                            arrayDeclaratorList();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:214:117: ( throwsClause )?
                    int alt25 = 2;
                    int LA25_0 = input.LA(1);

                    if ((LA25_0 == THROWS_CLAUSE)) {
                        alt25 = 1;
                    }
                    switch (alt25) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: throwsClause
                        {
                            pushFollow(FOLLOW_throwsClause_in_classScopeDeclarations463);
                            throwsClause();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:214:131: ( block )?
                    int alt26 = 2;
                    int LA26_0 = input.LA(1);

                    if ((LA26_0 == BLOCK_SCOPE)) {
                        alt26 = 1;
                    }
                    switch (alt26) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: block
                        {
                            pushFollow(FOLLOW_block_in_classScopeDeclarations466);
                            block();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }


                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 4:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:216:3: ^( VOID_METHOD_DECL modifierList
                    // ( genericTypeParameterList )? IDENT formalParameterList ( throwsClause )? ( block )? )
                {
                    match(input, VOID_METHOD_DECL, FOLLOW_VOID_METHOD_DECL_in_classScopeDeclarations477);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_modifierList_in_classScopeDeclarations479);
                    modifierList();

                    state._fsp--;
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:216:35: ( genericTypeParameterList )?
                    int alt27 = 2;
                    int LA27_0 = input.LA(1);

                    if ((LA27_0 == GENERIC_TYPE_PARAM_LIST)) {
                        alt27 = 1;
                    }
                    switch (alt27) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: genericTypeParameterList
                        {
                            pushFollow(FOLLOW_genericTypeParameterList_in_classScopeDeclarations481);
                            genericTypeParameterList();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    match(input, IDENT, FOLLOW_IDENT_in_classScopeDeclarations484);
                    if (state.failed) return;
                    pushFollow(FOLLOW_formalParameterList_in_classScopeDeclarations486);
                    formalParameterList();

                    state._fsp--;
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:216:87: ( throwsClause )?
                    int alt28 = 2;
                    int LA28_0 = input.LA(1);

                    if ((LA28_0 == THROWS_CLAUSE)) {
                        alt28 = 1;
                    }
                    switch (alt28) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: throwsClause
                        {
                            pushFollow(FOLLOW_throwsClause_in_classScopeDeclarations488);
                            throwsClause();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:216:101: ( block )?
                    int alt29 = 2;
                    int LA29_0 = input.LA(1);

                    if ((LA29_0 == BLOCK_SCOPE)) {
                        alt29 = 1;
                    }
                    switch (alt29) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: block
                        {
                            pushFollow(FOLLOW_block_in_classScopeDeclarations491);
                            block();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }


                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 5:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:218:3: ^( VAR_DECLARATION modifierList
                    // type variableDeclaratorList )
                {
                    match(input, VAR_DECLARATION, FOLLOW_VAR_DECLARATION_in_classScopeDeclarations502);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_modifierList_in_classScopeDeclarations504);
                    modifierList();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_type_in_classScopeDeclarations506);
                    type();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_variableDeclaratorList_in_classScopeDeclarations508);
                    variableDeclaratorList();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 6:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:220:3: ^( CONSTRUCTOR_DECL modifierList
                    // ( genericTypeParameterList )? formalParameterList ( throwsClause )? block )
                {
                    match(input, CONSTRUCTOR_DECL, FOLLOW_CONSTRUCTOR_DECL_in_classScopeDeclarations518);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_modifierList_in_classScopeDeclarations520);
                    modifierList();

                    state._fsp--;
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:220:35: ( genericTypeParameterList )?
                    int alt30 = 2;
                    int LA30_0 = input.LA(1);

                    if ((LA30_0 == GENERIC_TYPE_PARAM_LIST)) {
                        alt30 = 1;
                    }
                    switch (alt30) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: genericTypeParameterList
                        {
                            pushFollow(FOLLOW_genericTypeParameterList_in_classScopeDeclarations522);
                            genericTypeParameterList();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    pushFollow(FOLLOW_formalParameterList_in_classScopeDeclarations525);
                    formalParameterList();

                    state._fsp--;
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:220:81: ( throwsClause )?
                    int alt31 = 2;
                    int LA31_0 = input.LA(1);

                    if ((LA31_0 == THROWS_CLAUSE)) {
                        alt31 = 1;
                    }
                    switch (alt31) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: throwsClause
                        {
                            pushFollow(FOLLOW_throwsClause_in_classScopeDeclarations527);
                            throwsClause();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    pushFollow(FOLLOW_block_in_classScopeDeclarations530);
                    block();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 7:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:221:5: typeDeclaration
                {
                    pushFollow(FOLLOW_typeDeclaration_in_classScopeDeclarations537);
                    typeDeclaration();

                    state._fsp--;
                    if (state.failed) return;

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 13, classScopeDeclarations_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "classScopeDeclarations"


    // $ANTLR start "interfaceTopLevelScope"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:224:1: interfaceTopLevelScope : ^(
    // INTERFACE_TOP_LEVEL_SCOPE ( interfaceScopeDeclarations )* ) ;
    public final void interfaceTopLevelScope() throws RecognitionException {
        int interfaceTopLevelScope_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 14)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:225:3: ( ^( INTERFACE_TOP_LEVEL_SCOPE (
            // interfaceScopeDeclarations )* ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:226:3: ^( INTERFACE_TOP_LEVEL_SCOPE (
            // interfaceScopeDeclarations )* )
            {
                match(input, INTERFACE_TOP_LEVEL_SCOPE, FOLLOW_INTERFACE_TOP_LEVEL_SCOPE_in_interfaceTopLevelScope553);
                if (state.failed) return;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:226:31: ( interfaceScopeDeclarations )*
                    loop33:
                    do {
                        int alt33 = 2;
                        int LA33_0 = input.LA(1);

                        if ((LA33_0 == AT || LA33_0 == CLASS || LA33_0 == ENUM || LA33_0 == INTERFACE || LA33_0 == FUNCTION_METHOD_DECL ||
                             LA33_0 == VAR_DECLARATION || LA33_0 == VOID_METHOD_DECL)) {
                            alt33 = 1;
                        }


                        switch (alt33) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: interfaceScopeDeclarations
                            {
                                pushFollow(FOLLOW_interfaceScopeDeclarations_in_interfaceTopLevelScope555);
                                interfaceScopeDeclarations();

                                state._fsp--;
                                if (state.failed) return;

                            }
                            break;

                            default:
                                break loop33;
                        }
                    } while (true);


                    match(input, Token.UP, null);
                    if (state.failed) return;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 14, interfaceTopLevelScope_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "interfaceTopLevelScope"


    // $ANTLR start "interfaceScopeDeclarations"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:229:1: interfaceScopeDeclarations : ( ^(
    // FUNCTION_METHOD_DECL modifierList ( genericTypeParameterList )? type IDENT formalParameterList ( arrayDeclaratorList )? (
    // throwsClause )? ) | ^( VOID_METHOD_DECL modifierList ( genericTypeParameterList )? IDENT formalParameterList ( throwsClause )? ) |
    // ^( VAR_DECLARATION modifierList type variableDeclaratorList ) | typeDeclaration );
    public final void interfaceScopeDeclarations() throws RecognitionException {
        int interfaceScopeDeclarations_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 15)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:230:3: ( ^( FUNCTION_METHOD_DECL modifierList (
            // genericTypeParameterList )? type IDENT formalParameterList ( arrayDeclaratorList )? ( throwsClause )? ) | ^(
            // VOID_METHOD_DECL modifierList ( genericTypeParameterList )? IDENT formalParameterList ( throwsClause )? ) | ^(
            // VAR_DECLARATION modifierList type variableDeclaratorList ) | typeDeclaration )
            int alt39 = 4;
            switch (input.LA(1)) {
                case FUNCTION_METHOD_DECL: {
                    alt39 = 1;
                }
                break;
                case VOID_METHOD_DECL: {
                    alt39 = 2;
                }
                break;
                case VAR_DECLARATION: {
                    alt39 = 3;
                }
                break;
                case AT:
                case CLASS:
                case ENUM:
                case INTERFACE: {
                    alt39 = 4;
                }
                break;
                default:
                    if (state.backtracking > 0) {
                        state.failed = true;
                        return;
                    }
                    NoViableAltException nvae =
                            new NoViableAltException("", 39, 0, input);

                    throw nvae;
            }

            switch (alt39) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:231:3: ^( FUNCTION_METHOD_DECL
                    // modifierList ( genericTypeParameterList )? type IDENT formalParameterList ( arrayDeclaratorList )? ( throwsClause
                    // )? )
                {
                    match(input, FUNCTION_METHOD_DECL, FOLLOW_FUNCTION_METHOD_DECL_in_interfaceScopeDeclarations573);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_modifierList_in_interfaceScopeDeclarations575);
                    modifierList();

                    state._fsp--;
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:231:39: ( genericTypeParameterList )?
                    int alt34 = 2;
                    int LA34_0 = input.LA(1);

                    if ((LA34_0 == GENERIC_TYPE_PARAM_LIST)) {
                        alt34 = 1;
                    }
                    switch (alt34) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: genericTypeParameterList
                        {
                            pushFollow(FOLLOW_genericTypeParameterList_in_interfaceScopeDeclarations577);
                            genericTypeParameterList();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    pushFollow(FOLLOW_type_in_interfaceScopeDeclarations580);
                    type();

                    state._fsp--;
                    if (state.failed) return;
                    match(input, IDENT, FOLLOW_IDENT_in_interfaceScopeDeclarations582);
                    if (state.failed) return;
                    pushFollow(FOLLOW_formalParameterList_in_interfaceScopeDeclarations584);
                    formalParameterList();

                    state._fsp--;
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:231:96: ( arrayDeclaratorList )?
                    int alt35 = 2;
                    int LA35_0 = input.LA(1);

                    if ((LA35_0 == ARRAY_DECLARATOR_LIST)) {
                        alt35 = 1;
                    }
                    switch (alt35) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: arrayDeclaratorList
                        {
                            pushFollow(FOLLOW_arrayDeclaratorList_in_interfaceScopeDeclarations586);
                            arrayDeclaratorList();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:231:117: ( throwsClause )?
                    int alt36 = 2;
                    int LA36_0 = input.LA(1);

                    if ((LA36_0 == THROWS_CLAUSE)) {
                        alt36 = 1;
                    }
                    switch (alt36) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: throwsClause
                        {
                            pushFollow(FOLLOW_throwsClause_in_interfaceScopeDeclarations589);
                            throwsClause();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }


                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:233:3: ^( VOID_METHOD_DECL modifierList
                    // ( genericTypeParameterList )? IDENT formalParameterList ( throwsClause )? )
                {
                    match(input, VOID_METHOD_DECL, FOLLOW_VOID_METHOD_DECL_in_interfaceScopeDeclarations600);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_modifierList_in_interfaceScopeDeclarations602);
                    modifierList();

                    state._fsp--;
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:233:35: ( genericTypeParameterList )?
                    int alt37 = 2;
                    int LA37_0 = input.LA(1);

                    if ((LA37_0 == GENERIC_TYPE_PARAM_LIST)) {
                        alt37 = 1;
                    }
                    switch (alt37) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: genericTypeParameterList
                        {
                            pushFollow(FOLLOW_genericTypeParameterList_in_interfaceScopeDeclarations604);
                            genericTypeParameterList();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    match(input, IDENT, FOLLOW_IDENT_in_interfaceScopeDeclarations607);
                    if (state.failed) return;
                    pushFollow(FOLLOW_formalParameterList_in_interfaceScopeDeclarations609);
                    formalParameterList();

                    state._fsp--;
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:233:87: ( throwsClause )?
                    int alt38 = 2;
                    int LA38_0 = input.LA(1);

                    if ((LA38_0 == THROWS_CLAUSE)) {
                        alt38 = 1;
                    }
                    switch (alt38) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: throwsClause
                        {
                            pushFollow(FOLLOW_throwsClause_in_interfaceScopeDeclarations611);
                            throwsClause();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }


                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 3:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:238:3: ^( VAR_DECLARATION modifierList
                    // type variableDeclaratorList )
                {
                    match(input, VAR_DECLARATION, FOLLOW_VAR_DECLARATION_in_interfaceScopeDeclarations631);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_modifierList_in_interfaceScopeDeclarations633);
                    modifierList();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_type_in_interfaceScopeDeclarations635);
                    type();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_variableDeclaratorList_in_interfaceScopeDeclarations637);
                    variableDeclaratorList();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 4:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:239:5: typeDeclaration
                {
                    pushFollow(FOLLOW_typeDeclaration_in_interfaceScopeDeclarations644);
                    typeDeclaration();

                    state._fsp--;
                    if (state.failed) return;

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 15, interfaceScopeDeclarations_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "interfaceScopeDeclarations"


    // $ANTLR start "variableDeclaratorList"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:242:1: variableDeclaratorList : ^( VAR_DECLARATOR_LIST (
    // variableDeclarator )+ ) ;
    public final void variableDeclaratorList() throws RecognitionException {
        int variableDeclaratorList_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 16)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:243:3: ( ^( VAR_DECLARATOR_LIST (
            // variableDeclarator )+ ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:244:3: ^( VAR_DECLARATOR_LIST (
            // variableDeclarator )+ )
            {
                match(input, VAR_DECLARATOR_LIST, FOLLOW_VAR_DECLARATOR_LIST_in_variableDeclaratorList660);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:244:25: ( variableDeclarator )+
                int cnt40 = 0;
                loop40:
                do {
                    int alt40 = 2;
                    int LA40_0 = input.LA(1);

                    if ((LA40_0 == VAR_DECLARATOR)) {
                        alt40 = 1;
                    }


                    switch (alt40) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: variableDeclarator
                        {
                            pushFollow(FOLLOW_variableDeclarator_in_variableDeclaratorList662);
                            variableDeclarator();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                        default:
                            if (cnt40 >= 1) break loop40;
                            if (state.backtracking > 0) {
                                state.failed = true;
                                return;
                            }
                            EarlyExitException eee =
                                    new EarlyExitException(40, input);
                            throw eee;
                    }
                    cnt40++;
                } while (true);


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 16, variableDeclaratorList_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "variableDeclaratorList"


    // $ANTLR start "variableDeclarator"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:247:1: variableDeclarator : ^( VAR_DECLARATOR
    // variableDeclaratorId ( variableInitializer )? ) ;
    public final void variableDeclarator() throws RecognitionException {
        int variableDeclarator_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 17)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:248:3: ( ^( VAR_DECLARATOR variableDeclaratorId
            // ( variableInitializer )? ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:249:3: ^( VAR_DECLARATOR variableDeclaratorId (
            // variableInitializer )? )
            {
                match(input, VAR_DECLARATOR, FOLLOW_VAR_DECLARATOR_in_variableDeclarator680);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                pushFollow(FOLLOW_variableDeclaratorId_in_variableDeclarator682);
                variableDeclaratorId();

                state._fsp--;
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:249:41: ( variableInitializer )?
                int alt41 = 2;
                int LA41_0 = input.LA(1);

                if ((LA41_0 == ARRAY_INITIALIZER || LA41_0 == EXPR)) {
                    alt41 = 1;
                }
                switch (alt41) {
                    case 1:
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: variableInitializer
                    {
                        pushFollow(FOLLOW_variableInitializer_in_variableDeclarator684);
                        variableInitializer();

                        state._fsp--;
                        if (state.failed) return;

                    }
                    break;

                }


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 17, variableDeclarator_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "variableDeclarator"


    // $ANTLR start "variableDeclaratorId"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:252:1: variableDeclaratorId : ^( IDENT (
    // arrayDeclaratorList )? ) ;
    public final void variableDeclaratorId() throws RecognitionException {
        int variableDeclaratorId_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 18)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:253:3: ( ^( IDENT ( arrayDeclaratorList )? ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:254:3: ^( IDENT ( arrayDeclaratorList )? )
            {
                match(input, IDENT, FOLLOW_IDENT_in_variableDeclaratorId702);
                if (state.failed) return;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:254:11: ( arrayDeclaratorList )?
                    int alt42 = 2;
                    int LA42_0 = input.LA(1);

                    if ((LA42_0 == ARRAY_DECLARATOR_LIST)) {
                        alt42 = 1;
                    }
                    switch (alt42) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: arrayDeclaratorList
                        {
                            pushFollow(FOLLOW_arrayDeclaratorList_in_variableDeclaratorId704);
                            arrayDeclaratorList();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }


                    match(input, Token.UP, null);
                    if (state.failed) return;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 18, variableDeclaratorId_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "variableDeclaratorId"


    // $ANTLR start "variableInitializer"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:257:1: variableInitializer : ( arrayInitializer |
    // expression );
    public final void variableInitializer() throws RecognitionException {
        int variableInitializer_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 19)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:258:3: ( arrayInitializer | expression )
            int alt43 = 2;
            int LA43_0 = input.LA(1);

            if ((LA43_0 == ARRAY_INITIALIZER)) {
                alt43 = 1;
            } else if ((LA43_0 == EXPR)) {
                alt43 = 2;
            } else {
                if (state.backtracking > 0) {
                    state.failed = true;
                    return;
                }
                NoViableAltException nvae =
                        new NoViableAltException("", 43, 0, input);

                throw nvae;
            }
            switch (alt43) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:259:3: arrayInitializer
                {
                    pushFollow(FOLLOW_arrayInitializer_in_variableInitializer721);
                    arrayInitializer();

                    state._fsp--;
                    if (state.failed) return;

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:260:5: expression
                {
                    pushFollow(FOLLOW_expression_in_variableInitializer727);
                    expression();

                    state._fsp--;
                    if (state.failed) return;

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 19, variableInitializer_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "variableInitializer"


    // $ANTLR start "arrayDeclarator"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:263:1: arrayDeclarator : LBRACK RBRACK ;
    public final void arrayDeclarator() throws RecognitionException {
        int arrayDeclarator_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 20)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:264:3: ( LBRACK RBRACK )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:265:3: LBRACK RBRACK
            {
                match(input, LBRACK, FOLLOW_LBRACK_in_arrayDeclarator742);
                if (state.failed) return;
                match(input, RBRACK, FOLLOW_RBRACK_in_arrayDeclarator744);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 20, arrayDeclarator_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "arrayDeclarator"


    // $ANTLR start "arrayDeclaratorList"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:268:1: arrayDeclaratorList : ^( ARRAY_DECLARATOR_LIST (
    // ARRAY_DECLARATOR )* ) ;
    public final void arrayDeclaratorList() throws RecognitionException {
        int arrayDeclaratorList_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 21)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:269:3: ( ^( ARRAY_DECLARATOR_LIST (
            // ARRAY_DECLARATOR )* ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:270:3: ^( ARRAY_DECLARATOR_LIST (
            // ARRAY_DECLARATOR )* )
            {
                match(input, ARRAY_DECLARATOR_LIST, FOLLOW_ARRAY_DECLARATOR_LIST_in_arrayDeclaratorList760);
                if (state.failed) return;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:270:27: ( ARRAY_DECLARATOR )*
                    loop44:
                    do {
                        int alt44 = 2;
                        int LA44_0 = input.LA(1);

                        if ((LA44_0 == ARRAY_DECLARATOR)) {
                            alt44 = 1;
                        }


                        switch (alt44) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: ARRAY_DECLARATOR
                            {
                                match(input, ARRAY_DECLARATOR, FOLLOW_ARRAY_DECLARATOR_in_arrayDeclaratorList762);
                                if (state.failed) return;

                            }
                            break;

                            default:
                                break loop44;
                        }
                    } while (true);


                    match(input, Token.UP, null);
                    if (state.failed) return;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 21, arrayDeclaratorList_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "arrayDeclaratorList"


    // $ANTLR start "arrayInitializer"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:273:1: arrayInitializer : ^( ARRAY_INITIALIZER (
    // variableInitializer )* ) ;
    public final void arrayInitializer() throws RecognitionException {
        int arrayInitializer_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 22)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:274:3: ( ^( ARRAY_INITIALIZER (
            // variableInitializer )* ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:275:3: ^( ARRAY_INITIALIZER (
            // variableInitializer )* )
            {
                match(input, ARRAY_INITIALIZER, FOLLOW_ARRAY_INITIALIZER_in_arrayInitializer780);
                if (state.failed) return;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:275:23: ( variableInitializer )*
                    loop45:
                    do {
                        int alt45 = 2;
                        int LA45_0 = input.LA(1);

                        if ((LA45_0 == ARRAY_INITIALIZER || LA45_0 == EXPR)) {
                            alt45 = 1;
                        }


                        switch (alt45) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: variableInitializer
                            {
                                pushFollow(FOLLOW_variableInitializer_in_arrayInitializer782);
                                variableInitializer();

                                state._fsp--;
                                if (state.failed) return;

                            }
                            break;

                            default:
                                break loop45;
                        }
                    } while (true);


                    match(input, Token.UP, null);
                    if (state.failed) return;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 22, arrayInitializer_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "arrayInitializer"


    // $ANTLR start "throwsClause"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:278:1: throwsClause : ^( THROWS_CLAUSE (
    // qualifiedIdentifier )+ ) ;
    public final void throwsClause() throws RecognitionException {
        int throwsClause_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 23)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:279:3: ( ^( THROWS_CLAUSE ( qualifiedIdentifier
            // )+ ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:280:3: ^( THROWS_CLAUSE ( qualifiedIdentifier )+ )
            {
                match(input, THROWS_CLAUSE, FOLLOW_THROWS_CLAUSE_in_throwsClause800);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:280:19: ( qualifiedIdentifier )+
                int cnt46 = 0;
                loop46:
                do {
                    int alt46 = 2;
                    int LA46_0 = input.LA(1);

                    if ((LA46_0 == DOT || LA46_0 == IDENT)) {
                        alt46 = 1;
                    }


                    switch (alt46) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: qualifiedIdentifier
                        {
                            pushFollow(FOLLOW_qualifiedIdentifier_in_throwsClause802);
                            qualifiedIdentifier();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                        default:
                            if (cnt46 >= 1) break loop46;
                            if (state.backtracking > 0) {
                                state.failed = true;
                                return;
                            }
                            EarlyExitException eee =
                                    new EarlyExitException(46, input);
                            throw eee;
                    }
                    cnt46++;
                } while (true);


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 23, throwsClause_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "throwsClause"


    // $ANTLR start "modifierList"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:283:1: modifierList : ^( MODIFIER_LIST ( modifier )* ) ;
    public final void modifierList() throws RecognitionException {
        int modifierList_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 24)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:284:3: ( ^( MODIFIER_LIST ( modifier )* ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:285:3: ^( MODIFIER_LIST ( modifier )* )
            {
                match(input, MODIFIER_LIST, FOLLOW_MODIFIER_LIST_in_modifierList820);
                if (state.failed) return;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:285:19: ( modifier )*
                    loop47:
                    do {
                        int alt47 = 2;
                        int LA47_0 = input.LA(1);

                        if ((LA47_0 == AT || LA47_0 == ABSTRACT || LA47_0 == FINAL || LA47_0 == NATIVE ||
                             (LA47_0 >= PRIVATE && LA47_0 <= PUBLIC) || (LA47_0 >= STATIC && LA47_0 <= STRICTFP) ||
                             LA47_0 == SYNCHRONIZED || LA47_0 == TRANSIENT || LA47_0 == VOLATILE)) {
                            alt47 = 1;
                        }


                        switch (alt47) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: modifier
                            {
                                pushFollow(FOLLOW_modifier_in_modifierList822);
                                modifier();

                                state._fsp--;
                                if (state.failed) return;

                            }
                            break;

                            default:
                                break loop47;
                        }
                    } while (true);


                    match(input, Token.UP, null);
                    if (state.failed) return;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 24, modifierList_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "modifierList"


    // $ANTLR start "modifier"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:288:1: modifier : ( PUBLIC | PROTECTED | PRIVATE |
    // STATIC | ABSTRACT | NATIVE | SYNCHRONIZED | TRANSIENT | VOLATILE | STRICTFP | localModifier );
    public final void modifier() throws RecognitionException {
        int modifier_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 25)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:289:3: ( PUBLIC | PROTECTED | PRIVATE | STATIC |
            // ABSTRACT | NATIVE | SYNCHRONIZED | TRANSIENT | VOLATILE | STRICTFP | localModifier )
            int alt48 = 11;
            switch (input.LA(1)) {
                case PUBLIC: {
                    alt48 = 1;
                }
                break;
                case PROTECTED: {
                    alt48 = 2;
                }
                break;
                case PRIVATE: {
                    alt48 = 3;
                }
                break;
                case STATIC: {
                    alt48 = 4;
                }
                break;
                case ABSTRACT: {
                    alt48 = 5;
                }
                break;
                case NATIVE: {
                    alt48 = 6;
                }
                break;
                case SYNCHRONIZED: {
                    alt48 = 7;
                }
                break;
                case TRANSIENT: {
                    alt48 = 8;
                }
                break;
                case VOLATILE: {
                    alt48 = 9;
                }
                break;
                case STRICTFP: {
                    alt48 = 10;
                }
                break;
                case AT:
                case FINAL: {
                    alt48 = 11;
                }
                break;
                default:
                    if (state.backtracking > 0) {
                        state.failed = true;
                        return;
                    }
                    NoViableAltException nvae =
                            new NoViableAltException("", 48, 0, input);

                    throw nvae;
            }

            switch (alt48) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:290:3: PUBLIC
                {
                    match(input, PUBLIC, FOLLOW_PUBLIC_in_modifier839);
                    if (state.failed) return;

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:291:5: PROTECTED
                {
                    match(input, PROTECTED, FOLLOW_PROTECTED_in_modifier845);
                    if (state.failed) return;

                }
                break;
                case 3:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:292:5: PRIVATE
                {
                    match(input, PRIVATE, FOLLOW_PRIVATE_in_modifier851);
                    if (state.failed) return;

                }
                break;
                case 4:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:293:5: STATIC
                {
                    match(input, STATIC, FOLLOW_STATIC_in_modifier857);
                    if (state.failed) return;

                }
                break;
                case 5:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:294:5: ABSTRACT
                {
                    match(input, ABSTRACT, FOLLOW_ABSTRACT_in_modifier863);
                    if (state.failed) return;

                }
                break;
                case 6:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:295:5: NATIVE
                {
                    match(input, NATIVE, FOLLOW_NATIVE_in_modifier869);
                    if (state.failed) return;

                }
                break;
                case 7:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:296:5: SYNCHRONIZED
                {
                    match(input, SYNCHRONIZED, FOLLOW_SYNCHRONIZED_in_modifier875);
                    if (state.failed) return;

                }
                break;
                case 8:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:297:5: TRANSIENT
                {
                    match(input, TRANSIENT, FOLLOW_TRANSIENT_in_modifier881);
                    if (state.failed) return;

                }
                break;
                case 9:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:298:5: VOLATILE
                {
                    match(input, VOLATILE, FOLLOW_VOLATILE_in_modifier887);
                    if (state.failed) return;

                }
                break;
                case 10:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:299:5: STRICTFP
                {
                    match(input, STRICTFP, FOLLOW_STRICTFP_in_modifier893);
                    if (state.failed) return;

                }
                break;
                case 11:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:300:5: localModifier
                {
                    pushFollow(FOLLOW_localModifier_in_modifier899);
                    localModifier();

                    state._fsp--;
                    if (state.failed) return;

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 25, modifier_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "modifier"


    // $ANTLR start "localModifierList"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:303:1: localModifierList : ^( LOCAL_MODIFIER_LIST (
    // localModifier )* ) ;
    public final void localModifierList() throws RecognitionException {
        int localModifierList_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 26)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:304:3: ( ^( LOCAL_MODIFIER_LIST ( localModifier
            // )* ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:305:3: ^( LOCAL_MODIFIER_LIST ( localModifier )* )
            {
                match(input, LOCAL_MODIFIER_LIST, FOLLOW_LOCAL_MODIFIER_LIST_in_localModifierList915);
                if (state.failed) return;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:305:25: ( localModifier )*
                    loop49:
                    do {
                        int alt49 = 2;
                        int LA49_0 = input.LA(1);

                        if ((LA49_0 == AT || LA49_0 == FINAL)) {
                            alt49 = 1;
                        }


                        switch (alt49) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: localModifier
                            {
                                pushFollow(FOLLOW_localModifier_in_localModifierList917);
                                localModifier();

                                state._fsp--;
                                if (state.failed) return;

                            }
                            break;

                            default:
                                break loop49;
                        }
                    } while (true);


                    match(input, Token.UP, null);
                    if (state.failed) return;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 26, localModifierList_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "localModifierList"


    // $ANTLR start "localModifier"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:308:1: localModifier : ( FINAL | annotation );
    public final void localModifier() throws RecognitionException {
        int localModifier_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 27)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:309:3: ( FINAL | annotation )
            int alt50 = 2;
            int LA50_0 = input.LA(1);

            if ((LA50_0 == FINAL)) {
                alt50 = 1;
            } else if ((LA50_0 == AT)) {
                alt50 = 2;
            } else {
                if (state.backtracking > 0) {
                    state.failed = true;
                    return;
                }
                NoViableAltException nvae =
                        new NoViableAltException("", 50, 0, input);

                throw nvae;
            }
            switch (alt50) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:310:3: FINAL
                {
                    match(input, FINAL, FOLLOW_FINAL_in_localModifier934);
                    if (state.failed) return;

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:311:5: annotation
                {
                    pushFollow(FOLLOW_annotation_in_localModifier940);
                    annotation();

                    state._fsp--;
                    if (state.failed) return;

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 27, localModifier_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "localModifier"


    // $ANTLR start "type"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:314:1: type : ^( TYPE ( primitiveType |
    // qualifiedTypeIdent ) ( arrayDeclaratorList )? ) ;
    public final void type() throws RecognitionException {
        int type_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 28)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:315:3: ( ^( TYPE ( primitiveType |
            // qualifiedTypeIdent ) ( arrayDeclaratorList )? ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:316:3: ^( TYPE ( primitiveType |
            // qualifiedTypeIdent ) ( arrayDeclaratorList )? )
            {
                match(input, TYPE, FOLLOW_TYPE_in_type961);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:318:5: ( primitiveType | qualifiedTypeIdent )
                int alt51 = 2;
                int LA51_0 = input.LA(1);

                if ((LA51_0 == BOOLEAN || LA51_0 == BYTE || LA51_0 == CHAR || LA51_0 == DOUBLE || LA51_0 == FLOAT ||
                     (LA51_0 >= INT && LA51_0 <= LONG) || LA51_0 == SHORT)) {
                    alt51 = 1;
                } else if ((LA51_0 == QUALIFIED_TYPE_IDENT)) {
                    alt51 = 2;
                } else {
                    if (state.backtracking > 0) {
                        state.failed = true;
                        return;
                    }
                    NoViableAltException nvae =
                            new NoViableAltException("", 51, 0, input);

                    throw nvae;
                }
                switch (alt51) {
                    case 1:
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:319:7: primitiveType
                    {
                        pushFollow(FOLLOW_primitiveType_in_type975);
                        primitiveType();

                        state._fsp--;
                        if (state.failed) return;

                    }
                    break;
                    case 2:
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:320:9: qualifiedTypeIdent
                    {
                        pushFollow(FOLLOW_qualifiedTypeIdent_in_type985);
                        qualifiedTypeIdent();

                        state._fsp--;
                        if (state.failed) return;

                    }
                    break;

                }

                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:322:5: ( arrayDeclaratorList )?
                int alt52 = 2;
                int LA52_0 = input.LA(1);

                if ((LA52_0 == ARRAY_DECLARATOR_LIST)) {
                    alt52 = 1;
                }
                switch (alt52) {
                    case 1:
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: arrayDeclaratorList
                    {
                        pushFollow(FOLLOW_arrayDeclaratorList_in_type997);
                        arrayDeclaratorList();

                        state._fsp--;
                        if (state.failed) return;

                    }
                    break;

                }


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 28, type_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "type"


    // $ANTLR start "qualifiedTypeIdent"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:326:1: qualifiedTypeIdent : ^( QUALIFIED_TYPE_IDENT (
    // typeIdent )+ ) ;
    public final void qualifiedTypeIdent() throws RecognitionException {
        int qualifiedTypeIdent_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 29)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:327:3: ( ^( QUALIFIED_TYPE_IDENT ( typeIdent )+ ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:328:3: ^( QUALIFIED_TYPE_IDENT ( typeIdent )+ )
            {
                match(input, QUALIFIED_TYPE_IDENT, FOLLOW_QUALIFIED_TYPE_IDENT_in_qualifiedTypeIdent1019);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:328:26: ( typeIdent )+
                int cnt53 = 0;
                loop53:
                do {
                    int alt53 = 2;
                    int LA53_0 = input.LA(1);

                    if ((LA53_0 == IDENT)) {
                        alt53 = 1;
                    }


                    switch (alt53) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: typeIdent
                        {
                            pushFollow(FOLLOW_typeIdent_in_qualifiedTypeIdent1021);
                            typeIdent();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                        default:
                            if (cnt53 >= 1) break loop53;
                            if (state.backtracking > 0) {
                                state.failed = true;
                                return;
                            }
                            EarlyExitException eee =
                                    new EarlyExitException(53, input);
                            throw eee;
                    }
                    cnt53++;
                } while (true);


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 29, qualifiedTypeIdent_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "qualifiedTypeIdent"


    // $ANTLR start "typeIdent"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:331:1: typeIdent : ^( IDENT ( genericTypeArgumentList )?
    // ) ;
    public final void typeIdent() throws RecognitionException {
        int typeIdent_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 30)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:332:3: ( ^( IDENT ( genericTypeArgumentList )? ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:333:3: ^( IDENT ( genericTypeArgumentList )? )
            {
                match(input, IDENT, FOLLOW_IDENT_in_typeIdent1039);
                if (state.failed) return;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:333:11: ( genericTypeArgumentList )?
                    int alt54 = 2;
                    int LA54_0 = input.LA(1);

                    if ((LA54_0 == GENERIC_TYPE_ARG_LIST)) {
                        alt54 = 1;
                    }
                    switch (alt54) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: genericTypeArgumentList
                        {
                            pushFollow(FOLLOW_genericTypeArgumentList_in_typeIdent1041);
                            genericTypeArgumentList();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }


                    match(input, Token.UP, null);
                    if (state.failed) return;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 30, typeIdent_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "typeIdent"


    // $ANTLR start "primitiveType"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:336:1: primitiveType : ( BOOLEAN | CHAR | BYTE | SHORT |
    // INT | LONG | FLOAT | DOUBLE );
    public final void primitiveType() throws RecognitionException {
        int primitiveType_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 31)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:337:3: ( BOOLEAN | CHAR | BYTE | SHORT | INT |
            // LONG | FLOAT | DOUBLE )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:
            {
                if (input.LA(1) == BOOLEAN || input.LA(1) == BYTE || input.LA(1) == CHAR || input.LA(1) == DOUBLE || input.LA(1) == FLOAT ||
                    (input.LA(1) >= INT && input.LA(1) <= LONG) || input.LA(1) == SHORT) {
                    input.consume();
                    state.errorRecovery = false;
                    state.failed = false;
                } else {
                    if (state.backtracking > 0) {
                        state.failed = true;
                        return;
                    }
                    MismatchedSetException mse = new MismatchedSetException(null, input);
                    throw mse;
                }


            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 31, primitiveType_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "primitiveType"


    // $ANTLR start "genericTypeArgumentList"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:348:1: genericTypeArgumentList : ^(
    // GENERIC_TYPE_ARG_LIST ( genericTypeArgument )+ ) ;
    public final void genericTypeArgumentList() throws RecognitionException {
        int genericTypeArgumentList_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 32)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:349:3: ( ^( GENERIC_TYPE_ARG_LIST (
            // genericTypeArgument )+ ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:350:3: ^( GENERIC_TYPE_ARG_LIST (
            // genericTypeArgument )+ )
            {
                match(input, GENERIC_TYPE_ARG_LIST, FOLLOW_GENERIC_TYPE_ARG_LIST_in_genericTypeArgumentList1116);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:350:27: ( genericTypeArgument )+
                int cnt55 = 0;
                loop55:
                do {
                    int alt55 = 2;
                    int LA55_0 = input.LA(1);

                    if ((LA55_0 == QUESTION || LA55_0 == TYPE)) {
                        alt55 = 1;
                    }


                    switch (alt55) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: genericTypeArgument
                        {
                            pushFollow(FOLLOW_genericTypeArgument_in_genericTypeArgumentList1118);
                            genericTypeArgument();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                        default:
                            if (cnt55 >= 1) break loop55;
                            if (state.backtracking > 0) {
                                state.failed = true;
                                return;
                            }
                            EarlyExitException eee =
                                    new EarlyExitException(55, input);
                            throw eee;
                    }
                    cnt55++;
                } while (true);


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 32, genericTypeArgumentList_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "genericTypeArgumentList"


    // $ANTLR start "genericTypeArgument"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:353:1: genericTypeArgument : ( type | ^( QUESTION (
    // genericWildcardBoundType )? ) );
    public final void genericTypeArgument() throws RecognitionException {
        int genericTypeArgument_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 33)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:354:3: ( type | ^( QUESTION (
            // genericWildcardBoundType )? ) )
            int alt57 = 2;
            int LA57_0 = input.LA(1);

            if ((LA57_0 == TYPE)) {
                alt57 = 1;
            } else if ((LA57_0 == QUESTION)) {
                alt57 = 2;
            } else {
                if (state.backtracking > 0) {
                    state.failed = true;
                    return;
                }
                NoViableAltException nvae =
                        new NoViableAltException("", 57, 0, input);

                throw nvae;
            }
            switch (alt57) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:355:3: type
                {
                    pushFollow(FOLLOW_type_in_genericTypeArgument1135);
                    type();

                    state._fsp--;
                    if (state.failed) return;

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:357:3: ^( QUESTION (
                    // genericWildcardBoundType )? )
                {
                    match(input, QUESTION, FOLLOW_QUESTION_in_genericTypeArgument1144);
                    if (state.failed) return;

                    if (input.LA(1) == Token.DOWN) {
                        match(input, Token.DOWN, null);
                        if (state.failed) return;
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:357:14: ( genericWildcardBoundType )?
                        int alt56 = 2;
                        int LA56_0 = input.LA(1);

                        if ((LA56_0 == EXTENDS || LA56_0 == SUPER)) {
                            alt56 = 1;
                        }
                        switch (alt56) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: genericWildcardBoundType
                            {
                                pushFollow(FOLLOW_genericWildcardBoundType_in_genericTypeArgument1146);
                                genericWildcardBoundType();

                                state._fsp--;
                                if (state.failed) return;

                            }
                            break;

                        }


                        match(input, Token.UP, null);
                        if (state.failed) return;
                    }

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 33, genericTypeArgument_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "genericTypeArgument"


    // $ANTLR start "genericWildcardBoundType"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:360:1: genericWildcardBoundType : ( ^( EXTENDS type ) |
    // ^( SUPER type ) );
    public final void genericWildcardBoundType() throws RecognitionException {
        int genericWildcardBoundType_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 34)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:361:3: ( ^( EXTENDS type ) | ^( SUPER type ) )
            int alt58 = 2;
            int LA58_0 = input.LA(1);

            if ((LA58_0 == EXTENDS)) {
                alt58 = 1;
            } else if ((LA58_0 == SUPER)) {
                alt58 = 2;
            } else {
                if (state.backtracking > 0) {
                    state.failed = true;
                    return;
                }
                NoViableAltException nvae =
                        new NoViableAltException("", 58, 0, input);

                throw nvae;
            }
            switch (alt58) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:362:3: ^( EXTENDS type )
                {
                    match(input, EXTENDS, FOLLOW_EXTENDS_in_genericWildcardBoundType1164);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_type_in_genericWildcardBoundType1166);
                    type();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:364:3: ^( SUPER type )
                {
                    match(input, SUPER, FOLLOW_SUPER_in_genericWildcardBoundType1176);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_type_in_genericWildcardBoundType1178);
                    type();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 34, genericWildcardBoundType_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "genericWildcardBoundType"


    // $ANTLR start "formalParameterList"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:367:1: formalParameterList : ^( FORMAL_PARAM_LIST (
    // formalParameterStandardDecl )* ( formalParameterVarargDecl )? ) ;
    public final void formalParameterList() throws RecognitionException {
        int formalParameterList_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 35)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:368:3: ( ^( FORMAL_PARAM_LIST (
            // formalParameterStandardDecl )* ( formalParameterVarargDecl )? ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:369:3: ^( FORMAL_PARAM_LIST (
            // formalParameterStandardDecl )* ( formalParameterVarargDecl )? )
            {
                match(input, FORMAL_PARAM_LIST, FOLLOW_FORMAL_PARAM_LIST_in_formalParameterList1195);
                if (state.failed) return;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:369:23: ( formalParameterStandardDecl )*
                    loop59:
                    do {
                        int alt59 = 2;
                        int LA59_0 = input.LA(1);

                        if ((LA59_0 == FORMAL_PARAM_STD_DECL)) {
                            alt59 = 1;
                        }


                        switch (alt59) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0:
                                // formalParameterStandardDecl
                            {
                                pushFollow(FOLLOW_formalParameterStandardDecl_in_formalParameterList1197);
                                formalParameterStandardDecl();

                                state._fsp--;
                                if (state.failed) return;

                            }
                            break;

                            default:
                                break loop59;
                        }
                    } while (true);

                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:369:52: ( formalParameterVarargDecl )?
                    int alt60 = 2;
                    int LA60_0 = input.LA(1);

                    if ((LA60_0 == FORMAL_PARAM_VARARG_DECL)) {
                        alt60 = 1;
                    }
                    switch (alt60) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: formalParameterVarargDecl
                        {
                            pushFollow(FOLLOW_formalParameterVarargDecl_in_formalParameterList1200);
                            formalParameterVarargDecl();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }


                    match(input, Token.UP, null);
                    if (state.failed) return;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 35, formalParameterList_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "formalParameterList"


    // $ANTLR start "formalParameterStandardDecl"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:372:1: formalParameterStandardDecl : ^(
    // FORMAL_PARAM_STD_DECL localModifierList type variableDeclaratorId ) ;
    public final void formalParameterStandardDecl() throws RecognitionException {
        int formalParameterStandardDecl_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 36)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:373:3: ( ^( FORMAL_PARAM_STD_DECL
            // localModifierList type variableDeclaratorId ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:374:3: ^( FORMAL_PARAM_STD_DECL
            // localModifierList type variableDeclaratorId )
            {
                match(input, FORMAL_PARAM_STD_DECL, FOLLOW_FORMAL_PARAM_STD_DECL_in_formalParameterStandardDecl1218);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                pushFollow(FOLLOW_localModifierList_in_formalParameterStandardDecl1220);
                localModifierList();

                state._fsp--;
                if (state.failed) return;
                pushFollow(FOLLOW_type_in_formalParameterStandardDecl1222);
                type();

                state._fsp--;
                if (state.failed) return;
                pushFollow(FOLLOW_variableDeclaratorId_in_formalParameterStandardDecl1224);
                variableDeclaratorId();

                state._fsp--;
                if (state.failed) return;

                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 36, formalParameterStandardDecl_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "formalParameterStandardDecl"


    // $ANTLR start "formalParameterVarargDecl"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:377:1: formalParameterVarargDecl : ^(
    // FORMAL_PARAM_VARARG_DECL localModifierList type variableDeclaratorId ) ;
    public final void formalParameterVarargDecl() throws RecognitionException {
        int formalParameterVarargDecl_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 37)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:378:3: ( ^( FORMAL_PARAM_VARARG_DECL
            // localModifierList type variableDeclaratorId ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:379:3: ^( FORMAL_PARAM_VARARG_DECL
            // localModifierList type variableDeclaratorId )
            {
                match(input, FORMAL_PARAM_VARARG_DECL, FOLLOW_FORMAL_PARAM_VARARG_DECL_in_formalParameterVarargDecl1241);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                pushFollow(FOLLOW_localModifierList_in_formalParameterVarargDecl1243);
                localModifierList();

                state._fsp--;
                if (state.failed) return;
                pushFollow(FOLLOW_type_in_formalParameterVarargDecl1245);
                type();

                state._fsp--;
                if (state.failed) return;
                pushFollow(FOLLOW_variableDeclaratorId_in_formalParameterVarargDecl1247);
                variableDeclaratorId();

                state._fsp--;
                if (state.failed) return;

                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 37, formalParameterVarargDecl_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "formalParameterVarargDecl"


    // $ANTLR start "qualifiedIdentifier"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:382:1: qualifiedIdentifier : ( IDENT | ^( DOT
    // qualifiedIdentifier IDENT ) );
    public final void qualifiedIdentifier() throws RecognitionException {
        int qualifiedIdentifier_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 38)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:383:3: ( IDENT | ^( DOT qualifiedIdentifier
            // IDENT ) )
            int alt61 = 2;
            int LA61_0 = input.LA(1);

            if ((LA61_0 == IDENT)) {
                alt61 = 1;
            } else if ((LA61_0 == DOT)) {
                alt61 = 2;
            } else {
                if (state.backtracking > 0) {
                    state.failed = true;
                    return;
                }
                NoViableAltException nvae =
                        new NoViableAltException("", 61, 0, input);

                throw nvae;
            }
            switch (alt61) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:384:3: IDENT
                {
                    match(input, IDENT, FOLLOW_IDENT_in_qualifiedIdentifier1263);
                    if (state.failed) return;

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:386:3: ^( DOT qualifiedIdentifier IDENT )
                {
                    match(input, DOT, FOLLOW_DOT_in_qualifiedIdentifier1272);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_qualifiedIdentifier_in_qualifiedIdentifier1274);
                    qualifiedIdentifier();

                    state._fsp--;
                    if (state.failed) return;
                    match(input, IDENT, FOLLOW_IDENT_in_qualifiedIdentifier1276);
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 38, qualifiedIdentifier_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "qualifiedIdentifier"


    // $ANTLR start "annotationList"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:391:1: annotationList : ^( ANNOTATION_LIST ( annotation
    // )* ) ;
    public final void annotationList() throws RecognitionException {
        int annotationList_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 39)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:392:3: ( ^( ANNOTATION_LIST ( annotation )* ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:393:3: ^( ANNOTATION_LIST ( annotation )* )
            {
                match(input, ANNOTATION_LIST, FOLLOW_ANNOTATION_LIST_in_annotationList1295);
                if (state.failed) return;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:393:21: ( annotation )*
                    loop62:
                    do {
                        int alt62 = 2;
                        int LA62_0 = input.LA(1);

                        if ((LA62_0 == AT)) {
                            alt62 = 1;
                        }


                        switch (alt62) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: annotation
                            {
                                pushFollow(FOLLOW_annotation_in_annotationList1297);
                                annotation();

                                state._fsp--;
                                if (state.failed) return;

                            }
                            break;

                            default:
                                break loop62;
                        }
                    } while (true);


                    match(input, Token.UP, null);
                    if (state.failed) return;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 39, annotationList_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "annotationList"


    // $ANTLR start "annotation"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:396:1: annotation : ^( AT qualifiedIdentifier (
    // annotationInit )? ) ;
    public final void annotation() throws RecognitionException {
        int annotation_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 40)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:397:3: ( ^( AT qualifiedIdentifier (
            // annotationInit )? ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:398:3: ^( AT qualifiedIdentifier (
            // annotationInit )? )
            {
                match(input, AT, FOLLOW_AT_in_annotation1315);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                pushFollow(FOLLOW_qualifiedIdentifier_in_annotation1317);
                qualifiedIdentifier();

                state._fsp--;
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:398:28: ( annotationInit )?
                int alt63 = 2;
                int LA63_0 = input.LA(1);

                if ((LA63_0 == ANNOTATION_INIT_BLOCK)) {
                    alt63 = 1;
                }
                switch (alt63) {
                    case 1:
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: annotationInit
                    {
                        pushFollow(FOLLOW_annotationInit_in_annotation1319);
                        annotationInit();

                        state._fsp--;
                        if (state.failed) return;

                    }
                    break;

                }


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 40, annotation_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "annotation"


    // $ANTLR start "annotationInit"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:401:1: annotationInit : ^( ANNOTATION_INIT_BLOCK
    // annotationInitializers ) ;
    public final void annotationInit() throws RecognitionException {
        int annotationInit_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 41)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:402:3: ( ^( ANNOTATION_INIT_BLOCK
            // annotationInitializers ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:403:3: ^( ANNOTATION_INIT_BLOCK
            // annotationInitializers )
            {
                match(input, ANNOTATION_INIT_BLOCK, FOLLOW_ANNOTATION_INIT_BLOCK_in_annotationInit1337);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                pushFollow(FOLLOW_annotationInitializers_in_annotationInit1339);
                annotationInitializers();

                state._fsp--;
                if (state.failed) return;

                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 41, annotationInit_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "annotationInit"


    // $ANTLR start "annotationInitializers"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:406:1: annotationInitializers : ( ^(
    // ANNOTATION_INIT_KEY_LIST ( annotationInitializer )+ ) | ^( ANNOTATION_INIT_DEFAULT_KEY annotationElementValue ) );
    public final void annotationInitializers() throws RecognitionException {
        int annotationInitializers_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 42)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:407:3: ( ^( ANNOTATION_INIT_KEY_LIST (
            // annotationInitializer )+ ) | ^( ANNOTATION_INIT_DEFAULT_KEY annotationElementValue ) )
            int alt65 = 2;
            int LA65_0 = input.LA(1);

            if ((LA65_0 == ANNOTATION_INIT_KEY_LIST)) {
                alt65 = 1;
            } else if ((LA65_0 == ANNOTATION_INIT_DEFAULT_KEY)) {
                alt65 = 2;
            } else {
                if (state.backtracking > 0) {
                    state.failed = true;
                    return;
                }
                NoViableAltException nvae =
                        new NoViableAltException("", 65, 0, input);

                throw nvae;
            }
            switch (alt65) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:408:3: ^( ANNOTATION_INIT_KEY_LIST (
                    // annotationInitializer )+ )
                {
                    match(input, ANNOTATION_INIT_KEY_LIST, FOLLOW_ANNOTATION_INIT_KEY_LIST_in_annotationInitializers1356);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:408:30: ( annotationInitializer )+
                    int cnt64 = 0;
                    loop64:
                    do {
                        int alt64 = 2;
                        int LA64_0 = input.LA(1);

                        if ((LA64_0 == IDENT)) {
                            alt64 = 1;
                        }


                        switch (alt64) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: annotationInitializer
                            {
                                pushFollow(FOLLOW_annotationInitializer_in_annotationInitializers1358);
                                annotationInitializer();

                                state._fsp--;
                                if (state.failed) return;

                            }
                            break;

                            default:
                                if (cnt64 >= 1) break loop64;
                                if (state.backtracking > 0) {
                                    state.failed = true;
                                    return;
                                }
                                EarlyExitException eee =
                                        new EarlyExitException(64, input);
                                throw eee;
                        }
                        cnt64++;
                    } while (true);


                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:410:3: ^( ANNOTATION_INIT_DEFAULT_KEY
                    // annotationElementValue )
                {
                    match(input, ANNOTATION_INIT_DEFAULT_KEY, FOLLOW_ANNOTATION_INIT_DEFAULT_KEY_in_annotationInitializers1369);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_annotationElementValue_in_annotationInitializers1371);
                    annotationElementValue();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 42, annotationInitializers_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "annotationInitializers"


    // $ANTLR start "annotationInitializer"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:413:1: annotationInitializer : ^( IDENT
    // annotationElementValue ) ;
    public final void annotationInitializer() throws RecognitionException {
        int annotationInitializer_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 43)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:414:3: ( ^( IDENT annotationElementValue ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:415:3: ^( IDENT annotationElementValue )
            {
                match(input, IDENT, FOLLOW_IDENT_in_annotationInitializer1388);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                pushFollow(FOLLOW_annotationElementValue_in_annotationInitializer1390);
                annotationElementValue();

                state._fsp--;
                if (state.failed) return;

                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 43, annotationInitializer_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "annotationInitializer"


    // $ANTLR start "annotationElementValue"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:418:1: annotationElementValue : ( ^(
    // ANNOTATION_INIT_ARRAY_ELEMENT ( annotationElementValue )* ) | annotation | expression );
    public final void annotationElementValue() throws RecognitionException {
        int annotationElementValue_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 44)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:419:3: ( ^( ANNOTATION_INIT_ARRAY_ELEMENT (
            // annotationElementValue )* ) | annotation | expression )
            int alt67 = 3;
            switch (input.LA(1)) {
                case ANNOTATION_INIT_ARRAY_ELEMENT: {
                    alt67 = 1;
                }
                break;
                case AT: {
                    alt67 = 2;
                }
                break;
                case EXPR: {
                    alt67 = 3;
                }
                break;
                default:
                    if (state.backtracking > 0) {
                        state.failed = true;
                        return;
                    }
                    NoViableAltException nvae =
                            new NoViableAltException("", 67, 0, input);

                    throw nvae;
            }

            switch (alt67) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:420:3: ^( ANNOTATION_INIT_ARRAY_ELEMENT
                    // ( annotationElementValue )* )
                {
                    match(input, ANNOTATION_INIT_ARRAY_ELEMENT, FOLLOW_ANNOTATION_INIT_ARRAY_ELEMENT_in_annotationElementValue1407);
                    if (state.failed) return;

                    if (input.LA(1) == Token.DOWN) {
                        match(input, Token.DOWN, null);
                        if (state.failed) return;
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:420:35: ( annotationElementValue )*
                        loop66:
                        do {
                            int alt66 = 2;
                            int LA66_0 = input.LA(1);

                            if ((LA66_0 == AT || LA66_0 == ANNOTATION_INIT_ARRAY_ELEMENT || LA66_0 == EXPR)) {
                                alt66 = 1;
                            }


                            switch (alt66) {
                                case 1:
                                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: annotationElementValue
                                {
                                    pushFollow(FOLLOW_annotationElementValue_in_annotationElementValue1409);
                                    annotationElementValue();

                                    state._fsp--;
                                    if (state.failed) return;

                                }
                                break;

                                default:
                                    break loop66;
                            }
                        } while (true);


                        match(input, Token.UP, null);
                        if (state.failed) return;
                    }

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:421:5: annotation
                {
                    pushFollow(FOLLOW_annotation_in_annotationElementValue1417);
                    annotation();

                    state._fsp--;
                    if (state.failed) return;

                }
                break;
                case 3:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:422:5: expression
                {
                    pushFollow(FOLLOW_expression_in_annotationElementValue1423);
                    expression();

                    state._fsp--;
                    if (state.failed) return;

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 44, annotationElementValue_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "annotationElementValue"


    // $ANTLR start "annotationTopLevelScope"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:425:1: annotationTopLevelScope : ^(
    // ANNOTATION_TOP_LEVEL_SCOPE ( annotationScopeDeclarations )* ) ;
    public final void annotationTopLevelScope() throws RecognitionException {
        int annotationTopLevelScope_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 45)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:426:3: ( ^( ANNOTATION_TOP_LEVEL_SCOPE (
            // annotationScopeDeclarations )* ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:427:3: ^( ANNOTATION_TOP_LEVEL_SCOPE (
            // annotationScopeDeclarations )* )
            {
                match(input, ANNOTATION_TOP_LEVEL_SCOPE, FOLLOW_ANNOTATION_TOP_LEVEL_SCOPE_in_annotationTopLevelScope1439);
                if (state.failed) return;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:427:32: ( annotationScopeDeclarations )*
                    loop68:
                    do {
                        int alt68 = 2;
                        int LA68_0 = input.LA(1);

                        if ((LA68_0 == AT || LA68_0 == CLASS || LA68_0 == ENUM || LA68_0 == INTERFACE || LA68_0 == ANNOTATION_METHOD_DECL ||
                             LA68_0 == VAR_DECLARATION)) {
                            alt68 = 1;
                        }


                        switch (alt68) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0:
                                // annotationScopeDeclarations
                            {
                                pushFollow(FOLLOW_annotationScopeDeclarations_in_annotationTopLevelScope1441);
                                annotationScopeDeclarations();

                                state._fsp--;
                                if (state.failed) return;

                            }
                            break;

                            default:
                                break loop68;
                        }
                    } while (true);


                    match(input, Token.UP, null);
                    if (state.failed) return;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 45, annotationTopLevelScope_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "annotationTopLevelScope"


    // $ANTLR start "annotationScopeDeclarations"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:430:1: annotationScopeDeclarations : ( ^(
    // ANNOTATION_METHOD_DECL modifierList type IDENT ( annotationDefaultValue )? ) | ^( VAR_DECLARATION modifierList type
    // variableDeclaratorList ) | typeDeclaration );
    public final void annotationScopeDeclarations() throws RecognitionException {
        int annotationScopeDeclarations_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 46)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:431:3: ( ^( ANNOTATION_METHOD_DECL modifierList
            // type IDENT ( annotationDefaultValue )? ) | ^( VAR_DECLARATION modifierList type variableDeclaratorList ) | typeDeclaration )
            int alt70 = 3;
            switch (input.LA(1)) {
                case ANNOTATION_METHOD_DECL: {
                    alt70 = 1;
                }
                break;
                case VAR_DECLARATION: {
                    alt70 = 2;
                }
                break;
                case AT:
                case CLASS:
                case ENUM:
                case INTERFACE: {
                    alt70 = 3;
                }
                break;
                default:
                    if (state.backtracking > 0) {
                        state.failed = true;
                        return;
                    }
                    NoViableAltException nvae =
                            new NoViableAltException("", 70, 0, input);

                    throw nvae;
            }

            switch (alt70) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:432:3: ^( ANNOTATION_METHOD_DECL
                    // modifierList type IDENT ( annotationDefaultValue )? )
                {
                    match(input, ANNOTATION_METHOD_DECL, FOLLOW_ANNOTATION_METHOD_DECL_in_annotationScopeDeclarations1459);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_modifierList_in_annotationScopeDeclarations1461);
                    modifierList();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_type_in_annotationScopeDeclarations1463);
                    type();

                    state._fsp--;
                    if (state.failed) return;
                    match(input, IDENT, FOLLOW_IDENT_in_annotationScopeDeclarations1465);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:432:52: ( annotationDefaultValue )?
                    int alt69 = 2;
                    int LA69_0 = input.LA(1);

                    if ((LA69_0 == DEFAULT)) {
                        alt69 = 1;
                    }
                    switch (alt69) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: annotationDefaultValue
                        {
                            pushFollow(FOLLOW_annotationDefaultValue_in_annotationScopeDeclarations1467);
                            annotationDefaultValue();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }


                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:434:3: ^( VAR_DECLARATION modifierList
                    // type variableDeclaratorList )
                {
                    match(input, VAR_DECLARATION, FOLLOW_VAR_DECLARATION_in_annotationScopeDeclarations1478);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_modifierList_in_annotationScopeDeclarations1480);
                    modifierList();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_type_in_annotationScopeDeclarations1482);
                    type();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_variableDeclaratorList_in_annotationScopeDeclarations1484);
                    variableDeclaratorList();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 3:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:435:5: typeDeclaration
                {
                    pushFollow(FOLLOW_typeDeclaration_in_annotationScopeDeclarations1491);
                    typeDeclaration();

                    state._fsp--;
                    if (state.failed) return;

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 46, annotationScopeDeclarations_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "annotationScopeDeclarations"


    // $ANTLR start "annotationDefaultValue"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:438:1: annotationDefaultValue : ^( DEFAULT
    // annotationElementValue ) ;
    public final void annotationDefaultValue() throws RecognitionException {
        int annotationDefaultValue_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 47)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:439:3: ( ^( DEFAULT annotationElementValue ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:440:3: ^( DEFAULT annotationElementValue )
            {
                match(input, DEFAULT, FOLLOW_DEFAULT_in_annotationDefaultValue1507);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                pushFollow(FOLLOW_annotationElementValue_in_annotationDefaultValue1509);
                annotationElementValue();

                state._fsp--;
                if (state.failed) return;

                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 47, annotationDefaultValue_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "annotationDefaultValue"


    // $ANTLR start "block"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:445:1: block : ^( BLOCK_SCOPE ( blockStatement )* ) ;
    public final void block() throws RecognitionException {
        int block_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 48)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:446:3: ( ^( BLOCK_SCOPE ( blockStatement )* ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:447:3: ^( BLOCK_SCOPE ( blockStatement )* )
            {
                match(input, BLOCK_SCOPE, FOLLOW_BLOCK_SCOPE_in_block1528);
                if (state.failed) return;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:447:17: ( blockStatement )*
                    loop71:
                    do {
                        int alt71 = 2;
                        int LA71_0 = input.LA(1);

                        if ((LA71_0 == AT || LA71_0 == SEMI || LA71_0 == ASSERT || LA71_0 == BREAK ||
                             (LA71_0 >= CLASS && LA71_0 <= CONTINUE) || LA71_0 == DO || LA71_0 == ENUM || (LA71_0 >= FOR && LA71_0 <= IF) ||
                             LA71_0 == INTERFACE || LA71_0 == RETURN || (LA71_0 >= SWITCH && LA71_0 <= SYNCHRONIZED) || LA71_0 == THROW ||
                             LA71_0 == TRY || LA71_0 == WHILE || LA71_0 == BLOCK_SCOPE || LA71_0 == EXPR || LA71_0 == FOR_EACH ||
                             LA71_0 == LABELED_STATEMENT || LA71_0 == VAR_DECLARATION)) {
                            alt71 = 1;
                        }


                        switch (alt71) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: blockStatement
                            {
                                pushFollow(FOLLOW_blockStatement_in_block1530);
                                blockStatement();

                                state._fsp--;
                                if (state.failed) return;

                            }
                            break;

                            default:
                                break loop71;
                        }
                    } while (true);


                    match(input, Token.UP, null);
                    if (state.failed) return;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 48, block_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "block"


    // $ANTLR start "blockStatement"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:450:1: blockStatement : ( localVariableDeclaration |
    // typeDeclaration | statement );
    public final void blockStatement() throws RecognitionException {
        int blockStatement_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 49)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:451:3: ( localVariableDeclaration |
            // typeDeclaration | statement )
            int alt72 = 3;
            switch (input.LA(1)) {
                case VAR_DECLARATION: {
                    alt72 = 1;
                }
                break;
                case AT:
                case CLASS:
                case ENUM:
                case INTERFACE: {
                    alt72 = 2;
                }
                break;
                case SEMI:
                case ASSERT:
                case BREAK:
                case CONTINUE:
                case DO:
                case FOR:
                case IF:
                case RETURN:
                case SWITCH:
                case SYNCHRONIZED:
                case THROW:
                case TRY:
                case WHILE:
                case BLOCK_SCOPE:
                case EXPR:
                case FOR_EACH:
                case LABELED_STATEMENT: {
                    alt72 = 3;
                }
                break;
                default:
                    if (state.backtracking > 0) {
                        state.failed = true;
                        return;
                    }
                    NoViableAltException nvae =
                            new NoViableAltException("", 72, 0, input);

                    throw nvae;
            }

            switch (alt72) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:452:3: localVariableDeclaration
                {
                    pushFollow(FOLLOW_localVariableDeclaration_in_blockStatement1547);
                    localVariableDeclaration();

                    state._fsp--;
                    if (state.failed) return;

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:453:5: typeDeclaration
                {
                    pushFollow(FOLLOW_typeDeclaration_in_blockStatement1553);
                    typeDeclaration();

                    state._fsp--;
                    if (state.failed) return;

                }
                break;
                case 3:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:454:5: statement
                {
                    pushFollow(FOLLOW_statement_in_blockStatement1559);
                    statement();

                    state._fsp--;
                    if (state.failed) return;

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 49, blockStatement_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "blockStatement"


    // $ANTLR start "localVariableDeclaration"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:457:1: localVariableDeclaration : ^( VAR_DECLARATION
    // localModifierList type variableDeclaratorList ) ;
    public final void localVariableDeclaration() throws RecognitionException {
        int localVariableDeclaration_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 50)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:458:3: ( ^( VAR_DECLARATION localModifierList
            // type variableDeclaratorList ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:459:3: ^( VAR_DECLARATION localModifierList type
            // variableDeclaratorList )
            {
                match(input, VAR_DECLARATION, FOLLOW_VAR_DECLARATION_in_localVariableDeclaration1575);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                pushFollow(FOLLOW_localModifierList_in_localVariableDeclaration1577);
                localModifierList();

                state._fsp--;
                if (state.failed) return;
                pushFollow(FOLLOW_type_in_localVariableDeclaration1579);
                type();

                state._fsp--;
                if (state.failed) return;
                pushFollow(FOLLOW_variableDeclaratorList_in_localVariableDeclaration1581);
                variableDeclaratorList();

                state._fsp--;
                if (state.failed) return;

                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 50, localVariableDeclaration_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "localVariableDeclaration"


    // $ANTLR start "statement"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:462:1: statement : ( block | ^( ASSERT expression (
    // expression )? ) | ^( IF parenthesizedExpression statement ( statement )? ) | ^( FOR forInit forCondition forUpdater statement ) |
    // ^( FOR_EACH localModifierList type IDENT expression statement ) | ^( WHILE parenthesizedExpression statement ) | ^( DO statement
    // parenthesizedExpression ) | ^( TRY block ( catches )? ( block )? ) | ^( SWITCH parenthesizedExpression switchBlockLabels ) | ^(
    // SYNCHRONIZED parenthesizedExpression block ) | ^( RETURN ( expression )? ) | ^( THROW expression ) | ^( BREAK ( IDENT )? ) | ^(
    // CONTINUE ( IDENT )? ) | ^( LABELED_STATEMENT IDENT statement ) | expression | SEMI );
    public final void statement() throws RecognitionException {
        int statement_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 51)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:463:3: ( block | ^( ASSERT expression (
            // expression )? ) | ^( IF parenthesizedExpression statement ( statement )? ) | ^( FOR forInit forCondition forUpdater
            // statement ) | ^( FOR_EACH localModifierList type IDENT expression statement ) | ^( WHILE parenthesizedExpression statement
            // ) | ^( DO statement parenthesizedExpression ) | ^( TRY block ( catches )? ( block )? ) | ^( SWITCH parenthesizedExpression
            // switchBlockLabels ) | ^( SYNCHRONIZED parenthesizedExpression block ) | ^( RETURN ( expression )? ) | ^( THROW expression
            // ) | ^( BREAK ( IDENT )? ) | ^( CONTINUE ( IDENT )? ) | ^( LABELED_STATEMENT IDENT statement ) | expression | SEMI )
            int alt80 = 17;
            switch (input.LA(1)) {
                case BLOCK_SCOPE: {
                    alt80 = 1;
                }
                break;
                case ASSERT: {
                    alt80 = 2;
                }
                break;
                case IF: {
                    alt80 = 3;
                }
                break;
                case FOR: {
                    alt80 = 4;
                }
                break;
                case FOR_EACH: {
                    alt80 = 5;
                }
                break;
                case WHILE: {
                    alt80 = 6;
                }
                break;
                case DO: {
                    alt80 = 7;
                }
                break;
                case TRY: {
                    alt80 = 8;
                }
                break;
                case SWITCH: {
                    alt80 = 9;
                }
                break;
                case SYNCHRONIZED: {
                    alt80 = 10;
                }
                break;
                case RETURN: {
                    alt80 = 11;
                }
                break;
                case THROW: {
                    alt80 = 12;
                }
                break;
                case BREAK: {
                    alt80 = 13;
                }
                break;
                case CONTINUE: {
                    alt80 = 14;
                }
                break;
                case LABELED_STATEMENT: {
                    alt80 = 15;
                }
                break;
                case EXPR: {
                    alt80 = 16;
                }
                break;
                case SEMI: {
                    alt80 = 17;
                }
                break;
                default:
                    if (state.backtracking > 0) {
                        state.failed = true;
                        return;
                    }
                    NoViableAltException nvae =
                            new NoViableAltException("", 80, 0, input);

                    throw nvae;
            }

            switch (alt80) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:464:3: block
                {
                    pushFollow(FOLLOW_block_in_statement1597);
                    block();

                    state._fsp--;
                    if (state.failed) return;

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:466:3: ^( ASSERT expression ( expression
                    // )? )
                {
                    match(input, ASSERT, FOLLOW_ASSERT_in_statement1606);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_expression_in_statement1608);
                    expression();

                    state._fsp--;
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:466:23: ( expression )?
                    int alt73 = 2;
                    int LA73_0 = input.LA(1);

                    if ((LA73_0 == EXPR)) {
                        alt73 = 1;
                    }
                    switch (alt73) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: expression
                        {
                            pushFollow(FOLLOW_expression_in_statement1610);
                            expression();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }


                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 3:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:468:3: ^( IF parenthesizedExpression
                    // statement ( statement )? )
                {
                    match(input, IF, FOLLOW_IF_in_statement1621);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_parenthesizedExpression_in_statement1623);
                    parenthesizedExpression();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_statement_in_statement1625);
                    statement();

                    state._fsp--;
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:468:42: ( statement )?
                    int alt74 = 2;
                    int LA74_0 = input.LA(1);

                    if ((LA74_0 == SEMI || LA74_0 == ASSERT || LA74_0 == BREAK || LA74_0 == CONTINUE || LA74_0 == DO ||
                         (LA74_0 >= FOR && LA74_0 <= IF) || LA74_0 == RETURN || (LA74_0 >= SWITCH && LA74_0 <= SYNCHRONIZED) ||
                         LA74_0 == THROW || LA74_0 == TRY || LA74_0 == WHILE || LA74_0 == BLOCK_SCOPE || LA74_0 == EXPR ||
                         LA74_0 == FOR_EACH || LA74_0 == LABELED_STATEMENT)) {
                        alt74 = 1;
                    }
                    switch (alt74) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: statement
                        {
                            pushFollow(FOLLOW_statement_in_statement1627);
                            statement();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }


                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 4:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:470:3: ^( FOR forInit forCondition
                    // forUpdater statement )
                {
                    match(input, FOR, FOLLOW_FOR_in_statement1638);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_forInit_in_statement1640);
                    forInit();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_forCondition_in_statement1642);
                    forCondition();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_forUpdater_in_statement1644);
                    forUpdater();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_statement_in_statement1646);
                    statement();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 5:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:472:3: ^( FOR_EACH localModifierList
                    // type IDENT expression statement )
                {
                    match(input, FOR_EACH, FOLLOW_FOR_EACH_in_statement1656);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_localModifierList_in_statement1658);
                    localModifierList();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_type_in_statement1660);
                    type();

                    state._fsp--;
                    if (state.failed) return;
                    match(input, IDENT, FOLLOW_IDENT_in_statement1662);
                    if (state.failed) return;
                    pushFollow(FOLLOW_expression_in_statement1664);
                    expression();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_statement_in_statement1666);
                    statement();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 6:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:474:3: ^( WHILE parenthesizedExpression
                    // statement )
                {
                    match(input, WHILE, FOLLOW_WHILE_in_statement1676);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_parenthesizedExpression_in_statement1678);
                    parenthesizedExpression();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_statement_in_statement1680);
                    statement();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 7:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:476:3: ^( DO statement
                    // parenthesizedExpression )
                {
                    match(input, DO, FOLLOW_DO_in_statement1690);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_statement_in_statement1692);
                    statement();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_parenthesizedExpression_in_statement1694);
                    parenthesizedExpression();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 8:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:478:3: ^( TRY block ( catches )? ( block
                    // )? )
                {
                    match(input, TRY, FOLLOW_TRY_in_statement1704);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_block_in_statement1706);
                    block();

                    state._fsp--;
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:478:15: ( catches )?
                    int alt75 = 2;
                    int LA75_0 = input.LA(1);

                    if ((LA75_0 == CATCH_CLAUSE_LIST)) {
                        alt75 = 1;
                    }
                    switch (alt75) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: catches
                        {
                            pushFollow(FOLLOW_catches_in_statement1708);
                            catches();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:478:24: ( block )?
                    int alt76 = 2;
                    int LA76_0 = input.LA(1);

                    if ((LA76_0 == BLOCK_SCOPE)) {
                        alt76 = 1;
                    }
                    switch (alt76) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: block
                        {
                            pushFollow(FOLLOW_block_in_statement1711);
                            block();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }


                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 9:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:480:3: ^( SWITCH parenthesizedExpression
                    // switchBlockLabels )
                {
                    match(input, SWITCH, FOLLOW_SWITCH_in_statement1723);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_parenthesizedExpression_in_statement1725);
                    parenthesizedExpression();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_switchBlockLabels_in_statement1727);
                    switchBlockLabels();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 10:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:482:3: ^( SYNCHRONIZED
                    // parenthesizedExpression block )
                {
                    match(input, SYNCHRONIZED, FOLLOW_SYNCHRONIZED_in_statement1737);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_parenthesizedExpression_in_statement1739);
                    parenthesizedExpression();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_block_in_statement1741);
                    block();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 11:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:484:3: ^( RETURN ( expression )? )
                {
                    match(input, RETURN, FOLLOW_RETURN_in_statement1751);
                    if (state.failed) return;

                    if (input.LA(1) == Token.DOWN) {
                        match(input, Token.DOWN, null);
                        if (state.failed) return;
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:484:12: ( expression )?
                        int alt77 = 2;
                        int LA77_0 = input.LA(1);

                        if ((LA77_0 == EXPR)) {
                            alt77 = 1;
                        }
                        switch (alt77) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: expression
                            {
                                pushFollow(FOLLOW_expression_in_statement1753);
                                expression();

                                state._fsp--;
                                if (state.failed) return;

                            }
                            break;

                        }


                        match(input, Token.UP, null);
                        if (state.failed) return;
                    }

                }
                break;
                case 12:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:486:3: ^( THROW expression )
                {
                    match(input, THROW, FOLLOW_THROW_in_statement1764);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    pushFollow(FOLLOW_expression_in_statement1766);
                    expression();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 13:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:488:3: ^( BREAK ( IDENT )? )
                {
                    match(input, BREAK, FOLLOW_BREAK_in_statement1776);
                    if (state.failed) return;

                    if (input.LA(1) == Token.DOWN) {
                        match(input, Token.DOWN, null);
                        if (state.failed) return;
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:488:11: ( IDENT )?
                        int alt78 = 2;
                        int LA78_0 = input.LA(1);

                        if ((LA78_0 == IDENT)) {
                            alt78 = 1;
                        }
                        switch (alt78) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: IDENT
                            {
                                match(input, IDENT, FOLLOW_IDENT_in_statement1778);
                                if (state.failed) return;

                            }
                            break;

                        }


                        match(input, Token.UP, null);
                        if (state.failed) return;
                    }

                }
                break;
                case 14:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:490:3: ^( CONTINUE ( IDENT )? )
                {
                    match(input, CONTINUE, FOLLOW_CONTINUE_in_statement1789);
                    if (state.failed) return;

                    if (input.LA(1) == Token.DOWN) {
                        match(input, Token.DOWN, null);
                        if (state.failed) return;
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:490:14: ( IDENT )?
                        int alt79 = 2;
                        int LA79_0 = input.LA(1);

                        if ((LA79_0 == IDENT)) {
                            alt79 = 1;
                        }
                        switch (alt79) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: IDENT
                            {
                                match(input, IDENT, FOLLOW_IDENT_in_statement1791);
                                if (state.failed) return;

                            }
                            break;

                        }


                        match(input, Token.UP, null);
                        if (state.failed) return;
                    }

                }
                break;
                case 15:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:492:3: ^( LABELED_STATEMENT IDENT
                    // statement )
                {
                    match(input, LABELED_STATEMENT, FOLLOW_LABELED_STATEMENT_in_statement1802);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    match(input, IDENT, FOLLOW_IDENT_in_statement1804);
                    if (state.failed) return;
                    pushFollow(FOLLOW_statement_in_statement1806);
                    statement();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 16:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:493:5: expression
                {
                    pushFollow(FOLLOW_expression_in_statement1813);
                    expression();

                    state._fsp--;
                    if (state.failed) return;

                }
                break;
                case 17:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:494:5: SEMI
                {
                    match(input, SEMI, FOLLOW_SEMI_in_statement1819);
                    if (state.failed) return;

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 51, statement_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "statement"


    // $ANTLR start "catches"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:497:1: catches : ^( CATCH_CLAUSE_LIST ( catchClause )+ ) ;
    public final void catches() throws RecognitionException {
        int catches_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 52)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:498:3: ( ^( CATCH_CLAUSE_LIST ( catchClause )+ ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:499:3: ^( CATCH_CLAUSE_LIST ( catchClause )+ )
            {
                match(input, CATCH_CLAUSE_LIST, FOLLOW_CATCH_CLAUSE_LIST_in_catches1836);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:499:23: ( catchClause )+
                int cnt81 = 0;
                loop81:
                do {
                    int alt81 = 2;
                    int LA81_0 = input.LA(1);

                    if ((LA81_0 == CATCH)) {
                        alt81 = 1;
                    }


                    switch (alt81) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: catchClause
                        {
                            pushFollow(FOLLOW_catchClause_in_catches1838);
                            catchClause();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                        default:
                            if (cnt81 >= 1) break loop81;
                            if (state.backtracking > 0) {
                                state.failed = true;
                                return;
                            }
                            EarlyExitException eee =
                                    new EarlyExitException(81, input);
                            throw eee;
                    }
                    cnt81++;
                } while (true);


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 52, catches_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "catches"


    // $ANTLR start "catchClause"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:502:1: catchClause : ^( CATCH
    // formalParameterStandardDecl block ) ;
    public final void catchClause() throws RecognitionException {
        int catchClause_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 53)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:503:3: ( ^( CATCH formalParameterStandardDecl
            // block ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:504:3: ^( CATCH formalParameterStandardDecl block )
            {
                match(input, CATCH, FOLLOW_CATCH_in_catchClause1856);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                pushFollow(FOLLOW_formalParameterStandardDecl_in_catchClause1858);
                formalParameterStandardDecl();

                state._fsp--;
                if (state.failed) return;
                pushFollow(FOLLOW_block_in_catchClause1860);
                block();

                state._fsp--;
                if (state.failed) return;

                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 53, catchClause_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "catchClause"


    // $ANTLR start "switchBlockLabels"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:507:1: switchBlockLabels : ^( SWITCH_BLOCK_LABEL_LIST (
    // switchCaseLabel )* ( switchDefaultLabel )? ( switchCaseLabel )* ) ;
    public final void switchBlockLabels() throws RecognitionException {
        int switchBlockLabels_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 54)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:508:3: ( ^( SWITCH_BLOCK_LABEL_LIST (
            // switchCaseLabel )* ( switchDefaultLabel )? ( switchCaseLabel )* ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:509:3: ^( SWITCH_BLOCK_LABEL_LIST (
            // switchCaseLabel )* ( switchDefaultLabel )? ( switchCaseLabel )* )
            {
                match(input, SWITCH_BLOCK_LABEL_LIST, FOLLOW_SWITCH_BLOCK_LABEL_LIST_in_switchBlockLabels1877);
                if (state.failed) return;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:509:29: ( switchCaseLabel )*
                    loop82:
                    do {
                        int alt82 = 2;
                        int LA82_0 = input.LA(1);

                        if ((LA82_0 == CASE)) {
                            int LA82_2 = input.LA(2);

                            if ((synpred125_JavaTreeParser())) {
                                alt82 = 1;
                            }


                        }


                        switch (alt82) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: switchCaseLabel
                            {
                                pushFollow(FOLLOW_switchCaseLabel_in_switchBlockLabels1879);
                                switchCaseLabel();

                                state._fsp--;
                                if (state.failed) return;

                            }
                            break;

                            default:
                                break loop82;
                        }
                    } while (true);

                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:509:46: ( switchDefaultLabel )?
                    int alt83 = 2;
                    int LA83_0 = input.LA(1);

                    if ((LA83_0 == DEFAULT)) {
                        alt83 = 1;
                    }
                    switch (alt83) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: switchDefaultLabel
                        {
                            pushFollow(FOLLOW_switchDefaultLabel_in_switchBlockLabels1882);
                            switchDefaultLabel();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:509:66: ( switchCaseLabel )*
                    loop84:
                    do {
                        int alt84 = 2;
                        int LA84_0 = input.LA(1);

                        if ((LA84_0 == CASE)) {
                            alt84 = 1;
                        }


                        switch (alt84) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: switchCaseLabel
                            {
                                pushFollow(FOLLOW_switchCaseLabel_in_switchBlockLabels1885);
                                switchCaseLabel();

                                state._fsp--;
                                if (state.failed) return;

                            }
                            break;

                            default:
                                break loop84;
                        }
                    } while (true);


                    match(input, Token.UP, null);
                    if (state.failed) return;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 54, switchBlockLabels_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "switchBlockLabels"


    // $ANTLR start "switchCaseLabel"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:512:1: switchCaseLabel : ^( CASE expression (
    // blockStatement )* ) ;
    public final void switchCaseLabel() throws RecognitionException {
        int switchCaseLabel_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 55)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:513:3: ( ^( CASE expression ( blockStatement )* ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:514:3: ^( CASE expression ( blockStatement )* )
            {
                match(input, CASE, FOLLOW_CASE_in_switchCaseLabel1903);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                pushFollow(FOLLOW_expression_in_switchCaseLabel1905);
                expression();

                state._fsp--;
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:514:21: ( blockStatement )*
                loop85:
                do {
                    int alt85 = 2;
                    int LA85_0 = input.LA(1);

                    if ((LA85_0 == AT || LA85_0 == SEMI || LA85_0 == ASSERT || LA85_0 == BREAK || (LA85_0 >= CLASS && LA85_0 <= CONTINUE) ||
                         LA85_0 == DO || LA85_0 == ENUM || (LA85_0 >= FOR && LA85_0 <= IF) || LA85_0 == INTERFACE || LA85_0 == RETURN ||
                         (LA85_0 >= SWITCH && LA85_0 <= SYNCHRONIZED) || LA85_0 == THROW || LA85_0 == TRY || LA85_0 == WHILE ||
                         LA85_0 == BLOCK_SCOPE || LA85_0 == EXPR || LA85_0 == FOR_EACH || LA85_0 == LABELED_STATEMENT ||
                         LA85_0 == VAR_DECLARATION)) {
                        alt85 = 1;
                    }


                    switch (alt85) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: blockStatement
                        {
                            pushFollow(FOLLOW_blockStatement_in_switchCaseLabel1907);
                            blockStatement();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                        default:
                            break loop85;
                    }
                } while (true);


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 55, switchCaseLabel_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "switchCaseLabel"


    // $ANTLR start "switchDefaultLabel"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:517:1: switchDefaultLabel : ^( DEFAULT ( blockStatement
    // )* ) ;
    public final void switchDefaultLabel() throws RecognitionException {
        int switchDefaultLabel_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 56)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:518:3: ( ^( DEFAULT ( blockStatement )* ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:519:3: ^( DEFAULT ( blockStatement )* )
            {
                match(input, DEFAULT, FOLLOW_DEFAULT_in_switchDefaultLabel1925);
                if (state.failed) return;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:519:13: ( blockStatement )*
                    loop86:
                    do {
                        int alt86 = 2;
                        int LA86_0 = input.LA(1);

                        if ((LA86_0 == AT || LA86_0 == SEMI || LA86_0 == ASSERT || LA86_0 == BREAK ||
                             (LA86_0 >= CLASS && LA86_0 <= CONTINUE) || LA86_0 == DO || LA86_0 == ENUM || (LA86_0 >= FOR && LA86_0 <= IF) ||
                             LA86_0 == INTERFACE || LA86_0 == RETURN || (LA86_0 >= SWITCH && LA86_0 <= SYNCHRONIZED) || LA86_0 == THROW ||
                             LA86_0 == TRY || LA86_0 == WHILE || LA86_0 == BLOCK_SCOPE || LA86_0 == EXPR || LA86_0 == FOR_EACH ||
                             LA86_0 == LABELED_STATEMENT || LA86_0 == VAR_DECLARATION)) {
                            alt86 = 1;
                        }


                        switch (alt86) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: blockStatement
                            {
                                pushFollow(FOLLOW_blockStatement_in_switchDefaultLabel1927);
                                blockStatement();

                                state._fsp--;
                                if (state.failed) return;

                            }
                            break;

                            default:
                                break loop86;
                        }
                    } while (true);


                    match(input, Token.UP, null);
                    if (state.failed) return;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 56, switchDefaultLabel_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "switchDefaultLabel"


    // $ANTLR start "forInit"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:522:1: forInit : ^( FOR_INIT ( localVariableDeclaration
    // | ( expression )* )? ) ;
    public final void forInit() throws RecognitionException {
        int forInit_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 57)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:523:3: ( ^( FOR_INIT ( localVariableDeclaration
            // | ( expression )* )? ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:524:3: ^( FOR_INIT ( localVariableDeclaration |
            // ( expression )* )? )
            {
                match(input, FOR_INIT, FOLLOW_FOR_INIT_in_forInit1950);
                if (state.failed) return;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:526:5: ( localVariableDeclaration | (
                    // expression )* )?
                    int alt88 = 3;
                    switch (input.LA(1)) {
                        case VAR_DECLARATION: {
                            alt88 = 1;
                        }
                        break;
                        case EXPR: {
                            alt88 = 2;
                        }
                        break;
                        case UP: {
                            int LA88_3 = input.LA(2);

                            if ((synpred132_JavaTreeParser())) {
                                alt88 = 2;
                            }
                        }
                        break;
                    }

                    switch (alt88) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:527:7: localVariableDeclaration
                        {
                            pushFollow(FOLLOW_localVariableDeclaration_in_forInit1964);
                            localVariableDeclaration();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;
                        case 2:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:528:9: ( expression )*
                        {
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:528:9: ( expression )*
                            loop87:
                            do {
                                int alt87 = 2;
                                int LA87_0 = input.LA(1);

                                if ((LA87_0 == EXPR)) {
                                    alt87 = 1;
                                }


                                switch (alt87) {
                                    case 1:
                                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: expression
                                    {
                                        pushFollow(FOLLOW_expression_in_forInit1974);
                                        expression();

                                        state._fsp--;
                                        if (state.failed) return;

                                    }
                                    break;

                                    default:
                                        break loop87;
                                }
                            } while (true);


                        }
                        break;

                    }


                    match(input, Token.UP, null);
                    if (state.failed) return;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 57, forInit_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "forInit"


    // $ANTLR start "forCondition"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:533:1: forCondition : ^( FOR_CONDITION ( expression )? ) ;
    public final void forCondition() throws RecognitionException {
        int forCondition_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 58)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:534:3: ( ^( FOR_CONDITION ( expression )? ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:535:3: ^( FOR_CONDITION ( expression )? )
            {
                match(input, FOR_CONDITION, FOLLOW_FOR_CONDITION_in_forCondition2003);
                if (state.failed) return;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:535:19: ( expression )?
                    int alt89 = 2;
                    int LA89_0 = input.LA(1);

                    if ((LA89_0 == EXPR)) {
                        alt89 = 1;
                    }
                    switch (alt89) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: expression
                        {
                            pushFollow(FOLLOW_expression_in_forCondition2005);
                            expression();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }


                    match(input, Token.UP, null);
                    if (state.failed) return;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 58, forCondition_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "forCondition"


    // $ANTLR start "forUpdater"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:538:1: forUpdater : ^( FOR_UPDATE ( expression )* ) ;
    public final void forUpdater() throws RecognitionException {
        int forUpdater_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 59)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:539:3: ( ^( FOR_UPDATE ( expression )* ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:540:3: ^( FOR_UPDATE ( expression )* )
            {
                match(input, FOR_UPDATE, FOLLOW_FOR_UPDATE_in_forUpdater2023);
                if (state.failed) return;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:540:16: ( expression )*
                    loop90:
                    do {
                        int alt90 = 2;
                        int LA90_0 = input.LA(1);

                        if ((LA90_0 == EXPR)) {
                            alt90 = 1;
                        }


                        switch (alt90) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: expression
                            {
                                pushFollow(FOLLOW_expression_in_forUpdater2025);
                                expression();

                                state._fsp--;
                                if (state.failed) return;

                            }
                            break;

                            default:
                                break loop90;
                        }
                    } while (true);


                    match(input, Token.UP, null);
                    if (state.failed) return;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 59, forUpdater_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "forUpdater"


    // $ANTLR start "evaluate"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:545:1: evaluate returns [com.sun.jdi.Value value] :
    // expression ;
    public final com.sun.jdi.Value evaluate() throws RecognitionException {
        com.sun.jdi.Value value = null;
        int evaluate_StartIndex = input.index();
        ExpressionValue expression1 = null;


        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 60)) {
                return value;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:546:3: ( expression )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:547:3: expression
            {
                pushFollow(FOLLOW_expression_in_evaluate2048);
                expression1 = expression();

                state._fsp--;
                if (state.failed) return value;
                if (state.backtracking == 0) {

                    value = expression1.getValue();

                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 60, evaluate_StartIndex);
            }
        }
        return value;
    }
    // $ANTLR end "evaluate"


    // $ANTLR start "parenthesizedExpression"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:553:1: parenthesizedExpression returns [ExpressionValue
    // value] : ^( PARENTESIZED_EXPR expression ) ;
    public final ExpressionValue parenthesizedExpression() throws RecognitionException {
        ExpressionValue value = null;
        int parenthesizedExpression_StartIndex = input.index();
        ExpressionValue expression2 = null;


        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 61)) {
                return value;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:554:3: ( ^( PARENTESIZED_EXPR expression ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:555:3: ^( PARENTESIZED_EXPR expression )
            {
                match(input, PARENTESIZED_EXPR, FOLLOW_PARENTESIZED_EXPR_in_parenthesizedExpression2084);
                if (state.failed) return value;

                match(input, Token.DOWN, null);
                if (state.failed) return value;
                pushFollow(FOLLOW_expression_in_parenthesizedExpression2086);
                expression2 = expression();

                state._fsp--;
                if (state.failed) return value;

                match(input, Token.UP, null);
                if (state.failed) return value;
                if (state.backtracking == 0) {

                    value = expression2;

                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 61, parenthesizedExpression_StartIndex);
            }
        }
        return value;
    }
    // $ANTLR end "parenthesizedExpression"


    // $ANTLR start "expression"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:561:1: expression returns [ExpressionValue value] : ^(
    // EXPR expr ) ;
    public final ExpressionValue expression() throws RecognitionException {
        ExpressionValue value = null;
        int expression_StartIndex = input.index();
        ExpressionValue expr3 = null;


        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 62)) {
                return value;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:562:3: ( ^( EXPR expr ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:563:3: ^( EXPR expr )
            {
                match(input, EXPR, FOLLOW_EXPR_in_expression2126);
                if (state.failed) return value;

                match(input, Token.DOWN, null);
                if (state.failed) return value;
                pushFollow(FOLLOW_expr_in_expression2128);
                expr3 = expr();

                state._fsp--;
                if (state.failed) return value;

                match(input, Token.UP, null);
                if (state.failed) return value;
                if (state.backtracking == 0) {

                    value = expr3;

                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 62, expression_StartIndex);
            }
        }
        return value;
    }
    // $ANTLR end "expression"


    // $ANTLR start "expr"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:569:1: expr returns [ExpressionValue value] : ( ^(
    // ASSIGN a= expr b= expr ) | ^( PLUS_ASSIGN a= expr b= expr ) | ^( MINUS_ASSIGN a= expr b= expr ) | ^( STAR_ASSIGN a= expr b= expr )
    // | ^( DIV_ASSIGN a= expr b= expr ) | ^( AND_ASSIGN a= expr b= expr ) | ^( OR_ASSIGN a= expr b= expr ) | ^( XOR_ASSIGN a= expr b=
    // expr ) | ^( MOD_ASSIGN a= expr b= expr ) | ^( BIT_SHIFT_RIGHT_ASSIGN a= expr b= expr ) | ^( SHIFT_RIGHT_ASSIGN a= expr b= expr ) |
    // ^( SHIFT_LEFT_ASSIGN a= expr b= expr ) | ^( QUESTION test= expr a= expr b= expr ) | ^( LOGICAL_OR a= expr b= expr ) | ^(
    // LOGICAL_AND a= expr b= expr ) | ^( OR a= expr b= expr ) | ^( XOR a= expr b= expr ) | ^( AND a= expr b= expr ) | ^( EQUAL a= expr
    // b= expr ) | ^( NOT_EQUAL a= expr b= expr ) | ^( INSTANCEOF expr type ) | ^( LESS_OR_EQUAL a= expr b= expr ) | ^( GREATER_OR_EQUAL
    // a= expr b= expr ) | ^( BIT_SHIFT_RIGHT a= expr b= expr ) | ^( SHIFT_RIGHT a= expr b= expr ) | ^( GREATER_THAN a= expr b= expr ) |
    // ^( SHIFT_LEFT a= expr b= expr ) | ^( LESS_THAN a= expr b= expr ) | ^( PLUS a= expr b= expr ) | ^( MINUS a= expr b= expr ) | ^(
    // STAR a= expr b= expr ) | ^( DIV a= expr b= expr ) | ^( MOD a= expr b= expr ) | ^( UNARY_PLUS a= expr ) | ^( UNARY_MINUS a= expr )
    // | ^( PRE_INC a= expr ) | ^( PRE_DEC expr ) | ^( POST_INC a= expr ) | ^( POST_DEC expr ) | ^( NOT a= expr ) | ^( LOGICAL_NOT a=
    // expr ) | ^( CAST_EXPR type expr ) | primaryExpression );
    public final ExpressionValue expr() throws RecognitionException {
        ExpressionValue value = null;
        int expr_StartIndex = input.index();
        CommonTree ASSIGN4 = null;
        CommonTree PLUS_ASSIGN5 = null;
        CommonTree MINUS_ASSIGN6 = null;
        CommonTree STAR_ASSIGN7 = null;
        CommonTree DIV_ASSIGN8 = null;
        CommonTree AND_ASSIGN9 = null;
        CommonTree OR_ASSIGN10 = null;
        CommonTree XOR_ASSIGN11 = null;
        CommonTree MOD_ASSIGN12 = null;
        CommonTree BIT_SHIFT_RIGHT_ASSIGN13 = null;
        CommonTree SHIFT_RIGHT_ASSIGN14 = null;
        CommonTree SHIFT_LEFT_ASSIGN15 = null;
        CommonTree LOGICAL_OR16 = null;
        CommonTree LOGICAL_AND17 = null;
        CommonTree OR18 = null;
        CommonTree XOR19 = null;
        CommonTree AND20 = null;
        CommonTree EQUAL21 = null;
        CommonTree NOT_EQUAL22 = null;
        CommonTree LESS_OR_EQUAL23 = null;
        CommonTree GREATER_OR_EQUAL24 = null;
        CommonTree BIT_SHIFT_RIGHT25 = null;
        CommonTree SHIFT_RIGHT26 = null;
        CommonTree GREATER_THAN27 = null;
        CommonTree SHIFT_LEFT28 = null;
        CommonTree LESS_THAN29 = null;
        CommonTree PLUS30 = null;
        CommonTree MINUS31 = null;
        CommonTree STAR32 = null;
        CommonTree DIV33 = null;
        CommonTree MOD34 = null;
        CommonTree UNARY_PLUS35 = null;
        CommonTree UNARY_MINUS36 = null;
        CommonTree NOT37 = null;
        CommonTree LOGICAL_NOT38 = null;
        ExpressionValue a = null;

        ExpressionValue b = null;

        ExpressionValue test = null;

        JavaTreeParser.primaryExpression_return primaryExpression39 = null;


        latest = ev.getThisObject();

        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 63)) {
                return value;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:573:3: ( ^( ASSIGN a= expr b= expr ) | ^(
            // PLUS_ASSIGN a= expr b= expr ) | ^( MINUS_ASSIGN a= expr b= expr ) | ^( STAR_ASSIGN a= expr b= expr ) | ^( DIV_ASSIGN a=
            // expr b= expr ) | ^( AND_ASSIGN a= expr b= expr ) | ^( OR_ASSIGN a= expr b= expr ) | ^( XOR_ASSIGN a= expr b= expr ) | ^(
            // MOD_ASSIGN a= expr b= expr ) | ^( BIT_SHIFT_RIGHT_ASSIGN a= expr b= expr ) | ^( SHIFT_RIGHT_ASSIGN a= expr b= expr ) | ^(
            // SHIFT_LEFT_ASSIGN a= expr b= expr ) | ^( QUESTION test= expr a= expr b= expr ) | ^( LOGICAL_OR a= expr b= expr ) | ^(
            // LOGICAL_AND a= expr b= expr ) | ^( OR a= expr b= expr ) | ^( XOR a= expr b= expr ) | ^( AND a= expr b= expr ) | ^( EQUAL
            // a= expr b= expr ) | ^( NOT_EQUAL a= expr b= expr ) | ^( INSTANCEOF expr type ) | ^( LESS_OR_EQUAL a= expr b= expr ) | ^(
            // GREATER_OR_EQUAL a= expr b= expr ) | ^( BIT_SHIFT_RIGHT a= expr b= expr ) | ^( SHIFT_RIGHT a= expr b= expr ) | ^(
            // GREATER_THAN a= expr b= expr ) | ^( SHIFT_LEFT a= expr b= expr ) | ^( LESS_THAN a= expr b= expr ) | ^( PLUS a= expr b=
            // expr ) | ^( MINUS a= expr b= expr ) | ^( STAR a= expr b= expr ) | ^( DIV a= expr b= expr ) | ^( MOD a= expr b= expr ) | ^(
            // UNARY_PLUS a= expr ) | ^( UNARY_MINUS a= expr ) | ^( PRE_INC a= expr ) | ^( PRE_DEC expr ) | ^( POST_INC a= expr ) | ^(
            // POST_DEC expr ) | ^( NOT a= expr ) | ^( LOGICAL_NOT a= expr ) | ^( CAST_EXPR type expr ) | primaryExpression )
            int alt91 = 43;
            switch (input.LA(1)) {
                case ASSIGN: {
                    alt91 = 1;
                }
                break;
                case PLUS_ASSIGN: {
                    alt91 = 2;
                }
                break;
                case MINUS_ASSIGN: {
                    alt91 = 3;
                }
                break;
                case STAR_ASSIGN: {
                    alt91 = 4;
                }
                break;
                case DIV_ASSIGN: {
                    alt91 = 5;
                }
                break;
                case AND_ASSIGN: {
                    alt91 = 6;
                }
                break;
                case OR_ASSIGN: {
                    alt91 = 7;
                }
                break;
                case XOR_ASSIGN: {
                    alt91 = 8;
                }
                break;
                case MOD_ASSIGN: {
                    alt91 = 9;
                }
                break;
                case BIT_SHIFT_RIGHT_ASSIGN: {
                    alt91 = 10;
                }
                break;
                case SHIFT_RIGHT_ASSIGN: {
                    alt91 = 11;
                }
                break;
                case SHIFT_LEFT_ASSIGN: {
                    alt91 = 12;
                }
                break;
                case QUESTION: {
                    alt91 = 13;
                }
                break;
                case LOGICAL_OR: {
                    alt91 = 14;
                }
                break;
                case LOGICAL_AND: {
                    alt91 = 15;
                }
                break;
                case OR: {
                    alt91 = 16;
                }
                break;
                case XOR: {
                    alt91 = 17;
                }
                break;
                case AND: {
                    alt91 = 18;
                }
                break;
                case EQUAL: {
                    alt91 = 19;
                }
                break;
                case NOT_EQUAL: {
                    alt91 = 20;
                }
                break;
                case INSTANCEOF: {
                    alt91 = 21;
                }
                break;
                case LESS_OR_EQUAL: {
                    alt91 = 22;
                }
                break;
                case GREATER_OR_EQUAL: {
                    alt91 = 23;
                }
                break;
                case BIT_SHIFT_RIGHT: {
                    alt91 = 24;
                }
                break;
                case SHIFT_RIGHT: {
                    alt91 = 25;
                }
                break;
                case GREATER_THAN: {
                    alt91 = 26;
                }
                break;
                case SHIFT_LEFT: {
                    alt91 = 27;
                }
                break;
                case LESS_THAN: {
                    alt91 = 28;
                }
                break;
                case PLUS: {
                    alt91 = 29;
                }
                break;
                case MINUS: {
                    alt91 = 30;
                }
                break;
                case STAR: {
                    alt91 = 31;
                }
                break;
                case DIV: {
                    alt91 = 32;
                }
                break;
                case MOD: {
                    alt91 = 33;
                }
                break;
                case UNARY_PLUS: {
                    alt91 = 34;
                }
                break;
                case UNARY_MINUS: {
                    alt91 = 35;
                }
                break;
                case PRE_INC: {
                    alt91 = 36;
                }
                break;
                case PRE_DEC: {
                    alt91 = 37;
                }
                break;
                case POST_INC: {
                    alt91 = 38;
                }
                break;
                case POST_DEC: {
                    alt91 = 39;
                }
                break;
                case NOT: {
                    alt91 = 40;
                }
                break;
                case LOGICAL_NOT: {
                    alt91 = 41;
                }
                break;
                case CAST_EXPR: {
                    alt91 = 42;
                }
                break;
                case DOT:
                case FALSE:
                case NULL:
                case SUPER:
                case THIS:
                case TRUE:
                case ARRAY_DECLARATOR:
                case ARRAY_ELEMENT_ACCESS:
                case CLASS_CONSTRUCTOR_CALL:
                case METHOD_CALL:
                case PARENTESIZED_EXPR:
                case STATIC_ARRAY_CREATOR:
                case SUPER_CONSTRUCTOR_CALL:
                case THIS_CONSTRUCTOR_CALL:
                case IDENT:
                case HEX_LITERAL:
                case OCTAL_LITERAL:
                case DECIMAL_LITERAL:
                case FLOATING_POINT_LITERAL:
                case CHARACTER_LITERAL:
                case STRING_LITERAL: {
                    alt91 = 43;
                }
                break;
                default:
                    if (state.backtracking > 0) {
                        state.failed = true;
                        return value;
                    }
                    NoViableAltException nvae =
                            new NoViableAltException("", 91, 0, input);

                    throw nvae;
            }

            switch (alt91) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:574:3: ^( ASSIGN a= expr b= expr )
                {
                    ASSIGN4 = (CommonTree)match(input, ASSIGN, FOLLOW_ASSIGN_in_expr2207);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2211);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2215);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (ASSIGN4 != null ? ASSIGN4.getType() : 0));

                    }

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:579:3: ^( PLUS_ASSIGN a= expr b= expr )
                {
                    PLUS_ASSIGN5 = (CommonTree)match(input, PLUS_ASSIGN, FOLLOW_PLUS_ASSIGN_in_expr2271);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2275);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2279);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (PLUS_ASSIGN5 != null ? PLUS_ASSIGN5.getType() : 0));

                    }

                }
                break;
                case 3:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:584:3: ^( MINUS_ASSIGN a= expr b= expr )
                {
                    MINUS_ASSIGN6 = (CommonTree)match(input, MINUS_ASSIGN, FOLLOW_MINUS_ASSIGN_in_expr2307);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2311);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2315);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (MINUS_ASSIGN6 != null ? MINUS_ASSIGN6.getType() : 0));

                    }

                }
                break;
                case 4:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:589:3: ^( STAR_ASSIGN a= expr b= expr )
                {
                    STAR_ASSIGN7 = (CommonTree)match(input, STAR_ASSIGN, FOLLOW_STAR_ASSIGN_in_expr2343);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2347);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2351);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (STAR_ASSIGN7 != null ? STAR_ASSIGN7.getType() : 0));

                    }

                }
                break;
                case 5:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:594:3: ^( DIV_ASSIGN a= expr b= expr )
                {
                    DIV_ASSIGN8 = (CommonTree)match(input, DIV_ASSIGN, FOLLOW_DIV_ASSIGN_in_expr2379);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2383);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2387);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (DIV_ASSIGN8 != null ? DIV_ASSIGN8.getType() : 0));

                    }

                }
                break;
                case 6:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:599:3: ^( AND_ASSIGN a= expr b= expr )
                {
                    AND_ASSIGN9 = (CommonTree)match(input, AND_ASSIGN, FOLLOW_AND_ASSIGN_in_expr2415);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2419);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2423);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (AND_ASSIGN9 != null ? AND_ASSIGN9.getType() : 0));

                    }

                }
                break;
                case 7:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:604:3: ^( OR_ASSIGN a= expr b= expr )
                {
                    OR_ASSIGN10 = (CommonTree)match(input, OR_ASSIGN, FOLLOW_OR_ASSIGN_in_expr2479);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2483);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2487);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (OR_ASSIGN10 != null ? OR_ASSIGN10.getType() : 0));

                    }

                }
                break;
                case 8:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:609:3: ^( XOR_ASSIGN a= expr b= expr )
                {
                    XOR_ASSIGN11 = (CommonTree)match(input, XOR_ASSIGN, FOLLOW_XOR_ASSIGN_in_expr2543);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2547);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2551);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (XOR_ASSIGN11 != null ? XOR_ASSIGN11.getType() : 0));

                    }

                }
                break;
                case 9:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:614:3: ^( MOD_ASSIGN a= expr b= expr )
                {
                    MOD_ASSIGN12 = (CommonTree)match(input, MOD_ASSIGN, FOLLOW_MOD_ASSIGN_in_expr2607);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2611);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2615);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (MOD_ASSIGN12 != null ? MOD_ASSIGN12.getType() : 0));

                    }

                }
                break;
                case 10:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:619:3: ^( BIT_SHIFT_RIGHT_ASSIGN a= expr
                    // b= expr )
                {
                    BIT_SHIFT_RIGHT_ASSIGN13 = (CommonTree)match(input, BIT_SHIFT_RIGHT_ASSIGN, FOLLOW_BIT_SHIFT_RIGHT_ASSIGN_in_expr2671);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2675);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2679);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (BIT_SHIFT_RIGHT_ASSIGN13 != null ? BIT_SHIFT_RIGHT_ASSIGN13.getType() : 0));

                    }

                }
                break;
                case 11:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:624:3: ^( SHIFT_RIGHT_ASSIGN a= expr b=
                    // expr )
                {
                    SHIFT_RIGHT_ASSIGN14 = (CommonTree)match(input, SHIFT_RIGHT_ASSIGN, FOLLOW_SHIFT_RIGHT_ASSIGN_in_expr2735);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2739);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2743);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (SHIFT_RIGHT_ASSIGN14 != null ? SHIFT_RIGHT_ASSIGN14.getType() : 0));

                    }

                }
                break;
                case 12:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:629:3: ^( SHIFT_LEFT_ASSIGN a= expr b=
                    // expr )
                {
                    SHIFT_LEFT_ASSIGN15 = (CommonTree)match(input, SHIFT_LEFT_ASSIGN, FOLLOW_SHIFT_LEFT_ASSIGN_in_expr2799);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2803);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2807);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (SHIFT_LEFT_ASSIGN15 != null ? SHIFT_LEFT_ASSIGN15.getType() : 0));

                    }

                }
                break;
                case 13:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:634:3: ^( QUESTION test= expr a= expr b=
                    // expr )
                {
                    match(input, QUESTION, FOLLOW_QUESTION_in_expr2863);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2867);
                    test = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2871);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2875);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.ternaryOperator(test, a, b);

                    }

                }
                break;
                case 14:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:639:3: ^( LOGICAL_OR a= expr b= expr )
                {
                    LOGICAL_OR16 = (CommonTree)match(input, LOGICAL_OR, FOLLOW_LOGICAL_OR_in_expr2931);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2935);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2939);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (LOGICAL_OR16 != null ? LOGICAL_OR16.getType() : 0));

                    }

                }
                break;
                case 15:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:644:3: ^( LOGICAL_AND a= expr b= expr )
                {
                    LOGICAL_AND17 = (CommonTree)match(input, LOGICAL_AND, FOLLOW_LOGICAL_AND_in_expr2995);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr2999);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3003);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (LOGICAL_AND17 != null ? LOGICAL_AND17.getType() : 0));

                    }

                }
                break;
                case 16:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:649:3: ^( OR a= expr b= expr )
                {
                    OR18 = (CommonTree)match(input, OR, FOLLOW_OR_in_expr3059);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3063);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3067);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (OR18 != null ? OR18.getType() : 0));

                    }

                }
                break;
                case 17:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:654:3: ^( XOR a= expr b= expr )
                {
                    XOR19 = (CommonTree)match(input, XOR, FOLLOW_XOR_in_expr3123);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3127);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3131);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (XOR19 != null ? XOR19.getType() : 0));

                    }

                }
                break;
                case 18:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:659:3: ^( AND a= expr b= expr )
                {
                    AND20 = (CommonTree)match(input, AND, FOLLOW_AND_in_expr3187);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3191);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3195);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (AND20 != null ? AND20.getType() : 0));

                    }

                }
                break;
                case 19:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:664:3: ^( EQUAL a= expr b= expr )
                {
                    EQUAL21 = (CommonTree)match(input, EQUAL, FOLLOW_EQUAL_in_expr3251);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3255);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3259);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (EQUAL21 != null ? EQUAL21.getType() : 0));

                    }

                }
                break;
                case 20:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:669:3: ^( NOT_EQUAL a= expr b= expr )
                {
                    NOT_EQUAL22 = (CommonTree)match(input, NOT_EQUAL, FOLLOW_NOT_EQUAL_in_expr3315);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3319);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3323);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (NOT_EQUAL22 != null ? NOT_EQUAL22.getType() : 0));

                    }

                }
                break;
                case 21:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:674:3: ^( INSTANCEOF expr type )
                {
                    match(input, INSTANCEOF, FOLLOW_INSTANCEOF_in_expr3379);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3381);
                    expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_type_in_expr3383);
                    type();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        throw new ExpressionException("Operation 'instanceof' is not supported yet. ");

                    }

                }
                break;
                case 22:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:679:3: ^( LESS_OR_EQUAL a= expr b= expr )
                {
                    LESS_OR_EQUAL23 = (CommonTree)match(input, LESS_OR_EQUAL, FOLLOW_LESS_OR_EQUAL_in_expr3439);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3443);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3447);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (LESS_OR_EQUAL23 != null ? LESS_OR_EQUAL23.getType() : 0));

                    }

                }
                break;
                case 23:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:684:3: ^( GREATER_OR_EQUAL a= expr b=
                    // expr )
                {
                    GREATER_OR_EQUAL24 = (CommonTree)match(input, GREATER_OR_EQUAL, FOLLOW_GREATER_OR_EQUAL_in_expr3503);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3507);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3511);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (GREATER_OR_EQUAL24 != null ? GREATER_OR_EQUAL24.getType() : 0));

                    }

                }
                break;
                case 24:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:689:3: ^( BIT_SHIFT_RIGHT a= expr b= expr )
                {
                    BIT_SHIFT_RIGHT25 = (CommonTree)match(input, BIT_SHIFT_RIGHT, FOLLOW_BIT_SHIFT_RIGHT_in_expr3567);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3571);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3575);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (BIT_SHIFT_RIGHT25 != null ? BIT_SHIFT_RIGHT25.getType() : 0));

                    }

                }
                break;
                case 25:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:694:3: ^( SHIFT_RIGHT a= expr b= expr )
                {
                    SHIFT_RIGHT26 = (CommonTree)match(input, SHIFT_RIGHT, FOLLOW_SHIFT_RIGHT_in_expr3603);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3607);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3611);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (SHIFT_RIGHT26 != null ? SHIFT_RIGHT26.getType() : 0));

                    }

                }
                break;
                case 26:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:699:3: ^( GREATER_THAN a= expr b= expr )
                {
                    GREATER_THAN27 = (CommonTree)match(input, GREATER_THAN, FOLLOW_GREATER_THAN_in_expr3639);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3643);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3647);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (GREATER_THAN27 != null ? GREATER_THAN27.getType() : 0));

                    }

                }
                break;
                case 27:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:704:3: ^( SHIFT_LEFT a= expr b= expr )
                {
                    SHIFT_LEFT28 = (CommonTree)match(input, SHIFT_LEFT, FOLLOW_SHIFT_LEFT_in_expr3675);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3679);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3683);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (SHIFT_LEFT28 != null ? SHIFT_LEFT28.getType() : 0));

                    }

                }
                break;
                case 28:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:709:3: ^( LESS_THAN a= expr b= expr )
                {
                    LESS_THAN29 = (CommonTree)match(input, LESS_THAN, FOLLOW_LESS_THAN_in_expr3711);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3715);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3719);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (LESS_THAN29 != null ? LESS_THAN29.getType() : 0));

                    }

                }
                break;
                case 29:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:714:3: ^( PLUS a= expr b= expr )
                {
                    PLUS30 = (CommonTree)match(input, PLUS, FOLLOW_PLUS_in_expr3747);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3751);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3755);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (PLUS30 != null ? PLUS30.getType() : 0));

                    }

                }
                break;
                case 30:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:719:3: ^( MINUS a= expr b= expr )
                {
                    MINUS31 = (CommonTree)match(input, MINUS, FOLLOW_MINUS_in_expr3783);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3787);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3791);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (MINUS31 != null ? MINUS31.getType() : 0));

                    }

                }
                break;
                case 31:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:724:3: ^( STAR a= expr b= expr )
                {
                    STAR32 = (CommonTree)match(input, STAR, FOLLOW_STAR_in_expr3819);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3823);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3827);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (STAR32 != null ? STAR32.getType() : 0));

                    }

                }
                break;
                case 32:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:729:3: ^( DIV a= expr b= expr )
                {
                    DIV33 = (CommonTree)match(input, DIV, FOLLOW_DIV_in_expr3855);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3859);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3863);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (DIV33 != null ? DIV33.getType() : 0));

                    }

                }
                break;
                case 33:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:734:3: ^( MOD a= expr b= expr )
                {
                    MOD34 = (CommonTree)match(input, MOD, FOLLOW_MOD_in_expr3891);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3895);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3899);
                    b = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.operation(a, b, (MOD34 != null ? MOD34.getType() : 0));

                    }

                }
                break;
                case 34:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:739:3: ^( UNARY_PLUS a= expr )
                {
                    UNARY_PLUS35 = (CommonTree)match(input, UNARY_PLUS, FOLLOW_UNARY_PLUS_in_expr3927);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3931);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.unaryOperation(a, (UNARY_PLUS35 != null ? UNARY_PLUS35.getType() : 0));

                    }

                }
                break;
                case 35:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:744:3: ^( UNARY_MINUS a= expr )
                {
                    UNARY_MINUS36 = (CommonTree)match(input, UNARY_MINUS, FOLLOW_UNARY_MINUS_in_expr3959);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3963);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.unaryOperation(a, (UNARY_MINUS36 != null ? UNARY_MINUS36.getType() : 0));

                    }

                }
                break;
                case 36:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:749:3: ^( PRE_INC a= expr )
                {
                    match(input, PRE_INC, FOLLOW_PRE_INC_in_expr3991);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr3995);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        throw new ExpressionException("Operation '++' is not supported yet. ");

                    }

                }
                break;
                case 37:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:754:3: ^( PRE_DEC expr )
                {
                    match(input, PRE_DEC, FOLLOW_PRE_DEC_in_expr4023);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr4025);
                    expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        throw new ExpressionException("Operation '--' is not supported yet. ");

                    }

                }
                break;
                case 38:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:759:3: ^( POST_INC a= expr )
                {
                    match(input, POST_INC, FOLLOW_POST_INC_in_expr4053);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr4057);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        throw new ExpressionException("Operation '++' is not supported yet. ");

                    }

                }
                break;
                case 39:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:764:3: ^( POST_DEC expr )
                {
                    match(input, POST_DEC, FOLLOW_POST_DEC_in_expr4085);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr4087);
                    expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        throw new ExpressionException("Operation '--' is not supported yet. ");

                    }

                }
                break;
                case 40:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:769:3: ^( NOT a= expr )
                {
                    NOT37 = (CommonTree)match(input, NOT, FOLLOW_NOT_in_expr4115);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr4119);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.unaryOperation(a, (NOT37 != null ? NOT37.getType() : 0));

                    }

                }
                break;
                case 41:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:774:3: ^( LOGICAL_NOT a= expr )
                {
                    LOGICAL_NOT38 = (CommonTree)match(input, LOGICAL_NOT, FOLLOW_LOGICAL_NOT_in_expr4147);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr4151);
                    a = expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.unaryOperation(a, (LOGICAL_NOT38 != null ? LOGICAL_NOT38.getType() : 0));

                    }

                }
                break;
                case 42:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:779:3: ^( CAST_EXPR type expr )
                {
                    match(input, CAST_EXPR, FOLLOW_CAST_EXPR_in_expr4179);
                    if (state.failed) return value;

                    match(input, Token.DOWN, null);
                    if (state.failed) return value;
                    pushFollow(FOLLOW_type_in_expr4181);
                    type();

                    state._fsp--;
                    if (state.failed) return value;
                    pushFollow(FOLLOW_expr_in_expr4183);
                    expr();

                    state._fsp--;
                    if (state.failed) return value;

                    match(input, Token.UP, null);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        throw new ExpressionException("Operation 'cast' is not supported yet. ");

                    }

                }
                break;
                case 43:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:783:5: primaryExpression
                {
                    pushFollow(FOLLOW_primaryExpression_in_expr4208);
                    primaryExpression39 = primaryExpression();

                    state._fsp--;
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = (primaryExpression39 != null ? primaryExpression39.value : null);

                    }

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 63, expr_StartIndex);
            }
        }
        return value;
    }
    // $ANTLR end "expr"

    public static class primaryExpression_return extends TreeRuleReturnScope {
        public ExpressionValue value;
    }

    ;

    // $ANTLR start "primaryExpression"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:789:1: primaryExpression returns [ExpressionValue value]
    // : ( ^( DOT (e= primaryExpression ( IDENT | THIS | SUPER | innerNewExpression | CLASS ) | primitiveType CLASS | VOID CLASS ) ) |
    // parenthesizedExpression | IDENT | ^( METHOD_CALL o= primaryExpression ( genericTypeArgumentList )? arguments ) |
    // explicitConstructorCall | ^( ARRAY_ELEMENT_ACCESS arr= primaryExpression index= expression ) | literal | newExpression | THIS |
    // arrayTypeDeclarator | SUPER );
    public final JavaTreeParser.primaryExpression_return primaryExpression() throws RecognitionException {
        JavaTreeParser.primaryExpression_return retval = new JavaTreeParser.primaryExpression_return();
        retval.start = input.LT(1);
        int primaryExpression_StartIndex = input.index();
        CommonTree IDENT40 = null;
        CommonTree IDENT42 = null;
        JavaTreeParser.primaryExpression_return e = null;

        JavaTreeParser.primaryExpression_return o = null;

        JavaTreeParser.primaryExpression_return arr = null;

        ExpressionValue index = null;

        ExpressionValue parenthesizedExpression41 = null;

        List<com.sun.jdi.Value> arguments43 = null;

        ExpressionValue literal44 = null;


        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 64)) {
                return retval;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:793:3: ( ^( DOT (e= primaryExpression ( IDENT |
            // THIS | SUPER | innerNewExpression | CLASS ) | primitiveType CLASS | VOID CLASS ) ) | parenthesizedExpression | IDENT | ^(
            // METHOD_CALL o= primaryExpression ( genericTypeArgumentList )? arguments ) | explicitConstructorCall | ^(
            // ARRAY_ELEMENT_ACCESS arr= primaryExpression index= expression ) | literal | newExpression | THIS | arrayTypeDeclarator |
            // SUPER )
            int alt95 = 11;
            switch (input.LA(1)) {
                case DOT: {
                    alt95 = 1;
                }
                break;
                case PARENTESIZED_EXPR: {
                    alt95 = 2;
                }
                break;
                case IDENT: {
                    alt95 = 3;
                }
                break;
                case METHOD_CALL: {
                    alt95 = 4;
                }
                break;
                case SUPER_CONSTRUCTOR_CALL:
                case THIS_CONSTRUCTOR_CALL: {
                    alt95 = 5;
                }
                break;
                case ARRAY_ELEMENT_ACCESS: {
                    alt95 = 6;
                }
                break;
                case FALSE:
                case NULL:
                case TRUE:
                case HEX_LITERAL:
                case OCTAL_LITERAL:
                case DECIMAL_LITERAL:
                case FLOATING_POINT_LITERAL:
                case CHARACTER_LITERAL:
                case STRING_LITERAL: {
                    alt95 = 7;
                }
                break;
                case CLASS_CONSTRUCTOR_CALL:
                case STATIC_ARRAY_CREATOR: {
                    alt95 = 8;
                }
                break;
                case THIS: {
                    alt95 = 9;
                }
                break;
                case ARRAY_DECLARATOR: {
                    alt95 = 10;
                }
                break;
                case SUPER: {
                    alt95 = 11;
                }
                break;
                default:
                    if (state.backtracking > 0) {
                        state.failed = true;
                        return retval;
                    }
                    NoViableAltException nvae =
                            new NoViableAltException("", 95, 0, input);

                    throw nvae;
            }

            switch (alt95) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:794:3: ^( DOT (e= primaryExpression (
                    // IDENT | THIS | SUPER | innerNewExpression | CLASS ) | primitiveType CLASS | VOID CLASS ) )
                {
                    match(input, DOT, FOLLOW_DOT_in_primaryExpression4302);
                    if (state.failed) return retval;

                    match(input, Token.DOWN, null);
                    if (state.failed) return retval;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:796:5: (e= primaryExpression ( IDENT |
                    // THIS | SUPER | innerNewExpression | CLASS ) | primitiveType CLASS | VOID CLASS )
                    int alt93 = 3;
                    switch (input.LA(1)) {
                        case DOT:
                        case FALSE:
                        case NULL:
                        case SUPER:
                        case THIS:
                        case TRUE:
                        case ARRAY_DECLARATOR:
                        case ARRAY_ELEMENT_ACCESS:
                        case CLASS_CONSTRUCTOR_CALL:
                        case METHOD_CALL:
                        case PARENTESIZED_EXPR:
                        case STATIC_ARRAY_CREATOR:
                        case SUPER_CONSTRUCTOR_CALL:
                        case THIS_CONSTRUCTOR_CALL:
                        case IDENT:
                        case HEX_LITERAL:
                        case OCTAL_LITERAL:
                        case DECIMAL_LITERAL:
                        case FLOATING_POINT_LITERAL:
                        case CHARACTER_LITERAL:
                        case STRING_LITERAL: {
                            alt93 = 1;
                        }
                        break;
                        case BOOLEAN:
                        case BYTE:
                        case CHAR:
                        case DOUBLE:
                        case FLOAT:
                        case INT:
                        case LONG:
                        case SHORT: {
                            alt93 = 2;
                        }
                        break;
                        case VOID: {
                            alt93 = 3;
                        }
                        break;
                        default:
                            if (state.backtracking > 0) {
                                state.failed = true;
                                return retval;
                            }
                            NoViableAltException nvae =
                                    new NoViableAltException("", 93, 0, input);

                            throw nvae;
                    }

                    switch (alt93) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:797:7: e= primaryExpression (
                            // IDENT | THIS | SUPER | innerNewExpression | CLASS )
                        {
                            pushFollow(FOLLOW_primaryExpression_in_primaryExpression4318);
                            e = primaryExpression();

                            state._fsp--;
                            if (state.failed) return retval;
                            if (state.backtracking == 0) {

                                retval.value = (e != null ? e.value : null);

                            }
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:801:7: ( IDENT | THIS | SUPER |
                            // innerNewExpression | CLASS )
                            int alt92 = 5;
                            switch (input.LA(1)) {
                                case IDENT: {
                                    alt92 = 1;
                                }
                                break;
                                case THIS: {
                                    alt92 = 2;
                                }
                                break;
                                case SUPER: {
                                    alt92 = 3;
                                }
                                break;
                                case CLASS_CONSTRUCTOR_CALL: {
                                    alt92 = 4;
                                }
                                break;
                                case CLASS: {
                                    alt92 = 5;
                                }
                                break;
                                default:
                                    if (state.backtracking > 0) {
                                        state.failed = true;
                                        return retval;
                                    }
                                    NoViableAltException nvae =
                                            new NoViableAltException("", 92, 0, input);

                                    throw nvae;
                            }

                            switch (alt92) {
                                case 1:
                                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:802:9: IDENT
                                {
                                    IDENT40 = (CommonTree)match(input, IDENT, FOLLOW_IDENT_in_primaryExpression4353);
                                    if (state.failed) return retval;
                                    if (state.backtracking == 0) {

                                        if (((CommonTree)retval.start).getParent().getType() != METHOD_CALL) {
                                            retval.value = ev.getField(latest.getValue(), (IDENT40 != null ? IDENT40.getText() : null));
                                            if (retval.value == null) {
                                                throw new ExpressionException(
                                                        "Unknown field " + (IDENT40 != null ? IDENT40.getText() : null));
                                            }
                                        }

                                    }

                                }
                                break;
                                case 2:
                                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:811:11: THIS
                                {
                                    match(input, THIS, FOLLOW_THIS_in_primaryExpression4383);
                                    if (state.failed) return retval;
                                    if (state.backtracking == 0) {

                                        retval.value = ev.getThisObject();

                                    }

                                }
                                break;
                                case 3:
                                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:815:11: SUPER
                                {
                                    match(input, SUPER, FOLLOW_SUPER_in_primaryExpression4413);
                                    if (state.failed) return retval;

                                }
                                break;
                                case 4:
                                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:816:11: innerNewExpression
                                {
                                    pushFollow(FOLLOW_innerNewExpression_in_primaryExpression4425);
                                    innerNewExpression();

                                    state._fsp--;
                                    if (state.failed) return retval;
                                    if (state.backtracking == 0) {

                                        throw new ExpressionException("Unable create new instance. Operation not supported yet. ");

                                    }

                                }
                                break;
                                case 5:
                                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:820:11: CLASS
                                {
                                    match(input, CLASS, FOLLOW_CLASS_in_primaryExpression4485);
                                    if (state.failed) return retval;

                                }
                                break;

                            }


                        }
                        break;
                        case 2:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:822:9: primitiveType CLASS
                        {
                            pushFollow(FOLLOW_primitiveType_in_primaryExpression4503);
                            primitiveType();

                            state._fsp--;
                            if (state.failed) return retval;
                            match(input, CLASS, FOLLOW_CLASS_in_primaryExpression4505);
                            if (state.failed) return retval;

                        }
                        break;
                        case 3:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:823:9: VOID CLASS
                        {
                            match(input, VOID, FOLLOW_VOID_in_primaryExpression4515);
                            if (state.failed) return retval;
                            match(input, CLASS, FOLLOW_CLASS_in_primaryExpression4517);
                            if (state.failed) return retval;

                        }
                        break;

                    }


                    match(input, Token.UP, null);
                    if (state.failed) return retval;

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:826:5: parenthesizedExpression
                {
                    pushFollow(FOLLOW_parenthesizedExpression_in_primaryExpression4534);
                    parenthesizedExpression41 = parenthesizedExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if (state.backtracking == 0) {

                        retval.value = parenthesizedExpression41;

                    }

                }
                break;
                case 3:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:830:5: IDENT
                {
                    IDENT42 = (CommonTree)match(input, IDENT, FOLLOW_IDENT_in_primaryExpression4558);
                    if (state.failed) return retval;
                    if (state.backtracking == 0) {

                        if (((CommonTree)retval.start).getParent().getType() != METHOD_CALL) {
                            retval.value = ev.getLocalVariable((IDENT42 != null ? IDENT42.getText() : null));
                            if (retval.value == null) {
                                retval.value = ev.getField(latest.getValue(), (IDENT42 != null ? IDENT42.getText() : null));
                            }
                            if (retval.value == null) {
                                throw new ExpressionException(
                                        "Unknown local variable or field " + (IDENT42 != null ? IDENT42.getText() : null));
                            }
                        } else {
                            retval.value = ev.getThisObject();
                        }


                    }

                }
                break;
                case 4:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:846:3: ^( METHOD_CALL o=
                    // primaryExpression ( genericTypeArgumentList )? arguments )
                {
                    match(input, METHOD_CALL, FOLLOW_METHOD_CALL_in_primaryExpression4585);
                    if (state.failed) return retval;

                    match(input, Token.DOWN, null);
                    if (state.failed) return retval;
                    pushFollow(FOLLOW_primaryExpression_in_primaryExpression4589);
                    o = primaryExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:846:37: ( genericTypeArgumentList )?
                    int alt94 = 2;
                    int LA94_0 = input.LA(1);

                    if ((LA94_0 == GENERIC_TYPE_ARG_LIST)) {
                        alt94 = 1;
                    }
                    switch (alt94) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: genericTypeArgumentList
                        {
                            pushFollow(FOLLOW_genericTypeArgumentList_in_primaryExpression4591);
                            genericTypeArgumentList();

                            state._fsp--;
                            if (state.failed) return retval;

                        }
                        break;

                    }

                    pushFollow(FOLLOW_arguments_in_primaryExpression4594);
                    arguments43 = arguments();

                    state._fsp--;
                    if (state.failed) return retval;

                    match(input, Token.UP, null);
                    if (state.failed) return retval;
                    if (state.backtracking == 0) {


                        String name =
                                (o != null ? ((CommonTree)o.start) : null).getChildCount() == 0 ? (o != null ? ((CommonTree)o.start) : null)
                                        .getText() : (o != null ? ((CommonTree)o.start) : null).getChild(1).getText();
                        retval.value = ev.invokeMethod(o.value.getValue(), name, arguments43);


                    }

                }
                break;
                case 5:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:853:5: explicitConstructorCall
                {
                    pushFollow(FOLLOW_explicitConstructorCall_in_primaryExpression4618);
                    explicitConstructorCall();

                    state._fsp--;
                    if (state.failed) return retval;
                    if (state.backtracking == 0) {

                        throw new ExpressionException("Unable create new instance. Operation not supported yet. ");

                    }

                }
                break;
                case 6:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:858:3: ^( ARRAY_ELEMENT_ACCESS arr=
                    // primaryExpression index= expression )
                {
                    match(input, ARRAY_ELEMENT_ACCESS, FOLLOW_ARRAY_ELEMENT_ACCESS_in_primaryExpression4645);
                    if (state.failed) return retval;

                    match(input, Token.DOWN, null);
                    if (state.failed) return retval;
                    pushFollow(FOLLOW_primaryExpression_in_primaryExpression4649);
                    arr = primaryExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    pushFollow(FOLLOW_expression_in_primaryExpression4653);
                    index = expression();

                    state._fsp--;
                    if (state.failed) return retval;

                    match(input, Token.UP, null);
                    if (state.failed) return retval;
                    if (state.backtracking == 0) {

                        retval.value = ev.getArrayElement((arr != null ? arr.value : null).getValue(), index.getValue());

                    }

                }
                break;
                case 7:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:862:5: literal
                {
                    pushFollow(FOLLOW_literal_in_primaryExpression4677);
                    literal44 = literal();

                    state._fsp--;
                    if (state.failed) return retval;
                    if (state.backtracking == 0) {

                        retval.value = literal44;

                    }

                }
                break;
                case 8:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:866:5: newExpression
                {
                    pushFollow(FOLLOW_newExpression_in_primaryExpression4701);
                    newExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if (state.backtracking == 0) {

                        throw new ExpressionException("Unable create new instance. Operation not supported yet. ");

                    }

                }
                break;
                case 9:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:870:5: THIS
                {
                    match(input, THIS, FOLLOW_THIS_in_primaryExpression4725);
                    if (state.failed) return retval;
                    if (state.backtracking == 0) {

                        retval.value = ev.getThisObject();

                    }

                }
                break;
                case 10:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:874:5: arrayTypeDeclarator
                {
                    pushFollow(FOLLOW_arrayTypeDeclarator_in_primaryExpression4749);
                    arrayTypeDeclarator();

                    state._fsp--;
                    if (state.failed) return retval;

                }
                break;
                case 11:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:875:5: SUPER
                {
                    match(input, SUPER, FOLLOW_SUPER_in_primaryExpression4755);
                    if (state.failed) return retval;
                    if (state.backtracking == 0) {

                        retval.value = ev.getThisObject();

                    }

                }
                break;

            }
            if (state.backtracking == 0) {

                latest = retval.value;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 64, primaryExpression_StartIndex);
            }
        }
        return retval;
    }
    // $ANTLR end "primaryExpression"


    // $ANTLR start "explicitConstructorCall"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:881:1: explicitConstructorCall : ( ^(
    // THIS_CONSTRUCTOR_CALL ( genericTypeArgumentList )? arguments ) | ^( SUPER_CONSTRUCTOR_CALL ( primaryExpression )? (
    // genericTypeArgumentList )? arguments ) );
    public final void explicitConstructorCall() throws RecognitionException {
        int explicitConstructorCall_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 65)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:882:3: ( ^( THIS_CONSTRUCTOR_CALL (
            // genericTypeArgumentList )? arguments ) | ^( SUPER_CONSTRUCTOR_CALL ( primaryExpression )? ( genericTypeArgumentList )?
            // arguments ) )
            int alt99 = 2;
            int LA99_0 = input.LA(1);

            if ((LA99_0 == THIS_CONSTRUCTOR_CALL)) {
                alt99 = 1;
            } else if ((LA99_0 == SUPER_CONSTRUCTOR_CALL)) {
                alt99 = 2;
            } else {
                if (state.backtracking > 0) {
                    state.failed = true;
                    return;
                }
                NoViableAltException nvae =
                        new NoViableAltException("", 99, 0, input);

                throw nvae;
            }
            switch (alt99) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:883:3: ^( THIS_CONSTRUCTOR_CALL (
                    // genericTypeArgumentList )? arguments )
                {
                    match(input, THIS_CONSTRUCTOR_CALL, FOLLOW_THIS_CONSTRUCTOR_CALL_in_explicitConstructorCall4789);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:883:27: ( genericTypeArgumentList )?
                    int alt96 = 2;
                    int LA96_0 = input.LA(1);

                    if ((LA96_0 == GENERIC_TYPE_ARG_LIST)) {
                        alt96 = 1;
                    }
                    switch (alt96) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: genericTypeArgumentList
                        {
                            pushFollow(FOLLOW_genericTypeArgumentList_in_explicitConstructorCall4791);
                            genericTypeArgumentList();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    pushFollow(FOLLOW_arguments_in_explicitConstructorCall4794);
                    arguments();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:885:3: ^( SUPER_CONSTRUCTOR_CALL (
                    // primaryExpression )? ( genericTypeArgumentList )? arguments )
                {
                    match(input, SUPER_CONSTRUCTOR_CALL, FOLLOW_SUPER_CONSTRUCTOR_CALL_in_explicitConstructorCall4804);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:885:28: ( primaryExpression )?
                    int alt97 = 2;
                    int LA97_0 = input.LA(1);

                    if ((LA97_0 == DOT || LA97_0 == FALSE || LA97_0 == NULL || LA97_0 == SUPER || LA97_0 == THIS || LA97_0 == TRUE ||
                         LA97_0 == ARRAY_DECLARATOR || LA97_0 == ARRAY_ELEMENT_ACCESS || LA97_0 == CLASS_CONSTRUCTOR_CALL ||
                         LA97_0 == METHOD_CALL || LA97_0 == PARENTESIZED_EXPR ||
                         (LA97_0 >= STATIC_ARRAY_CREATOR && LA97_0 <= SUPER_CONSTRUCTOR_CALL) || LA97_0 == THIS_CONSTRUCTOR_CALL ||
                         (LA97_0 >= IDENT && LA97_0 <= STRING_LITERAL))) {
                        alt97 = 1;
                    }
                    switch (alt97) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: primaryExpression
                        {
                            pushFollow(FOLLOW_primaryExpression_in_explicitConstructorCall4806);
                            primaryExpression();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:885:47: ( genericTypeArgumentList )?
                    int alt98 = 2;
                    int LA98_0 = input.LA(1);

                    if ((LA98_0 == GENERIC_TYPE_ARG_LIST)) {
                        alt98 = 1;
                    }
                    switch (alt98) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: genericTypeArgumentList
                        {
                            pushFollow(FOLLOW_genericTypeArgumentList_in_explicitConstructorCall4809);
                            genericTypeArgumentList();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    pushFollow(FOLLOW_arguments_in_explicitConstructorCall4812);
                    arguments();

                    state._fsp--;
                    if (state.failed) return;

                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 65, explicitConstructorCall_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "explicitConstructorCall"


    // $ANTLR start "arrayTypeDeclarator"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:888:1: arrayTypeDeclarator : ^( ARRAY_DECLARATOR (
    // arrayTypeDeclarator | qualifiedIdentifier | primitiveType ) ) ;
    public final void arrayTypeDeclarator() throws RecognitionException {
        int arrayTypeDeclarator_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 66)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:889:3: ( ^( ARRAY_DECLARATOR (
            // arrayTypeDeclarator | qualifiedIdentifier | primitiveType ) ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:890:3: ^( ARRAY_DECLARATOR ( arrayTypeDeclarator
            // | qualifiedIdentifier | primitiveType ) )
            {
                match(input, ARRAY_DECLARATOR, FOLLOW_ARRAY_DECLARATOR_in_arrayTypeDeclarator4834);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:892:5: ( arrayTypeDeclarator |
                // qualifiedIdentifier | primitiveType )
                int alt100 = 3;
                switch (input.LA(1)) {
                    case ARRAY_DECLARATOR: {
                        alt100 = 1;
                    }
                    break;
                    case DOT:
                    case IDENT: {
                        alt100 = 2;
                    }
                    break;
                    case BOOLEAN:
                    case BYTE:
                    case CHAR:
                    case DOUBLE:
                    case FLOAT:
                    case INT:
                    case LONG:
                    case SHORT: {
                        alt100 = 3;
                    }
                    break;
                    default:
                        if (state.backtracking > 0) {
                            state.failed = true;
                            return;
                        }
                        NoViableAltException nvae =
                                new NoViableAltException("", 100, 0, input);

                        throw nvae;
                }

                switch (alt100) {
                    case 1:
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:893:7: arrayTypeDeclarator
                    {
                        pushFollow(FOLLOW_arrayTypeDeclarator_in_arrayTypeDeclarator4848);
                        arrayTypeDeclarator();

                        state._fsp--;
                        if (state.failed) return;

                    }
                    break;
                    case 2:
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:894:9: qualifiedIdentifier
                    {
                        pushFollow(FOLLOW_qualifiedIdentifier_in_arrayTypeDeclarator4858);
                        qualifiedIdentifier();

                        state._fsp--;
                        if (state.failed) return;

                    }
                    break;
                    case 3:
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:895:9: primitiveType
                    {
                        pushFollow(FOLLOW_primitiveType_in_arrayTypeDeclarator4868);
                        primitiveType();

                        state._fsp--;
                        if (state.failed) return;

                    }
                    break;

                }


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 66, arrayTypeDeclarator_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "arrayTypeDeclarator"


    // $ANTLR start "newExpression"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:900:1: newExpression : ( ^( STATIC_ARRAY_CREATOR (
    // primitiveType newArrayConstruction | ( genericTypeArgumentList )? qualifiedTypeIdent newArrayConstruction ) ) | ^(
    // CLASS_CONSTRUCTOR_CALL ( genericTypeArgumentList )? qualifiedTypeIdent arguments ( classTopLevelScope )? ) );
    public final void newExpression() throws RecognitionException {
        int newExpression_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 67)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:901:3: ( ^( STATIC_ARRAY_CREATOR ( primitiveType
            // newArrayConstruction | ( genericTypeArgumentList )? qualifiedTypeIdent newArrayConstruction ) ) | ^(
            // CLASS_CONSTRUCTOR_CALL ( genericTypeArgumentList )? qualifiedTypeIdent arguments ( classTopLevelScope )? ) )
            int alt105 = 2;
            int LA105_0 = input.LA(1);

            if ((LA105_0 == STATIC_ARRAY_CREATOR)) {
                alt105 = 1;
            } else if ((LA105_0 == CLASS_CONSTRUCTOR_CALL)) {
                alt105 = 2;
            } else {
                if (state.backtracking > 0) {
                    state.failed = true;
                    return;
                }
                NoViableAltException nvae =
                        new NoViableAltException("", 105, 0, input);

                throw nvae;
            }
            switch (alt105) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:902:3: ^( STATIC_ARRAY_CREATOR (
                    // primitiveType newArrayConstruction | ( genericTypeArgumentList )? qualifiedTypeIdent newArrayConstruction ) )
                {
                    match(input, STATIC_ARRAY_CREATOR, FOLLOW_STATIC_ARRAY_CREATOR_in_newExpression4900);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:904:5: ( primitiveType
                    // newArrayConstruction | ( genericTypeArgumentList )? qualifiedTypeIdent newArrayConstruction )
                    int alt102 = 2;
                    int LA102_0 = input.LA(1);

                    if ((LA102_0 == BOOLEAN || LA102_0 == BYTE || LA102_0 == CHAR || LA102_0 == DOUBLE || LA102_0 == FLOAT ||
                         (LA102_0 >= INT && LA102_0 <= LONG) || LA102_0 == SHORT)) {
                        alt102 = 1;
                    } else if ((LA102_0 == GENERIC_TYPE_ARG_LIST || LA102_0 == QUALIFIED_TYPE_IDENT)) {
                        alt102 = 2;
                    } else {
                        if (state.backtracking > 0) {
                            state.failed = true;
                            return;
                        }
                        NoViableAltException nvae =
                                new NoViableAltException("", 102, 0, input);

                        throw nvae;
                    }
                    switch (alt102) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:905:7: primitiveType
                            // newArrayConstruction
                        {
                            pushFollow(FOLLOW_primitiveType_in_newExpression4914);
                            primitiveType();

                            state._fsp--;
                            if (state.failed) return;
                            pushFollow(FOLLOW_newArrayConstruction_in_newExpression4916);
                            newArrayConstruction();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;
                        case 2:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:906:9: ( genericTypeArgumentList
                            // )? qualifiedTypeIdent newArrayConstruction
                        {
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:906:9: ( genericTypeArgumentList )?
                            int alt101 = 2;
                            int LA101_0 = input.LA(1);

                            if ((LA101_0 == GENERIC_TYPE_ARG_LIST)) {
                                alt101 = 1;
                            }
                            switch (alt101) {
                                case 1:
                                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0:
                                    // genericTypeArgumentList
                                {
                                    pushFollow(FOLLOW_genericTypeArgumentList_in_newExpression4926);
                                    genericTypeArgumentList();

                                    state._fsp--;
                                    if (state.failed) return;

                                }
                                break;

                            }

                            pushFollow(FOLLOW_qualifiedTypeIdent_in_newExpression4929);
                            qualifiedTypeIdent();

                            state._fsp--;
                            if (state.failed) return;
                            pushFollow(FOLLOW_newArrayConstruction_in_newExpression4931);
                            newArrayConstruction();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }


                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:910:3: ^( CLASS_CONSTRUCTOR_CALL (
                    // genericTypeArgumentList )? qualifiedTypeIdent arguments ( classTopLevelScope )? )
                {
                    match(input, CLASS_CONSTRUCTOR_CALL, FOLLOW_CLASS_CONSTRUCTOR_CALL_in_newExpression4951);
                    if (state.failed) return;

                    match(input, Token.DOWN, null);
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:910:28: ( genericTypeArgumentList )?
                    int alt103 = 2;
                    int LA103_0 = input.LA(1);

                    if ((LA103_0 == GENERIC_TYPE_ARG_LIST)) {
                        alt103 = 1;
                    }
                    switch (alt103) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: genericTypeArgumentList
                        {
                            pushFollow(FOLLOW_genericTypeArgumentList_in_newExpression4953);
                            genericTypeArgumentList();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }

                    pushFollow(FOLLOW_qualifiedTypeIdent_in_newExpression4956);
                    qualifiedTypeIdent();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_arguments_in_newExpression4958);
                    arguments();

                    state._fsp--;
                    if (state.failed) return;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:910:82: ( classTopLevelScope )?
                    int alt104 = 2;
                    int LA104_0 = input.LA(1);

                    if ((LA104_0 == CLASS_TOP_LEVEL_SCOPE)) {
                        alt104 = 1;
                    }
                    switch (alt104) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: classTopLevelScope
                        {
                            pushFollow(FOLLOW_classTopLevelScope_in_newExpression4960);
                            classTopLevelScope();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }


                    match(input, Token.UP, null);
                    if (state.failed) return;

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 67, newExpression_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "newExpression"


    // $ANTLR start "innerNewExpression"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:913:1: innerNewExpression : ^( CLASS_CONSTRUCTOR_CALL (
    // genericTypeArgumentList )? IDENT arguments ( classTopLevelScope )? ) ;
    public final void innerNewExpression() throws RecognitionException {
        int innerNewExpression_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 68)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:914:3: ( ^( CLASS_CONSTRUCTOR_CALL (
            // genericTypeArgumentList )? IDENT arguments ( classTopLevelScope )? ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:915:3: ^( CLASS_CONSTRUCTOR_CALL (
            // genericTypeArgumentList )? IDENT arguments ( classTopLevelScope )? )
            {
                match(input, CLASS_CONSTRUCTOR_CALL, FOLLOW_CLASS_CONSTRUCTOR_CALL_in_innerNewExpression4979);
                if (state.failed) return;

                match(input, Token.DOWN, null);
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:915:28: ( genericTypeArgumentList )?
                int alt106 = 2;
                int LA106_0 = input.LA(1);

                if ((LA106_0 == GENERIC_TYPE_ARG_LIST)) {
                    alt106 = 1;
                }
                switch (alt106) {
                    case 1:
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: genericTypeArgumentList
                    {
                        pushFollow(FOLLOW_genericTypeArgumentList_in_innerNewExpression4981);
                        genericTypeArgumentList();

                        state._fsp--;
                        if (state.failed) return;

                    }
                    break;

                }

                match(input, IDENT, FOLLOW_IDENT_in_innerNewExpression4984);
                if (state.failed) return;
                pushFollow(FOLLOW_arguments_in_innerNewExpression4986);
                arguments();

                state._fsp--;
                if (state.failed) return;
                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:915:69: ( classTopLevelScope )?
                int alt107 = 2;
                int LA107_0 = input.LA(1);

                if ((LA107_0 == CLASS_TOP_LEVEL_SCOPE)) {
                    alt107 = 1;
                }
                switch (alt107) {
                    case 1:
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: classTopLevelScope
                    {
                        pushFollow(FOLLOW_classTopLevelScope_in_innerNewExpression4988);
                        classTopLevelScope();

                        state._fsp--;
                        if (state.failed) return;

                    }
                    break;

                }


                match(input, Token.UP, null);
                if (state.failed) return;

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 68, innerNewExpression_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "innerNewExpression"


    // $ANTLR start "newArrayConstruction"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:918:1: newArrayConstruction : ( arrayDeclaratorList
    // arrayInitializer | ( expression )+ ( arrayDeclaratorList )? );
    public final void newArrayConstruction() throws RecognitionException {
        int newArrayConstruction_StartIndex = input.index();
        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 69)) {
                return;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:919:3: ( arrayDeclaratorList arrayInitializer |
            // ( expression )+ ( arrayDeclaratorList )? )
            int alt110 = 2;
            int LA110_0 = input.LA(1);

            if ((LA110_0 == ARRAY_DECLARATOR_LIST)) {
                alt110 = 1;
            } else if ((LA110_0 == EXPR)) {
                alt110 = 2;
            } else {
                if (state.backtracking > 0) {
                    state.failed = true;
                    return;
                }
                NoViableAltException nvae =
                        new NoViableAltException("", 110, 0, input);

                throw nvae;
            }
            switch (alt110) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:920:3: arrayDeclaratorList arrayInitializer
                {
                    pushFollow(FOLLOW_arrayDeclaratorList_in_newArrayConstruction5005);
                    arrayDeclaratorList();

                    state._fsp--;
                    if (state.failed) return;
                    pushFollow(FOLLOW_arrayInitializer_in_newArrayConstruction5007);
                    arrayInitializer();

                    state._fsp--;
                    if (state.failed) return;

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:921:5: ( expression )+ (
                    // arrayDeclaratorList )?
                {
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:921:5: ( expression )+
                    int cnt108 = 0;
                    loop108:
                    do {
                        int alt108 = 2;
                        int LA108_0 = input.LA(1);

                        if ((LA108_0 == EXPR)) {
                            alt108 = 1;
                        }


                        switch (alt108) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: expression
                            {
                                pushFollow(FOLLOW_expression_in_newArrayConstruction5013);
                                expression();

                                state._fsp--;
                                if (state.failed) return;

                            }
                            break;

                            default:
                                if (cnt108 >= 1) break loop108;
                                if (state.backtracking > 0) {
                                    state.failed = true;
                                    return;
                                }
                                EarlyExitException eee =
                                        new EarlyExitException(108, input);
                                throw eee;
                        }
                        cnt108++;
                    } while (true);

                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:921:17: ( arrayDeclaratorList )?
                    int alt109 = 2;
                    int LA109_0 = input.LA(1);

                    if ((LA109_0 == ARRAY_DECLARATOR_LIST)) {
                        alt109 = 1;
                    }
                    switch (alt109) {
                        case 1:
                            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: arrayDeclaratorList
                        {
                            pushFollow(FOLLOW_arrayDeclaratorList_in_newArrayConstruction5016);
                            arrayDeclaratorList();

                            state._fsp--;
                            if (state.failed) return;

                        }
                        break;

                    }


                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 69, newArrayConstruction_StartIndex);
            }
        }
        return;
    }
    // $ANTLR end "newArrayConstruction"


    // $ANTLR start "arguments"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:924:1: arguments returns [List < com.sun.jdi.Value >
    // args] : ^( ARGUMENT_LIST (e= expression )* ) ;
    public final List<com.sun.jdi.Value> arguments() throws RecognitionException {
        List<com.sun.jdi.Value> args = null;
        int arguments_StartIndex = input.index();
        ExpressionValue e = null;


        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 70)) {
                return args;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:925:3: ( ^( ARGUMENT_LIST (e= expression )* ) )
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:926:17: ^( ARGUMENT_LIST (e= expression )* )
            {
                if (state.backtracking == 0) {

                    args = new ArrayList<com.sun.jdi.Value>();

                }
                match(input, ARGUMENT_LIST, FOLLOW_ARGUMENT_LIST_in_arguments5060);
                if (state.failed) return args;

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    if (state.failed) return args;
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:931:5: (e= expression )*
                    loop111:
                    do {
                        int alt111 = 2;
                        int LA111_0 = input.LA(1);

                        if ((LA111_0 == EXPR)) {
                            alt111 = 1;
                        }


                        switch (alt111) {
                            case 1:
                                // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:932:7: e= expression
                            {
                                pushFollow(FOLLOW_expression_in_arguments5076);
                                e = expression();

                                state._fsp--;
                                if (state.failed) return args;
                                if (state.backtracking == 0) {

                                    args.add(e.getValue());

                                }

                            }
                            break;

                            default:
                                break loop111;
                        }
                    } while (true);


                    match(input, Token.UP, null);
                    if (state.failed) return args;
                }

            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 70, arguments_StartIndex);
            }
        }
        return args;
    }
    // $ANTLR end "arguments"


    // $ANTLR start "literal"
    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:940:1: literal returns [ExpressionValue value] : (
    // HEX_LITERAL | OCTAL_LITERAL | DECIMAL_LITERAL | FLOATING_POINT_LITERAL | CHARACTER_LITERAL | STRING_LITERAL | TRUE | FALSE | NULL );
    public final ExpressionValue literal() throws RecognitionException {
        ExpressionValue value = null;
        int literal_StartIndex = input.index();
        CommonTree HEX_LITERAL45 = null;
        CommonTree OCTAL_LITERAL46 = null;
        CommonTree DECIMAL_LITERAL47 = null;
        CommonTree FLOATING_POINT_LITERAL48 = null;
        CommonTree CHARACTER_LITERAL49 = null;
        CommonTree STRING_LITERAL50 = null;
        CommonTree TRUE51 = null;
        CommonTree FALSE52 = null;

        try {
            if (state.backtracking > 0 && alreadyParsedRule(input, 71)) {
                return value;
            }
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:941:3: ( HEX_LITERAL | OCTAL_LITERAL |
            // DECIMAL_LITERAL | FLOATING_POINT_LITERAL | CHARACTER_LITERAL | STRING_LITERAL | TRUE | FALSE | NULL )
            int alt112 = 9;
            switch (input.LA(1)) {
                case HEX_LITERAL: {
                    alt112 = 1;
                }
                break;
                case OCTAL_LITERAL: {
                    alt112 = 2;
                }
                break;
                case DECIMAL_LITERAL: {
                    alt112 = 3;
                }
                break;
                case FLOATING_POINT_LITERAL: {
                    alt112 = 4;
                }
                break;
                case CHARACTER_LITERAL: {
                    alt112 = 5;
                }
                break;
                case STRING_LITERAL: {
                    alt112 = 6;
                }
                break;
                case TRUE: {
                    alt112 = 7;
                }
                break;
                case FALSE: {
                    alt112 = 8;
                }
                break;
                case NULL: {
                    alt112 = 9;
                }
                break;
                default:
                    if (state.backtracking > 0) {
                        state.failed = true;
                        return value;
                    }
                    NoViableAltException nvae =
                            new NoViableAltException("", 112, 0, input);

                    throw nvae;
            }

            switch (alt112) {
                case 1:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:942:3: HEX_LITERAL
                {
                    HEX_LITERAL45 = (CommonTree)match(input, HEX_LITERAL, FOLLOW_HEX_LITERAL_in_literal5125);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.hexValue((HEX_LITERAL45 != null ? HEX_LITERAL45.getText() : null));

                    }

                }
                break;
                case 2:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:946:5: OCTAL_LITERAL
                {
                    OCTAL_LITERAL46 = (CommonTree)match(input, OCTAL_LITERAL, FOLLOW_OCTAL_LITERAL_in_literal5149);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.octalValue((OCTAL_LITERAL46 != null ? OCTAL_LITERAL46.getText() : null));

                    }

                }
                break;
                case 3:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:950:5: DECIMAL_LITERAL
                {
                    DECIMAL_LITERAL47 = (CommonTree)match(input, DECIMAL_LITERAL, FOLLOW_DECIMAL_LITERAL_in_literal5173);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.decimalValue((DECIMAL_LITERAL47 != null ? DECIMAL_LITERAL47.getText() : null));

                    }

                }
                break;
                case 4:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:954:5: FLOATING_POINT_LITERAL
                {
                    FLOATING_POINT_LITERAL48 =
                            (CommonTree)match(input, FLOATING_POINT_LITERAL, FOLLOW_FLOATING_POINT_LITERAL_in_literal5197);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.floating_pointValue((FLOATING_POINT_LITERAL48 != null ? FLOATING_POINT_LITERAL48.getText() : null));

                    }

                }
                break;
                case 5:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:958:5: CHARACTER_LITERAL
                {
                    CHARACTER_LITERAL49 = (CommonTree)match(input, CHARACTER_LITERAL, FOLLOW_CHARACTER_LITERAL_in_literal5221);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.charValue((CHARACTER_LITERAL49 != null ? CHARACTER_LITERAL49.getText() : null));

                    }

                }
                break;
                case 6:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:962:5: STRING_LITERAL
                {
                    STRING_LITERAL50 = (CommonTree)match(input, STRING_LITERAL, FOLLOW_STRING_LITERAL_in_literal5245);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.stringValue((STRING_LITERAL50 != null ? STRING_LITERAL50.getText() : null));

                    }

                }
                break;
                case 7:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:966:5: TRUE
                {
                    TRUE51 = (CommonTree)match(input, TRUE, FOLLOW_TRUE_in_literal5269);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.booleanValue((TRUE51 != null ? TRUE51.getText() : null));

                    }

                }
                break;
                case 8:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:970:5: FALSE
                {
                    FALSE52 = (CommonTree)match(input, FALSE, FOLLOW_FALSE_in_literal5293);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.booleanValue((FALSE52 != null ? FALSE52.getText() : null));

                    }

                }
                break;
                case 9:
                    // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:974:5: NULL
                {
                    match(input, NULL, FOLLOW_NULL_in_literal5317);
                    if (state.failed) return value;
                    if (state.backtracking == 0) {

                        value = ev.nullValue();

                    }

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            if (state.backtracking > 0) {
                memoize(input, 71, literal_StartIndex);
            }
        }
        return value;
    }
    // $ANTLR end "literal"

    // $ANTLR start synpred125_JavaTreeParser
    public final void synpred125_JavaTreeParser_fragment() throws RecognitionException {
        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:509:29: ( switchCaseLabel )
        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:509:29: switchCaseLabel
        {
            pushFollow(FOLLOW_switchCaseLabel_in_synpred125_JavaTreeParser1879);
            switchCaseLabel();

            state._fsp--;
            if (state.failed) return;

        }
    }
    // $ANTLR end synpred125_JavaTreeParser

    // $ANTLR start synpred132_JavaTreeParser
    public final void synpred132_JavaTreeParser_fragment() throws RecognitionException {
        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:528:9: ( ( expression )* )
        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:528:9: ( expression )*
        {
            // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:528:9: ( expression )*
            loop143:
            do {
                int alt143 = 2;
                int LA143_0 = input.LA(1);

                if ((LA143_0 == EXPR)) {
                    alt143 = 1;
                }


                switch (alt143) {
                    case 1:
                        // org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g:0:0: expression
                    {
                        pushFollow(FOLLOW_expression_in_synpred132_JavaTreeParser1974);
                        expression();

                        state._fsp--;
                        if (state.failed) return;

                    }
                    break;

                    default:
                        break loop143;
                }
            } while (true);


        }
    }
    // $ANTLR end synpred132_JavaTreeParser

    // Delegated rules

    public final boolean synpred132_JavaTreeParser() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred132_JavaTreeParser_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: " + re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed = false;
        return success;
    }

    public final boolean synpred125_JavaTreeParser() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred125_JavaTreeParser_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: " + re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed = false;
        return success;
    }


    public static final BitSet FOLLOW_JAVA_SOURCE_in_javaSource90                                 =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_annotationList_in_javaSource92                              =
            new BitSet(new long[]{0x2000000000000088L, 0x0000000000106008L});
    public static final BitSet FOLLOW_packageDeclaration_in_javaSource94                          =
            new BitSet(new long[]{0x2000000000000088L, 0x0000000000006008L});
    public static final BitSet FOLLOW_importDeclaration_in_javaSource97                           =
            new BitSet(new long[]{0x2000000000000088L, 0x0000000000006008L});
    public static final BitSet FOLLOW_typeDeclaration_in_javaSource100                            =
            new BitSet(new long[]{0x2000000000000088L, 0x0000000000002008L});
    public static final BitSet FOLLOW_PACKAGE_in_packageDeclaration118                            =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_qualifiedIdentifier_in_packageDeclaration120                =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_IMPORT_in_importDeclaration137                              =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_STATIC_in_importDeclaration139                              =
            new BitSet(new long[]{0x0000000000008000L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_qualifiedIdentifier_in_importDeclaration142                 =
            new BitSet(new long[]{0x0000000000010008L});
    public static final BitSet FOLLOW_DOTSTAR_in_importDeclaration144                             =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CLASS_in_typeDeclaration162                                 =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_modifierList_in_typeDeclaration164                          =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_IDENT_in_typeDeclaration166                                 =
            new BitSet(new long[]{0x0000000000000000L, 0x0800000000000000L, 0x0000000000001401L});
    public static final BitSet FOLLOW_genericTypeParameterList_in_typeDeclaration168              =
            new BitSet(new long[]{0x0000000000000000L, 0x0800000000000000L, 0x0000000000001401L});
    public static final BitSet FOLLOW_extendsClause_in_typeDeclaration171                         =
            new BitSet(new long[]{0x0000000000000000L, 0x0800000000000000L, 0x0000000000001401L});
    public static final BitSet FOLLOW_implementsClause_in_typeDeclaration174                      =
            new BitSet(new long[]{0x0000000000000000L, 0x0800000000000000L, 0x0000000000001401L});
    public static final BitSet FOLLOW_classTopLevelScope_in_typeDeclaration177                    =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_INTERFACE_in_typeDeclaration187                             =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_modifierList_in_typeDeclaration189                          =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_IDENT_in_typeDeclaration191                                 =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000000000C01L});
    public static final BitSet FOLLOW_genericTypeParameterList_in_typeDeclaration193              =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000000000C01L});
    public static final BitSet FOLLOW_extendsClause_in_typeDeclaration196                         =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000000000C01L});
    public static final BitSet FOLLOW_interfaceTopLevelScope_in_typeDeclaration199                =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ENUM_in_typeDeclaration209                                  =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_modifierList_in_typeDeclaration211                          =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_IDENT_in_typeDeclaration213                                 =
            new BitSet(new long[]{0x0000000000000000L, 0x2000000000000000L, 0x0000000000001000L});
    public static final BitSet FOLLOW_implementsClause_in_typeDeclaration215                      =
            new BitSet(new long[]{0x0000000000000000L, 0x2000000000000000L, 0x0000000000001000L});
    public static final BitSet FOLLOW_enumTopLevelScope_in_typeDeclaration218                     =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_AT_in_typeDeclaration228                                    =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_modifierList_in_typeDeclaration230                          =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_IDENT_in_typeDeclaration232                                 =
            new BitSet(new long[]{0x0000000000000000L, 0x0000800000000000L});
    public static final BitSet FOLLOW_annotationTopLevelScope_in_typeDeclaration234               =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_EXTENDS_CLAUSE_in_extendsClause255                          =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_type_in_extendsClause257                                    =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000020000000L});
    public static final BitSet FOLLOW_IMPLEMENTS_CLAUSE_in_implementsClause275                    =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_type_in_implementsClause277                                 =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000020000000L});
    public static final BitSet FOLLOW_GENERIC_TYPE_PARAM_LIST_in_genericTypeParameterList295      =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_genericTypeParameter_in_genericTypeParameterList297         =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_IDENT_in_genericTypeParameter315                            =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_bound_in_genericTypeParameter317                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_EXTENDS_BOUND_LIST_in_bound335                              =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_type_in_bound337                                            =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000020000000L});
    public static final BitSet FOLLOW_ENUM_TOP_LEVEL_SCOPE_in_enumTopLevelScope355                =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_enumConstant_in_enumTopLevelScope357                        =
            new BitSet(new long[]{0x0000000000000008L, 0x0800000000000000L, 0x0000001000001401L});
    public static final BitSet FOLLOW_classTopLevelScope_in_enumTopLevelScope360                  =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_IDENT_in_enumConstant378                                    =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_annotationList_in_enumConstant380                           =
            new BitSet(new long[]{0x0000000000000008L, 0x0801000000000000L, 0x0000000000001401L});
    public static final BitSet FOLLOW_arguments_in_enumConstant382                                =
            new BitSet(new long[]{0x0000000000000008L, 0x0800000000000000L, 0x0000000000001401L});
    public static final BitSet FOLLOW_classTopLevelScope_in_enumConstant385                       =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CLASS_TOP_LEVEL_SCOPE_in_classTopLevelScope403              =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_classScopeDeclarations_in_classTopLevelScope405             =
            new BitSet(new long[]{0x2000000000000088L, 0x1600000000002008L, 0x0000000900000100L});
    public static final BitSet FOLLOW_CLASS_INSTANCE_INITIALIZER_in_classScopeDeclarations423     =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_block_in_classScopeDeclarations425                          =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CLASS_STATIC_INITIALIZER_in_classScopeDeclarations435       =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_block_in_classScopeDeclarations437                          =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_FUNCTION_METHOD_DECL_in_classScopeDeclarations447           =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_modifierList_in_classScopeDeclarations449                   =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000020000400L});
    public static final BitSet FOLLOW_genericTypeParameterList_in_classScopeDeclarations451       =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000020000000L});
    public static final BitSet FOLLOW_type_in_classScopeDeclarations454                           =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_IDENT_in_classScopeDeclarations456                          =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000000000020L});
    public static final BitSet FOLLOW_formalParameterList_in_classScopeDeclarations458            =
            new BitSet(new long[]{0x0000000000000008L, 0x0024000000000000L, 0x0000000010000000L});
    public static final BitSet FOLLOW_arrayDeclaratorList_in_classScopeDeclarations460            =
            new BitSet(new long[]{0x0000000000000008L, 0x0020000000000000L, 0x0000000010000000L});
    public static final BitSet FOLLOW_throwsClause_in_classScopeDeclarations463                   =
            new BitSet(new long[]{0x0000000000000008L, 0x0020000000000000L});
    public static final BitSet FOLLOW_block_in_classScopeDeclarations466                          =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_VOID_METHOD_DECL_in_classScopeDeclarations477               =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_modifierList_in_classScopeDeclarations479                   =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000001000000400L});
    public static final BitSet FOLLOW_genericTypeParameterList_in_classScopeDeclarations481       =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_IDENT_in_classScopeDeclarations484                          =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000000000020L});
    public static final BitSet FOLLOW_formalParameterList_in_classScopeDeclarations486            =
            new BitSet(new long[]{0x0000000000000008L, 0x0020000000000000L, 0x0000000010000000L});
    public static final BitSet FOLLOW_throwsClause_in_classScopeDeclarations488                   =
            new BitSet(new long[]{0x0000000000000008L, 0x0020000000000000L});
    public static final BitSet FOLLOW_block_in_classScopeDeclarations491                          =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_VAR_DECLARATION_in_classScopeDeclarations502                =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_modifierList_in_classScopeDeclarations504                   =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000020000000L});
    public static final BitSet FOLLOW_type_in_classScopeDeclarations506                           =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000400000000L});
    public static final BitSet FOLLOW_variableDeclaratorList_in_classScopeDeclarations508         =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CONSTRUCTOR_DECL_in_classScopeDeclarations518               =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_modifierList_in_classScopeDeclarations520                   =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000000000420L});
    public static final BitSet FOLLOW_genericTypeParameterList_in_classScopeDeclarations522       =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000000000020L});
    public static final BitSet FOLLOW_formalParameterList_in_classScopeDeclarations525            =
            new BitSet(new long[]{0x0000000000000000L, 0x0020000000000000L, 0x0000000010000000L});
    public static final BitSet FOLLOW_throwsClause_in_classScopeDeclarations527                   =
            new BitSet(new long[]{0x0000000000000000L, 0x0020000000000000L});
    public static final BitSet FOLLOW_block_in_classScopeDeclarations530                          =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_typeDeclaration_in_classScopeDeclarations537                =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTERFACE_TOP_LEVEL_SCOPE_in_interfaceTopLevelScope553      =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_interfaceScopeDeclarations_in_interfaceTopLevelScope555     =
            new BitSet(new long[]{0x2000000000000088L, 0x0000000000002008L, 0x0000000900000100L});
    public static final BitSet FOLLOW_FUNCTION_METHOD_DECL_in_interfaceScopeDeclarations573       =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_modifierList_in_interfaceScopeDeclarations575               =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000020000400L});
    public static final BitSet FOLLOW_genericTypeParameterList_in_interfaceScopeDeclarations577   =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000020000000L});
    public static final BitSet FOLLOW_type_in_interfaceScopeDeclarations580                       =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_IDENT_in_interfaceScopeDeclarations582                      =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000000000020L});
    public static final BitSet FOLLOW_formalParameterList_in_interfaceScopeDeclarations584        =
            new BitSet(new long[]{0x0000000000000008L, 0x0004000000000000L, 0x0000000010000000L});
    public static final BitSet FOLLOW_arrayDeclaratorList_in_interfaceScopeDeclarations586        =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000010000000L});
    public static final BitSet FOLLOW_throwsClause_in_interfaceScopeDeclarations589               =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_VOID_METHOD_DECL_in_interfaceScopeDeclarations600           =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_modifierList_in_interfaceScopeDeclarations602               =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000001000000400L});
    public static final BitSet FOLLOW_genericTypeParameterList_in_interfaceScopeDeclarations604   =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_IDENT_in_interfaceScopeDeclarations607                      =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000000000020L});
    public static final BitSet FOLLOW_formalParameterList_in_interfaceScopeDeclarations609        =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000010000000L});
    public static final BitSet FOLLOW_throwsClause_in_interfaceScopeDeclarations611               =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_VAR_DECLARATION_in_interfaceScopeDeclarations631            =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_modifierList_in_interfaceScopeDeclarations633               =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000020000000L});
    public static final BitSet FOLLOW_type_in_interfaceScopeDeclarations635                       =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000400000000L});
    public static final BitSet FOLLOW_variableDeclaratorList_in_interfaceScopeDeclarations637     =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_typeDeclaration_in_interfaceScopeDeclarations644            =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VAR_DECLARATOR_LIST_in_variableDeclaratorList660            =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_variableDeclarator_in_variableDeclaratorList662             =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000200000000L});
    public static final BitSet FOLLOW_VAR_DECLARATOR_in_variableDeclarator680                     =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_variableDeclaratorId_in_variableDeclarator682               =
            new BitSet(new long[]{0x0000000000000008L, 0x4010000000000000L});
    public static final BitSet FOLLOW_variableInitializer_in_variableDeclarator684                =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_IDENT_in_variableDeclaratorId702                            =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_arrayDeclaratorList_in_variableDeclaratorId704              =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_arrayInitializer_in_variableInitializer721                  =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_variableInitializer727                        =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACK_in_arrayDeclarator742                                =
            new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_RBRACK_in_arrayDeclarator744                                =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_DECLARATOR_LIST_in_arrayDeclaratorList760             =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ARRAY_DECLARATOR_in_arrayDeclaratorList762                  =
            new BitSet(new long[]{0x0000000000000008L, 0x0002000000000000L});
    public static final BitSet FOLLOW_ARRAY_INITIALIZER_in_arrayInitializer780                    =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_variableInitializer_in_arrayInitializer782                  =
            new BitSet(new long[]{0x0000000000000008L, 0x4010000000000000L});
    public static final BitSet FOLLOW_THROWS_CLAUSE_in_throwsClause800                            =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_qualifiedIdentifier_in_throwsClause802                      =
            new BitSet(new long[]{0x0000000000008008L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_MODIFIER_LIST_in_modifierList820                            =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_modifier_in_modifierList822                                 =
            new BitSet(new long[]{0x0020000000000088L, 0x000000444CE20040L});
    public static final BitSet FOLLOW_PUBLIC_in_modifier839                                       =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PROTECTED_in_modifier845                                    =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PRIVATE_in_modifier851                                      =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STATIC_in_modifier857                                       =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ABSTRACT_in_modifier863                                     =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NATIVE_in_modifier869                                       =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SYNCHRONIZED_in_modifier875                                 =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRANSIENT_in_modifier881                                    =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VOLATILE_in_modifier887                                     =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRICTFP_in_modifier893                                     =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_localModifier_in_modifier899                                =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LOCAL_MODIFIER_LIST_in_localModifierList915                 =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_localModifier_in_localModifierList917                       =
            new BitSet(new long[]{0x0020000000000088L, 0x000000444CE20040L});
    public static final BitSet FOLLOW_FINAL_in_localModifier934                                   =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotation_in_localModifier940                              =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TYPE_in_type961                                             =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_primitiveType_in_type975                                    =
            new BitSet(new long[]{0x0000000000000008L, 0x0004000000000000L});
    public static final BitSet FOLLOW_qualifiedTypeIdent_in_type985                               =
            new BitSet(new long[]{0x0000000000000008L, 0x0004000000000000L});
    public static final BitSet FOLLOW_arrayDeclaratorList_in_type997                              =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_QUALIFIED_TYPE_IDENT_in_qualifiedTypeIdent1019              =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_typeIdent_in_qualifiedTypeIdent1021                         =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_IDENT_in_typeIdent1039                                      =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_genericTypeArgumentList_in_typeIdent1041                    =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_set_in_primitiveType0                                       =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GENERIC_TYPE_ARG_LIST_in_genericTypeArgumentList1116        =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_genericTypeArgument_in_genericTypeArgumentList1118          =
            new BitSet(new long[]{0x0000010000000008L, 0x0000000000000000L, 0x0000000020000000L});
    public static final BitSet FOLLOW_type_in_genericTypeArgument1135                             =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUESTION_in_genericTypeArgument1144                         =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_genericWildcardBoundType_in_genericTypeArgument1146         =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_EXTENDS_in_genericWildcardBoundType1164                     =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_type_in_genericWildcardBoundType1166                        =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SUPER_in_genericWildcardBoundType1176                       =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_type_in_genericWildcardBoundType1178                        =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_FORMAL_PARAM_LIST_in_formalParameterList1195                =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_formalParameterStandardDecl_in_formalParameterList1197      =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x00000000000000C0L});
    public static final BitSet FOLLOW_formalParameterVarargDecl_in_formalParameterList1200        =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_FORMAL_PARAM_STD_DECL_in_formalParameterStandardDecl1218    =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_localModifierList_in_formalParameterStandardDecl1220        =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000020000000L});
    public static final BitSet FOLLOW_type_in_formalParameterStandardDecl1222                     =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_variableDeclaratorId_in_formalParameterStandardDecl1224     =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_FORMAL_PARAM_VARARG_DECL_in_formalParameterVarargDecl1241   =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_localModifierList_in_formalParameterVarargDecl1243          =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000020000000L});
    public static final BitSet FOLLOW_type_in_formalParameterVarargDecl1245                       =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_variableDeclaratorId_in_formalParameterVarargDecl1247       =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_IDENT_in_qualifiedIdentifier1263                            =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_qualifiedIdentifier1272                              =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_qualifiedIdentifier_in_qualifiedIdentifier1274              =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_IDENT_in_qualifiedIdentifier1276                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ANNOTATION_LIST_in_annotationList1295                       =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_annotation_in_annotationList1297                            =
            new BitSet(new long[]{0x0020000000000088L, 0x000000444CE20040L});
    public static final BitSet FOLLOW_AT_in_annotation1315                                        =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_qualifiedIdentifier_in_annotation1317                       =
            new BitSet(new long[]{0x0000000000000008L, 0x0000020000000000L});
    public static final BitSet FOLLOW_annotationInit_in_annotation1319                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ANNOTATION_INIT_BLOCK_in_annotationInit1337                 =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_annotationInitializers_in_annotationInit1339                =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ANNOTATION_INIT_KEY_LIST_in_annotationInitializers1356      =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_annotationInitializer_in_annotationInitializers1358         =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_ANNOTATION_INIT_DEFAULT_KEY_in_annotationInitializers1369   =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_annotationElementValue_in_annotationInitializers1371        =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_IDENT_in_annotationInitializer1388                          =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_annotationElementValue_in_annotationInitializer1390         =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ANNOTATION_INIT_ARRAY_ELEMENT_in_annotationElementValue1407 =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_annotationElementValue_in_annotationElementValue1409        =
            new BitSet(new long[]{0x0020000000000088L, 0x401001444CE20040L});
    public static final BitSet FOLLOW_annotation_in_annotationElementValue1417                    =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_annotationElementValue1423                    =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ANNOTATION_TOP_LEVEL_SCOPE_in_annotationTopLevelScope1439   =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_annotationScopeDeclarations_in_annotationTopLevelScope1441  =
            new BitSet(new long[]{0x2000000000000088L, 0x0000200000002008L, 0x0000000100000000L});
    public static final BitSet FOLLOW_ANNOTATION_METHOD_DECL_in_annotationScopeDeclarations1459   =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_modifierList_in_annotationScopeDeclarations1461             =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000020000000L});
    public static final BitSet FOLLOW_type_in_annotationScopeDeclarations1463                     =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_IDENT_in_annotationScopeDeclarations1465                    =
            new BitSet(new long[]{0x8000000000000008L});
    public static final BitSet FOLLOW_annotationDefaultValue_in_annotationScopeDeclarations1467   =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_VAR_DECLARATION_in_annotationScopeDeclarations1478          =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_modifierList_in_annotationScopeDeclarations1480             =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000020000000L});
    public static final BitSet FOLLOW_type_in_annotationScopeDeclarations1482                     =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000400000000L});
    public static final BitSet FOLLOW_variableDeclaratorList_in_annotationScopeDeclarations1484   =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_typeDeclaration_in_annotationScopeDeclarations1491          =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DEFAULT_in_annotationDefaultValue1507                       =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_annotationElementValue_in_annotationDefaultValue1509        =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_BLOCK_SCOPE_in_block1528                                    =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_blockStatement_in_block1530                                 =
            new BitSet(new long[]{0x6140100000000088L, 0x4030009161002609L, 0x0000000100002004L});
    public static final BitSet FOLLOW_localVariableDeclaration_in_blockStatement1547              =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_typeDeclaration_in_blockStatement1553                       =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_statement_in_blockStatement1559                             =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VAR_DECLARATION_in_localVariableDeclaration1575             =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_localModifierList_in_localVariableDeclaration1577           =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000020000000L});
    public static final BitSet FOLLOW_type_in_localVariableDeclaration1579                        =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000400000000L});
    public static final BitSet FOLLOW_variableDeclaratorList_in_localVariableDeclaration1581      =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_block_in_statement1597                                      =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASSERT_in_statement1606                                     =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expression_in_statement1608                                 =
            new BitSet(new long[]{0x0000000000000008L, 0x4010000000000000L});
    public static final BitSet FOLLOW_expression_in_statement1610                                 =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_IF_in_statement1621                                         =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_parenthesizedExpression_in_statement1623                    =
            new BitSet(new long[]{0x6140100000000088L, 0x4030009161002609L, 0x0000000100002004L});
    public static final BitSet FOLLOW_statement_in_statement1625                                  =
            new BitSet(new long[]{0x6140100000000088L, 0x4030009161002609L, 0x0000000100002004L});
    public static final BitSet FOLLOW_statement_in_statement1627                                  =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_FOR_in_statement1638                                        =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_forInit_in_statement1640                                    =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000000000002L});
    public static final BitSet FOLLOW_forCondition_in_statement1642                               =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000000000010L});
    public static final BitSet FOLLOW_forUpdater_in_statement1644                                 =
            new BitSet(new long[]{0x6140100000000088L, 0x4030009161002609L, 0x0000000100002004L});
    public static final BitSet FOLLOW_statement_in_statement1646                                  =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_FOR_EACH_in_statement1656                                   =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_localModifierList_in_statement1658                          =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000020000000L});
    public static final BitSet FOLLOW_type_in_statement1660                                       =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_IDENT_in_statement1662                                      =
            new BitSet(new long[]{0x0000000000000000L, 0x4010000000000000L});
    public static final BitSet FOLLOW_expression_in_statement1664                                 =
            new BitSet(new long[]{0x6140100000000088L, 0x4030009161002609L, 0x0000000100002004L});
    public static final BitSet FOLLOW_statement_in_statement1666                                  =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_WHILE_in_statement1676                                      =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_parenthesizedExpression_in_statement1678                    =
            new BitSet(new long[]{0x6140100000000088L, 0x4030009161002609L, 0x0000000100002004L});
    public static final BitSet FOLLOW_statement_in_statement1680                                  =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_DO_in_statement1690                                         =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_statement_in_statement1692                                  =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000000040000L});
    public static final BitSet FOLLOW_parenthesizedExpression_in_statement1694                    =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_TRY_in_statement1704                                        =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_block_in_statement1706                                      =
            new BitSet(new long[]{0x0000000000000008L, 0x00A0000000000000L});
    public static final BitSet FOLLOW_catches_in_statement1708                                    =
            new BitSet(new long[]{0x0000000000000008L, 0x0020000000000000L});
    public static final BitSet FOLLOW_block_in_statement1711                                      =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SWITCH_in_statement1723                                     =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_parenthesizedExpression_in_statement1725                    =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000004000000L});
    public static final BitSet FOLLOW_switchBlockLabels_in_statement1727                          =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SYNCHRONIZED_in_statement1737                               =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_parenthesizedExpression_in_statement1739                    =
            new BitSet(new long[]{0x0000000000000000L, 0x0020000000000000L});
    public static final BitSet FOLLOW_block_in_statement1741                                      =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_RETURN_in_statement1751                                     =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expression_in_statement1753                                 =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_THROW_in_statement1764                                      =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expression_in_statement1766                                 =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_BREAK_in_statement1776                                      =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENT_in_statement1778                                      =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CONTINUE_in_statement1789                                   =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENT_in_statement1791                                      =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_LABELED_STATEMENT_in_statement1802                          =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENT_in_statement1804                                      =
            new BitSet(new long[]{0x6140100000000088L, 0x4030009161002609L, 0x0000000100002004L});
    public static final BitSet FOLLOW_statement_in_statement1806                                  =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_expression_in_statement1813                                 =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SEMI_in_statement1819                                       =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CATCH_CLAUSE_LIST_in_catches1836                            =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_catchClause_in_catches1838                                  =
            new BitSet(new long[]{0x0800000000000008L});
    public static final BitSet FOLLOW_CATCH_in_catchClause1856                                    =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_formalParameterStandardDecl_in_catchClause1858              =
            new BitSet(new long[]{0x0000000000000000L, 0x0020000000000000L});
    public static final BitSet FOLLOW_block_in_catchClause1860                                    =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SWITCH_BLOCK_LABEL_LIST_in_switchBlockLabels1877            =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_switchCaseLabel_in_switchBlockLabels1879                    =
            new BitSet(new long[]{0x8400000000000008L});
    public static final BitSet FOLLOW_switchDefaultLabel_in_switchBlockLabels1882                 =
            new BitSet(new long[]{0x0400000000000008L});
    public static final BitSet FOLLOW_switchCaseLabel_in_switchBlockLabels1885                    =
            new BitSet(new long[]{0x0400000000000008L});
    public static final BitSet FOLLOW_CASE_in_switchCaseLabel1903                                 =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expression_in_switchCaseLabel1905                           =
            new BitSet(new long[]{0x6140100000000088L, 0x4030009161002609L, 0x0000000100002004L});
    public static final BitSet FOLLOW_blockStatement_in_switchCaseLabel1907                       =
            new BitSet(new long[]{0x6140100000000088L, 0x4030009161002609L, 0x0000000100002004L});
    public static final BitSet FOLLOW_DEFAULT_in_switchDefaultLabel1925                           =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_blockStatement_in_switchDefaultLabel1927                    =
            new BitSet(new long[]{0x6140100000000088L, 0x4030009161002609L, 0x0000000100002004L});
    public static final BitSet FOLLOW_FOR_INIT_in_forInit1950                                     =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_localVariableDeclaration_in_forInit1964                     =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_expression_in_forInit1974                                   =
            new BitSet(new long[]{0x0000000000000008L, 0x4010000000000000L});
    public static final BitSet FOLLOW_FOR_CONDITION_in_forCondition2003                           =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expression_in_forCondition2005                              =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_FOR_UPDATE_in_forUpdater2023                                =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expression_in_forUpdater2025                                =
            new BitSet(new long[]{0x0000000000000008L, 0x4010000000000000L});
    public static final BitSet FOLLOW_expression_in_evaluate2048                                  =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PARENTESIZED_EXPR_in_parenthesizedExpression2084            =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expression_in_parenthesizedExpression2086                   =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_EXPR_in_expression2126                                      =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expression2128                                      =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ASSIGN_in_expr2207                                          =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr2211                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr2215                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_PLUS_ASSIGN_in_expr2271                                     =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr2275                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr2279                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_MINUS_ASSIGN_in_expr2307                                    =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr2311                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr2315                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_STAR_ASSIGN_in_expr2343                                     =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr2347                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr2351                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_DIV_ASSIGN_in_expr2379                                      =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr2383                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr2387                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_AND_ASSIGN_in_expr2415                                      =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr2419                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr2423                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_OR_ASSIGN_in_expr2479                                       =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr2483                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr2487                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_XOR_ASSIGN_in_expr2543                                      =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr2547                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr2551                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_MOD_ASSIGN_in_expr2607                                      =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr2611                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr2615                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_BIT_SHIFT_RIGHT_ASSIGN_in_expr2671                          =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr2675                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr2679                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SHIFT_RIGHT_ASSIGN_in_expr2735                              =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr2739                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr2743                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SHIFT_LEFT_ASSIGN_in_expr2799                               =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr2803                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr2807                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_QUESTION_in_expr2863                                        =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr2867                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr2871                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr2875                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_LOGICAL_OR_in_expr2931                                      =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr2935                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr2939                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_LOGICAL_AND_in_expr2995                                     =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr2999                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr3003                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_OR_in_expr3059                                              =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3063                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr3067                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_XOR_in_expr3123                                             =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3127                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr3131                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_AND_in_expr3187                                             =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3191                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr3195                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_EQUAL_in_expr3251                                           =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3255                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr3259                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_NOT_EQUAL_in_expr3315                                       =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3319                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr3323                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_INSTANCEOF_in_expr3379                                      =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3381                                            =
            new BitSet(new long[]{0x0000000000000008L, 0x0000000000000000L, 0x0000000020000000L});
    public static final BitSet FOLLOW_type_in_expr3383                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_LESS_OR_EQUAL_in_expr3439                                   =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3443                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr3447                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_GREATER_OR_EQUAL_in_expr3503                                =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3507                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr3511                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_BIT_SHIFT_RIGHT_in_expr3567                                 =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3571                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr3575                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SHIFT_RIGHT_in_expr3603                                     =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3607                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr3611                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_GREATER_THAN_in_expr3639                                    =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3643                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr3647                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SHIFT_LEFT_in_expr3675                                      =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3679                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr3683                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_LESS_THAN_in_expr3711                                       =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3715                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr3719                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_PLUS_in_expr3747                                            =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3751                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr3755                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_MINUS_in_expr3783                                           =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3787                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr3791                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_STAR_in_expr3819                                            =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3823                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr3827                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_DIV_in_expr3855                                             =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3859                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr3863                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_MOD_in_expr3891                                             =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3895                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr3899                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_UNARY_PLUS_in_expr3927                                      =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3931                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_UNARY_MINUS_in_expr3959                                     =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3963                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_PRE_INC_in_expr3991                                         =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr3995                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_PRE_DEC_in_expr4023                                         =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr4025                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_POST_INC_in_expr4053                                        =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr4057                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_POST_DEC_in_expr4085                                        =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr4087                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_NOT_in_expr4115                                             =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr4119                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_LOGICAL_NOT_in_expr4147                                     =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_expr4151                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CAST_EXPR_in_expr4179                                       =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_type_in_expr4181                                            =
            new BitSet(new long[]{0x001FE1FFDF1CE370L, 0x014A000890081020L, 0x000007F0CB7D0000L});
    public static final BitSet FOLLOW_expr_in_expr4183                                            =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_primaryExpression_in_expr4208                               =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_primaryExpression4302                                =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_primaryExpression_in_primaryExpression4318                  =
            new BitSet(new long[]{0x2000000000000000L, 0x0100000090000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_IDENT_in_primaryExpression4353                              =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_THIS_in_primaryExpression4383                               =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SUPER_in_primaryExpression4413                              =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_innerNewExpression_in_primaryExpression4425                 =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CLASS_in_primaryExpression4485                              =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_primitiveType_in_primaryExpression4503                      =
            new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_CLASS_in_primaryExpression4505                              =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_VOID_in_primaryExpression4515                               =
            new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_CLASS_in_primaryExpression4517                              =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_parenthesizedExpression_in_primaryExpression4534            =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_primaryExpression4558                              =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_METHOD_CALL_in_primaryExpression4585                        =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_primaryExpression_in_primaryExpression4589                  =
            new BitSet(new long[]{0x0000000000000000L, 0x0001000000000000L, 0x0000000000000200L});
    public static final BitSet FOLLOW_genericTypeArgumentList_in_primaryExpression4591            =
            new BitSet(new long[]{0x0000000000000000L, 0x0001000000000000L});
    public static final BitSet FOLLOW_arguments_in_primaryExpression4594                          =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_explicitConstructorCall_in_primaryExpression4618            =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_ELEMENT_ACCESS_in_primaryExpression4645               =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_primaryExpression_in_primaryExpression4649                  =
            new BitSet(new long[]{0x0000000000000000L, 0x4010000000000000L});
    public static final BitSet FOLLOW_expression_in_primaryExpression4653                         =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_literal_in_primaryExpression4677                            =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_newExpression_in_primaryExpression4701                      =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_THIS_in_primaryExpression4725                               =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arrayTypeDeclarator_in_primaryExpression4749                =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUPER_in_primaryExpression4755                              =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_THIS_CONSTRUCTOR_CALL_in_explicitConstructorCall4789        =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_genericTypeArgumentList_in_explicitConstructorCall4791      =
            new BitSet(new long[]{0x0000000000000000L, 0x0001000000000000L});
    public static final BitSet FOLLOW_arguments_in_explicitConstructorCall4794                    =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SUPER_CONSTRUCTOR_CALL_in_explicitConstructorCall4804       =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_primaryExpression_in_explicitConstructorCall4806            =
            new BitSet(new long[]{0x0000000000000000L, 0x0001000000000000L, 0x0000000000000200L});
    public static final BitSet FOLLOW_genericTypeArgumentList_in_explicitConstructorCall4809      =
            new BitSet(new long[]{0x0000000000000000L, 0x0001000000000000L});
    public static final BitSet FOLLOW_arguments_in_explicitConstructorCall4812                    =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ARRAY_DECLARATOR_in_arrayTypeDeclarator4834                 =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_arrayTypeDeclarator_in_arrayTypeDeclarator4848              =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_qualifiedIdentifier_in_arrayTypeDeclarator4858              =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_primitiveType_in_arrayTypeDeclarator4868                    =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_STATIC_ARRAY_CREATOR_in_newExpression4900                   =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_primitiveType_in_newExpression4914                          =
            new BitSet(new long[]{0x0000000000000000L, 0x4014000000000000L});
    public static final BitSet FOLLOW_newArrayConstruction_in_newExpression4916                   =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_genericTypeArgumentList_in_newExpression4926                =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000000800000L});
    public static final BitSet FOLLOW_qualifiedTypeIdent_in_newExpression4929                     =
            new BitSet(new long[]{0x0000000000000000L, 0x4014000000000000L});
    public static final BitSet FOLLOW_newArrayConstruction_in_newExpression4931                   =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CLASS_CONSTRUCTOR_CALL_in_newExpression4951                 =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_genericTypeArgumentList_in_newExpression4953                =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000000000800000L});
    public static final BitSet FOLLOW_qualifiedTypeIdent_in_newExpression4956                     =
            new BitSet(new long[]{0x0000000000000000L, 0x0001000000000000L});
    public static final BitSet FOLLOW_arguments_in_newExpression4958                              =
            new BitSet(new long[]{0x0000000000000008L, 0x0800000000000000L, 0x0000000000001401L});
    public static final BitSet FOLLOW_classTopLevelScope_in_newExpression4960                     =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CLASS_CONSTRUCTOR_CALL_in_innerNewExpression4979            =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_genericTypeArgumentList_in_innerNewExpression4981           =
            new BitSet(new long[]{0x0000000000000000L, 0x0000000000000000L, 0x0000001000000000L});
    public static final BitSet FOLLOW_IDENT_in_innerNewExpression4984                             =
            new BitSet(new long[]{0x0000000000000000L, 0x0001000000000000L});
    public static final BitSet FOLLOW_arguments_in_innerNewExpression4986                         =
            new BitSet(new long[]{0x0000000000000008L, 0x0800000000000000L, 0x0000000000001401L});
    public static final BitSet FOLLOW_classTopLevelScope_in_innerNewExpression4988                =
            new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_arrayDeclaratorList_in_newArrayConstruction5005             =
            new BitSet(new long[]{0x0000000000000000L, 0x0010000000000000L});
    public static final BitSet FOLLOW_arrayInitializer_in_newArrayConstruction5007                =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_newArrayConstruction5013                      =
            new BitSet(new long[]{0x0000000000000002L, 0x4014000000000000L});
    public static final BitSet FOLLOW_arrayDeclaratorList_in_newArrayConstruction5016             =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARGUMENT_LIST_in_arguments5060                              =
            new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expression_in_arguments5076                                 =
            new BitSet(new long[]{0x0000000000000008L, 0x4010000000000000L});
    public static final BitSet FOLLOW_HEX_LITERAL_in_literal5125                                  =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OCTAL_LITERAL_in_literal5149                                =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_LITERAL_in_literal5173                              =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOATING_POINT_LITERAL_in_literal5197                       =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHARACTER_LITERAL_in_literal5221                            =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_literal5245                               =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_literal5269                                         =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_literal5293                                        =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NULL_in_literal5317                                         =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_switchCaseLabel_in_synpred125_JavaTreeParser1879            =
            new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_synpred132_JavaTreeParser1974                 =
            new BitSet(new long[]{0x0000000000000002L, 0x4010000000000000L});

}
