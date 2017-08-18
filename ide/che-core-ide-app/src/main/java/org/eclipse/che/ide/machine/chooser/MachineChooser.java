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
package org.eclipse.che.ide.machine.chooser;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.Executor.ExecutorBody;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;

/** Provides a simple mechanism for the user to choose a {@link Machine}. */
public class MachineChooser implements MachineChooserView.ActionDelegate {

  private final MachineChooserView view;
  private final AppContext appContext;
  private final PromiseProvider promiseProvider;

  private ResolveFunction<MachineImpl> resolveFunction;
  private RejectFunction rejectFunction;

  @Inject
  public MachineChooser(
      MachineChooserView view, AppContext appContext, PromiseProvider promiseProvider) {
    this.view = view;
    this.appContext = appContext;
    this.promiseProvider = promiseProvider;

    view.setDelegate(this);
  }

  /**
   * Pops up a dialog for choosing a machine.
   *
   * <p><b>Note:</b> if there is only one machine running in the workspace then returned promise
   * will be resolved with that machine without asking user.
   *
   * @return promise that will be resolved with a chosen {@link MachineImpl} or rejected in case
   *     machine selection has been cancelled.
   */
  public Promise<MachineImpl> show() {
    final WorkspaceImpl workspace = appContext.getWorkspace();
    final RuntimeImpl runtime = workspace.getRuntime();

    if (runtime != null) {
      final List<? extends MachineImpl> machines = new ArrayList<>(runtime.getMachines().values());

      if (machines.size() == 1) {
        return promiseProvider.resolve(machines.get(0));
      }

      view.setMachines(machines);
    }

    view.show();

    return promiseProvider.create(
        Executor.create(
            (ExecutorBody<MachineImpl>)
                (resolve, reject) -> {
                  resolveFunction = resolve;
                  rejectFunction = reject;
                }));
  }

  @Override
  public void onMachineSelected(MachineImpl machine) {
    view.close();

    resolveFunction.apply(machine);
  }

  @Override
  public void onCanceled() {
    rejectFunction.apply(JsPromiseError.create("Machine selection has been canceled"));
  }
}
