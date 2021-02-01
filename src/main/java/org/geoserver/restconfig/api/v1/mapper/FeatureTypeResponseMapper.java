package org.geoserver.restconfig.api.v1.mapper;

import org.geoserver.restconfig.api.v1.model.FeatureTypeResponse;
import org.geoserver.restconfig.api.v1.model.NamedLink;
import org.geoserver.restconfig.model.catalog.DataStoreInfo;
import org.geoserver.restconfig.model.catalog.FeatureTypeInfo;
import org.geoserver.restconfig.model.catalog.WorkspaceInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface FeatureTypeResponseMapper extends ResourceResponseMapper {

	@Mapping(source = "attributes.attribute", target = "attributes")
	FeatureTypeInfo map(FeatureTypeResponse source);

	public default DataStoreInfo namedLinkToDataStoreInfo(NamedLink namedLink) {
		if (namedLink == null)
			return null;

		DataStoreInfo store = new DataStoreInfo();

		String prefixedName = namedLink.getName();
		int i = prefixedName.indexOf(":");
		if (-1 == i) {
			store.setName(prefixedName);
		} else {
			String workspace = prefixedName.substring(0, i);
			String name = prefixedName.substring(i + 1);
			store.setName(name);
			store.setWorkspace(new WorkspaceInfo().name(workspace));
		}
		return store;
	}

}
