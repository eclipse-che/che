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
package org.eclipse.che.ide.extension.machine.client.command.producer;

import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;

/**
 * Can produce commands from the current context.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandProducer {

    /** Returns the text that will be used as related action's title. */
    String getName();

    /** Whether the command produced by concrete producer is applicable to the current context? */
    boolean isApplicable();

    /** Creates command from the current context. */
    CommandConfiguration createCommand();
}
