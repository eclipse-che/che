/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.client.comunnication.progressor.background;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.plugin.maven.client.MavenLocalizationConstant;

/**
 * Action which shows the process of resolving Maven dependencies.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class DependencyResolverAction extends BaseAction implements CustomComponentAction {
  private final BackgroundLoaderPresenter dependencyResolver;

  @Inject
  public DependencyResolverAction(
      BackgroundLoaderPresenter dependencyResolver, MavenLocalizationConstant locale) {
    super(locale.loaderActionName(), locale.loaderActionDescription());
    this.dependencyResolver = dependencyResolver;

    dependencyResolver.hide();
  }

  @Override
  public void actionPerformed(ActionEvent e) {}

  @Override
  public Widget createCustomComponent(Presentation presentation) {
    return dependencyResolver.getCustomComponent();
  }
}
