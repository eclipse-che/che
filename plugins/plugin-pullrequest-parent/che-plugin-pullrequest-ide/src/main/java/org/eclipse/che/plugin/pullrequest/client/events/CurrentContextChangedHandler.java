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
package org.eclipse.che.plugin.pullrequest.client.events;

import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import com.google.gwt.event.shared.EventHandler;

/**
 * Handler for {@link CurrentContextChangedEvent}.
 *
 * @author Yevhenii Voevodin
 */
public interface CurrentContextChangedHandler extends EventHandler {

    /**
     * Called when the current context changed.
     *
     * @param context
     *         new context
     */
    void onContextChanged(final Context context);
}
