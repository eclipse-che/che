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
package org.eclipse.che.plugin.maven.client.comunnication.progressor.background;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.plugin.maven.client.MavenLocalizationConstant;

/**
 * Action which shows the process of resolving Maven dependencies.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class DependencyResolverAction extends Action implements CustomComponentAction {
    private final BackgroundLoaderPresenter dependencyResolver;

    @Inject
    public DependencyResolverAction(BackgroundLoaderPresenter dependencyResolver, MavenLocalizationConstant locale) {
        super(locale.loaderActionName(), locale.loaderActionDescription());
        this.dependencyResolver = dependencyResolver;

        dependencyResolver.hide();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public Widget createCustomComponent(Presentation presentation) {
        return dependencyResolver.getCustomComponent();
    }

}
