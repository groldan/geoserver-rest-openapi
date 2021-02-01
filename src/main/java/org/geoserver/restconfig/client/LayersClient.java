package org.geoserver.restconfig.client;

import java.util.List;
import java.util.Optional;

import org.geoserver.restconfig.api.v1.client.LayersApi;
import org.geoserver.restconfig.api.v1.model.Layer;
import org.geoserver.restconfig.api.v1.model.LayerInfoWrapper;
import org.geoserver.restconfig.api.v1.model.LayerReference;
import org.geoserver.restconfig.api.v1.model.Layers;
import org.geoserver.restconfig.api.v1.model.NamedLink;
import org.geoserver.restconfig.model.catalog.LayerInfo;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LayersClient {

	private @NonNull GeoServerClient client;

	LayersApi api() {
		return client.api(LayersApi.class);
	}

	public Optional<Layer> getLayer(@NonNull String qualifiedLayerName) {
		try {
			return Optional.of(api().getLayer(qualifiedLayerName).getLayer());
		} catch (ServerException.NotFound nf) {
			return Optional.empty();
		}
	}

	public Optional<Layer> getLayer(@NonNull String workspace, @NonNull String layerName) {
		try {
			return Optional.of(api().getLayerByWorkspace(workspace, layerName, Boolean.TRUE).getLayer());
		} catch (ServerException.NotFound nf) {
			return Optional.empty();
		}
	}

	public List<NamedLink> getLayers() {
		Layers layers = api().getLayers();
		LayerReference ref = layers.getLayers();
		return ref.getLayer();
	}

	public List<NamedLink> getLayers(@NonNull String workspaceName) {
		Layers layers = api().getLayersByWorkspace(workspaceName);
		LayerReference ref = layers.getLayers();
		return ref.getLayer();
	}

	public void deleteLayer(@NonNull String qualifiedLayerName) {
		boolean recurse = true;
		api().deleteLayer(qualifiedLayerName, recurse);
	}

	public void deleteLayer(@NonNull String workspaceName, @NonNull String layerName) {
		boolean recurse = true;
		api().deleteLayerByWorkspace(workspaceName, layerName, recurse);
	}

	public void updateLayer(@NonNull String workspaceName, @NonNull String layerName, @NonNull LayerInfo layerInfo) {
		api().updateLayerByWorkspace(workspaceName, layerName, new LayerInfoWrapper().layer(layerInfo));
	}
}
