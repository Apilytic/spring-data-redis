/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.redis.core.mapping;

import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsEqual.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.keyvalue.core.mapping.KeySpaceResolver;
import org.springframework.data.keyvalue.core.mapping.KeyValuePersistentProperty;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.redis.core.TimeToLiveAccessor;
import org.springframework.data.redis.core.convert.ConversionTestEntities;
import org.springframework.data.util.TypeInformation;

/**
 * @author Christoph Strobl
 * @param <T>
 * @param <ID>
 */
@RunWith(MockitoJUnitRunner.class)
public class BasicRedisPersistentEntityUnitTests<T, ID extends Serializable> {

	public @Rule ExpectedException expectedException = ExpectedException.none();

	@Mock TypeInformation<T> entityInformation;
	@Mock KeySpaceResolver keySpaceResolver;
	@Mock TimeToLiveAccessor ttlAccessor;

	BasicRedisPersistentEntity<T> entity;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {

		when(entityInformation.getType()).thenReturn((Class<T>) ConversionTestEntities.Person.class);
		entity = new BasicRedisPersistentEntity<T>(entityInformation, keySpaceResolver, ttlAccessor);
	}

	@Test // DATAREDIS-425
	public void addingMultipleIdPropertiesWithoutAnExplicitOneThrowsException() {

		expectedException.expect(MappingException.class);
		expectedException.expectMessage("Attempt to add id property");
		expectedException.expectMessage("but already have an property");

		KeyValuePersistentProperty property1 = mock(RedisPersistentProperty.class);
		when(property1.isIdProperty()).thenReturn(true);

		KeyValuePersistentProperty property2 = mock(RedisPersistentProperty.class);
		when(property2.isIdProperty()).thenReturn(true);

		entity.addPersistentProperty(property1);
		entity.addPersistentProperty(property2);
	}

	@Test // DATAREDIS-425
	@SuppressWarnings("unchecked")
	public void addingMultipleExplicitIdPropertiesThrowsException() {

		expectedException.expect(MappingException.class);
		expectedException.expectMessage("Attempt to add explicit id property");
		expectedException.expectMessage("but already have an property");

		KeyValuePersistentProperty property1 = mock(RedisPersistentProperty.class);
		when(property1.isIdProperty()).thenReturn(true);
		when(property1.isAnnotationPresent(any(Class.class))).thenReturn(true);

		KeyValuePersistentProperty property2 = mock(RedisPersistentProperty.class);
		when(property2.isIdProperty()).thenReturn(true);
		when(property2.isAnnotationPresent(any(Class.class))).thenReturn(true);

		entity.addPersistentProperty(property1);
		entity.addPersistentProperty(property2);
	}

	@Test // DATAREDIS-425
	@SuppressWarnings("unchecked")
	public void explicitIdPropertiyShouldBeFavoredOverNonExplicit() {

		KeyValuePersistentProperty property1 = mock(RedisPersistentProperty.class);
		when(property1.isIdProperty()).thenReturn(true);

		KeyValuePersistentProperty property2 = mock(RedisPersistentProperty.class);
		when(property2.isIdProperty()).thenReturn(true);
		when(property2.isAnnotationPresent(any(Class.class))).thenReturn(true);

		entity.addPersistentProperty(property1);
		entity.addPersistentProperty(property2);

		assertThat(entity.getIdProperty(), is(equalTo(property2)));
	}
}
