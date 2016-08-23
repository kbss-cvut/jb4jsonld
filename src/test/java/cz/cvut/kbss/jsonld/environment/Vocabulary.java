/**
 * Copyright (C) 2016 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.environment;

public class Vocabulary {

    public static final String PERSON = "http://onto.fel.cvut.cz/ontologies/ufo/Person";
    public static final String USER = "http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld/User";
    public static final String EMPLOYEE = "http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld/Employee";
    public static final String ORGANIZATION = "http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld/Organization";

    public static final String FIRST_NAME = "http://xmlns.com/foaf/0.1/firstName";
    public static final String LAST_NAME = "http://xmlns.com/foaf/0.1/lastName";
    public static final String USERNAME = "http://xmlns.com/foaf/0.1/accountName";
    public static final String DATE_CREATED = "http://purl.org/dc/terms/created";
    public static final String IS_MEMBER_OF = "http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld/isMemberOf";
    public static final String HAS_MEMBER = "http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld/hasMember";
    public static final String BRAND = "http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld/brand";
    public static final String IS_ADMIN = "http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld/isAdmin";

    private Vocabulary() {
        throw new AssertionError();
    }
}
