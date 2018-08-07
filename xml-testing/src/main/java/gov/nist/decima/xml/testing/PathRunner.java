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

package gov.nist.decima.xml.testing;

import gov.nist.decima.core.requirement.DefaultRequirementsManager;
import gov.nist.decima.core.requirement.RequirementsManager;
import gov.nist.decima.core.requirement.RequirementsParserException;
import gov.nist.decima.xml.requirement.XMLRequirementsParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

public class PathRunner extends Runner implements UnitTestFileHandler {
  /**
   * Annotation for the runner class to provide paths to be injected into the runner.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public static @interface Paths {
    /**
     * A filesystem path. May be relative to the project base directory.
     * 
     * @return an array of filesystem paths
     */
    String[] value();
  }

  /**
   * Annotation for the runner class to provide paths to be injected into the runner.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public static @interface Requirements {
    /**
     * One or more paths to Decima requirement XML definition files to use. May be a classpath resource
     * using the prefix "classpath:"
     * 
     * @return an array of paths to Decima requirements definition files
     */
    String[] value();

    /**
     * Extension schema to use to validate any loaded Decima requirement XML files. May be a classpath
     * resource using the prefix "classpath:"
     * 
     * @return an array of paths to schema extensions
     */
    String[] extensions() default "";
  }

  private static final Logger log = LogManager.getLogger(PathRunner.class);

  private final List<AssessmentUnitTest> tests = new LinkedList<>();
  private final File resultDir;
  private final AssessmentUnitTestParser parser = AssessmentUnitTestParser.getInstance();
  private final RequirementsManager requirementsManager;

  /**
   * Constructs a new Path-based Decima JUnit test runner.
   * 
   * @param testClass
   *          a class instance used to retrieve annotations
   * @throws InitializationError
   *           if an error occurred setting up the referenced unit tests
   */
  public PathRunner(Class<?> testClass) throws InitializationError {
    List<File> paths = handlePaths(testClass);
    this.resultDir = initializeResultDir();
    try {
      requirementsManager = handleRequirements(testClass);
    } catch (MalformedURLException | RequirementsParserException | URISyntaxException | JDOMException
        | SAXException e) {
      throw new InitializationError(e);
    }
    handlePaths(paths);
  }

  private static RequirementsManager handleRequirements(Class<?> clazz)
      throws MalformedURLException, RequirementsParserException, URISyntaxException, JDOMException, SAXException {
    DefaultRequirementsManager retval = null;

    if (clazz.isAnnotationPresent(Requirements.class)) {
      Requirements reqs = clazz.getAnnotation(Requirements.class);
      String[] requirementDefs = reqs.value();
      retval = new DefaultRequirementsManager();

      // Get extension schemas
      List<Source> extension = getExtensions(reqs.extensions());

      XMLRequirementsParser parser = new XMLRequirementsParser(extension);
      for (String path : requirementDefs) {
        parser.parse(new URL(path), retval);
      }
    }
    return retval;
  }

  private static List<Source> getExtensions(String[] extensions) {
    List<Source> retval;
    if (extensions.length == 0 || extensions[0].isEmpty()) {
      retval = Collections.emptyList();
    } else {
      retval = new ArrayList<>(extensions.length);
      for (String path : extensions) {
        retval.add(new StreamSource(path));
      }
    }
    return retval;
  }

  private static List<File> handlePaths(Class<?> clazz) throws InitializationError {
    List<File> retval = null;
    if (clazz.isAnnotationPresent(Paths.class)) {
      String[] paths = clazz.getAnnotation(Paths.class).value();
      retval = new ArrayList<>(paths.length);
      for (String file : paths) {
        retval.add(new File(file));
      }
    } else {
      Method pathMethod = null;
      retval = null;
      try {
        pathMethod = clazz.getMethod("paths");
        if (!Modifier.isStatic(pathMethod.getModifiers())) {
          throw new InitializationError(clazz.getName() + ".paths() must be static method");
        }

        // invoke the static method
        @SuppressWarnings("unchecked")
        List<File> obj = (List<File>) pathMethod.invoke(null);
        retval = obj;
      } catch (InvocationTargetException e) {
        throw new InitializationError(e.getCause());
      } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
        throw new InitializationError(e);
      }
    }
    return retval;
  }

  private void handlePaths(List<File> paths) throws InitializationError {

    for (File unitTestDir : paths) {
      URI unitTestDirURI = unitTestDir.toURI();

      if (unitTestDir.isDirectory()) {

        try {
          Files.walkFileTree(unitTestDir.toPath(), new DirectoryVisitor(unitTestDirURI, this));
        } catch (IOException e) {
          throw new InitializationError(e);
        }
      } else if (unitTestDir.isFile()) {
        try {
          handleUnitTestFile(unitTestDir.toPath(), unitTestDirURI);
        } catch (IOException e) {
          throw new InitializationError(e);
        }
      } else {
        throw new InitializationError(unitTestDir.getAbsolutePath() + " is not an available directory or file");
      }

    }
  }

  public File getResultDirectory() {
    return resultDir;
  }

  public AssessmentUnitTestParser getParser() {
    return parser;
  }

  @Override
  public void handleUnitTestFile(Path file, URI unitTestDirURI) throws IOException {
    URI pathURI = file.toUri();
    URI relative = unitTestDirURI.relativize(pathURI);
    File unitTestResultDir = new File(getResultDirectory(), relative.getPath());
    String path = file.toFile().getPath();
    // if (!path.contains("scap-1.2\\GENERAL\\requirement-370-scenario-1"))
    // {
    // return FileVisitResult.CONTINUE;
    // }
    // if (!path.contains("GEN-3-1-authoritative-incomplete-entity.xml")) {
    // return FileVisitResult.CONTINUE;
    // }
    // path = path.replace("src\\test\\resources", "classpath:");
    try {
      parser.setResultDirectory(unitTestResultDir);
      DefaultAssessmentUnitTest unitTest = parser.parse(pathURI.toURL());
      if (requirementsManager != null) {
        unitTest.setRequirementsManager(requirementsManager);
      }
      tests.add(unitTest);
    } catch (ParserException e) {
      String msg = "Unable to parse unit test XML: " + path;
      log.error(msg, e);
      log.error("Ignoring unit test: " + path);
      throw new IOException(msg, e);
      // throw new IOException(msg, e);
    } catch (MalformedURLException e) {
      String msg = "Unable to get URL for unit test XML: " + path;
      log.error(msg, e);
      log.error("Ignoring unit test: " + path);
    }

  }

  private static File initializeResultDir() throws InitializationError {
    File resultDir = new File("test-results").getAbsoluteFile();
    if (resultDir.exists()) {
      if (!resultDir.isDirectory()) {
        throw new InitializationError("Result dir is not a directory: " + resultDir.getAbsolutePath());
      } else {
        // Walk all sub directories in reverse order and delete them
        try {
          for (Path p : Files.walk(resultDir.toPath()).sorted((first, second) -> second.compareTo(first))
              .toArray(Path[]::new)) {
            try {
              Files.delete(p);
            } catch (IOException e) {
              throw new InitializationError(e);
            }
          }
        } catch (IOException e) {
          throw new InitializationError(e);
        }
      }
    }

    if (!resultDir.mkdirs()) {
      // didn't exist and cannot create it
      throw new InitializationError("Unable to create result dir: " + resultDir.getAbsolutePath());
    }
    return resultDir;
  }

  @Override
  public Description getDescription() {
    Description suite = Description.createSuiteDescription("Decima Tests");
    for (AssessmentUnitTest child : tests) {
      suite.addChild(child.getDescription());
    }
    return suite;
  }

  @Override
  public void run(RunNotifier notifier) {

    for (AssessmentUnitTest child : tests) {
      try {
        notifier.fireTestStarted(child.getDescription());
        child.execute(notifier);
      } catch (InitializationError e) {
        notifier.fireTestFailure(new Failure(child.getDescription(), e));
      } finally {
        notifier.fireTestFinished(child.getDescription());
      }
    }
  }

  private static class DirectoryVisitor extends SimpleFileVisitor<Path> {
    private final URI unitTestDirURI;
    private final UnitTestFileHandler handler;

    public DirectoryVisitor(URI unitTestDirURI, UnitTestFileHandler handler) {
      this.unitTestDirURI = unitTestDirURI;
      this.handler = handler;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      handler.handleUnitTestFile(file, unitTestDirURI);
      return FileVisitResult.CONTINUE;
    }

  }
}
