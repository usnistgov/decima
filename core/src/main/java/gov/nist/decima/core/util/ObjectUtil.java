/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */
package gov.nist.decima.core.util;

import java.util.Collection;

public class ObjectUtil {

  private ObjectUtil() {
    // disable construction
  }

  /**
   * Checks that the provided string is not empty.
   * 
   * @param str
   *          the string to check
   * @return the same string
   * @throws NullPointerException
   *           if the provided string is <code>null</code>
   * @throws IllegalArgumentException
   *           if the provided string is empty
   */
  public static String requireNonEmpty(String str) {
    if (str.isEmpty()) {
      throw new IllegalArgumentException();
    }
    return str;
  }

  /**
   * Checks that the provided string is not empty.
   * 
   * @param str
   *          the string to check
   * @param message
   *          the exception message to use
   * @return the same string
   * @throws NullPointerException
   *           if the provided string is <code>null</code>
   * @throws IllegalArgumentException
   *           if the provided string is empty
   */
  public static String requireNonEmpty(String str, String message) {
    if (str.isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return str;
  }

  /**
   * Checks that the provided collection is not empty.
   * 
   * @param <T>
   *          the contained type of the collection
   * @param col
   *          the collection to check
   * @return the same collection
   * @throws NullPointerException
   *           if the provided collection is <code>null</code>
   * @throws IllegalArgumentException
   *           if the provided collection is empty
   */
  public static <T> Collection<T> requireNonEmpty(Collection<T> col) {
    if (col.isEmpty()) {
      throw new IllegalArgumentException();
    }
    return col;
  }

  /**
   * Checks that the provided collection is not empty.
   * 
   * @param <T>
   *          the contained type of the collection
   * @param col
   *          the collection to check
   * @param message
   *          the exception message to use
   * @return the same collection
   * @throws NullPointerException
   *           if the provided collection is <code>null</code>
   * @throws IllegalArgumentException
   *           if the provided collection is empty
   */
  public static <T> Collection<T> requireNonEmpty(Collection<T> col, String message) {
    if (col.isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return col;
  }

  /**
   * Checks that the provided string is either null or not empty.
   * 
   * @param str
   *          the string to check
   * @return the same string
   * @throws IllegalArgumentException
   *           if the provided string is not null and is empty
   */
  public static String requireNullOrNonEmpty(String str) {
    if (str != null && str.isEmpty()) {
      throw new IllegalArgumentException();
    }
    return str;
  }

  /**
   * Checks that the provided string is either null or not empty.
   * 
   * @param str
   *          the string to check
   * @param message
   *          the exception message to use
   * @return the same string
   * @throws IllegalArgumentException
   *           if the provided string is not null and is empty
   */
  public static String requireNullOrNonEmpty(String str, String message) {
    if (str != null && str.isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return str;
  }

  /**
   * Checks that the provided collection is either null or not empty.
   * 
   * @param <T>
   *          the contained type of the collection
   * @param col
   *          the collection to check
   * @return the same collection
   * @throws IllegalArgumentException
   *           if the provided string is not null and is empty
   */
  public static <T> Collection<T> requireNullOrNonEmpty(Collection<T> col) {
    if (col != null && col.isEmpty()) {
      throw new IllegalArgumentException();
    }
    return col;
  }

  /**
   * Checks that the provided collection is either null or not empty.
   * 
   * @param <T>
   *          the contained type of the collection
   * @param col
   *          the collection to check
   * @param message
   *          the exception message to use
   * @return the same collection
   * @throws IllegalArgumentException
   *           if the provided string is not null and is empty
   */
  public static <T> Collection<T> requireNullOrNonEmpty(Collection<T> col, String message) {
    if (col != null && col.isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return col;
  }
}
