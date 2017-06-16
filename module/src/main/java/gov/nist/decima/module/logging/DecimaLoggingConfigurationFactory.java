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

package gov.nist.decima.module.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;

@Plugin(name = "DecimaLoggingConfigurationFactory", category = "ConfigurationFactory")
@Order(10)
public class DecimaLoggingConfigurationFactory extends ConfigurationFactory {

  /**
   * Sets the logging level of the root logger.
   * 
   * @param logLevel
   *          the new {@link Level} to use
   */
  public static void changeRootLogLevel(Level logLevel) {

    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();

    config.getRootLogger().setLevel(logLevel);
    ctx.updateLoggers();
  }

  /**
   * Valid file extensions for XML files.
   */
  public static final String[] SUFFIXES = new String[] { ".xml", "*" };

  private Level logLevel = Level.INFO;

  public Level getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(Level logLevel) {
    this.logLevel = logLevel;
  }

  @Override
  public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource source) {
    return new DecimaXmlConfiguration(loggerContext, source);
  }

  @Override
  protected String[] getSupportedTypes() {
    return SUFFIXES;
  }

  public class DecimaXmlConfiguration extends XmlConfiguration {

    /** the default serial version UID. */
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    public DecimaXmlConfiguration(LoggerContext loggerContext, ConfigurationSource configSource) {
      super(loggerContext, configSource);
    }

    @Override
    protected void doConfigure() {
      super.doConfigure();

      // final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
      LoggerConfig rootConfig = getRootLogger();
      rootConfig.setLevel(getLogLevel());
    }
  }
}
