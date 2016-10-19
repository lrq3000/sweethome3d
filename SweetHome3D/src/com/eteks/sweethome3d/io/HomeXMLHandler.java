/*
 * HomeXMLHandler.java 15 sept. 2016
 *
 * Sweet Home 3D, Copyright (c) 2016 Emmanuel PUYBARET / eTeks <info@eteks.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.eteks.sweethome3d.io;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.eteks.sweethome3d.model.AspectRatio;
import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.Baseboard;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.CatalogDoorOrWindow;
import com.eteks.sweethome3d.model.CatalogLight;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.Compass;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomeLight;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomeObject;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomePrint;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.LightSource;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.Polyline;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Sash;
import com.eteks.sweethome3d.model.TextStyle;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.tools.ResourceURLContent;

/**
 * SAX handler for Sweet Home 3D XML stream. Read home should respect the following DTD:<pre> 
 * &lt;!ELEMENT home (property*, furnitureVisibleProperty*, environment?, backgroundImage?, print?, compass?, (camera | observerCamera)*, level*, 
 *       (pieceOfFurniture | doorOrWindow | furnitureGroup | light)*, wall*, room*, polyline*, dimensionLine*, label*)>
 * &lt;!ATTLIST home 
 *       version CDATA #IMPLIED
 *       name CDATA #IMPLIED
 *       camera (observerCamera | topCamera) "topCamera"
 *       selectedLevel CDATA #IMPLIED
 *       wallHeight CDATA #IMPLIED
 *       basePlanLocked (false | true) "false"
 *       furnitureSortedProperty CDATA #IMPLIED
 *       furnitureDescendingSorted (false | true) "false">
 *  
 * &lt;!ELEMENT property EMPTY> 
 * &lt;!ATTLIST property 
 *       name CDATA #REQUIRED
 *       value CDATA #REQUIRED>
 * 
 * &lt;!ELEMENT furnitureVisibleProperty EMPTY> 
 * &lt;!ATTLIST furnitureVisibleProperty name CDATA #REQUIRED>
 * 
 * &lt;!ELEMENT environment ((camera | observerCamera)*, texture?, texture?) >
 * &lt;!ATTLIST environment 
 *       groundColor CDATA #IMPLIED
 *       skyColor CDATA #IMPLIED
 *       lightColor CDATA #IMPLIED
 *       wallsAlpha CDATA "0"
 *       allLevelsVisible (false | true) "false"
 *       observerCameraElevationAdjusted (false | true) "true"
 *       ceillingLightColor CDATA #IMPLIED
 *       drawingMode (FILL | OUTLINE | FILL_AND_OUTLINE) "FILL"
 *       subpartSizeUnderLight CDATA "0"
 *       photoWidth CDATA "400"
 *       photoHeight CDATA "300"
 *       photoAspectRatio (FREE_RATIO | VIEW_3D_RATIO | RATIO_4_3 | RATIO_3_2 | RATIO_16_9 | RATIO_2_1 | SQUARE_RATIO) "VIEW_3D_RATIO"
 *       photoQuality CDATA "0"
 *       videoWidth CDATA "320"
 *       videoAspectRatio (RATIO_4_3 | RATIO_16_9) "RATIO_4_3"
 *       videoQuality CDATA "0"
 *       videoFrameRate CDATA "25">
 * 
 * &lt;!ELEMENT backgroundImage EMPTY>
 * &lt;!ATTLIST backgroundImage 
 *       image CDATA #REQUIRED
 *       scaleDistance CDATA #REQUIRED
 *       scaleDistanceXStart CDATA #REQUIRED
 *       scaleDistanceYStart CDATA #REQUIRED
 *       scaleDistanceXEnd CDATA #REQUIRED
 *       scaleDistanceYEnd CDATA #REQUIRED
 *       xOrigin CDATA "0"
 *       yOrigin CDATA "0"
 *       visible (false | true) "true">
 *  
 * &lt;!ELEMENT print EMPTY>
 * &lt;!ATTLIST print 
 *       headerFormat CDATA #IMPLIED
 *       footerFormat CDATA #IMPLIED
 *       planScale CDATA #IMPLIED
 *       furniturePrinted (false | true) "true" 
 *       planPrinted (false | true) "true" 
 *       view3DPrinted (false | true) "true" 
 *       paperWidth CDATA #REQUIRED
 *       paperHeight CDATA #REQUIRED
 *       paperTopMargin CDATA #REQUIRED
 *       paperLeftMargin CDATA #REQUIRED
 *       paperBottomMargin CDATA #REQUIRED
 *       paperRightMargin CDATA #REQUIRED
 *       paperOrientation (PORTRAIT | LANDSCAPE | REVERSE_LANDSCAPE) #REQUIRED>
 * 
 * &lt;!ELEMENT compass (property*)>
 * &lt;!ATTLIST compass 
 *       x CDATA #REQUIRED
 *       y CDATA #REQUIRED
 *       diameter CDATA #REQUIRED
 *       northDirection CDATA "0"
 *       longitude CDATA #IMPLIED
 *       latitude CDATA #IMPLIED
 *       timeZone CDATA #IMPLIED
 *       visible (false | true) "true">
 * 
 * &lt;!ENTITY % cameraCommonAttributes 
 *      'name CDATA #IMPLIED
 *       lens (PINHOLE | NORMAL | FISHEYE | SPHERICAL) "PINHOLE"
 *       x CDATA #REQUIRED
 *       y CDATA #REQUIRED
 *       z CDATA #REQUIRED
 *       yaw CDATA #REQUIRED
 *       pitch CDATA #REQUIRED
 *       time CDATA #IMPLIED
 *       fieldOfView CDATA #REQUIRED'>
 *
 * &lt;!ELEMENT camera (property*)>
 * &lt;!ATTLIST camera 
 *       %cameraCommonAttributes;
 *       attribute (topCamera | storedCamera | cameraPath) #REQUIRED>
 * 
 * &lt;!ELEMENT observerCamera (property*)>
 * &lt;!ATTLIST observerCamera 
 *       %cameraCommonAttributes;
 *       attribute (observerCamera | storedCamera | cameraPath) #REQUIRED
 *       fixedSize (false | true) "false">
 * 
 * &lt;!ELEMENT level (property*, backgroundImage?)>
 * &lt;!ATTLIST level 
 *       id ID #REQUIRED
 *       name CDATA #REQUIRED
 *       elevation CDATA #REQUIRED
 *       floorThickness CDATA #REQUIRED
 *       height CDATA #REQUIRED
 *       elevationIndex CDATA "-1"
 *       visible (false | true) "true"
 *       viewable (false | true) "true">
 * 
 * &lt;!ENTITY % furnitureCommonAttributes
 *      'name CDATA #REQUIRED
 *       angle CDATA "0"
 *       visible (false | true) "true"
 *       movable (false | true) "true"
 *       description CDATA #IMPLIED
 *       modelMirrored (false | true) "false"
 *       nameVisible (false | true) "false"
 *       nameAngle CDATA "0"
 *       nameXOffset CDATA "0"
 *       nameYOffset CDATA "0"
 *       price CDATA #IMPLIED'>
 *  
 * &lt;!ELEMENT furnitureGroup ((pieceOfFurniture | doorOrWindow | furnitureGroup | light)*, property*, textStyle?)>
 * &lt;!ATTLIST furnitureGroup 
 *       %furnitureCommonAttributes;
 *       level IDREF #IMPLIED 
 *       x CDATA #IMPLIED
 *       y CDATA #IMPLIED
 *       elevation CDATA #IMPLIED
 *       width CDATA #IMPLIED
 *       depth CDATA #IMPLIED
 *       height CDATA #IMPLIED
 *       dropOnTopElevation CDATA #IMPLIED>
 * 
 * &lt;!ENTITY % pieceOfFurnitureCommonAttributes
 *      'level IDREF #IMPLIED
 *       catalogId CDATA #IMPLIED
 *       x CDATA #REQUIRED
 *       y CDATA #REQUIRED
 *       elevation CDATA "0"
 *       width CDATA #REQUIRED
 *       depth CDATA #REQUIRED
 *       height CDATA #REQUIRED
 *       dropOnTopElevation CDATA "1"
 *       information CDATA #IMPLIED
 *       model CDATA #IMPLIED
 *       icon CDATA #IMPLIED
 *       planIcon CDATA #IMPLIED
 *       modelRotation CDATA "1 0 0 0 1 0 0 0 1"
 *       backFaceShown (false | true) "false"
 *       doorOrWindow (false | true) "false"
 *       resizable (false | true) "true"
 *       deformable (false | true) "true"
 *       texturable (false | true) "true"
 *       staircaseCutOutShape CDATA #IMPLIED
 *       color CDATA #IMPLIED
 *       shininess CDATA #IMPLIED
 *       creator CDATA #IMPLIED
 *       valueAddedTaxPercentage CDATA #IMPLIED
 *       currency CDATA #IMPLIED'>
 * 
 * &lt;!ELEMENT pieceOfFurniture (property*, textStyle?, texture?, material*)>
 * &lt;!ATTLIST pieceOfFurniture 
 *       %furnitureCommonAttributes;
 *       %pieceOfFurnitureCommonAttributes;>
 * 
 * &lt;!ELEMENT doorOrWindow (sash*, property*, textStyle?, texture?, material*)>
 * &lt;!ATTLIST doorOrWindow 
 *       %furnitureCommonAttributes;
 *       %pieceOfFurnitureCommonAttributes;
 *       wallThickness CDATA "1"
 *       wallDistance CDATA "0"
 *       cutOutShape CDATA #IMPLIED
 *       boundToWall (false | true) "true">
 * 
 * &lt;!ELEMENT sash EMPTY>
 * &lt;!ATTLIST sash 
 *       xAxis CDATA #REQUIRED
 *       yAxis CDATA #REQUIRED
 *       width CDATA #REQUIRED
 *       startAngle CDATA #REQUIRED
 *       endAngle CDATA #REQUIRED>
 *
 * &lt;!ELEMENT light (lightSource*, property*, textStyle?, texture?, material*)>
 * &lt;!ATTLIST light 
 *       %furnitureCommonAttributes;
 *       %pieceOfFurnitureCommonAttributes;
 *       power CDATA "0.5">
 * 
 * &lt;!ELEMENT lightSource EMPTY>
 * &lt;!ATTLIST lightSource 
 *       x CDATA #REQUIRED
 *       y CDATA #REQUIRED
 *       z CDATA #REQUIRED
 *       color CDATA #REQUIRED
 *       diameter CDATA #IMPLIED>
 *       
 * &lt;!ELEMENT textStyle EMPTY>
 * &lt;!ATTLIST textStyle 
 *       attribute (nameStyle | areaStyle | lengthStyle) #IMPLIED
 *       fontName CDATA #IMPLIED
 *       fontSize CDATA #REQUIRED
 *       bold (false | true) "false"
 *       italic (false | true) "false">
 *       
 * &lt;!ELEMENT texture EMPTY>
 * &lt;!ATTLIST texture 
 *       attribute (groundTexture | skyTexture | leftSideTexture | rightSideTexture | floorTexture | ceilingTexture) #IMPLIED
 *       catalogId CDATA #IMPLIED
 *       name CDATA #REQUIRED
 *       width CDATA #REQUIRED
 *       height CDATA #REQUIRED
 *       angle CDATA "0"
 *       leftToRightOriented (true | false) "true"
 *       image CDATA #REQUIRED>
 *       
 * &lt;!ELEMENT material (texture?)>
 * &lt;!ATTLIST material 
 *       name CDATA #REQUIRED
 *       key CDATA #IMPLIED
 *       color CDATA #IMPLIED
 *       shininess CDATA #IMPLIED>
 *       
 * &lt;!ELEMENT wall (property*, texture?, texture?, baseboard?, baseboard?)>
 * &lt;!ATTLIST wall 
 *       id ID #REQUIRED
 *       level IDREF #IMPLIED
 *       wallAtStart IDREF #IMPLIED
 *       wallAtEnd IDREF #IMPLIED
 *       xStart CDATA #REQUIRED
 *       yStart CDATA #REQUIRED
 *       xEnd CDATA #REQUIRED
 *       yEnd CDATA #REQUIRED
 *       height CDATA #IMPLIED
 *       heightAtEnd CDATA #IMPLIED
 *       thickness CDATA #REQUIRED
 *       arcExtent CDATA #IMPLIED
 *       pattern CDATA #IMPLIED
 *       topColor CDATA #IMPLIED
 *       leftSideColor CDATA #IMPLIED
 *       leftSideShininess CDATA "0"
 *       rightSideColor CDATA #IMPLIED
 *       rightSideShininess CDATA "0">
 *       
 * &lt;!ELEMENT baseboard (texture?)>
 * &lt;!ATTLIST baseboard 
 *       attribute (leftSideBaseboard | rightSideBaseboard) #REQUIRED
 *       thickness CDATA #REQUIRED
 *       height CDATA #REQUIRED
 *       color CDATA #IMPLIED>
 *       
 * &lt;!ELEMENT room (property*, textStyle?, textStyle?, texture?, texture?, point+)>
 * &lt;!ATTLIST room 
 *       level IDREF #IMPLIED
 *       name CDATA #IMPLIED
 *       nameAngle CDATA "0"
 *       nameXOffset CDATA "0"
 *       nameYOffset CDATA "0"
 *       areaVisible (false | true) "false"
 *       areaAngle CDATA "0"
 *       areaXOffset CDATA "0"
 *       areaYOffset CDATA "0"
 *       floorVisible (false | true) "true"
 *       floorColor CDATA #IMPLIED
 *       floorShininess CDATA "0"
 *       ceilingVisible (false | true) "true"
 *       ceilingColor CDATA #IMPLIED
 *       ceilingShininess CDATA "0">
 *       
 * &lt;!ELEMENT point EMPTY> 
 * &lt;!ATTLIST point 
 *       x CDATA #REQUIRED
 *       y CDATA #REQUIRED>
 *       
 * &lt;!ELEMENT polyline (property*, point+)>
 * &lt;!ATTLIST polyline 
 *       level IDREF #IMPLIED
 *       thickness CDATA "1"
 *       capStyle (BUTT | SQUARE | ROUND) "BUTT"
 *       joinStyle (BEVEL | MITER | ROUND | CURVED) "MITER"
 *       dashStyle (SOLID | DOT | DASH | DASH_DOT | DASH_DOT_DOT) "SOLID"
 *       startArrowStyle (NONE | DELTA | OPEN | DISC) "NONE"
 *       endArrowStyle (NONE | DELTA | OPEN | DISC) "NONE"
 *       color CDATA #IMPLIED
 *       closedPath (false | true) "false">
 *       
 * &lt;!ELEMENT dimensionLine (property*, textStyle?)>
 * &lt;!ATTLIST dimensionLine 
 *       level IDREF #IMPLIED
 *       xStart CDATA #REQUIRED
 *       yStart CDATA #REQUIRED
 *       xEnd CDATA #REQUIRED
 *       yEnd CDATA #REQUIRED
 *       offset CDATA #REQUIRED>
 *       
 * &lt;!ELEMENT label (property*, textStyle?, text)>
 * &lt;!ATTLIST label 
 *       level IDREF #IMPLIED
 *       x CDATA #REQUIRED
 *       y CDATA #REQUIRED
 *       angle CDATA "0"
 *       elevation CDATA "0"
 *       pitch CDATA #IMPLIED
 *       color CDATA #IMPLIED
 *       outlineColor CDATA #IMPLIED>
 *       
 * &lt;!ELEMENT text (#PCDATA)>
 * </pre>
 * with <code>home</code> as root element. 
 * Attributes named <code>attribute</code> indicate the names of the fields 
 * where some elements should be stored.
 * @author Emmanuel Puybaret
 */
