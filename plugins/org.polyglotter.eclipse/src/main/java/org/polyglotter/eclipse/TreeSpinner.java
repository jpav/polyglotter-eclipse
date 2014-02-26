/*
 * Polyglotter (http://polyglotter.org)
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * See the AUTHORS.txt file in the distribution for a full listing of 
 * individual contributors.
 *
 * Polyglotter is free software. Unless otherwise indicated, all code in Polyglotter
 * is licensed to you under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * Polyglotter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.polyglotter.eclipse;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.polyglotter.common.CheckArg;

/**
 * 
 */
public class TreeSpinner extends Composite {

    final Composite pathButtonBar;
    final Composite upColumnBar;
    final Composite downColumnBar;
    final TreeCanvas canvas;
    final Image upArrow;
    final Image downArrow;
    final Image disabledUpArrow;
    final Image disabledDownArrow;
    final List< Column > columns = new ArrayList<>();
    final MouseAdapter upButtonMouseListener = new MouseAdapter() {

        @Override
        public void mouseUp( final MouseEvent event ) {
            focusCell( event, -1 );
        }
    };
    final MouseAdapter downButtonMouseListener = new MouseAdapter() {

        @Override
        public void mouseUp( final MouseEvent event ) {
            focusCell( event, 1 );
        }
    };
    final PaintListener pathButtonPaintListener = new PaintListener() {

        @Override
        public void paintControl( final PaintEvent event ) {
            event.gc.setBackground( pathButtonBar.getBackground() );
            event.gc.fillRectangle( event.x, event.y, event.width, event.height );
            final Label label = ( Label ) event.widget;
            event.gc.setBackground( label.getBackground() );
            event.gc.fillRoundRectangle( event.x, event.y, event.width, event.height, event.height, event.height );
            event.gc.drawString( label.getText(), event.height / 2, event.y );
        }
    };

    Object root;
    TreeSpinnerContentProvider provider;

    /**
     * @param parent
     *        parent composite
     */
    public TreeSpinner( final Composite parent ) {
        super( parent, SWT.NONE );
        GridLayoutFactory.fillDefaults().numColumns( 4 ).applyTo( this );
        ( ( GridLayout ) getLayout() ).verticalSpacing = 0;

        // Construct left tool bar
        // TODO i18n
        ToolBar toolBar = new ToolBar( this, SWT.NONE );
        newToolBarButton( toolBar, SWT.PUSH, "home.gif", "Scroll to root backgroundColumn" );
        newToolBarButton( toolBar, SWT.PUSH, "add.gif", "Add item below focus line of selected backgroundColumn" );
        newToolBarButton( toolBar, SWT.PUSH, "delete.gif", "Delete item from focus line of selected backgroundColumn" );
        newToolBarButton( toolBar, SWT.PUSH, "collapseall.gif", "Collapse all columns below root backgroundColumn" );
        newToolBarButton( toolBar, SWT.PUSH, "icons.gif", "View only the selected backgroundColumn as thumbnails" );
        newToolBarButton( toolBar, SWT.PUSH, "duplicate.gif", "Create a clone below this tree" );
        newToolBarButton( toolBar, SWT.CHECK, "sync.gif", "Link with other views" );
        newToolBarButton( toolBar, SWT.PUSH, "rotate.gif", "Rotate tree 90°" );

        // Construct zoom slider
        final Composite sliderPanel = new Composite( this, SWT.BORDER );
        GridDataFactory.swtDefaults().applyTo( sliderPanel );
        GridLayoutFactory.fillDefaults().applyTo( sliderPanel );
        final Slider slider = new Slider( sliderPanel, SWT.NONE );
        slider.setSelection( 50 );
        slider.setThumb( 1 );
        slider.setToolTipText( "Zoom view in or out" );

        // Construct search field
        final Text text = new Text( this, SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL );
        GridDataFactory.swtDefaults().align( SWT.FILL, SWT.CENTER ).grab( true, false ).applyTo( text );
        text.setToolTipText( "Search for items in or below this tree's root" );

        // Construct right tool bar
        toolBar = new ToolBar( this, SWT.NONE );
        GridDataFactory.swtDefaults().applyTo( toolBar );
        newToolBarButton( toolBar, SWT.PUSH, "close.gif", "Close this tool bar (Reopen using context menu)" );

        // Construct path bar
        final Composite pathBar = new Composite( this, SWT.BORDER );
        GridDataFactory.swtDefaults().align( SWT.FILL, SWT.CENTER ).grab( true, false ).span( 4, 1 ).applyTo( pathBar );
        GridLayoutFactory.fillDefaults().margins( LayoutConstants.getSpacing().x, 0 ).numColumns( 3 ).applyTo( pathBar );
        pathButtonBar = new Composite( pathBar, SWT.NONE );
        RowLayoutFactory.fillDefaults().fill( true ).wrap( false ).applyTo( pathButtonBar );
        toolBar = new ToolBar( pathBar, SWT.NONE );
        newToolBarButton( toolBar, SWT.PUSH, "copy.gif", "Copy the path of the selected backgroundColumn to the clipboard" );
        toolBar = new ToolBar( pathBar, SWT.NONE );
        GridDataFactory.swtDefaults().align( SWT.END, SWT.CENTER ).grab( true, false ).applyTo( toolBar );
        newToolBarButton( toolBar, SWT.PUSH, "close.gif", "Close this path bar (Reopen using context menu)" );

        // Construct up backgroundColumn bar
        upColumnBar = newColumnBar();

        // Construct arrow images for columns
        final Label label = new Label( upColumnBar, SWT.NONE );
        final int height = label.computeSize( SWT.DEFAULT, SWT.DEFAULT ).y - 2;
        final int width = height * 2 - 1;
        upArrow = newArrowImage( width, height, true );
        disabledUpArrow = new Image( Display.getCurrent(), upArrow, SWT.IMAGE_DISABLE );
        downArrow = newArrowImage( width, height, false );
        disabledDownArrow = new Image( Display.getCurrent(), downArrow, SWT.IMAGE_DISABLE );
        label.dispose();

        // Construct canvas
        canvas = new TreeCanvas( this, SWT.NONE );
        GridDataFactory.fillDefaults().grab( true, true ).span( 4, 1 ).applyTo( canvas );

        // Construct down backgroundColumn bar
        downColumnBar = newColumnBar();
    }

