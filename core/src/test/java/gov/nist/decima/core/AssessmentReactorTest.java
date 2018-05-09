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

package gov.nist.decima.core;

import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.assessment.AssessmentException;
import gov.nist.decima.core.assessment.AssessmentExecutor;
import gov.nist.decima.core.assessment.AssessmentReactor;
import gov.nist.decima.core.assessment.ConditionalAssessment;
import gov.nist.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.decima.core.document.Document;
import gov.nist.decima.core.requirement.RequirementsManager;

import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssessmentReactorTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testNullRequirementsManager() {
        exception.expect(NullPointerException.class);

        new AssessmentReactor(null) {
        };
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPushAndReact() throws AssessmentException {
        ConditionalAssessment<Document> assessment1
                = (ConditionalAssessment<Document>) context.mock(ConditionalAssessment.class, "assessment1");

        Assessment<Document> assessment2 = context.mock(Assessment.class, "assessment2");
        Assessment<Document> assessment3 = context.mock(Assessment.class, "assessment3");

        List<Assessment<Document>> assesments = new ArrayList<>(3);
        assesments.add(assessment1);
        assesments.add(assessment2);
        assesments.add(assessment3);

        RequirementsManager requirementsManager = context.mock(RequirementsManager.class);
        AssessmentExecutor<Document> executor = context.mock(AssessmentExecutor.class);
        Document document = context.mock(Document.class);

        Sequence sequence = context.sequence("execute-assessments");
        context.checking(new Expectations() {
            {
                // Starting the assessment execution
                oneOf(executor).execute(with(same(document)), with(any(AssessmentResultBuilder.class)));
                inSequence(sequence);

                // Preparing to produce the AssessmentResults
                oneOf(requirementsManager).getBaseRequirements();
                will(returnValue(Collections.emptyList()));
                inSequence(sequence);
            }
        });

        new AssessmentReactor(requirementsManager).pushAssessmentExecution(document, executor).react();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPushAndReactNullNotifier() throws AssessmentException {
        RequirementsManager requirementsManager = context.mock(RequirementsManager.class);
        AssessmentExecutor<Document> executor = context.mock(AssessmentExecutor.class);
        Document document = context.mock(Document.class);

        Sequence sequence = context.sequence("execute-assessments");
        context.checking(new Expectations() {
            {
                // Starting the assessment execution
                oneOf(executor).execute(with(same(document)), with(any(AssessmentResultBuilder.class)));
                inSequence(sequence);

                // Preparing to produce the AssessmentResults
                oneOf(requirementsManager).getBaseRequirements();
                will(returnValue(Collections.emptyList()));
                inSequence(sequence);
            }
        });

        new AssessmentReactor(requirementsManager).pushAssessmentExecution(document, executor).react();
    }
}
