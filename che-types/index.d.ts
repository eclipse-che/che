/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
declare interface CheApi {
    imageRegistry: ImageRegistry;
    actionManager: ActionManager;
    partManager: PartManager;
    editorManager: EditorManager;
    dialogManager: che.ide.dialogs.DialogManager;
    appContext: AppContext;
    eventBus: EventBus;
}

declare interface EventBus {
    fire<E>(type: EventType<E>, event: E): EventBus;
    addHandler<E>(type: EventType<E>, handler: { (event: E): void }): void;
}
declare interface EventType<E> {
    type(): string;
}
declare interface PluginContext {
    getApi(): CheApi;
    addDisposable(d: Disposable): void
}

declare interface Disposable {
    dispose(): void;
}
/**
 * Represents current context of the IDE application.
 */
declare interface AppContext {

    /**
     * Returns the workspace root container, which is holder of registered projects.
     *
     * @return the workspace root
     */
    getWorkspaceRoot(): che.ide.resource.Container;
    /**
     * Returns the registered projects in current workspace. If no projects were registered before,
     * then empty array is returned.
     *
     * @return the registered projects
     */
    getProjects(): che.ide.resource.Project[];
    /**
     * Returns the resource which is in current context. By current context means, that resource may
     * be in use in specified part if IDE. For example, project part may provide resource which is
     * under selection at this moment, editor may provide resource which is open, full text search may
     * provide resource which is under selection.
     *
     * <p>If specified part provides more than one resource, then last selected resource is returned.
     *
     * <p>May return {@code null} if there is no resource in context.
     *
     * @return the resource in context
     */
    getResource(): che.ide.resource.Resource;

    /**
     * Returns the resources which are in current context. By current context means, that resources
     * may be in use in specified part if IDE. For example, project part may provide resources which
     * are under selection at this moment, editor may provide resource which is open, full text search
     * may provide resources which are under selection.
     *
     * <p>If specified part provides more than one resource, then all selected resources are returned.
     *
     * <p>May return {@code null} if there is no resources in context.
     *
     * @return the resource in context
     */
    getResources(): che.ide.resource.Resource[];
    /**
     * Returns the path where projects are stored on file system.
     *
     * @return the path to projects root.
     */
    getProjectsRoot(): che.ide.resource.Path;
    //TODO
    //getWorkspace(): WorkspaceImpl;

    /**
     * Returns the root project which is in context. To find out specified sub-project in context,
     * method {@link #getResource()} should be called. Resource is bound to own project and to get
     * {@link Project} instance from {@link Resource}, method {@link Resource#getRelatedProject()}
     * should be called.
     *
     * <p>May return {@code null} if there is no project in context.
     *
     * @return the root project or {@code null}
     */
    getRootProject(): che.ide.resource.Project;

    /**
    * Returns the current user.
    *
    * @return current user
    */
    getCurrentUser(): CurrentUser;

    getWorkspaceId(): string;

    /**
     * Returns URL of Che Master API endpoint.
     */
    getMasterApiEndpoint(): string;

    /**
     * Returns URL of ws-agent server API endpoint.
     *
     * @throws RuntimeException if ws-agent server doesn't exist. Normally it may happen when
     *     workspace is stopped.
     */
    getWsAgentServerApiEndpoint(): string;

    /**
    * Returns context properties, key-value storage that allows to store data in the context for
    * plugins and extensions.
    *
    * @return a modifiable properties map
    */
    getProperties(): { [key: string]: string }

}



declare interface CurrentUser {
    getId(): string;
    getPreferences(): { [key: string]: string };
}

declare interface EditorManager {
    //TODO
}

declare interface EditorPartPresenter {
    //TODO
}


declare interface PartManager {
    /**
   * Sets passed part as active. Sets focus to part and open it.
   *
   * @param part part which will be active
   */
    activatePart(part: Part): void;

    /**
     * Check is given part is active
     *
     * @return true if part is active
     */
    isActivePart(part: Part): boolean;

    /**
     * Opens given Part
     *
     * @param part
     * @param type
     */
    openPart(part: Part, type: che.ide.parts.PartStackType): void;

    /**
     * Hides given Part
     *
     * @param part
     */
    hidePart(part: Part): void;

    /**
     * Remove given Part
     *
     * @param part
     */
    removePart(part: Part): void;
}

declare interface Part {
    /** @return Title of the Part */
    getTitle(): string;

    /**
     * Returns count of unread notifications. Is used to display a badge on part button.
     *
     * @return count of unread notifications
     */
    getUnreadNotificationsCount(): number;

    /**
     * Returns the title tool tip text of this part. An empty string result indicates no tool tip. If
     * this value changes the part must fire a property listener event with <code>PROP_TITLE</code>.
     *
     * <p>The tool tip text is used to populate the title bar of this part's visual container.
     *
     * @return the part title tool tip (not <code>null</code>)
     */
    getTitleToolTip(): string;

    /**
     * Return size of part. If current part is vertical panel then size is height. If current part is
     * horizontal panel then size is width.
     *
     * @return size of part
     */
    getSize(): number;

    /**
     * This method is called when Part is opened. Note: this method is NOT called when part gets
     * focused. It is called when new tab in PartStack created.
     */
    onOpen(): void;

    /** @return */
    getView(): Element;

    getImageId(): string;
}

declare namespace che {
    namespace ide {
        namespace resource {
            /**
             * A path is an ordered collection of string segments, separated by a standard separator
             * character, "/". A path may also have a leading and/or a trailing separator.
             *
             * Note that paths are value objects; all operations on paths return a new path; the path that is
             * operated on is unscathed.
             *
             * This class is not intended to be extended by clients.
             */
            class Path {
                SEPARATOR: string;
                /**
                 * Constructs a new path from the given string path. The string path must represent a valid file
                 * system path on the local file system. The path is canonicalized and double slashes are removed
                 * except at the beginning. (to handle UNC paths). All forward slashes ('/') are treated as
                 * segment delimiters, and any segment and device delimiters for the local file system are also
                 * respected.
                 */
                static valueOf(pathstring: string): Path;

                /**
                 * Returns a new path which is the same as this path but with the given file extension added. If
                 * this path is empty, root or has a trailing separator, this path is returned. If this path
                 * already has an extension, the existing extension is left and the given extension simply
                 * appended. Clients wishing to replace the current extension should first remove the extension
                 * and then add the desired one.
                 *
                 * <p>The file extension portion is defined as the string following the last period (".")
                 * character in the last segment. The given extension should not include a leading ".".
                 * @param extension the file extension to append
                 */
                addFileExtension(extension: string): Path;
                /**
                 * Returns a path with the same segments as this path but with a trailing separator added. This
                 * path must have at least one segment.
                 *
                 * If this path already has a trailing separator, this path is returned.
                 */
                addTrailingSeparator(): Path;

                /**
                 * Returns a path with the same segments as this path but with a leading separator added.
                 *
                 * <p>If this path already has a leading separator, this path is returned.
                 *
                 * @return the new path
                 */
                addLeadingSeparator(): Path;

                /**
                 * Returns the canonicalized path obtained from the concatenation of the given path's segments to
                 * the end of this path. If the given path has a trailing separator, the result will have a
                 * trailing separator. The device id of this path is preserved (the one of the given path is
                 * ignored). Duplicate slashes are removed from the path except at the beginning where the path is
                 * considered to be UNC.
                 */
                appendPath(path: Path): Path;

                /**
                 * Returns the canonicalized path obtained from the concatenation of the given path's segments to
                 * the end of this path. If the given path has a trailing separator, the result will have a
                 * trailing separator. The device id of this path is preserved (the one of the given path is
                 * ignored). Duplicate slashes are removed from the path except at the beginning where the path is
                 * considered to be UNC.
                 */
                append(path: string): Path;

                equals(obj: any): boolean;

