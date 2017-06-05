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

import com.liferay.dynamic.data.mapping.form.renderer.internal.servlet.transport.DDMFormEvaluationMessages.DDMFormEvaluationRequest;
import com.liferay.dynamic.data.mapping.form.renderer.internal.servlet.transport.DDMFormEvaluationMessages.DDMFormEvaluationRequest.Form;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormFieldOptions;
import com.liferay.dynamic.data.mapping.model.DDMFormFieldValidation;
import com.liferay.dynamic.data.mapping.model.DDMFormRule;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.model.UnlocalizedValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author Marcellus Tavares
 */
public class DDMFormTemplateContextProcessor {

	public DDMFormTemplateContextProcessor(
		DDMFormEvaluationRequest ddmFormEvaluationRequest) {

		_form = ddmFormEvaluationRequest.getForm();

		_ddmForm = new DDMForm();

		_ddmFormValues = new DDMFormValues(_ddmForm);

		_locale = Locale.US;

		initModels();

		process();
	}

	public DDMForm getDDMForm() {
		return _ddmForm;
	}

	public DDMFormValues getDDMFormValues() {
		return _ddmFormValues;
	}

	protected void addDDMFormDDMFormField(Form.Field field) {
		String fieldName = field.getFieldName();

		if (_ddmFormFieldNames.contains(fieldName)) {
			return;
		}

		DDMFormField ddmFormField = getDDMFormField(field);

		_ddmForm.addDDMFormField(ddmFormField);

		_ddmFormFieldNames.add(fieldName);
	}

	protected void addDDMFormValuesDDMFormFieldValue(Form.Field field) {
		DDMFormFieldValue ddmFormFieldValue = getDDMFormFieldValue(field);

		_ddmFormValues.addDDMFormFieldValue(ddmFormFieldValue);
	}

	protected DDMFormField getDDMFormField(Form.Field field) {
		String name = field.getFieldName();
		String type = field.getType();

		DDMFormField ddmFormField = new DDMFormField(name, type);

		ddmFormField.setDataType(field.getDataType());
		ddmFormField.setIndexType("keyword");

		DDMFormFieldOptions ddmFormFieldOptions = getDDMFormFieldOptions(
			field.getOptionsList());

		ddmFormField.setDDMFormFieldOptions(ddmFormFieldOptions);

		DDMFormFieldValidation ddmFormFieldValidation =
			getDDMFormFieldValidation(field.getValidation());

		ddmFormField.setDDMFormFieldValidation(ddmFormFieldValidation);

		ddmFormField.setReadOnly(field.getReadOnly());
		ddmFormField.setRequired(field.getRequired());
		ddmFormField.setLocalizable(field.getLocalizable());
		ddmFormField.setVisibilityExpression(field.getVisibilityExpression());

		setDDMFormFieldNestedFields(field.getNestedFieldsList(), ddmFormField);

		return ddmFormField;
	}

	protected DDMFormFieldOptions getDDMFormFieldOptions(
		List<Form.Field.Option> options) {

		DDMFormFieldOptions ddmFormFieldOptions = new DDMFormFieldOptions();

		ddmFormFieldOptions.setDefaultLocale(_locale);

		if (options == null) {
			return ddmFormFieldOptions;
		}

		for (Form.Field.Option option : options) {
			ddmFormFieldOptions.addOptionLabel(
				option.getValue(), _locale, option.getLabel());
		}

		return ddmFormFieldOptions;
	}

	protected DDMFormFieldValidation getDDMFormFieldValidation(
		Form.Field.Validation validation) {

		DDMFormFieldValidation ddmFormFieldValidation =
			new DDMFormFieldValidation();

		ddmFormFieldValidation.setErrorMessage(validation.getErrorMessage());
		ddmFormFieldValidation.setExpression(validation.getExpression());

		return ddmFormFieldValidation;
	}

	protected DDMFormFieldValue getDDMFormFieldValue(Form.Field field) {
		DDMFormFieldValue ddmFormFieldValue = new DDMFormFieldValue();

		ddmFormFieldValue.setName(field.getFieldName());
		ddmFormFieldValue.setInstanceId(field.getInstanceId());

		setDDMFormFieldValueValue(
			field.getValue(), field.getLocalizable(), ddmFormFieldValue);

		setDDMFormFieldValueNestedFieldValues(
			field.getNestedFieldsList(), ddmFormFieldValue);

		return ddmFormFieldValue;
	}

	protected List<DDMFormRule> getDDMFormRules(List<Form.Rule> rules) {
		List<DDMFormRule> ddmFormRules = new ArrayList<>();

		for (Form.Rule rule : rules) {
			DDMFormRule ddmFormRule = new DDMFormRule(
				rule.getCondition(), rule.getActionsList());

			ddmFormRules.add(ddmFormRule);
		}

		return ddmFormRules;
	}

	protected LocalizedValue getLocalizedValue(String value) {
		LocalizedValue localizedValue = new LocalizedValue(_locale);

		localizedValue.addString(_locale, value);

		return localizedValue;
	}

	protected void initModels() {
		setDDMFormRules();

		setDDMFormValuesDefaultLocale();
		setDDMFormValuesAvailableLocales();
	}

	protected void process() {
		for (DDMFormEvaluationRequest.Form.Field field :
				_form.getFieldsList()) {

			addDDMFormDDMFormField(field);
			addDDMFormValuesDDMFormFieldValue(field);
		}
	}

	protected void setDDMFormFieldNestedFields(
		List<Form.Field> nestedFields, DDMFormField ddmFormField) {

		for (Form.Field nestedField : nestedFields) {
			DDMFormField nestedDDMFormField = getDDMFormField(nestedField);

			ddmFormField.addNestedDDMFormField(nestedDDMFormField);
		}
	}

	protected void setDDMFormFieldValueNestedFieldValues(
		List<Form.Field> nestedFields, DDMFormFieldValue ddmFormFieldValue) {

		for (Form.Field nestedField : nestedFields) {
			DDMFormFieldValue nestedDDMFormFieldValue = getDDMFormFieldValue(
				nestedField);

			ddmFormFieldValue.addNestedDDMFormFieldValue(
				nestedDDMFormFieldValue);
		}
	}

	protected void setDDMFormFieldValueValue(
		String value, boolean localizable,
		DDMFormFieldValue ddmFormFieldValue) {

		if (localizable) {
			LocalizedValue localizedValue = getLocalizedValue(value);

			ddmFormFieldValue.setValue(localizedValue);
		}
		else {
			ddmFormFieldValue.setValue(new UnlocalizedValue(value));
		}
	}

	protected void setDDMFormRules() {
		List<DDMFormRule> ddmFormRules = getDDMFormRules(_form.getRulesList());

		_ddmForm.setDDMFormRules(ddmFormRules);
	}

	protected void setDDMFormValuesAvailableLocales() {
		_ddmFormValues.addAvailableLocale(_locale);
	}

	protected void setDDMFormValuesDefaultLocale() {
		_ddmFormValues.setDefaultLocale(_locale);
	}

	private final DDMForm _ddmForm;
	private final Set<String> _ddmFormFieldNames = new HashSet<>();
	private final DDMFormValues _ddmFormValues;
	private final DDMFormEvaluationRequest.Form _form;
	private final Locale _locale;

}