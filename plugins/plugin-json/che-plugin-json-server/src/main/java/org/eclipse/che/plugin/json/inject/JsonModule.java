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
package org.eclipse.che.plugin.json.inject;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.json.languageserver.JsonLanguageServerLauncher;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;

/**
 * @author Anatolii Bazko
 */
@DynaModule
public class JsonModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), LanguageServerLauncher.class).addBinding().to(JsonLanguageServerLauncher.class);
    }
}
