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

package gov.nist.decima.core.assessment;

import gov.nist.decima.core.document.Document;
import gov.nist.decima.core.util.ExecutorServiceUtil;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class provides a mechanism to create new {@link AssessmentExecutor} instances that leverage
 * the same {@link Executor} instance to execute a sequence of configured {@link Assessment}
 * instances against a {@link Document}.
 * <p>
 * Instances of this class are immutable and can be shared between threads.
 *
 */
public class ConcurrentAssessmentExecutorFactory implements AssessmentExecutorFactory {
  private final Executor executor;

  /**
   * Creates an {@link AssessmentExecutorFactory} that uses a {@link ThreadPoolExecutor} of the
   * requested size to execute a sequences of configured {@link Assessment} instances. A shutdown hook
   * is automatically added that will close this executor when the application ends. For greater
   * control over the executor used or shutdown behavior, use the
   * {@link #ConcurrentAssessmentExecutorFactory(Executor)} constructor instead.
   * 
   * @param threads
   *          the size of the thread pool
   */
  public ConcurrentAssessmentExecutorFactory(int threads) {
    this(ExecutorServiceUtil.addShutdownHook(Executors.newFixedThreadPool(threads), 10, TimeUnit.SECONDS));
  }

  public ConcurrentAssessmentExecutorFactory(Executor executor) {
    this.executor = executor;
  }

  public Executor getExecutor() {
    return executor;
  }

  @Override
  public <DOC extends Document> ConcurrentAssessmentExecutor<DOC>
      newAssessmentExecutor(List<? extends Assessment<DOC>> assessments) {
    // The constructor will check that the arguments are valid
    return new ConcurrentAssessmentExecutor<DOC>(getExecutor(), assessments);
  }

}
