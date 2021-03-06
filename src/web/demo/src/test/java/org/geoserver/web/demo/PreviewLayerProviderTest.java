/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.data.test.MockData;
import org.geoserver.web.GeoServerWicketTestSupport;
import org.junit.Test;

public class PreviewLayerProviderTest extends GeoServerWicketTestSupport {

    @Test
    public void testNonAdvertisedLayer() throws Exception {
        String layerId = getLayerId(MockData.BUILDINGS);
        LayerInfo layer = getCatalog().getLayerByName(layerId);
        try {
            // now you see me
            PreviewLayerProvider provider = new PreviewLayerProvider();
            PreviewLayer pl = getPreviewLayer(provider, layerId);
            assertNotNull(pl);
            
            // now you don't!
            layer.setAdvertised(false);
            getCatalog().save(layer);
            pl = getPreviewLayer(provider, layerId);
            assertNull(pl);
        } finally {
            layer.setAdvertised(true);
            getCatalog().save(layer);
        }
    }

    @Test
    public void testSingleLayerGroup() throws Exception {
        String layerId = getLayerId(MockData.BUILDINGS);
        LayerInfo layer = getCatalog().getLayerByName(layerId);
        
        LayerGroupInfo group = getCatalog().getFactory().createLayerGroup();
        group.setName("testSingleLayerGroup");
        group.setMode(LayerGroupInfo.Mode.SINGLE);        
        group.getLayers().add(layer);
        group.setTitle("This is the title");
        group.setAbstract("This is the abstract");
        getCatalog().add(group);
        try {
            PreviewLayerProvider provider = new PreviewLayerProvider();
            PreviewLayer pl = getPreviewLayer(provider, group.prefixedName());
            assertNotNull(pl);
            assertEquals("This is the title", pl.getTitle());
            assertEquals("This is the abstract", pl.getAbstract());
        } finally {
            getCatalog().remove(group);
        }        
    }    
    
    @Test
    public void testContainerLayerGroup() throws Exception {
        String layerId = getLayerId(MockData.BUILDINGS);
        LayerInfo layer = getCatalog().getLayerByName(layerId);
        
        LayerGroupInfo group = getCatalog().getFactory().createLayerGroup();
        group.setName("testContainerLayerGroup");
        group.setMode(LayerGroupInfo.Mode.CONTAINER);        
        group.getLayers().add(layer);
        getCatalog().add(group);
        try {
            PreviewLayerProvider provider = new PreviewLayerProvider();
            PreviewLayer pl = getPreviewLayer(provider, group.prefixedName());
            assertNull(pl);
        } finally {
            getCatalog().remove(group);
        }        
    }

    @Test
    public void testNestedContainerLayerGroup() throws Exception {
        String layerId = getLayerId(MockData.BUILDINGS);
        LayerInfo layer = getCatalog().getLayerByName(layerId);

        LayerGroupInfo containerGroup = getCatalog().getFactory().createLayerGroup();
        containerGroup.setName("testContainerLayerGroup");
        containerGroup.setMode(LayerGroupInfo.Mode.SINGLE);        
        containerGroup.getLayers().add(layer);
        getCatalog().add(containerGroup);           
        
        LayerGroupInfo singleGroup = getCatalog().getFactory().createLayerGroup();
        singleGroup.setName("testSingleLayerGroup");
        singleGroup.setMode(LayerGroupInfo.Mode.SINGLE);        
        singleGroup.getLayers().add(containerGroup);
        getCatalog().add(singleGroup);
           
        try {
            PreviewLayerProvider provider = new PreviewLayerProvider();
            assertNotNull(getPreviewLayer(provider, singleGroup.prefixedName()));
            assertNotNull(getPreviewLayer(provider, layer.prefixedName()));
        } finally {
            getCatalog().remove(singleGroup);
            getCatalog().remove(containerGroup);
        }        
    }    
    
    private PreviewLayer getPreviewLayer(PreviewLayerProvider provider, String prefixedName) {
        for (PreviewLayer pl : provider.getItems()) {
            if(pl.getName().equals(prefixedName)) {
                return pl; 
            }
        }
        return null;
    }
}
