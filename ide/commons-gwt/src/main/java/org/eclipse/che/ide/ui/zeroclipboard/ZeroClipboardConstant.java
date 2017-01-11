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
package org.eclipse.che.ide.ui.zeroclipboard;

import com.google.gwt.i18n.client.Messages;

/**
 * Tooltip messages interface for the ZeroClipboard lib.
 *
 * @author Oleksii Orel
 */
public interface ZeroClipboardConstant extends Messages {
        /* Prompts */
    @Key("prompt.readyToCopy")
    String promptReadyToCopy();

    @Key("prompt.afterCopy")
    String promptAfterCopy();

    @Key("prompt.copyError")
    String promptCopyError();

    @Key("prompt.readyToSelect")
    String promptReadyToSelect();
}
