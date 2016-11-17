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
package org.eclipse.che.ide.resource;

import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * Client side implementation for the resource path.
 * <p/>
 * A path is an ordered collection of string segments, separated by a
 * standard separator character, "/". A path may also have a leading
 * and/or a trailing separator.
 * <p/>
 * Note that paths are value objects; all operations on paths return
 * a new path; the path that is operated on is unscathed.
 * <p/>
 * This class is not intended to be extended by clients.
 *
 * @author Vlad Zhukovskyi
 * @since 4.0.0-RC7
 */
public final class Path {

    /**
     * Path separator character constant "/" used in paths.
     */
    public static char SEPARATOR = '/';

    /**
     * Device separator character constant ":" used in paths.
     */
    public static char DEVICE_SEPARATOR = ':';

    /** masks for separator values */
    private static final int HAS_LEADING  = 1;
    private static final int IS_UNC       = 2;
    private static final int HAS_TRAILING = 4;

    private static final int ALL_SEPARATORS = HAS_LEADING | IS_UNC | HAS_TRAILING;

    /** Constant empty string value. */
    private static final String EMPTY_STRING = "";

    /** Constant value indicating no segments */
    private static final String[] NO_SEGMENTS = new String[0];

    /** Constant value containing the empty path with no device. */
    public static final Path EMPTY = new Path(EMPTY_STRING);

    /** Mask for all bits that are involved in the hash code */
    private static final int HASH_MASK = ~HAS_TRAILING;

    /** Constant root path string (<code>"/"</code>). */
    private static final String ROOT_STRING = "/";

    /** Constant value containing the root path with no device. */
    public static final Path ROOT = new Path(ROOT_STRING);

    /** The device id string. May be null if there is no device. */
    private String device = null;

    /** The path segments */
    private String[] segments;

    /** flags indicating separators (has leading, is UNC, has trailing) */
    private int separators;

    /**
     * Constructs a new path from the given string path.
     * The string path must represent a valid file system path
     * on the local file system.
     * The path is canonicalized and double slashes are removed
     * except at the beginning. (to handle UNC paths). All forward
     * slashes ('/') are treated as segment delimiters, and any
     * segment and device delimiters for the local file system are
     * also respected.
     *
     * @param pathString
     *         the portable string path
     * @since 4.0.0-RC5
     */
    public static Path valueOf(String pathString) {
        return new Path(pathString);
    }

    /* (Intentionally not included in javadoc)
     * Private constructor.
     */
    private Path() {
        // not allowed
    }

    /**
     * Constructs a new path from the given string path.
     * The string path must represent a valid file system path
     * on the local file system.
     * The path is canonicalized and double slashes are removed
     * except at the beginning. (to handle UNC paths). All forward
     * slashes ('/') are treated as segment delimiters, and any
     * segment and device delimiters for the local file system are
     * also respected (such as colon (':') and backslash ('\') on some file systems).
     *
     * @param fullPath
     *         the string path
     * @see #isValidPath(String)
     * @since 4.0.0-RC5
     */
    public Path(String fullPath) {
        initialize(null, fullPath);
    }

    /**
     * Constructs a new path from the given device id and string path.
     * The given string path must be valid.
     * The path is canonicalized and double slashes are removed except
     * at the beginning (to handle UNC paths). All forward
     * slashes ('/') are treated as segment delimiters, and any
     * segment delimiters for the local file system are
     * also respected (such as backslash ('\') on some file systems).
     *
     * @param device
     *         the device id
     * @param path
     *         the string path
     * @see #isValidPath(String)
     * @see #setDevice(String)
     * @since 4.0.0-RC5
     */
    public Path(String device, String path) {
        initialize(device, path);
    }

    /* (Intentionally not included in javadoc)
     * Private constructor.
     */
    private Path(String device, String[] segments, int _separators) {
        // no segment validations are done for performance reasons
        this.segments = segments;
        this.device = device;
        //hash code is cached in all but the bottom three bits of the separators field
        this.separators = (computeHashCode() << 3) | (_separators & ALL_SEPARATORS);
    }

    /**
     * Returns a new path which is the same as this path but with
     * the given file extension added.  If this path is empty, root or has a
     * trailing separator, this path is returned.  If this path already
     * has an extension, the existing extension is left and the given
     * extension simply appended. Clients wishing to replace
     * the current extension should first remove the extension and
     * then add the desired one.
     * <p>
     * The file extension portion is defined as the string
     * following the last period (".") character in the last segment.
     * The given extension should not include a leading ".".
     * </p>
     *
     * @param extension
     *         the file extension to append
     * @return the new path
     * @see #getFileExtension()
     * @since 4.0.0-RC5
     */
    public Path addFileExtension(String extension) {
        if (isRoot() || isEmpty() || hasTrailingSeparator())
            return this;
        int len = segments.length;
        String[] newSegments = new String[len];
        System.arraycopy(segments, 0, newSegments, 0, len - 1);
        newSegments[len - 1] = segments[len - 1] + '.' + extension;
        return new Path(device, newSegments, separators);
    }

