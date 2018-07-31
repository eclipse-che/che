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
package org.eclipse.che.core.db.jpa.guice;

import com.google.inject.persist.PersistService;
import javax.inject.Inject;
import org.eclipse.che.core.db.jpa.JpaInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Should be bound as eager singleton. See <a
 * href="https://github.com/google/guice/wiki/JPA">doc</a>
 *
 * @author Yevhenii Voevodin
 * @author Anton Korneta
 */
public class GuiceJpaInitializer implements JpaInitializer {

  private static final Logger LOG = LoggerFactory.getLogger(GuiceJpaInitializer.class);

  @Inject private PersistService persistService;

  public void init() {
    try {
      persistService.start();
    } catch (Exception x) {
      LOG.error(x.getLocalizedMessage(), x);
    }
  }
}
