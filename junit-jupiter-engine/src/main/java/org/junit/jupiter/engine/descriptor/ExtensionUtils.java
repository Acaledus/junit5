/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;
import static org.junit.platform.commons.util.ReflectionUtils.isPrivate;
import static org.junit.platform.commons.util.ReflectionUtils.isStatic;
import static org.junit.platform.commons.util.ReflectionUtils.readFieldValue;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.engine.extension.ExtensionRegistry;

/**
 * Collection of utilities for working with extensions and the extension registry.
 *
 * @since 5.1
 */
final class ExtensionUtils {

	private static final Predicate<Field> isStaticExtension = new IsStaticExtensionField();
	private static final Predicate<Field> isNonStaticExtension = new IsNonStaticExtensionField();

	///CLOVER:OFF
	private ExtensionUtils() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	 * Register extensions in the supplied registry from fields in the supplied
	 * class that are annotated with {@link RegisterExtension @RegisterExtension}.
	 *
	 * @param clazz the class in which to search for fields
	 * @param registry the registry in which to register the extensions
	 * @param instance the instance of the supplied class; may be {@code null}
	 * when searching for {@code static} fields in the class
	 */
	static void registerExtensionsFromFields(Class<?> clazz, ExtensionRegistry registry, Object instance) {
		Predicate<Field> predicate = (instance == null) ? isStaticExtension : isNonStaticExtension;

		findAnnotatedFields(clazz, RegisterExtension.class, predicate).forEach(field -> {
			readFieldValue(field, instance).ifPresent(value -> {
				Extension extension = (Extension) value;
				registry.registerExtension(extension, field);
			});
		});
	}

	private static class IsNonStaticExtensionField implements Predicate<Field> {

		@Override
		public boolean test(Field field) {
			// Please do not collapse the following into a single statement.
			if (isStatic(field)) {
				return false;
			}
			if (isPrivate(field)) {
				return false;
			}
			if (!Extension.class.isAssignableFrom(field.getType())) {
				return false;
			}
			return true;
		}
	}

	private static class IsStaticExtensionField implements Predicate<Field> {

		@Override
		public boolean test(Field field) {
			// Please do not collapse the following into a single statement.
			if (!isStatic(field)) {
				return false;
			}
			if (isPrivate(field)) {
				return false;
			}
			if (!Extension.class.isAssignableFrom(field.getType())) {
				return false;
			}
			return true;
		}
	}

}
