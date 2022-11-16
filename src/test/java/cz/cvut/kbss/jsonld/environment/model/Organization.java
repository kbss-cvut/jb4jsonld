/**
 * Copyright (C) 2022 Czech Technical University in Prague
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

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@OWLClass(iri = Vocabulary.ORGANIZATION)
public class Organization implements GeneratesRdf {

    @SuppressWarnings("unused")
    private static final String DEFAULT_COUNTRY = "Czech Republic";

    @Id
    private URI uri;

    @OWLAnnotationProperty(iri = Vocabulary.DATE_CREATED)
    private Date dateCreated;

    @OWLAnnotationProperty(iri = RDFS.LABEL)
    private String name;

    @OWLDataProperty(iri = Vocabulary.BRAND)
    private Set<String> brands;

    private Long age;

    @OWLObjectProperty(iri = Vocabulary.HAS_ADMIN)
    private Set<Employee> admins;

    @OWLObjectProperty(iri = Vocabulary.HAS_MEMBER)
    private Set<Employee> employees;

    @OWLObjectProperty(iri = Vocabulary.ORIGIN)
    private URI country;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getBrands() {
        return brands;
    }

    public void setBrands(Set<String> brands) {
        this.brands = brands;
    }

    public Long getAge() {
        return age;
    }

    public void setAge(Long age) {
        this.age = age;
    }

    public Set<Employee> getAdmins() {
        return admins;
    }

    public void setAdmins(Set<Employee> admins) {
        this.admins = admins;
    }

    public void addAdmin(Employee admin) {
        Objects.requireNonNull(admin);
        if (admins == null) {
            this.admins = new HashSet<>();
        }
        admins.add(admin);
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }

    public void addEmployee(Employee employee) {
        Objects.requireNonNull(employee);
        if (employees == null) {
            this.employees = new HashSet<>();
        }
        employees.add(employee);
    }

    public URI getCountry() {
        return country;
    }

    public void setCountry(URI country) {
        this.country = country;
    }

    @Override
    public void toRdf(Model model, ValueFactory vf, Set<URI> visited) {
        if (visited.contains(uri)) {
            return;
        }
        visited.add(uri);
        final IRI id = vf.createIRI(uri.toString());
        model.add(id, RDF.TYPE, vf.createIRI(Vocabulary.ORGANIZATION));
        model.add(id, vf.createIRI(Vocabulary.DATE_CREATED), vf.createLiteral(
                DateTimeFormatter.ISO_DATE_TIME.format(dateCreated.toInstant().atOffset(ZoneOffset.UTC)),
                vf.createIRI(XSD.DATETIME)));
        model.add(id, vf.createIRI(RDFS.LABEL), vf.createLiteral(name));
        if (country != null) {
            model.add(id, vf.createIRI(Vocabulary.ORIGIN), vf.createIRI(country.toString()));
        }
        if (brands != null) {
            brands.forEach(b -> model.add(id, vf.createIRI(Vocabulary.BRAND), vf.createLiteral(b)));
        }
        if (employees != null) {
            employees.forEach(e -> {
                model.add(id, vf.createIRI(Vocabulary.HAS_MEMBER), vf.createIRI(e.getUri().toString()));
                e.toRdf(model, vf, visited);
            });
        }
        if (admins != null) {
            admins.forEach(a -> {
                model.add(id, vf.createIRI(Vocabulary.HAS_ADMIN), vf.createIRI(a.getUri().toString()));
                a.toRdf(model, vf, visited);
            });
        }
    }

    @Override
    public String toString() {
        return "Organization{" +
                "uri=" + uri +
                ", dateCreated=" + dateCreated +
                ", name='" + name + '\'' +
                ", brands=" + brands +
                ", age=" + age +
                ", employees=" + employees +
                '}';
    }

    public static Field getEmployeesField() throws NoSuchFieldException {
        return Organization.class.getDeclaredField("employees");
    }
}
