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

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.Test;

public class KubernetesStringUtilsTest {

    @Test
    public void getNormalizedStringShouldTrimLongStrings() {
        // Given
        String input = RandomStringUtils.random(70, true, true);
        String expected = input.substring(0, 62);

        // When
        String output = KubernetesStringUtils.getNormalizedString(input);

        // Then
        assertEquals(output, expected, "getNormalizedString should limit string length");
    }

    @Test
    public void getNormalizedStringShouldDoNothingWithShortStrings() {
        // Given
        String input = RandomStringUtils.random(24, true, true);
        String expected = input;

        // When
        String output = KubernetesStringUtils.getNormalizedString(input);

        // Then
        assertEquals(output, expected, "getNormalizedString should do nothing to short strings");
    }

    @Test
    public void convertPullSpecToImageStreamNameShouldTrimTag() {
        // Given
        String input = "testImage:testTag";
        String expected = "testImage";

        // When
        String output = KubernetesStringUtils.convertPullSpecToImageStreamName(input);

        // Then
        assertEquals(output, expected, "Should trim tag off pull spec");
    }

    @Test
    public void convertPullSpecToImageStreamNameShouldBeValidOpenShiftName() {
        // Given
        String input = "eclipse/ubuntu_jdk8";

        // When
        String output = KubernetesStringUtils.convertPullSpecToImageStreamName(input);

        // Then
        assertTrue(!output.contains("/"), "Should remove invalid chars from ImageStream name");
    }

    @Test
    public void converPullSpecToImageStreamNameShouldLimitLength() {
        // Given
        String input = RandomStringUtils.random(100, true, false);

        // When
        String output = KubernetesStringUtils.convertPullSpecToImageStreamName(input);

        // Then
        assertTrue(output.length() < 64, "ImageStream name cannot be over 63 chars");
    }

    @Test
    public void convertPullSpecToTagNameShouldIgnoreRegistryAndTag() {
        // Given
        String inputWithRegistry = "registry/organisation/image:tag";
        String inputWithoutRegistry = "image";

        // When
        String outputWithRegistry = KubernetesStringUtils.convertPullSpecToTagName(inputWithRegistry);
        String outputWithoutRegistry = KubernetesStringUtils.convertPullSpecToTagName(inputWithoutRegistry);

        // Then
        assertEquals(outputWithoutRegistry,
                     outputWithRegistry,
                     "Converting pull spec to tag name should only use image name");
    }

    @Test
    public void convertPullSpecToTagNameShouldLimitLength() {
        // Given
        String input = RandomStringUtils.random(100, true, false);

        // When
        String output = KubernetesStringUtils.convertPullSpecToTagName(input);

        // Then
        assertTrue(output.length() < 63, "ImageStream tag cannot be over 63 chars");
    }

    @Test
    public void createImageStreamTagNameShouldConvertNameInSameWayAsConvertPullSpec() {
        // Given
        String inputOldRepo = "eclipse/ubuntu_jdk8";
        String inputNewRepo = "eclipse-che/che-workspace_" + RandomStringUtils.random(20);
        String expectedImageStreamName = KubernetesStringUtils.convertPullSpecToImageStreamName(inputOldRepo);

        // When
        String rawOutput = KubernetesStringUtils.createImageStreamTagName(inputOldRepo, inputNewRepo);

        // Then
        assertTrue(rawOutput.contains(":"), "ImageStreamTag name is invalid: must contain ':'");
        String outputImageStreamName = rawOutput.split(":")[0];
        assertEquals(outputImageStreamName,
                     expectedImageStreamName,
                     "ImageStreamName should match output of convertPullSpecToImageStreamName");
    }

    @Test
    public void createImageStreamTagNameShouldConvertTagInSameWayAsConvertPullSpec() {
        // Given
        String inputOldRepo = "eclipse/ubuntu_jdk8";
        String inputNewRepo = "eclipse-che/che-workspace_" + RandomStringUtils.random(20);
        String expectedTagName = KubernetesStringUtils.convertPullSpecToTagName(inputNewRepo);

        // When
        String rawOutput = KubernetesStringUtils.createImageStreamTagName(inputOldRepo, inputNewRepo);

        // Then
        assertTrue(rawOutput.contains(":"), "ImageStreamTag name is invalid: must contain ':'");
        String outputImageStreamName = rawOutput.split(":")[1];
        assertEquals(outputImageStreamName,
                     expectedTagName,
                     "ImageStream Tag should match output of convertPullSpecToTagName");
    }

    @Test
    public void createImageStreamTagNameShouldLimitLengthOfCreatedTag() {
        // Given
        String inputOldRepo = RandomStringUtils.random(50, true, false);
        String inputNewRepo = RandomStringUtils.random(50, true, false);

        // When
        String output = KubernetesStringUtils.createImageStreamTagName(inputOldRepo, inputNewRepo);

        // Then
        assertTrue(output.length() < 63, "ImageStreamTags must be shorter than 63 characters");
    }

    @Test
    public void getImageStreamNameFromPullSpecShouldReturnOnlyImageName() {
        // Given
        String input = "registry/organisation/imagename:tagname";
        String expected = "imagename";

        // When
        String output = KubernetesStringUtils.getImageStreamNameFromPullSpec(input);

        // Then
        assertEquals(output, expected);
    }

    @Test
    public void stripTagFromPullSpecShouldRemoveTag() {
        // Given
        String input = "registry/organisation/imagename:tagname";
        String expected = "registry/organisation/imagename";

        // When
        String output = KubernetesStringUtils.stripTagFromPullSpec(input);

        // Then
        assertEquals(output, expected);
    }

    @Test
    public void stripTagFromPullSpecShouldDoNothingIfNoTag() {
        // Given
        String input = "registry/organisation/imagename";

        // When
        String output = KubernetesStringUtils.stripTagFromPullSpec(input);

        // Then
        assertEquals(output, input);
    }

    @Test
    public void getTagNameFromPullSpecShouldReturnTag() {
        // Given
        String input = "registry/organisation/imagename:tagname";
        String expected = "tagname";

        // When
        String output = KubernetesStringUtils.getTagNameFromPullSpec(input);

        // Then
        assertEquals(output, expected);
    }

    @Test
    public void getTagNameFromPullSpecShouldReturnNullWhenPullSpecDoesNotHaveTag() {
        // Given
        String input = "registry/organisation/imagename";

        // When
        String output = KubernetesStringUtils.getTagNameFromPullSpec(input);

        // Then
        assertEquals(output, null);
    }
}
