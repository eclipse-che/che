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

/** @author andrew00x */
public abstract class ExpressionParser {
    private final String expression;

    protected ExpressionParser(String expression) {
        this.expression = expression;
    }

    /**
     * Create new instance of parser for specified Java expression.
     *
     * @param expression
     *         Java language expression
     * @return concrete implementation of ExpressionParser
     */
    public static ExpressionParser newInstance(String expression) {
        // At the moment create instance of ANTLRExpressionParser directly.
        return new ANTLRExpressionParser(expression);
    }

    /**
     * Get expression for this parser.
     *
     * @return expression
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Evaluate expression.
     *
     * @param ev
     *         Evaluator
     * @return result of evaluation
     * @throws ExpressionException
     *         if specified expression is invalid or another error occurs when try to evaluate expression
     */
    public abstract com.sun.jdi.Value evaluate(Evaluator ev);
}
