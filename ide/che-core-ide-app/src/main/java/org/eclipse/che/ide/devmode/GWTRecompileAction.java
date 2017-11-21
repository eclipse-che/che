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
package org.eclipse.che.ide.devmode;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;

/** Action that invokes IDE GWT app recompilation in Super DevMode. */
@Singleton
public class GWTRecompileAction extends BaseAction {

  private final GWTRecompiler gwtRecompiler;

  @Inject
  public GWTRecompileAction(CoreLocalizationConstant messages, GWTRecompiler gwtRecompiler) {
    super(messages.gwtRecompileActionTitle());

    this.gwtRecompiler = gwtRecompiler;
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    gwtRecompiler.recompile();
  }
}
