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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

public class ConcurrentAssessmentExecutor<DOC extends Document> extends AbstractAssessmentExecutor<DOC> {
    private static final Logger log = LogManager.getLogger(ConcurrentAssessmentExecutor.class);
    private final Executor executor;

    /**
     * Constructs a new AssessmentExecutor that is capable of executing multiple assessments
     * Concurrently, using the provided Executor to execute the provided assessments.
     * 
     * @param executor
     *            the executor to use to execute the assessment tasks
     * @param assessments
     *            the assessments to perform
     */
    public ConcurrentAssessmentExecutor(Executor executor, List<? extends Assessment<DOC>> assessments) {
        super(assessments);
        Objects.requireNonNull(executor, "executor");
        this.executor = executor;
    }

    public Executor getExecutor() {
        return executor;
    }

    @Override
    protected final void executeInternal(DOC targetDocument, AssessmentResultBuilder builder)
            throws AssessmentException {
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);
        Set<Future<Void>> futures = new HashSet<>();
        for (Assessment<DOC> assessment : getExecutableAssessments(targetDocument)) {
            log.info("Submitting assessment for execution: " + assessment.getName(true));
            futures.add(completionService.submit(new AssessmentTask(assessment, targetDocument, builder)));
        }

        try {
            while (!futures.isEmpty()) {
                Future<Void> future = completionService.take();
                futures.remove(future);
                future.get();
            }
        } catch (InterruptedException e) {
            throw new AssessmentException("the assessment execution was interrupted", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof AssessmentException) {
                throw (AssessmentException) e.getCause();
            }
        } finally {
            for (Future<Void> future : futures) {
                future.cancel(true);
            }
        }
    }

    private class AssessmentTask implements Callable<Void> {
        private final Assessment<DOC> assessment;
        private final DOC documentToAssess;
        private final AssessmentResultBuilder builder;

        public AssessmentTask(Assessment<DOC> assessment, DOC documentToAssess, AssessmentResultBuilder builder) {
            Objects.requireNonNull(assessment, "assessment");
            Objects.requireNonNull(documentToAssess, "documentToAssess");
            Objects.requireNonNull(builder, "builder");
            this.assessment = assessment;
            this.documentToAssess = documentToAssess;
            this.builder = builder;
        }

        @Override
        public Void call() throws AssessmentException {
            AssessmentExecutionHelper.executeAssessment(assessment, documentToAssess, builder);
            return null;
        }

    }

}
