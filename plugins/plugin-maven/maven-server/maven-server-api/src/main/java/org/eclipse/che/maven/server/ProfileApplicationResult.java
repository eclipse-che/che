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
package org.eclipse.che.maven.server;

import java.io.Serializable;
import org.eclipse.che.maven.data.MavenExplicitProfiles;
import org.eclipse.che.maven.data.MavenModel;

/**
 * Describes an information after applying profiles. Contains a maven model and a list of applied
 * profiles.
 */
public class ProfileApplicationResult implements Serializable {
  private final MavenModel myModel;
  private final MavenExplicitProfiles myActivatedProfiles;

  public ProfileApplicationResult(MavenModel model, MavenExplicitProfiles activatedProfiles) {
    myModel = model;
    myActivatedProfiles = activatedProfiles;
  }

  /** Returns a maven model which was modified after applying maven profiles. */
  public MavenModel getModel() {
    return myModel;
  }

  /** Returns a list of activated profiles. */
  public MavenExplicitProfiles getActivatedProfiles() {
    return myActivatedProfiles;
  }
}
