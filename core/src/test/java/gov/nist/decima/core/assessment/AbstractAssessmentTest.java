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

package gov.nist.decima.core.assessment;

import gov.nist.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.decima.core.document.Document;

import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

public class AbstractAssessmentTest {
    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testChangeResultDirectory() throws IOException {
        File resultDirectory = folder.newFolder();
        TestCallback callback = context.mock(TestCallback.class);

        TestableAbstractAssessment assessment = new TestableAbstractAssessment(callback);
        assessment.setResultDirectory(resultDirectory);
        Assert.assertSame(resultDirectory, assessment.getResultDirectory());

        assessment.setResultDirectory(null);
        Assert.assertNull(assessment.getResultDirectory());
    }

    @Test
    public void testExecute() throws AssessmentException, IOException {
        TestCallback callback = context.mock(TestCallback.class);
        Document document = context.mock(Document.class);

        AssessmentResultBuilder builder = context.mock(AssessmentResultBuilder.class);

        File resultDirectory = folder.newFolder();
        Assert.assertTrue(resultDirectory.delete());

        TestableAbstractAssessment assessment = new TestableAbstractAssessment(callback);
        assessment.setResultDirectory(resultDirectory);

        Sequence sequence = context.sequence("execute-assessment");
        context.checking(new Expectations() {
            {
                oneOf(callback).handleExecuteInternal(with(same(document)), with(same(builder)));
                inSequence(sequence);
                oneOf(builder).addAssessmentTarget(with(same(document)));
            }
        });

        assessment.execute(document, builder);
        Assert.assertTrue(resultDirectory.exists());
    }

    @Test
    public void testExecuteNoResultDir() throws AssessmentException, IOException {
        TestCallback callback = context.mock(TestCallback.class);
        Document document = context.mock(Document.class);

        AssessmentResultBuilder builder = context.mock(AssessmentResultBuilder.class);

        TestableAbstractAssessment assessment = new TestableAbstractAssessment(callback);

        Sequence sequence = context.sequence("execute-assessment");
        context.checking(new Expectations() {
            {
                oneOf(callback).handleExecuteInternal(with(same(document)), with(same(builder)));
                inSequence(sequence);
                oneOf(builder).addAssessmentTarget(with(same(document)));
            }
        });

        assessment.execute(document, builder);
    }

    @Test
    public void testGetNameNull() throws AssessmentException, IOException {
        TestCallback callback = context.mock(TestCallback.class);

        TestableAbstractAssessment assessment = new TestableAbstractAssessment(callback);

        Sequence sequence = context.sequence("execute-assessment");
        context.checking(new Expectations() {
            {
                oneOf(callback).handleGetAssessmentType();
                will(returnValue("test"));
                inSequence(sequence);
                oneOf(callback).handleGetNameDetails();
                will(returnValue(null));
                inSequence(sequence);
            }
        });

        String name = assessment.getName(false);
        Assert.assertEquals("[1]test", name);
    }

    @Test
    public void testGetNameNonNull() throws AssessmentException, IOException {
        TestCallback callback = context.mock(TestCallback.class);

        TestableAbstractAssessment assessment = new TestableAbstractAssessment(callback);

        Sequence sequence = context.sequence("execute-assessment");
        context.checking(new Expectations() {
            {
                oneOf(callback).handleGetAssessmentType();
                will(returnValue("test"));
                inSequence(sequence);
                oneOf(callback).handleGetNameDetails();
                will(returnValue("test2"));
                inSequence(sequence);
            }
        });

        String name = assessment.getName(true);
        Assert.assertEquals("[2]test: test2", name);
    }

    public static interface TestCallback {
        String handleGetAssessmentType();

        void handleExecuteInternal(Document document, AssessmentResultBuilder builder) throws AssessmentException;

        String handleGetNameDetails();
    }

    private static class TestableAbstractAssessment extends AbstractAssessment<Document> {
        private final TestCallback callback;

        public TestableAbstractAssessment(TestCallback callback) {
            this.callback = callback;
        }

        @Override
        public String getAssessmentType() {
            return callback.handleGetAssessmentType();
        }

        @Override
        protected void executeInternal(Document document, AssessmentResultBuilder builder) throws AssessmentException {
            callback.handleExecuteInternal(document, builder);
        }

        @Override
        protected String getNameDetails() {
            return callback.handleGetNameDetails();
        }

    }
}
