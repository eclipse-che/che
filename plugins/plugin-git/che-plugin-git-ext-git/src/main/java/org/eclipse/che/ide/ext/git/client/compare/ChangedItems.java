/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.git.client.compare;

import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.eclipse.che.ide.ext.git.client.compare.FileStatus.defineStatus;

/**
 * Describes changed files in git comparison process.
 *
 * @author Mykola Morhun
 */
public class ChangedItems {

    private final Project                       project;
    private final LinkedHashMap<String, Status> changedFilesStatuses;
    private final List<String>                  changedFilesList;
    private final int                           length;

    /**
     * Creates user-friendly representation of git diff.
     *
     * @param project
     *         the project under diff operation
     * @param diff
     *         plain result of git diff operation
     */
    public ChangedItems(Project project, String diff) {
        this.project = project;

        changedFilesStatuses = new LinkedHashMap<>();
        for (String item : diff.split("\n")) {
            changedFilesStatuses.put(item.substring(2, item.length()), defineStatus(item.substring(0, 1)));
        }

        changedFilesList = new ArrayList<>(changedFilesStatuses.keySet());

        length = changedFilesList.size();
    }

    public Project getProject() {
        return project;
    }

    /**
     * @return number of files in the diff
     */
    public int getFilesQuantity() {
        return length;
    }

    public boolean isEmpty() {
        return 0 == length;
    }

    public Map<String, Status> getChangedItemsMap() {
        return changedFilesStatuses;
    }

    public List<String> getChangedItemsList() {
        return changedFilesList;
    }

    public Status getStatusByPath(String pathToChangedItem) {
        return changedFilesStatuses.get(pathToChangedItem);
    }

    public Status getStatusByIndex(int index) {
        return changedFilesStatuses.get(changedFilesList.get(index));
    }

    public String getItemByIndex(int index) {
        return changedFilesList.get(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangedItems that = (ChangedItems)o;
        return Objects.equals(project, that.project) &&
               Objects.equals(changedFilesStatuses, that.changedFilesStatuses) &&
               Objects.equals(changedFilesList, that.changedFilesList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(project, changedFilesStatuses, changedFilesList);
    }

}
