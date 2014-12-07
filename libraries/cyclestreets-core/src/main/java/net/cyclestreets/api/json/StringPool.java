package net.cyclestreets.api.json;

/**
 * A pool of string instances. Unlike the {@link String#intern() VM's
 * interned strings}, this pool provides no guarantee of reference equality.
 * It is intended only to save allocations. This class is not thread safe.
 */
final class StringPool {
  private final String[] pool = new String[512];
  private static boolean contentEquals(String s, char[] chars, int start, int length) {
    if (s.length() != length) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      if (chars[start + i] != s.charAt(i)) {
        return false;
      }
    }
    return true;
  }
  /**
   * Returns a string equal to {@code new String(array, start, length)}.
   */
  public String get(char[] array, int start, int length) {
    // Compute an arbitrary hash of the content
    int hashCode = 0;
    for (int i = start; i < start + length; i++) {
      hashCode = (hashCode * 31) + array[i];
    }
    // Pick a bucket using Doug Lea's supplemental secondaryHash function (from HashMap)
    hashCode ^= (hashCode >>> 20) ^ (hashCode >>> 12);
    hashCode ^= (hashCode >>> 7) ^ (hashCode >>> 4);
    int index = hashCode & (pool.length - 1);
    String pooled = pool[index];
    if (pooled != null && contentEquals(pooled, array, start, length)) {
      return pooled;
    }
    String result = new String(array, start, length);
    pool[index] = result;
    return result;
  }
}


