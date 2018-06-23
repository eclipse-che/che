package org.eclipse.che.plugin.java.server.rest.recommend.similarity;

/**
 * Represents a difference, as used in <code>Diff</code>. A difference consists of two pairs of
 * starting and ending points, each pair representing either the "from" or the "to" collection
 * passed to <code>Diff</code>. If an ending point is -1, then the difference was either a deletion
 * or an addition. For example, if <code>getDeletedEnd()</code> returns -1, then the difference
 * represents an addition.
 */
public class Difference {
  public static final int NONE = -1;

  /** The point at which the deletion starts. */
  private int delStart = NONE;

  /** The point at which the deletion ends. */
  private int delEnd = NONE;

  /** The point at which the addition starts. */
  private int addStart = NONE;

  /** The point at which the addition ends. */
  private int addEnd = NONE;

  /** Creates the difference for the given start and end points for the deletion and addition. */
  public Difference(int delStart, int delEnd, int addStart, int addEnd) {
    this.delStart = delStart;
    this.delEnd = delEnd;
    this.addStart = addStart;
    this.addEnd = addEnd;
  }

  /**
   * The point at which the deletion starts, if any. A value equal to <code>NONE</code> means this
   * is an addition.
   */
  public int getDeletedStart() {
    return delStart;
  }

  /**
   * The point at which the deletion ends, if any. A value equal to <code>NONE</code> means this is
   * an addition.
   */
  public int getDeletedEnd() {
    return delEnd;
  }

  /**
   * The point at which the addition starts, if any. A value equal to <code>NONE</code> means this
   * must be an addition.
   */
  public int getAddedStart() {
    return addStart;
  }

  /**
   * The point at which the addition ends, if any. A value equal to <code>NONE</code> means this
   * must be an addition.
   */
  public int getAddedEnd() {
    return addEnd;
  }

  /**
   * Sets the point as deleted. The start and end points will be modified to include the given line.
   */
  public void setDeleted(int line) {
    delStart = Math.min(line, delStart);
    delEnd = Math.max(line, delEnd);
  }

  /**
   * Sets the point as added. The start and end points will be modified to include the given line.
   */
  public void setAdded(int line) {
    addStart = Math.min(line, addStart);
    addEnd = Math.max(line, addEnd);
  }

  /**
   * Compares this object to the other for equality. Both objects must be of type Difference, with
   * the same starting and ending points.
   */
  public boolean equals(Object obj) {
    if (obj instanceof Difference) {
      Difference other = (Difference) obj;

      return (delStart == other.delStart
          && delEnd == other.delEnd
          && addStart == other.addStart
          && addEnd == other.addEnd);
    } else {
      return false;
    }
  }

  /** Returns a string representation of this difference. */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("del: [" + delStart + ", " + delEnd + "]");
    buf.append(" ");
    buf.append("add: [" + addStart + ", " + addEnd + "]");
    return buf.toString();
  }
}
