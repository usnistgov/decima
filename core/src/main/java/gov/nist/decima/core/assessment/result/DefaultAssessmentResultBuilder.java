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

package gov.nist.decima.core.assessment.result;

import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.assessment.util.LoggingHandler;
import gov.nist.decima.core.assessment.util.NoOpLoggingHandler;
import gov.nist.decima.core.document.Document;
import gov.nist.decima.core.requirement.BaseRequirement;
import gov.nist.decima.core.requirement.DerivedRequirement;
import gov.nist.decima.core.requirement.RequirementsManager;
import gov.nist.decima.core.util.ObjectUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DefaultAssessmentResultBuilder implements AssessmentResultBuilder {
    private static final Logger log = LogManager.getLogger(DefaultAssessmentResultBuilder.class);
    private static final ResultStatusBehavior DEFAULT_RESULT_STATUS_BEHAVIOR = new DefaultResultStatusBehavior();
    private final ResultStatusBehavior resultStatusBehavior;
    private final Map<String, Document> systemIdToAssessedDocumentMap;
    private final Map<String, List<TestResult>> derivedRequirementToTestResultsMap;
    private final Map<String, TestState> derivedRequirementsTestStatusMap;
    private final Map<String, String> assessmentProperties;

    private ZonedDateTime startDateTime;
    private ZonedDateTime endDateTime;
    private LoggingHandler loggingHandler = NoOpLoggingHandler.instance();

    public DefaultAssessmentResultBuilder() {
        this(DEFAULT_RESULT_STATUS_BEHAVIOR);
    }

    /**
     * Construct a new assessment result builder using the provided result status behavior
     * Implementation.
     * 
     * @param resultStatusBehavior
     *            the behavior to use
     */
    public DefaultAssessmentResultBuilder(ResultStatusBehavior resultStatusBehavior) {
        Objects.requireNonNull(resultStatusBehavior, "resultStatusBehavior");
        this.resultStatusBehavior = resultStatusBehavior;
        this.systemIdToAssessedDocumentMap = new HashMap<>();
        this.derivedRequirementToTestResultsMap = new HashMap<>(50);
        this.derivedRequirementsTestStatusMap = new HashMap<>(50);
        this.assessmentProperties = new LinkedHashMap<>();
    }

    /**
     * Get the time for when the assessment started.
     * 
     * @return the time the assessment was started
     */
    public synchronized ZonedDateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * This can be used by child classes to manipulate the start time of the assessment.
     * 
     * @param startDateTime
     *            the new start time for the assessment
     */
    protected synchronized void setStartDateTime(ZonedDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    /**
     * Get the time for when the assessment ended.
     * 
     * @return the time the assessment ended
     */
    public synchronized ZonedDateTime getEndDateTime() {
        return endDateTime;
    }

    /**
     * This can be used by child classes to manipulate the end time of the assessment.
     * 
     * @param endDateTime
     *            the new end time for the assessment
     */
    protected synchronized void setEndDateTime(ZonedDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    @Override
    public synchronized Map<String, TestState> getTestStateByDerivedRequirementId() {
        return Collections.unmodifiableMap(derivedRequirementsTestStatusMap);
    }

    public LoggingHandler getLoggingHandler() {
        return loggingHandler;
    }

    public void setLoggingHandler(LoggingHandler loggingHandler) {
        Objects.requireNonNull(loggingHandler, "loggingHandler");
        this.loggingHandler = loggingHandler;
    }

    @Override
    public synchronized AssessmentResultBuilder start() {
        synchronized (this) {
            if (getStartDateTime() == null) {
                setStartDateTime(ZonedDateTime.now(Clock.systemDefaultZone()));
                getLoggingHandler().validationStarted();
            }
        }
        return this;
    }

    @Override
    public synchronized AssessmentResultBuilder end() {
        synchronized (this) {
            if (getStartDateTime() == null) {
                throw new IllegalStateException("The builder was not started. Please call start() first.");
            }
            if (getEndDateTime() == null) {
                setEndDateTime(ZonedDateTime.now(Clock.systemDefaultZone()));
                getLoggingHandler().validationEnded(this);
            }
        }
        return this;
    }

    @Override
    public AssessmentResultBuilder addAssessmentTarget(Document document) {
        String systemId = document.getSystemId();
        if (!systemIdToAssessedDocumentMap.containsKey(systemId)) {
            systemIdToAssessedDocumentMap.put(systemId, document);
        } else {
            Document other = systemIdToAssessedDocumentMap.get(systemId);
            if (!other.equals(document)) {
                if (log.isDebugEnabled()) {
                    log.debug("Duplicate systemId {} found for documents {} and {}", systemId, document.toString(),
                            other.toString());
                }
            }
        }
        return this;
    }

    @Override
    public <DOC extends Document> AssessmentResultBuilder addTestResult(Assessment<? extends DOC> assessment,
            DOC document, String derivedRequirementId, TestResult result) {
        ObjectUtil.requireNonEmpty(derivedRequirementId);
        Objects.requireNonNull(result);

        synchronized (this) {
            start();
            List<TestResult> resultList = derivedRequirementToTestResultsMap.get(derivedRequirementId);
            if (resultList == null) {
                resultList = new LinkedList<>();
                derivedRequirementToTestResultsMap.put(derivedRequirementId, resultList);
            }
            resultList.add(result);
            assignTestStatus(assessment, document, derivedRequirementId, TestState.TESTED);
        }

        LoggingHandler loggingHandler = getLoggingHandler();
        if (loggingHandler != null) {
            loggingHandler.addTestResult(assessment, document, derivedRequirementId, result);
        }
        return this;
    }

    @Override
    public AssessmentResults build(RequirementsManager requirementsManager) {
        getLoggingHandler().producingResults(this, requirementsManager);

        log.info("Compiling assessment results");
        DefaultAssessmentResults retval;

        synchronized (this) {
            if (startDateTime == null) {
                throw new IllegalStateException("The builder was not started. Please call start() first.");
            }

            if (endDateTime == null) {
                throw new IllegalStateException("The builder has not been stopped. Please call end() first.");
            }

            retval = new DefaultAssessmentResults(requirementsManager, getStartDateTime(), getEndDateTime());

            for (Map.Entry<String, String> entry : assessmentProperties.entrySet()) {
                retval.setProperty(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, Document> entry : systemIdToAssessedDocumentMap.entrySet()) {
                retval.addAssessmentSubject(entry.getValue());
            }
            for (BaseRequirement base : requirementsManager.getBaseRequirements()) {
                boolean inScope = resultStatusBehavior.isInScope(base);
                DefaultBaseRequirementResult baseResult;
                if (inScope) {
                    baseResult = buildBaseRequirementResult(base, retval);
                } else {
                    baseResult = new DefaultBaseRequirementResult(base, ResultStatus.NOT_IN_SCOPE);
                    for (DerivedRequirement derived : base.getDerivedRequirements()) {
                        baseResult.addDerivedRequirementResult(
                                new DefaultDerivedRequirementResult(derived, ResultStatus.NOT_IN_SCOPE));
                    }
                }
                retval.addValidationResult(baseResult);
            }
        }

        getLoggingHandler().completedResults(this, requirementsManager, retval);
        return retval;
    }

    private DefaultBaseRequirementResult buildBaseRequirementResult(BaseRequirement base,
            DefaultAssessmentResults results) {
        DefaultBaseRequirementResult retval;

        Collection<DerivedRequirement> derivedRequirements = base.getDerivedRequirements();
        retval = new DefaultBaseRequirementResult(base, ResultStatus.NOT_TESTED);
        if (!derivedRequirements.isEmpty()) {
            for (DerivedRequirement derived : base.getDerivedRequirements()) {
                DefaultDerivedRequirementResult result = buildDerivedRequirementResult(derived, results);
                retval.addDerivedRequirementResult(result);
            }
        }
        return retval;
    }

    private DefaultDerivedRequirementResult buildDerivedRequirementResult(DerivedRequirement derived,
            DefaultAssessmentResults results) {

        DefaultDerivedRequirementResult derivedResult;

        boolean inScope = resultStatusBehavior.isInScope(derived);
        if (!inScope) {
            derivedResult = new DefaultDerivedRequirementResult(derived, ResultStatus.NOT_IN_SCOPE);
        } else {
            derivedResult = new DefaultDerivedRequirementResult(derived, ResultStatus.NOT_TESTED);

            // Build from the ground up
            // First add assertion results to the derived result
            List<TestResult> assertionResults = getAssertionResultsByDerivedRequirementId(derived.getId());
            if (!assertionResults.isEmpty()) {
                derivedResult.addTestResults(assertionResults);
            } else {
                // No test results means that all the tests passed, the test was
                // not applicable, or the test was not implemented
                TestState testStatus = derivedRequirementsTestStatusMap.get(derived.getId());
                if (testStatus == null) {
                    testStatus = TestState.NOT_TESTED;
                }

                switch (testStatus) {
                case NOT_APPLICABLE:
                    derivedResult.setStatus(ResultStatus.NOT_APPLICABLE);
                    break;
                case TESTED:
                    // the test was implemented and was tested
                    if (Severity.INFO.equals(derived.getType().getSeverity())) {
                        derivedResult.setStatus(ResultStatus.INFORMATIONAL);
                        // } else {
                        // derivedResult.setStatus(ResultStatus.PASS);
                    } else {
                        derivedResult.setStatus(ResultStatus.PASS);
                    }
                    break;
                case NOT_TESTED:
                    // do nothing;
                    break;
                default:
                    throw new UnsupportedOperationException(testStatus.toString());
                }
            }
        }
        return derivedResult;
    }

    @Override
    public <DOC extends Document> AssessmentResultBuilder assignTestStatus(Assessment<? extends DOC> assessment,
            DOC document, String derivedRequirementId, TestState state) {
        ObjectUtil.requireNonEmpty(derivedRequirementId, "derivedRequirementId");
        Objects.requireNonNull(state, "state");

        synchronized (this) {
            start();
            TestState oldStatus = derivedRequirementsTestStatusMap.get(derivedRequirementId);
            if (oldStatus == null || oldStatus.ordinal() < state.ordinal()) {
                derivedRequirementsTestStatusMap.put(derivedRequirementId, state);
            }
        }

        LoggingHandler loggingHandler = getLoggingHandler();
        if (loggingHandler != null) {
            loggingHandler.assignTestStatus(assessment, document, derivedRequirementId, state);
        }
        return this;
    }

    /**
     * Allows the retrieval of all reported test results for a given derived requirement.
     * 
     * @param derivedRequirementId
     *            the derived requirement to retrieve test results for
     * @return a non-null list of test results
     */
    public List<TestResult> getAssertionResultsByDerivedRequirementId(String derivedRequirementId) {
        ObjectUtil.requireNonEmpty(derivedRequirementId);

        List<TestResult> retval;

        synchronized (this) {
            retval = derivedRequirementToTestResultsMap.get(derivedRequirementId);
        }

        if (retval == null) {
            retval = Collections.emptyList();
        } else {
            retval = Collections.unmodifiableList(retval);
        }
        return retval;
    }

    /**
     * Allows arbitrary assessment properties to be associated with the results. This can be used to
     * include meta information in the assessment results produced.
     * 
     * @param key
     *            the property key
     * @param value
     *            the property value
     * @return this builder
     */
    public AssessmentResultBuilder assignProperty(String key, String value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        this.assessmentProperties.put(key, value);
        return this;
    }
}
