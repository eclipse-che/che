/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.testing.phpunit.ide;

import com.google.gwt.resources.client.ClientBundle;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * PHPUnit ide part resources.
 *
 * @author Bartlomiej Laczkowski
 */
public interface PHPUnitTestResources extends ClientBundle {

  @Source("org/eclipse/che/plugin/testing/phpunit/ide/svg/test.svg")
  SVGResource testIcon();
}
