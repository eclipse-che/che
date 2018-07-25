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
package org.eclipse.che.infrastructure.docker.client.json.network;

import java.util.Map;
import java.util.Objects;

/**
 * Represents configuration that should be passed to docker API to create new network.
 *
 * @author Alexander Garagatyi
 */
public class NewNetwork {
  private String name;
  private boolean checkDuplicate;
  private String driver;
  private boolean internal;
  private Ipam iPAM;
  private boolean enableIPv6;
  private Map<String, String> options;
  private Map<String, String> labels;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public NewNetwork withName(String name) {
    this.name = name;
    return this;
  }

  public boolean isCheckDuplicate() {
    return checkDuplicate;
  }

  public void setCheckDuplicate(boolean checkDuplicate) {
    this.checkDuplicate = checkDuplicate;
  }

  public NewNetwork withCheckDuplicate(boolean checkDuplicate) {
    this.checkDuplicate = checkDuplicate;
    return this;
  }

  public String getDriver() {
    return driver;
  }

  public void setDriver(String driver) {
    this.driver = driver;
  }

  public NewNetwork withDriver(String driver) {
    this.driver = driver;
    return this;
  }

  public boolean isInternal() {
    return internal;
  }

  public void setInternal(boolean internal) {
    this.internal = internal;
  }

  public NewNetwork withInternal(boolean internal) {
    this.internal = internal;
    return this;
  }

  public Ipam getIPAM() {
    return iPAM;
  }

  public void setIPAM(Ipam iPAM) {
    this.iPAM = iPAM;
  }

  public NewNetwork withIPAM(Ipam iPAM) {
    this.iPAM = iPAM;
    return this;
  }

  public boolean isEnableIPv6() {
    return enableIPv6;
  }

  public void setEnableIPv6(boolean enableIPv6) {
    this.enableIPv6 = enableIPv6;
  }

  public NewNetwork withEnableIPv6(boolean enableIPv6) {
    this.enableIPv6 = enableIPv6;
    return this;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public void setOptions(Map<String, String> options) {
    this.options = options;
  }

  public NewNetwork withOptions(Map<String, String> options) {
    this.options = options;
    return this;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }

  public NewNetwork withLabels(Map<String, String> labels) {
    this.labels = labels;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof NewNetwork)) {
      return false;
    }
    final NewNetwork that = (NewNetwork) obj;
    return checkDuplicate == that.checkDuplicate
        && internal == that.internal
        && enableIPv6 == that.enableIPv6
        && Objects.equals(name, that.name)
        && Objects.equals(driver, that.driver)
        && Objects.equals(iPAM, that.iPAM)
        && getOptions().equals(that.getOptions())
        && getLabels().equals(that.getLabels());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(name);
    hash = 31 * hash + Boolean.hashCode(checkDuplicate);
    hash = 31 * hash + Objects.hashCode(driver);
    hash = 31 * hash + Boolean.hashCode(internal);
    hash = 31 * hash + Objects.hashCode(iPAM);
    hash = 31 * hash + Boolean.hashCode(enableIPv6);
    hash = 31 * hash + getOptions().hashCode();
    hash = 31 * hash + getLabels().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "NewNetwork{"
        + "name='"
        + name
        + '\''
        + ", checkDuplicate="
        + checkDuplicate
        + ", driver='"
        + driver
        + '\''
        + ", internal="
        + internal
        + ", iPAM="
        + iPAM
        + ", enableIPv6="
        + enableIPv6
        + ", options="
        + options
        + ", labels="
        + labels
        + '}';
  }
}
