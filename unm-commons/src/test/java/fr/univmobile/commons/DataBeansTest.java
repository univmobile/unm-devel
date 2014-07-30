package fr.univmobile.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import javax.annotation.Nullable;

import org.junit.Test;

import fr.univmobile.commons.DataBeans;

public class DataBeansTest {

	@Test
	public void test_instantiatePerson() throws Exception {

		final Person person = DataBeans.instantiate(Person.class);

		assertNull(person.getFirstName());

		assertEquals("{firstName: null, lastName: null}", person.toString());

		assertSame(person, person);

		assertEquals(person, person);

		person.setFirstName("David");

		assertEquals("David", person.getFirstName());

		assertEquals("{firstName: David, lastName: null}", person.toString());

		assertEquals(person, person);
	}

	@Test
	public void test_equalsPerson() throws Exception {

		final Person person0 = DataBeans.instantiate(Person.class);
		final Person person1 = DataBeans.instantiate(Person.class);

		assertNotSame(person0, person1);

		assertEquals(person0, person1);

		person0.setFirstName("David");

		assertNotEquals(person0, person1);

		person1.setFirstName("David");

		assertEquals(person0, person1);

		person1.setFirstName("André");

		assertNotEquals(person0, person1);
	}

	@Test
	public void test_instantiatePersonWithSetter() throws Exception {

		final Person person = DataBeans.instantiate(Person.class).setFirstName(
				"David");

		person.setFirstName("David");

		assertEquals("David", person.getFirstName());

		assertEquals("{firstName: David, lastName: null}", person.toString());

		assertEquals(person, person);
	}

	@Test
	public void test_instantiatePerson_isSameAfterSetter() throws Exception {

		final Person person0 = DataBeans.instantiate(Person.class);

		final Person person1 = person0.setFirstName("David");

		assertSame(person0, person1);
	}
}

interface Person {

	@Nullable
	String getFirstName();

	String getLastName();

	Person setFirstName(String firstName);

	void setLastName(String lastName);
}