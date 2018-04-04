/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2009 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;

/**
 * Represents a text modification as a document replace command. The text modification is given as a
 * {@link swt.events.VerifyEvent} and translated into a document replace command relative to a given
 * offset. A document command can also be used to initialize a given <code>VerifyEvent</code>.
 *
 * <p>A document command can also represent a list of related changes.
 */
public class DocumentCommand {

  /**
   * A command which is added to document commands.
   *
   * @since 2.1
   */
  private static class Command implements Comparable {
    /** The offset of the range to be replaced */
    private final int fOffset;
    /** The length of the range to be replaced. */
    private final int fLength;
    /** The replacement text */
    private final String fText;
    /** The listener who owns this command */
    private final IDocumentListener fOwner;

    /**
     * Creates a new command with the given specification.
     *
     * @param offset the offset of the replace command
     * @param length the length of the replace command
     * @param text the text to replace with, may be <code>null</code>
     * @param owner the document command owner, may be <code>null</code>
     * @since 3.0
     */
    public Command(int offset, int length, String text, IDocumentListener owner) {
      if (offset < 0 || length < 0) throw new IllegalArgumentException();
      fOffset = offset;
      fLength = length;
      fText = text;
      fOwner = owner;
    }

    /**
     * Executes the document command on the specified document.
     *
     * @param document the document on which to execute the command.
     * @throws BadLocationException in case this commands cannot be executed
     */
    public void execute(IDocument document) throws BadLocationException {

      if (fLength == 0 && fText == null) return;

      if (fOwner != null) document.removeDocumentListener(fOwner);

      document.replace(fOffset, fLength, fText);

      if (fOwner != null) document.addDocumentListener(fOwner);
    }

    /*
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compareTo(final Object object) {
      if (isEqual(object)) return 0;

      final Command command = (Command) object;

      // diff middle points if not intersecting
      if (fOffset + fLength <= command.fOffset || command.fOffset + command.fLength <= fOffset) {
        int value = (2 * fOffset + fLength) - (2 * command.fOffset + command.fLength);
        if (value != 0) return value;
      }
      // the answer
      return 42;
    }

    private boolean isEqual(Object object) {
      if (object == this) return true;
      if (!(object instanceof Command)) return false;
      final Command command = (Command) object;
      return command.fOffset == fOffset && command.fLength == fLength;
    }
  }

  /** An iterator, which iterates in reverse over a list. */
  private static class ReverseListIterator implements Iterator {

    /** The list iterator. */
    private final ListIterator fListIterator;

    /**
     * Creates a reverse list iterator.
     *
     * @param listIterator the iterator that this reverse iterator is based upon
     */
    public ReverseListIterator(ListIterator listIterator) {
      if (listIterator == null) throw new IllegalArgumentException();
      fListIterator = listIterator;
    }

    /*
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
      return fListIterator.hasPrevious();
    }

    /*
     * @see java.util.Iterator#next()
     */
    public Object next() {
      return fListIterator.previous();
    }

    /*
     * @see java.util.Iterator#remove()
     */
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  /** A command iterator. */
  private static class CommandIterator implements Iterator {

    /** The command iterator. */
    private final Iterator fIterator;

    /** The original command. */
    private Command fCommand;

    /** A flag indicating the direction of iteration. */
    private boolean fForward;

    /**
     * Creates a command iterator.
     *
     * @param commands an ascending ordered list of commands
     * @param command the original command
     * @param forward the direction
     */
    public CommandIterator(final List commands, final Command command, final boolean forward) {
      if (commands == null || command == null) throw new IllegalArgumentException();
      fIterator =
          forward
              ? commands.iterator()
              : new ReverseListIterator(commands.listIterator(commands.size()));
      fCommand = command;
      fForward = forward;
    }

    /*
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
      return fCommand != null || fIterator.hasNext();
    }

    /*
     * @see java.util.Iterator#next()
     */
    public Object next() {

      if (!hasNext()) throw new NoSuchElementException();

      if (fCommand == null) return fIterator.next();

      if (!fIterator.hasNext()) {
        final Command tempCommand = fCommand;
        fCommand = null;
        return tempCommand;
      }

      final Command command = (Command) fIterator.next();
      final int compareValue = command.compareTo(fCommand);

      if ((compareValue < 0) ^ !fForward) {
        return command;

      } else if ((compareValue > 0) ^ !fForward) {
        final Command tempCommand = fCommand;
        fCommand = command;
        return tempCommand;

      } else {
        throw new IllegalArgumentException();
      }
    }

    /*
     * @see java.util.Iterator#remove()
     */
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  /** Must the command be updated */
  public boolean doit = false;
  /** The offset of the command. */
  public int offset;
  /** The length of the command */
  public int length;
  /** The text to be inserted */
  public String text;
  /**
   * The owner of the document command which will not be notified.
   *
   * @since 2.1
   */
  public IDocumentListener owner;
  /**
   * The caret offset with respect to the document before the document command is executed.
   *
   * @since 2.1
   */
  public int caretOffset;
  /**
   * Additional document commands.
   *
   * @since 2.1
   */
  private final List fCommands = new ArrayList();
  /**
   * Indicates whether the caret should be shifted by this command.
   *
   * @since 3.0
   */
  public boolean shiftsCaret;

  /** Creates a new document command. */
  protected DocumentCommand() {}

