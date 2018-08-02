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
package org.eclipse.che.ide.project.node.icon;

import com.google.gwt.resources.client.ClientBundle;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.resources.Resource;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Icon provider for Docker file
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class DockerfileIconProvider implements NodeIconProvider {

  private Icons icons;

  @Inject
  public DockerfileIconProvider(Icons icons) {
    this.icons = icons;
  }

  @Override
  public SVGResource getIcon(Resource resource) {
    return "Dockerfile".equals(resource.getName()) ? icons.dockerfile() : null;
  }

  protected interface Icons extends ClientBundle {
    @Source("dockerfile.svg")
    SVGResource dockerfile();
  }
}
