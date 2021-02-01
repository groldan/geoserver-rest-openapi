package org.geoserver.restconfig.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.geoserver.restconfig.api.v1.model.Layer;
import org.geoserver.restconfig.api.v1.model.Layer.TypeEnum;
import org.geoserver.restconfig.api.v1.model.NamedLink;
import org.geoserver.restconfig.api.v1.model.WorkspaceSummary;
import org.geoserver.restconfig.model.catalog.DataStoreInfo;
import org.geoserver.restconfig.model.catalog.FeatureTypeInfo;
import org.geoserver.restconfig.model.catalog.LayerInfo;
import org.geoserver.restconfig.model.catalog.StyleInfo;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

public class LayersClientIT {

	public static @ClassRule IntegrationTestSupport support = new IntegrationTestSupport();

	public @Rule TestName testName = new TestName();
	public @Rule ExpectedException ex = ExpectedException.none();

	private SecureRandom rnd = new SecureRandom();

	// two workspaces, both have a "roads" and a "streams" shapefile datastore
	private WorkspaceSummary ws;

	private URI roadsShapefile;

	private URI streamsShapefile;

	private LayersClient layers;

	private FeatureTypeInfo roadsFT;
	private FeatureTypeInfo streamsFT;

	public @Before void before() {
		Assume.assumeTrue(support.isAlive());
		WorkspacesClient workspaces = support.client().workspaces();
		DataStoresClient dataStores = support.client().dataStores();
		FeatureTypesClient featureTyes = support.client().featureTypes();
		this.layers = support.client().layers();

		String wsname = String.format("%s-ws1-%d", testName.getMethodName(), rnd.nextInt((int) 1e6));

		workspaces.create(wsname);
		this.ws = workspaces.findByName(wsname).get();

		this.roadsShapefile = support.getRoadsShapefile();
		this.streamsShapefile = support.getStreamsShapefile();

		dataStores.createShapefileDataStore(wsname, "roadsStore", roadsShapefile);
		dataStores.createShapefileDataStore(wsname, "streamsStore", streamsShapefile);

		this.roadsShapefile = support.getRoadsShapefile();
		this.streamsShapefile = support.getStreamsShapefile();

		// 2.15.2 fails if nativeName is not set, gets it deserialized as the literal
		// string "null"
		roadsFT = new FeatureTypeInfo().name("roads").nativeName("roads").store(new DataStoreInfo().name("roadsStore"));
		streamsFT = new FeatureTypeInfo().name("streams").nativeName("streams")
				.store(new DataStoreInfo().name("streamsStore"));

		featureTyes.create(ws.getName(), roadsFT);
		featureTyes.create(ws.getName(), streamsFT);

	}

	public @After void after() {
		if (this.ws != null) {
			support.client().workspaces().deleteRecursively(this.ws.getName());
		}
	}

	public @Test void getLayers() {
		List<NamedLink> list = layers.getLayers(ws.getName());
		assertNotNull(list);
		assertEquals(2, list.size());
		List<String> layerNames = list.stream().map(NamedLink::getName).collect(Collectors.toList());
		assertTrue(layerNames.toString(), layerNames.contains(roadsFT.getName()));
		assertTrue(layerNames.toString(), layerNames.contains(streamsFT.getName()));
	}

	public @Test void getLayerByName() {
		Optional<Layer> layerResponse = layers.getLayer(ws.getName(), roadsFT.getName());
		assertNotNull(layerResponse);
		Layer layer = layerResponse.get();
		assertNotNull(layer);
		assertEquals(TypeEnum.VECTOR, layer.getType());
		assertEquals(roadsFT.getName(), layer.getName());
		assertNotNull(layer.getDefaultStyle());
		assertNotNull(layer.getResource());
		NamedLink resource = layer.getResource();
		assertEquals(ws.getName() + ":" + roadsFT.getName(), resource.getName());

		layerResponse = layers.getLayer(ws.getName(), "nonExistingLayer");
		assertNotNull(layerResponse);
		assertFalse(layerResponse.isPresent());
	}

	@Ignore // NEED TO USE SLD STYLE
	public @Test void updateLayerSetDefaultStyle() throws IOException {
		StylesClient styles = support.client().styles();
		String styleBody = "{\n" + //
				"  \"version\": 8,\n" + //
				"  \"name\": \"violet polygon\",\n" + //
				"  \"layers\": [\n" + //
				"      {\n" + //
				"          \"id\": \"violet polygon\",\n" + //
				"          \"type\": \"fill\",\n" + //
				"          \"source-layer\": \"roads\",\n" + //
				"          \"paint\": {\n" + //
				"              \"fill-color\": \"#3300ff\",\n" + //
				"              \"fill-outline-color\":\"#000000\"\n" + //
				"          }\n" + //
				"      }, {\n" + //
				"          \"id\": \"red polygon\",\n" + //
				"          \"type\": \"fill\",\n" + //
				"          \"source-layer\": \"roads\",\n" + //
				"          \"paint\": {\n" + //
				"              \"fill-color\": \"#aa0000\",\n" + //
				"              \"fill-outline-color\":\"#000000\"\n" + //
				"          }\n" + //
				"      }\n" + //
				"  ]\n" + //
				"}";
		StyleInfo style = styles.createMapboxStyle(ws.getName(), "roads-style", styleBody);

		LayerInfo info = new LayerInfo();
		info.setName(roadsFT.getName());
		info.setDefaultStyle(style);
		layers.updateLayer(ws.getName(), info.getName(), info);

		Layer layer = layers.getLayer(ws.getName(), info.getName()).get();

		String expected = ws.getName() + ":" + style.getName();
		assertEquals(expected, layer.getDefaultStyle().getName());
	}
}