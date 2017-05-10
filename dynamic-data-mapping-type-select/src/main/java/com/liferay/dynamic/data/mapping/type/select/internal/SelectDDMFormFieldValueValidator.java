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
import com.liferay.dynamic.data.mapping.model.Value;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.PortalSessionThreadLocal;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.util.Encryptor;
import com.liferay.util.EncryptorException;

import java.security.Key;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marcellus Tavares
 */
@Component(
	immediate = true, property = "ddm.form.field.type.name=select",
	service = DDMFormFieldValueValidator.class
)
public class SelectDDMFormFieldValueValidator
	implements DDMFormFieldValueValidator {

	@Override
	public void validate(DDMFormField ddmFormField, Value value)
		throws DDMFormFieldValueValidationException {

		String dataSourceType = GetterUtil.getString(
			ddmFormField.getProperty("dataSourceType"), "manual");

		if (Objects.equals(dataSourceType, "manual")) {
			validateManualDDMFormFieldOptions(ddmFormField, value);
		}
		else {
			validateDataProviderDDMFormFieldOptions(ddmFormField, value);
		}
	}

	protected JSONArray createJSONArray(String fieldName, String json)
		throws DDMFormFieldValueValidationException {

		try {
			return jsonFactory.createJSONArray(json);
		}
		catch (JSONException jsone) {

			// LPS-52675

			if (_log.isDebugEnabled()) {
				_log.debug(jsone, jsone);
			}

			throw new DDMFormFieldValueValidationException(
				String.format(
					"Invalid data stored for select field \"%s\"", fieldName));
		}
	}

	protected String decryptSelectedValue(
			DDMFormField ddmFormField, Key key, String encryptedOptionValue)
		throws DDMFormFieldValueValidationException {

		try {
			return Encryptor.decrypt(key, encryptedOptionValue);
		}
		catch (Exception e) {
			throw new DDMFormFieldValueValidationException(
				String.format(
					"Invalid value encrypted found for select field \"%s\"",
					ddmFormField.getName()));
		}
	}

	protected Key getKey() {
		HttpSession session = PortalSessionThreadLocal.getHttpSession();

		if (session == null) {
			return null;
		}

		String serializedKey = GetterUtil.getString(session.getAttribute(_KEY));

		System.out.println("serializedKey: " + serializedKey);


		if (Validator.isNotNull(serializedKey)) {
			return Encryptor.deserializeKey(serializedKey);
		}

		return null;
	}

	protected void validateDataProviderDDMFormFieldOptions(
			DDMFormField ddmFormField, Value value)
		throws DDMFormFieldValueValidationException {

		Key key = getKey();

		if (key == null) {
			throw new DDMFormFieldValueValidationException(
				String.format(
					"No encryption key found for select field \"%s\"",
					ddmFormField.getName()));
		}

		Map<Locale, String> selectedValues = value.getValues();

		for (String selectedValue : selectedValues.values()) {
			validateSelectedValue(ddmFormField, key, selectedValue);
		}
	}

	protected void validateManualDDMFormFieldOptions(
			DDMFormField ddmFormField, Value value)
		throws DDMFormFieldValueValidationException {

		DDMFormFieldOptions ddmFormFieldOptions =
			ddmFormField.getDDMFormFieldOptions();

		if (ddmFormFieldOptions == null) {
			throw new DDMFormFieldValueValidationException(
				String.format(
					"Options must be set for select field \"%s\"",
					ddmFormField.getName()));
		}

		Set<String> optionValues = ddmFormFieldOptions.getOptionsValues();

		if (optionValues.isEmpty()) {
			throw new DDMFormFieldValueValidationException(
				"Options must contain at least one alternative");
		}

		Map<Locale, String> selectedValues = value.getValues();

		for (String selectedValue : selectedValues.values()) {
			validateSelectedValue(ddmFormField, optionValues, selectedValue);
		}
	}

	protected void validateSelectedValue(
			DDMFormField ddmFormField, Key key, String selectedValue)
		throws DDMFormFieldValueValidationException {

		JSONArray jsonArray = createJSONArray(
			ddmFormField.getName(), selectedValue);

		for (int i = 0; i < jsonArray.length(); i++) {
			String[] selectedValueArray = StringUtil.split(
				jsonArray.getString(i), CharPool.POUND);

			if (selectedValueArray.length != 2) {
				throw new DDMFormFieldValueValidationException(
					String.format(
						"Invalid pattern value found for select field \"%s\"",
						ddmFormField.getName()));
			}

			String decryptedSelectedValue = decryptSelectedValue(
				ddmFormField, key, selectedValueArray[1]);

			System.out.println("decrypted value " + decryptedSelectedValue);

			Object ddmDataProviderInstanceId = ddmFormField.getProperty(
				"ddmDataProviderInstanceId");

			String expectedSelectedValue = String.format(
				"%s#%s", ddmDataProviderInstanceId, selectedValueArray[0]);

			System.out.println("expectedSelectedValue  " + expectedSelectedValue);

			if (!expectedSelectedValue.equals(decryptedSelectedValue)) {
				throw new DDMFormFieldValueValidationException(
					String.format(
						"Invalid value found for select field \"%s\"",
						ddmFormField.getName()));
			}
		}
	}

	protected void validateSelectedValue(
			DDMFormField ddmFormField, Set<String> optionValues,
			String selectedValue)
		throws DDMFormFieldValueValidationException {

		JSONArray jsonArray = createJSONArray(
			ddmFormField.getName(), selectedValue);

		for (int i = 0; i < jsonArray.length(); i++) {
			if (Validator.isNull(jsonArray.getString(i)) &&
				!ddmFormField.isRequired()) {

				continue;
			}

			if (!optionValues.contains(jsonArray.getString(i))) {
				throw new DDMFormFieldValueValidationException(
					String.format(
						"The selected option \"%s\" is not a valid alternative",
						jsonArray.getString(i)));
			}
		}
	}

	@Reference
	protected JSONFactory jsonFactory;

	private static final String _KEY = "DDM_SERIALIZED_KEY";

	private static final Log _log = LogFactoryUtil.getLog(
		SelectDDMFormFieldValueValidator.class);

}
