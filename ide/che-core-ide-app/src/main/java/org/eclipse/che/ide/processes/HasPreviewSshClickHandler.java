package org.eclipse.che.ide.processes;

/**
 * An object that implements this interface provides registration for {@link PreviewSshClickHandler} instances.
 *
 * @author Vlad Zhukovskyi
 * @see PreviewSshClickHandler
 * @since 5.10.0
 */
public interface HasPreviewSshClickHandler {

    /**
     * Adds a {@link PreviewSshClickHandler} handler.
     *
     * @param handler
     *         the preview ssh click handler
     */
    void addPreviewSshClickHandler(PreviewSshClickHandler handler);
}
