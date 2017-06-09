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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.plugin.composer.ide.communication.ComposerOutputHandler;

/**
 * @author Kaloyan Raev
 */
@Singleton
@Extension(title = "Composer")
public class ComposerExtension {

    @Inject
    public ComposerExtension(ComposerOutputHandler composerOutputHandler) {
    }

}
