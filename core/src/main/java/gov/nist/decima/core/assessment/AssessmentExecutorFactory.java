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

import gov.nist.decima.core.Decima;
import gov.nist.decima.core.document.Document;

import java.util.List;

/**
 * 
 * An interface for a factory that creates {@link AssessmentExecutor} instances. The factory object
 * {@link Decima} can construct a number of different types of {@link AssessmentExecutorFactory}
 * instances.
 * <p>
 * In general, implementations of this class are not required to be thread safe, since generation of
 * an {@link AssessmentExecutor} will typically be handled in a single thread.
 * 
 */
public interface AssessmentExecutorFactory {

    /**
     * Constructs a new {@link AssessmentExecutor} that can execute the provided assessments over
     * one or more documents.
     * 
     * @param <DOC>
     *            the type of document that is the target of the assessment
     * @param assessments
     *            the list of assessments to execute against each {@link Document} instance provided
     *            to the {@link AssessmentExecutor}
     * @return a new {@link AssessmentExecutor}
     * @see AssessmentExecutor#execute(Document,
     *      gov.nist.decima.core.assessment.result.AssessmentResultBuilder)
     */
    <DOC extends Document> AssessmentExecutor<DOC> newAssessmentExecutor(List<? extends Assessment<DOC>> assessments);
}
