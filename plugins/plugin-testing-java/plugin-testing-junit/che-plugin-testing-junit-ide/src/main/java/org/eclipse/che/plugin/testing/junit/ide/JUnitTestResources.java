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
package org.eclipse.che.plugin.testing.junit.ide;

import org.vectomatic.dom.svg.ui.SVGResource;

import com.google.gwt.resources.client.ClientBundle;
/**
 *
 * @author Mirage Abeysekara
 */
public interface JUnitTestResources extends ClientBundle {

    @Source("org/eclipse/che/plugin/testing/junit/ide/svg/test.svg")
    SVGResource testIcon();

    @Source("org/eclipse/che/plugin/testing/junit/ide/svg/test_all.svg")
    SVGResource testAllIcon();

}
