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
