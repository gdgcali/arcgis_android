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

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.esri.arcgis.android.samples.geometryeditor.GeometryEditorActivity.FeatureTypeListAdapter;

/**
 * Esta clase implementa un DialogFragment que pide al usuario que seleccione
 * el tipo de función para añadir
 */
public class FeatureTypeDialogFragment extends DialogFragment {
  AdapterView.OnItemClickListener mListListener;

  FeatureTypeListAdapter mAdapter;

  // Contructor
  public FeatureTypeDialogFragment() {
  }

  /**
   * listener para los item.
   * 
   * @param listener
   */
  public void setListListener(AdapterView.OnItemClickListener listener) {
    mListListener = listener;
  }

  /**
   * Metodo adaptador
   * 
   * @param adapter
   */
  public void setListAdapter(FeatureTypeListAdapter adapter) {
    mAdapter = adapter;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, 0);
  }
  //Creacion de vista
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.selectfeaturetype, container, false);
    getDialog().setTitle(R.string.title_feature_type);
    ListView listView = (ListView) view.findViewById(R.id.listView1);
    if (mListListener != null) {
      listView.setOnItemClickListener(mListListener);
    }
    if (mAdapter != null) {
      listView.setAdapter(mAdapter);
    }
    return view;
  }
}
