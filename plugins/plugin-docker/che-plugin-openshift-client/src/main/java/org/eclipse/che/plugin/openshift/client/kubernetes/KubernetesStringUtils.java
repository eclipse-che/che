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

import org.apache.commons.lang.RandomStringUtils;
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
        int end = Math.min(input.length(), MAX_CHARS - 1);
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
     * Che workspace id is used as OpenShift service / deployment config name
     * and must match the regex [a-z]([-a-z0-9]*[a-z0-9]) e.g. "q5iuhkwjvw1w9emg"
     *
     * @return randomly generated workspace id
     */
    public static String generateWorkspaceID() {
        return RandomStringUtils.random(16, true, true).toLowerCase();
    }

    /**
     * Converts a String into a suitable name for an openshift container.
     * Kubernetes names are limited to 63 chars and must match the regex
     * {@code [a-z0-9]([-a-z0-9]*[a-z0-9])?}
     * @param input the string to convert
     */
    public static String convertToContainerName(String input) {
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
    public static String convertPullSpecToImageStreamName(String repository) {
        repository = stripTagFromPullSpec(repository);
        return getNormalizedString(repository.replaceAll("/", "_"));
    }

    /**
     * Generates a name to be used as a tag from a docker repository.
     * In OpenShift, tagging functionality is limited, so while in Docker we may want to
     * <p>{@code docker tag eclipse/ubuntu_jdk8 eclipse-che/<workspace-id>},<p> this is not
     * possible in OpenShift. This method returns a trimmed version of {@code <workspace-id>}
     * @param repository the target repository spec in a {@code docker tag} command.
     * @return an appropriate tag name
     */
    public static String convertPullSpecToTagName(String repository) {
        String name;
        if (repository.contains("/")) {
            String[] nameSegments = repository.split("/");
            name = nameSegments[nameSegments.length - 1];
        } else {
            name = repository;
        }
        name = stripTagFromPullSpec(name);
        name = name.replaceAll("workspace", "")
                   .replaceAll("machine", "")
                   .replaceAll("che_.*", "")
                   .replaceAll("_", "");

        name = "che-ws-" + name;
        return getNormalizedString(name);
    }

    /**
     * Gets an ImageStreamTag name from docker pull specs by converting repository strings
     * to suit the convention used in {@link KubernetesStringUtils#convertPullSpecToImageStreamName(String)}
     * and {@link KubernetesStringUtils#convertPullSpecToTagName(String)}.
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
    public static String createImageStreamTagName(String oldRepository, String newRepository) {
        String tag = convertPullSpecToTagName(newRepository);
        String repo = convertPullSpecToImageStreamName(oldRepository);
        return getNormalizedString(String.format("%s:%s", repo, tag));
    }

    /**
     * Gets the ImageStreamName fromm a docker pull spec. For example, provided
     * {@code [<registry>]/[<organization>]/<image>:[<tag>]}, will return just {@code <image>}
     *
     * In the case where the pull spec does not contain optional components, this method simply
     * returns the pull spec provided.
     *
     * @param pullSpec
     * @return
     */
    public static String getImageStreamNameFromPullSpec(String pullSpec) {
        return pullSpec.replaceAll(".*/", "").replaceAll(":.*", "");
    }

    /**
     * Remove the tag from a pull spec, if applicable. If pull spec does not include a tag,
     * returns the pull spec unchanged.
     * @param pullSpec
     * @return
     */
    public static String stripTagFromPullSpec(String pullSpec) {
        return pullSpec.replaceAll(":.*", "");
    }

    /**
     * Gets the tag fromm a docker pull spec. For example, provided
     * {@code [<registry>]/[<organization>]/<image>:[<tag>]}, will return just {@code <tag>}
     *
     * @param pullSpec
     * @return the tag on the pull spec, or null if pull spec does not contain a tag
     */
    public static String getTagNameFromPullSpec(String pullSpec) {
        if (!pullSpec.contains(":")) {
            return null;
        }
        return pullSpec.replaceAll(".*:", "");
    }
}
