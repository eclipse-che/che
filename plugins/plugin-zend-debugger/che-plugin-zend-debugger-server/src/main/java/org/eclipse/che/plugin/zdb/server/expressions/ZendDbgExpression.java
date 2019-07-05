/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.server.expressions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract implementation of Zend debug expression.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgExpression implements IDbgExpression {

  private final String expression;
  private final List<String> expressionChain;
  private final ZendDbgExpressionEvaluator expressionEvaluator;
  private final Set<Facet> expressionFacets = new HashSet<Facet>();
  private ZendDbgExpressionResult expressionResult;

  public ZendDbgExpression(
      ZendDbgExpressionEvaluator expressionEvaluator,
      String expression,
      List<String> expressionChain,
      Facet... facets) {
    this.expressionEvaluator = expressionEvaluator;
    this.expression = expression;
    this.expressionChain = expressionChain;
    this.expressionResult = ZendDbgExpressionResult.NULL;
    addFacets(facets);
  }

  @Override
  public String getExpression() {
    return expression;
  }

  @Override
  public List<String> getExpressionChain() {
    return expressionChain;
  }

  @Override
  public int getChildrenCount() {
    return expressionResult.getChildrenCount();
  }

  @Override
  public List<IDbgExpression> getChildren() {
    return expressionResult.getChildren();
  }

  @Override
  public String getValue() {
    return expressionResult.getValue();
  }

  @Override
  public boolean setValue(String value) {
    return expressionEvaluator.assign(this, value, 1);
  }

  @Override
  public void evaluate() {
    expressionEvaluator.evaluate(this, 1);
  }

  @Override
  public boolean hasFacet(Facet facet) {
    return expressionFacets.contains(facet);
  }

  @Override
  public void addFacets(Facet... facets) {
    for (Facet facet : facets) this.expressionFacets.add(facet);
  }

  @Override
  public DataType getDataType() {
    return expressionResult.getDataType();
  }

  protected ZendDbgExpressionEvaluator getExpressionEvaluator() {
    return expressionEvaluator;
  }

  protected void setExpressionResult(ZendDbgExpressionResult expressionResult) {
    this.expressionResult = expressionResult;
  }

  protected ZendDbgExpression createChild(String expression, Facet... facets) {
    List<String> chain = new ArrayList<>(getExpressionChain());
    chain.add(expression);
    return new ZendDbgExpression(expressionEvaluator, expression, chain, facets);
  }
}
