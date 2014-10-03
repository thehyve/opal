/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain.taxonomy;

import java.util.List;

import com.google.common.collect.Lists;

public class Vocabulary extends TaxonomyEntity {

  private boolean repeatable;

  private List<Term> terms;

  public Vocabulary() {
  }

  public Vocabulary(String name) {
    setName(name);
  }

  public boolean isRepeatable() {
    return repeatable;
  }

  public void setRepeatable(boolean repeatable) {
    this.repeatable = repeatable;
  }

  public List<Term> getTerms() {
    return terms;
  }

  public void setTerms(List<Term> terms) {
    this.terms = terms;
  }

  public boolean hasTerms() {
    return terms != null && terms.size() > 0;
  }

  public Vocabulary addTerm(Term term) {
    if(terms == null) terms = Lists.newArrayList();
    int idx = terms.indexOf(term);
    if(idx <= 0) terms.add(term);
    else terms.set(idx, term);
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(!(o instanceof Vocabulary)) return false;
    Vocabulary voc = (Vocabulary) o;
    return getName().equals(voc.getName());
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }
}