    /**
     * Returns a path with the same segments as this path
     * but with a trailing separator added.
     * This path must have at least one segment.
     * <p>
     * If this path already has a trailing separator,
     * this path is returned.
     * </p>
     *
     * @return the new path
     * @see #hasTrailingSeparator()
     * @see #removeTrailingSeparator()
     * @since 4.0.0-RC5
     */
    public Path addTrailingSeparator() {
        if (hasTrailingSeparator() || isRoot()) {
            return this;
        }
        if (isEmpty()) {
            return new Path(device, segments, HAS_LEADING);
        }
        return new Path(device, segments, separators | HAS_TRAILING);
    }

    /**
     * Returns the canonicalized path obtained from the
     * concatenation of the given path's segments to the
     * end of this path.  If the given path has a trailing
     * separator, the result will have a trailing separator.
     * The device id of this path is preserved (the one
     * of the given path is ignored). Duplicate slashes
     * are removed from the path except at the beginning
     * where the path is considered to be UNC.
     *
     * @param path
     *         the path to concatenate
     * @return the new path
     * @since 4.0.0-RC5
     */
    public Path append(Path path) {
        //optimize some easy cases
        if (path == null || path.segmentCount() == 0)
            return this;
        //these call chains look expensive, but in most cases they are no-ops
        if (this.isEmpty())
            return path.setDevice(device).makeRelative().makeUNC(isUNC());
        if (this.isRoot())
            return path.setDevice(device).makeAbsolute().makeUNC(isUNC());

        //concatenate the two segment arrays
        int myLen = segments.length;
        int tailLen = path.segmentCount();
        String[] newSegments = new String[myLen + tailLen];
        System.arraycopy(segments, 0, newSegments, 0, myLen);
        for (int i = 0; i < tailLen; i++) {
            newSegments[myLen + i] = path.segment(i);
        }
        //use my leading separators and the tail's trailing separator
        Path result = new Path(device, newSegments,
                               (separators & (HAS_LEADING | IS_UNC)) | (path.hasTrailingSeparator() ? HAS_TRAILING : 0));
        String tailFirstSegment = newSegments[myLen];
        if (tailFirstSegment.equals("..") || tailFirstSegment.equals(".")) {
            result.canonicalize();
        }
        return result;
    }

    /**
     * Returns the canonicalized path obtained from the
     * concatenation of the given string path to the
     * end of this path. The given string path must be a valid
     * path. If it has a trailing separator,
     * the result will have a trailing separator.
     * The device id of this path is preserved (the one
     * of the given string is ignored). Duplicate slashes
     * are removed from the path except at the beginning
     * where the path is considered to be UNC.
     *
     * @param path
     *         the string path to concatenate
     * @return the new path
     * @see #isValidPath(String)
     * @since 4.0.0-RC5
     */
    public Path append(String path) {
        //optimize addition of a single segment
        if (path.indexOf(SEPARATOR) == -1 && path.indexOf("\\") == -1 && path.indexOf(DEVICE_SEPARATOR) == -1) {
            int tailLength = path.length();
            if (tailLength < 3) {
                //some special cases
                if (tailLength == 0 || ".".equals(path)) {
                    return this;
                }
                if ("..".equals(path))
                    return removeLastSegments(1);
            }
            //just add the segment
            int myLen = segments.length;
            String[] newSegments = new String[myLen + 1];
            System.arraycopy(segments, 0, newSegments, 0, myLen);
            newSegments[myLen] = path;
            return new Path(device, newSegments, separators & ~HAS_TRAILING);
        }
        //go with easy implementation
        return append(new Path(path));
    }

    /**
     * Destructively converts this path to its canonical form.
     * <p>
     * In its canonical form, a path does not have any
     * "." segments, and parent references ("..") are collapsed
     * where possible.
     * </p>
     *
     * @return true if the path was modified, and false otherwise
     * @since 4.0.0-RC5
     */
    private boolean canonicalize() {
        //look for segments that need canonicalizing
        for (int i = 0, max = segments.length; i < max; i++) {
            String segment = segments[i];
            if (segment.charAt(0) == '.' && (segment.equals("..") || segment.equals("."))) {
                //path needs to be canonicalized
                collapseParentReferences();
                //paths of length 0 have no trailing separator
                if (segments.length == 0)
                    separators &= (HAS_LEADING | IS_UNC);
                //recompute hash because canonicalize affects hash
                separators = (separators & ALL_SEPARATORS) | (computeHashCode() << 3);
                return true;
            }
        }
        return false;
    }