    void addColumn( final Object item ) {

        // Add a new column
        final Column column = new Column();
        columns.add( column );
        column.item = item;

        // Add button for backgroundColumn to path button bar
        column.pathButton = new Label( pathButtonBar, SWT.NONE );
        column.pathButton.setBackground( provider.backgroundColor( item ) );
        column.pathButton.setForeground( provider.foregroundColor( item ) );
        column.pathButton.setText( provider.name( item ) );
        final Point size = column.pathButton.computeSize( SWT.DEFAULT, SWT.DEFAULT );
        column.pathButton.setAlignment( SWT.CENTER );
        column.pathButton.setLayoutData( new RowData( size.x + 10, size.y ) );
        column.pathButton.addPaintListener( pathButtonPaintListener );
        pathButtonBar.getParent().layout();

        // Construct up backgroundColumn panel
        column.upColumnPanel = newColumnPanel( upColumnBar, 3 );
        final Label childCount = new Label( column.upColumnPanel, SWT.NONE );
        childCount.setText( String.valueOf( provider.childCount( item ) ) );
        column.upButton = newArrowButton( column.upColumnPanel );
        column.upButton.addMouseListener( upButtonMouseListener );
        final Label minimizeButton = new Label( column.upColumnPanel, SWT.NONE );
        minimizeButton.setImage( Activator.plugin().image( "minimize.gif" ) );
        minimizeButton.setAlignment( SWT.RIGHT );
        minimizeButton.setToolTipText( "Minimize this backgroundColumn" );

        // Make child count and minimize buttons the same size so arrow is centered
        final Point childCountSize = childCount.computeSize( SWT.DEFAULT, SWT.DEFAULT );
        final Point minimizeButtonSize = minimizeButton.computeSize( SWT.DEFAULT, SWT.DEFAULT );
        int width = Math.max( childCountSize.x, minimizeButtonSize.x );
        GridDataFactory.swtDefaults().hint( width, SWT.DEFAULT ).applyTo( childCount );
        GridDataFactory.swtDefaults().hint( width, SWT.DEFAULT ).applyTo( minimizeButton );

        // Construct down backgroundColumn panel
        column.downColumnPanel = newColumnPanel( downColumnBar, 1 );
        column.downButton = newArrowButton( column.downColumnPanel );
        column.downButton.addMouseListener( downButtonMouseListener );

        // Construct tree canvas
        canvas.addColumn( column );

        // Update width of backgroundColumn panels to match backgroundColumn width
        width = column.childColumn.getSize().width;
        GridDataFactory.swtDefaults().hint( width, SWT.DEFAULT ).applyTo( column.upColumnPanel );
        GridDataFactory.swtDefaults().hint( width, column.upColumnPanel.computeSize( SWT.DEFAULT, SWT.DEFAULT ).y )
                       .applyTo( column.downColumnPanel );

        upColumnBar.layout();
        downColumnBar.layout();
    }

    void focusCell( final MouseEvent event,
                    final int offset ) {
        if ( event.button == 1 && event.count == 1 ) {
            final Composite columnArrowPanel = ( ( Label ) event.widget ).getParent();
            for ( final Column column : columns )
                if ( column.upColumnPanel == columnArrowPanel || column.downColumnPanel == columnArrowPanel ) {
                    final List< ? > children = column.childColumn.getChildren();
                    int ndx = 0;
                    for ( final Object cell : children ) {
                        if ( cell == column.focusCell ) break;
                        ndx++;
                    }
                    canvas.focusCell( ( IFigure ) children.get( ndx + offset ) );
                    break;
                }
        }
    }

