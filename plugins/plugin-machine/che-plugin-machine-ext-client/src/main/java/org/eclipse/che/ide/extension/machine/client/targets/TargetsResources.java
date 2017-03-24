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
package org.eclipse.che.ide.extension.machine.client.targets;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import org.vectomatic.dom.svg.ui.SVGResource;


/**
 * Class contains references to resources which need to correct displaying of targets dialog.
 *
 * @author Oleksii Orel
 */
public interface TargetsResources extends ClientBundle {

    interface EditTargetsStyles extends CssResource {

        String categoryHeader();

        String categorySubElementHeader();

        String buttonArea();

        String running();
    }

    @Source({"TargetRenderer.css", "org/eclipse/che/ide/api/ui/style.css"})
    EditTargetsStyles getCss();

    @Source("add-command-button.svg")
    SVGResource addCommandButton();

    @Source("remove-command-button.svg")
    SVGResource removeCommandButton();
}
