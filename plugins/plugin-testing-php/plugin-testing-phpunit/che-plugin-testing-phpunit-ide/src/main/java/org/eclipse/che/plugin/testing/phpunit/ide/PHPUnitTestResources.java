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
package org.eclipse.che.plugin.testing.phpunit.ide;

import org.vectomatic.dom.svg.ui.SVGResource;

import com.google.gwt.resources.client.ClientBundle;

/**
 * PHPUnit ide part resources.
 *
 * @author Bartlomiej Laczkowski
 */
public interface PHPUnitTestResources extends ClientBundle {

    @Source("org/eclipse/che/plugin/testing/phpunit/ide/svg/test.svg")
    SVGResource testIcon();

}
