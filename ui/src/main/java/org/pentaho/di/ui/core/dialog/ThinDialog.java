/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.core.dialog;

import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.WebSpoonUtils;

/**
 * Created by bmorrise on 2/18/16.
 */
public class ThinDialog extends Dialog {

  protected Shell parent;
  protected int width;
  protected int height;
  protected Browser browser;
  protected Shell dialog;
  protected Display display;

  public ThinDialog( Shell shell, int width, int height ) {
    super( shell );

    this.width = width;
    this.height = height;
  }

  public void createDialog( String title, String url, int options, Image logo ) {

    Shell parent = getParent();
    display = parent.getDisplay();

    dialog = new Shell( parent, options );
    dialog.setText( title );
    dialog.setImage( logo );
    dialog.setSize( width, height );
    dialog.setLayout( new FillLayout() );

    dialog.addListener( SWT.Traverse, new Listener() {
      public void handleEvent( Event e ) {
        if ( e.detail == SWT.TRAVERSE_ESCAPE ) {
          e.doit = false;
        }
      }
    } );

    try {
      browser = new Browser( dialog, SWT.NONE );
      browser.setUrl( url );
      browser.addCloseWindowListener( event -> {
        Browser browser = (Browser) event.widget;
        Shell shell = browser.getShell();
        shell.close();
      } );
      new BrowserFunction( browser, "getConnectionId" ) {
        @Override public Object function( Object[] arguments ) {
          return WebSpoonUtils.getConnectionId();
        }
      };
    } catch ( Exception e ) {
      MessageBox messageBox = new MessageBox( dialog, SWT.ICON_ERROR | SWT.OK );
      messageBox.setMessage( "Browser cannot be initialized." );
      messageBox.setText( "Exit" );
      messageBox.open();
    }
    setPosition();
    final ServerPushSession pushSession = new ServerPushSession();
    pushSession.start();
    dialog.addDisposeListener( ( event ) -> {
      pushSession.stop();
    });
    dialog.open();
  }

  protected void setPosition() {
    Rectangle shellBounds = getParent().getBounds();
    Point dialogSize = dialog.getSize();
    dialog.setLocation( shellBounds.x + ( shellBounds.width - dialogSize.x ) / 2, shellBounds.y
      + ( shellBounds.height - dialogSize.y ) / 2 );
  }
}
