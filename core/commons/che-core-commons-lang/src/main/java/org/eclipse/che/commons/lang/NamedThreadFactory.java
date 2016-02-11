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
package org.eclipse.che.commons.lang;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @deprecated
 * Use guava ThreadFactoryBuilder instead
 */
@Deprecated
public class NamedThreadFactory implements ThreadFactory {
    private static final AtomicLong threadPoolNumGen = new AtomicLong();

    private final String  namePrefix;
    private final boolean daemon;

    public NamedThreadFactory(String namePrefix, boolean daemon) {
        if (namePrefix == null) {
            throw new IllegalArgumentException();
        }
        this.namePrefix = namePrefix;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        final Thread t = new Thread(r, namePrefix + threadPoolNumGen.getAndIncrement());
        if (daemon) {
            t.setDaemon(true);
        }
        return t;
    }
}