                /**
                 * Returns the device id for this path, or <code>null</code> if this path has no device id. Note
                 * that the result will end in ':'.
                 *
                 * @return the device id, or <code>null</code>
                 */
                getDevice(): string;
                /**
                 * Returns the file extension portion of this path, or <code>null</code> if there is none.
                 *
                 * <p>The file extension portion is defined as the string following the last period (".")
                 * character in the last segment. If there is no period in the last segment, the path has no file
                 * extension portion. If the last segment ends in a period, the file extension portion is the
                 * empty string.
                 *
                 * @return the file extension or <code>null</code>
                 */
                getFileExtension(): string;
                /**
                 * Returns whether this path has a trailing separator.
                 *
                 * <p>Note: In the root path ("/"), the separator is considered to be leading rather than
                 * trailing.
                 *
                 * @return <code>true</code> if this path has a trailing separator, and <code>false</code>
                 *     otherwise
                 */
                hasTrailingSeparator(): boolean;

                /**
                 * Returns whether this path has a leading separator.
                 *
                 * <p>Note: In the root path ("/"), the separator is considered to be leading rather than
                 * trailing.
                 *
                 * @return <code>true</code> if this path has a leading separator, and <code>false</code>
                 *     otherwise
                 */
                hasLeadingSeparator(): boolean;
                /**
                 * Returns whether this path is an absolute path (ignoring any device id).
                 *
                 * <p>Absolute paths start with a path separator. A root path, like <code>/</code> or <code>C:/
                 * </code>, is considered absolute. UNC paths are always absolute.
                 *
                 * @return <code>true</code> if this path is an absolute path, and <code>false</code> otherwise
                 */
                isAbsolute(): boolean;

                /**
                 * Returns whether this path has no segments and is not a root path.
                 *
                 * @return <code>true</code> if this path is empty, and <code>false</code> otherwise
                 */
                isEmpty(): boolean;
                /**
                 * Returns whether this path is a prefix of the given path. To be a prefix, this path's segments
                 * must appear in the argument path in the same order, and their device ids must match.
                 *
                 * <p>An empty path is a prefix of all paths with the same device; a root path is a prefix of all
                 * absolute paths with the same device.
                 *
                 * @param anotherPath the other path
                 * @return <code>true</code> if this path is a prefix of the given path, and <code>false</code>
                 *     otherwise
                 */
                isPrefixOf(anotherPath: Path): boolean;

                /**
                 * Returns whether this path is a root path.
                 *
                 * <p>The root path is the absolute non-UNC path with zero segments; e.g., <code>/</code> or
                 * <code>C:/</code>. The separator is considered a leading separator, not a trailing one.
                 *
                 * @return <code>true</code> if this path is a root path, and <code>false</code> otherwise
                 */
                isRoot(): boolean;

                /**
                 * Returns a boolean value indicating whether or not this path is considered to be in UNC form.
                 * Return false if this path has a device set or if the first 2 characters of the path string are
                 * not <code>Path.SEPARATOR</code>.
                 *
                 * @return boolean indicating if this path is UNC
                 */
                isUNC(): boolean;

                /**
                 * Returns the last segment of this path, or <code>null</code> if it does not have any segments.
                 *
                 * @return the last segment of this path, or <code>null</code>
                 */
                lastSegment(): string;

                /**
                 * Returns an absolute path with the segments and device id of this path. Absolute paths start
                 * with a path separator. If this path is absolute, it is simply returned.
                 *
                 * @return the new path
                 */
                makeAbsolute(): Path;

                /**
                 * Returns a relative path with the segments and device id of this path. Absolute paths start with
                 * a path separator and relative paths do not. If this path is relative, it is simply returned.
                 *
                 * @return the new path
                 */
                makeRelative(): Path;

                /**
                 * Returns a path equivalent to this path, but relative to the given base path if possible.
                 *
                 * <p>The path is only made relative if the base path if both paths have the same device and have
                 * a non-zero length common prefix. If the paths have different devices, or no common prefix, then
                 * this path is simply returned. If the path is successfully made relative, then appending the
                 * returned path to the base will always produce a path equal to this path.
                 *
                 * @param base The base path to make this path relative to
                 * @return A path relative to the base path, or this path if it could not be made relative to the
                 *     given base
                 */
                makeRelativeTo(base: Path): Path;
                /**
                 * Returns a count of the number of segments which match in this path and the given path (device
                 * ids are ignored), comparing in increasing segment number order.
                 *
                 * @param anotherPath the other path
                 * @return the number of matching segments
                 */
                matchingFirstSegments(anotherPath: Path): number;

                /**
                 * Returns a new path which is the same as this path but with the file extension removed. If this
                 * path does not have an extension, this path is returned.
                 *
                 * <p>The file extension portion is defined as the string following the last period (".")
                 * character in the last segment. If there is no period in the last segment, the path has no file
                 * extension portion. If the last segment ends in a period, the file extension portion is the
                 * empty string.
                 *
                 * @return the new path
                 */
                removeFileExtension(): Path;

                /**
                 * Returns a copy of this path with the given number of segments removed from the beginning. The
                 * device id is preserved. The number must be greater or equal zero. If the count is zero, this
                 * path is returned. The resulting path will always be a relative path with respect to this path.
                 * If the number equals or exceeds the number of segments in this path, an empty relative path is
                 * returned.
                 *
                 * @param count the number of segments to remove
                 */
                removeFirstSegments(count: number): Path;
                /**
                 * Returns a copy of this path with the given number of segments removed from the end. The device
                 * id is preserved. The number must be greater or equal zero. If the count is zero, this path is
                 * returned.
                 *
                 * <p>If this path has a trailing separator, it will still have a trailing separator after the
                 * last segments are removed (assuming there are some segments left). If there is no trailing
                 * separator, the result will not have a trailing separator. If the number equals or exceeds the
                 * number of segments in this path, a path with no segments is returned.
                 *
                 * @param count the number of segments to remove
                 */
                removeLastSegments(count: number): Path;
                /**
                 * Returns a path with the same segments as this path but with a trailing separator removed. Does
                 * nothing if this path does not have at least one segment. The device id is preserved.
                 *
                 * <p>If this path does not have a trailing separator, this path is returned.
                 *
                 * @return the new path
                 */
                removeTrailingSeparator(): Path;

                /**
                 * Returns the specified segment of this path, or <code>null</code> if the path does not have such
                 * a segment.
                 *
                 * @param index the 0-based segment index
                 * @return the specified segment, or <code>null</code>
                 */
                segment(index: number): string;
                /**
                 * Returns the number of segments in this path.
                 *
                 * <p>Note that both root and empty paths have 0 segments.
                 *
                 * @return the number of segments
                 */
                segmentCount(): number;

                /**
                 * Returns the segments in this path in order.
                 *
                 * @return an array of string segments
                 */
                segments(): string[];

                /**
                 * Returns a new path which is the same as this path but with the given device id. The device id
                 * must end with a ":". A device independent path is obtained by passing <code>null</code>.
                 *
                 * <p>For example, "C:" and "Server/Volume:" are typical device ids.
                 *
                 * @param device the device id or <code>null</code>
                 * @return a new path
                 */
                setDevice(device: string): Path;

                /**
                 * Returns a string representation of this path, including its device id. The same separator, "/",
                 * is used on all platforms.
                 *
                 * <p>Example result strings (without and with device id):
                 *
                 * <pre>
                 * "/foo/bar.txt"
                 * "bar.txt"
                 * "/foo/"
                 * "foo/"
                 * ""
                 * "/"
                 * "C:/foo/bar.txt"
                 * "C:bar.txt"
                 * "C:/foo/"
                 * "C:foo/"
                 * "C:"
                 * "C:/"
                 * </pre>
                 *
                 * This string is suitable for passing to <code>Path(string)</code>.
                 *
                 * @return a string representation of this path
                 */
                tostring(): string;
                /**
                 * Returns a copy of this path with removed last segment.
                 *
                 * @return the new path
                 */
                parent(): Path;
            }
            /**
             * Markers are a general mechanism for associating notes and meta-data with resources.
             *
             * <p>Each marker has a type string, specifying its unique id. The resources plugin defines only one
             * standard marker (at this moment): {@link ProblemProjectMarker#PROBLEM_PROJECT}.
             *
             * <p>Marker, by nature is only runtime attribute and doesn't store on the server side.
             */
            class Marker {
                /**
                 * Kind constant (bit mask) indicating that the marker has been created to given resource.
                 */
                static CREATED: number;

