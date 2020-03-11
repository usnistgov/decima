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

package gov.nist.secauto.decima.core.assessment.result;

import gov.nist.secauto.decima.core.document.Context;
import gov.nist.secauto.decima.core.requirement.DerivedRequirement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Describes a {@link Context} within a content instance where a {@link DerivedRequirement} was
 * tested and found to be evaluated to some status. This class supports a list of result values that
 * are generated during the assessment evaluation that further provide context. These values are
 * used in the formatting of result messages based on the format string of the
 * {@link DerivedRequirement}.
 */
public class BasicTestResult implements TestResult {
  private final String testId;
  private final TestStatus status;
  private final List<String> resultValues = new LinkedList<>();
  private final Context context;

  /**
   * Construct a new test result.
   * 
   * @param testId
   *          the identifier for the test performed
   * @param result
   *          the result of the test performed
   * @param context
   *          the document context associated with the test result
   */
  public BasicTestResult(String testId, TestStatus result, Context context) {
    this.testId = testId;
    this.status = result;
    this.context = context;
  }

  @Override
  public String getTestId() {
    return testId;
  }

  @Override
  public TestStatus getStatus() {
    return status;
  }

  /**
   * Adds a new result value to the ordered collection of result values. This value must be
   * {@code non-null}.
   * 
   * @param value
   *          the new result value to add
   */
  public void addResultValue(String value) {
    Objects.requireNonNull(value);
    resultValues.add(value);
  }

  /**
   * Adds a number of new result value to the end of the ordered collection of result values. This
   * value must be {@code non-null}.
   * 
   * @param values
   *          the new result values to add
   */
  public void addResultValues(Collection<String> values) {
    Objects.requireNonNull(values);
    resultValues.addAll(values);
  }

  @Override
  public List<String> getResultValues() {
    return resultValues;
  }

  @Override
  public Context getContext() {
    return context;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", getTestId())
        .append("status", getStatus()).toString();
  }
}
