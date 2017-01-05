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
package org.eclipse.che.api.core.util;

import java.io.IOException;

/**
 * No-op implementation of {@link MessageConsumer}
 *
 * @author Alexander Garagatyi
 */
public class AbstractMessageConsumer<T> implements MessageConsumer<T> {
    @Override
    public void consume(T message) throws IOException {}

    @Override
    public void close() throws IOException {}
}
