package org.eclipse.core.filesystem;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/** @author Evgen Vidolob */
public class URIUtil {
  /**
   * Returns an {@link IPath} representing this {@link URI} in the local file system, or <code>null
   * </code> if this URI does not represent a file in the local file system.
   *
   * @param uri The URI to convert
   * @return The path representing the provided URI, <code>null</code>
   */
  public static IPath toPath(URI uri) {
    Assert.isNotNull(uri);
    // Special treatment for LocalFileSystem. For performance only.
    if (EFS.SCHEME_FILE.equals(uri.getScheme())) return new Path(uri.getSchemeSpecificPart());
    // Relative path
    if (uri.getScheme() == null) return new Path(uri.getPath());
    // General case
    try {
      IFileStore store = EFS.getStore(uri);
      if (store == null) return null;
      File file = store.toLocalFile(EFS.NONE, null);
      if (file == null) return null;
      return new Path(file.getAbsolutePath());
    } catch (CoreException e) {
      // Fall through to return null.
    }
    return null;
  }

  /**
   * Converts an {@link IPath} representing a local file system path to a {@link URI}.
   *
   * @param path The path to convert
   * @return The URI representing the provided path
   */
  public static URI toURI(IPath path) {
    if (path == null) return null;
    if (path.isAbsolute()) return toURI(path.toFile().getAbsolutePath(), true);
    // Must use the relativize method to properly construct a relative URI
    URI base = toURI(Path.ROOT.setDevice(path.getDevice()));
    return base.relativize(toURI(path.makeAbsolute()));
  }

  /**
   * Converts a String representing a local file system path to a {@link URI}. For example, this
   * method can be used to create a URI from the output of {@link File#getAbsolutePath()}.
   *
   * <p>The <code>forceAbsolute</code> flag controls how this method handles relative paths. If the
   * value is <code>true</code>, then the input path is always treated as an absolute path, and the
   * returned URI will be an absolute URI. If the value is <code>false</code>, then a relative path
   * provided as input will result in a relative URI being returned.
   *
   * @param pathString The path string to convert
   * @param forceAbsolute if <code>true</code> the path is treated as an absolute path
   * @return The URI representing the provided path string
   * @since org.eclipse.core.filesystem 1.2
   */
  public static URI toURI(String pathString, boolean forceAbsolute) {
    if (File.separatorChar != '/') pathString = pathString.replace(File.separatorChar, '/');
    final int length = pathString.length();
    StringBuffer pathBuf = new StringBuffer(length + 1);
    // mark if path is relative
    if (length > 0 && (pathString.charAt(0) != '/') && forceAbsolute) {
      pathBuf.append('/');
    }
    // additional double-slash for UNC paths to distinguish from host separator
    if (pathString.startsWith("//")) // $NON-NLS-1$
    pathBuf.append('/').append('/');
    pathBuf.append(pathString);
    try {
      String scheme = null;
      if (length > 0 && (pathBuf.charAt(0) == '/')) {
        scheme = EFS.SCHEME_FILE;
      }
      return new URI(scheme, null, pathBuf.toString(), null);
    } catch (URISyntaxException e) {
      // try java.io implementation
      return new File(pathString).toURI();
    }
  }

  /**
   * Converts a String representing a local file system path to a {@link URI}. For example, this
   * method can be used to create a URI from the output of {@link File#getAbsolutePath()}. The
   * provided path string is always treated as an absolute path.
   *
   * @param pathString The absolute path string to convert
   * @return The URI representing the provided path string
   */
  public static URI toURI(String pathString) {
    IPath path = new Path(pathString);
    return toURI(path);
  }
}