                /**
                 * Kind constant (bit mask) indicating that the marker has been removed from given resource.
                 */
                static REMOVED: number;

                /**
                 * Kind constant (bit mask) indicating that the marker has been updated to given resource.
                 */
                static UPDATED: number;

                /**
                 * Returns the type of this marker. The returned marker type will not be {@code null}.
                 *
                 * @return the type of this marker
                 */
                getType(): string;
            }

            /**
             * A resource delta represents changes in the state of concrete resource.
             */
            class ResourceDelta {

                /**
                 * Delta kind constant (bit mask) indicating that the resource has been added to its its parent.
                 */
                static ADDED: number;

                /**
                 * Delta kind constant (bit mask) indicating that the resource has been removed from its parent.
                 */
                static REMOVED: number;

                /**
                 * Delta kind constant (bit mask) indicating that the resource has been updated.
                 */
                static UPDATED: number;

                /**
                 * Delta kind constant (bit mask) indicating that resource (usually container-based) has been
                 * synchronized.
                 */
                static SYNCHRONIZED: number;

                /**
                 * Change constant (bit mask) indicating that the content of the resource has changed.
                 */
                static CONTENT: number;

                /**
                 * Change constant (bit mask) indicating that the resource was moved from another location. The
                 * location can be retrieved using {@link ResourceDelta#getFromPath()}
                 */
                static MOVED_FROM: number;

                /**
                 * Change constant (bit mask) indicating that the resource was moved to another location. The
                 * location can be retrieved using {@link ResourceDelta#getToPath()}
                 */
                static MOVED_TO: number;

                /**
                 * Change constant (bit mask) indicating that the resource was copied from another location. The
                 * location can be retrieved using {@link ResourceDelta#getFromPath()}
                 */
                static COPIED_FROM: number;

                /**
                 * Change constant (bit mask) indicating that the resource was generated after user actions, not
                 * automatically.
                 */
                static DERIVED: number;

                /**
                 * Returns the kind of this resource delta. Normally, one of {@code ADDED}, {@code REMOVED},
                 * {@code UPDATED}, {@code LOADED}, {@code UNLOADED}.
                 *
                 * @return the kind of this resource delta.
                 */
                getKind(): number;

                /**
                 * Returns flags which describe in more detail how a resource has been affected.
                 *
                 * <p>The following codes (bit masks) are used when kind is {@code UPDATED}, and also when the
                 * resource is involved in a move:
                 *
                 * <ul>
                 *   <li>{@code CONTENT} - The bytes contained by the resource have been altered, or <code>
                 *       IResource.touch</code> has been called on the resource.
                 *   <li>{@code MOVED_FROM} - The resource has moved. {@link #getFromPath()} will return the path
                 *       of where it was moved from.
                 *   <li>{@code MOVED_TO} - The resource has moved. {@link #getToPath()} ()} will return the path
                 *       of where it was moved to.
                 *   <li>{@code COPIED_FROM} - Change constant (bit mask) indicating that the resource was copied
                 *       from another location. The location can be retrieved using {@link #getFromPath()}.
                 * </ul>
                 *
                 * <p>A simple move operation would result in the following delta information. If a resource is
                 * moved from A to B (with no other changes to A or B), then A will have kind {@code REMOVED},
                 * with flag {@code MOVED_TO}, and {@link #getToPath()} on A will return the path for B. B will
                 * have kind {@code ADDED}, with flag {@code MOVED_FROM}, and {@link #getFromPath()} on B will
                 * return the path for A.
                 *
                 * @return the flags
                 */
                getFlags(): number;

                /**
                 * Returns the path from which resource was moved. This value is valid if the {@code MOVED_FROM}
                 * or {@code COPIED_FROM} change flag is set, otherwise, {@code null} is returned.
                 *
                 * @return instance of {@link Path} or {@code null}
                 */
                getFromPath(): Path;

                /**
                 * Returns the path to which resource was moved. This value is valid if the {@code MOVED_TO}
                 * change flag is set, otherwise, {@code null} is returned.
                 *
                 * @return instance of {@link Path} or {@code null}
                 */
                getToPath(): Path;

                /**
                 * Returns a handle for the affected resource.
                 *
                 * @return the affected resource
                 */
                getResource(): Resource;
            }

            interface ProjectRequest extends Request<Project, ProjectConfig> {
                getBody(): ProjectConfig;
            }

            interface Request<R extends Resource, O> {
                withBody(object: O): Request<R, O>;

                getBody(): O;

                send(): Promise<R>;
            }

            class ProjectConfig {
                getName(): string;

                getPath(): string;

                getDescription(): string;

                getType(): string;

                getMixins(): string[];

                getAttributes(): che.util.Map<string, string[]>;

                getSource(): SourceStorage;

                getProblems(): ProjectProblem[];
            }

            class SourceStorage {
                getType(): string;

                getLocation(): string;

                getParameters(): che.util.Map<string, string>;
            }

            /** Class contains an information about result of the text search operation. */
            class SearchResult {
                constructor(itemReferences: SearchItemReference[], totalHits: number);

                getItemReferences(): SearchItemReference[];

                getTotalHits(): number;
            }

            class SearchItemReference {
                getName(): string;
                setName(name: string): void;
                getPath(): string;
                setPath(path: string): void;
                getProject(): string;
                setProject(project: string): void;
                getOccurrences(): SearchOccurrence[];
                setOccurrences(occurrences: SearchOccurrence[]): void;
                getContentUrl(): string;
                setContentUrl(contentUrl: string): void;
            }

            /**
             * Contain information about occurrence of found phrase like: - start and send offsets in the file
             * for found phrase; - number and content of line where given phrase found; - found phrase itself;
             */
            class SearchOccurrence {
                getScore(): number;

                /** @param score */
                setScore(score: number): void;

                /**
                 * Found phrase (eg if you try to find 'hel' and text contain 'hello', in this case phrase will be
                 * 'hello')
                 *
                 * @return
                 */
                getPhrase(): string;

                /** @param phrase */
                setPhrase(phrase: string): void;

                /**
                 * End offset in file of found phrase
                 *
                 * @return
                 */
                getEndOffset(): number;

                /** @param endOffset */
                setEndOffset(endOffset: number): void;

                /**
                 * Begin offset in file of found phrase
                 *
                 * @return
                 */
                getStartOffset(): number;

                /** @param startOffset */
                setStartOffset(startOffset: number): void;

                /**
                 * Number of line where phrase found
                 *
                 * @param lineNumber
                 */
                setLineNumber(lineNumber: number): void;

                /**
                 * Number of line where phrase found
                 *
                 * @return
                 */
                getLineNumber(): number;

                /**
                 * Content of the line where phrase found
                 *
                 * @param lineContent
                 */
                setLineContent(lineContent: string): void;

                /**
                 * Content of the line where phrase found
                 *
                 * @return
                 */
                getLineContent(): void;
            }

            class QueryExpression {
                /**
                 * Get path to start search.
                 *
                 * @return path to start search
                 */
                getPath(): string;

                /**
                 * Set path to start search.
                 *
                 * @param path path to start search
                 * @return this {@code QueryExpression}
                 */
                setPath(path: string): QueryExpression;

                /**
                 * Get name of file to search.
                 *
                 * @return file name to search
                 */
                getName(): string;

                /**
                 * Set name of file to search.
                 *
                 * <p>Supported wildcards are:
                 *
                 * <ul>
                 *   <li><code>*</code>, which matches any character sequence (including the empty one);
                 *   <li><code>?</code>, which matches any single character.
                 * </ul>
                 *
                 * @param name file name to search
                 * @return this {@code QueryExpression}
                 */
                setName(name: string): QueryExpression

                /**
                 * Get text to search.
                 *
                 * @return text to search
                 */
                getText(): string;

                /**
                 * Set text to search.
                 *
                 * @param text text to search
                 * @return this {@code QueryExpression}
                 */
                setText(text: string): QueryExpression;

                /**
                 * Get maximum number of items in response.
                 *
                 * @return maximum number of items in response
                 */
                getMaxItems(): number;

