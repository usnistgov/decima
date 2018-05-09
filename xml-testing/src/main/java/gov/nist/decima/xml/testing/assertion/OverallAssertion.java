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

package gov.nist.decima.xml.testing.assertion;

import gov.nist.decima.core.assessment.result.AssessmentResults;
import gov.nist.decima.core.assessment.result.BaseRequirementResult;
import gov.nist.decima.core.assessment.result.DerivedRequirementResult;
import gov.nist.decima.core.assessment.result.ResultStatus;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Set;

public class OverallAssertion extends AbstractRequirementGroupAssertion {

    public OverallAssertion(ResultStatus status, String quantifier, Operator operator) {
        super(status, quantifier, operator);
    }
    //
    // private ResultStatus getOpposite(ResultStatus status) {
    // ResultStatus retval;
    // if (ResultStatus.PASS.equals(status)) {
    // retval = ResultStatus.FAIL;
    // } else if (ResultStatus.FAIL.equals(status)) {
    // retval = ResultStatus.PASS;
    // } else {
    // throw new UnsupportedOperationException("Cannot invert result status:
    // "+status);
    // }
    // return retval;
    // }

    @Override
    protected Set<String> getMatchingDerivedRequirements(AssessmentResults results, ResultStatus matchingStatus,
            AssertionTracker tracker) throws AssertionException {
        FindMatchingDerivedRequirementsHandler handler
                = new FindMatchingDerivedRequirementsHandler(matchingStatus, tracker);
        ResultsWalker.getInstance().walk(results, handler);
        return handler.getRequirements();
    }

    @Override
    protected Set<DerivedRequirementResult> getInvalidDerivedRequirements(ResultStatus requiredStatus,
            AssessmentResults results, AssertionTracker tracker) throws AssertionException {
        FindNonMatchingDerivedRequirementsHandler handler
                = new FindNonMatchingDerivedRequirementsHandler(requiredStatus, tracker);
        ResultsWalker.getInstance().walk(results, handler);
        return handler.getRequirements();
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        Integer quantifier = getQuantifier();
        if (quantifier == null) {
            builder.append("quantifer", "ALL");
        } else {
            builder.append("operator", getOperator().toString());
            builder.append("quantifer", Integer.toString(quantifier));
        }
        builder.append(getResultStatus());
        return builder.build();
    }

    private static class FindMatchingDerivedRequirementsHandler
            extends AbstractFindMatchingDerivedRequirements<String> {

        public FindMatchingDerivedRequirementsHandler(ResultStatus matchingStatus, AssertionTracker tracker) {
            super(matchingStatus, tracker);
        }

        @Override
        public boolean handleBaseRequirementResult(BaseRequirementResult baseResult) throws AssertionException {
            AssertionTracker tracker = getAssertionTracker();
            tracker.assertRequirement(baseResult);
            boolean retval = super.handleBaseRequirementResult(baseResult);
            if (!retval) {
                for (DerivedRequirementResult result : baseResult.getDerivedRequirementResults()) {
                    tracker.assertRequirement(result);
                }
            }
            return retval;
        }

        @Override
        public boolean handleDerivedRequirementResult(BaseRequirementResult baseResult,
                DerivedRequirementResult derivedResult) throws AssertionException {
            getAssertionTracker().assertRequirement(derivedResult);
            return super.handleDerivedRequirementResult(baseResult, derivedResult);
        }

        @Override
        protected String handleMatchingDerivedRequirement(BaseRequirementResult baseResult,
                DerivedRequirementResult derivedResult) {
            return derivedResult.getDerivedRequirement().getId();
        }
    }

    private static class FindNonMatchingDerivedRequirementsHandler
            extends AbstractFindNonMatchingDerivedRequirementsHandler<DerivedRequirementResult> {

        public FindNonMatchingDerivedRequirementsHandler(ResultStatus matchingStatus, AssertionTracker tracker) {
            super(matchingStatus, tracker);
        }

        @Override
        public boolean handleBaseRequirementResult(BaseRequirementResult baseResult) throws AssertionException {
            AssertionTracker tracker = getAssertionTracker();
            tracker.assertRequirement(baseResult);
            boolean retval = super.handleBaseRequirementResult(baseResult);
            if (!retval) {
                for (DerivedRequirementResult result : baseResult.getDerivedRequirementResults()) {
                    tracker.assertRequirement(result);
                }
            }
            return retval;
        }

        @Override
        public boolean handleDerivedRequirementResult(BaseRequirementResult baseResult,
                DerivedRequirementResult derivedResult) throws AssertionException {
            getAssertionTracker().assertRequirement(derivedResult);
            return super.handleDerivedRequirementResult(baseResult, derivedResult);
        }

        @Override
        protected DerivedRequirementResult handleNonMatchingDerivedRequirement(BaseRequirementResult baseResult,
                DerivedRequirementResult derivedResult) {
            return derivedResult;
        }
    }
}
