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

package com.liferay.dynamic.data.mapping.io.internal;

import com.liferay.dynamic.data.mapping.io.DDMFormFieldJSONConverter;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marcellus Tavares
 */
@Component(immediate = true)
public class DDMFormFieldJSONConverterImpl
	implements DDMFormFieldJSONConverter {

	@Override
	public JSONObject convert(DDMFormField ddmFormField) {
		return ddmFormFieldToJSONObjectConverter.convert(ddmFormField);
	}

	@Override
	public DDMFormField convert(JSONObject jsonObject) throws PortalException {
		return jsonObjectToDDMFormFieldConverter.convert(jsonObject);
	}

	@Reference
	protected DDMFormFieldToJSONObjectConverter
		ddmFormFieldToJSONObjectConverter;

	@Reference
	protected JSONObjectToDDMFormFieldConverter
		jsonObjectToDDMFormFieldConverter;

}