                /**
                 * Set maximum number of items in response.
                 *
                 * @param maxItems maximum number of items in response
                 * @return this {@code QueryExpression}
                 */
                setMaxItems(maxItems: number): QueryExpression;

                /**
                 * Get amount of items to skip.
                 *
                 * @return amount of items to skip
                 */
                getSkipCount(): number;

                /**
                 * Set amount of items to skip.
                 *
                 * @param skipCount amount of items to skip
                 * @return this {@code QueryExpression}
                 */
                setSkipCount(skipCount: number): QueryExpression
            }
            /**
             * The client side analog of file system files and directories. There are exactly three types of
             * resources: files, folders and projects.
             *
             * <p>Workspace root is representing by {@link Container}. In which only {@link Project} is allowed
             * to be created.
             *
             * <p>File resources are similar to files in that they hold data directly. Folder resources are
             * analogous to directories in that they hold other resources but cannot directly hold data. Project
             * resources group files and folders into reusable clusters.
             *
             * <p>Features of resources:
             *
             * <ul>
             *   <li>{@code Resource} objects are handles to state maintained by a workspace. That is, resources
             *       objects do not actually contain data themselves but rather represent resource state and
             *       give it behaviour.
             *   <li>Resources are identified by type and their {@code path}, which is similar to a file system
             *       path. The name of the resource is the last segment of its path. A resource's parent is
             *       located by removing the last segment (the resource's name) from the resource's full path.
             * </ul>
             *
             * <p>To obtain already initialized resource in workspace you just need to inject {@link AppContext}
             * into your component and call {@link AppContext#getProjects()} or {@link
             * AppContext#getWorkspaceRoot()}.
             *
             * <p>Note. This interface is not intended to be implemented by clients.
             */
            class Resource {
                /**
                 * Type constant that describes {@code File} resource.
                 */
                static FILE: number;

                /**
                 * Type constant that describes {@code Folder} resource.
                 */
                static FOLDER: number;
                /**
                 * Type constant that describes {@code Project} resource.
                 */
                static PROJECT: number;
                /**
                 * Returns {@code true} if current represents a file.
                 *
                 * @return true if current resource is file based resource.
                 */
                isFile(): boolean;
                /**
                 * Casts current resource to the {@link File} if the last one's represents a file.
                 *
                 * <p>Example of usage:
                 *
                 * <pre>
                 *    public void doSome() {
                 *        Resource resource = ...;
                 *        if (resource.isFile()) {
                 *            File file = resource.asFile();
                 *        }
                 *    }
                 * </pre>
                 *
                 * @return instance of {@link File}
                 * @throws IllegalStateException in case if current resource is not a file
                 */
                asFile(): File;

                /**
                 * Returns {@code true} if current represents a folder.
                 *
                 * @return true if current resource is folder based resource.
                 */
                isFolder(): boolean;
                /**
                 * Casts current resource to the {@link Folder} if the last one's represents a folder.
                 *
                 * <p>Example of usage:
                 *
                 * <pre>
                 *    public void doSome() {
                 *        Resource resource = ...;
                 *        if (resource.isFolder()) {
                 *            Folder folder = resource.asFolder();
                 *        }
                 *    }
                 * </pre>
                 *
                 * @return instance of {@link Folder}
                 * @throws IllegalStateException in case if current resource is not a folder
                 */
                asFolder(): Folder;

                /**
                 * Returns {@code true} if current represents a project.
                 *
                 * @return true if current resource is project based resource.
                 */
                isProject(): boolean;

                /**
                 * Casts current resource to the {@link Project} if the last one's represents a project.
                 *
                 * <p>Example of usage:
                 *
                 * <pre>
                 *    public void doSome() {
                 *        Resource resource = ...;
                 *        if (resource.isProject()) {
                 *            Project project = resource.asProject();
                 *        }
                 *    }
                 * </pre>
                 *
                 * @return instance of {@link Project}
                 * @throws IllegalStateException in case if current resource is not a project
                 */
                asProject(): Project;
                /**
                 * Copies resource to given {@code destination} path. Copy operation performs asynchronously and
                 * result of current operation will be provided in {@code Promise} result. Destination path should
                 * have write access.
                 *
                 * <p>Copy operation produces new {@link Resource} which is already cached.
                 *
                 * <p>Fires following events: {@link ResourceChangedEvent} when resource has successfully copied.
                 * This event provides information about copied resource and source resource.
                 *
                 * <p>Passing {@code force} argument as true method will ignore existed resource on the server and
                 * overwrite them.
                 *
                 * <p>Example of usage:
                 *
                 * <pre>
                 *     Resource resource = ... ;
                 *     Path copyTo = ... ;
                 *
                 *     resource.copy(copyTo, true).then(new Operation<Resource>() {
                 *          public void apply(Resource copiedResource) throws OperationException {
                 *              //do something with copiedResource
                 *          }
                 *     })
                 * </pre>
                 *
                 * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
                 * {@link ResourceDelta#ADDED}. Copied resource provided by {@link ResourceDelta#getResource()}.
                 * Contains flags {@link ResourceDelta#COPIED_FROM}. Source resource is accessible by calling
                 * {@link ResourceDelta#getFromPath()}.
                 *
                 * @param destination the destination path
                 * @param force overwrite existed resource on the server
                 * @return {@link Promise} with copied {@link Resource}
                 * @throws IllegalStateException if this resource could not be copied. Reasons include:
                 *     <ul>
                 *       <li>Resource already exists
                 *       <li>Resource with path '/path' isn't a project
                 *     </ul>
                 *
                 * @throws IllegalArgumentException if current resource can not be copied. Reasons include:
                 *     <ul>
                 *       <li>Workspace root is not allowed to be copied
                 *     </ul>
                 */
                copy(destination: Path, force: boolean): Promise<Resource>;

                /**
                * Moves resource to given new {@code destination}. Move operation performs asynchronously and
                * result of current operation will be displayed in {@code Promise} result.
                *
                * <p>Move operation produces new {@link Resource} which is already cached.
                *
                * <p>Fires following events: {@link ResourceChangedEvent} when resource has successfully moved.
                * This event provides information about moved resource.
                *
                * <p>Before moving mechanism remembers deepest depth which was read and tries to restore it after
                * move.
                *
                * <p>Passing {@code force} argument as true method will ignore existed resource on the server and
                * overwrite them.
                *
                * <p>Example of usage:
                *
                * <pre>
                *     Resource resource = ... ;
                *     Path moveTo = ... ;
                *
                *     resource.move(moveTo, true).then(new Operation<Resource>() {
                *          public void apply(Resource movedResource) throws OperationException {
                *              //do something with movedResource
                *          }
                *     })
                * </pre>
                *
                * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
                * {@link ResourceDelta#REMOVED}. Removed resource is provided by {@link
                * ResourceDelta#getResource()}.
                *
                * <p>Also fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta
                * kind: {@link ResourceDelta#ADDED}. Moved resource provided by {@link
                * ResourceDelta#getResource()}. Contains flags {@link ResourceDelta#MOVED_FROM} and {@link
                * ResourceDelta#MOVED_TO}. Source resource is accessible by calling {@link
                * ResourceDelta#getFromPath()}. Moved resource (or new resource) is accessible by calling {@link
                * ResourceDelta#getToPath()}.
                *
                * @param destination the destination path
                * @return {@code Promise} with move moved {@link Resource}
                * @throws IllegalStateException if this resource could not be moved. Reasons include:
                *     <ul>
                *       <li>Resource already exists
                *       <li>Resource with path '/path' isn't a project
                *     </ul>
                *
                * @throws IllegalArgumentException if current resource can not be moved. Reasons include:
                *     <ul>
                *       <li>Workspace root is not allowed to be moved
                *     </ul>
                */
                move(destination: Path, force: boolean): Promise<Resource>;

