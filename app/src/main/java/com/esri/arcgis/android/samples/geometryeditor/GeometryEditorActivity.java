/* Copyright 2015 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgis.android.samples.geometryeditor;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.FeatureTemplate;
import com.esri.core.map.FeatureType;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.Renderer;
import com.esri.core.symbol.FillSymbol;
import com.esri.core.symbol.LineSymbol;
import com.esri.core.symbol.MarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.Symbol;
import com.esri.core.symbol.SymbolHelper;

import java.util.ArrayList;

/*
El propósito de este ejemplo es demostrar cómo crear características (punto, polilínea, polígono) con los ArcGIS
 * API de Android. La muestra es compatible con la edición basada plantilla para los tres tipos de capas de entidades (puntos, líneas y
 * Polígono).
 * <P>
 * Toque en el icono "+" en la barra de acción para empezar a añadir una característica. Se muestra una lista de plantillas disponibles
 * Muestra símbolo de las plantillas "que le permite seleccionar rápidamente la función que se requiera para agregar al mapa.
 * <P>
 * Al agregar una función de punto, presione en el mapa para colocar la operación. Al tocar el mapa de nuevo mueve el punto de
 * La nueva posición.
 * <P>
 * Cuando la adición de características de polígono o polilínea:
 * <Ul>
 * <Li> añadir un nuevo vértice con un simple toque en una nueva ubicación en el mapa;
 * <Li> mover un vértice existente tocando y después pulsa en su nueva ubicación en el mapa;
 * <Li> eliminar un vértice existente tocando y después pulsa en el icono de papelera en la barra de acción.
 * </ Ul>
 * Los puntos adicionales se dibujan en el punto medio de cada línea. Un punto medio se puede mover pulsando en el punto medio y luego
 * Tocando su nueva ubicación en el mapa.
 * <P>
 * Además de la papelera, la barra de acción presenta los siguientes iconos cuando se edita una característica:
 * <Ul>
 * <Li> icono del disquete para guardar la función, subiéndolo al servidor;
 * <Li> icono "X" para descartar la función;
 * <Li> deshacer icono para deshacer la última acción realizada (es decir, la última adición, movimiento o supresión de un punto).
 * </ Ul>
 * Cada vez que se añade una característica, una pulsación larga en el mapa muestra una lupa que permite una ubicación que se seleccione
 * Con más precisión.
 */
public class GeometryEditorActivity extends Activity {

  //Atributo
  protected static final String TAG = "EditGraphicElements";

  private static final String TAG_DIALOG_FRAGMENTS = "dialog";

  private static final String KEY_MAP_STATE = "com.esri.MapState";

  //Enumeracion
  private enum EditMode {
    NONE, POINT, POLYLINE, POLYGON, SAVING
  }
  //Menu opciones
  Menu mOptionsMenu;
  //Mapa
  MapView mMapView;

  String mMapState;
  //Dialog
  DialogFragment mDialogFragment;

  GraphicsLayer mGraphicsLayerEditing;
  //Lista de puntos
  ArrayList<Point> mPoints = new ArrayList<Point>();
  //Lista de puntos medios
  ArrayList<Point> mMidPoints = new ArrayList<Point>();

  boolean mMidPointSelected = false;

  boolean mVertexSelected = false;

  int mInsertingIndex;

  EditMode mEditMode;

  boolean mClosingTheApp = false;

  ArrayList<EditingStates> mEditingStates = new ArrayList<EditingStates>();

  ArrayList<FeatureTypeData> mFeatureTypeList;

  ArrayList<FeatureTemplate> mTemplateList;

  ArrayList<ArcGISFeatureLayer> mFeatureLayerList;

  FeatureTemplate mTemplate;

  ArcGISFeatureLayer mTemplateLayer;

  SimpleMarkerSymbol mRedMarkerSymbol = new SimpleMarkerSymbol(Color.RED, 20, SimpleMarkerSymbol.STYLE.CIRCLE);

  SimpleMarkerSymbol mBlackMarkerSymbol = new SimpleMarkerSymbol(Color.BLACK, 20, SimpleMarkerSymbol.STYLE.CIRCLE);

