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
package org.eclipse.che.ide.project.node.icon;

import com.google.gwt.resources.client.ClientBundle;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.resources.Resource;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Icon provider for Docker file
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