    /**
     * Destructively removes all occurrences of ".." segments from this path.
     */
    private void collapseParentReferences() {
        int segmentCount = segments.length;
        String[] stack = new String[segmentCount];
        int stackPointer = 0;
        for (String segment : segments) {
            if (segment.equals("..")) {
                if (stackPointer == 0) {
                    // if the stack is empty we are going out of our scope
                    // so we need to accumulate segments.  But only if the original
                    // path is relative.  If it is absolute then we can't go any higher than
                    // root so simply toss the .. references.
                    if (!isAbsolute())
                        stack[stackPointer++] = segment; //stack push
                } else {
                    // if the top is '..' then we are accumulating segments so don't pop
                    if ("..".equals(stack[stackPointer - 1]))
                        stack[stackPointer++] = "..";
                    else
                        stackPointer--;
                    //stack pop
                }
                //collapse current references
            } else if (!segment.equals(".") || segmentCount == 1)
                stack[stackPointer++] = segment; //stack push
        }
        //if the number of segments hasn't changed, then no modification needed
        if (stackPointer == segmentCount)
            return;
        //build the new segment array backwards by popping the stack
        String[] newSegments = new String[stackPointer];
        System.arraycopy(stack, 0, newSegments, 0, stackPointer);
        this.segments = newSegments;
    }

    /**
     * Removes duplicate slashes from the given path, with the exception
     * of leading double slash which represents a UNC path.
     */
    private String collapseSlashes(String path) {
        int length = path.length();
        // if the path is only 0, 1 or 2 chars long then it could not possibly have illegal
        // duplicate slashes.
        if (length < 3)
            return path;
        // check for an occurrence of // in the path.  Start at index 1 to ensure we skip leading UNC //
        // If there are no // then there is nothing to collapse so just return.
        if (path.indexOf("//", 1) == -1)
            return path;
        // We found an occurrence of // in the path so do the slow collapse.
        char[] result = new char[path.length()];
        int count = 0;
        boolean hasPrevious = false;
        char[] characters = path.toCharArray();
        for (int index = 0; index < characters.length; index++) {
            char c = characters[index];
            if (c == SEPARATOR) {
                if (hasPrevious) {
                    // skip double slashes, except for beginning of UNC.
                    // note that a UNC path can't have a device.
                    if (device == null && index == 1) {
                        result[count] = c;
                        count++;
                    }
                } else {
                    hasPrevious = true;
                    result[count] = c;
                    count++;
                }
            } else {
                hasPrevious = false;
                result[count] = c;
                count++;
            }
        }
        return new String(result, 0, count);
    }

    /* (Intentionally not included in javadoc)
     * Computes the hash code for this object.
     */
    private int computeHashCode() {
        int hash = device == null ? 17 : device.hashCode();
        int segmentCount = segments.length;
        for (int i = 0; i < segmentCount; i++) {
            //this function tends to given a fairly even distribution
            hash = hash * 37 + segments[i].hashCode();
        }

        return hash;
    }

    /* (Intentionally not included in javadoc)
     * Returns the size of the string that will be created by toString or toOSString.
     */
    private int computeLength() {
        int length = 0;
        if (device != null)
            length += device.length();
        if ((separators & HAS_LEADING) != 0)
            length++;
        if ((separators & IS_UNC) != 0)
            length++;
        //add the segment lengths
        int max = segments.length;
        if (max > 0) {
            for (String segment : segments) {
                length += segment.length();
            }
            //add the separator lengths
            length += max - 1;
        }
        if ((separators & HAS_TRAILING) != 0)
            length++;
        return length;
    }

    /* (Intentionally not included in javadoc)
     * Returns the number of segments in the given path
     */
    private int computeSegmentCount(String path) {
        int len = path.length();
        if (len == 0 || (len == 1 && path.charAt(0) == SEPARATOR)) {
            return 0;
        }
        int count = 1;
        int prev = -1;
        int i;
        while ((i = path.indexOf(SEPARATOR, prev + 1)) != -1) {
            if (i != prev + 1 && i != len) {
                ++count;
            }
            prev = i;
        }
        if (path.charAt(len - 1) == SEPARATOR) {
            --count;
        }
        return count;
    }