    Label newArrowButton( final Composite columnPanel ) {
        final Label label = new Label( columnPanel, SWT.NONE );
        GridDataFactory.swtDefaults().align( SWT.FILL, SWT.FILL ).grab( true, true ).applyTo( label );
        return label;
    }

    Image newArrowImage( final int width,
                         final int height,
                         final boolean upArrow ) {
        final Image image = new Image( Display.getDefault(), width, height );
        final GC gc = new GC( image );
        gc.setAntialias( SWT.ON );
        gc.setForeground( Display.getCurrent().getSystemColor( SWT.COLOR_BLACK ) );
        if ( upArrow ) for ( int x = width / 2, y = 2; y < height - 2; x--, y++ )
            gc.drawLine( x, y, x + y * 2 - 2, y );
        else for ( int y = 2, w = ( height - 3 ) * 2 - 2, x = ( width - w ) / 2; y < height - 2; x++, y++, w -= 2 )
            gc.drawLine( x, y, x + w, y );
        gc.dispose();
        final ImageData data = image.getImageData();
        image.dispose();
        data.transparentPixel = data.palette.getPixel( new RGB( 255, 255, 255 ) );
        return new Image( Display.getCurrent(), data );
    }

    Composite newColumnBar() {
        final Composite columnBar = new Composite( this, SWT.NONE );
        GridDataFactory.swtDefaults().align( SWT.FILL, SWT.CENTER ).grab( true, false ).span( 4, 1 ).applyTo( columnBar );
        GridLayoutFactory.fillDefaults().spacing( 0, 0 ).numColumns( 0 ).applyTo( columnBar );
        return columnBar;
    }

    Composite newColumnPanel( final Composite columnBar,
                              final int numColumns ) {
        ( ( GridLayout ) columnBar.getLayout() ).numColumns++;
        final Composite panel = new Composite( columnBar, SWT.NONE );
        GridLayoutFactory.fillDefaults().numColumns( numColumns ).applyTo( panel );
        panel.setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_GRAY ) );
        panel.addPaintListener( new PaintListener() {

            @Override
            public void paintControl( final PaintEvent event ) {
                final Rectangle bounds = panel.getBounds();
                event.gc.drawRectangle( 0, 0, bounds.width - 1, bounds.height - 1 );
            }
        } );
        return panel;
    }

    void newToolBarButton( final ToolBar toolBar,
                           final int style,
                           final String iconName,
                           final String toolTip ) {
        final ToolItem item = new ToolItem( toolBar, style );
        item.setImage( Activator.plugin().image( iconName ) );
        item.setToolTipText( toolTip );
    }

    void removeColumn( final Column column ) {
        int ndx = columns.size() - 1;
        for ( Column col = columns.get( ndx ); col != column; col = columns.get( --ndx ) ) {
            col.upColumnPanel.dispose();
            col.downColumnPanel.dispose();
            col.pathButton.dispose();
            canvas.removeColumn( col );
            columns.remove( ndx );
        }
        upColumnBar.layout();
        downColumnBar.layout();
        pathButtonBar.getParent().layout();
    }

    /**
     * @param root
     *        the root object of the tree
     * @param provider
     *        a tree content provider
     */
    public void setRootAndContentProvider( final Object root,
                                           final TreeSpinnerContentProvider provider ) {
        CheckArg.notNull( root, "root" );
        CheckArg.notNull( provider, "provider" );
        this.root = root;
        this.provider = provider;
        canvas.provider = provider;
        for ( final Control control : pathButtonBar.getChildren() )
            control.dispose();
        for ( final Control control : upColumnBar.getChildren() )
            control.dispose();
        for ( final Control control : downColumnBar.getChildren() )
            control.dispose();
        addColumn( root );
    }

    void updateArrowButtonEnablement( final Column column ) {
        final List< ? > children = column.focusCell.getParent().getChildren();
        boolean enabled = column.focusCell != children.get( 0 );
        column.upButton.setEnabled( enabled );
        column.upButton.setImage( enabled ? upArrow : disabledUpArrow );
        enabled = column.focusCell != children.get( children.size() - 1 );
        column.downButton.setEnabled( enabled );
        column.downButton.setImage( enabled ? downArrow : disabledDownArrow );
    }

    class Column {

        Object item;
        Composite upColumnPanel, downColumnPanel;
        Label downButton, upButton;
        Label pathButton;
        IFigure backgroundColumn;
        IFigure childColumn;
        Object focusChild;
        Shape focusCell;
        ImageFigure childrenButton;
    }
}
