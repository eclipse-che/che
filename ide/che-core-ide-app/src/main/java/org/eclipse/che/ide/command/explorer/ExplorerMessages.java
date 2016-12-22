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
package org.eclipse.che.ide.command.explorer;

import com.google.gwt.i18n.client.Messages;

/**
 * I18n messages for the Command Explorer.
 *
 * @author Artem Zatsarynnyi
 */
public interface ExplorerMessages extends Messages {

    @Key("explorer.part.title")
    String explorerPartTitle();

    @Key("explorer.part.tooltip")
    String explorerPartTooltip();

    @Key("explorer.view.title")
    String explorerViewTitle();

    @Key("explorer.message.unable_create")
    String explorerMessageUnableCreate();

    @Key("explorer.message.unable_duplicate")
    String explorerMessageUnableDuplicate();

    @Key("explorer.message.unable_remove")
    String explorerMessageUnableRemove();
}
