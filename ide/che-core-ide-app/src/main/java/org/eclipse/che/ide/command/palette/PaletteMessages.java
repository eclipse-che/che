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
package org.eclipse.che.ide.command.palette;

import com.google.gwt.i18n.client.Messages;

/**
 * I18n messages for the Commands Palette.
 *
 * @author Artem Zatsarynnyi
 */
public interface PaletteMessages extends Messages {

    @Key("action.show_palette.title")
    String actionShowPaletteTitle();

    @Key("action.show_palette.description")
    String actionShowPaletteDescription();

    @Key("view.title")
    String viewTitle();

    @Key("view.filter.placeholder")
    String filterPlaceholder();

    @Key("view.hint.text")
    String viewHintText();

    @Key("message.no_machine")
    String messageNoMachine();
}