                /**
                 * Deletes current resource. Delete operation performs asynchronously and result of current
                 * operation will be displayed in {@code Promise} result as {@code void}.
                 *
                 * <p>Fires following events: {@link ResourceChangedEvent} when resource has successfully removed.
                 *
                 * <p>Example of usage:
                 *
                 * <pre>
                 *     Resource resource = ... ;
                 *
                 *     resource.delete().then(new Operation<Void>() {
                 *         public void apply(Void ignored) throws OperationException {
                 *             //do something
                 *         }
                 *     })
                 * </pre>
                 *
                 * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
                 * {@link ResourceDelta#REMOVED}. Removed resource provided by {@link ResourceDelta#getResource()}
                 *
                 * @return {@code Promise} with {@code void}
                 * @throws IllegalArgumentException if current resource can not be removed. Reasons include:
                 *     <ul>
                 *       <li>Workspace root is not allowed to be removed
                 *     </ul>
                 */
                delete(): Promise<void>;

                /**
                 * Returns the full, absolute path of this resource relative to the project's root. e.g. {@code
                 * "/project_name/path/to/resource"}.
                 *
                 * @return the absolute path of this resource
                 * @see Path
                 * @since 4.4.0
                 */
                getLocation(): Path;

                /**
                 * Returns the name of the resource. The name of a resource is synonymous with the last segment of
                 * its full (or project-relative) path.
                 *
                 * @return the name of the resource
                 * @since 4.4.0
                 */
                getName(): string;

                /**
                 * Returns the resource which is the parent of this resource or {@code null} if such parent
                 * doesn't exist. (This means that this resource is 'root' project)
                 *
                 * @return the resource's parent {@link Container}
                 */
                getParent(): Container;

                /**
                 * Returns the {@link Project} which is bound to this resource or {@code null}.
                 *
                 * <p>Returns itself for projects.
                 *
                 * @return the bound instance of {@link Project} or null
                 */
                getProject(): Project;
                /**
                 * Returns the type of this resource. Th returned value will be on of {@code FILE}, {@code
                 * FOLDER}, {@code PROJECT}.
                 *
                 * <p>
                 *
                 * <ul>
                 *   <li>All resources of type {@code FILE} implement {@code File}.
                 *   <li>All resources of type {@code FOLDER} implement {@code Folder}.
                 *   <li>All resources of type {@code PROJECT} implement {@code Project}.
                 * </ul>
                 *
                 * @return the type of this resource
                 */
                getResourceType(): number;
                /**
                 * Returns the URL of this resource. The URL allows to download locally current resource.
                 *
                 * <p>For container based resource the URL link will allow download container as zip archive.
                 *
                 * @return the URL of the resource
                 * @throws IllegalArgumentException if URL is requested on workspace root. Reasons include:
                 *     <ul>
                 *       <li>Workspace root doesn't have export URL
                 *     </ul>
                 */
                getURL(): string;

                /**
                 * Returns all markers of the specified type on this resource. If there is no marker bound to the
                 * resource, then empty array will be returned.
                 *
                 * @return the array of markers
                 */
                getMarkers(): Marker[];

                /**
                 * Bound given {@code marker} to current resource. if such marker is already bound to the resource
                 * it will be overwritten.
                 *
                 * <p>Fires following events: {@link MarkerChangedEvent} with status {@link Marker#UPDATED} when
                 * existed marker has been replaced with new one. {@link MarkerChangedEvent} with status {@link
                 * Marker#CREATED} when marker has been added to the current resource.
                 *
                 * @param marker the resource marker
                 * @throws IllegalArgumentException in case if given marker is invalid. Reasons include:
                 *     <ul>
                 *       <li>Null marker occurred
                 *     </ul>
                 */
                addMarker(marker: Marker): void;

                /**
                 * Delete specified marker with given {@code type}.
                 *
                 * <p>Fires following event: {@link MarkerChangedEvent} with status {@link Marker#REMOVED} when
                 * given marker has been removed from current resource.
                 *
                 * @param type the marker type
                 * @return true if specified marker removed
                 * @throws IllegalArgumentException in case if given marker type is invalid (null or empty).
                 *     Reasons include:
                 *     <ul>
                 *       <li>Invalid marker type occurred
                 *     </ul>
                 */
                deleteMarker(type: string): boolean;
                /**
                 * Delete all markers which is bound to current resource.
                 *
                 * @return true if all markers has been removed
                 */
                deleteAllMarkers(): boolean;
            }

            /**
             * Files are leaf resources which contain data. The contents of a file resource is stored as a file
             * in the local file system.
             *
             * <p>File extends also {@link VirtualFile}, so this resource can be easily opened in editor.
             *
             * <p>File instance can be obtained by calling {@link Container#getFile(Path)} or by {@link
             * Container#getChildren(boolean)}.
             *
             * <p>Note. This interface is not intended to be implemented by clients.
             */
            class File extends Resource implements VirtualFile {
                /** @see VirtualFile#getDisplayName() */
                getDisplayName(): string;

                /** @see VirtualFile#isReadOnly() */
                isReadOnly(): boolean;

                /** @see VirtualFile#getContentUrl() */
                getContentUrl(): string;

                /** @see VirtualFile#getContent() */
                getContent(): Promise<string>;

                /** @see VirtualFile#updateContent(String) */
                updateContent(content: string): Promise<void>;

                /**
                 * Returns the file extension portion of this resource's name or {@code null} if it does not have
                 * one.
                 *
                 * @return a string file extension or {@code null}
                 */
                getExtension(): string;

                /**
                 * Returns the name without the extension. If file name contains '.' the substring till the last
                 * '.' is returned. Otherwise the same value as {@link #getName()} method returns is returned.
                 *
                 * @return the name without extension
                 */
                getNameWithoutExtension(): string;
            }

            /**
             * Interface for resource which may contain other resources (termed its members).
             *
             * <p>If {@code location} of current container is equals to {@link Path#ROOT} then it means that
             * current container represent the workspace root. To obtain the workspace root {@link AppContext}
             * should be injected into third- party component and method {@link AppContext#getWorkspaceRoot()}
             * should be called. Only {@link Project}s are allowed to be created in workspace root.
             *
             * <p>Note. This interface is not intended to be implemented by clients.
             */
            class Container extends Resource {
                /**
                 * Returns the {@code Promise} with array of existing member resources (projects, folders and
                 * files) in this resource, in particular order. Order is organized by alphabetic resource name
                 * ignoring case.
                 *
                 * <p>Supplied parameter {@code force} instructs that stored children should be updated.
                 *
                 * <p>Note, that if supplied argument {@code force} is set to {@code false} and result array is
                 * empty, then method thinks that children may not be loaded from the server and send a request ot
                 * the server to load the children.
                 *
                 * <p>Method guarantees that resources will be sorted by their {@link #getLocation()} in ascending
                 * order.
                 *
                 * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
                 * {@link ResourceDelta#ADDED}. Cached and loaded resource provided by {@link
                 * ResourceDelta#getResource()}.
                 *
                 * <p>Or
                 *
                 * <p>Delta kind: {@link ResourceDelta#UPDATED}. When resource was cached previously. Updated
                 * resource provided by {@link ResourceDelta#getResource()}.
                 *
                 * <p>May fire {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
                 * {@link ResourceDelta#REMOVED}. Removed resource provided by {@link
                 * ResourceDelta#getResource()}. In case if {@code force} is set in {@code true}.
                 *
                 * @return the {@code Promise} with array of members of this resource
                 */
                getChildren(force: boolean): Promise<Resource[]>;
                /**
                 * Creates the new {@link Project} in current container.
                 *
                 * <p>Fires following events: {@link ResourceChangedEvent} when project has successfully created.
                 *
                 * <p>Calling this method doesn't create a project immediately. To complete the request method
                 * {@link ProjectRequest#send()} should be called. {@link ProjectRequest} has ability to
                 * reconfigure project during update/create operations.
                 *
                 * <p>Calling {@link ProjectRequest#send()} produces new {@link Project} resource.
                 *
                 * <p>The supplied argument {@code name} should be a valid and pass validation within {@link
                 * NameUtils#checkProjectName(String)}. The supplied argument {@code type} should be a valid and
                 * registered project type.
                 *
                 * <p>
                 *
                 * <p>Example of usage for creating a new project:
                 *
                 * <pre>
                 *     ProjectConfig config = ... ;
                 *     Container workspace = ... ;
                 *
                 *     Promise<Project> newProjectPromise = workspace.newProject()
                 *                                                   .withBody(config)
                 *                                                   .send();
                 *
                 *     newProjectPromise.then(new Operation<Project>() {
                 *         public void apply(Project newProject) throws OperationException {
                 *              //do something with new project
                 *         }
                 *     });
                 * </pre>
                 *
                 * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
                 * {@link ResourceDelta#ADDED}. Created resource (instance of {@link Project}) provided by {@link
                 * ResourceDelta#getResource()}
                 *
                 * @return the create project request
                 * @throws IllegalArgumentException if arguments is not a valid. Reasons include:
                 *     <ul>
                 *       <li>Invalid project name
                 *       <li>Invalid project type
                 *     </ul>
                 *
                 * @throws IllegalStateException if creation was failed. Reasons include:
                 *     <ul>
                 *       <li>Resource already exists
                 *     </ul>
                 */
                newProject(): ProjectRequest;
                /**
                 * Creates the new {@link Project} in current container with specified source storage (in other
                 * words, imports a remote project).
                 *
                 * <p>Fires following events: {@link ResourceChangedEvent} when project has successfully created.
                 *
                 * <p>Calling this method doesn't import a project immediately. To complete the request method
                 * {@link ProjectRequest#send()} should be called.
                 *
                 * <p>Calling {@link ProjectRequest#send()} produces new {@link Project} resource.
                 *
                 * <p>The supplied argument {@code name} should be a valid and pass validation within {@link
                 * NameUtils#checkProjectName(String)}.
                 *
                 * <p>
                 *
                 * <p>Example of usage for creating a new project:
                 *
                 * <pre>
                 *     ProjectConfig config = ... ;
                 *     Container workspace = ... ;
                 *
                 *     Promise<Project> newProjectPromise = workspace.importProject()
                 *                                                   .withBody(config)
                 *                                                   .send();
                 *
                 *     newProjectPromise.then(new Operation<Project>() {
                 *         public void apply(Project newProject) throws OperationException {
                 *              //do something with new project
                 *         }
                 *     });
                 * </pre>
                 *
                 * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
                 * {@link ResourceDelta#ADDED}. Created resource (instance of {@link Project}) provided by {@link
                 * ResourceDelta#getResource()}
                 *
                 * @return the create project request
                 * @throws IllegalArgumentException if arguments is not a valid. Reasons include:
                 *     <ul>
                 *       <li>Invalid project name
                 *     </ul>
                 *
                 * @throws IllegalStateException if creation was failed. Reasons include:
                 *     <ul>
                 *       <li>Resource already exists
                 *     </ul>
                 */
                importProject(): ProjectRequest;

