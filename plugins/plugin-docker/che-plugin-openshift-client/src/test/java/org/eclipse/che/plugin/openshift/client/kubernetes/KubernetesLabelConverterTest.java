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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class KubernetesLabelConverterTest {

    @Test
    public void shouldConvertLabelsToValidKubernetesLabelNames() {
        String validLabelRegex = "([A-Za-z0-9][-A-Za-z0-9_\\.]*)?[A-Za-z0-9]";
        String prefix = KubernetesLabelConverter.getCheServerLabelPrefix();

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
        String prefix = KubernetesLabelConverter.getCheServerLabelPrefix();
        Map<String, String> originalLabels = new HashMap<>();
        originalLabels.put(prefix + "4401/tcp:path:", "/api");
        originalLabels.put(prefix + "8000/tcp:ref:", "tomcat-debug");

        // When
        Map<String, String> converted = KubernetesLabelConverter.labelsToNames(originalLabels);
        Map<String, String> unconverted = KubernetesLabelConverter.namesToLabels(converted);

        // Then
        assertEquals(originalLabels, unconverted);
    }

}