    /**
     * Computes the segment array for the given canonicalized path.
     */
    private String[] computeSegments(String path) {
        // performance sensitive --- avoid creating garbage
        int segmentCount = computeSegmentCount(path);
        if (segmentCount == 0)
            return NO_SEGMENTS;
        String[] newSegments = new String[segmentCount];
        int len = path.length();
        // check for initial slash
        int firstPosition = (path.charAt(0) == SEPARATOR) ? 1 : 0;
        // check for UNC
        if (firstPosition == 1 && len > 1 && (path.charAt(1) == SEPARATOR))
            firstPosition = 2;
        int lastPosition = (path.charAt(len - 1) != SEPARATOR) ? len - 1 : len - 2;
        // for non-empty paths, the number of segments is
        // the number of slashes plus 1, ignoring any leading
        // and trailing slashes
        int next = firstPosition;
        for (int i = 0; i < segmentCount; i++) {
            int start = next;
            int end = path.indexOf(SEPARATOR, next);
            if (end == -1) {
                newSegments[i] = path.substring(start, lastPosition + 1);
            } else {
                newSegments[i] = path.substring(start, end);
            }
            next = end + 1;
        }
        return newSegments;
    }

    /**
     * Returns whether this path equals the given object.
     * <p>
     * Equality for paths is defined to be: same sequence of segments,
     * same absolute/relative status, and same device.
     * Trailing separators are disregarded.
     * Paths are not generally considered equal to objects other than paths.
     * </p>
     *
     * @param obj
     *         the other object
     * @return <code>true</code> if the paths are equivalent,
     * and <code>false</code> if they are not
     * @since 4.0.0-RC5
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Path))
            return false;
        Path target = (Path)obj;
        //check leading separators and hash code
        if ((separators & HASH_MASK) != (target.separators & HASH_MASK))
            return false;
        String[] targetSegments = target.segments;
        int i = segments.length;
        //check segment count
        if (i != targetSegments.length)
            return false;
        //check segments in reverse order - later segments more likely to differ
        while (--i >= 0)
            if (!segments[i].equals(targetSegments[i]))
                return false;
        //check device last (least likely to differ)
        return device == target.device || (device != null && device.equals(target.device));
    }

    /**
     * Returns the device id for this path, or <code>null</code> if this
     * path has no device id. Note that the result will end in ':'.
     *
     * @return the device id, or <code>null</code>
     * @see #setDevice(String)
     * @since 4.0.0-RC5
     */
    public String getDevice() {
        return device;
    }

    /**
     * Returns the file extension portion of this path,
     * or <code>null</code> if there is none.
     * <p>
     * The file extension portion is defined as the string
     * following the last period (".") character in the last segment.
     * If there is no period in the last segment, the path has no
     * file extension portion. If the last segment ends in a period,
     * the file extension portion is the empty string.
     * </p>
     *
     * @return the file extension or <code>null</code>
     * @see #addFileExtension(String)
     * @since 4.0.0-RC5
     */
    public String getFileExtension() {
        if (hasTrailingSeparator()) {
            return null;
        }
        String lastSegment = lastSegment();
        if (lastSegment == null) {
            return null;
        }
        int index = lastSegment.lastIndexOf('.');
        if (index == -1) {
            return null;
        }
        return lastSegment.substring(index + 1);
    }

    /* (Intentionally not included in javadoc)
     * Computes the hash code for this object.
     */
    public int hashCode() {
        return Objects.hashCode(segments);
    }

    /**
     * Returns whether this path has a trailing separator.
     * <p>
     * Note: In the root path ("/"), the separator is considered to
     * be leading rather than trailing.
     * </p>
     *
     * @return <code>true</code> if this path has a trailing
     * separator, and <code>false</code> otherwise
     * @see #addTrailingSeparator()
     * @see #removeTrailingSeparator()
     * @since 4.0.0-RC5
     */
    public boolean hasTrailingSeparator() {
        return (separators & HAS_TRAILING) != 0;
    }

    /*
     * Initialize the current path with the given string.
     */
    private Path initialize(String deviceString, String path) {
        checkNotNull(path);
        this.device = deviceString;

        path = collapseSlashes(path);
        int len = path.length();

        //compute the separators array
        if (len < 2) {
            if (len == 1 && path.charAt(0) == SEPARATOR) {
                this.separators = HAS_LEADING;
            } else {
                this.separators = 0;
            }
        } else {
            boolean hasLeading = path.charAt(0) == SEPARATOR;
            boolean isUNC = hasLeading && path.charAt(1) == SEPARATOR;
            //UNC path of length two has no trailing separator
            boolean hasTrailing = !(isUNC && len == 2) && path.charAt(len - 1) == SEPARATOR;
            separators = hasLeading ? HAS_LEADING : 0;
            if (isUNC)
                separators |= IS_UNC;
            if (hasTrailing)
                separators |= HAS_TRAILING;
        }
        //compute segments and ensure canonical form
        segments = computeSegments(path);
        if (!canonicalize()) {
            //compute hash now because canonicalize didn't need to do it
            separators = (separators & ALL_SEPARATORS) | (computeHashCode() << 3);
        }
        return this;
    }

