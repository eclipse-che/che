/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.pullrequest.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import org.vectomatic.dom.svg.ui.SVGResource;

/** Contributor plugin resources. */
public interface ContributeResources extends ClientBundle {
  @Source({"Contribute.css", "org/eclipse/che/ide/api/ui/style.css"})
  ContributeCss contributeCss();

  @Source("images/refresh.svg")
  SVGResource refreshIcon();

  @Source("images/icon.svg")
  SVGResource titleIcon();

  interface ContributeCss extends CssResource {
    String blueButton();

    String openOnVcsButton();

    String errorMessage();

    String inputError();

    String inputField();

    String statusSteps();

    String stepLabel();

    String checkIcon();

    String errorIcon();

    String stepLabelRow();

    String statusTitleStepLabel();

    String statusIndexStepLabel();
  }
}
