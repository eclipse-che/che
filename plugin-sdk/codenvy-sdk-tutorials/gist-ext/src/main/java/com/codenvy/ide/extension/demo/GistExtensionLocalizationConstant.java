/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.extension.demo;

import com.google.gwt.i18n.client.Messages;

/** Interface to represent the constants contained in resource bundle: 'GistExtensionLocalizationConstant.properties'. */
public interface GistExtensionLocalizationConstant extends Messages {
    /* Buttons */
    @Key("button.cancel")
    String buttonCancel();

    @Key("button.create")
    String buttonCreate();

    /* Actions */
    @Key("control.createGist.id")
    String createGistActionlId();

    @Key("control.createGist.text")
    String createGistActionText();

    @Key("control.createGist.description")
    String createGistActionDescription();

    /* Messages */
    @Key("messages.openFileToCreateGist")
    String openFileToCreateGist();

    @Key("messages.projectIsNotOpened")
    String projectIsNotOpened();

    @Key("messages.createGistError")
    String createGistError();

    @Key("messages.detectGistIdError")
    String detectGistIdError();

    /* CreateGistView */
    @Key("createGist.title")
    String createViewTitle();

    @Key("createGist.publicFieldTitle")
    String publicFieldTitle();

    @Key("createGist.snippetFieldTitle")
    String snippetFieldTitle();
}