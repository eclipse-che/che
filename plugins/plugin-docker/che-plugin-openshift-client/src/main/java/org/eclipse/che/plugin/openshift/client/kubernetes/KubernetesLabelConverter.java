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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converter of labels defined in {@link ContainerConfig} for matching to Kubernetes
 * annotation requirements
 */
public final class KubernetesLabelConverter {
    private static final Logger LOG = LoggerFactory.getLogger(KubernetesLabelConverter.class);
    /** Prefix used for che server labels */
    private static final String CHE_SERVER_LABEL_PREFIX  = "che:server";
    /** Padding to use when converting server label to DNS name */
    private static final String CHE_SERVER_LABEL_PADDING = "0%s0";
    /** Regex to use when matching converted labels -- should match {@link CHE_SERVER_LABEL_PADDING} */
    private static final Pattern CHE_SERVER_LABEL_KEY    = Pattern.compile("^0(.*)0$");
    private static final String KUBERNETES_ANNOTATION_REGEX = "([A-Za-z0-9][-A-Za-z0-9_\\.]*)?[A-Za-z0-9]";

    private KubernetesLabelConverter() {
    }

    /**
     * @return prefix that is used for Che server labels
     */
    public static String getCheServerLabelPrefix() {
        return CHE_SERVER_LABEL_PREFIX;
    }

    /**
     * Converts a map of labels to match Kubernetes annotation requirements. Annotations are limited
     * to alphanumeric characters, {@code '.'}, {@code '_'} and {@code '-'}, and must start and end
     * with an alphanumeric character, i.e. they must match the regex
     * {@code ([A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9]}
     *
     * <p>Note that entry keys should begin with {@link KubernetesLabelConverter#CHE_SERVER_LABEL_PREFIX} and
     * entries should not contain {@code '.'} or {@code '_'} before conversion;
     * otherwise label will not be converted and included in output.
     *
     * <p>This implementation is relatively fragile -- changes to how Che generates labels may cause
     * this method to stop working. In general, it will only be possible to convert labels that are
     * alphanumeric plus up to 3 special characters (by converting the special characters to {@code '_'},
     * {@code '-'}, and {@code '.'} as necessary).
     *
     * @param labels Map of labels to convert
     * @return Map of labels converted to DNS Names
     */
    public static Map<String, String> labelsToNames(Map<String, String> labels) {
        Map<String, String> names = new HashMap<>();
        for (Map.Entry<String, String> label : labels.entrySet()) {

            if (!hasConversionProblems(label)) {

                String key = label.getKey();
                String value = label.getValue();

                // Convert keys: e.g. "che:server:4401/tcp:ref" ->
                // "che.server.4401-tcp.ref"
                key = convertLabelKey(key);
                // Convert values: e.g. "/api" -> ".api" -- note values may
                // include '-' e.g. "tomcat-debug"
                value = convertLabelValue(value);

                // Add padding since DNS names must start and end with
                // alphanumeric characters
                key = addPadding(key);
                value = addPadding(value);

                if (matchesKubernetesLabelRegex(key) && matchesKubernetesLabelRegex(value)) {
                    names.put(key, value);
                } else {
                    LOG.error(
                            "Could not convert label {} into Kubernetes annotation: labels must be alphanumeric with ':' and '/'",
                            label.toString());
                }
            }
        }
        return names;
    }

    /**
     * Undoes the label conversion done by {@link KubernetesLabelConverter#labelsToNames(Map)}
     *
     * @param labels Map of DNS names
     * @return Map of unconverted labels
     */
    public static Map<String, String> namesToLabels(Map<String, String> names) {
        Map<String, String> labels = new HashMap<>();
        for (Map.Entry<String, String> entry: names.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();

            // Remove padding
            Matcher keyMatcher   = CHE_SERVER_LABEL_KEY.matcher(key);
            Matcher valueMatcher = CHE_SERVER_LABEL_KEY.matcher(value);
            if (!keyMatcher.matches() || !valueMatcher.matches()) {
                continue;
            }
            key = keyMatcher.group(1);
            value = valueMatcher.group(1);

            // Convert key: e.g. "che.server.4401_tcp.ref" -> "che:server:4401/tcp:ref"
            key = key.replaceAll("\\.", ":").replaceAll("_", "/");
            // Convert value: e.g. Convert values: e.g. "_api" -> "/api"
            value = value.replaceAll("_", "/");

            labels.put(key, value);
        }
        return labels;
    }

    /**
     * Checks if there are any potential problems coupled with label conversion
     * @param label
     * @return true if label has no conversion issues, false otherwise
     */
    private static boolean hasConversionProblems(final Map.Entry<String, String> label) {
        boolean hasProblems = false;
        String key = label.getKey();
        String value = label.getValue();

        if (StringUtils.isBlank(value)) {
            LOG.error("The label {} is blank", label.toString());
            hasProblems = true;
        } else if (key.contains(".") || key.contains("_") || value.contains("_")) {
            LOG.error("Cannot convert label {} to DNS Name: '-' and '.' are used as escape characters",
                    label.toString());
            hasProblems = true;
        } else if (!key.startsWith(CHE_SERVER_LABEL_PREFIX)) {
            LOG.warn("Expected CreateContainerParams label key {} to start with {}", key, CHE_SERVER_LABEL_PREFIX);
        }
        return hasProblems;
    }

    /**
     * Convert keys: e.g. "che:server:4401/tcp:ref" -> "che.server.4401-tcp.ref"
     */
    private static String convertLabelKey(final String key) {
        return key.replaceAll(":", ".").replaceAll("/", "_");
    }

    /**
     * Convert values: e.g. "/api" -> ".api" Note: values may include '-' e.g.
     * "tomcat-debug"
     */
    private static String convertLabelValue(final String value) {
        return value.replaceAll("/", "_");
    }

    /**
     * Adds padding since DNS names must start and end with alphanumeric
     * characters
     */
    private static String addPadding(final String label) {
        return String.format(CHE_SERVER_LABEL_PADDING, label);
    }

    private static boolean matchesKubernetesLabelRegex(final String label) {
        return label.matches(KUBERNETES_ANNOTATION_REGEX);
    }
}
