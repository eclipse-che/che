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
package org.eclipse.che.ide.ext.help.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.ext.help.client.BuildDetailsProvider;
import org.eclipse.che.ide.ext.help.client.about.AboutView;
import org.eclipse.che.ide.ext.help.client.about.AboutViewImpl;
import org.eclipse.che.ide.ext.help.client.about.impl.FormattedBuildDetailsProvider;
import org.eclipse.che.ide.ext.help.client.about.info.BuildDetailsView;
import org.eclipse.che.ide.ext.help.client.about.info.BuildDetailsViewImpl;

/** @author Vitalii Parfonov */
@ExtensionGinModule
public class HelpAboutGinModule extends AbstractGinModule {
  /** {@inheritDoc} */
  @Override
  protected void configure() {
    bind(AboutView.class).to(AboutViewImpl.class);
    bind(BuildDetailsView.class).to(BuildDetailsViewImpl.class);
    bind(BuildDetailsProvider.class).to(FormattedBuildDetailsProvider.class);
  }
}
