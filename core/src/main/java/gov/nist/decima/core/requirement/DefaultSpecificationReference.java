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

package gov.nist.decima.core.requirement;

import java.net.URI;
import java.net.URISyntaxException;

public class DefaultSpecificationReference implements SpecificationReference {
  private final Specification specification;
  private final String section;
  private final String sectionFragment;
  private final String requirementFragment;

  /**
   * Represents a reference to a specification that supports a given requirement.
   * 
   * @param specification
   *          the referenced specification
   * @param section
   *          the section in the specification containing the requirement
   * @param sectionFragment
   *          a URL fragment that can be used to resolve the section location within a specification
   * @param requirementFragment
   *          a URL fragment that can be used to resolve the requirement location within a
   *          specification
   * 
   */
  public DefaultSpecificationReference(Specification specification, String section, String sectionFragment,
      String requirementFragment) {
    this.specification = specification;
    this.section = section;
    this.sectionFragment = sectionFragment;
    this.requirementFragment = requirementFragment;
  }

  @Override
  public Specification getSpecification() {
    return specification;
  }

  @Override
  public String getSection() {
    return section;
  }

  public String getSectionFragment() {
    return sectionFragment;
  }

  public String getRequirementFragment() {
    return requirementFragment;
  }

  @Override
  public URI getSectionURI() throws URISyntaxException {
    return appendFragment(getSpecification().getHref(), getSectionFragment());
  }

  @Override
  public URI getRequirementURI() throws URISyntaxException {
    return appendFragment(getSpecification().getHref(), getRequirementFragment());
  }

  protected URI appendFragment(URI href, String fragment) throws URISyntaxException {
    return new URI(href.getScheme(), href.getUserInfo(), href.getHost(), href.getPort(), href.getPath(),
        href.getQuery(), fragment);
  }
}
