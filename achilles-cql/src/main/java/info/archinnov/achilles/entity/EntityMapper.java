/**
 *
 * Copyright (C) 2012-2013 DuyHai DOAN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.archinnov.achilles.entity;

import info.archinnov.achilles.entity.metadata.EntityMeta;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.reflection.RowMethodInvoker;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.Row;

public class EntityMapper  {

    private static final Logger log  = LoggerFactory.getLogger(EntityMapper.class);

    private RowMethodInvoker cqlRowInvoker = new RowMethodInvoker();

	public void setEagerPropertiesToEntity(Row row, EntityMeta entityMeta, Object entity) {
        log.debug("Set eager properties to entity class {} from fetched CQL row", entityMeta.getClassName());
		for (PropertyMeta pm : entityMeta.getEagerMetas()) {
			setPropertyToEntity(row, pm, entity);
		}
	}

	public void setPropertyToEntity(Row row, PropertyMeta pm, Object entity) {
        log.debug("Set property {} value from fetched CQL row", pm.getPropertyName());
		if (row != null) {
			if (pm.isEmbeddedId()) {
				Object compoundKey = cqlRowInvoker.extractCompoundPrimaryKeyFromRow(row, pm, true);
				pm.setValueToField(entity, compoundKey);
			} else {
				String propertyName = pm.getPropertyName();
				if (!row.isNull(propertyName)) {
					Object value = cqlRowInvoker.invokeOnRowForFields(row, pm);
					pm.setValueToField(entity, value);
				}
			}
		}
	}

	public <T> T mapRowToEntityWithPrimaryKey(EntityMeta meta, Row row,
                                              Map<String, PropertyMeta> propertiesMap, boolean isEntityManaged) {
        log.debug("Map CQL row to entity of class {}", meta.getClassName());
        T entity = null;
		ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
		if (columnDefinitions != null) {
			entity = meta.instanciate();
			for (Definition column : columnDefinitions) {
				String columnName = column.getName();
				PropertyMeta pm = propertiesMap.get(columnName);
				if (pm != null) {
					Object value = cqlRowInvoker.invokeOnRowForFields(row, pm);
					pm.setValueToField(entity, value);
				}
			}
			PropertyMeta idMeta = meta.getIdMeta();
			if (idMeta.isEmbeddedId()) {
				Object compoundKey = cqlRowInvoker.extractCompoundPrimaryKeyFromRow(row, idMeta, isEntityManaged);
				idMeta.setValueToField(entity, compoundKey);
			}
		}
		return entity;
	}

}
