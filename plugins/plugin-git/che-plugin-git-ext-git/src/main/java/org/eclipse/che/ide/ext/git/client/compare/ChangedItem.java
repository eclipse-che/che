package org.eclipse.che.ide.ext.git.client.compare;

import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;

/**
 * Describes changed file in git comparison process.
 *
 * @author Mykola Morhun
 */
public class ChangedItem {

    private final File   file;
    private final Status status;

    public ChangedItem(File path, Status status) {
        this.file = path;
        this.status = status;
    }

    public File getFile() {
        return file;
    }

    public Status getStatus() {
        return status;
    }

}