public class HomeXMLHandler extends DefaultHandler {
  private HomeContentContext contentContext;
  private UserPreferences    preferences;
  private Home               home;
  
  private final StringBuilder     buffer  = new StringBuilder();
  private final Stack<String>     elements = new Stack<String>();
  private final Stack<Map<String, String>> attributes = new Stack<Map<String, String>>();
  private final Stack<List<HomePieceOfFurniture>> groupsFurniture = new Stack<List<HomePieceOfFurniture>>();
  private final Map<String, Level>      levels = new HashMap<String, Level>();
  private final Map<String, JoinedWall> joinedWalls  = new HashMap<String, JoinedWall>();
  
  private String homeElementName;
  private String labelText;
  private Baseboard leftSideBaseboard;
  private Baseboard rightSideBaseboard;
  private BackgroundImage homeBackgroundImage;
  private BackgroundImage backgroundImage;
  private final Map<String, String> homeProperties = new LinkedHashMap<String, String>();
  private final Map<String, String> properties = new LinkedHashMap<String, String>();
  private final Map<String, TextStyle>    textStyles = new HashMap<String, TextStyle>();
  private final Map<String, HomeTexture>  textures = new HashMap<String, HomeTexture>();
  private final List<HomeMaterial> materials = new ArrayList<HomeMaterial>();
  private HomeTexture materialTexture;
  private final List<Sash>         sashes = new ArrayList<Sash>();
  private final List<LightSource>  lightSources = new ArrayList<LightSource>();
  private final List<float[]>      points = new ArrayList<float[]>();
  private final List<HomePieceOfFurniture.SortableProperty> furnitureVisibleProperties = new ArrayList<HomePieceOfFurniture.SortableProperty>();
  
