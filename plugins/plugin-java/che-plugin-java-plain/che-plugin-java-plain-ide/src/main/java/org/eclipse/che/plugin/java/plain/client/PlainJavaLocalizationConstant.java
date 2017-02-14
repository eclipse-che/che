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
package org.eclipse.che.plugin.java.plain.client;

import com.google.gwt.i18n.client.Messages;

/**
 * Localization constants. Interface to represent the constants defined in resource bundle:
 * 'PlainJavaLocalizationConstant.properties'.
 *
 * @author Valeriy Svydenko
 */
public interface PlainJavaLocalizationConstant extends Messages {
    @Key("browse.button.name")
    String browseButton();

    @Key("source.folder.attribute.label.name")
    String sourceFolderAttribute();

    @Key("library.folder.attribute.label.name")
    String libraryFolderAttribute();
}
