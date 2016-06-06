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
import android.widget.Button;
import android.widget.TextView;

/**
 * Esta clase implementa un DialogFragment que informa de que los cambios aplicados a una capa
 */
public class EditFailedDialogFragment extends DialogFragment {
  String mMessage;

  // Constructor.
  public EditFailedDialogFragment() {
  }

  /**
   * Mensaje de Usuario.
   * 
   * @param message
   */
  public void setMessage(String message) {
    mMessage = message;
  }

  //Actividad
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, 0); //Un dialog
  }

  //Creacion de vista
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.edit_failed, container, false);
    getDialog().setTitle(R.string.title_edit_failed);
    Button button = (Button) view.findViewById(R.id.ok_key);
    TextView textView = (TextView) view.findViewById(R.id.edit_failed_msg);
    textView.setText(mMessage);
    //Evento {@link presionar el botom}
    button.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        dismiss();
      }

    });
    return view;
  }
}
