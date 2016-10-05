/*******************************************************************************
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend Technologies - initial API and implementation
 *******************************************************************************/
package zend.com.che.plugin.zdb.ide;

import com.google.gwt.resources.client.ClientBundle;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Zend debugger resources.
 * 
 * @author Bartlomiej Laczkowski
 */
public interface ZendDebuggerResources extends ClientBundle {

    /** Returns the icon for PHP debug configuration type. */
    @Source("configuration/zend-debug-configuration-type.svg")
    SVGResource zendDebugConfigurationType();
    
}
