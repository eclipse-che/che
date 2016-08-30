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
package org.eclipse.che.ide.ext.java.client.command.valueproviders;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.ext.java.client.command.JavaCommandConfiguration;
import org.eclipse.che.ide.ext.java.client.command.JavaCommandPagePresenter;
import org.eclipse.che.ide.api.machine.CommandPropertyValueProvider;

/**
 * Provides a path to the Main class.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class MainClassProvider implements CommandPropertyValueProvider {
    private static final String KEY = "${java.main.class}";

    private final JavaCommandPagePresenter javaCommandPagePresenter;

    @Inject
    public MainClassProvider(JavaCommandPagePresenter javaCommandPagePresenter) {
        this.javaCommandPagePresenter = javaCommandPagePresenter;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public Promise<String> getValue() {
        JavaCommandConfiguration commandConfiguration = javaCommandPagePresenter.getConfiguration();

        return commandConfiguration == null ? Promises.resolve("") : Promises.resolve(commandConfiguration.getMainClass());
    }

}
