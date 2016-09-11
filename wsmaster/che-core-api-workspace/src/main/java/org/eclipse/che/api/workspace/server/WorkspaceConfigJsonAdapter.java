/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.inject.Singleton;

import static com.google.common.primitives.Ints.tryParse;
import static java.lang.String.format;

/**
 * Adapts an old workspace configuration object format to a new format.
 *
 * <pre>
 * Old workspace config format:
 * {
 *      "name" : "default",
 *      "defaultEnv" : "dev-env",
 *      "description" : "This is workspace description",
 *      "environments": [
 *          {
 *              "name": "dev-env",
 *              "machineConfigs": [
 *                  {
 *                      "name": "dev", <- becomes environment defined machine
 *                      "limits": {
 *                          "ram": 2048 <- in bytes
 *                      },
 *                      "source": { <- goes to recipe
 *                          "location": "https://somewhere/Dockerfile",
 *                          "type": "dockerfile"
 *                      },
 *                      "type": "docker", <- will be defined by environment recipe type
 *                      "dev": true, <- if agents contain 'org.eclipse.che.ws-agent'
 *                      "envVariables" : {
 *                          "env1" : "value1",
 *                          "env2" : "value2
 *                      },
 *                      "servers" : [ <- goes to machine definition
 *                          {
 *                              {
 *                                  "ref" : "some_reference",
 *                                  "port" : "9090/udp",
 *                                  "protocol" : "some_protocol",
 *                                  "path" : "/some/path"
 *                              }
 *                          }
 *                      ]
 *                  }
 *              ]
 *          }
 *      ],
 * }
 *
 * New workspace config format:
 * {
 *      "name" : "default",
 *      "defaultEnv" : "dev-env",
 *      "description" : "This is workspace description",
 *      "environments" : {
 *          "dev-env" : {
 *              "recipe" : {
 *                  "type" : "dockerfile",
 *                  "contentType" : "text/x-dockerfile",
 *                  "location" : "https://somewhere/Dockerfile"
 *              },
 *              "machines" : {
 *                  "dev-machine" : {
 *                      "agents" : [ "org.eclipse.che.terminal", "org.eclipse.che.ws-agent", "org.eclipse.che.ssh" ],
 *                      "servers" : {
 *                          "some_reference" : {
 *                              "port" : "9090/udp",
 *                              "protocol" : "some_protocol",
 *                              "properties" : {
 *                                  "prop1" : "value1"
 *                              }
 *                          }
 *                      },
 *                      "attributes" : {
 *                          "memoryLimitBytes" : "2147483648"
 *                      }
 *                  }
 *              }
 *          }
 *      }
 * }
 * </pre>
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class WorkspaceConfigJsonAdapter {

    /**
     * Adapts given workspace configuration environments to a new format,
     * if it contains environments array, otherwise does nothing.
     *
     * @throws IllegalArgumentException
     *         if the old format is bad, and can't be converted to a new one
     */
    public void adaptModifying(JsonObject conf) {
        if (conf.has("environments") && conf.get("environments").isJsonArray()) {
            final JsonObject newEnvironments = new JsonObject();
            for (JsonElement environmentEl : conf.get("environments").getAsJsonArray()) {
                if (!environmentEl.isJsonObject()) {
                    throw new IllegalArgumentException("Bad format, expected environment to be json object");
                }
                final JsonObject env = environmentEl.getAsJsonObject();
                if (!env.has("name") || env.get("name").isJsonNull()) {
                    throw new IllegalArgumentException("Bad format, expected environment to provide a name");
                }
                final String envName = env.get("name").getAsString();
                newEnvironments.add(envName, asEnvironment(env, envName));
            }
            conf.add("environments", newEnvironments);
        }
    }

    private static JsonObject asEnvironment(JsonObject env, String envName) {
        final JsonObject devMachine = findDevMachine(env);

        // check environment is a valid old format environment
        if (devMachine == null) {
            throw new IllegalArgumentException("Bad format, expected dev-machine to be present in environment " + envName);
        }
        if (!devMachine.has("name") || devMachine.get("name").isJsonNull()) {
            throw new IllegalArgumentException("Bad format, expected dev-machine to provide a name");
        }
        if (!devMachine.has("source") || !devMachine.get("source").isJsonObject()) {
            throw new IllegalArgumentException("Bad format, expected dev-machine to provide a source");
        }
        final JsonObject source = devMachine.getAsJsonObject("source");
        if (!source.has("type") || source.get("type").isJsonObject()) {
            throw new IllegalArgumentException("Bad format, expected dev-machine to provide a source with a type");
        }

        // convert dev-machine to a new format
        final JsonObject newMachine = new JsonObject();

        // dev-machine agents
        final JsonArray agents = new JsonArray();
        agents.add(new JsonPrimitive("org.eclipse.che.terminal"));
        agents.add(new JsonPrimitive("org.eclipse.che.ws-agent"));
        agents.add(new JsonPrimitive("org.eclipse.che.ssh"));
        newMachine.add("agents", agents);

        // dev-machine ram
        if (devMachine.has("limits")) {
            if (!devMachine.get("limits").isJsonObject()) {
                throw new IllegalArgumentException(format("Bad limits format in the dev-machine of '%s' environment", envName));
            }
            final JsonObject limits = devMachine.getAsJsonObject("limits");
            if (limits.has("ram")) {
                final Integer ram = tryParse(limits.get("ram").getAsString());
                if (ram == null || ram < 0) {
                    throw new IllegalArgumentException(format("Bad format, ram of dev-machine in environment '%s' " +
                                                              "must an unsigned integer value", envName));
                }
                final JsonObject attributes = new JsonObject();
                attributes.addProperty("memoryLimitBytes", Long.toString(1024L * 1024L * ram));
                newMachine.add("attributes", attributes);
            }
        }

        // dev-machine servers
        if (devMachine.has("servers")) {
            if (!devMachine.get("servers").isJsonArray()) {
                throw new IllegalArgumentException("Bad format of servers in dev-machine, servers must be json array");
            }
            final JsonObject newServersObj = new JsonObject();
            for (JsonElement serversEl : devMachine.get("servers").getAsJsonArray()) {
                final JsonObject oldServerObj = serversEl.getAsJsonObject();
                if (!oldServerObj.has("ref")) {
                    throw new IllegalArgumentException("Bad format of server in dev-machine, server must contain ref");
                }
                final String ref = oldServerObj.get("ref").getAsString();
                oldServerObj.remove("ref");
                if (oldServerObj.has("path")) {
                    final JsonObject props = new JsonObject();
                    props.add("path", oldServerObj.get("path"));
                    oldServerObj.add("properties", props);
                    oldServerObj.remove("path");
                }
                newServersObj.add(ref, oldServerObj);
            }
            newMachine.add("servers", newServersObj);
        }

        // create an environment recipe
        final JsonObject envRecipe = new JsonObject();
        final String type = source.get("type").getAsString();
        switch (type) {
            case "recipe":
            case "dockerfile":
                envRecipe.addProperty("type", "dockerfile");
                envRecipe.addProperty("contentType", "text/x-dockerfile");
                if (source.has("content") && !source.get("content").isJsonNull()) {
                    envRecipe.addProperty("content", source.get("content").getAsString());
                } else if (source.has("location") && !source.get("location").isJsonNull()) {
                    envRecipe.addProperty("location", source.get("location").getAsString());
                } else {
                    throw new IllegalArgumentException("Bad format, expected dev-machine source with type 'dockerfile' " +
                                                       "to provide either 'content' or 'location'");
                }
                break;
            case "image":
                if (!source.has("location") || source.get("location").isJsonNull()) {
                    throw new IllegalArgumentException("Bad format, expected dev-machine source with type 'image' " +
                                                       "to provide image 'location'");
                }
                envRecipe.addProperty("type", "dockerimage");
                envRecipe.addProperty("location", source.get("location").getAsString());
                break;
            default:
                throw new IllegalArgumentException(format("Bad format, dev-machine source type '%s' is not supported", type));
        }

        // create a new environment
        final JsonObject newEnv = new JsonObject();
        newEnv.add("recipe", envRecipe);
        final JsonObject machines = new JsonObject();
        machines.add(devMachine.get("name").getAsString(), newMachine);
        newEnv.add("machines", machines);
        return newEnv;
    }

    /** Searches for dev-machine in default environment. */
    public static JsonObject findDevMachine(JsonObject env) {
        if (env.has("machineConfigs") && env.get("machineConfigs").isJsonArray()) {
            for (JsonElement machineCfgEl : env.getAsJsonArray("machineConfigs")) {
                final JsonObject machineCfgObj = machineCfgEl.getAsJsonObject();
                if (machineCfgObj.has("dev") && machineCfgObj.get("dev").getAsBoolean()) {
                    return machineCfgObj;
                }
            }
        }
        return null;
    }
}
