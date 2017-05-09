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

package com.liferay.dynamic.data.mapping.form.field.type.internal;

import com.liferay.dynamic.data.mapping.data.provider.DDMDataProviderContext;
import com.liferay.dynamic.data.mapping.data.provider.DDMDataProviderContextFactory;
import com.liferay.dynamic.data.mapping.data.provider.DDMDataProviderInvoker;
import com.liferay.dynamic.data.mapping.data.provider.DDMDataProviderRequest;
import com.liferay.dynamic.data.mapping.data.provider.DDMDataProviderResponse;
import com.liferay.dynamic.data.mapping.data.provider.DDMDataProviderResponseOutput;
import com.liferay.dynamic.data.mapping.form.field.type.DDMFormFieldOptionsFactory;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormFieldOptions;
import com.liferay.dynamic.data.mapping.render.DDMFormFieldRenderingContext;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.KeyValuePair;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.util.Encryptor;

import java.security.Key;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marcellus Tavares
 */
@Component(immediate = true, service = DDMFormFieldOptionsFactory.class)
public class DDMFormFieldOptionsFactoryImpl
	implements DDMFormFieldOptionsFactory {

	@Override
	public DDMFormFieldOptions create(
		DDMFormField ddmFormField,
		DDMFormFieldRenderingContext ddmFormFieldRenderingContext) {

		String dataSourceType = GetterUtil.getString(
			ddmFormField.getProperty("dataSourceType"), "manual");

		if (Objects.equals(dataSourceType, "data-provider")) {
			return createDDMFormFieldOptionsFromDataProvider(
				ddmFormField, ddmFormFieldRenderingContext);
		}
		else {
			return createDDMFormFieldOptions(
				ddmFormField, ddmFormFieldRenderingContext);
		}
	}

	protected DDMFormFieldOptions createDDMFormFieldOptions(
		DDMFormField ddmFormField,
		DDMFormFieldRenderingContext ddmFormFieldRenderingContext) {

		List<Map<String, String>> options =
			(List<Map<String, String>>)
				ddmFormFieldRenderingContext.getProperty("options");

		if (options.isEmpty()) {
			return ddmFormField.getDDMFormFieldOptions();
		}

		DDMFormFieldOptions ddmFormFieldOptions = new DDMFormFieldOptions();

		for (Map<String, String> option : options) {
			ddmFormFieldOptions.addOptionLabel(
				option.get("value"), ddmFormFieldRenderingContext.getLocale(),
				option.get("label"));
		}

		return ddmFormFieldOptions;
	}

	protected DDMFormFieldOptions createDDMFormFieldOptionsFromDataProvider(
		DDMFormField ddmFormField,
		DDMFormFieldRenderingContext ddmFormFieldRenderingContext) {

		DDMFormFieldOptions ddmFormFieldOptions = new DDMFormFieldOptions();

		ddmFormFieldOptions.setDefaultLocale(
			ddmFormFieldRenderingContext.getLocale());

		try {
			String ddmDataProviderInstanceId = GetterUtil.getString(
				ddmFormField.getProperty("ddmDataProviderInstanceId"));

			DDMDataProviderContext ddmDataProviderContext =
				ddmDataProviderContextFactory.create(ddmDataProviderInstanceId);

			DDMDataProviderRequest ddmDataProviderRequest =
				new DDMDataProviderRequest(
					ddmDataProviderContext,
					ddmFormFieldRenderingContext.getHttpServletRequest());

			ddmDataProviderRequest.queryString(
				"filterParameterValue",
				String.valueOf(ddmFormFieldRenderingContext.getValue()));

			DDMDataProviderResponse ddmDataProviderResponse =
				ddmDataProviderInvoker.invoke(ddmDataProviderRequest);

			String ddmDataProviderInstanceOutput = GetterUtil.getString(
				ddmFormField.getProperty("ddmDataProviderInstanceOutput"),
				"Default-Output");

			DDMDataProviderResponseOutput dataProviderResponseOutput =
				ddmDataProviderResponse.get(ddmDataProviderInstanceOutput);

			if ((dataProviderResponseOutput == null) ||
				!Objects.equals(dataProviderResponseOutput.getType(), "list")) {

				return ddmFormFieldOptions;
			}

			List<KeyValuePair> keyValuesPairs =
				dataProviderResponseOutput.getValue(List.class);

			Key key = getKey(ddmFormFieldRenderingContext);

			for (KeyValuePair keyValuePair : keyValuesPairs) {
				String optionValue = keyValuePair.getKey();

				if (key != null) {
					optionValue = encryptOptionValue(
						ddmDataProviderInstanceId, key, optionValue);
				}

				ddmFormFieldOptions.addOptionLabel(
					optionValue, ddmFormFieldRenderingContext.getLocale(),
					keyValuePair.getValue());
			}
		}
		catch (Exception e) {
			if (_log.isDebugEnabled()) {
				_log.debug(e, e);
			}
		}

		return ddmFormFieldOptions;
	}

	protected String encryptOptionValue(
		String ddmDataProviderInstanceId, Key key, String optionValue) {

		String text = String.format(
			"%s#%s", ddmDataProviderInstanceId, optionValue);

		try {
			String encryptedText = Encryptor.encrypt(key, text);

			return String.format("%s#%s", optionValue, encryptedText);
		}
		catch (Exception e) {
			if (_log.isDebugEnabled()) {
				_log.debug(e, e);
			}

			return optionValue;
		}
	}

	protected Key getKey(
		DDMFormFieldRenderingContext ddmFormFieldRenderingContext) {

		boolean encryptionEnabled = GetterUtil.getBoolean(
			ddmFormFieldRenderingContext.getProperty("encryptionEnabled"));

		if (!encryptionEnabled) {
			return null;
		}

		HttpServletRequest request = portal.getOriginalServletRequest(
			ddmFormFieldRenderingContext.getHttpServletRequest());

		HttpSession session = request.getSession();

		Key key = null;

		try {
			String serializedKey = (String)session.getAttribute(_KEY);

			if (serializedKey == null) {
				key = Encryptor.generateKey();

				serializedKey = Encryptor.serializeKey(key);

				session.setAttribute(_KEY, serializedKey);
			}
			else {
				key = Encryptor.deserializeKey(serializedKey);
			}
		}
		catch (Exception e) {
			if (_log.isDebugEnabled()) {
				_log.debug(e, e);
			}
		}

		return key;
	}

	@Reference
	protected DDMDataProviderContextFactory ddmDataProviderContextFactory;

	@Reference
	protected DDMDataProviderInvoker ddmDataProviderInvoker;

	@Reference
	protected Portal portal;

	private static final String _KEY = "DDM_SERIALIZED_KEY";

	private static final Log _log = LogFactoryUtil.getLog(
		DDMFormFieldOptionsFactoryImpl.class);

}