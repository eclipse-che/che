/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.testing.ide.messages;

import org.eclipse.che.ide.util.loging.Log;

/** Data class represents test count. */
public class TestCount extends ClientTestingMessage {

  TestCount() {}

  @Override
  public void visit(TestingMessageVisitor visitor) {
    visitor.visitTestCount(this);
  }

  public Integer getCount() {
    int result = 0;
    try {
      result = Integer.parseInt(getAttributeValue("count"));
    } catch (NumberFormatException e) {
      Log.error(getClass(), e.getMessage(), e);
    }
    return result;
  }
}
