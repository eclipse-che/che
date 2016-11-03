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
package org.eclipse.che.commons.test.tck;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

/**
 * This class is designed to close {@link EntityManagerFactory}
 * on finish of tck test with jpa implementation.
 *
 * <p/>
 * Examples of usage:<br>
 * <code>bind(TckResourcesCleaner.class).to(JpaCleaner.class)</code><br>
 * <code>bind(TckResourcesCleaner.class).annotatedWith(Names.named(MyTckTest.class.getName())).to(JpaCleaner.class);</code>
 *
 * @author Sergii Leschenko
 */
public class JpaCleaner implements TckResourcesCleaner {
    @Inject
    private EntityManagerFactory entityManagerFactory;

    @Override
    public void clean() {
        entityManagerFactory.close();
    }
}
