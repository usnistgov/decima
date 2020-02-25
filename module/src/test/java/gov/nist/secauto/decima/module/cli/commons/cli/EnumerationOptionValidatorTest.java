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
package gov.nist.secauto.decima.module.cli.commons.cli;

import gov.nist.secauto.decima.module.cli.commons.cli.OptionEnumerationValidator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EnumerationOptionValidatorTest {
  private static String OPTION_NAME = "test";
  private static String OPTION_VALUE = "value";
  private static String OPTION_VALUE1 = "value1";
  private static String OPTION_VALUE2 = "value2";

  @Test
  public void testValidValue() throws ParseException {
    Option option = Option.builder(OPTION_NAME).desc("description").hasArg().required().build();
    Set<String> validValues = new HashSet<>();
    validValues.add(OPTION_VALUE1);
    validValues.add(OPTION_VALUE2);
    OptionEnumerationValidator validator = new OptionEnumerationValidator(option, validValues);

    Options options = new Options().addOption(option);
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, new String[] { "-" + OPTION_NAME, OPTION_VALUE1 });

    Assert.assertTrue(cmd.hasOption(OPTION_NAME));
    Assert.assertEquals(OPTION_VALUE1, cmd.getOptionValue(OPTION_NAME));
    Assert.assertTrue(validator.isValid(cmd));
  }

  @Test
  public void testInvalidValue() throws ParseException {
    Option option = Option.builder(OPTION_NAME).desc("description").hasArg().required().build();
    Set<String> validValues = new HashSet<>();
    validValues.add(OPTION_VALUE1);
    validValues.add(OPTION_VALUE2);
    OptionEnumerationValidator validator = new OptionEnumerationValidator(option, validValues);

    Options options = new Options().addOption(option);
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, new String[] { "-" + OPTION_NAME, OPTION_VALUE });

    Assert.assertTrue(cmd.hasOption(OPTION_NAME));
    Assert.assertEquals(OPTION_VALUE, cmd.getOptionValue(OPTION_NAME));
    Assert.assertFalse(validator.isValid(cmd));
    Assert.assertEquals(Collections.singletonList(OPTION_VALUE), validator.getInvalidValues(cmd));
    Assert.assertEquals("Allowed values must be one of: \"" + OPTION_VALUE1 + "\", \"" + OPTION_VALUE2 + "\".",
        validator.getAllowedValuesMessage());
  }

  @Test
  public void testValidValues() throws ParseException {
    Option option = Option.builder(OPTION_NAME).desc("description").hasArgs().required().build();
    Set<String> validValues = new HashSet<>();
    validValues.add(OPTION_VALUE1);
    validValues.add(OPTION_VALUE2);
    OptionEnumerationValidator validator = new OptionEnumerationValidator(option, validValues);

    Options options = new Options().addOption(option);
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, new String[] { "-" + OPTION_NAME, OPTION_VALUE1, OPTION_VALUE2 });

    Assert.assertTrue(cmd.hasOption(OPTION_NAME));
    Assert.assertEquals(OPTION_VALUE1, cmd.getOptionValues(OPTION_NAME)[0]);
    Assert.assertEquals(OPTION_VALUE2, cmd.getOptionValues(OPTION_NAME)[1]);
    Assert.assertTrue(validator.isValid(cmd));
  }

  @Test
  public void testInvalidValues() throws ParseException {
    Option option = Option.builder(OPTION_NAME).desc("description").hasArg().required().build();
    Set<String> validValues = new HashSet<>();
    validValues.add(OPTION_VALUE1);
    validValues.add(OPTION_VALUE2);
    OptionEnumerationValidator validator = new OptionEnumerationValidator(option, validValues);

    Options options = new Options().addOption(option);
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd
        = parser.parse(options, new String[] { "-" + OPTION_NAME, OPTION_VALUE1, "-" + OPTION_NAME, OPTION_VALUE });

    Assert.assertTrue(cmd.hasOption(OPTION_NAME));
    Assert.assertEquals(OPTION_VALUE1, cmd.getOptionValues(OPTION_NAME)[0]);
    Assert.assertEquals(OPTION_VALUE, cmd.getOptionValues(OPTION_NAME)[1]);
    Assert.assertFalse(validator.isValid(cmd));
    Assert.assertEquals("The list of invalid values did not match.", Collections.singletonList(OPTION_VALUE),
        validator.getInvalidValues(cmd));
  }
}
