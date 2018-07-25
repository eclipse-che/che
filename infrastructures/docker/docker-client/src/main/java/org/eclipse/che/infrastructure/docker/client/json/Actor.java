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
package org.eclipse.che.infrastructure.docker.client.json;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * Represents Actor field of {@link Event}.
 *
 * @author Mykola Morhun
 */
public class Actor {
  @SerializedName("ID")
  private String id;

  @SerializedName("Attributes")
  private Map<String, String> attributes;

  public String getId() {
    return id;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public Actor withId(String id) {
    this.id = id;
    return this;
  }

  public Actor withAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }

  @Override
  public String toString() {
    return "Actor{" + "id='" + id + '\'' + ", attributes=" + attributes + '}';
  }
}
