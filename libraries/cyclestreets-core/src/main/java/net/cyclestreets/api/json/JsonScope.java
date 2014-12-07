package net.cyclestreets.api.json;

enum JsonScope {

  /**
   * An array with no elements requires no separators or newlines before
   * it is closed.
   */
  EMPTY_ARRAY,

  /**
   * A array with at least one value requires a comma and newline before
   * the next element.
   */
  NONEMPTY_ARRAY,

  /**
   * An object with no name/value pairs requires no separators or newlines
   * before it is closed.
   */
  EMPTY_OBJECT,

  /**
   * An object whose most recent element is a key. The next element must
   * be a value.
   */
  DANGLING_NAME,

  /**
   * An object with at least one name/value pair requires a comma and
   * newline before the next element.
   */
  NONEMPTY_OBJECT,

  /**
   * No object or array has been started.
   */
  EMPTY_DOCUMENT,

  /**
   * A document with at an array or object.
   */
  NONEMPTY_DOCUMENT,

  /**
   * A document that's been closed and cannot be accessed.
   */
  CLOSED,
}
