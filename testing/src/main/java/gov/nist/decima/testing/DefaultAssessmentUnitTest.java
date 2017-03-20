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

package gov.nist.decima.testing;

import gov.nist.decima.core.Decima;
import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.assessment.AssessmentException;
import gov.nist.decima.core.assessment.AssessmentExecutor;
import gov.nist.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.decima.core.assessment.result.AssessmentResults;
import gov.nist.decima.core.document.DocumentException;
import gov.nist.decima.core.document.post.template.TemplateProcessor;
import gov.nist.decima.core.requirement.RequirementsManager;
import gov.nist.decima.testing.assertion.Assertion;
import gov.nist.decima.testing.assertion.AssertionException;
import gov.nist.decima.testing.assertion.AssertionTracker;
import gov.nist.decima.xml.assessment.result.XMLResultBuilder;
import gov.nist.decima.xml.document.XMLDocument;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class DefaultAssessmentUnitTest extends UnitTestSupport implements AssessmentUnitTest {
  private static final Logger log = LogManager.getLogger(DefaultAssessmentUnitTest.class);

  private Description description;
  private Map<Object, DescriptionAdapter<?>> descriptions = new HashMap<>();
  private RequirementsManager requirementsManager;

  public DefaultAssessmentUnitTest(String name, String sourceURI, File resultDir) {
    super(name, sourceURI, resultDir);
  }

  public RequirementsManager getRequirementsManager() {
    return requirementsManager;
  }

  public void setRequirementsManager(RequirementsManager requirementsManager) {
    Objects.requireNonNull(requirementsManager);
    this.requirementsManager = requirementsManager;
  }

  @Override
  public Description getDescription() {
    if (description == null) {
      String name = getName() + "[" + getSourceURI() + "]";

      description = Description.createSuiteDescription(name);
      TemplateProcessor processor = getTemplateProcessor();
      DescriptionAdapter<?> adapter = new TemplateProcessorDescriptionAdapter(processor);
      descriptions.put(processor, adapter);
      description.addChild(adapter.getDescription());

      for (Assessment<XMLDocument> assessment : getAssessments()) {
        adapter = new AssessmentDescriptionAdapter<XMLDocument>(assessment);
        descriptions.put(assessment, adapter);
        description.addChild(adapter.getDescription());
      }

      for (Assertion assertion : getAssertions()) {
        adapter = new AssertionDescriptionAdapter(assertion);
        descriptions.put(assertion, adapter);
        description.addChild(adapter.getDescription());
      }
    }
    return description;
  }

  protected XMLDocument processTemplate(RunNotifier notifier) throws DocumentException {
    TemplateProcessor tp = getTemplateProcessor();

    Description templateDescription = descriptions.get(tp).getDescription();
    notifier.fireTestStarted(templateDescription);

    XMLDocument doc;
    try {
      doc = super.processTemplate();
    } catch (DocumentException e) {
      notifier.fireTestFailure(new Failure(templateDescription, e));
      throw e;
    } catch (Throwable e) {
      notifier.fireTestFailure(new Failure(templateDescription, e));
      throw e;
    } finally {
      notifier.fireTestFinished(templateDescription);
    }
    return doc;
  }

  private void processAssertions(AssessmentResults assessmentResults, XMLDocument doc, RunNotifier notifier) {
    AssertionTracker tracker = new AssertionTracker();
    for (Assertion assertion : getAssertions()) {
      Description assertionDescription = descriptions.get(assertion).getDescription();
      notifier.fireTestStarted(assertionDescription);
      try {
        if (log.isDebugEnabled()) {
          log.debug("Evaluating assertion: {}", assertion.toString());
        }
        assertion.evaluate(doc, assessmentResults, tracker);
        if (log.isDebugEnabled()) {
          log.debug("Successful assertion: {}", assertion.toString());
        }
      } catch (AssertionError | AssertionException e) {
        log.error("An error occured while processing the assertion: " + assertion.toString(), e);
        notifier.fireTestFailure(new Failure(assertionDescription, e));
      } catch (Throwable e) {
        log.error("An unexpected error occured while processing the assertion: " + assertion.toString(), e);
        notifier.fireTestFailure(new Failure(assertionDescription, e));
      }
      notifier.fireTestFinished(assertionDescription);
    }
  }

  @Override
  public void execute(RunNotifier runNotifier) throws InitializationError {
    log.info("Executing unit test: " + getSourceURI());

    // initialize
    getDescription();
    XMLDocument doc;
    try {
      doc = processTemplate(runNotifier);
      List<? extends Assessment<XMLDocument>> assessments = getAssessments();
      AssessmentExecutor<XMLDocument> executor
          = Decima.newAssessmentExecutorFactory().newAssessmentExecutor(assessments);
      AssessmentResults assessmentResults = processAssessments(doc, executor, runNotifier);
      processAssertions(assessmentResults, doc, runNotifier);

      XMLResultBuilder writer = new XMLResultBuilder();
      File resultFile = new File(getResultDir(), "result.xml");
      BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(resultFile));
      writer.write(assessmentResults, os);
      os.close();
    } catch (Throwable e) {
      log.error("An error occured while executing unit test: " + getSourceURI(), e);
      throw new InitializationError(e);
    }
    log.debug("Completed executing unit test: " + getSourceURI());
  }

  protected AssessmentResults processAssessments(XMLDocument doc, AssessmentExecutor<XMLDocument> executor,
      RunNotifier notifier) throws AssessmentException {
    AssessmentResultBuilder builder = Decima.newAssessmentResultBuilder();
    builder.setLoggingHandler(new AssessmentRunNotifierDecorator(notifier, new DescriptionHandler()));
    executor.execute(doc, builder);

    builder.end();
    RequirementsManager requirementsManager;
    if (getRequirementsManager() != null) {
      requirementsManager = getRequirementsManager();
    } else {
      requirementsManager = new StubRequirementsManager(builder.getTestStateByDerivedRequirementId().keySet());
    }

    return builder.build(requirementsManager);
  }

  private class DescriptionHandler implements DescriptionResolver {

    @Override
    public Description getDescription(Assessment<?> assessment) {
      return descriptions.get(assessment).getDescription();
    }

  }
}
