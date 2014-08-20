package fr.univmobile.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import javax.annotation.Nullable;

import org.junit.Test;

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
	public void test_setNullable() throws Exception {

		final Person person = DataBeans.instantiate(Person.class);

		assertNull(person.getFirstName());

		person.setFirstName("David");

		assertNotNull(person.getFirstName());

		person.setFirstName(null);

		assertNull(person.getFirstName());
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_setNotNullable() throws Exception {

		final Person person = DataBeans.instantiate(Person.class);

		person.setLastName("David");

		assertNotNull(person.getLastName());

		person.setLastName(null);
	}

	@Test(expected = IllegalStateException.class)
	public void test_getNotNullable() throws Exception {

		final Person person = DataBeans.instantiate(Person.class);

		person.getLastName();
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

	@Test
	public void test_instantiatePersonWithAddress_arrayFieldIsEmpty()
			throws Exception {

		final PersonWithAddress person = DataBeans
				.instantiate(PersonWithAddress.class);

		assertEquals(0, person.getAddresses().length);
	}

	@Test
	public void test_instantiatePersonWithAddress_addToArray() throws Exception {

		final PersonWithAddress person = DataBeans
				.instantiate(PersonWithAddress.class);

		person.addToAddresses(DataBeans.instantiate(
				PersonWithAddress.Address.class).setText("20 street"));

		assertEquals(1, person.getAddresses().length);

		assertEquals("20 street", person.getAddresses()[0].getText());

		person.addToAddresses(DataBeans.instantiate(
				PersonWithAddress.Address.class).setText("40 avenue"));

		assertEquals(2, person.getAddresses().length);

		assertEquals("20 street", person.getAddresses()[0].getText());
		assertEquals("40 avenue", person.getAddresses()[1].getText());
	}
}

interface Person {

	@Nullable
	String getFirstName();

	String getLastName();

	Person setFirstName(@Nullable String firstName);

	void setLastName(String lastName);
}

interface PersonWithAddress {

	Address[] getAddresses();

	void addToAddresses(Address address);

	interface Address {

		String getText();

		Address setText(String text);
	}
}