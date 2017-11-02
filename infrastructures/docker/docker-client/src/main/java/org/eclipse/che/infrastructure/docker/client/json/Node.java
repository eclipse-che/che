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
package org.eclipse.che.infrastructure.docker.client.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Describe docker node in swarm model
 *
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
public class Node {

  private String id;
  private String name;
  private String addr;
  private String ip;
  private int cpus;
  private long memory;

  private Map<String, String> labels = new HashMap<>();

  public String getID() {
    return id;
  }

  public void setID(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAddr() {
    return addr;
  }

  public void setAddr(String addr) {
    this.addr = addr;
  }

  public String getIP() {
    return ip;
  }

  public void setIP(String ip) {
    this.ip = ip;
  }

  public int getCpus() {
    return cpus;
  }

  public void setCpus(int cpus) {
    this.cpus = cpus;
  }

  public long getMemory() {
    return memory;
  }

  public void setMemory(long memory) {
    this.memory = memory;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }

  @Override
  public String toString() {
    return "Node{"
        + "ID='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", addr='"
        + addr
        + '\''
        + ", IP='"
        + ip
        + '\''
        + ", cpus="
        + cpus
        + ", memory="
        + memory
        + ", labels="
        + labels
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Node)) return false;
    Node node = (Node) o;
    return Objects.equals(getCpus(), node.getCpus())
        && Objects.equals(getMemory(), node.getMemory())
        && Objects.equals(getID(), node.getID())
        && Objects.equals(getName(), node.getName())
        && Objects.equals(getAddr(), node.getAddr())
        && Objects.equals(getIP(), node.getIP())
        && Objects.equals(getLabels(), node.getLabels());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getID(), getName(), getAddr(), getIP(), getCpus(), getMemory(), getLabels());
  }
}