  //	/**
  //	 * Translates a verify event into a document replace command using the given offset.
  //	 *
  //	 * @param event the event to be translated
  //	 * @param modelRange the event range as model range
  //	 */
  //	void setEvent(VerifyEvent event, IRegion modelRange) {
  //
  //		doit= true;
  //		text= event.text;
  //
  //		offset= modelRange.getOffset();
  //		length= modelRange.getLength();
  //
  //		owner= null;
  //		caretOffset= -1;
  //		shiftsCaret= true;
  //		fCommands.clear();
  //	}
  //
  //	/**
  //	 * Fills the given verify event with the replace text and the <code>doit</code>
  //	 * flag of this document command. Returns whether the document command
  //	 * covers the same range as the verify event considering the given offset.
  //	 *
  //	 * @param event the event to be changed
  //	 * @param modelRange to be considered for range comparison
  //	 * @return <code>true</code> if this command and the event cover the same range
  //	 */
  //	boolean fillEvent(VerifyEvent event, IRegion modelRange) {
  //		event.text= text;
  //		event.doit= (offset == modelRange.getOffset() && length == modelRange.getLength() && doit &&
  // caretOffset == -1);
  //		return event.doit;
  //	}

  /**
   * Adds an additional replace command. The added replace command must not overlap with existing
   * ones. If the document command owner is not <code>null</code>, it will not get document change
   * notifications for the particular command.
   *
   * @param commandOffset the offset of the region to replace
   * @param commandLength the length of the region to replace
   * @param commandText the text to replace with, may be <code>null</code>
   * @param commandOwner the command owner, may be <code>null</code>
   * @throws BadLocationException if the added command intersects with an existing one
   * @since 2.1
   */
  public void addCommand(
      int commandOffset, int commandLength, String commandText, IDocumentListener commandOwner)
      throws BadLocationException {
    final Command command = new Command(commandOffset, commandLength, commandText, commandOwner);

    if (intersects(command)) throw new BadLocationException();

    final int index = Collections.binarySearch(fCommands, command);

    // a command with exactly the same ranges exists already
    if (index >= 0) throw new BadLocationException();

    // binary search result is defined as (-(insertionIndex) - 1)
    final int insertionIndex = -(index + 1);

    // overlaps to the right?
    if (insertionIndex != fCommands.size()
        && intersects((Command) fCommands.get(insertionIndex), command))
      throw new BadLocationException();

    // overlaps to the left?
    if (insertionIndex != 0 && intersects((Command) fCommands.get(insertionIndex - 1), command))
      throw new BadLocationException();

    fCommands.add(insertionIndex, command);
  }

  /**
   * Returns an iterator over the commands in ascending position order. The iterator includes the
   * original document command. Commands cannot be removed.
   *
   * @return returns the command iterator
   */
  public Iterator getCommandIterator() {
    Command command = new Command(offset, length, text, owner);
    return new CommandIterator(fCommands, command, true);
  }

  /**
   * Returns the number of commands including the original document command.
   *
   * @return returns the number of commands
   * @since 2.1
   */
  public int getCommandCount() {
    return 1 + fCommands.size();
  }

  /**
   * Returns whether the two given commands intersect.
   *
   * @param command0 the first command
   * @param command1 the second command
   * @return <code>true</code> if the commands intersect
   * @since 2.1
   */
  private boolean intersects(Command command0, Command command1) {
    // diff middle points if not intersecting
    if (command0.fOffset + command0.fLength <= command1.fOffset
        || command1.fOffset + command1.fLength <= command0.fOffset)
      return (2 * command0.fOffset + command0.fLength) - (2 * command1.fOffset + command1.fLength)
          == 0;
    return true;
  }

  /**
   * Returns whether the given command intersects with this command.
   *
   * @param command the command
   * @return <code>true</code> if the command intersects with this command
   * @since 2.1
   */
  private boolean intersects(Command command) {
    // diff middle points if not intersecting
    if (offset + length <= command.fOffset || command.fOffset + command.fLength <= offset)
      return (2 * offset + length) - (2 * command.fOffset + command.fLength) == 0;
    return true;
  }

  /**
   * Executes the document commands on a document.
   *
   * @param document the document on which to execute the commands
   * @throws BadLocationException in case access to the given document fails
   * @since 2.1
   */
  void execute(IDocument document) throws BadLocationException {

    if (length == 0 && text == null && fCommands.size() == 0) return;

    DefaultPositionUpdater updater = new DefaultPositionUpdater(getCategory());
    Position caretPosition = null;
    try {
      if (updateCaret()) {
        document.addPositionCategory(getCategory());
        document.addPositionUpdater(updater);
        caretPosition = new Position(caretOffset);
        document.addPosition(getCategory(), caretPosition);
      }

      final Command originalCommand = new Command(offset, length, text, owner);
      for (final Iterator iterator = new CommandIterator(fCommands, originalCommand, false);
          iterator.hasNext(); ) ((Command) iterator.next()).execute(document);

    } catch (BadLocationException e) {
      // ignore
    } catch (BadPositionCategoryException e) {
      // ignore
    } finally {
      if (updateCaret()) {
        document.removePositionUpdater(updater);
        try {
          document.removePositionCategory(getCategory());
        } catch (BadPositionCategoryException e) {
          Assert.isTrue(false);
        }
        caretOffset = caretPosition.getOffset();
      }
    }
  }

  /**
   * Returns <code>true</code> if the caret offset should be updated, <code>false</code> otherwise.
   *
   * @return <code>true</code> if the caret offset should be updated, <code>false</code> otherwise
   * @since 3.0
   */
  private boolean updateCaret() {
    return shiftsCaret && caretOffset != -1;
  }

  /**
   * Returns the position category for the caret offset position.
   *
   * @return the position category for the caret offset position
   * @since 3.0
   */
  private String getCategory() {
    return toString();
  }
}
