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

package gov.nist.secauto.decima.xml.templating.document.post.template;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class TemplateProcessorBuilder {
  // TODO: should this be a URI?
  private URL contextSystemId;
  // TODO: also allow loading of the document?
  private URL baseTemplateURL;
  private final List<Action> actions = new LinkedList<>();

  public URL getTemplateURL() {
    return baseTemplateURL;
  }

  public TemplateProcessorBuilder setTemplateURL(URL url) {
    this.baseTemplateURL = url;
    return this;
  }

  public List<Action> getActions() {
    return actions;
  }

  /**
   * Create a new TemplateProcessor based on the information provided by this builder.
   * 
   * @return a new template processor
   */
  public TemplateProcessor build() {
    URL templateURL = getTemplateURL();
    Objects.requireNonNull(templateURL);

    List<Action> actions = this.actions;
    if (actions.isEmpty()) {
      actions = Collections.emptyList();
    }
    return new DefaultTemplateProcessor(getContextSystemId(), templateURL, actions);
  }

  public void addActions(Collection<? extends Action> actions) {
    Objects.requireNonNull(actions);
    this.actions.addAll(actions);
  }

  public void addAction(Action action) {
    Objects.requireNonNull(action);
    actions.add(action);
  }

  public URL getContextSystemId() {
    return contextSystemId;
  }

  public void setContextSystemId(URL url) {
    Objects.requireNonNull(url);
    this.contextSystemId = url;
  }
}
