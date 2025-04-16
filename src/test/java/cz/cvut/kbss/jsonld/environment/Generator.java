/*
 * JB4JSON-LD
 * Copyright (C) 2025 Czech Technical University in Prague
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package cz.cvut.kbss.jsonld.environment;

import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.serialization.context.DummyJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.net.URI;
import java.util.*;

public class Generator {

    public static final String URI_BASE = "http://krizik.felk.cvut.cz/ontologies/jb4jsonld#";

    private static final Random RAND = new Random();

    private Generator() {
        throw new AssertionError();
    }

    /**
     * Returns a (pseudo)random positive integer between 1 (inclusive) and {@code max} (exclusive).
     *
     * @param max Upper bound
     * @return random integer
     */
    public static int randomInt(int max) {
        return randomInt(1, max);
    }

    /**
     * Returns a (pseudo)random positive integer between {@code min} (inclusive) and {@code max} (exclusive).
     *
     * @param min Lower bound
     * @param max Upper bound
     * @return random integer
     */
    public static int randomInt(int min, int max) {
        assert min >= 0;
        assert max > 1;
        assert min < max;
        int res;
        do {
            res = RAND.nextInt(max);
        } while (res < min);
        return res;
    }

    public static boolean randomBoolean() {
        return RAND.nextBoolean();
    }

    public static User generateUser() {
        final User user = new User();
        setUserAttributes(user);
        return user;
    }

    public static Set<User> generateUsers() {
        final Set<User> users = new HashSet<>();
        for (int i = 0; i < randomInt(10); i++) {
            users.add(generateUser());
        }
        return users;
    }

    private static void setUserAttributes(User user) {
        final int number = RAND.nextInt();
        user.setUsername("user" + number);
        user.setFirstName("FirstName" + number);
        user.setLastName("LastName" + number);
        user.setAdmin(randomBoolean());
        user.setPassword("pass" + number);
        user.setUri(generateUri());
    }

    public static URI generateUri() {
        return URI.create(URI_BASE + RAND.nextInt());
    }

    public static Employee generateEmployee() {
        final Employee employee = new Employee();
        setUserAttributes(employee);
        final Organization company = generateOrganization();
        employee.setEmployer(company);
        return employee;
    }

    public static Person generatePerson() {
        final Person person = new Person();
        person.setUri(Generator.generateUri());
        person.setFirstName("Catherine");
        person.setLastName("Halsey");
        person.setProperties(Generator.generateProperties(false));
        return person;
    }

    public static Organization generateOrganization() {
        final Organization org = new Organization();
        org.setUri(generateUri());
        org.setDateCreated(new Date());
        org.setName("Organization" + RAND.nextInt());
        org.setBrands(new HashSet<>());
        for (int i = 0; i < randomInt(10); i++) {
            org.getBrands().add("Brandy" + i);
        }
        return org;
    }

    /**
     * Generates random count of random properties with random values.
     *
     * @param singletons Whether the values should be singletons or collections of more than 1 value
     * @return The generated properties
     */
    public static Map<String, Set<String>> generateProperties(boolean singletons) {
        final Map<String, Set<String>> map = new HashMap<>();
        for (int i = 0; i < Generator.randomInt(5, 10); i++) {
            final String property = Generator.generateUri().toString();
            if (singletons) {
                map.put(property, Collections.singleton(Generator.generateUri().toString()));
            } else {
                final Set<String> value = new HashSet<>();
                for (int j = 0; j < Generator.randomInt(2, 5); j++) {
                    value.add(Generator.generateUri().toString());
                }
                map.put(property, value);
            }
        }
        return map;
    }

    public static <T> SerializationContext<T> serializationContext(T value) {
        return new SerializationContext<>(generateUri().toString(), value, DummyJsonLdContext.INSTANCE);
    }
}
