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
package org.eclipse.che.ide.ext.java.client.editor;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.ext.java.client.action.ProposalAction;
import org.eclipse.che.ide.util.loging.Log;

/** @author Evgen Vidolob */
public class ActionCompletionProposal implements CompletionProposal {

  private final String display;
  private final String actionId;
  private final ProposalAction action;
  private final Icon icon;

  public ActionCompletionProposal(
      String display, String actionId, ProposalAction action, Icon icon) {
    this.display = display;
    this.actionId = actionId;
    this.action = action;
    this.icon = icon;
  }

  @Override
  public void getAdditionalProposalInfo(AsyncCallback<Widget> callback) {
    callback.onSuccess(null);
  }

  @Override
  public String getDisplayString() {
    return display;
  }

  @Override
  public Icon getIcon() {
    return icon;
  }

  @Override
  public void getCompletion(CompletionCallback callback) {
    if (action == null) {
      Log.error(getClass(), "Can't run Action " + actionId);
    } else {
      action.performAsProposal();
    }
  }
}
