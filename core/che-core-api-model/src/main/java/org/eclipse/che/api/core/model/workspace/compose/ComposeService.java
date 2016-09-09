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
package org.eclipse.che.api.core.model.workspace.compose;

import java.util.List;
import java.util.Map;

/**
 * Description of docker compose service.
 *
 * @author Alexander Garagatyi
 */
public interface ComposeService {
    /**
     * Build context for container image creation.
     */
    BuildContext getBuild();

    /**
     * Image for container creation.
     */
    String getImage();

    /**
     * Override the default entrypoint.
     */
    List<String> getEntrypoint();

    /**
     * Override the default command.
     */
    List<String> getCommand();

    /**
     * Environment variables that should be added into container.
     */
    Map<String, String> getEnvironment();

    /**
     * Express dependency between services.
     *
     * <p/> Compose engine implementation should start services in dependency order.
     */
    List<String> getDependsOn();

    /**
     * Specify a custom container name, rather than a generated default name.
     */
    String getContainerName();

    /**
     * Link to containers in another service.
     *
     * <p/> Either specify both the service name and a link alias (SERVICE:ALIAS), or just the service name.
     * <br/> Examples:
     * <ul>
     *     <li>db</li>
     *     <li>db:database</li>
     * </ul>
     */
    List<String> getLinks();

    /**
     * Add metadata to containers using Docker labels.
     */
    Map<String, String> getLabels();

    /**
     * Expose ports without publishing them to the host machine - they’ll only be accessible to linked services.
     *
     * <p/> Only the internal port can be specified.
     * <br/> Examples:
     * <ul>
     *     <li>3000</li>
     *     <li>8000</li>
     * </ul>
     */
    List<String> getExpose();

    /**
     * Expose ports. Either specify both ports (HOST:CONTAINER), or just the container port (a random host port will be chosen).
     *
     * <p/> Examples:
     * <ul>
     *     <li>80</li>
     *     <li>3000</li>
     *     <li>8080:80</li>
     *     <li>80:8000</li>
     *     <li>9090-9091:8080-8081</li>
     *     <li>127.0.0.1:8001:8001</li>
     *     <li>127.0.0.1:5000-5010:5000-5010</li>
     * </ul>
     */
    List<String> getPorts();

    /**
     * Mount paths or named volumes.
     *
     * <p/> Examples:
     * <ul>
     *     <li>/var/lib/mysql</li>
     *     <li>/opt/data:/var/lib/mysql</li>
     *     <li>data-volume:/var/lib/mysql</li>
     * </ul>
     */
    List<String> getVolumes();

    /**
     * Mount all of the volumes from another service or container.
     *
     * <p/> Optionally access level can be specified: read-only access (ro) or read-write (rw).
     * If no access level is specified, then read-write will be used.
     * <p/> Examples:
     * <ul>
     *     <li>service_name</li>
     *     <li>service_name:ro</li>
     *     <li>service_name:rw</li>
     * </ul>
     */
    List<String> getVolumesFrom();

    /**
     * Memory limit for the container of service, specified in bytes.
     */
    Long getMemLimit();
}
