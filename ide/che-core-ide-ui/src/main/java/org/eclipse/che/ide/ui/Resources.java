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
package org.eclipse.che.ide.ui;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * @author Vitaly Parfonov
 */
public interface Resources extends com.google.gwt.resources.client.ClientBundle {

    @Source("logo/che-logo.svg")
    SVGResource logo();

    @Source({"Styles.css", "org/eclipse/che/ide/api/ui/style.css"})
    Styles styles();
}
