/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.composer.ide;

import com.google.gwt.i18n.client.Messages;

/**
 * Interface providing access to localization constants.
 *
 * @author Kaloyan Raev
 */
public interface ComposerLocalizationConstants extends Messages {

    @Key("importer.packageName")
    String projectImporterPackageName();

    @Key("importer.packageNameInvalid")
    String projectImporterPackageNameInvalid();

    @Key("importer.packageNameStartWithWhiteSpace")
    String projectImporterPackageNameStartWithWhiteSpace();

}
