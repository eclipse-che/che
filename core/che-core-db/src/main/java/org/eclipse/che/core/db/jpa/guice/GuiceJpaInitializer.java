/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.core.db.jpa.guice;

import com.google.inject.persist.PersistService;

import org.eclipse.che.core.db.jpa.JpaInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Should be bound as eager singleton.
 * See <a href="https://github.com/google/guice/wiki/JPA">doc</a>
 *
 * @author Yevhenii Voevodin
 * @author Anton Korneta
 */
public class GuiceJpaInitializer implements JpaInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(GuiceJpaInitializer.class);

    @Inject
    private PersistService persistService;

    public void init() {
        try {
            persistService.start();
        } catch (Exception x) {
            LOG.error(x.getLocalizedMessage(), x);
        }
    }
}
