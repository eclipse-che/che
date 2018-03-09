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
package org.eclipse.che.workspace.infrastructure.docker;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;

/**
 * Helps to convert docker infrastructure entities to docker labels and vise-versa.
 *
 * @author Yevhenii Voevodin
 */
public final class Labels {

  public static final String LABEL_PREFIX = "org.eclipse.che.";
  public static final String LABEL_WORKSPACE_ID = LABEL_PREFIX + "workspace.id";
  public static final String LABEL_WORKSPACE_ENV = LABEL_PREFIX + "workspace.env";
  public static final String LABEL_WORKSPACE_OWNER = LABEL_PREFIX + "workspace.owner";
  public static final String LABEL_WORKSPACE_OWNER_ID = LABEL_PREFIX + "workspace.owner.id";
  public static final String LABEL_MACHINE_NAME = LABEL_PREFIX + "machine.name";

  private static final String LABEL_MACHINE_ATTRIBUTES = LABEL_PREFIX + "machine.attributes";
  private static final String SERVER_PORT_LABEL_FMT = LABEL_PREFIX + "server.%s.port";
  private static final String SERVER_PROTOCOL_LABEL_FMT = LABEL_PREFIX + "server.%s.protocol";
  private static final String SERVER_PATH_LABEL_FMT = LABEL_PREFIX + "server.%s.path";
  private static final String SERVER_ATTR_LABEL_FMT = LABEL_PREFIX + "server.%s.attributes";

  /** Pattern that matches server labels e.g. "org.eclipse.che.server.exec-agent.port". */
  private static final Pattern SERVER_LABEL_PATTERN =
      Pattern.compile("org\\.eclipse\\.che\\.server\\.(?<ref>[\\w-/.]+)\\..+");

  private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
  // used to avoid frequent creations of the object at runtime
  private static final java.lang.reflect.Type mapTypeToken =
      new TypeToken<Map<String, String>>() {}.getType();

  /** Creates new label serializer. */
  public static Serializer newSerializer() {
    return new Serializer();
  }

  /** Creates new label deserializer from given labels. */
  public static Deserializer newDeserializer(Map<String, String> labels) {
    return new Deserializer(labels);
  }

  /** Helps to serialize known entities to docker labels. */
  public static class Serializer {

    private final Map<String, String> labels = new HashMap<>();

    /**
     * Serializes machine name as docker container label. Appends serialization result to this
     * aggregate.
     *
     * @param name machine name
     * @return this serializer
     */
    public Serializer machineName(String name) {
      labels.put(LABEL_MACHINE_NAME, name);
      return this;
    }

    /**
     * Serializes runtime identity as docker container labels. Appends serialization result to this
     * aggregate.
     *
     * @param runtimeId the id of runtime
     * @return this serializer
     */
    public Serializer runtimeId(RuntimeIdentity runtimeId) {
      labels.put(LABEL_WORKSPACE_ID, runtimeId.getWorkspaceId());
      labels.put(LABEL_WORKSPACE_ENV, runtimeId.getEnvName());
      labels.put(LABEL_WORKSPACE_OWNER, runtimeId.getOwnerName());
      labels.put(LABEL_WORKSPACE_OWNER_ID, runtimeId.getOwnerId());
      return this;
    }

    /**
     * Serializes server configuration as docker container labels. Appends serialization result to
     * this aggregate.
     *
     * @param ref server reference e.g. "exec-agent"
     * @param server server configuration
     * @return this serializer
     */
    public Serializer server(String ref, ServerConfig server) {
      labels.put(String.format(SERVER_PORT_LABEL_FMT, ref), server.getPort());
      labels.put(String.format(SERVER_PROTOCOL_LABEL_FMT, ref), server.getProtocol());
      if (server.getPath() != null) {
        labels.put(String.format(SERVER_PATH_LABEL_FMT, ref), server.getPath());
      }
      labels.put(String.format(SERVER_ATTR_LABEL_FMT, ref), GSON.toJson(server.getAttributes()));
      return this;
    }

    /**
     * Serializer many servers as docker container labels. Appends serialization result to this
     * aggregate.
     *
     * @param servers ref -> server map
     * @return this serializer
     */
    public Serializer servers(Map<String, ? extends ServerConfig> servers) {
      servers.forEach(this::server);
      return this;
    }

    /**
     * Serializes machine attributes as docker container labels. Appends serialization result to
     * this aggregate.
     *
     * @param attributes machine attributes
     * @return this serializer
     */
    public Serializer machineAttributes(Map<String, String> attributes) {
      labels.put(LABEL_MACHINE_ATTRIBUTES, GSON.toJson(attributes));
      return this;
    }

    /** Returns docker container labels aggregated during serialization. */
    public Map<String, String> labels() {
      return labels;
    }
  }

  /** Helps to deserializer docker labels to known entities. */
  public static class Deserializer {

    private final Map<String, String> labels;

    public Deserializer(Map<String, String> labels) {
      this.labels = Objects.requireNonNull(labels);
    }

    /** Retrieves machine name from docker container labels and returns it. */
    public String machineName() {
      return labels.get(LABEL_MACHINE_NAME);
    }

    /** Retrieves runtime identity from docker labels and returns it. */
    public RuntimeIdentity runtimeId() {
      return new RuntimeIdentityImpl(
          labels.get(LABEL_WORKSPACE_ID),
          labels.get(LABEL_WORKSPACE_ENV),
          labels.get(LABEL_WORKSPACE_OWNER),
          labels.get(LABEL_WORKSPACE_OWNER_ID));
    }

    /** Retrieves server configuration from docker labels and returns (ref -> server config) map. */
    public Map<String, ServerConfig> servers() {
      Map<String, ServerConfig> servers = new HashMap<>();
      for (Map.Entry<String, String> entry : labels.entrySet()) {
        Matcher refMatcher = SERVER_LABEL_PATTERN.matcher(entry.getKey());
        if (refMatcher.matches()) {
          String ref = refMatcher.group("ref");
          if (!servers.containsKey(ref)) {
            servers.put(
                ref,
                new ServerConfigImpl(
                    labels.get(String.format(SERVER_PORT_LABEL_FMT, ref)),
                    labels.get(String.format(SERVER_PROTOCOL_LABEL_FMT, ref)),
                    labels.get(String.format(SERVER_PATH_LABEL_FMT, ref)),
                    GSON.fromJson(
                        labels.get(String.format(SERVER_ATTR_LABEL_FMT, ref)), mapTypeToken)));
          }
        }
      }
      return servers;
    }

    /** Retrieves machine attributes from docker labels and returns them. */
    public Map<String, String> machineAttributes() {
      final Map<String, String> attributes =
          GSON.fromJson(labels.get(LABEL_MACHINE_ATTRIBUTES), mapTypeToken);
      if (attributes != null) {
        return attributes;
      }
      return new HashMap<>();
    }
  }

  private Labels() {}
}