                /**
                 * Creates the new {@link Folder} in current container.
                 *
                 * <p>Fires following events: {@link ResourceChangedEvent} when folder has successfully created.
                 *
                 * <p>Method produces new {@link Folder}.
                 *
                 * <p>The supplied argument {@code name} should be a valid and pass validation within {@link
                 * NameUtils#checkFolderName(String)}.
                 *
                 * <p>Note. That folders can not be created in workspace root (obtained by {@link
                 * AppContext#getWorkspaceRoot()}). Creating folder in this container will be failed.
                 *
                 * <p>Example of usage:
                 *
                 * <pre>
                 *     Container workspace = ... ;
                 *
                 *     workspace.newFolder("name").then(new Operation<Folder>() {
                 *         public void apply(Folder newFolder) throws OperationException {
                 *              //do something with new folder
                 *         }
                 *     });
                 * </pre>
                 *
                 * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
                 * {@link ResourceDelta#ADDED}. Created resource (instance of {@link Folder}) provided by {@link
                 * ResourceDelta#getResource()}
                 *
                 * @param name the name of the folder
                 * @return the {@link Promise} with created {@link Folder}
                 * @throws IllegalArgumentException if arguments is not a valid. Reasons include:
                 *     <ul>
                 *       <li>Invalid folder name
                 *       <li>Failed to create folder in workspace root
                 *     </ul>
                 *
                 * @throws IllegalStateException if creation was failed. Reasons include:
                 *     <ul>
                 *       <li>Resource already exists
                 *     </ul>
                 */
                newFolder(name: string): Promise<Folder>;

                /**
                 * Creates the new {@link File} in current container.
                 *
                 * <p>Fires following events: {@link ResourceChangedEvent} when file has successfully created.
                 *
                 * <p>Method produces new {@link File}.
                 *
                 * <p>The supplied argument {@code name} should be a valid and pass validation within {@link
                 * NameUtils#checkFileName(String)} (String)}.
                 *
                 * <p>Note. That files can not be created in workspace root (obtained by {@link
                 * AppContext#getWorkspaceRoot()}). Creating folder in this container will be failed.
                 *
                 * <p>The file content may be a {@code null} or empty.
                 *
                 * <p>Example of usage:
                 *
                 * <pre>
                 *     Container workspace = ... ;
                 *
                 *     workspace.newFile("name", "content").then(new Operation<File>() {
                 *         public void apply(File newFile) throws OperationException {
                 *              //do something with new file
                 *         }
                 *     });
                 * </pre>
                 *
                 * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
                 * {@link ResourceDelta#ADDED}. Created resource (instance of {@link File}) provided by {@link
                 * ResourceDelta#getResource()}
                 *
                 * @param name the name of the file
                 * @param content the file content
                 * @return the {@link Promise} with created {@link File}
                 * @throws IllegalArgumentException if arguments is not a valid. Reasons include:
                 *     <ul>
                 *       <li>Invalid file name
                 *       <li>Failed to create file in workspace root
                 *     </ul>
                 *
                 * @throws IllegalStateException if creation was failed. Reasons include:
                 *     <ul>
                 *       <li>Resource already exists
                 *     </ul>
                 */
                newFile(name: string, content: string): Promise<File>;

                /**
                 * Synchronizes the cached container and its children with the local file system.
                 *
                 * <p>For refreshing entire workspace root this method should be called on the container, which
                 * obtained from {@link AppContext#getWorkspaceRoot()}.
                 *
                 * <p>Fires following events: {@link ResourceChangedEvent} when the synchronized resource has
                 * changed.
                 *
                 * <p>Method doesn't guarantees the sorted order of the returned resources.
                 *
                 * @return the array of resource which where affected by synchronize operation
                 */
                synchronize(): Promise<Resource[]>;
                /**
                 * Synchronizes the given {@code deltas} with already cached resources. Method is useful for
                 * third-party components which performs changes with resources outside of client side resource
                 * management.
                 *
                 * <p>Method should be called on the workspace root {@link AppContext#getWorkspaceRoot()}.
                 *
                 * @param deltas the deltas which should be resolved
                 * @return the {@link Promise} with resolved deltas
                 * @throws IllegalStateException in case if method has been called outside of workspace root.
                 *     Reasons include:
                 *     <ul>
                 *       <li>External deltas should be applied on the workspace root
                 *     </ul>
                 */
                synchronizeDeltas(...deltas: ResourceDelta[]): Promise<ResourceDelta[]>;

                /**
                 * Searches the all possible files which matches given file or content mask.
                 *
                 * <p>Supplied file mask may supports wildcard:
                 *
                 * <ul>
                 *   <li>{@code *} - which matches any character sequence (including the empty one)
                 *   <li>{@code ?} - which matches any single character
                 * </ul>
                 *
                 * <p>Method doesn't guarantees the sorted order of the returned resources.
                 */
                search(fileMask: string, contentMask: string): Promise<SearchResult>;

                /**
                 * Searches the all possible files which configured into {@link QueryExpression}.
                 *
                 * <p>Method doesn't guarantees the sorted order of the returned resources.
                 *
                 * @param queryExpression the search query expression includes search parameters
                 * @return the {@link Promise} with array of found results
                 */
                searchByQuery(queryExpression: QueryExpression): Promise<SearchResult>;

                /**
                 * Creates the search expression which matches given file or content mask.
                 *
                 * <p>Supplied file mask may supports wildcard:
                 *
                 * <ul>
                 *   <li>{@code *} - which matches any character sequence (including the empty one)
                 *   <li>{@code ?} - which matches any single character
                 * </ul>
                 *
                 * @param fileMask the file name mask
                 * @param query the content entity mask
                 * @return the instance of {@link QueryExpression}
                 */
                createSearchQueryExpression(fileMask: string, query: string): QueryExpression;

