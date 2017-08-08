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
package org.eclipse.che.ide.formatter;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.ide.formatter.preferences.FormatterPreferencePagePresenter;
import org.eclipse.che.ide.formatter.preferences.FormatterPreferencePageView;
import org.eclipse.che.ide.formatter.preferences.FormatterPreferencePageViewImpl;

import javax.inject.Singleton;


public class FormatterGinModule extends AbstractGinModule {
    @Override
    protected void configure() {
        bind(FormatterPreferencePageView.class).to(FormatterPreferencePageViewImpl.class).in(Singleton.class);
        final GinMultibinder<PreferencePagePresenter> prefBinder = GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class);
        prefBinder.addBinding().to(FormatterPreferencePagePresenter.class);
    }
}
