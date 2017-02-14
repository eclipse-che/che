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
package org.eclipse.che.plugin.jdb.server.expression;

import com.sun.jdi.Value;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTreeNodeStream;

/**
 * ANTLR based implementation of ExpressionParser.
 *
 * @author andrew00x
 */
public final class ANTLRExpressionParser extends ExpressionParser {
    private CommonTreeNodeStream nodes;

    public ANTLRExpressionParser(String expression) {
        super(expression);
    }

    @Override
    public Value evaluate(Evaluator ev) {
        try {
            if (nodes == null) {
                parse();
            } else {
                nodes.reset();
            }
            JavaTreeParser walker = new JavaTreeParser(nodes, ev);
            return walker.evaluate();
        } catch (RecognitionException e) {
            throw new ExpressionException(e.getMessage(), e);
        }
    }

    private void parse() throws RecognitionException {
        JavaLexer lexer = new JavaLexer(new ANTLRStringStream(getExpression()));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        nodes = new CommonTreeNodeStream(parser.expression().getTree());
    }
}
