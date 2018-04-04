/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client.json;

import java.util.Arrays;
import java.util.Map;

/**
 * Defines information about container from list containers.
 *
 * @author Alexander Andrienko
 */
public class ContainerListEntry {
  private String id;
  private String[] names;
  private String image;
  private String imageID;
  private String command;
  private long created;
  private String status;
  private ContainerPort[] ports;
  private Map<String, String> labels;
  private int sizeRw;
  private int sizeRootFs;
  // TODO this is not valid object, fix it
  private NetworkSettings networkSettings;

  /** Returns unique container identifier */
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /**
   * Returns docker container name. Earlier docker api allowed creation additional names, that why
   * this method return String[]. But for now container name is the first element of this array.
   * Read more https://github.com/docker/docker/issues/12538
   */
  public String[] getNames() {
    return names;
  }

  public void setNames(String[] names) {
    this.names = names;
  }

  /** Returns name for image, which was used for creation the container */
  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  /** Returns id for image, which was used for creation the container */
  public String getImageID() {
    return imageID;
  }

  public void setImageID(String imageID) {
    this.imageID = imageID;
  }

  /**
   * Returns time creation of the container in Unix time format(number of milliseconds since Unix
   * epoch January 1 1970 to data creation container)
   */
  public long getCreated() {
    return created;
  }

  public void setCreated(long created) {
    this.created = created;
  }

  /** Returns command which container performs */
  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  /** Returns container status in declarative form, f.e "Up 6 minutes", "Exit 0" */
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  /** Returns published a container's port(s) to the host. See more {@link ContainerPort} */
  public ContainerPort[] getPorts() {
    return ports;
  }

  public void setPorts(ContainerPort[] ports) {
    this.ports = ports;
  }

  /** Returns metadata of the container */
  public Map<String, String> getLabels() {
    return labels;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }

  /** Returns the real size of the container. */
  public int getSizeRw() {
    return sizeRw;
  }

  public void setSizeRw(int sizeRw) {
    this.sizeRw = sizeRw;
  }

  /** Returns the virtual size of the container */
  public int getSizeRootFs() {
    return sizeRootFs;
  }

  public void setSizeRootFs(int sizeRootFs) {
    this.sizeRootFs = sizeRootFs;
  }

  public NetworkSettings getNetworkSettings() {
    return networkSettings;
  }

  public void setNetworkSettings(NetworkSettings networkSettings) {
    this.networkSettings = networkSettings;
  }

  @Override
  public String toString() {
    return "ContainerListEntry{"
        + "id='"
        + id
        + '\''
        + ", names="
        + Arrays.toString(names)
        + ", image='"
        + image
        + '\''
        + ", imageID='"
        + imageID
        + '\''
        + ", command='"
        + command
        + '\''
        + ", created="
        + created
        + ", status='"
        + status
        + '\''
        + ", ports="
        + Arrays.toString(ports)
        + ", labels="
        + labels
        + ", sizeRw="
        + sizeRw
        + ", sizeRootFs="
        + sizeRootFs
        + ", networkSettings="
        + networkSettings
        + '}';
  }
}
