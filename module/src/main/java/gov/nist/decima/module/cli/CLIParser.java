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

package gov.nist.decima.module.cli;

import gov.nist.decima.module.cli.commons.cli.OptionValidator;
import gov.nist.decima.module.logging.DecimaLoggingConfigurationFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CLIParser {
  // private static final Logger log = LogManager.getLogger(CLIParser.class);
  private static final String OPTION_DEBUG = "debug";
  private static final String OPTION_QUIET = "quiet";
  private static final String OPTION_VERSION = "version";
  private static final String OPTION_HELP = "h";

  public static final String OPTION_VALIDATION_RESULT_FILE = "valresultfile";
  public static final String OPTION_VALIDATION_REPORT_FILE = "valreportfile";
  public static final String DEFAULT_VALIDATION_RESULT_FILE = "validation-result.xml";
  public static final String DEFAULT_VALIDATION_REPORT_FILE = "validation-report.html";

  private final Map<Option, OptionValidator> optionValidatorMap = new HashMap<>();
  private final Options options = new Options();
  private final String cmdLineSyntax;

  private String version;

  public CLIParser(String cmdLineSyntax) {
    this.cmdLineSyntax = cmdLineSyntax;
    initializeStandardOptions();
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    Objects.requireNonNull(version, "version");
    this.version = version;
  }

  protected void initializeStandardOptions() {
    Option debugOption = Option.builder(OPTION_DEBUG).desc("Enable verbose output").build();

    Option quietOption = Option.builder(OPTION_QUIET).desc("Silence console output").build();

    OptionGroup outputControlGroup = new OptionGroup().addOption(debugOption).addOption(quietOption);

    Option resultFile = Option.builder(OPTION_VALIDATION_RESULT_FILE)
        .desc("The validation result file location (default: " + DEFAULT_VALIDATION_RESULT_FILE + ")").hasArg()
        .argName("FILE").build();

    Option reportFile = Option.builder(OPTION_VALIDATION_REPORT_FILE)
        .desc("The validation HTML report file location (default: " + DEFAULT_VALIDATION_REPORT_FILE + ")").hasArg()
        .argName("FILE").build();

    Option versionOption = Option.builder(OPTION_VERSION).desc("Display the version of the tool").build();

    Option helpOption = Option.builder(OPTION_HELP).longOpt("help").desc("Display the available cli arguments").build();

    getOptions().addOptionGroup(outputControlGroup).addOption(resultFile).addOption(reportFile).addOption(versionOption)
        .addOption(helpOption);
  }

  protected Options getOptions() {
    return options;
  }

  /**
   * Adds an CLI option with a coupled {@link OptionValidator}.
   * 
   * @param validator
   *          the option validator related to the option to add
   * @return the current class instance
   */
  public CLIParser addOption(OptionValidator validator) {
    Option option = validator.getOption();
    getOptions().addOption(option);
    optionValidatorMap.put(option, validator);
    return this;
  }

  public CLIParser addOption(Option option) {
    getOptions().addOption(option);
    return this;
  }

  public CLIParser addOptionGroup(OptionGroup group) {
    getOptions().addOptionGroup(group);
    return this;
  }

  /**
   * Parses the command line options provided by a main(String) method.
   * 
   * @param arguments
   *          the arguments to parse
   * @return a {@link CommandLine} object representing the parsed command line arguments
   * @throws ParseException
   *           if an error occurred while parsing CLI arguments
   */
  public CommandLine parse(String[] arguments) throws ParseException {
    CommandLineParser parser = new DefaultParser();

    ParseException parseException = null;
    CommandLine cmd;
    try {
      cmd = parser.parse(options, arguments);
    } catch (ParseException e) {
      parseException = e;
      cmd = null;
    }

    if (cmd != null && !validateOptions(cmd)) {
      cmd = null;
    }

    CommandLine retval;
    if (cmd == null || cmd.hasOption(OPTION_HELP)) {
      doShowHelp();
      if (cmd == null) {
        if (parseException != null) {
          throw parseException;
        }
        // I've seen parseException null here if user provided options don't match
        // .addAllowedValue()
        throw new ParseException("There was a problem with the command line input.");
      } else {
        retval = null;
      }
    } else if (cmd.hasOption(OPTION_VERSION)) {
      retval = doShowVersion(cmd);
    } else {
      retval = cmd;
      if (cmd.hasOption(OPTION_DEBUG)) {
        DecimaLoggingConfigurationFactory.changeRootLogLevel(Level.DEBUG);
      } else if (cmd.hasOption(OPTION_QUIET)) {
        DecimaLoggingConfigurationFactory.changeRootLogLevel(Level.FATAL);
      }
    }

    return retval;
  }

  protected CommandLine doShowVersion(CommandLine cmd) {
    String version = getVersion();
    if (version == null) {
      version = "unknown";
    }
    System.out.println("Version " + version);
    return null;
  }

  public void doShowHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(cmdLineSyntax, options);
  }

  private boolean validateOptions(CommandLine cmdline) {
    boolean retval = true;
    for (OptionValidator validator : optionValidatorMap.values()) {
      Option option = validator.getOption();
      if (cmdline.hasOption(option.getOpt()) && !validator.isValid(cmdline)) {
        System.err.println("Option '" + option.getOpt() + "' has invalid value(s): "
            + validator.getInvalidValues(cmdline) + ". " + validator.getAllowedValuesMessage());
        retval = false;
      }
    }
    return retval;
  }
  //
  // public static void main(String[] args) {
  // CLIParser config = new CLIParser("decima");
  // CommandLine cmd = null;
  // try {
  // cmd = config.parse(args);
  // } catch (ParseException e) {
  // System.err.println(e.getLocalizedMessage());
  // System.exit(-1);
  // }
  //
  // if (cmd != null) {
  // for (Option option : cmd.getOptions()) {
  // System.out.println(option.getOpt() + ": " + option.getValuesList());
  // }
  // } else {
  // System.exit(1);
  // }
  // }
}
