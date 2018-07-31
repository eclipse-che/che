/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.che.commons.lang.Pair;

/** @author andrew00x */
public class DockerImage {
  private String from;
  private List<String> maintainer;
  private List<String> run;
  private String cmd;
  private List<String> expose;
  private Map<String, String> env;
  private List<Pair<String, String>> add;
  private String entrypoint;
  private List<String> volume;
  private String user;
  private String workdir;
  private List<String> onbuild;
  private List<String> comments;

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public List<String> getMaintainer() {
    if (maintainer == null) {
      maintainer = new LinkedList<>();
    }
    return maintainer;
  }

  public List<String> getRun() {
    if (run == null) {
      run = new LinkedList<>();
    }
    return run;
  }

  public String getCmd() {
    return cmd;
  }

  public void setCmd(String cmd) {
    this.cmd = cmd;
  }

  public List<String> getExpose() {
    if (expose == null) {
      expose = new LinkedList<>();
    }
    return expose;
  }

  public Map<String, String> getEnv() {
    if (env == null) {
      env = new LinkedHashMap<>();
    }
    return env;
  }

  public List<Pair<String, String>> getAdd() {
    if (add == null) {
      add = new LinkedList<>();
    }
    return add;
  }

  public String getEntrypoint() {
    return entrypoint;
  }

  public void setEntrypoint(String entrypoint) {
    this.entrypoint = entrypoint;
  }

  public List<String> getVolume() {
    if (volume == null) {
      volume = new LinkedList<>();
    }
    return volume;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getWorkdir() {
    return workdir;
  }

  public void setWorkdir(String workdir) {
    this.workdir = workdir;
  }

  public List<String> getOnbuild() {
    if (onbuild == null) {
      onbuild = new LinkedList<>();
    }
    return onbuild;
  }

  public List<String> getComments() {
    if (comments == null) {
      comments = new LinkedList<>();
    }
    return comments;
  }
}
