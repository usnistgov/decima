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
/**
 * This package is the core of Decima providing assessment functions for evaluating a series of
 * tests grouped into assessments that check the well-formedness and content-completeness of a
 * document instance.
 * <p>
 * This package supports a pattern of evaluation where a document is evaluated by executing one or
 * more {@link gov.nist.decima.core.assessment.Assessment Assessment} instances. The document
 * instance is represented by a {@link gov.nist.decima.core.document.Document} object instance. This
 * assessment pattern is handled by an implementation of an
 * {@link gov.nist.decima.core.assessment.AssessmentExecutor AssessmentExecutor}.
 * <p>
 * The following example illustrates the typical way of setting up an assessment execution:
 * 
 * <pre>
 * {@code
 * List<Assessment> assessments = new ArrayList<>(2);
 * Assessment assessment1 = new ...
 * assessments.add(assessment1);
 * Assessment assessment2 = new ...
 * assessments.add(assessment2);
 * 
 * Document document = new ...
 * 
 * BasicAssessmentExecutor executor = new BasicAssessmentExecutor(assessments);
 * AssessmentResults results = executor.execute(document);
 * 
 * // do something with the results
 * }
 * </pre>
 */

package gov.nist.decima.core.assessment;