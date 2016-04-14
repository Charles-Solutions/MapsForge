package com.solutions.mapsforge;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.FileSystemTileCache;
import org.mapsforge.map.layer.cache.InMemoryTileCache;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.cache.TwoLevelTileCache;
import org.mapsforge.map.layer.debug.TileCoordinatesLayer;
import org.mapsforge.map.layer.debug.TileGridLayer;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final GraphicFactory GRAPHIC_FACTORY = AwtGraphicFactory.INSTANCE;
    private static final boolean SHOW_DEBUG_LAYERS = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] args = new String[1];
        args[0] = "file/not/found.map";

        List<File> mapFiles = getMapFiles(args);
        MapView mapView = new MapView(this);
        mapView.getMapScaleBar().setVisible(true);


        Layers layers = mapView.getLayerManager().getLayers();
        BoundingBox result = null;

        for (int i = 0; i < mapFiles.size(); i++) {
            File mapFile = mapFiles.get(i);
            TileRendererLayer tileRendererLayer = createTileRendererLayer(createTileCache(i),
                    mapView.getModel().mapViewPosition, true, true, mapFile);
            BoundingBox boundingBox = tileRendererLayer.getMapDataStore().boundingBox();
            result = result == null ? boundingBox : result.extendBoundingBox(boundingBox);
            layers.add(tileRendererLayer);
        }

        if (SHOW_DEBUG_LAYERS) {
            layers.add(new TileGridLayer(GRAPHIC_FACTORY, mapView.getModel().displayModel));
            layers.add(new TileCoordinatesLayer(GRAPHIC_FACTORY, mapView.getModel().displayModel));
        }


        setContentView(mapView);
       // setContentView(R.layout.activity_main);
    }



    private static List<File> getMapFiles(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("missing argument: <mapFile>");
        }

        List<File> result = new ArrayList<>();
        for(String arg : args) {
            File mapFile = new File(arg);
            if (!mapFile.exists()) {
                throw new IllegalArgumentException("file does not exist: " + mapFile);
            } else if (!mapFile.isFile()) {
                throw new IllegalArgumentException("not a file: " + mapFile);
            } else if (!mapFile.canRead()) {
                throw new IllegalArgumentException("cannot read file: " + mapFile);
            }
            result.add(mapFile);
        }
        return result;
    }

    private static TileRendererLayer createTileRendererLayer(
            TileCache tileCache,
            MapViewPosition mapViewPosition, boolean isTransparent, boolean renderLabels, File mapFile) {
        TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache, new MapFile(mapFile), mapViewPosition, isTransparent,
                renderLabels, GRAPHIC_FACTORY);
        tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
        return tileRendererLayer;
    }

    private static TileCache createTileCache(int index) {
        TileCache firstLevelTileCache = new InMemoryTileCache(128);
        File cacheDirectory = new File(System.getProperty("java.io.tmpdir"), "mapsforge" + index);
        TileCache secondLevelTileCache = new FileSystemTileCache(1024, cacheDirectory, GRAPHIC_FACTORY);
        return new TwoLevelTileCache(firstLevelTileCache, secondLevelTileCache);
    }


}