  public HomeXMLHandler() {
    this(null);
  }

  public HomeXMLHandler(UserPreferences preferences) {
    this.preferences = preferences != null ? preferences : new DefaultUserPreferences(false, null);
  }
  
  /**
   * Sets the context that will be used to lookup content referenced by the read home.
   */
  void setContentContext(HomeContentContext contentContext) {
    this.contentContext = contentContext;
  }
  
  @Override
  public void startDocument() throws SAXException {
    this.home = null;
    this.elements.clear();
    this.attributes.clear();
    this.groupsFurniture.clear();
    this.levels.clear();
    this.joinedWalls.clear();
  }

  @Override
  public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
    this.buffer.setLength(0);
    this.elements.push(name);
    Map<String, String> attributesMap = new HashMap<String, String>();
    for (int i = 0; i < attributes.getLength(); i++) {
      attributesMap.put(attributes.getQName(i), attributes.getValue(i));
    }
    this.attributes.push(attributesMap);
    
    if ("home".equals(name)) {
      setHome(createHome(attributesMap));
      this.homeProperties.clear();
      this.furnitureVisibleProperties.clear();
      this.homeBackgroundImage = null;
    } else if ("environment".equals(name)) {
      this.textures.clear();
    } else if ("compass".equals(name)) {
      this.properties.clear();
    } else if ("level".equals(name)) {
      this.properties.clear();
      this.backgroundImage = null;
    } else if ("pieceOfFurniture".equals(name)
              || "doorOrWindow".equals(name)
              || "light".equals(name)
              || "furnitureGroup".equals(name)) {
      this.properties.clear();
      this.textStyles.clear();
      this.textures.clear();
      this.materials.clear();
      if ("doorOrWindow".equals(name)) {
        this.sashes.clear();
      } else if ("light".equals(name)) {
        this.lightSources.clear();
      } else if ("furnitureGroup".equals(name)) {
        this.groupsFurniture.push(new ArrayList<HomePieceOfFurniture>());
      }
    } else if ("camera".equals(name)
        || "observerCamera".equals(name)) {
      this.properties.clear();
    } else if ("room".equals(name)) {
      this.properties.clear();
      this.textStyles.clear();
      this.textures.clear();
      this.points.clear();
    } else if ("polyline".equals(name)) {
      this.properties.clear();
      this.points.clear();
    } else if ("dimensionLine".equals(name)) {
      this.properties.clear();
      this.textStyles.clear();
    } else if ("label".equals(name)) {
      this.properties.clear();
      this.textStyles.clear();
      this.labelText = null;
    } else if ("wall".equals(name)) {
      this.properties.clear();
      this.textures.clear();
      this.leftSideBaseboard = null;
      this.rightSideBaseboard = null;
    } else if ("baseboard".equals(name)) {
      this.textures.remove(null);
    } else if ("material".equals(name)) {
      this.materialTexture = null;
    }
  }

  @Override
  public void characters(char [] ch, int start, int length) throws SAXException {
    this.buffer.append(ch, start, length);
  }

  @Override
  public void endElement(String uri, String localName, String name) throws SAXException {
    this.elements.pop();
    String parent = this.elements.isEmpty() ? null : this.elements.peek();
    Map<String, String> attributesMap = this.attributes.pop();
    if (this.homeElementName != null && this.homeElementName.equals(name)) {
      setHomeAttributes(home, attributesMap);
    } else if ("furnitureVisibleProperty".equals(name)) {
      try {
        this.furnitureVisibleProperties.add(HomePieceOfFurniture.SortableProperty.valueOf(attributesMap.get("name")));
      } catch (IllegalArgumentException ex) {
        // Ignore malformed enum constant 
      }
    } else if ("environment".equals(name)) {
      setEnvironmentAttributes(this.home.getEnvironment(), attributesMap);
    } else if ("compass".equals(name)) {
      setCompassAttributes(this.home.getCompass(), attributesMap);
    } else if ("print".equals(name)) {
      this.home.setPrint(createPrint(attributesMap));
    } else if ("level".equals(name)) {
      Level level = createLevel(attributesMap);
      setLevelAttributes(level, attributesMap);
      this.levels.put(attributesMap.get("id"), level);
      this.home.addLevel(level);
    } else if ("camera".equals(name)
        || "observerCamera".equals(name)) {
      Camera camera = createCamera(name, attributesMap);
      setCameraAttributes(camera, attributesMap);
      String attribute = attributesMap.get("attribute");
      if ("cameraPath".equals(attribute)) {
        // Update camera path
        List<Camera> cameraPath = new ArrayList<Camera>(this.home.getEnvironment().getVideoCameraPath());
        cameraPath.add(camera);
        this.home.getEnvironment().setVideoCameraPath(cameraPath);
      } else if ("topCamera".equals(attribute)) {
        Camera topCamera = this.home.getTopCamera();
        topCamera.setCamera(camera);
        topCamera.setTime(camera.getTime());
        topCamera.setLens(camera.getLens());
      } else if ("observerCamera".equals(attribute)) {
        ObserverCamera observerCamera = this.home.getObserverCamera();
        observerCamera.setCamera(camera);
        observerCamera.setTime(camera.getTime());
        observerCamera.setLens(camera.getLens());
        observerCamera.setFixedSize(((ObserverCamera)camera).isFixedSize());
      } else if ("storedCamera".equals(attribute)) {
        List<Camera> storedCameras = new ArrayList<Camera>(this.home.getStoredCameras());
        storedCameras.add(camera);
        this.home.setStoredCameras(storedCameras);
      } 
    } else if ("pieceOfFurniture".equals(name)
        || "doorOrWindow".equals(name)
        || "light".equals(name)
        || "furnitureGroup".equals(name)) {
      HomePieceOfFurniture piece = "furnitureGroup".equals(name)
          ? createFurnitureGroup(attributesMap, this.groupsFurniture.pop())
          : createPieceOfFurniture(name, attributesMap);
      setPieceOfFurnitureAttributes(piece, attributesMap);
      if (this.homeElementName != null && this.homeElementName.equals(parent)) {
        this.home.addPieceOfFurniture(piece);
        String levelId = attributesMap.get("level");
        if (levelId != null) {
          piece.setLevel(this.levels.get(levelId));
        }
      } else if ("furnitureGroup".equals(parent)) {
        this.groupsFurniture.peek().add(piece);
      }
    } else if ("wall".equals(name)) {
      Wall wall = createWall(attributesMap);
      this.joinedWalls.put(attributesMap.get("id"), 
          new JoinedWall(wall, attributesMap.get("wallAtStart"), attributesMap.get("wallAtEnd")));
      setWallAttributes(wall, attributesMap);
      this.home.addWall(wall);
      String levelId = attributesMap.get("level");
      if (levelId != null) {
        wall.setLevel(this.levels.get(levelId));
      }
    } else if ("baseboard".equals(name)) {
      Baseboard baseboard = createBaseboard(attributesMap);
      if ("leftSideBaseboard".equals(attributesMap.get("attribute"))) {
        this.leftSideBaseboard = baseboard;
      } else {
        this.rightSideBaseboard = baseboard;
      }
    } else if ("room".equals(name)) {
      Room room = createRoom(attributesMap, this.points.toArray(new float [this.points.size()][]));
      setRoomAttributes(room, attributesMap);
      this.home.addRoom(room);
      String levelId = attributesMap.get("level");
      if (levelId != null) {
        room.setLevel(this.levels.get(levelId));
      }
    } else if ("polyline".equals(name)) {
      Polyline polyline = createPolyline(attributesMap, this.points.toArray(new float [this.points.size()][]));
      setPolylineAttributes(polyline, attributesMap);
      this.home.addPolyline(polyline);
      String levelId = attributesMap.get("level");
      if (levelId != null) {
        polyline.setLevel(this.levels.get(levelId));
      }
    } else if ("dimensionLine".equals(name)) {
      DimensionLine dimensionLine = createDimensionLine(attributesMap);
      setDimensionLineAttributes(dimensionLine, attributesMap);
      this.home.addDimensionLine(dimensionLine);
      String levelId = attributesMap.get("level");
      if (levelId != null) {
        dimensionLine.setLevel(this.levels.get(levelId));
      }
    } else if ("label".equals(name)) {
      Label label = createLabel(attributesMap, this.labelText);
      setLabelAttributes(label, attributesMap);
      this.home.addLabel(label);
      String levelId = attributesMap.get("level");
      if (levelId != null) {
        label.setLevel(this.levels.get(levelId));
      }      
    } else if ("text".equals(name)) {
      this.labelText = getCharacters();
    } else if ("textStyle".equals(name)) {
      this.textStyles.put(attributesMap.get("attribute"), createTextStyle(attributesMap));
    } else if ("texture".equals(name)) {
      if ("material".equals(parent)) {
        this.materialTexture = createTexture(attributesMap); 
      } else {
        this.textures.put(attributesMap.get("attribute"), createTexture(attributesMap));
      }
    } else if ("material".equals(name)) {
      this.materials.add(createMaterial(attributesMap));
    } else if ("point".equals(name)) {
      this.points.add(new float [] {
          Float.parseFloat(attributesMap.get("x")), 
          Float.parseFloat(attributesMap.get("y"))});
    } else if ("sash".equals(name)) {
      this.sashes.add(new Sash(
          Float.parseFloat(attributesMap.get("xAxis")), 
          Float.parseFloat(attributesMap.get("yAxis")),
          Float.parseFloat(attributesMap.get("width")),
          Float.parseFloat(attributesMap.get("startAngle")),
          Float.parseFloat(attributesMap.get("endAngle"))));
    } else if ("lightSource".equals(name)) {
      this.lightSources.add(new LightSource(
          Float.parseFloat(attributesMap.get("x")), 
          Float.parseFloat(attributesMap.get("y")),
          Float.parseFloat(attributesMap.get("z")),
          getColor(attributesMap.get("color")),
          getFloat(attributesMap.get("diameter"))));
    } else if ("backgroundImage".equals(name)) {
      BackgroundImage backgroundImage = new BackgroundImage(
          getContent(attributesMap.get("image")), 
          Float.parseFloat(attributesMap.get("scaleDistance")), 
          Float.parseFloat(attributesMap.get("scaleDistanceXStart")), 
          Float.parseFloat(attributesMap.get("scaleDistanceYStart")), 
          Float.parseFloat(attributesMap.get("scaleDistanceXEnd")), 
          Float.parseFloat(attributesMap.get("scaleDistanceYEnd")), 
          Float.parseFloat(attributesMap.get("xOrigin")), 
          Float.parseFloat(attributesMap.get("yOrigin")), 
          !"false".equals(attributesMap.get("visible")));
      if (this.homeElementName != null && this.homeElementName.equals(parent)) {
        this.homeBackgroundImage = backgroundImage;
      } else {
        this.backgroundImage = backgroundImage;
      }
    } else if ("property".equals(name)) {
      if (this.homeElementName != null) {
        if (this.homeElementName.equals(parent)) {
          this.homeProperties.put(attributesMap.get("name"), attributesMap.get("value"));
        } else {
          this.properties.put(attributesMap.get("name"), attributesMap.get("value"));
        }
      }
    } 
  }
  
  /**
   * Returns the trimmed string of last element value. 
   */
  private String getCharacters() {
    return this.buffer.toString().trim();
  }

  @Override
  public void endDocument() throws SAXException {
    // Rebind wall starts and ends
    for (JoinedWall joinedWall : this.joinedWalls.values()) {
      Wall wall = joinedWall.getWall();
      if (joinedWall.getWallAtStartId() != null) {
        JoinedWall joinedWallAtStart = this.joinedWalls.get(joinedWall.getWallAtStartId());
        if (joinedWallAtStart != null) {
          wall.setWallAtStart(joinedWallAtStart.getWall());
        }
      }
      if (joinedWall.getWallAtEndId() != null) {
        JoinedWall joinedWallAtEnd = this.joinedWalls.get(joinedWall.getWallAtEndId());
        if (joinedWallAtEnd != null) {
          wall.setWallAtEnd(joinedWallAtEnd.getWall());
        }
      }
    }
  }
  
  /**
   * Returns a new {@link Home} instance initialized from the given <code>attributes</code>.
   */
  private Home createHome(Map<String, String> attributes) {
    String wallHeight = attributes.get("wallHeight");
    if (wallHeight != null) {
      return new Home(Float.parseFloat(wallHeight));
    } else {
      return new Home();
    }
  }
  
  /**
   * Sets the attributes of the given <code>home</code>.
   * If needed, this method should be called from {@link #endElement}.
   */
  protected void setHomeAttributes(Home home, Map<String, String> attributes) throws SAXException {
    for (Map.Entry<String, String> property : this.homeProperties.entrySet()) {
      home.setProperty(property.getKey(), property.getValue());
    }
    if (this.furnitureVisibleProperties.size() > 0) {
      this.home.setFurnitureVisibleProperties(this.furnitureVisibleProperties);
    }
    this.home.setBackgroundImage(this.homeBackgroundImage);
    String version = attributes.get("version");
    if (version != null) {
      home.setVersion(Integer.parseInt(version));
    }
    home.setName(attributes.get("name"));
    String selectedLevelId = attributes.get("selectedLevel");
    if (selectedLevelId != null) {
      this.home.setSelectedLevel(this.levels.get(selectedLevelId));
    }
    if ("observerCamera".equals(attributes.get("camera"))) {
      this.home.setCamera(this.home.getObserverCamera());
    }
    home.setBasePlanLocked("true".equals(attributes.get("basePlanLocked")));
    String furnitureSortedProperty = attributes.get("furnitureSortedProperty");
    if (furnitureSortedProperty != null) {
      try {
        home.setFurnitureSortedProperty(HomePieceOfFurniture.SortableProperty.valueOf(furnitureSortedProperty));
      } catch (IllegalArgumentException ex) {
        // Ignore malformed enum constant 
      }
    }
    home.setFurnitureDescendingSorted("true".equals(attributes.get("furnitureDescendingSorted")));
  }
    
  /**
   * Sets the attributes of the given <code>environment</code>.
   * If needed, this method should be called from {@link #endElement}.
   */
  private void setEnvironmentAttributes(HomeEnvironment environment, Map<String, String> attributes) throws SAXException {
    Integer groundColor = getColor(attributes.get("groundColor"));
    if (groundColor != null) {
      environment.setGroundColor(groundColor);
    }
    environment.setGroundTexture(this.textures.get("groundTexture"));
    Integer skyColor = getColor(attributes.get("skyColor"));
    if (skyColor != null) {
      environment.setSkyColor(skyColor);
    }
    environment.setSkyTexture(this.textures.get("skyTexture"));
    Integer lightColor = getColor(attributes.get("lightColor"));
    if (lightColor != null) {
      environment.setLightColor(lightColor);
    }
    Float wallsAlpha = getFloat(attributes.get("wallsAlpha"));
    if (wallsAlpha != null) {
      environment.setWallsAlpha(wallsAlpha);
    }
    environment.setAllLevelsVisible("true".equals(attributes.get("allLevelsVisible")));
    environment.setObserverCameraElevationAdjusted(!"false".equals(attributes.get("allLevelsVisible")));
    Integer ceillingLightColor = getColor(attributes.get("ceillingLightColor"));
    if (ceillingLightColor != null) {
      environment.setCeillingLightColor(ceillingLightColor);
    }
    String drawingMode = attributes.get("drawingMode");
    if (drawingMode != null) {
      try {
        environment.setDrawingMode(HomeEnvironment.DrawingMode.valueOf(drawingMode));
      } catch (IllegalArgumentException ex) {
        // Ignore malformed enum constant 
      }
    }
    Float subpartSizeUnderLight = getFloat(attributes.get("subpartSizeUnderLight"));
    if (subpartSizeUnderLight != null) {
      environment.setSubpartSizeUnderLight(subpartSizeUnderLight);
    }
    Integer photoWidth = getInteger(attributes.get("photoWidth"));
    if (photoWidth != null) {
      environment.setPhotoWidth(photoWidth);
    }
    Integer photoHeight = getInteger(attributes.get("photoHeight"));
    if (photoHeight != null) {
      environment.setPhotoHeight(photoHeight);
    }
    String photoAspectRatio = attributes.get("photoAspectRatio");
    if (photoAspectRatio != null) {
      try {
        environment.setPhotoAspectRatio(AspectRatio.valueOf(photoAspectRatio));
      } catch (IllegalArgumentException ex) {
        // Ignore malformed enum constant 
      }
    }
    Integer photoQuality = getInteger(attributes.get("photoQuality"));
    if (photoQuality != null) {
      environment.setPhotoQuality(photoQuality);
    }
    Integer videoWidth = getInteger(attributes.get("subpartSizeUnderLight"));
    if (videoWidth != null) {
      environment.setVideoWidth(videoWidth);
    }
    String videoAspectRatio = attributes.get("videoAspectRatio");
    if (videoAspectRatio != null) {
      try {
        environment.setVideoAspectRatio(AspectRatio.valueOf(videoAspectRatio));
      } catch (IllegalArgumentException ex) {
        // Ignore malformed enum constant 
      }
    }
    Integer videoQuality = getInteger(attributes.get("videoQuality"));
    if (videoQuality != null) {
      environment.setVideoQuality(videoQuality);
    }
    Integer videoFrameRate = getInteger(attributes.get("videoFrameRate"));
    if (videoFrameRate != null) {
      environment.setVideoFrameRate(videoFrameRate);
    }
  }

  /**
   * Returns a new {@link Print} instance initialized from the given <code>attributes</code>.
   */
  protected HomePrint createPrint(Map<String, String> attributes) {
    return new HomePrint(HomePrint.PaperOrientation.valueOf(attributes.get("paperOrientation")), 
        Float.parseFloat(attributes.get("paperWidth")), 
        Float.parseFloat(attributes.get("paperHeight")), 
        Float.parseFloat(attributes.get("paperTopMargin")), 
        Float.parseFloat(attributes.get("paperLeftMargin")), 
        Float.parseFloat(attributes.get("paperBottomMargin")), 
        Float.parseFloat(attributes.get("paperRightMargin")), 
        !"false".equals(attributes.get("furniturePrinted")),
        !"false".equals(attributes.get("planPrinted")),
        !"false".equals(attributes.get("view3DPrinted")),
        getFloat(attributes.get("planScale")), 
        attributes.get("headerFormat"), 
        attributes.get("footerFormat"));
  }

  /**
   * Sets the attributes of the given <code>compass</code>.
   * If needed, this method should be called from {@link #endElement}.
   */
  protected void setCompassAttributes(Compass compass, Map<String, String> attributes) throws SAXException {
    setProperties(compass);
    compass.setX(getFloat(attributes.get("x")));
    compass.setY(getFloat(attributes.get("y")));
    compass.setDiameter(getFloat(attributes.get("diameter")));
    Float northDirection = getFloat(attributes.get("northDirection"));
    if (northDirection != null) {
      compass.setNorthDirection(northDirection);
    }
    Float longitude = getFloat(attributes.get("longitude"));
    if (longitude != null) {
      compass.setLongitude(longitude);
    }
    Float latitude = getFloat(attributes.get("latitude"));
    if (latitude != null) {
      compass.setLatitude(latitude);
    }
    String timeZone = attributes.get("timeZone");
    if (timeZone != null) {
      compass.setTimeZone(timeZone);
    }
    compass.setVisible(!"false".equals(attributes.get("visible")));
  }

  /**
   * Returns a new {@link Camera} instance initialized from the given <code>attributes</code>.
   */
  private Camera createCamera(String elementName, Map<String, String> attributes) {
    if ("observerCamera".equals(elementName)) {
      return new ObserverCamera(Float.parseFloat(attributes.get("x")),
          Float.parseFloat(attributes.get("y")),
          Float.parseFloat(attributes.get("z")),
          Float.parseFloat(attributes.get("yaw")),
          Float.parseFloat(attributes.get("pitch")),
          Float.parseFloat(attributes.get("fieldOfView")));
    } else {
      return new Camera(Float.parseFloat(attributes.get("x")),
          Float.parseFloat(attributes.get("y")),
          Float.parseFloat(attributes.get("z")),
          Float.parseFloat(attributes.get("yaw")),
          Float.parseFloat(attributes.get("pitch")),
          Float.parseFloat(attributes.get("fieldOfView")));
    }
  }

  /**
   * Sets the attributes of the given <code>camera</code>.
   * If needed, this method should be called from {@link #endElement}.
   */
  protected void setCameraAttributes(Camera camera, Map<String, String> attributes) throws SAXException {
    setProperties(camera);
    if (camera instanceof ObserverCamera) {
      ((ObserverCamera)camera).setFixedSize("true".equals(attributes.get("fixedSize")));
    }
    String lens = attributes.get("lens");
    if (lens != null) {
      try {
        camera.setLens(Camera.Lens.valueOf(lens));
      } catch (IllegalArgumentException ex) {
        // Ignore malformed enum constant 
      }
    }
    String time = attributes.get("time");
    if (time != null) {
      camera.setTime(Long.parseLong(time));
    }
    
    camera.setName(attributes.get("name"));
  }

  /**
   * Returns a new {@link Level} instance initialized from the given <code>attributes</code>.
   */
  private Level createLevel(Map<String, String> attributes) {
    return new Level(attributes.get("name"),
        Float.parseFloat(attributes.get("elevation")),
        Float.parseFloat(attributes.get("floorThickness")),
        Float.parseFloat(attributes.get("height")));
  }

  /**
   * Sets the attributes of the given <code>level</code>.
   * If needed, this method should be called from {@link #endElement}.
   */
  protected void setLevelAttributes(Level level, Map<String, String> attributes) throws SAXException {
    setProperties(level);
    level.setBackgroundImage(this.backgroundImage);
    Integer elevationIndex = getInteger(attributes.get("elevationIndex"));
    if (elevationIndex != null) {
      level.setElevationIndex(elevationIndex);
    }
    level.setVisible(!"false".equals(attributes.get("visible")));
    level.setViewable(!"viewable".equals(attributes.get("viewable")));
  }

  /**
   * Returns a new {@link HomePieceOfFurniture} instance initialized from the given <code>attributes</code>.
   */
  private HomePieceOfFurniture createPieceOfFurniture(String elementName, Map<String, String> attributes) throws SAXException {
    String [] tags = attributes.get("tags") != null 
        ? attributes.get("tags").split(" ")
        : null;
    float elevation = attributes.get("elevation") != null
        ? Float.parseFloat(attributes.get("elevation"))
        : 0;
    float dropOnTopElevation = attributes.get("dropOnTopElevation") != null
        ? Float.parseFloat(attributes.get("dropOnTopElevation"))
        : 1;
    float [][] modelRotation = null;
    if (attributes.get("modelRotation") != null) {
      String [] values = attributes.get("modelRotation").split(" ", 9);
      modelRotation = new float [][] {
         {Float.parseFloat(values [0]), 
          Float.parseFloat(values [1]), 
          Float.parseFloat(values [2])}, 
         {Float.parseFloat(values [3]), 
          Float.parseFloat(values [4]), 
          Float.parseFloat(values [5])}, 
         {Float.parseFloat(values [6]), 
          Float.parseFloat(values [7]), 
          Float.parseFloat(values [8])}};
    }
    BigDecimal price = attributes.get("price") != null
        ? new BigDecimal(attributes.get("price"))
        : null;
    BigDecimal valueAddedTaxPercentage = attributes.get("valueAddedTaxPercentage") != null
        ? new BigDecimal(attributes.get("valueAddedTaxPercentage"))
        : null;
    if ("doorOrWindow".equals(elementName)) {
      float wallThickness = attributes.get("wallThickness") != null
          ? Float.parseFloat(attributes.get("wallThickness"))
          : 1;
      float wallDistance = attributes.get("wallDistance") != null
          ? Float.parseFloat(attributes.get("wallDistance"))
          : 0;
      return new HomeDoorOrWindow(new CatalogDoorOrWindow(
          attributes.get("catalogId"), 
          attributes.get("name"), 
          attributes.get("description"), 
          attributes.get("information"), 
          tags, 
          getLong(attributes.get("creationDate")), 
          getFloat(attributes.get("grade")), 
          getContent(attributes.get("icon")),  
          getContent(attributes.get("planIcon")), 
          getContent(attributes.get("model")), 
          Float.parseFloat(attributes.get("width")), 
          Float.parseFloat(attributes.get("depth")), 
          Float.parseFloat(attributes.get("height")), 
          elevation, 
          dropOnTopElevation, 
          !"false".equals(attributes.get("movable")), 
          attributes.get("cutOutShape"), 
          wallThickness,
          wallDistance,
          this.sashes.toArray(new Sash [this.sashes.size()]),
          modelRotation, 
          "true".equals(attributes.get("backFaceShown")),
          attributes.get("creator"), 
          !"false".equals(attributes.get("resizable")), 
          !"false".equals(attributes.get("deformable")), 
          !"false".equals(attributes.get("texturable")), 
          price, 
          valueAddedTaxPercentage, 
          attributes.get("currency")));
    } else if ("light".equals(elementName)) {
      return new HomeLight(new CatalogLight(
          attributes.get("catalogId"), 
          attributes.get("name"), 
          attributes.get("description"), 
          attributes.get("information"), 
          tags, 
          getLong(attributes.get("creationDate")), 
          getFloat(attributes.get("grade")), 
          getContent(attributes.get("icon")),  
          getContent(attributes.get("planIcon")), 
          getContent(attributes.get("model")), 
          Float.parseFloat(attributes.get("width")), 
          Float.parseFloat(attributes.get("depth")), 
          Float.parseFloat(attributes.get("height")), 
          elevation, 
          dropOnTopElevation, 
          !"false".equals(attributes.get("movable")), 
          this.lightSources.toArray(new LightSource [this.lightSources.size()]),
          attributes.get("staircaseCutOutShape"), 
          modelRotation, 
          "true".equals(attributes.get("backFaceShown")),
          attributes.get("creator"), 
          !"false".equals(attributes.get("resizable")), 
          !"false".equals(attributes.get("deformable")), 
          !"false".equals(attributes.get("texturable")), 
          price, 
          valueAddedTaxPercentage, 
          attributes.get("currency")));
    } else {
      return new HomePieceOfFurniture(new CatalogPieceOfFurniture(
          attributes.get("catalogId"), 
          attributes.get("name"), 
          attributes.get("description"), 
          attributes.get("information"), 
          tags, 
          getLong(attributes.get("creationDate")), 
          getFloat(attributes.get("grade")), 
          getContent(attributes.get("icon")),  
          getContent(attributes.get("planIcon")), 
          getContent(attributes.get("model")), 
          Float.parseFloat(attributes.get("width")), 
          Float.parseFloat(attributes.get("depth")), 
          Float.parseFloat(attributes.get("height")), 
          elevation, 
          dropOnTopElevation, 
          !"false".equals(attributes.get("movable")), 
          attributes.get("staircaseCutOutShape"), 
          modelRotation, 
          "true".equals(attributes.get("backFaceShown")),
          attributes.get("creator"), 
          !"false".equals(attributes.get("resizable")), 
          !"false".equals(attributes.get("deformable")), 
          !"false".equals(attributes.get("texturable")), 
          price, 
          valueAddedTaxPercentage, 
          attributes.get("currency")));
    }
  }

  /**
   * Returns a new {@link HomeFurnitureGroup} instance initialized from the given <code>attributes</code>.
   */
  private HomeFurnitureGroup createFurnitureGroup(Map<String, String> attributes, 
                                                  List<HomePieceOfFurniture> furniture) {
    return new HomeFurnitureGroup(furniture, 
        attributes.get("angle") != null ? Float.parseFloat(attributes.get("angle")) : 0, 
        "true".equals(attributes.get("modelMirrored")), 
        attributes.get("name"));
  }

  /**
   * Sets the attributes of the given <code>piece</code>.
   * If needed, this method should be called from {@link #endElement}.
   */
  protected void setPieceOfFurnitureAttributes(HomePieceOfFurniture piece, Map<String, String> attributes) throws SAXException {
    setProperties(piece);
    piece.setNameStyle(this.textStyles.get("nameStyle"));
    if (this.materials.size() > 0) {
      piece.setModelMaterials(this.materials.toArray(new HomeMaterial [this.materials.size()]));
    }
    Float angle = getFloat(attributes.get("angle"));
    if (angle != null) {
      piece.setAngle(angle);
    }
    piece.setNameVisible("true".equals(attributes.get("nameVisible")));
    Float nameAngle = getFloat(attributes.get("nameAngle"));
    if (nameAngle != null) {
      piece.setNameAngle(nameAngle);
    }
    Float nameXOffset = getFloat(attributes.get("nameXOffset"));
    if (nameXOffset != null) {
      piece.setNameXOffset(nameXOffset);
    }
    Float nameYOffset = getFloat(attributes.get("nameYOffset"));
    if (nameYOffset != null) {
      piece.setNameYOffset(nameYOffset);
    }
    Float x = getFloat(attributes.get("x"));
    if (x != null) {
      piece.setX(x);
    }
    Float y = getFloat(attributes.get("y"));
    if (y != null) {
      piece.setY(y);
    }
    if (piece.isTexturable()) {
      piece.setColor(getColor(attributes.get("color")));
      piece.setTexture(this.textures.get(null));
      piece.setShininess(getFloat(attributes.get("shininess")));
    }
    
    if (piece instanceof HomeLight
        && attributes.get("power") != null) {
      ((HomeLight)piece).setPower(Float.parseFloat(attributes.get("power")));
    } else if (piece instanceof HomeDoorOrWindow) {
      ((HomeDoorOrWindow)piece).setBoundToWall(!"false".equals(attributes.get("boundToWall")));
    }
  }

  /**
   * Returns a new {@link Wall} instance initialized from the given <code>attributes</code>.
   */
  private Wall createWall(Map<String, String> attributes) {
    return new Wall(Float.parseFloat(attributes.get("xStart")),
        Float.parseFloat(attributes.get("yStart")),
        Float.parseFloat(attributes.get("xEnd")),
        Float.parseFloat(attributes.get("yEnd")),
        Float.parseFloat(attributes.get("thickness")));
  }

  /**
   * Sets the attributes of the given <code>wall</code>.
   * If needed, this method should be called from {@link #endElement}.
   */
  protected void setWallAttributes(Wall wall, Map<String, String> attributes) throws SAXException {
    setProperties(wall);
    wall.setLeftSideBaseboard(this.leftSideBaseboard);
    wall.setRightSideBaseboard(this.rightSideBaseboard);
    Float height = getFloat(attributes.get("height"));
    if (height != null) {
      wall.setHeight(height);
    }
    wall.setHeightAtEnd(getFloat(attributes.get("heightAtEnd")));
    wall.setArcExtent(getFloat(attributes.get("arcExtent")));
    wall.setTopColor(getColor(attributes.get("topColor")));
    wall.setLeftSideColor(getColor(attributes.get("leftSideColor")));
    wall.setLeftSideTexture(this.textures.get("leftSideTexture"));
    Float leftSideShininess = getFloat(attributes.get("leftSideShininess"));
    if (leftSideShininess != null) {
      wall.setLeftSideShininess(leftSideShininess);
    }
    wall.setRightSideColor(getColor(attributes.get("rightSideColor")));
    wall.setRightSideTexture(this.textures.get("rightSideTexture"));
    Float rightSideShininess = getFloat(attributes.get("rightSideShininess"));
    if (rightSideShininess != null) {
      wall.setRightSideShininess(rightSideShininess);
    }
    String pattern = attributes.get("pattern");
    if (pattern != null) {
      try {
        wall.setPattern(this.preferences.getPatternsCatalog().getPattern(pattern));
      } catch (IllegalArgumentException ex) {
        // Ignore pattern
      }
    }
  }

  /**
   * Returns a new {@link Room} instance initialized from the given <code>attributes</code>.
   */
  private Room createRoom(Map<String, String> attributes, float[][] points) {
    return new Room(points);
  }

  /**
   * Sets the attributes of the given <code>room</code>.
   * If needed, this method should be called from {@link #endElement}.
   */
  protected void setRoomAttributes(Room room, Map<String, String> attributes) throws SAXException {
    setProperties(room);
    room.setNameStyle(this.textStyles.get("nameStyle"));
    room.setAreaStyle(this.textStyles.get("areaStyle"));
    room.setName(attributes.get("name"));
    Float nameAngle = getFloat(attributes.get("nameAngle"));
    if (nameAngle != null) {
      room.setNameAngle(nameAngle);
    }
    Float nameXOffset = getFloat(attributes.get("nameXOffset"));
    if (nameXOffset != null) {
      room.setNameXOffset(nameXOffset);
    }
    Float nameYOffset = getFloat(attributes.get("nameYOffset"));
    if (nameYOffset != null) {
      room.setNameYOffset(nameYOffset);
    }
    room.setAreaVisible("true".equals(attributes.get("areaVisible")));
    Float areaAngle = getFloat(attributes.get("areaAngle"));
    if (areaAngle != null) {
      room.setAreaAngle(areaAngle);
    }
    Float areaXOffset = getFloat(attributes.get("areaXOffset"));
    if (areaXOffset != null) {
      room.setAreaXOffset(areaXOffset);
    }
    Float areaYOffset = getFloat(attributes.get("areaYOffset"));
    if (areaYOffset != null) {
      room.setAreaYOffset(areaYOffset);
    }
    room.setFloorVisible(!"false".equals(attributes.get("floorVisible")));
    room.setFloorColor(getColor(attributes.get("floorColor")));
    room.setFloorTexture(this.textures.get("floorTexture"));
    Float floorShininess = getFloat(attributes.get("floorShininess"));
    if (floorShininess != null) {
      room.setFloorShininess(floorShininess);
    }
    room.setCeilingVisible(!"false".equals(attributes.get("ceilingVisible")));
    room.setCeilingColor(getColor(attributes.get("ceilingColor")));
    room.setCeilingTexture(this.textures.get("ceilingTexture"));
    Float ceilingShininess = getFloat(attributes.get("ceilingShininess"));
    if (ceilingShininess != null) {
      room.setCeilingShininess(ceilingShininess);
    }
  }
  
  /**
   * Returns a new {@link Polyline} instance initialized from the given <code>attributes</code>.
   */
  private Polyline createPolyline(Map<String, String> attributes, float[][] points) {
    return new Polyline(points);
  }

  /**
   * Sets the attributes of the given <code>polyline</code>.
   * If needed, this method should be called from {@link #endElement}.
   */
  protected void setPolylineAttributes(Polyline polyline, Map<String, String> attributes) throws SAXException {
    setProperties(polyline);
    Float thickness = getFloat(attributes.get("thickness"));
    if (thickness != null) {
      polyline.setThickness(thickness);
    }
    String capStyle = attributes.get("capStyle");
    if (capStyle != null) {
      try {
        polyline.setCapStyle(Polyline.CapStyle.valueOf(capStyle));
      } catch (IllegalArgumentException ex) {
        // Ignore malformed enum constant 
      }
    }
    String joinStyle = attributes.get("joinStyle");
    if (joinStyle != null) {
      try {
        polyline.setJoinStyle(Polyline.JoinStyle.valueOf(joinStyle));
      } catch (IllegalArgumentException ex) {
        // Ignore malformed enum constant 
      }
    }
    String dashStyle = attributes.get("dashStyle");
    if (dashStyle != null) {
      try {
        polyline.setDashStyle(Polyline.DashStyle.valueOf(dashStyle));
      } catch (IllegalArgumentException ex) {
        // Ignore malformed enum constant 
      }
    }
    String startArrowStyle = attributes.get("startArrowStyle");
    if (startArrowStyle != null) {
      try {
        polyline.setStartArrowStyle(Polyline.ArrowStyle.valueOf(startArrowStyle));
      } catch (IllegalArgumentException ex) {
        // Ignore malformed enum constant 
      }
    }
    String endArrowStyle = attributes.get("endArrowStyle");
    if (endArrowStyle != null) {
      try {
        polyline.setEndArrowStyle(Polyline.ArrowStyle.valueOf(endArrowStyle));
      } catch (IllegalArgumentException ex) {
        // Ignore malformed enum constant 
      }
    }
    Integer color = getColor(attributes.get("color"));
    if (color != null) {
      polyline.setColor(color);
    }
    polyline.setClosedPath("true".equals(attributes.get("closedPath")));
  }

  /**
   * Returns a new {@link DimensionLine} instance initialized from the given <code>attributes</code>.
   */
  private DimensionLine createDimensionLine(Map<String, String> attributes) {
    return new DimensionLine(Float.parseFloat(attributes.get("xStart")),
        Float.parseFloat(attributes.get("yStart")),
        Float.parseFloat(attributes.get("xEnd")),
        Float.parseFloat(attributes.get("yEnd")),
        Float.parseFloat(attributes.get("offset")));
  }

  /**
   * Sets the attributes of the given dimension line.
   * If needed, this method should be called from {@link #endElement}.
   */
  protected void setDimensionLineAttributes(DimensionLine dimensionLine, 
                                            Map<String, String> attributes) throws SAXException {
    setProperties(dimensionLine);
    dimensionLine.setLengthStyle(this.textStyles.get("lengthStyle"));
  }

  /**
   * Returns a new {@link Label} instance initialized from the given <code>attributes</code>.
   */
  private Label createLabel(Map<String, String> attributes, String text) {
    return new Label(text, 
        Float.parseFloat(attributes.get("x")),
        Float.parseFloat(attributes.get("y")));
  }

  /**
   * Sets the attributes of the given <code>label</code>.
   * If needed, this method should be called from {@link #endElement}.
   */
  protected void setLabelAttributes(Label label, Map<String, String> attributes) throws SAXException {
    setProperties(label);
    label.setStyle(this.textStyles.get(null));
    Float angle = getFloat(attributes.get("angle"));
    if (angle != null) {
      label.setAngle(angle);
    }
    Float elevation = getFloat(attributes.get("elevation"));
    if (elevation != null) {
      label.setElevation(elevation);
    }
    Float pitch = getFloat(attributes.get("pitch"));
    if (pitch != null) {
      label.setPitch(pitch);
    }
    label.setColor(getColor(attributes.get("color")));
    label.setOutlineColor(getColor(attributes.get("outlineColor")));
  }

  /**
   * Returns a new {@link Baseboard} instance initialized from the given <code>attributes</code>.
   */
  private Baseboard createBaseboard(Map<String, String> attributes) {
    return Baseboard.getInstance(Float.parseFloat(attributes.get("thickness")), 
        Float.parseFloat(attributes.get("height")), 
        getColor(attributes.get("color")),
        this.textures.get(null));
  }
  
  /**
   * Returns a new {@link TextStyle} instance initialized from the given <code>attributes</code>.
   */
  private TextStyle createTextStyle(Map<String, String> attributes) {
    return new TextStyle(attributes.get("fontName"),
        Float.parseFloat(attributes.get("fontSize")),
        "true".equals(attributes.get("bold")),
        "true".equals(attributes.get("italic")));
  }
  
  /**
   * Returns a new {@link HomeTexture} instance initialized from the given <code>attributes</code>.
   */
  private HomeTexture createTexture(Map<String, String> attributes) throws SAXException {
    return new HomeTexture(new CatalogTexture(
            attributes.get("catalogId"), 
            attributes.get("name"), 
            getContent(attributes.get("image")), 
            Float.parseFloat(attributes.get("width")), 
            Float.parseFloat(attributes.get("height")), 
            attributes.get("creator")),
        attributes.get("angle") != null
            ? Float.parseFloat(attributes.get("angle"))
            : 0,
        !"false".equals(attributes.get("leftToRightOriented")));
  }

  /**
   * Returns a new {@link HomeMaterial} instance initialized from the given <code>attributes</code>.
   */
  private HomeMaterial createMaterial(Map<String, String> attributes) {
    return new HomeMaterial(
        attributes.get("name"), 
        attributes.get("key"), 
        getColor(attributes.get("color")), 
        this.materialTexture,
        getFloat(attributes.get("shininess")));
  }

  /**
   * Sets the properties of the given <code>object</code>.
   */
  private void setProperties(HomeObject object) {
    for (Map.Entry<String, String> property : this.properties.entrySet()) {
      object.setProperty(property.getKey(), property.getValue());
    }
  }

  /**
   * Returns the color integer from a hexadecimal string.
   */
  private Integer getColor(String color) {
    if (color != null) {
      // Need to use parseLong in case parsed number is bigger than 2^31
      Integer integer = new Integer((int)Long.parseLong(color, 16));
      return integer;
    } else {
      return null;
    }
  }
  
  private Integer getInteger(String s) {
    if (s != null) {
      return Integer.parseInt(s);
    } else {
      return null;
    }
  }
  
  private Long getLong(String s) {
    if (s != null) {
      return Long.parseLong(s);
    } else {
      return null;
    }
  }
  
  private Float getFloat(String s) {
    if (s != null) {
      return Float.parseFloat(s);
    } else {
      return null;
    }
  }
  
  /**
   * Returns the content object matching the given string.
   */
  private Content getContent(String content) throws SAXException {
    if (content != null) {
      try {
        return new ResourceURLContent(new URL(content), content.startsWith("jar:"));
      } catch (MalformedURLException ex1) {
        if (this.contentContext != null) {
          try {
            return this.contentContext.lookupContent(content);
          } catch (IOException ex2) {
            throw new SAXException("Invalid content " + content, ex2);
          }
        } else {
          throw new SAXException("Missing URL base", ex1);
        }
      }
    } else {
      return null;
    }
  }

  /**
   * Sets the home that will be updated by this handler.
   * If a subclass of this handler uses a root element different from <code>home</code>,
   * it should call this method from {@link #startElement} to store the 
   * {@link Home} subclass instance read from the XML stream.
   */
  protected void setHome(Home home) {
    this.home = home;
    this.homeElementName = this.elements.peek();
  }

  /**
   * Returns the home read by this handler.
   */
  public Home getHome() {
    return this.home;
  }
  
  /**
   * Class storing the ID of the walls connected to a given wall.
   */
  private static final class JoinedWall {
    private final Wall    wall;
    private final String  wallAtStartId;
    private final String  wallAtEndId;
    
    public JoinedWall(Wall wall, String wallAtStartId, String wallAtEndId) {
      this.wall = wall;
      this.wallAtStartId = wallAtStartId;
      this.wallAtEndId = wallAtEndId;
    }
    
    public Wall getWall() {
      return this.wall;
    }
    
    public String getWallAtStartId() {
      return this.wallAtStartId;
    }
    
    public String getWallAtEndId() {
      return this.wallAtEndId;
    }
  }
}
