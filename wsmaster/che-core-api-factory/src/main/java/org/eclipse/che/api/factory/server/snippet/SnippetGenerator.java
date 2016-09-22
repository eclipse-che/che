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
package org.eclipse.che.api.factory.server.snippet;

import org.eclipse.che.api.core.model.factory.Button;
import org.eclipse.che.api.core.model.factory.Factory;

import javax.ws.rs.core.UriBuilder;
import java.util.Formatter;


/** Helper for snippet generating. */

public class SnippetGenerator {

    public static String generateHtmlSnippet(String baseUrl, String id) {
        return format("<script type=\"text/javascript\" " + "src=\"%s/factory/resources/factory.js?%s\"></script>", baseUrl, id);
    }

    public static String generateiFrameSnippet(String baseUrl, String factoryId) {
        return format("<iframe src=\"%s\" width=\"800\" height=\"480\"></iframe>",
                      UriBuilder.fromUri(baseUrl).replacePath("factory").queryParam("id", factoryId).build().toString());
    }

    public static String generateMarkdownSnippet(String baseUrl, Factory factory, String imageId) {
        String imgUrl;

        if (factory.getButton() == null) {
            throw new IllegalArgumentException("Unable to generate markdown snippet for factory without button");
        }

        if (Button.Type.LOGO.equals(factory.getButton().getType())) {
            if (imageId != null && factory.getId() != null) {
                imgUrl = format("%s/api/factory/%s/image?imgId=%s", baseUrl, factory.getId(), imageId);
            } else {
                throw new IllegalArgumentException("Factory with logo MUST have at leas one image.");
            }
        } else if (factory.getButton().getAttributes() == null) {
            throw new IllegalArgumentException("Unable to generate markdown snippet for factory without button's attributes");
        } else if ("white".equals(factory.getButton().getAttributes().getColor())) {
            imgUrl = format("%s/factory/resources/factory-white.png", baseUrl);
        } else if ("gray".equals(factory.getButton().getAttributes().getColor())) {
            imgUrl = format("%s/factory/resources/factory-dark.png", baseUrl);
        } else {
            throw new IllegalArgumentException("Unable to generate markdown snippet with nologo button and empty color");
        }


        final String factoryURL = UriBuilder.fromUri(baseUrl).replacePath("factory").queryParam("id", factory.getId()).build().toString();
        return format("[![alt](%s)](%s)", imgUrl, factoryURL);
    }

    /**
     * Formats the input string filling the {@link String} arguments.
     * Is intended to be used on client side, where {@link String#format(String, Object...)} and {@link Formatter}
     * cannot be used.
     *
     * @param format
     *         string format
     * @param args
     *         {@link String} arguments
     * @return {@link String} formatted string
     */
    private static String format(final String format, final String... args) {
        String[] split = format.split("%s");
        final StringBuilder msg = new StringBuilder();
        for (int pos = 0; pos < args.length; pos += 1) {
            msg.append(split[pos]);
            msg.append(args[pos]);
        }
        for (int pos = args.length; pos < split.length; pos += 1) {
            msg.append(split[pos]);
        }
        return msg.toString();
    }
}
