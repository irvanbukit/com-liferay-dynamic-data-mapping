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

import com.liferay.dynamic.data.mapping.form.field.type.DDMFormFieldValueJSONSerializer;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.Value;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.Locale;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Leonardo Barros
 */
@Component(immediate = true, property = "ddm.form.field.type.name=select")
public class SelectDDMFormFieldValueJSONSerializer
	implements DDMFormFieldValueJSONSerializer {

	@Override
	public Object serialize(DDMFormField ddmFormField, Value value) {
		boolean manualDataSourceType = isManualDataSourceType(ddmFormField);

		try {
			if (value.isLocalized()) {
				return toJSONObject(value, manualDataSourceType);
			}
			else {
				String valueString = value.getString(LocaleUtil.ROOT);

				if (manualDataSourceType || Validator.isNull(valueString)) {
					return valueString;
				}

				JSONArray jsonArray = extractValuesJSONArray(valueString);

				return jsonArray.toJSONString();
			}
		}
		catch (Exception e) {
			throw new IllegalArgumentException(
				String.format(
					"Unable to serialize select field \"%s\" value",
					ddmFormField.getName()),
				e);
		}
	}

	protected JSONArray extractValuesJSONArray(String valueString)
		throws Exception {

		JSONArray jsonArray = jsonFactory.createJSONArray();

		JSONArray valuesJSONArray = jsonFactory.createJSONArray(valueString);

		for (int i = 0; i < valuesJSONArray.length(); i++) {
			String[] values = StringUtil.split(
				valuesJSONArray.getString(i), CharPool.POUND);

			jsonArray.put(values[0]);
		}

		return jsonArray;
	}

	protected boolean isManualDataSourceType(DDMFormField ddmFormField) {
		String dataSourceType = GetterUtil.getString(
			ddmFormField.getProperty("dataSourceType"), "manual");

		if (Objects.equals(dataSourceType, "manual")) {
			return true;
		}

		return false;
	}

	protected JSONObject toJSONObject(Value value, boolean manualDataSourceType)
		throws Exception {

		JSONObject jsonObject = jsonFactory.createJSONObject();

		for (Locale availableLocale : value.getAvailableLocales()) {
			String valueString = value.getString(availableLocale);

			if (manualDataSourceType) {
				jsonObject.put(
					LocaleUtil.toLanguageId(availableLocale), valueString);
			}
			else {
				JSONArray jsonArray = extractValuesJSONArray(valueString);

				jsonObject.put(
					LocaleUtil.toLanguageId(availableLocale),
					jsonArray.toJSONString());
			}
		}

		return jsonObject;
	}

	@Reference
	protected JSONFactory jsonFactory;

}