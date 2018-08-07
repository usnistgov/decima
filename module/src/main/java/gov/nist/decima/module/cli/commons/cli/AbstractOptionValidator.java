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

package gov.nist.decima.module.cli.commons.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractOptionValidator implements Serializable, OptionValidator {

  /** the serial version UID. */
  private static final long serialVersionUID = 1L;

  private final Option option;

  public AbstractOptionValidator(Option option) {
    this.option = option;
  }

  @Override
  public Option getOption() {
    return option;
  }

  protected abstract boolean validateValue(String value);
  // protected abstract String getAllowedValuesMessage();

  @Override
  public boolean isValid(CommandLine cmd) {
    String[] values = cmd.getOptionValues(getOption().getOpt());

    boolean retval = true;
    for (String value : values) {
      if (!validateValue(value)) {
        retval = false;
        break;
      }
    }
    return retval;
  }

  @Override
  public List<String> getInvalidValues(CommandLine cmd) {
    String[] values = cmd.getOptionValues(getOption().getOpt());
    List<String> retval;
    if (values == null || values.length == 0) {
      retval = Collections.emptyList();
    } else {
      retval = new ArrayList<>(values.length);
      for (String value : values) {
        if (!validateValue(value)) {
          retval.add(value);
        }
      }
      if (retval.isEmpty()) {
        retval = Collections.emptyList();
      }
    }
    return retval;
  }
}