    /**
     * Returns whether this path is an absolute path (ignoring
     * any device id).
     * <p>
     * Absolute paths start with a path separator.
     * A root path, like <code>/</code> or <code>C:/</code>,
     * is considered absolute.  UNC paths are always absolute.
     * </p>
     *
     * @return <code>true</code> if this path is an absolute path,
     * and <code>false</code> otherwise
     * @since 4.0.0-RC5
     */
    public boolean isAbsolute() {
        //it's absolute if it has a leading separator
        return (separators & HAS_LEADING) != 0;
    }

    /**
     * Returns whether this path has no segments and is not
     * a root path.
     *
     * @return <code>true</code> if this path is empty,
     * and <code>false</code> otherwise
     * @since 4.0.0-RC5
     */
    public boolean isEmpty() {
        //true if no segments and no leading prefix
        return segments.length == 0 && ((separators & ALL_SEPARATORS) != HAS_LEADING);

    }

    /**
     * Returns whether this path is a prefix of the given path.
     * To be a prefix, this path's segments must
     * appear in the argument path in the same order,
     * and their device ids must match.
     * <p>
     * An empty path is a prefix of all paths with the same device; a root path is a prefix of
     * all absolute paths with the same device.
     * </p>
     *
     * @param anotherPath
     *         the other path
     * @return <code>true</code> if this path is a prefix of the given path,
     * and <code>false</code> otherwise
     * @since 4.0.0-RC5
     */
    public boolean isPrefixOf(Path anotherPath) {
        if (device == null) {
            if (anotherPath.getDevice() != null) {
                return false;
            }
        } else {
            if (!device.equalsIgnoreCase(anotherPath.getDevice())) {
                return false;
            }
        }
        if (isEmpty() || (isRoot() && anotherPath.isAbsolute())) {
            return true;
        }
        int len = segments.length;
        if (len > anotherPath.segmentCount()) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (!segments[i].equals(anotherPath.segment(i)))
                return false;
        }
        return true;
    }

    /**
     * Returns whether this path is a root path.
     * <p>
     * The root path is the absolute non-UNC path with zero segments;
     * e.g., <code>/</code> or <code>C:/</code>.
     * The separator is considered a leading separator, not a trailing one.
     * </p>
     *
     * @return <code>true</code> if this path is a root path,
     * and <code>false</code> otherwise
     * @since 4.0.0-RC5
     */
    public boolean isRoot() {
        //must have no segments, a leading separator, and not be a UNC path.
        return this == ROOT || (segments.length == 0 && ((separators & ALL_SEPARATORS) == HAS_LEADING));
    }

    /**
     * Returns a boolean value indicating whether or not this path
     * is considered to be in UNC form. Return false if this path
     * has a device set or if the first 2 characters of the path string
     * are not <code>Path.SEPARATOR</code>.
     *
     * @return boolean indicating if this path is UNC
     * @since 4.0.0-RC5
     */
    public boolean isUNC() {
        return device == null && (separators & IS_UNC) != 0;
    }

    /**
     * Returns whether the given string is syntactically correct as
     * a path. The device id is the prefix up to and including the device
     * separator for the local file system; the path proper is everything to
     * the right of it, or the entire string if there is no device separator.
     * When the platform location is a file system with no meaningful device
     * separator, the entire string is treated as the path proper.
     * The device id is not checked for validity; the path proper is correct
     * if each of the segments in its canonicalized form is valid.
     *
     * @param path
     *         the path to check
     * @return <code>true</code> if the given string is a valid path,
     * and <code>false</code> otherwise
     * @see #isValidSegment(String)
     * @since 4.0.0-RC5
     */
    public static boolean isValidPath(String path) {
        Path test = new Path(path);
        for (int i = 0, max = test.segmentCount(); i < max; i++)
            if (!isValidSegment(test.segment(i)))
                return false;
        return true;
    }

    /**
     * Returns whether the given string is valid as a segment in
     * a path. The rules for valid segments are as follows:
     * <ul>
     * <li> the empty string is not valid
     * <li> any string containing the slash character ('/') is not valid
     * <li>any string containing segment or device separator characters
     * on the local file system, such as the backslash ('\') and colon (':')
     * on some file systems.
     * </ul>
     *
     * @param segment
     *         the path segment to check
     * @return <code>true</code> if the given path segment is valid,
     * and <code>false</code> otherwise
     * @since 4.0.0-RC5
     */
    protected static boolean isValidSegment(String segment) {
        int size = segment.length();
        if (size == 0)
            return false;
        for (int i = 0; i < size; i++) {
            char c = segment.charAt(i);
            if (c == '/')
                return false;
        }
        return true;
    }

    /**
     * Returns the last segment of this path, or
     * <code>null</code> if it does not have any segments.
     *
     * @return the last segment of this path, or <code>null</code>
     * @since 4.0.0-RC5
     */
    public String lastSegment() {
        int len = segments.length;
        return len == 0 ? null : segments[len - 1];
    }

    /**
     * Returns an absolute path with the segments and device id of this path.
     * Absolute paths start with a path separator. If this path is absolute,
     * it is simply returned.
     *
     * @return the new path
     * @since 4.0.0-RC5
     */
    public Path makeAbsolute() {
        if (isAbsolute()) {
            return this;
        }
        Path result = new Path(device, segments, separators | HAS_LEADING);
        //may need canonicalizing if it has leading ".." or "." segments
        if (result.segmentCount() > 0) {
            String first = result.segment(0);
            assert first != null;
            if (first.equals("..") || first.equals(".")) {
                result.canonicalize();
            }
        }
        return result;
    }

    /**
     * Returns a relative path with the segments and device id of this path.
     * Absolute paths start with a path separator and relative paths do not.
     * If this path is relative, it is simply returned.
     *
     * @return the new path
     * @since 4.0.0-RC5
     */
    public Path makeRelative() {
        if (!isAbsolute()) {
            return this;
        }
        return new Path(device, segments, separators & HAS_TRAILING);
    }

    /**
     * Returns a path equivalent to this path, but relative to the given base path if possible.
     * <p>
     * The path is only made relative if the base path if both paths have the same device
     * and have a non-zero length common prefix. If the paths have different devices,
     * or no common prefix, then this path is simply returned. If the path is successfully
     * made relative, then appending the returned path to the base will always produce
     * a path equal to this path.
     * </p>
     *
     * @param base
     *         The base path to make this path relative to
     * @return A path relative to the base path, or this path if it could
     * not be made relative to the given base
     * @since 4.0.0-RC5
     */
    public Path makeRelativeTo(Path base) {
        //can't make relative if devices are not equal
        if (device != base.getDevice() && (device == null || !device.equalsIgnoreCase(base.getDevice())))
            return this;
        int commonLength = matchingFirstSegments(base);
        final int differenceLength = base.segmentCount() - commonLength;
        final int newSegmentLength = differenceLength + segmentCount() - commonLength;
        if (newSegmentLength == 0)
            return Path.EMPTY;
        String[] newSegments = new String[newSegmentLength];
        //add parent references for each segment different from the base
        Arrays.fill(newSegments, 0, differenceLength, ".."); //$NON-NLS-1$
        //append the segments of this path not in common with the base
        System.arraycopy(segments, commonLength, newSegments, differenceLength, newSegmentLength - differenceLength);
        return new Path(null, newSegments, separators & HAS_TRAILING);
    }

    /**
     * Return a new path which is the equivalent of this path converted to UNC
     * form (if the given boolean is true) or this path not as a UNC path (if the given
     * boolean is false). If UNC, the returned path will not have a device and the
     * first 2 characters of the path string will be <code>Path.SEPARATOR</code>. If not UNC, the
     * first 2 characters of the returned path string will not be <code>Path.SEPARATOR</code>.
     *
     * @param toUNC
     *         true if converting to UNC, false otherwise
     * @return the new path, either in UNC form or not depending on the boolean parameter
     * @since 4.0.0-RC5
     */
    public Path makeUNC(boolean toUNC) {
        // if we are already in the right form then just return
        if (!(toUNC ^ isUNC()))
            return this;

        int newSeparators = this.separators;
        if (toUNC) {
            newSeparators |= HAS_LEADING | IS_UNC;
        } else {
            //mask out the UNC bit
            newSeparators &= HAS_LEADING | HAS_TRAILING;
        }
        return new Path(toUNC ? null : device, segments, newSeparators);
    }

    /**
     * Returns a count of the number of segments which match in
     * this path and the given path (device ids are ignored),
     * comparing in increasing segment number order.
     *
     * @param anotherPath
     *         the other path
     * @return the number of matching segments
     * @since 4.0.0-RC5
     */
    public int matchingFirstSegments(Path anotherPath) {
        checkNotNull(anotherPath);
        int anotherPathLen = anotherPath.segmentCount();
        int max = Math.min(segments.length, anotherPathLen);
        int count = 0;
        for (int i = 0; i < max; i++) {
            if (!segments[i].equals(anotherPath.segment(i))) {
                return count;
            }
            count++;
        }
        return count;
    }

    /**
     * Returns a new path which is the same as this path but with
     * the file extension removed.  If this path does not have an
     * extension, this path is returned.
     * <p>
     * The file extension portion is defined as the string
     * following the last period (".") character in the last segment.
     * If there is no period in the last segment, the path has no
     * file extension portion. If the last segment ends in a period,
     * the file extension portion is the empty string.
     * </p>
     *
     * @return the new path
     * @see #addFileExtension(String)
     * @since 4.0.0-RC5
     */
    public Path removeFileExtension() {
        String extension = getFileExtension();
        if (extension == null || extension.equals("")) {
            return this;
        }
        String lastSegment = lastSegment();
        int index = lastSegment.lastIndexOf(extension) - 1;
        return removeLastSegments(1).append(lastSegment.substring(0, index));
    }

    /**
     * Returns a copy of this path with the given number of segments
     * removed from the beginning. The device id is preserved.
     * The number must be greater or equal zero.
     * If the count is zero, this path is returned.
     * The resulting path will always be a relative path with respect
     * to this path.  If the number equals or exceeds the number
     * of segments in this path, an empty relative path is returned.
     *
     * @param count
     *         the number of segments to remove
     * @return the new path
     * @since 4.0.0-RC5
     */
    public Path removeFirstSegments(int count) {
        if (count == 0)
            return this;
        if (count >= segments.length) {
            return new Path(device, NO_SEGMENTS, 0);
        }
        checkArgument(count > 0);
        int newSize = segments.length - count;
        String[] newSegments = new String[newSize];
        System.arraycopy(this.segments, count, newSegments, 0, newSize);

        //result is always a relative path
        return new Path(device, newSegments, separators & HAS_TRAILING);
    }

    /**
     * Returns a copy of this path with the given number of segments
     * removed from the end. The device id is preserved.
     * The number must be greater or equal zero.
     * If the count is zero, this path is returned.
     * <p>
     * If this path has a trailing separator, it will still
     * have a trailing separator after the last segments are removed
     * (assuming there are some segments left).  If there is no
     * trailing separator, the result will not have a trailing
     * separator.
     * If the number equals or exceeds the number
     * of segments in this path, a path with no segments is returned.
     * </p>
     *
     * @param count
     *         the number of segments to remove
     * @return the new path
     * @since 4.0.0-RC5
     */
    public Path removeLastSegments(int count) {
        if (count == 0)
            return this;
        if (count >= segments.length) {
            //result will have no trailing separator
            return new Path(device, NO_SEGMENTS, separators & (HAS_LEADING | IS_UNC));
        }
        checkArgument(count > 0);
        int newSize = segments.length - count;
        String[] newSegments = new String[newSize];
        System.arraycopy(this.segments, 0, newSegments, 0, newSize);
        return new Path(device, newSegments, separators & (HAS_LEADING | IS_UNC));
    }

    /**
     * Returns a path with the same segments as this path
     * but with a trailing separator removed.
     * Does nothing if this path does not have at least one segment.
     * The device id is preserved.
     * <p>
     * If this path does not have a trailing separator,
     * this path is returned.
     * </p>
     *
     * @return the new path
     * @see #addTrailingSeparator()
     * @see #hasTrailingSeparator()
     * @since 4.0.0-RC5
     */
    public Path removeTrailingSeparator() {
        if (!hasTrailingSeparator()) {
            return this;
        }
        return new Path(device, segments, separators & (HAS_LEADING | IS_UNC));
    }

    /**
     * Returns the specified segment of this path, or
     * <code>null</code> if the path does not have such a segment.
     *
     * @param index
     *         the 0-based segment index
     * @return the specified segment, or <code>null</code>
     * @since 4.0.0-RC5
     */
    public String segment(int index) {
        if (index >= segments.length)
            return null;
        return segments[index];
    }

    /**
     * Returns the number of segments in this path.
     * <p>
     * Note that both root and empty paths have 0 segments.
     * </p>
     *
     * @return the number of segments
     * @since 4.0.0-RC5
     */
    public int segmentCount() {
        return segments.length;
    }

    /**
     * Returns the segments in this path in order.
     *
     * @return an array of string segments
     * @since 4.0.0-RC5
     */
    public String[] segments() {
        String[] segmentCopy = new String[segments.length];
        System.arraycopy(segments, 0, segmentCopy, 0, segments.length);
        return segmentCopy;
    }

    /**
     * Returns a new path which is the same as this path but with
     * the given device id.  The device id must end with a ":".
     * A device independent path is obtained by passing <code>null</code>.
     * <p>
     * For example, "C:" and "Server/Volume:" are typical device ids.
     * </p>
     *
     * @param device
     *         the device id or <code>null</code>
     * @return a new path
     * @see #getDevice()
     * @since 4.0.0-RC5
     */
    public Path setDevice(String device) {
        if (device != null) {
            checkArgument(device.indexOf(Path.DEVICE_SEPARATOR) == (device.length() - 1), "Last character should be the device separator");
        }
        //return the receiver if the device is the same
        if (device == this.device || (device != null && device.equals(this.device)))
            return this;

        return new Path(device, segments, separators);
    }

    /**
     * Returns a string representation of this path, including its
     * device id.  The same separator, "/", is used on all platforms.
     * <p>
     * Example result strings (without and with device id):
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
     * This string is suitable for passing to <code>Path(String)</code>.
     * </p>
     *
     * @return a string representation of this path
     * @since 4.0.0-RC5
     */
    public String toString() {
        int resultSize = computeLength();
        if (resultSize <= 0)
            return EMPTY_STRING;
        char[] result = new char[resultSize];
        int offset = 0;
        if (device != null) {
            int size = device.length();
            device.getChars(0, size, result, offset);
            offset += size;
        }
        if ((separators & HAS_LEADING) != 0)
            result[offset++] = SEPARATOR;
        if ((separators & IS_UNC) != 0)
            result[offset++] = SEPARATOR;
        int len = segments.length - 1;
        if (len >= 0) {
            //append all but the last segment, with separators
            for (int i = 0; i < len; i++) {
                int size = segments[i].length();
                segments[i].getChars(0, size, result, offset);
                offset += size;
                result[offset++] = SEPARATOR;
            }
            //append the last segment
            int size = segments[len].length();
            segments[len].getChars(0, size, result, offset);
            offset += size;
        }
        if ((separators & HAS_TRAILING) != 0)
            result[offset++] = SEPARATOR;
        return new String(result);
    }

    /**
     * Returns a copy of this path truncated after the
     * given number of segments. The number must not be negative.
     * The device id is preserved.
     * <p>
     * If this path has a trailing separator, the result will too
     * (assuming there are some segments left). If there is no
     * trailing separator, the result will not have a trailing
     * separator.
     * Copying up to segment zero simply means making an copy with
     * no path segments.
     * </p>
     *
     * @param count
     *         the segment number at which to truncate the path
     * @return the new path
     * @since 4.0.0-RC5
     */
    public Path uptoSegment(int count) {
        if (count == 0)
            return new Path(device, NO_SEGMENTS, separators & (HAS_LEADING | IS_UNC));
        if (count >= segments.length)
            return this;
        checkArgument(count > 0, "Invalid parameter to Path.uptoSegment");
        String[] newSegments = new String[count];
        System.arraycopy(segments, 0, newSegments, 0, count);
        return new Path(device, newSegments, separators);
    }

    /**
     * Returns a copy of this path with removed last segment.
     *
     * @return the new path
     * @since 4.4.0
     */
    public Path parent() {
        return segmentCount() == 1 ? Path.ROOT : this.removeLastSegments(1);
    }

    /**
     * Converts given input array of paths into the list.
     *
     * @param paths
     *         the input array of paths
     * @return the converted list
     * @since 4.4.0
     */
    public static List<String> toList(Path[] paths) {
        if (paths == null || paths.length == 0) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<>(paths.length);
        for (Path path : paths) {
            list.add(path.toString());
        }

        return list;
    }

    /**
     * Calculated common path from the several paths given as array.
     * <p>
     * For example we have three paths:
     * <ul>
     * <li>{@code /a/b/c}</li>
     * <li>{@code /a/b/d}</li>
     * <li>{@code /a/b/d/e}</li>
     * </ul>
     * Common path will be {@code /a/b}
     *
     * @param paths
     *         paths array
     * @return common path of empty string if given array is empty
     * @throws NullPointerException
     *         in case if given {@code paths} array is null
     * @since 5.0.0
     */
    public static Path commonPath(Path... paths) {
        checkNotNull(paths);

        Path commonPath = Path.ROOT;

        if (paths.length == 0) {
            return EMPTY;
        }

        if (paths.length == 1) {
            return paths[0];
        }

        for (int i = 0; i < paths[0].segmentCount(); i++) {
            final String currentSegment = paths[0].segment(i);

            boolean segmentsMatched = true;

            for (int j = 1; j < paths.length && segmentsMatched; j++) {
                final Path comparedPath = paths[j];

                if (comparedPath.segmentCount() < i) {
                    segmentsMatched = false;
                    break;
                } else {
                    segmentsMatched = nullToEmpty(comparedPath.segment(i)).equals(currentSegment);
                }
            }

            if (segmentsMatched) {
                commonPath = commonPath.append(currentSegment);
            } else {
                break;
            }
        }

        return commonPath;
    }
}
