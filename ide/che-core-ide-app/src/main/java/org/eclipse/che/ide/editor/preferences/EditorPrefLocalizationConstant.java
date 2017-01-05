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
package org.eclipse.che.ide.editor.preferences;

import com.google.gwt.i18n.client.Messages;

/**
 * I18n Constants for the preference window.
 *
 * @author "Mickaël Leduque"
 */
public interface EditorPrefLocalizationConstant extends Messages {

    @Key("editortype.title")
    String editorTypeTitle();

    @Key("editortype.category")
    String editorTypeCategory();

    @DefaultMessage("Keys")
    String keysSectionLabel();

    @DefaultMessage("Key Bindings:")
    String keybindingsLabel();
}
