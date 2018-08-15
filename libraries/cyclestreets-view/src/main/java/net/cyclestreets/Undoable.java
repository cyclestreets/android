package net.cyclestreets;

public interface Undoable
{
  /**
   * Interface which our main app fragments may choose to implement, allowing them to handle the "Back" button action.
   *
   * @return True if the fragment has processed and wishes to swallow the "Back" action;
   *         False otherwise (in which case the main activity "Back" action will be performed).
   */
  boolean onBackPressed();
} // Undoable