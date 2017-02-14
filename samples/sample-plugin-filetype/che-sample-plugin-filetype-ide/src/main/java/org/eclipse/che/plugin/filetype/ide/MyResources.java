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
package org.eclipse.che.plugin.filetype.ide;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Resources that contain a custom icon that is used for the "MyFileType".
 *
 * @author Edgar Mueller
 */
public interface MyResources extends ClientBundle {

    MyResources INSTANCE = GWT.create(MyResources.class);

    @Source("icons/my.svg")
    SVGResource icon();
}
