/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.optimized.testing.ide;

import com.google.gwt.resources.client.ClientBundle;
import org.vectomatic.dom.svg.ui.SVGResource;

/** Resources for optimized test extension. */
public interface TestResources extends ClientBundle {

  @Source("org/eclipse/che/plugin/testing/ide/svg/test.svg")
  @Deprecated
  SVGResource testIcon();

  @Source("org/eclipse/che/plugin/testing/ide/svg/test_results_pass.svg")
  @Deprecated
  SVGResource testResultsPass();

  @Source("org/eclipse/che/plugin/testing/ide/svg/test_results_fail.svg")
  @Deprecated
  SVGResource testResultsFail();
}
