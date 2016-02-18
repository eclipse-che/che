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
package org.eclipse.che.plugin.docker.client.parser;

import org.eclipse.che.plugin.docker.client.DockerFileException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse docker image reference.
 * <p>
 * For example reference used in FROM instruction of Dockerfile.<br>
 * This class doesn't validate all components as Docker do.<br>
 * It was designed to extract base docker image reference from dockerfile.
 *
 * @author Alexander Garagatyi
 */
public class DockerImageIdentifierParser {

    private static final String HOSTNAME_COMPONENT = "(?:[a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9])";
    private static final String REGISTRY           = HOSTNAME_COMPONENT + "(?:\\." + HOSTNAME_COMPONENT + ")*([:][0-9]+)?";
    private static final String CAPTURED_REGISTRY  = "(?<registry>" + REGISTRY + ")";
    private static final String SEPARATOR           = "(?:[_.]|__|[-]*)";
    private static final String ALPHA_NUMERIC       = "[a-z0-9]+";
    private static final String NAME_COMPONENT      = ALPHA_NUMERIC + "(?:" + SEPARATOR + ALPHA_NUMERIC + ")*";
    private static final String REPOSITORY          = NAME_COMPONENT + "(?:[/]" + NAME_COMPONENT + ")*";
    private static final String CAPTURED_REPOSITORY = "(?<repository>" + REPOSITORY + ")";
    private static final String NAME                = "(?:" + CAPTURED_REGISTRY + "[/])?" + CAPTURED_REPOSITORY;

    private static final Pattern IMAGE_PATTERN = Pattern.compile(NAME);


    public static DockerImageIdentifier parse(final String image) throws DockerFileException {
        if (image == null) {
            throw new IllegalArgumentException("Null argument value is forbidden");
        }

        String workingCopyOfImage = image;
        String digest = "";
        String tag = "";

        // find digest
        int index = workingCopyOfImage.lastIndexOf('@');
        if (index != -1) {
            digest = workingCopyOfImage.substring(index + 1);
            if (!digest.isEmpty()) {
                workingCopyOfImage = workingCopyOfImage.substring(0, index);
            }
        }

        // find tag
        index = workingCopyOfImage.lastIndexOf(':');
        if (index != -1) {
            if (workingCopyOfImage.lastIndexOf('/') < index) {
                tag = workingCopyOfImage.substring(index + 1);
                if (!tag.isEmpty()) {
                    workingCopyOfImage = workingCopyOfImage.substring(0, index);
                }
            }
        }

        Matcher matcher = IMAGE_PATTERN.matcher(workingCopyOfImage);
        if (!matcher.matches()) {
            throw new DockerFileException("Provided image reference is invalid");
        }

        return DockerImageIdentifier.builder()
                                    .setRepository(matcher.group("repository"))
                                    .setRegistry(matcher.group("registry"))
                                    .setTag(tag.isEmpty() ? null : tag)
                                    .setDigest(digest.isEmpty() ? null : digest)
                                    .build();
    }

    private DockerImageIdentifierParser() {}
}
