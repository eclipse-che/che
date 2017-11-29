/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.optimized.testing.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.plugin.optimized.testing.ide.preference.SmartTestingExperimentalFeature;
import org.eclipse.che.plugin.optimized.testing.ide.view.OptimizedTestResultViewImpl;
import org.eclipse.che.plugin.testing.ide.view.TestResultViewImpl;

@ExtensionGinModule
public class OptimizedTestingGinModule extends AbstractGinModule {
  /** {@inheritDoc} */
  @Override
  protected void configure() {
    bind(TestResultViewImpl.class).to(OptimizedTestResultViewImpl.class);

    GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class)
        .addBinding()
        .to(SmartTestingExperimentalFeature.class);
  }
}