  SimpleMarkerSymbol mGreenMarkerSymbol = new SimpleMarkerSymbol(Color.GREEN, 15, SimpleMarkerSymbol.STYLE.CIRCLE);


  //Actividad
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Incializa la barra de progreso
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setProgressBarIndeterminateVisibility(false);
    setContentView(R.layout.main);

    mEditMode = EditMode.NONE;

    if (savedInstanceState == null) {
      mMapState = null;
    } else {
      mMapState = savedInstanceState.getString(KEY_MAP_STATE);


      Fragment dialogFrag = getFragmentManager().findFragmentByTag(TAG_DIALOG_FRAGMENTS);
      if (dialogFrag != null) {
        ((DialogFragment) dialogFrag).dismiss(); //Si la actividad es destruido y recreado
      }
    }

    // Crear proceso de escucha de estado para las capas de entidades
    OnStatusChangedListener statusChangedListener = new OnStatusChangedListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void onStatusChanged(Object source, STATUS status) {
      }
    };

    // Creacion de la capa layer
    ArcGISFeatureLayer fl1 = new ArcGISFeatureLayer(
        "http://sampleserver5.arcgisonline.com/ArcGIS/rest/services/LocalGovernment/Recreation/FeatureServer/2",
        ArcGISFeatureLayer.MODE.ONDEMAND);
    fl1.setOnStatusChangedListener(statusChangedListener);
    ArcGISFeatureLayer fl2 = new ArcGISFeatureLayer(
        "http://sampleserver5.arcgisonline.com/ArcGIS/rest/services/LocalGovernment/Recreation/FeatureServer/0",
        ArcGISFeatureLayer.MODE.ONDEMAND);
    fl2.setOnStatusChangedListener(statusChangedListener);
    ArcGISFeatureLayer fl3 = new ArcGISFeatureLayer(
        "http://sampleserver5.arcgisonline.com/ArcGIS/rest/services/LocalGovernment/Recreation/FeatureServer/1",
        ArcGISFeatureLayer.MODE.ONDEMAND);
    fl3.setOnStatusChangedListener(statusChangedListener);

    // Encuentre los layer
    mMapView = (MapView) findViewById(R.id.map);
    mMapView.addLayer(fl1);
    mMapView.addLayer(fl2);
    mMapView.addLayer(fl3);

    // Listener para el mapa
    mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void onStatusChanged(final Object source, final STATUS status) {
        if (STATUS.INITIALIZED == status) {
          if (source instanceof MapView) {
            mGraphicsLayerEditing = new GraphicsLayer();
            mMapView.addLayer(mGraphicsLayerEditing);
          }
        }
      }
    });
    mMapView.setOnTouchListener(new MyTouchListener(GeometryEditorActivity.this, mMapView));

    // Actualizar el map view
    if (!TextUtils.isEmpty(mMapState)) {
      mMapView.restoreState(mMapState);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Usamos el el menu en actiobar
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.actions, menu);
    mOptionsMenu = menu;
    updateActionBar();
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Elementos de la barra de accion
    switch (item.getItemId()) {
      case R.id.action_add:
        actionAdd();//Accion para adicionar
        return true;
      case R.id.action_save:
        actionSave();//Gravar
        return true;
      case R.id.action_discard:
        actionDiscard();//Descartar
        return true;
      case R.id.action_delete:
        actionDelete();//Eliminar
        return true;
      case R.id.action_undo:
        actionUndo();//borrar
        return true;
      default:
        return super.onOptionsItemSelected(item);//ninguno
    }
  }

  @Override
  //Metodo para progress bar
  public void onBackPressed() {
    if (mEditMode != EditMode.NONE && mEditMode != EditMode.SAVING && mEditingStates.size() > 0) {
      // Pregunta confirmacion
      mClosingTheApp = true;
      showConfirmDiscardDialogFragment();
    } else {
      // Sin elemento
      super.onBackPressed();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.unpause();
  }

  @Override
  //Preparar el Gravar
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(KEY_MAP_STATE, mMapView.retainState());
  }

  /**
   * Accion añadir metodos.
   */
  private void actionAdd() {
    listTemplates();
    showFeatureTypeDialogFragment();
  }

  /**
   * Accion descartar.
   */
  private void actionDiscard() {
    if (mEditingStates.size() > 0) {
      // para confirmar
      mClosingTheApp = false;
      showConfirmDiscardDialogFragment();
    } else {
      // No hay elemento
      exitEditMode();
    }
  }

  /**
   * Handles the 'Delete' action.
   */
  private void actionDelete() {
    if (!mVertexSelected) {
      mPoints.remove(mPoints.size() - 1); // Borra el ultimo vertice
    } else {
      mPoints.remove(mInsertingIndex); // Borra el vertice seleccionada
    }
    mMidPointSelected = false;
    mVertexSelected = false;
    mEditingStates.add(new EditingStates(mPoints, mMidPointSelected, mVertexSelected, mInsertingIndex));
    refresh();
  }

  /**
   * Accion para limpar
   */
  private void actionUndo() {
    mEditingStates.remove(mEditingStates.size() - 1);
    mPoints.clear();
    if (mEditingStates.size() == 0) {
      mMidPointSelected = false;
      mVertexSelected = false;
      mInsertingIndex = 0;
    } else {
      EditingStates state = mEditingStates.get(mEditingStates.size() - 1);
      mPoints.addAll(state.points);
      Log.d(TAG, "# of points = " + mPoints.size());
      mMidPointSelected = state.midPointSelected;
      mVertexSelected = state.vertexSelected;
      mInsertingIndex = state.insertingIndex;
    }
    refresh();
  }

  /**
   * Guardar Los cambios realizados se aplican y por lo tanto guardan en el servidor.
   */
  private void actionSave() {
    Graphic g;

    if (mEditMode == EditMode.POINT) {
      // Para un punto, basta con crear un gráfico desde el punto
      g = mTemplateLayer.createFeatureWithTemplate(mTemplate, mPoints.get(0));
    } else {
      // Polilíneas y polígonos, crear un MultiPath de los puntos
      MultiPath multipath;
      if (mEditMode == EditMode.POLYLINE) {
        multipath = new Polyline();
      } else if (mEditMode == EditMode.POLYGON) {
        multipath = new Polygon();
      } else {
        return;
      }
      multipath.startPath(mPoints.get(0));
      for (int i = 1; i < mPoints.size(); i++) {
        multipath.lineTo(mPoints.get(i));
      }

      // Simple geometria de puntos
      Geometry geom = GeometryEngine.simplify(multipath, mMapView.getSpatialReference());
      g = mTemplateLayer.createFeatureWithTemplate(mTemplate, geom);
    }
    
    // Muestra el progress bar al momento de salvar
    setProgressBarIndeterminateVisibility(true);
    mEditMode = EditMode.SAVING;
    updateActionBar();

    // Ahora agregue el gráfico a la capa
    mTemplateLayer.applyEdits(new Graphic[] { g }, null, null, new CallbackListener<FeatureEditResult[][]>() {

      @Override
      public void onError(Throwable e) {
        Log.d(TAG, e.getMessage());
        completeSaveAction(null);
      }

      @Override
      public void onCallback(FeatureEditResult[][] results) {
        completeSaveAction(results);
      }

    });

  }

  /**
   * Informes resultado de la acción "Guardar" para el modo de edición de usuario y una salida.
   * 
   * @param results Los resultados de la operación.
   */
  void completeSaveAction(final FeatureEditResult[][] results) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (results != null) {
          if (results[0][0].isSuccess()) {
            String msg = GeometryEditorActivity.this.getString(R.string.saved);
            Toast.makeText(GeometryEditorActivity.this, msg, Toast.LENGTH_SHORT).show();
          } else {
            EditFailedDialogFragment frag = new EditFailedDialogFragment();
            mDialogFragment = frag;
            frag.setMessage(results[0][0].getError().getDescription());
            frag.show(getFragmentManager(), TAG_DIALOG_FRAGMENTS);
          }
        }
        setProgressBarIndeterminateVisibility(false);
        exitEditMode();
      }
    });
  }

  /**
   * Muestra de usuario que pide diálogo para seleccionar el tipo de función para añadir
   */
  private void showFeatureTypeDialogFragment() {
    FeatureTypeDialogFragment frag = new FeatureTypeDialogFragment();
    mDialogFragment = frag;
    frag.setListListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mTemplate = mTemplateList.get(position);
        mTemplateLayer = mFeatureLayerList.get(position);

        FeatureTypeData featureType = mFeatureTypeList.get(position);
        Symbol symbol = featureType.getSymbol();
        if (symbol instanceof MarkerSymbol) {
          mEditMode = EditMode.POINT;
        } else if (symbol instanceof LineSymbol) {
          mEditMode = EditMode.POLYLINE;
        } else if (symbol instanceof FillSymbol) {
          mEditMode = EditMode.POLYGON;
        }
        clear();
        mDialogFragment.dismiss();

        // Configurar el uso de la lupa en una pulsación larga en el mapa
        mMapView.setShowMagnifierOnLongPress(true);
      }

    });
    frag.setListAdapter(new FeatureTypeListAdapter(this, mFeatureTypeList));
    frag.show(getFragmentManager(), TAG_DIALOG_FRAGMENTS);
  }

  /**
   * Muestra que se añade de diálogo pidiendo al usuario que confirme descartando la función.
   */
  private void showConfirmDiscardDialogFragment() {
    ConfirmDiscardDialogFragment frag = new ConfirmDiscardDialogFragment();
    mDialogFragment = frag;
    frag.setYesListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        mDialogFragment.dismiss();
        if (mClosingTheApp) {
          finish();
        } else {
          exitEditMode();
        }
      }

    });
    frag.show(getFragmentManager(), TAG_DIALOG_FRAGMENTS);
  }

  /**
   * Sale del estado del modo de edición.
   */
  void exitEditMode() {
    mEditMode = EditMode.NONE;
    clear();
    mMapView.setShowMagnifierOnLongPress(false);
  }

  /**
   * Método se enumeran todas las plantillas de entidades en la capa. Desde el MapView
   * tenemos todas las capas en una  Matriz
   */
  private void listTemplates() {
    mFeatureTypeList = new ArrayList<FeatureTypeData>();
    mTemplateList = new ArrayList<FeatureTemplate>();
    mFeatureLayerList = new ArrayList<ArcGISFeatureLayer>();

    // Mostramos todas las capas
    Layer[] layers = mMapView.getLayers();
    for (Layer l : layers) {

      // Comprobar si se trata de una ArcGISFeatureLayer
      if (l instanceof ArcGISFeatureLayer) {
        Log.d(TAG, l.getUrl());
        ArcGISFeatureLayer featureLayer = (ArcGISFeatureLayer) l;

        // Mostramos capas
        FeatureType[] types = featureLayer.getTypes();
        for (FeatureType featureType : types) {
          // Save data for each template for this feature type
          addTemplates(featureLayer, featureType.getTemplates());
        }

        // hay plantillas en esta capa
        if (mFeatureTypeList.size() == 0) {
          addTemplates(featureLayer, featureLayer.getTemplates());
        }
      }
    }
  }

  /**
   * Guarda los datos para un conjunto de plantillas de entidad.
   * 
   * @param featureLayer Feturelayer que presenta.
   * @param templates Array de capas
   */
  private void addTemplates(ArcGISFeatureLayer featureLayer, FeatureTemplate[] templates) {
    for (FeatureTemplate featureTemplate : templates) {
      String name = featureTemplate.getName();
      Graphic g = featureLayer.createFeatureWithTemplate(featureTemplate, null);
      Renderer renderer = featureLayer.getRenderer();
      Symbol symbol = renderer.getSymbol(g);

      final int WIDTH_IN_DP_UNITS = 30;
      final float scale = getResources().getDisplayMetrics().density;
      final int widthInPixels = (int) (WIDTH_IN_DP_UNITS * scale + 0.5f);
      Bitmap bitmap = SymbolHelper.getLegendImage(symbol, widthInPixels, widthInPixels);

      mFeatureTypeList.add(new FeatureTypeData(bitmap, name, symbol));
      mTemplateList.add(featureTemplate);
      mFeatureLayerList.add(featureLayer);
    }
  }

  /**
   * Actualiza los elementos
   */
  void refresh() {
    if (mGraphicsLayerEditing != null) {
      mGraphicsLayerEditing.removeAll();
    }
    drawPolylineOrPolygon();
    drawMidPoints();
    drawVertices();

    updateActionBar();
  }

  /**
   * Update el bar action
   */
  private void updateActionBar() {
    if (mEditMode == EditMode.NONE || mEditMode == EditMode.SAVING) {
      // No se edita
      if (mEditMode == EditMode.NONE) {
        showAction(R.id.action_add, true);
      } else {
        showAction(R.id.action_add, false);
      }
      showAction(R.id.action_discard, false);
      showAction(R.id.action_save, false);
      showAction(R.id.action_delete, false);
      showAction(R.id.action_undo, false);
    } else {
      // se Edita
      showAction(R.id.action_add, false);
      showAction(R.id.action_discard, true);
      if (isSaveValid()) {
        showAction(R.id.action_save, true);
      } else {
        showAction(R.id.action_save, false);
      }
      if (mEditMode != EditMode.POINT && mPoints.size() > 0 && !mMidPointSelected) {
        showAction(R.id.action_delete, true);
      } else {
        showAction(R.id.action_delete, false);
      }
      if (mEditingStates.size() > 0) {
        showAction(R.id.action_undo, true);
      } else {
        showAction(R.id.action_undo, false);
      }
    }
  }

  /**
   * Muestra los elementos de la barra de accion
   * 
   * @param resId item Resource
   * @param show muestra item, false lo oculta.
   */
  private void showAction(int resId, boolean show) {
    MenuItem item = mOptionsMenu.findItem(resId);
    item.setEnabled(show);
    item.setVisible(show);
  }

  /**
   * Validacion al salvar
   * 
   * @return true si es valido.
   */
  private boolean isSaveValid() {
    int minPoints;
    switch (mEditMode) {
      case POINT:
        minPoints = 1;
        break;
      case POLYGON:
        minPoints = 3;
        break;
      case POLYLINE:
        minPoints = 2;
        break;
      default:
        return false;
    }
    return mPoints.size() >= minPoints;
  }

  /**
   * Dibuja las polineas
   */
  private void drawPolylineOrPolygon() {
    Graphic graphic;
    MultiPath multipath;

    // Crea la capa de grafico existente
    if (mGraphicsLayerEditing == null) {
      mGraphicsLayerEditing = new GraphicsLayer();
      mMapView.addLayer(mGraphicsLayerEditing);
    }

    if (mPoints.size() > 1) {

      // Construye el multi punto
      if (mEditMode == EditMode.POLYLINE) {
        multipath = new Polyline();
      } else {
        multipath = new Polygon();
      }
      multipath.startPath(mPoints.get(0));
      for (int i = 1; i < mPoints.size(); i++) {
        multipath.lineTo(mPoints.get(i));
      }

      // Dibuja y rellena los simbolos
      if (mEditMode == EditMode.POLYLINE) {
        graphic = new Graphic(multipath, new SimpleLineSymbol(Color.BLACK, 4));
      } else {
        SimpleFillSymbol simpleFillSymbol = new SimpleFillSymbol(Color.YELLOW);
        simpleFillSymbol.setAlpha(100);
        simpleFillSymbol.setOutline(new SimpleLineSymbol(Color.BLACK, 4));
        graphic = new Graphic(multipath, (simpleFillSymbol));
      }
      mGraphicsLayerEditing.addGraphic(graphic);
    }
  }

  /**
   * Llama la mitad del punto medio
   */
  private void drawMidPoints() {
    int index;
    Graphic graphic;

    mMidPoints.clear();
    if (mPoints.size() > 1) {

      // Construye la nueva lista de puntos
      for (int i = 1; i < mPoints.size(); i++) {
        Point p1 = mPoints.get(i - 1);
        Point p2 = mPoints.get(i);
        mMidPoints.add(new Point((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2));
      }
      if (mEditMode == EditMode.POLYGON && mPoints.size() > 2) {
        // Completa el circulo
        Point p1 = mPoints.get(0);
        Point p2 = mPoints.get(mPoints.size() - 1);
        mMidPoints.add(new Point((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2));
      }

      // Dibuja la mitad de los puntos
      index = 0;
      for (Point pt : mMidPoints) {
        if (mMidPointSelected && mInsertingIndex == index) {
          graphic = new Graphic(pt, mRedMarkerSymbol);
        } else {
          graphic = new Graphic(pt, mGreenMarkerSymbol);
        }
        mGraphicsLayerEditing.addGraphic(graphic);
        index++;
      }
    }
  }

  /**
   * Dibuja cada vertice de los puntos
   */
  private void drawVertices() {
    int index = 0;
    SimpleMarkerSymbol symbol;

    for (Point pt : mPoints) {
      if (mVertexSelected && index == mInsertingIndex) {
        // Este vértice está actualmente seleccionada de modo que sea de color rojo
        symbol = mRedMarkerSymbol;
      } else if (index == mPoints.size() - 1 && !mMidPointSelected && !mVertexSelected) {
        // Último vértice y ninguno seleccionado en ese momento por lo que lo convierten en rojo
        symbol = mRedMarkerSymbol;
      } else {
        // Este color negro
        symbol = mBlackMarkerSymbol;
      }
      Graphic graphic = new Graphic(pt, symbol);
      mGraphicsLayerEditing.addGraphic(graphic);
      index++;
    }
  }

  /**
   * Borra las funciones de datos de edición y actualización de la barra de acción.
   */
  void clear() {
    // Clear feature editing data
    mPoints.clear();
    mMidPoints.clear();
    mEditingStates.clear();

    mMidPointSelected = false;
    mVertexSelected = false;
    mInsertingIndex = 0;

    if (mGraphicsLayerEditing != null) {
      mGraphicsLayerEditing.removeAll();
    }

    // Actualizar barra de acción para reflejar el nuevo estado
    updateActionBar();
    int resId;
    switch (mEditMode) {
      case POINT:
        resId = R.string.title_add_point;
        break;
      case POLYGON:
        resId = R.string.title_add_polygon;
        break;
      case POLYLINE:
        resId = R.string.title_add_polyline;
        break;
      case NONE:
      default:
        resId = R.string.app_name;
        break;
    }
    getActionBar().setTitle(resId);
  }

  /**
   * Una instancia de esta clase se crea cuando se agrega / movido / eliminado un nuevo punto.
   * Se registra el estado de la edición en que el tiempo y permite
   * operaciones de edición que se deshagan.
   */
  private class EditingStates {
    ArrayList<Point> points = new ArrayList<Point>();

    boolean midPointSelected = false;

    boolean vertexSelected = false;

    int insertingIndex;

    public EditingStates(ArrayList<Point> points, boolean midpointselected, boolean vertexselected, int insertingindex) {
      this.points.addAll(points);
      this.midPointSelected = midpointselected;
      this.vertexSelected = vertexselected;
      this.insertingIndex = insertingindex;
    }
  }

  /**
   * Toque en el mapa
   */
  private class MyTouchListener extends MapOnTouchListener {
    MapView mapView;

    public MyTouchListener(Context context, MapView view) {
      super(context, view);
      mapView = view;
    }

    @Override
    public boolean onLongPressUp(MotionEvent point) {
      handleTap(point);
      super.onLongPressUp(point);
      return true;
    }

    @Override
    public boolean onSingleTap(final MotionEvent e) {
      handleTap(e);
      return true;
    }

    /***
     * Manejar un golpecito en el mapa
     * 
     * @param e punto que se tocó.
     */
    private void handleTap(final MotionEvent e) {

      // Ignore the tap if we're not creating a feature just now
      if (mEditMode == EditMode.NONE || mEditMode == EditMode.SAVING) {
        return;
      }

      Point point = mapView.toMapPoint(new Point(e.getX(), e.getY()));

      // Si estamos creando un punto, despejar cualquier punto existente
      if (mEditMode == EditMode.POINT) {
        mPoints.clear();
      }

      // Si se selecciona un punto Actualmente, mover ese punto de aprovechar el punto
      if (mMidPointSelected || mVertexSelected) {
        movePoint(point);
      } else {
        // Seleccione punto medio
        int idx1 = getSelectedIndex(e.getX(), e.getY(), mMidPoints, mapView);
        if (idx1 != -1) {
          mMidPointSelected = true;
          mInsertingIndex = idx1;
        } else {
          // Seleccione el vertice
          int idx2 = getSelectedIndex(e.getX(), e.getY(), mPoints, mapView);
          if (idx2 != -1) {
            mVertexSelected = true;
            mInsertingIndex = idx2;
          } else {
            //añade nuevo verice
            mPoints.add(point);
            mEditingStates.add(new EditingStates(mPoints, mMidPointSelected, mVertexSelected, mInsertingIndex));
          }
        }
      }

      // Reinicia el grafico
      refresh();
    }

    /**
     * Comprueba si un lugar determinado coincide (dentro de una tolerancia) con un punto en una matriz dada.
     * 
     * @param x Locacion .
     * @param y Locacion.
     * @param points Array de puntos.
     * @param map MapView que contiene los puntos.
     * @return Index e retorno.
     */
    private int getSelectedIndex(double x, double y, ArrayList<Point> points, MapView map) {
      final int TOLERANCE = 40; // Tolerance in pixels

      if (points == null || points.size() == 0) {
        return -1;
      }

      // Encuentra los puntos
      int index = -1;
      double distSQ_Small = Double.MAX_VALUE;
      for (int i = 0; i < points.size(); i++) {
        Point p = map.toScreenPoint(points.get(i));
        double diffx = p.getX() - x;
        double diffy = p.getY() - y;
        double distSQ = diffx * diffx + diffy * diffy;
        if (distSQ < distSQ_Small) {
          index = i;
          distSQ_Small = distSQ;
        }
      }

      // Comprobacion si esta cerca
      if (distSQ_Small < (TOLERANCE * TOLERANCE)) {
        return index;
      }
      return -1;
    }

    /**
     * Desplaza el punto seleccionado en ese momento a un lugar determinado
     * 
     * @param point   Ubicación para mover el punto a.
     */
    private void movePoint(Point point) {
      if (mMidPointSelected) {
        // Mover el punto medio a la nueva ubicación y que sea un vértice
        mPoints.add(mInsertingIndex + 1, point);
      } else {
        // Debe ser un vértice: moverlo a la nueva ubicación
        ArrayList<Point> temp = new ArrayList<Point>();
        for (int i = 0; i < mPoints.size(); i++) {
          if (i == mInsertingIndex) {
            temp.add(point);
          } else {
            temp.add(mPoints.get(i));
          }
        }
        mPoints.clear();
        mPoints.addAll(temp);
      }
      // Volver al modo de dibujo normal y guardar el nuevo estado de edición
      mMidPointSelected = false;
      mVertexSelected = false;
      mEditingStates.add(new EditingStates(mPoints, mMidPointSelected, mVertexSelected, mInsertingIndex));
    }

  }

  /**
   * Esta clase proporciona el adaptador de la lista de los tipos de entidades.
   */
  class FeatureTypeListAdapter extends ArrayAdapter<FeatureTypeData> {

    public FeatureTypeListAdapter(Context context, ArrayList<FeatureTypeData> featureTypes) {
      super(context, 0, featureTypes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = convertView;
      FeatureTypeViewHolder holder = null;

      if (view == null) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.listitem, null);
        holder = new FeatureTypeViewHolder();
        holder.imageView = (ImageView) view.findViewById(R.id.icon);
        holder.textView = (TextView) view.findViewById(R.id.label);
      } else {
        holder = (FeatureTypeViewHolder) view.getTag();
      }

      FeatureTypeData featureType = getItem(position);
      holder.imageView.setImageBitmap(featureType.getBitmap());
      holder.textView.setText(mFeatureTypeList.get(position).getName());
      view.setTag(holder);
      return view;
    }

  }

  /**
   * Mantiene los datos relacionados con un elemento en la lista de tipos de entidad.
   */
  class FeatureTypeViewHolder {
    ImageView imageView;

    TextView textView;
  }

}
