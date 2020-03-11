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

import gov.nist.secauto.decima.core.assessment.Assessment;
import gov.nist.secauto.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.secauto.decima.core.assessment.result.AssessmentResults;
import gov.nist.secauto.decima.core.assessment.result.BaseRequirementResult;
import gov.nist.secauto.decima.core.assessment.result.DerivedRequirementResult;
import gov.nist.secauto.decima.core.assessment.result.ResultStatus;
import gov.nist.secauto.decima.core.assessment.result.TestResult;
import gov.nist.secauto.decima.core.assessment.result.TestStatus;
import gov.nist.secauto.decima.core.document.Document;
import gov.nist.secauto.decima.core.requirement.BaseRequirement;
import gov.nist.secauto.decima.core.requirement.DerivedRequirement;
import gov.nist.secauto.decima.core.requirement.RequirementType;
import gov.nist.secauto.decima.core.requirement.RequirementsManager;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.List;

public abstract class AbstractAssessmentResultBuilderTest {

  protected abstract AssessmentResultBuilder newInstance();

  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery();

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testEndBeforeStart() {
    try {
      newInstance().end().start();
      Assert.fail("Exception not thrown");
    } catch (IllegalStateException ex) {
      // pass
    }
  }

  @Test
  public void testBuildBeforeStart() {
    RequirementsManager requirementsManager = context.mock(RequirementsManager.class);
    try {
      newInstance().build(requirementsManager);
      Assert.fail("Exception not thrown");
    } catch (IllegalStateException ex) {
      // pass
    }
  }

  @Test
  public void testAddTestResult() {
    @SuppressWarnings("unchecked")
    Assessment<Document> assessment = (Assessment<Document>) context.mock(Assessment.class);
    Document document = context.mock(Document.class);

    RequirementsManager requirementsManager = context.mock(RequirementsManager.class);

    String baseRequirementId = "BASE-1";
    BaseRequirement baseRequirement = context.mock(BaseRequirement.class);
    // BaseRequirementResult baseRequirementResult = context.mock(BaseRequirementResult.class);

    String derivedRequirementId = "DER-1";
    DerivedRequirement derivedRequirement = context.mock(DerivedRequirement.class);

    String testId = "TEST-1";
    TestResult testResult = context.mock(TestResult.class);

    // Sequence sequence = context.sequence("test");
    context.checking(new Expectations() {
      {
        // The requirement manager contains a single base requirement
        allowing(requirementsManager).getBaseRequirements();
        will(returnValue(Collections.singleton(baseRequirement)));
        allowing(requirementsManager).getBaseRequirementById(with(same(baseRequirementId)));
        will(returnValue(baseRequirement));

        // This is that base requirement
        allowing(baseRequirement).getId();
        will(returnValue(baseRequirementId));
        // containing a single derived requirement
        allowing(baseRequirement).getDerivedRequirements();
        will(returnValue(Collections.singleton(derivedRequirement)));
        allowing(baseRequirement).getDerivedRequirementById(with(same(derivedRequirementId)));
        will(returnValue(derivedRequirement));

        // This is the derived requirement
        allowing(derivedRequirement).getId();
        will(returnValue(derivedRequirementId));
        allowing(derivedRequirement).getType();
        will(returnValue(RequirementType.MUST));
        allowing(derivedRequirement).isConditional();
        will(returnValue(false));

        // A single test result relates to that derived requirement
        allowing(testResult).getTestId();
        will(returnValue(testId));
        allowing(testResult).getStatus();
        will(returnValue(TestStatus.FAIL));
      }
    });

    AssessmentResults results = newInstance().addTestResult(assessment, document, derivedRequirementId, testResult)
        .end().build(requirementsManager);
    BaseRequirementResult baseResult = results.getBaseRequirementResult(baseRequirementId);
    Assert.assertSame(baseRequirement, baseResult.getBaseRequirement());
    Assert.assertSame(ResultStatus.FAIL, baseResult.getStatus());
    List<DerivedRequirementResult> derivedResults = baseResult.getDerivedRequirementResults();
    Assert.assertSame(1, derivedResults.size());
    DerivedRequirementResult derivedResult = derivedResults.get(0);
    Assert.assertSame(derivedRequirement, derivedResult.getDerivedRequirement());
    Assert.assertSame(ResultStatus.FAIL, derivedResult.getStatus());
  }
}
