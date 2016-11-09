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

package gov.nist.decima.core.assessment;

import gov.nist.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.decima.core.assessment.result.AssessmentResults;
import gov.nist.decima.core.document.Document;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Provides basic functions common to all {@link Assessment} implementations. A concrete
 * implementations of this class must provide implementations of the
 * {@link #executeInternal(Document, AssessmentResultBuilder)} and {@link #getNameDetails()} methods
 * to provide evaluation logic and descriptive label information respectively. Implementations of
 * this class are expected to be thread safe when the
 * {@link #executeInternal(Document, AssessmentResultBuilder)} method is invoked from different
 * calling contexts.
 */
public abstract class AbstractAssessment<DOC extends Document> implements Assessment<DOC> {
  private static Integer NEXT_ID = 0;
  private final int id;
  private File resultDirectory;

  protected static int getNextId() {
    int retval;
    synchronized (NEXT_ID) {
      retval = NEXT_ID++;
    }
    return retval;
  }

  public AbstractAssessment() {
    super();
    this.id = getNextId();
  }

  public int getId() {
    return id;
  }

  /**
   * Retrieves the result directory set using the {@link #setResultDirectory(File)} method.
   * 
   * @return the set result directory or {@code null} otherwise
   */
  public synchronized File getResultDirectory() {
    return resultDirectory;
  }

  /**
   * Sets an output directory to write assessment artifacts to. An assessment is not required to
   * create (and write) assessment artifacts. If there is a need to write artifacts and a directory
   * is provided, the assessment must write these artifacts under the provided directory. Calls to
   * {@link File#createTempFile(String, String, File)} (or an equivalent method) may be used if the
   * last argument is passed using the provided directory. If no directory is provided using this
   * method, then the assessment may create temporary files instead by calling
   * {@link File#createTempFile(String, String)} or an equivalent method.
   * 
   * @param resultDirectory
   *          if not {@code null}, the directory to write assessment artifacts to
   */
  public synchronized void setResultDirectory(File resultDirectory) {
    this.resultDirectory = resultDirectory;
  }

  @Override
  public final List<Assessment<DOC>> getExecutableAssessments(DOC document) {
    return Collections.singletonList(this);
  }

  /**
   * If a result directory has been provided by calling {@link #setResultDirectory(File)}, this
   * method ensures that that directory exists before calling
   * {@link #executeInternal(XMLDocument, AssessmentResultBuilder)}.
   */
  @Override
  public void execute(DOC document, AssessmentResultBuilder builder)
      throws AssessmentException {
    File resultDir = getResultDirectory();
    if (resultDir != null && !resultDir.exists() && !resultDir.mkdirs()) {
      throw new AssessmentException("Unable to create result directory: " + resultDir);
    }

    try {
      executeInternal(document, builder);
    } catch (AssessmentException e) {
      throw e;
    } catch (Throwable e) {
      throw new AssessmentException("An unknown error occured while executing the assessment", e);
    }

  }

  /**
   * Implementations of this method will conduct an assessment over the provided target document,
   * writing any results to the provided {@link AssessmentResultBuilder}.
   * 
   * @param document
   *          the target document to assess
   * @param builder
   *          the {@link AssessmentResultBuilder} used to eventually generate a
   *          {@link AssessmentResults} once all assessments have been performed
   * @throws AssessmentException
   *           if an error occurs while performing the assessment
   */
  protected abstract void executeInternal(DOC document, AssessmentResultBuilder builder) throws AssessmentException;

  @Override
  public String getName(boolean includeDetail) {
    StringBuilder builder = new StringBuilder();
    builder.append('[');
    builder.append(getId());
    builder.append(']');
    builder.append(getAssessmentType());
    String details = getNameDetails();
    if (includeDetail && details != null) {
      builder.append(": ");
      builder.append(details);
    }
    return builder.toString();
  }

  /**
   * Retrieves basic naming details used to build a human-readable label for the assessment.
   * 
   * @return the details or {@code null} if none exist
   */
  protected abstract String getNameDetails();
}
