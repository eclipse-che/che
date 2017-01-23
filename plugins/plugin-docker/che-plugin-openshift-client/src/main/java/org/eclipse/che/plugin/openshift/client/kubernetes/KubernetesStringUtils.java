/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.plugin.openshift.client.kubernetes;

import org.apache.commons.lang.StringUtils;

public final class KubernetesStringUtils {

    /**
     * Max length of a Kubernetes name or label;
     */
    private static final int    MAX_CHARS     = 63;
    private static final String DOCKER_PREFIX = "docker://";

    private KubernetesStringUtils() {
    }

    /**
     * Converts strings to fit requirements of Kubernetes names and labels.
     * Names in Kubernetes are limited to 63 characters.
     * @param input the string to normalize
     * @return the normalized string.
     */
    public static String getNormalizedString(String input) {
        int end = Math.min(input.length(), MAX_CHARS);
        return input.substring(0, end);
    }

    /**
     * @param containerID
     * @return normalized version of 'ContainerID' without 'docker://' prefix and double quotes
     */
    public static String normalizeContainerID(final String containerID) {
        return StringUtils.replaceOnce(containerID, DOCKER_PREFIX, "").replace("\"", "");
    }

    /**
     * @param containerID
     * @return label based on 'ContainerID' (first 12 chars of ID)
     */
    public static String getLabelFromContainerID(final String containerID) {
        return StringUtils.substring(containerID, 0, 12);
    }

    /**
     * Converts a String into a suitable name for an openshift container.
     * Kubernetes names are limited to 63 chars and must match the regex
     * {@code [a-z0-9]([-a-z0-9]*[a-z0-9])?}
     * @param input the string to convert
     */
    public static String getContainerName(String input) {
        if (input.startsWith("workspace")) {
            input = input.replaceFirst("workspace", "");
        }
        return getNormalizedString(input.replaceAll("_", "-"));
    }

    /**
     * Converts image stream name (e.g. eclipse/ubuntu_jdk8 to eclipse_ubuntu_jdk8).
     * This has to be done because for OpenShift ImageStream names, the organization component
     * of a docker repository is the namespace of the ImageStream, and so '/' is not supported
     * in ImageStream names.
     * @param repository the original docker repository String.
     * @return
     */
    public static String getImageStreamName(String repository) {
        return getNormalizedString(repository.replaceAll("/", "_"));
    }

    /**
     * Generates a name to be used as a tag from a docker repository.
     * In OpenShift, tagging functionality is limited, so while in Docker we may want to
     * <p>{@code docker tag eclipse/ubuntu_jdk8 eclipse-che/<workspace-id>},<p> this is not
     * possible in OpenShift. This method returns a trimmed version of {@code <workspace-id>}
     * @param repo the target repository spec in a {@code docker tag} command.
     * @return an appropriate tag name
     */
    public static String getTagNameFromRepoString(String repo) {
        String name;
        if (repo.contains("/")) {
            name = repo.split("/")[1];
        } else {
            name = repo;
        }
        name = name.replaceAll("workspace", "")
                   .replaceAll("machine", "")
                   .replaceAll("che_.*", "")
                   .replaceAll("_", "");

        name = "che-ws-" + name;
        return getNormalizedString(name);
    }

    /**
     * Gets an ImageStreamTag name from docker pull specs by converting repository strings
     * to suit the convention used in {@link KubernetesStringUtils#getImageStreamName(String)}
     * and {@link KubernetesStringUtils#getTagNameFromRepoString(String)}.
     *
     * <p> e.g. will convert {@code eclipse/ubuntu_jdk8} and {@code eclipse-che/<workspace-id>}
     * into {@code eclipse_ubuntu_jdk8:<workspace-id>}
     *
     * @param oldRepository
     *          The docker image repository that is tracked by the ImageStream
     * @param newRepository
     *          The docker repository that has been tagged to follow oldRepository
     * @return A string that can be used to refer to the ImageStreamTag formed from these repositories.
     */
    public static String getImageStreamTagName(String oldRepository, String newRepository) {
        String tag = getTagNameFromRepoString(newRepository);
        String repo = getImageStreamName(oldRepository);
        return getNormalizedString(String.format("%s:%s", repo, tag));
    }
}
