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

package com.liferay.dynamic.data.mapping.form.renderer.internal.servlet;

import com.liferay.dynamic.data.mapping.form.evaluator.DDMFormEvaluationResult;
import com.liferay.dynamic.data.mapping.form.evaluator.DDMFormEvaluator;
import com.liferay.dynamic.data.mapping.form.evaluator.DDMFormEvaluatorContext;
import com.liferay.dynamic.data.mapping.form.evaluator.DDMFormFieldEvaluationResult;
import com.liferay.dynamic.data.mapping.form.renderer.internal.servlet.transport.DDMFormEvaluationMessages.DDMFormEvaluationRequest;
import com.liferay.dynamic.data.mapping.form.renderer.internal.servlet.transport.DDMFormEvaluationMessages.DDMFormEvaluationResponse;
import com.liferay.dynamic.data.mapping.form.renderer.internal.servlet.transport.DDMFormEvaluationMessages.DDMFormEvaluationResponse.Builder;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormFieldOptions;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.util.KeyValuePair;
import com.liferay.portal.kernel.util.ParamUtil;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marcellus Tavares
 */
@Component(
	immediate = true,
	property = {
		"osgi.http.whiteboard.context.path=/dynamic-data-mapping-form-context-provider",
		"osgi.http.whiteboard.servlet.name=com.liferay.dynamic.data.mapping.form.renderer.internal.servlet.DDMFormContextProviderServlet",
		"osgi.http.whiteboard.servlet.pattern=/dynamic-data-mapping-form-context-provider/*"
	},
	service = Servlet.class
)
public class DDMFormContextProviderServlet extends HttpServlet {

	protected void addFieldBuilderOptions(
		DDMFormFieldEvaluationResult ddmFormFieldEvaluationResult,
		DDMFormFieldOptions defaultDDMFormFieldOptions,
		DDMFormEvaluationResponse.Field.Builder fieldBuilder) {

		List<KeyValuePair> keyValuePairs =
			ddmFormFieldEvaluationResult.getProperty("options");

		if (keyValuePairs == null) {
			keyValuePairs = new ArrayList<>();

			for (String value : defaultDDMFormFieldOptions.getOptionsValues()) {
				LocalizedValue label =
					defaultDDMFormFieldOptions.getOptionLabels(value);

				keyValuePairs.add(
					new KeyValuePair(value, label.getString(Locale.US)));
			}
		}

		for (KeyValuePair keyValuePair : keyValuePairs) {
			DDMFormEvaluationResponse.Field.Option.Builder optionBuilder =
				DDMFormEvaluationResponse.Field.Option.newBuilder();

			optionBuilder.setLabel(keyValuePair.getValue());
			optionBuilder.setValue(keyValuePair.getKey());

			fieldBuilder.addOptions(optionBuilder);
		}
	}

	protected DDMFormEvaluationResponse createDDMFormEvaluationResponse(
		HttpServletRequest request) {

		try {
			long groupId = ParamUtil.getLong(request, "groupId");

			DDMFormEvaluationRequest ddmFormEvaluationRequest =
				DDMFormEvaluationRequest.parseFrom(request.getInputStream());

			DDMFormTemplateContextProcessor ddmFormTemplateContextProcessor =
				new DDMFormTemplateContextProcessor(ddmFormEvaluationRequest);

			DDMForm ddmForm = ddmFormTemplateContextProcessor.getDDMForm();

			DDMFormEvaluatorContext ddmFormEvaluatorContext =
				new DDMFormEvaluatorContext(
					ddmForm, ddmFormTemplateContextProcessor.getDDMFormValues(),
					Locale.US);

			ddmFormEvaluatorContext.addProperty("groupId", groupId);
			ddmFormEvaluatorContext.addProperty("request", request);

			DDMFormEvaluationResult ddmFormEvaluationResult =
				_ddmFormEvaluator.evaluate(ddmFormEvaluatorContext);

			return createDDMFormEvaluationResponse(
				ddmForm.getDDMFormFieldsMap(true), ddmFormEvaluationResult);
		}
		catch (Exception e) {
			e.printStackTrace();

			if (_log.isDebugEnabled()) {
				_log.debug(e, e);
			}
		}

		return null;
	}

	protected DDMFormEvaluationResponse createDDMFormEvaluationResponse(
		Map<String, DDMFormField> ddmFormFieldsMap,
		DDMFormEvaluationResult ddmFormEvaluationResult) {

		DDMFormEvaluationResponse.Builder ddmFormEvaluationResponseBuilder =
			DDMFormEvaluationResponse.newBuilder();

		for (DDMFormFieldEvaluationResult ddmFormFieldEvaluationResult :
				ddmFormEvaluationResult.getDDMFormFieldEvaluationResults()) {

			DDMFormEvaluationResponse.Field field =
				createFieldEvaluationResponse(
					ddmFormFieldsMap.get(
						ddmFormFieldEvaluationResult.getName()),
					ddmFormFieldEvaluationResult);

			ddmFormEvaluationResponseBuilder.addFields(field);
		}

		return ddmFormEvaluationResponseBuilder.build();
	}

	protected DDMFormEvaluationResponse.Field
		createFieldEvaluationResponse(
			DDMFormField ddmFormField,
			DDMFormFieldEvaluationResult ddmFormFieldEvaluationResult) {

		DDMFormEvaluationResponse.Field.Builder fieldBuilder =
			DDMFormEvaluationResponse.Field.newBuilder();

		fieldBuilder.setName(ddmFormFieldEvaluationResult.getName());
		fieldBuilder.setInstanceId(
			ddmFormFieldEvaluationResult.getInstanceId());
		fieldBuilder.setReadOnly(ddmFormFieldEvaluationResult.isReadOnly());
		fieldBuilder.setRequired(ddmFormFieldEvaluationResult.isRequired());
		fieldBuilder.setValid(ddmFormFieldEvaluationResult.isValid());
		fieldBuilder.setVisible(ddmFormFieldEvaluationResult.isVisible());
		fieldBuilder.setErrorMessage(
			ddmFormFieldEvaluationResult.getErrorMessage());
		fieldBuilder.setValue(
			ddmFormFieldEvaluationResult.getValue().toString());

		addFieldBuilderOptions(
			ddmFormFieldEvaluationResult, ddmFormField.getDDMFormFieldOptions(),
			fieldBuilder);

		return fieldBuilder.build();
	}

	@Override
	protected void doPost(
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException {

		DDMFormEvaluationResponse ddmFormEvaluationResponse =
			createDDMFormEvaluationResponse(request);

		if (ddmFormEvaluationResponse == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);

			return;
		}

		response.setContentType("application/x-protobuf");
		response.setStatus(HttpServletResponse.SC_OK);

		ServletResponseUtil.write(
			response, ddmFormEvaluationResponse.toByteArray());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DDMFormContextProviderServlet.class);

	private static final long serialVersionUID = 1L;

	@Reference
	private DDMFormEvaluator _ddmFormEvaluator;

}