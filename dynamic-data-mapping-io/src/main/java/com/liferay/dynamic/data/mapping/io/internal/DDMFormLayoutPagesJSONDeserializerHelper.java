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

import com.liferay.dynamic.data.mapping.io.DDMFormLayoutJSONDeserializer.DDMFormLayoutColumnJSONObjectTransformer;
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutColumn;
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutPage;
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutRow;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.LocaleUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * @author Marcellus Tavares
 */
public class DDMFormLayoutPagesJSONDeserializerHelper {

	public DDMFormLayoutPagesJSONDeserializerHelper(
		Optional<DDMFormLayoutColumnJSONObjectTransformer>
			ddmFormLayoutColumnJSONObjectTransformerOptional) {

		_ddmFormLayoutColumnJSONObjectTransformer =
			ddmFormLayoutColumnJSONObjectTransformerOptional.orElse(
				_defaultDDMFormLayoutColumnJSONObjectTransformer);
	}

	protected DDMFormLayoutColumn getDDMFormLayoutColumn(
		JSONObject jsonObject) {

		return _ddmFormLayoutColumnJSONObjectTransformer.transform(jsonObject);
	}

	protected List<DDMFormLayoutColumn> getDDMFormLayoutColumns(
		JSONArray jsonArray) {

		List<DDMFormLayoutColumn> ddmFormLayoutColumns = new ArrayList<>();

		for (int i = 0; i < jsonArray.length(); i++) {
			DDMFormLayoutColumn ddmFormLayoutColumn = getDDMFormLayoutColumn(
				jsonArray.getJSONObject(i));

			ddmFormLayoutColumns.add(ddmFormLayoutColumn);
		}

		return ddmFormLayoutColumns;
	}

	protected DDMFormLayoutPage getDDMFormLayoutPage(JSONObject jsonObject) {
		DDMFormLayoutPage ddmFormLayoutPage = new DDMFormLayoutPage();

		setDDMFormLayoutPageDescription(
			jsonObject.getJSONObject("description"), ddmFormLayoutPage);
		setDDMFormLayoutPageRows(
			jsonObject.getJSONArray("rows"), ddmFormLayoutPage);
		setDDMFormLayoutPageTitle(
			jsonObject.getJSONObject("title"), ddmFormLayoutPage);

		return ddmFormLayoutPage;
	}

	protected List<DDMFormLayoutPage> getDDMFormLayoutPages(
		JSONArray jsonArray) {

		List<DDMFormLayoutPage> ddmFormLayoutPages = new ArrayList<>();

		for (int i = 0; i < jsonArray.length(); i++) {
			DDMFormLayoutPage ddmFormLayoutPage = getDDMFormLayoutPage(
				jsonArray.getJSONObject(i));

			ddmFormLayoutPages.add(ddmFormLayoutPage);
		}

		return ddmFormLayoutPages;
	}

	protected DDMFormLayoutRow getDDMFormLayoutRow(JSONObject jsonObject) {
		DDMFormLayoutRow ddmFormLayoutRow = new DDMFormLayoutRow();

		setDDMFormLayoutRowColumns(
			jsonObject.getJSONArray("columns"), ddmFormLayoutRow);

		return ddmFormLayoutRow;
	}

	protected List<DDMFormLayoutRow> getDDMFormLayoutRows(JSONArray jsonArray) {
		List<DDMFormLayoutRow> ddmFormLayoutRows = new ArrayList<>();

		for (int i = 0; i < jsonArray.length(); i++) {
			DDMFormLayoutRow ddmFormLayoutRow = getDDMFormLayoutRow(
				jsonArray.getJSONObject(i));

			ddmFormLayoutRows.add(ddmFormLayoutRow);
		}

		return ddmFormLayoutRows;
	}

	protected LocalizedValue getTitle(JSONObject jsonObject) {
		if (jsonObject == null) {
			return null;
		}

		LocalizedValue title = new LocalizedValue();

		Iterator<String> itr = jsonObject.keys();

		while (itr.hasNext()) {
			String languageId = itr.next();

			title.addString(
				LocaleUtil.fromLanguageId(languageId),
				jsonObject.getString(languageId));
		}

		return title;
	}

	protected void setDDMFormLayoutPageDescription(
		JSONObject jsonObject, DDMFormLayoutPage ddmFormLayoutPage) {

		LocalizedValue description = _getDescription(jsonObject);

		if (description == null) {
			return;
		}

		ddmFormLayoutPage.setDescription(description);
	}

	protected void setDDMFormLayoutPageRows(
		JSONArray jsonArray, DDMFormLayoutPage ddmFormLayoutPage) {

		List<DDMFormLayoutRow> ddmFormLayoutRows = getDDMFormLayoutRows(
			jsonArray);

		ddmFormLayoutPage.setDDMFormLayoutRows(ddmFormLayoutRows);
	}

	protected void setDDMFormLayoutPageTitle(
		JSONObject jsonObject, DDMFormLayoutPage ddmFormLayoutPage) {

		LocalizedValue title = getTitle(jsonObject);

		if (title == null) {
			return;
		}

		ddmFormLayoutPage.setTitle(title);
	}

	protected void setDDMFormLayoutRowColumns(
		JSONArray jsonArray, DDMFormLayoutRow ddmFormLayoutRow) {

		List<DDMFormLayoutColumn> ddmFormLayoutColumns =
			getDDMFormLayoutColumns(jsonArray);

		ddmFormLayoutRow.setDDMFormLayoutColumns(ddmFormLayoutColumns);
	}

	private LocalizedValue _getDescription(JSONObject jsonObject) {
		if (jsonObject == null) {
			return null;
		}

		LocalizedValue description = new LocalizedValue();

		Iterator<String> itr = jsonObject.keys();

		while (itr.hasNext()) {
			String languageId = itr.next();

			description.addString(
				LocaleUtil.fromLanguageId(languageId),
				jsonObject.getString(languageId));
		}

		return description;
	}

	private static final DDMFormLayoutColumnJSONObjectTransformer
		_defaultDDMFormLayoutColumnJSONObjectTransformer =
			new DefaultDDMFormLayoutColumnJSONObjectTransformer();

	private final DDMFormLayoutColumnJSONObjectTransformer
		_ddmFormLayoutColumnJSONObjectTransformer;

	private static class DefaultDDMFormLayoutColumnJSONObjectTransformer
		implements DDMFormLayoutColumnJSONObjectTransformer {

		@Override
		public DDMFormLayoutColumn transform(JSONObject jsonObject) {
			DDMFormLayoutColumn ddmFormLayoutColumn = new DDMFormLayoutColumn(
				jsonObject.getInt("size"));

			setDDMFormLayouColumnFieldNames(
				jsonObject.getJSONArray("fieldNames"), ddmFormLayoutColumn);

			return ddmFormLayoutColumn;
		}

		protected List<String> getDDMFormLayoutColumnFieldNames(
			JSONArray jsonArray) {

			List<String> ddmFormFieldNames = new ArrayList<>();

			for (int i = 0; i < jsonArray.length(); i++) {
				ddmFormFieldNames.add(jsonArray.getString(i));
			}

			return ddmFormFieldNames;
		}

		protected void setDDMFormLayouColumnFieldNames(
			JSONArray jsonArray, DDMFormLayoutColumn ddmFormLayoutColumn) {

			List<String> ddmFormLayoutColumnNames =
				getDDMFormLayoutColumnFieldNames(jsonArray);

			ddmFormLayoutColumn.setDDMFormFieldNames(ddmFormLayoutColumnNames);
		}

	}

}