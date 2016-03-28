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
package org.eclipse.che.git.impl.nativegit.commands;

import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.shared.Tag;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Get list of tags
 *
 * @author Eugene Voevodin
 */
public class TagListCommand extends GitCommand<List<Tag>> {

    private String pattern;

    public TagListCommand(File repository) {
        super(repository);
    }

    /**
     * @see GitCommand#execute()
     */
    @Override
    public List<Tag> execute() throws GitException {
        reset();
        commandLine.add("tag", "-l");
        if (pattern != null) {
            commandLine.add(pattern);
        }
        start();
        List<Tag> listOfTags = new LinkedList<>();
        DtoFactory dtoFactory = DtoFactory.getInstance();
        for (String outLine : lines) {
            listOfTags.add(dtoFactory.createDto(Tag.class).withName(outLine));
        }
        return listOfTags;
    }

    /**
     * @param pattern
     *         tag pattern
     * @return TagListCommand with established pattern
     */
    public TagListCommand setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }
}
