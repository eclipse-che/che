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
package org.eclipse.che.ide.ext.java.client.tree;

import static org.eclipse.che.ide.api.resources.Resource.FOLDER;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.marker.Marker;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.shared.ContentRoot;
import org.eclipse.che.ide.project.node.icon.NodeIconProvider;
import org.eclipse.che.ide.ui.smartTree.data.settings.SettingsProvider;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Vlad Zhukovskiy */
@Beta
public class SourceFolderDecorator implements NodeIconProvider {

  protected final PromiseProvider promises;
  protected final JavaNodeFactory nodeFactory;
  protected final SettingsProvider settingsProvider;
  private final JavaResources javaResources;

  @Inject
  public SourceFolderDecorator(
      PromiseProvider promises,
      JavaNodeFactory nodeFactory,
      SettingsProvider settingsProvider,
      JavaResources javaResources) {
    this.promises = promises;
    this.nodeFactory = nodeFactory;
    this.settingsProvider = settingsProvider;
    this.javaResources = javaResources;
  }

  @Override
  public SVGResource getIcon(Resource resource) {
    if (resource.getResourceType() != FOLDER) {
      return null;
    }

    final Optional<Marker> srcMarker = resource.getMarker(SourceFolderMarker.ID);

    if (srcMarker.isPresent()) {
      final ContentRoot contentRoot = ((SourceFolderMarker) srcMarker.get()).getContentRoot();

      switch (contentRoot) {
        case SOURCE:
          return javaResources.sourceFolder();
        case TEST_SOURCE:
          return javaResources.testFolder();
        default:
          return null;
      }
    }

    return null;
  }
}
