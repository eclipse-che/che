/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.debug.shared.model.impl;

import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.che.api.debug.shared.model.WatchExpression;

/**
 * Implementation {@link WatchExpression}
 *
 * @author Oleksandr Andriienko
 */
public class WatchExpressionImpl implements WatchExpression {

  private static final AtomicLong KEY_COUNTER = new AtomicLong();

  private final String key;
  private String expression;
  private String result;

  public WatchExpressionImpl(String expression, String result) {
    this.expression = expression;
    this.result = result;
    this.key = String.valueOf(KEY_COUNTER.incrementAndGet());
  }

  public WatchExpressionImpl(String expression) {
    this(expression, "");
  }

  public String getKey() {
    return key;
  }

  @Override
  public String getExpression() {
    return expression;
  }

  @Override
  public void setExpression(String expression) {
    this.expression = expression;
  }

  @Override
  public String getResult() {
    return result;
  }

  @Override
  public void setResult(String result) {
    this.result = result;
  }
}
