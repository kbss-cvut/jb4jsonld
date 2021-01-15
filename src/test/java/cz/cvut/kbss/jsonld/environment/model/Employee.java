/**
 * Copyright (C) 2020 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.environment.model;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jsonld.environment.Vocabulary;

import java.lang.reflect.Field;
import java.util.Objects;

@OWLClass(iri = Vocabulary.EMPLOYEE)
public class Employee extends User {

    @OWLObjectProperty(iri = Vocabulary.IS_MEMBER_OF)
    private Organization employer;

    public Organization getEmployer() {
        return employer;
    }

    public void setEmployer(Organization employer) {
        this.employer = employer;
    }

    public static Field getEmployerField() throws NoSuchFieldException {
        return Employee.class.getDeclaredField("employer");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return Objects.equals(getUri(), employee.getUri());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUri());
    }
}
