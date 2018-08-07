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

package gov.nist.decima.core.requirement;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractRequirement implements Requirement {
  private final String id;
  private final String statement;
  private final Map<String, Set<String>> metadataMap = new HashMap<>();

  /**
   * Constructs an {@link AbstractRequirement} instance.
   * 
   * @param id
   *          the requirement identifier
   * @param statement
   *          the statement describing the derived requirement
   */
  public AbstractRequirement(String id, String statement) {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(statement, "statement");
    this.id = id;
    this.statement = statement;
  }

  /**
   * Adds a metadata element to the requirement consisting of a name and a collection of values.
   * 
   * @param name
   *          the name of the metadata element
   * @param values
   *          the element's values
   */
  public void addMetadata(String name, List<String> values) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(values);
    if (values.isEmpty()) {
      throw new IllegalArgumentException("values is empty");
    }
    getOrCreateValueList(name).addAll(values);
  }

  /**
   * Adds a metadata element to the requirement consisting of a name/value pair.
   * 
   * @param name
   *          the name of the metadata element
   * @param value
   *          the element's value
   */
  public void addMetadata(String name, String value) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(value);
    getOrCreateValueList(name).add(value);
  }

  private Set<String> getOrCreateValueList(String name) {
    Set<String> retval = metadataMap.get(name);
    if (retval == null) {
      retval = new LinkedHashSet<>();
      metadataMap.put(name, retval);
    }
    return retval;
  }

  public Set<String> removeMetadata(String name) {
    return metadataMap.remove(name);
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getStatement() {
    return statement;
  }

  @Override
  public Map<String, Set<String>> getMetadataTagValueMap() {
    return Collections.unmodifiableMap(metadataMap);
  }
}
