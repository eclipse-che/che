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
package org.eclipse.che.plugin.docker.compose.yaml.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;

import org.eclipse.che.plugin.docker.compose.ComposeServiceImpl;
import org.eclipse.che.plugin.docker.compose.yaml.ComposeEnvironmentParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * Custom deserializer for parsing {@link ComposeServiceImpl#command}
 * to list command words in the yaml compose file.
 * <p>Command can be:</p>
 * <ul>
 * <li>string array:</li>
 *
 * <pre>
 *  image: codenvy/mysql
 *  environment:
 *   MYSQL_USER: petclinic
 *   MYSQL_PASSWORD: password
 *  mem_limit: 2147483648
 *  command: [service, mysql, start]
 * </pre>
 *
 * <li>simple string line:</li>
 *
 * <pre>
 *  image: codenvy/mysql
 *  environment:
 *   MYSQL_USER: petclinic
 *   MYSQL_PASSWORD: password
 *  mem_limit: 2147483648
 *  command: service mysql start
 * </pre>
 * </ul>
 *
 *
 * See more for parsing compose file
 * {@link ComposeEnvironmentParser}.
 * <p>Note: this deserializer works for json too.</p>
 *
 * @author Alexander Andrienko
 */
public class CommandDeserializer extends JsonDeserializer<List<String>> {

    private static final String SPLIT_COMMAND_REGEX = "[ \n\r]+";

    /**
     * Parse command field from the compose yaml file to list command words.
     *
     * @param jsonParser
     *         json parser
     * @param ctxt
     *         deserialization context
     * @throws IOException
     *         in case I/O error. For example element to parsing contains
     *         invalid yaml field type defined for this field by yaml document model.
     * @throws JsonProcessingException
     */
    @Override
    public List<String> deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        TreeNode tree = jsonParser.readValueAsTree();

        if (tree.isArray()) {
            return toCommand((ArrayNode)tree, ctxt);
        }
        if (tree instanceof TextNode) {
            TextNode textNode = (TextNode)tree;
            return asList(textNode.asText().trim().split(SPLIT_COMMAND_REGEX));
        }
        throw ctxt.mappingException((format("Field '%s' must be simple text or string array.", jsonParser.getCurrentName())));
    }

    private List<String> toCommand(ArrayNode arrayCommandNode, DeserializationContext ctxt) throws JsonMappingException {
        List<String> commands = new ArrayList<>();

        for (TreeNode treeNode : arrayCommandNode) {
            if (treeNode instanceof TextNode) {
                TextNode textNode = (TextNode)treeNode;
                commands.add(textNode.asText());
            } else {
                throw ctxt.mappingException("Array 'command' contains not string element.");
            }
        }

        return commands;
    }
}
