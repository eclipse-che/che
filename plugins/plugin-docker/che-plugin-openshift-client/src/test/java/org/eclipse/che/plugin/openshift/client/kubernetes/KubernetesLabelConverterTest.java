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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

public class KubernetesLabelConverterTest {

    private final String prefix = KubernetesLabelConverter.getCheServerLabelPrefix();

    @Test
    public void shouldConvertLabelsToValidKubernetesLabelNames() {
        String validLabelRegex = "([A-Za-z0-9][-A-Za-z0-9_\\.]*)?[A-Za-z0-9]";

        // Given
        Map<String, String> labels = new HashMap<>();
        labels.put(prefix + "4401/tcp:path:", "/api");
        labels.put(prefix + "8000/tcp:ref:", "tomcat-debug");

        // When
        Map<String, String> converted = KubernetesLabelConverter.labelsToNames(labels);

        // Then
        for (Map.Entry<String, String> entry : converted.entrySet()) {
            assertTrue(entry.getKey().matches(validLabelRegex),
                    String.format("Converted Key %s should be valid Kubernetes label name", entry.getKey()));
            assertTrue(entry.getValue().matches(validLabelRegex),
                    String.format("Converted Value %s should be valid Kubernetes label name", entry.getValue()));
        }
    }

    @Test
    public void shouldBeAbleToRecoverOriginalLabelsAfterConversion() {
        // Given
        Map<String, String> originalLabels = new HashMap<>();
        originalLabels.put(prefix + "4401/tcp:path:", "/api");
        originalLabels.put(prefix + "8000/tcp:ref:", "tomcat-debug");

        // When
        Map<String, String> converted = KubernetesLabelConverter.labelsToNames(originalLabels);
        Map<String, String> unconverted = KubernetesLabelConverter.namesToLabels(converted);

        // Then
        assertEquals(originalLabels, unconverted);
    }

    @Test
    public void shouldIgnoreAndLogProblemLabels() {
        // Given
        Map<String, String> originalLabels = new HashMap<>();
        Map<String, String> validLabels = new HashMap<>();
        validLabels.put(prefix + "4401/tcp:path:", "/api");
        validLabels.put(prefix + "8000/tcp:ref:", "tomcat-debug");
        Map<String, String> invalidLabels = new HashMap<>();
        invalidLabels.put(prefix + "9999/t.cp:path:", "/api");
        invalidLabels.put(prefix + "1111/tcp:path:", "/a_pi");

        originalLabels.putAll(validLabels);
        originalLabels.putAll(invalidLabels);

        // When
        Map<String, String> converted = KubernetesLabelConverter.labelsToNames(originalLabels);
        Map<String, String> unconverted = KubernetesLabelConverter.namesToLabels(converted);

        // Then
        assertTrue(validLabels.entrySet().stream().allMatch(unconverted.entrySet()::contains),
                   "Valid labels should be there when converting + unconverting");
        assertTrue(invalidLabels.entrySet().stream().noneMatch(unconverted.entrySet()::contains),
                   "Labels with invalid characters should be ignored");
    }

    @Test
    public void shouldIgnoreEmptyValues() {
        // Given
        Map<String, String> originalLabels = new HashMap<>();
        originalLabels.put(prefix + "4401/tcp:path:", null);
        originalLabels.put(prefix + "4402/tcp:path:", "");
        originalLabels.put(prefix + "4403/tcp:path:", "  ");

        // When
        Map<String, String> converted = KubernetesLabelConverter.labelsToNames(originalLabels);

        // Then
        assertTrue(converted.isEmpty(), "Labels with null, empty, or whitespace values should be ignored");
    }

    @Test
    public void shouldNotIgnoreValuesWithoutPrefix() {
        // Given
        Map<String, String> originalLabels = new HashMap<>();
        originalLabels.put("4401/tcp:path:", "/api");
        originalLabels.put(prefix + "8000/tcp:ref:", "tomcat-debug");

        // When
        Map<String, String> converted = KubernetesLabelConverter.labelsToNames(originalLabels);

        // Then
        // Currently we put a warning in the logs but convert these labels anyways.
        assertTrue(converted.size() == 2, "Should convert labels even without prefix");
    }
}