                /**
                 * Returns the plain list of file tree with given {@code depth}.
                 *
                 * <p>Input {@code depth} should be within the range from -1 to {@link Integer#MAX_VALUE}.
                 *
                 * <p>In case if {@code depth} equals to 0, then empty resource is returned. In case if {@code
                 * depth} equals to -1, then whole file tree is loaded and returned.
                 *
                 * <p>Method doesn't guarantee that resources will be sorted by their {@link #getLocation()} in
                 * any order.
                 *
                 * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
                 * {@link ResourceDelta#ADDED}. Cached and loaded resource provided by {@link
                 * ResourceDelta#getResource()}.
                 *
                 * <p>Or
                 *
                 * <p>Delta kind: {@link ResourceDelta#UPDATED}. When resource was cached previously. Updated
                 * resource provided by {@link ResourceDelta#getResource()}.
                 *
                 * @param depth the depth
                 * @return plain array of loaded resources
                 * @throws IllegalArgumentException in case if invalid depth passed as argument. i.e. depth equals
                 *     -2, -3 and so on. Reasons include:
                 *     <ul>
                 *       <li>Invalid depth
                 *     </ul>
                 */
                getTree(depth: number): Promise<Resource[]>;
            }

            /**
             * Folders may be leaf or non-leaf resources and may contain files and/or other folders. A folder
             * resource is stored as a directory in the local file system.
             *
             * <p>Folder instance can be obtained by calling {@link Container#getContainer(Path)} or by {@link
             * Container#getChildren(boolean)}.
             *
             * <p>Note. This interface is not intended to be implemented by clients.
             */
            class Folder extends Container {
                /**
                 * Transforms current folder into {@link Project}.
                 *
                 * <p>Calling current method doesn't create configuration immediately. To complete configuration
                 * creating method {@link ProjectRequest#send()} should be called. This is immutable operation
                 * which produce new {@link Project}.
                 *
                 * <p>Example of usage:
                 *
                 * <pre>
                 *     Folder folder = ... ;
                 *     ProjectConfig configuration = ... ;
                 *
                 *     Promise<Project> projectPromise = folder.toProject().withBody(configuration).send();
                 *
                 *     projectPromise.then(new Operation<Project>() {
                 *         public void apply(Project newProject) throws OperationException {
                 *              //do something with new project
                 *         }
                 *     });
                 * </pre>
                 *
                 * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
                 * {@link ResourceDelta#UPDATED}. Updated resource (instance of {@link Project}) provided by
                 * {@link ResourceDelta#getResource()}
                 *
                 * @return the create project request
                 * @throws IllegalArgumentException if arguments is not a valid. Reasons include:
                 *     <ul>
                 *       <li>Invalid project name
                 *       <li>Invalid project type
                 *     </ul>
                 *
                 * @throws IllegalStateException if creation was failed. Reasons include:
                 *     <ul>
                 *       <li>Resource already exists
                 *     </ul>
                 *
                 * @see NameUtils#checkProjectName(String)
                 * @see ProjectRequest
                 * @see ProjectRequest#send()
                 * @since @since 4.4.0
                 */
                toProject(): ProjectRequest;
            }

            /**
             * An object that represents client side project.
             *
             * <p>Features of projects include:
             *
             * <ul>
             *   <li>A project collects together a set of files and folders.
             *   <li>A project's location controls where the project's resources are stored in the local file
             *       system.
             * </ul>
             *
             * Project also extends {@link ProjectConfig} which contains the meta-data required to define a
             * project.
             *
             * <p>To get list of currently of all loaded projects in the IDE, use {@link
             * AppContext#getProjects()}
             *
             * <p>Note. This interface is not intended to be implemented by clients.
             *
             * @see AppContext#getProjects()
             */
            interface Project extends Container, ProjectConfig {
                /**
                 * Check whether current project has problems. Problem project calculates in a runtime, so it is
                 * not affects stored configuration on the server. To find out the reasons why project has
                 * problems, following code snippet may be helpful:
                 *
                 * <p>Example of usage:
                 *
                 * <pre>
                 *     Project project = ... ;
                 *     if (project.isProblem()) {
                 *         Marker problemMarker = getMarker(ProblemProjectMarker.PROBLEM_PROJECT).get();
                 *
                 *         string message = string.valueOf(problemMarker.getAttribute(Marker.MESSAGE));
                 *     }
                 * </pre>
                 *
                 * @return {@code true} if current project has problems, otherwise {@code false}
                 */
                isProblem(): boolean;
                /**
                 * Returns the {@code true} if project physically exists on the file system.
                 *
                 * <p>Project may not be exists on file system, but workspace may has configured in the current
                 * workspace.
                 *
                 * @return {@code true} if project physically exists on the file system, otherwise {@code false}
                 * @since 4.4.0
                 */
                exists(): boolean;

                /**
                 * Checks whether given project {@code type} is applicable to current project.
                 *
                 * @param type the project type to check
                 * @return true if given project type is applicable to current project
                 */
                isTypeOf(type: string): boolean;

                /**
                 * Returns the attribute value for given {@code key}. If such attribute doesn't exist, {@code
                 * null} is returned. If there is more than one value exists for given {@code key}, than first
                 * value is returned.
                 *
                 * @param key the attribute name
                 * @return first value for the given {@code key} or null if such attribute doesn't exist
                 */
                getAttribute(key: string): string;

                /**
                 * Returns the list of attributes for given {@code key}. If such attribute doesn't exist, {@code
                 * null} is returned.
                 *
                 * @param key the attribute name
                 * @return the list with values for the given {@code key} or null if such attribute doesn't exist
                 */
                getAttributes(key: string): string[];

                getAttributes(): che.util.Map<string, string[]>;
            }

            interface ProjectProblem {

                getCode(): number;

                getMessage(): string;
            }

            interface VirtualFile {
                getLocation(): Path;
                getName(): string;
                getDisplayName(): string;
                isReadOnly(): boolean;
                getContentUrl(): string;
                getContent(): Promise<string>;
                updateContent(content: string): Promise<void>;
            }
        }
        namespace editor {
            enum FileOperation {
                OPEN,
                SAVE,
                CLOSE
            }
            class FileOperationEvent {
                static TYPE: EventType<FileOperationEvent>;

                getFile(): che.ide.resource.VirtualFile;
                getOperationType(): che.ide.editor.FileOperation
            }
            class EditorOpenedEvent {
                static TYPE: EventType<EditorOpenedEvent>;

                getFile(): che.ide.resource.VirtualFile;
                getEditor(): EditorPartPresenter;
            }
        }
        namespace parts {
            enum PartStackType {
                /**
             * Contains navigation parts. Designed to navigate by project, types, classes and any other
             * entities. Usually placed on the LEFT side of the IDE.
             */
                NAVIGATION,
                /**
                 * Contains informative parts. Designed to display the state of the application, project or
                 * processes. Usually placed on the BOTTOM side of the IDE.
                 */
                INFORMATION,
                /**
                 * Contains editing parts. Designed to provide an ability to edit any resources or settings.
                 * Usually placed in the CENTRAL part of the IDE.
                 */
                EDITING,
                /**
                 * Contains tooling parts. Designed to provide handy features and utilities, access to other
                 * services or any other features that are out of other PartType scopes. Usually placed on the
                 * RIGHT side of the IDE.
                 */
                TOOLING
            }
        }
        namespace workspace {

            interface ProjectConfig {
                getName(): string;

                getPath(): string;

                getDescription(): string;

                getType(): string;

                getMixins(): string[];

                getAttributes(): che.util.Map<string, string[]>;

                getSource(): SourceStorage;

                getProblems(): che.ide.resource.ProjectProblem[];
            }

            interface SourceStorage {
                getType(): string;

                getLocation(): string;

                getParameters(): che.util.Map<string, string>;
            }

            namespace event {
                class ServerRunningEvent {
                    static TYPE: EventType<ServerRunningEvent>;

                    getServerName(): string;

