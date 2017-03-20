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
 * SHALL NASA BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.decima.core.assessment;

import gov.nist.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.decima.core.document.Document;

import java.util.Collections;
import java.util.List;

/**
 * A concrete implementation of a {@link ConditionalAssessment} that acts as a decorator for another
 * assessment. This allows any {@link Assessment} implementation to be made conditional.
 */
public class DefaultConditionalAssessment<DOC extends Document> implements ConditionalAssessment<DOC> {
  private final Assessment<DOC> delegate;
  private final Condition<DOC> condition;

  public DefaultConditionalAssessment(Assessment<DOC> delegate, Condition<DOC> condition) {
    this.delegate = delegate;
    this.condition = condition;
  }

  @Override
  public String getName(boolean includeDetail) {
    return delegate.getName(includeDetail);
  }

  @Override
  public String getAssessmentType() {
    return delegate.getAssessmentType();
  }

  public Assessment<DOC> getDelegate() {
    return delegate;
  }

  @Override
  public List<Assessment<DOC>> getExecutableAssessments(DOC document) throws AssessmentException {
    List<Assessment<DOC>> retval;
    if (appliesTo(document)) {
      retval = Collections.singletonList(getDelegate());
    } else {
      retval = Collections.emptyList();
    }
    return retval;
  }

  @Override
  public void execute(DOC document, AssessmentResultBuilder builder) throws AssessmentException {

    throw new UnsupportedOperationException(
        "The execute method must be called through the instances returned by getExecutableAssessments(Document).");
    // delegate.execute(document, builder, notifier);
  }

  @Override
  public boolean appliesTo(DOC targetDocument) throws AssessmentException {
    return condition.appliesTo(targetDocument);
  }

}
