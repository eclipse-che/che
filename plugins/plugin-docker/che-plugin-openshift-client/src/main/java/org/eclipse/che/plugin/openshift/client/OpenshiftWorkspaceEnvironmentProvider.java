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
package org.eclipse.che.plugin.openshift.client;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.plugin.openshift.client.exception.OpenShiftException;

@Singleton
public class OpenshiftWorkspaceEnvironmentProvider {

  private String openShiftCheProjectName;

  @Inject
  public OpenshiftWorkspaceEnvironmentProvider(
      @Named("che.openshift.project") String openShiftCheProjectName) {
    this.openShiftCheProjectName = openShiftCheProjectName;
  }

  public Config getDefaultOpenshiftConfig() {
    return new OpenShiftConfigBuilder().build();
  }

  public Config getWorkspacesOpenshiftConfig(Subject subject) throws OpenShiftException {
    return new OpenShiftConfigBuilder().build();
  }

  public final Config getWorkspacesOpenshiftConfig() throws OpenShiftException {
    return getWorkspacesOpenshiftConfig(EnvironmentContext.getCurrent().getSubject());
  }

  public String getWorkspacesOpenshiftNamespace(Subject subject) throws OpenShiftException {
    return openShiftCheProjectName;
  }

  public final String getWorkspacesOpenshiftNamespace() throws OpenShiftException {
    return getWorkspacesOpenshiftNamespace(EnvironmentContext.getCurrent().getSubject());
  }

  public Boolean areWorkspacesExternal() {
    return false;
  }
}
