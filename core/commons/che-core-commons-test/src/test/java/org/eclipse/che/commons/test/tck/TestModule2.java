/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.test.tck;

import static org.eclipse.che.commons.test.tck.DBServerListener.DB_SERVER_URL_ATTRIBUTE_NAME;

import org.eclipse.che.commons.test.tck.TckComponentsTest.DBUrlProvider;

/** @author Yevhenii Voevodin */
public class TestModule2 extends TckModule {

  @Override
  public void configure() {
    bind(DBUrlProvider.class)
        .toInstance(() -> getTestContext().getAttribute(DB_SERVER_URL_ATTRIBUTE_NAME).toString());
  }
}
