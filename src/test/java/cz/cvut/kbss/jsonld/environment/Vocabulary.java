/**
 * Copyright (C) 2020 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.environment;

import cz.cvut.kbss.jopa.vocabulary.DC;

public class Vocabulary {

    public static final String DEFAULT_PREFIX = "http://krizik.felk.cvut.cz/ontologies/jb4jsonld/";

    public static final String PERSON = "http://onto.fel.cvut.cz/ontologies/ufo/Person";
    public static final String USER = DEFAULT_PREFIX + "User";
    public static final String EMPLOYEE = DEFAULT_PREFIX + "Employee";
    public static final String ORGANIZATION = DEFAULT_PREFIX + "Organization";
    public static final String STUDY = DEFAULT_PREFIX + "Study";
    public static final String AGENT = "http://onto.fel.cvut.cz/ontologies/ufo/Agent";
    public static final String OBJECT_WITH_ANNOTATIONS = DEFAULT_PREFIX + "ObjectWithAnnotations";
    public static final String GENERIC_MEMBER = DEFAULT_PREFIX + "GenericMember";

    public static final String FIRST_NAME = "http://xmlns.com/foaf/0.1/firstName";
    public static final String LAST_NAME = "http://xmlns.com/foaf/0.1/lastName";
    public static final String USERNAME = "http://xmlns.com/foaf/0.1/accountName";
    public static final String KNOWS = "http://xmlns.com/foaf/0.1/knows";
    public static final String DATE_CREATED = DC.Terms.NAMESPACE + "created";
    public static final String IS_MEMBER_OF = DEFAULT_PREFIX + "isMemberOf";
    public static final String HAS_MEMBER = DEFAULT_PREFIX + "hasMember";
    public static final String HAS_PARTICIPANT = DEFAULT_PREFIX + "hasParticipant";
    public static final String HAS_ADMIN = DEFAULT_PREFIX + "hasAdmin";
    public static final String BRAND = DEFAULT_PREFIX + "brand";
    public static final String IS_ADMIN = DEFAULT_PREFIX + "isAdmin";
    public static final String ORIGIN = DEFAULT_PREFIX + "origin";
    public static final String HAS_EVENT_TYPE = DEFAULT_PREFIX + "hasEventType";
    public static final String ROLE = DEFAULT_PREFIX + "role";
    public static final String PASSWORD = DEFAULT_PREFIX + "password";
    public static final String NUMBER_OF_PEOPLE_INVOLVED = DEFAULT_PREFIX + "numberOfPeopleInvolved";

    public static final String CHANGED_VALUE = DEFAULT_PREFIX + "changedValue";

    private Vocabulary() {
        throw new AssertionError();
    }
}
