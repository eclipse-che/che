/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.zdb.ide;

import org.vectomatic.dom.svg.ui.SVGResource;

import com.google.gwt.resources.client.ClientBundle;

/**
 * Zend debugger SVG resources.
 *
 * @author Bartlomiej Laczkowski
 */
public interface ZendDbgResources extends ClientBundle {

    /** Returns the icon for PHP debug configuration type. */
    @Source("configuration/zend-dbg-configuration-type.svg")
    SVGResource zendDbgConfigurationType();

}
