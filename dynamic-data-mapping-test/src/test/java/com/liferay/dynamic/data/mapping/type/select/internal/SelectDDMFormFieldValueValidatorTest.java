/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.dynamic.data.mapping.type.select.internal;

import com.liferay.dynamic.data.mapping.form.field.type.DDMFormFieldValueValidationException;
import com.liferay.dynamic.data.mapping.form.field.type.DDMFormFieldValueValidator;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormFieldOptions;
import com.liferay.dynamic.data.mapping.model.UnlocalizedValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.test.util.DDMFormValuesTestUtil;
import com.liferay.dynamic.data.mapping.type.BaseDDMFormFieldOptionsValidationTestCase;
import com.liferay.portal.json.JSONFactoryImpl;

import com.liferay.portal.kernel.servlet.PortalSessionContext;
import com.liferay.portal.kernel.servlet.PortalSessionThreadLocal;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.util.Encryptor;
import com.liferay.util.EncryptorException;
import java.security.Key;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpSession;

/**
 * @author Marcellus Tavares
 */
public class SelectDDMFormFieldValueValidatorTest
	extends BaseDDMFormFieldOptionsValidationTestCase {

	@Before
	public void setUp() {
		setUpDDMFormFieldValueValidator();
		setUpPropsUtil();
	}

	protected void setUpPropsUtil() {
		PropsUtil propsUtil = new PropsUtil();

		Props props = Mockito.mock(Props.class);

		Mockito.when(
			props.get(Matchers.eq(PropsKeys.COMPANY_ENCRYPTION_ALGORITHM))
		).thenReturn(
			"AES"
		);

		Mockito.when(
			props.get(Matchers.eq(PropsKeys.COMPANY_ENCRYPTION_KEY_SIZE))
		).thenReturn(
			"128"
		);

		propsUtil.setProps(props);
	}

	@Test(expected = DDMFormFieldValueValidationException.class)
	public void testValidationWithNullEncryptionKey() throws Exception {
		DDMFormField ddmFormField = new DDMFormField("option", "select");

		ddmFormField.setProperty("dataSourceType", "data-provider");

		DDMFormFieldValue ddmFormFieldValue =
			DDMFormValuesTestUtil.createDDMFormFieldValue(
				"option", new UnlocalizedValue("[\"Option\"]"));

		_selectDDMFormFieldValueValidator.validate(
			ddmFormField, ddmFormFieldValue.getValue());
	}

	@Test(expected = DDMFormFieldValueValidationException.class)
	public void testValidationWithInvalidOptionPattern() throws Exception {
		DDMFormField ddmFormField = new DDMFormField("option", "select");

		ddmFormField.setProperty("dataSourceType", "data-provider");

		ddmFormField.setDataType("string");

		DDMFormFieldOptions ddmFormFieldOptions = new DDMFormFieldOptions();

		ddmFormFieldOptions.addOptionLabel("A", LocaleUtil.US, "Option A");
		ddmFormFieldOptions.addOptionLabel("B", LocaleUtil.US, "Option B");

		ddmFormField.setDDMFormFieldOptions(ddmFormFieldOptions);

		DDMFormFieldValue ddmFormFieldValue =
			DDMFormValuesTestUtil.createDDMFormFieldValue(
				"option", new UnlocalizedValue("[\"A\"]"));

		setPortalSessionThreadLocal(Encryptor.generateKey("AES"));

		_selectDDMFormFieldValueValidator.validate(
			ddmFormField, ddmFormFieldValue.getValue());
	}

	@Test(expected = DDMFormFieldValueValidationException.class)
	public void testValidationWithInvalidEncryptionKey() throws Exception {
		DDMFormField ddmFormField = new DDMFormField("option", "select");

		ddmFormField.setProperty("dataSourceType", "data-provider");

		ddmFormField.setDataType("string");

		long ddmDataProviderInstanceId = RandomTestUtil.randomLong();

		ddmFormField.setProperty(
			"ddmDataProviderInstanceId", ddmDataProviderInstanceId);

		DDMFormFieldOptions ddmFormFieldOptions = new DDMFormFieldOptions();

		ddmFormFieldOptions.addOptionLabel("A", LocaleUtil.US, "Option A");
		ddmFormFieldOptions.addOptionLabel("B", LocaleUtil.US, "Option B");

		ddmFormField.setDDMFormFieldOptions(ddmFormFieldOptions);

		Key key = Encryptor.generateKey();

		String valueString =
			"A" + StringPool.POUND +
				Encryptor.encrypt(
					key, ddmDataProviderInstanceId + StringPool.POUND + "A");

		System.out.println("encrypted: " + valueString);

		DDMFormFieldValue ddmFormFieldValue =
			DDMFormValuesTestUtil.createDDMFormFieldValue(
				"option",
				new UnlocalizedValue("[\"" + valueString +"\"]"));

		setPortalSessionThreadLocal(Encryptor.generateKey());

		_selectDDMFormFieldValueValidator.validate(
			ddmFormField, ddmFormFieldValue.getValue());
	}

	@Test
	public void testValidationWithValidEncryptionKey() throws Exception {
		DDMFormField ddmFormField = new DDMFormField("option", "select");

		ddmFormField.setProperty("dataSourceType", "data-provider");

		ddmFormField.setDataType("string");

		long ddmDataProviderInstanceId = RandomTestUtil.randomLong();

		ddmFormField.setProperty(
			"ddmDataProviderInstanceId", ddmDataProviderInstanceId);

		DDMFormFieldOptions ddmFormFieldOptions = new DDMFormFieldOptions();

		ddmFormFieldOptions.addOptionLabel("A", LocaleUtil.US, "Option A");
		ddmFormFieldOptions.addOptionLabel("B", LocaleUtil.US, "Option B");

		ddmFormField.setDDMFormFieldOptions(ddmFormFieldOptions);

		Key key = Encryptor.generateKey();

		String valueString =
			"A" + StringPool.POUND +
				Encryptor.encrypt(
					key, ddmDataProviderInstanceId + StringPool.POUND + "A");

		DDMFormFieldValue ddmFormFieldValue =
			DDMFormValuesTestUtil.createDDMFormFieldValue(
				"option",
				new UnlocalizedValue("[\"" + valueString +"\"]"));

		setPortalSessionThreadLocal(key);

		_selectDDMFormFieldValueValidator.validate(
			ddmFormField, ddmFormFieldValue.getValue());
	}

	protected void setPortalSessionThreadLocal(Key key)
		throws Exception {

		HttpSession httpSession = new MockHttpSession();

		httpSession.setAttribute(
			"DDM_SERIALIZED_KEY", Encryptor.serializeKey(key));

		System.out.println("serialized session key: " + Encryptor.serializeKey(key));

		PortalSessionContext.put(httpSession.getId(), httpSession);

		PortalSessionThreadLocal.setHttpSession(httpSession);
	}

	@Override
	protected DDMFormFieldValueValidator getDDMFormFieldValueValidator() {
		return _selectDDMFormFieldValueValidator;
	}

	protected void setUpDDMFormFieldValueValidator() {
		_selectDDMFormFieldValueValidator.jsonFactory = new JSONFactoryImpl();
	}

	private final SelectDDMFormFieldValueValidator
		_selectDDMFormFieldValueValidator =
			new SelectDDMFormFieldValueValidator();

}