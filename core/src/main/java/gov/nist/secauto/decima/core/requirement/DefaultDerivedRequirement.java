/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.decima.core.requirement;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DefaultDerivedRequirement extends AbstractRequirement implements DerivedRequirement {
  private final BaseRequirement baseRequirement;
  private final RequirementType type;
  private final boolean conditional;
  private final String message;

  /**
   * Constructs a derived requirement. A message may contain a variable placeholder using the {}
   * symbols.
   * 
   * @param baseRequirement
   *          the base requirement this requirement is derived from
   * @param id
   *          the derived requirement id
   * @param statement
   *          the statement describing the derived requirement
   * @param type
   *          the type of requirement
   * @param conditional
   *          if the requirement is conditional (e.g., "if X, them Y")
   * @param message
   *          an optional message to use if the requirement fails
   */
  public DefaultDerivedRequirement(BaseRequirement baseRequirement, String id, String statement, RequirementType type,
      boolean conditional, String message) {
    super(id, statement);
    Objects.requireNonNull(baseRequirement, "baseRequirement");
    Objects.requireNonNull(type, "type");
    this.type = type;
    this.conditional = conditional;
    this.baseRequirement = baseRequirement;
    this.message = message;
  }

  @Override
  public BaseRequirement getBaseRequirement() {
    return baseRequirement;
  }

  @Override
  public RequirementType getType() {
    return type;
  }

  @Override
  public boolean isConditional() {
    return conditional;
  }

  @Override
  public String getMessageText(String... args) {
    String retval = null;
    if (message != null) {
      MessageFormat mf = new MessageFormat(message);
      retval = mf.format(args);
    } else if (args != null && args.length > 0) {
      StringBuilder builder = new StringBuilder();
      boolean first = true;
      for (String s : args) {
        if (first) {
          first = false;
        } else {
          builder.append(' ');
        }
        builder.append(s);
      }
      retval = builder.toString();
    }
    return retval;
  }

  @Override
  public Map<String, Set<String>> getMetadataTagValueMap() {
    // make a copy of the base requirements to avoid modifying the internal value
    // set
    Map<String, Set<String>> retval = deepCopyMap(getBaseRequirement().getMetadataTagValueMap());

    // Merge the derived requirements in
    for (Map.Entry<String, Set<String>> entry : super.getMetadataTagValueMap().entrySet()) {
      Set<String> set = retval.get(entry.getKey());
      if (set == null) {
        set = new LinkedHashSet<>();
        retval.put(entry.getKey(), set);
      }
      set.addAll(entry.getValue());
    }
    return Collections.unmodifiableMap(retval);
  }

  private static Map<String, Set<String>> deepCopyMap(Map<String, Set<String>> source) {
    Map<String, Set<String>> retval = new HashMap<>();
    for (Map.Entry<String, Set<String>> entry : source.entrySet()) {
      retval.put(entry.getKey(), new LinkedHashSet<>(entry.getValue()));
    }
    return retval;
  }
}
