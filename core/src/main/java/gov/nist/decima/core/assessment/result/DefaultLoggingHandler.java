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

package gov.nist.decima.core.assessment.result;

import gov.nist.decima.core.requirement.DerivedRequirement;
import gov.nist.decima.core.requirement.RequirementsManager;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class DefaultLoggingHandler implements LoggingHandler {
  private final RequirementsManager requirementsManager;
  private Logger logger = LogManager.getLogger();

  public DefaultLoggingHandler(RequirementsManager requirementsManager) {
    super();
    this.requirementsManager = requirementsManager;
  }

  @Override
  public void handle(String derivedRequirementId, TestResult result) {

    Level level;
    switch (result.getStatus()) {
    case FAIL:
      level = Level.ERROR;
      break;
    case INFORMATIONAL:
      level = Level.INFO;
      break;
    case PASS:
      level = Level.DEBUG;
      break;
    case WARNING:
      level = Level.WARN;
      break;
    default:
      throw new UnsupportedOperationException(result.getStatus().toString());
    }

    DerivedRequirement req = requirementsManager.getDerivedRequirementById(derivedRequirementId);
    if (req != null) {

      List<String> values = result.getResultValues();
      String message = req.getMessageText(values.toArray(new String[values.size()]));
      if (message != null) {
        logger.log(level, "{}: {}", derivedRequirementId, message);
      }
    }
  }

}
