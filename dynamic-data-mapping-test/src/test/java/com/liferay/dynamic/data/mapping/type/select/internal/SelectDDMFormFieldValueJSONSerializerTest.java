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

import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormFieldOptions;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.model.UnlocalizedValue;
import com.liferay.dynamic.data.mapping.model.Value;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalServiceUtil;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalServiceUtil;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.test.util.DDMFormValuesTestUtil;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Marcellus Tavares
 */
@PrepareForTest(LocaleUtil.class)
@RunWith(PowerMockRunner.class)
public class SelectDDMFormFieldValueJSONSerializerTest {

	public void setUp() {
		_selectDDMFormFieldValueJSONSerializer.jsonFactory =
			new JSONFactoryImpl();

		//LocaleUtil.toLanguageId()
	}

//	protected void setUpLocaleUtil() {
//		when(
//			LocaleUtil.toLanguageId(LocaleUtil.US)
//		).thenReturn(
//			"en_US"
//		);
//
//		when(
//			LocaleUtil.toLanguageId(LocaleUtil.BRAZIL)
//		).thenReturn(
//			"pt_BR"
//		);
//	}

	@Test
	public void testSerializeWithManualOptionsAndUnlocalizedValue() {
		DDMFormField ddmFormField = new DDMFormField("option", "select");

		ddmFormField.setDataType("string");

		DDMFormFieldOptions ddmFormFieldOptions = new DDMFormFieldOptions();

		ddmFormFieldOptions.addOptionLabel("A", LocaleUtil.US, "Option A");
		ddmFormFieldOptions.addOptionLabel("B", LocaleUtil.US, "Option B");

		ddmFormField.setDDMFormFieldOptions(ddmFormFieldOptions);

		String valueString = "[\"A\"]";

		Object serializedValue =
			_selectDDMFormFieldValueJSONSerializer.serialize(
				ddmFormField, new UnlocalizedValue(valueString));

		Assert.assertEquals(valueString, serializedValue);
	}

	@Test
	public void testSerializeWithManualOptionsAndLocalizedValue() {
		DDMFormField ddmFormField = new DDMFormField("option", "select");

		ddmFormField.setDataType("string");

		DDMFormFieldOptions ddmFormFieldOptions = new DDMFormFieldOptions();

		ddmFormFieldOptions.addOptionLabel("A", LocaleUtil.US, "Option A");
		ddmFormFieldOptions.addOptionLabel("B", LocaleUtil.US, "Option B");

		ddmFormField.setDDMFormFieldOptions(ddmFormFieldOptions);

		LocalizedValue localizedValue = new LocalizedValue();

		localizedValue.addString(Locale.US,  "[\"A\"]");
		localizedValue.addString(Locale.FRANCE,  "[\"B\"]");

		Object serializedValue =
			_selectDDMFormFieldValueJSONSerializer.serialize(
				ddmFormField, localizedValue);

		JSONFactory jsonFactory = new JSONFactoryImpl();

		JSONObject jsonObject = jsonFactory.createJSONObject();

		jsonObject.put("")

		Assert.assertEquals(valueString, serializedValue);
	}

	private SelectDDMFormFieldValueJSONSerializer
		_selectDDMFormFieldValueJSONSerializer =
			new SelectDDMFormFieldValueJSONSerializer();

}