                    getMachineName(): string;
                }

                class WsAgentServerRunningEvent {
                    static TYPE: EventType<WsAgentServerRunningEvent>;
                    getMachineName(): string;
                }

                class TerminalAgentServerRunningEvent {
                    static TYPE: EventType<TerminalAgentServerRunningEvent>;
                    getMachineName(): string;
                }

                class ExecAgentServerRunningEvent {
                    static TYPE: EventType<ExecAgentServerRunningEvent>;
                    getMachineName(): string;
                }

                class ServerStoppedEvent {
                    static TYPE: EventType<ServerStoppedEvent>;
                    getServerName(): string;

                    getMachineName(): string;
                }

                class WsAgentServerStoppedEvent {
                    static TYPE: EventType<WsAgentServerStoppedEvent>;
                    getMachineName(): string;
                }

                class TerminalAgentServerStoppedEvent {
                    static TYPE: EventType<TerminalAgentServerStoppedEvent>;
                    getMachineName(): string;
                }

                class ExecAgentServerStoppedEvent {
                    static TYPE: EventType<ExecAgentServerStoppedEvent>;
                    getMachineName(): string;
                }

            }
        }
        namespace dialogs {
            interface DialogManager {

                /**
                 * Display a Message dialog.
                 * A dialog consists of a title, main part with text as content and confirmation button.
                 * Confirmation button text is 'OK' by default, can be overridden.
                 *
                 * @param dialogData the information necessary to create a Message dialog window
                 * @param confirmButtonClickedHandler the handler is used when user click on confirmation button
                 */
                displayMessageDialog(dialogData: MessageDialogData, confirmButtonClickedHandler: ClickButtonHandler): void;

                /**
                 * Display a Confirmation dialog.
                 * A dialog consists of a title, main part with text as content, confirmation and cancel buttons.
                 * Confirmation button text is 'OK' by default.
                 * Cancel button text is 'Cancel' by default.
                 * Text for confirmation and cancel buttons can be overridden.
                 *
                 * @param dialogData the information necessary to create a Confirmation dialog window
                 * @param confirmButtonClickedHandler the handler is used when user click on confirmation button
                 * @param cancelButtonClickedHandler the handler is used when user click on cancel button
                 */
                displayConfirmDialog(dialogData: ConfirmDialogData, confirmButtonClickedHandler: ClickButtonHandler, cancelButtonClickedHandler: ClickButtonHandler): void;

                /**
                 * Display an input dialog.
                 * A dialog consists of a title, main part with input field and label for it, confirmation and cancel buttons.
                 * Input field can contains an initial text. The initial text may be pre-selected.
                 * Confirmation button text is 'OK' by default.
                 * Cancel button text is 'Cancel' by default.
                 * Text for confirmation and cancel buttons can be overridden.
                 *
                 * @param dialogData the information necessary to create an input dialog window
                 * @param inputAcceptedHandler the handler is used when user click on confirmation button
                 * @param cancelButtonClickedHandler the handler is used when user click on cancel button
                 */
                displayInputDialog(dialogData: InputDialogData, inputAcceptedHandler: { (value: string): void }, cancelButtonClickedHandler: ClickButtonHandler): void;

                /**
                 * Display a Choice dialog.
                 * A dialog consists of a title, main part with text as content and three buttons to confirm some choice.
                 *
                 * @param dialogData the information necessary to create a Choice dialog window
                 * @param firstButtonClickedHandler the handler is used when user click on first button on the right
                 * @param secondButtonClickedHandler the handler is used when user click on second button on the right
                 * @param thirdButtonClickedHandler the handler is used when user click on third button on the right
                 */
                displayChoiceDialog(dialogData: ChoiceDialogData, firstButtonClickedHandler: ClickButtonHandler, secondButtonClickedHandler: ClickButtonHandler, thirdButtonClickedHandler: ClickButtonHandler): void;
            }

            /** Container for the information necessary to create a dialog window */
            interface DialogData {
                /** Dialog title. */
                title: string;

                /** Content for displaying. */
                content: string;
            }

            /** Container for the information necessary to create a message dialog window */
            interface MessageDialogData extends DialogData {
                /** Confirm button text. Confirmation button named 'OK' by default */
                confirmButtonText: string;
            }

            /** Container for the information necessary to create a confirmation dialog window */
            interface ConfirmDialogData extends MessageDialogData {
                /** Cancel button text. Cancel button named 'Cancel' by default */
                cancelButtonText: string;
            }

            /** Container for the information necessary to create an input dialog window with the specified initial text. */
            interface InputDialogData extends ConfirmDialogData {
                /** Text used to initialize the input. The {@code initialText} may be pre-selected.
                 * Selection begins at the specified {@code selectionStartIndex} and extends to the character at index {@code selectionLength}.
                 */
                initialText: string;

                /** Beginning index of the initial text to select, inclusive. */
                selectionStartIndex: number;

                /** Number of characters to be selected in the input. */
                selectionLength: number;
            }

            /** Container for the information necessary to create a choice dialog window */
            interface ChoiceDialogData extends DialogData {
                /** Text for displaying by first choice button. */
                firstChoiceButtonText: string;

                /** Text for displaying by second choice button */
                secondChoiceButtonText: string;

                /** Text for displaying by third choice button. */
                thirdChoiceButtonText: string;
            }

            /** Used when the user clicks on some button. */
            interface ClickButtonHandler {
                (): void;
            }
        }
    }
    namespace util {
        /**
         * Java like Map interface
         */
        interface Map<K, V> {
            size(): number;
            isEmpty(): boolean;
            containsKey(key: object): boolean;
            containsValue(value: object): boolean;
            get(key: object): V;
            put(key: K, value: V): V;
            remove(key: Object): V;
            putAll(m: Map<K, V>): void;
            clear(): void;
        }
    }
}


/**
 * Holds and manages all IDE icon resources, each resource mapped to their id. We support 3 way to
 * provide image: URL, HTML, image element factory
 */
declare interface ImageRegistry {
    /**
     * Register image url. 
     *
     * @param id the image id
     * @param url the image url
     */
    registerUrl(id: string, url: string): Disposable;

    /**
     * Register image html. For example html may be some FontAwesome icon
     *
     * @param id the image id
     * @param html the image html
     */
    registerHtml(id: string, html: string): Disposable;

    /**
     * Register image factory.Register image factory.
     *  For example : factory may provided by GWT plugin which use ClientBundle for images or
     *  plugin may construct image element manually.
     * 
     * @param id the image id
     * @param factory the image factory
     */
    registerFactory(id: string, factory: ImageFactory): Disposable;

    /**
     * Returns new image element each time
     *
     * @param id the image id
     * @return the image element or null if no image provided
     */
    getImage(id: string): Element;

}
/**
 * Factory to create some image Element, for example it's may be from GWT Image/SVG resource.
 * Should return new element each time called.
 */
declare interface ImageFactory {
    (): Element;
}

/**
 * A manager for actions. Used to register action handlers.
 */
declare interface ActionManager {
    /**
       * Register action handlers.
       * @param actionId the action id
       * @param updateAction the update handler
       * @param performAction the perform handler
       */
    registerAction(actionId: string, updateAction: UpdateAction, performAction: PerformAction): Disposable;
}

declare interface UpdateAction {
    /**
     * Updates the state of the action.
     * This method can be called frequently, for
     * instance, if an action is added to a toolbar, it will be updated twice a second. This means
     * that this method is supposed to work really fast, no real work should be done at this phase.
     *
     * @param actionData the action state data
     */
    (d: ActionData): void;
}

declare interface PerformAction {
    /**
     * Called when action performed
     * @param actionData the action state data
     */
    (d: ActionData): void;
}

/**
 * Container for the information necessary to execute or update an action
 */
declare interface ActionData {
    getText(): string;
    setText(text: string): void;
    getDescription(): string;
    setDescription(description: string): void;
    getImageElement(): Element;
    setImageElement(imageElement: Element): void;
    isVisible(): boolean;
    setVisible(visible: boolean): void;
    isEnabled(): boolean;
    setEnabled(enabled: boolean): void;
    setEnabledAndVisible(enabled: boolean): void;
}
