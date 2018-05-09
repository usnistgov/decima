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

/**
 * An enumeration of the possible values for a {@link TestResult}, a
 * {@link DerivedRequirementResult}, and a {@link BaseRequirementResult} indicating the result of a
 * test, a collection of test results for a derived requirement, or a collection of derived
 * requirements for a base requirement respectively.
 *
 */
public enum ResultStatus {
    // IMPORTANT NOTE: Do not change the order of these values, since the
    // ordinal is used in some comparison calculations
    /**
     * The requirement was determined to not be in the assessment scope; thus, it was not evaluated.
     */
    NOT_IN_SCOPE,
    /**
     * The requirement is in the assessment scope, but it does not have a test implementation.
     */
    NOT_TESTED,
    /**
     * The requirement is implemented, but was not evaluated because the requirement's pre-condition
     * was not met.
     */
    NOT_APPLICABLE,
    /**
     * The resulting test or requirement was evaluated, but produced an informative result that does
     * not indicate success or failure.
     */
    INFORMATIONAL,
    /**
     * The resulting test or requirement evaluation was successful.
     */
    PASS,
    /**
     * The resulting test or requirement evaluation was unsuccessful and resulted in a warning.
     */
    WARNING,
    /**
     * The resulting test or requirement evaluation was unsuccessful and resulted in an error.
     */
    FAIL;
}
