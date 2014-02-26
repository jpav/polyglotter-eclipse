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

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LayoutListener;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.polyglotter.eclipse.TreeSpinner.Column;

class TreeCanvas extends FigureCanvas {

    static final Color DEFAULT_BACKGROUND_COLOR = Display.getCurrent().getSystemColor( SWT.COLOR_WHITE );
    static final Color DEFAULT_FOCUS_COLOR = new Color( Display.getCurrent(), 0, 128, 255 );
    static final Color DEFAULT_SELECTED_COLUMN_BACKGROUND_COLOR = new Color( Display.getCurrent(), 194, 223, 255 );

    static final int DEFAULT_FOCUS_LINE_OFFSET = 75;

    static final int FOCUS_HEIGHT = 3;

    TreeSpinner treeSpinner;
    TreeSpinnerContentProvider provider;

    final FreeformLayer canvas = new FreeformLayer();
    final Panel focusLine = new Panel();
    final Border focusBorder = new LineBorder( DEFAULT_FOCUS_COLOR, FOCUS_HEIGHT ) {

        @Override
        public void paint( final IFigure figure,
                           final Graphics graphics,
                           final Insets insets ) {
            tempRect.setBounds( getPaintRectangle( figure, insets ) );
            if ( getWidth() % 2 == 1 ) {
                tempRect.width--;
                tempRect.height--;
            }
            tempRect.shrink( getWidth() / 2, getWidth() / 2 );
            graphics.setLineWidth( getWidth() );
            graphics.setLineStyle( getStyle() );
            graphics.setForegroundColor( getColor() );
            graphics.drawRoundRectangle( tempRect, 8, 8 );
        }
    };
    final Border noFocusBorder = new LineBorder( focusBorder.getInsets( null ).top ) {

        @Override
        public void paint( final IFigure figure,
                           final Graphics graphics,
                           final Insets insets ) {}
    };

    TreeCanvas( final TreeSpinner treeSpinner,
                final int style ) {
        super( treeSpinner, style );
        this.treeSpinner = treeSpinner;
        // TODO get background color from preferences
        setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_WHITE ) );
        setContents( canvas );
        canvas.addMouseListener( new MouseListener() {

            @Override
            public void mouseDoubleClicked( final MouseEvent event ) {}

            @Override
            public void mousePressed( final MouseEvent event ) {}

            @Override
            public void mouseReleased( final MouseEvent event ) {
                if ( event.button == 1 && ( event.getState() & SWT.MODIFIER_MASK ) == 0 )
                    focusCell( canvas.findFigureAt( event.x, event.y ) );
            }
        } );
        canvas.add( focusLine );
        // TODO get focus line color from preferences
        focusLine.setBackgroundColor( DEFAULT_FOCUS_COLOR );
        // focusLine.setBorder( focusBorder );
        focusLine.setBounds( new Rectangle( 0, 0, 0, FOCUS_HEIGHT ) );
        focusLine.addLayoutListener( new LayoutListener.Stub() {

            @Override
            public void postLayout( final IFigure container ) {
                // Make focus line extend across entire canvas
                final Rectangle bounds = focusLine.getBounds();
                bounds.width = canvas.getBounds().width;
                focusLine.setBounds( bounds );
            }
        } );
    }

    void addColumn( final Column column ) {
        column.backgroundColumn = new Figure();
        // Add columns to beginning of canvas's children to ensure their backgrounds are painted first
        canvas.add( column.backgroundColumn, 0 );
        column.backgroundColumn.setBackgroundColor( DEFAULT_SELECTED_COLUMN_BACKGROUND_COLOR );
        column.childColumn = new Figure();
        canvas.add( column.childColumn );
        final GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        column.childColumn.setLayoutManager( layout );

        // Create cells
        int ndx = 0;
        for ( final Object child : provider.children( column.item ) ) {
            final Shape cell = provider.createCell( child );
            // Focus on first cell
            if ( column.focusCell == null ) column.focusCell = cell;
            column.childColumn.add( cell );
            column.childColumn.setConstraint( cell, new GridData( SWT.FILL, SWT.DEFAULT, false, false ) );
            cell.setLayoutManager( new GridLayout() );
            cell.setBorder( noFocusBorder );
            cell.setBackgroundColor( provider.backgroundColor( child ) );
            final Label label = new Label( String.valueOf( ndx++ ) );
            cell.add( label );
            label.setForegroundColor( provider.childIndexColor( column.item ) );
            newLabel( provider.name( child ), cell, child, provider );
            newLabel( provider.type( child ), cell, child, provider );
        }

        // Set size of first cell to focus on
        final Dimension firstCellSize = column.focusCell.getPreferredSize();
        column.focusCell.setSize( firstCellSize );

        // Create children button
        final Dimension childColumnSize = column.childColumn.getPreferredSize();
        final int childColumnX;
        if ( treeSpinner.columns.size() == 1 ) childColumnX = 0;
        else {
            final Rectangle previousChildColumnBounds =
                treeSpinner.columns.get( treeSpinner.columns.size() - 2 ).childColumn.getBounds();
            childColumnX = previousChildColumnBounds.x + previousChildColumnBounds.width;
        }
        column.childColumn.setBounds( new Rectangle( childColumnX, 0, childColumnSize.width, childColumnSize.height ) );
        final Image expandImage = Activator.plugin().image( "expand.gif" );
        final Image collapseImage = Activator.plugin().image( "collapse.gif" );
        column.childrenButton = new ImageFigure( expandImage );
        canvas.add( column.childrenButton );
        final Dimension childrenButtonSize = column.childrenButton.getPreferredSize();
        final Rectangle focusLineBounds = focusLine.getBounds();
        final int focusLineCenterY = focusLineBounds.y + focusLineBounds.height / 2;
        column.childrenButton.setBounds( new Rectangle( childColumnX
                                                        + childColumnSize.width
                                                        - ( childColumnSize.width - firstCellSize.width ) / 2
                                                        // The expand icon is offset 3 pixels within the image
                                                        - 3,
                                                        focusLineCenterY - childrenButtonSize.height / 2,
                                                        childrenButtonSize.width,
                                                        childrenButtonSize.height ) );

        // Wire children button to show or hide children and toggle icon appropriately
        column.childrenButton.addMouseListener( new MouseListener() {

            @Override
            public void mouseDoubleClicked( final MouseEvent event ) {}

            @Override
            public void mousePressed( final MouseEvent event ) {}

            @Override
            public void mouseReleased( final MouseEvent event ) {
                if ( event.button == 1 && ( event.getState() & SWT.MODIFIER_MASK ) == 0 ) {
                    if ( column.childrenButton.getImage() == expandImage ) {
                        treeSpinner.addColumn( column.focusChild );
                        column.childrenButton.setImage( collapseImage );
                    } else {
                        treeSpinner.removeColumn( column );
                        column.childrenButton.setImage( expandImage );
                    }
                }
            }
        } );

        focusCell( column.focusCell );
    }

    void focusCell( IFigure figure ) {
        if ( figure instanceof Label ) figure = figure.getParent();
        if ( !( figure instanceof Shape ) ) return;
        final Shape focusCell = ( Shape ) ( figure instanceof Label ? figure.getParent() : figure );

        // Find column for focus cell
        for ( final Column focusColumn : treeSpinner.columns )
            for ( final Object cell : focusColumn.childColumn.getChildren() )
                if ( cell == focusCell ) {
                    focusColumn.focusCell.setBorder( noFocusBorder );
                    focusCell.setBorder( focusBorder );

                    // Set new focus cell and child
                    focusColumn.focusCell = focusCell;
                    int ndx = 0;
                    for ( final Object child : focusColumn.childColumn.getChildren() ) {
                        if ( child == focusCell ) {
                            focusColumn.focusChild = provider.children( focusColumn.item )[ ndx ];
                            break;
                        }
                        ndx++;
                    }
                    final Rectangle focusLineBounds = focusLine.getBounds();
                    int focusLineCenterY = focusLineBounds.y + focusLineBounds.height / 2;
                    final Rectangle focusCellBounds = focusCell.getBounds();
                    final int focusCellCenterY = focusCellBounds.y + focusCellBounds.height / 2;
                    final Rectangle childColumnBounds = focusColumn.childColumn.getBounds();
                    childColumnBounds.y -= focusCellCenterY - focusLineCenterY;
                    int minY = Short.MAX_VALUE;
                    int maxY = 0;
                    final Rectangle reusableBounds = new Rectangle();
                    for ( final Column column : treeSpinner.columns ) {
                        reusableBounds.setBounds( column.childColumn.getBounds() );
                        minY = Math.min( minY, reusableBounds.y );
                        maxY = Math.max( maxY, reusableBounds.y + reusableBounds.height );
                    }
                    int newCanvasHeight = maxY - minY;
                    final int topMargin = DEFAULT_FOCUS_LINE_OFFSET - ( focusLineBounds.y - minY );
                    if ( topMargin > 0 ) {
                        newCanvasHeight += topMargin;
                        minY -= topMargin;
                    }
                    final Rectangle viewBounds = getViewport().getClientArea();
                    final int bottomMargin = focusLineBounds.y + viewBounds.height - DEFAULT_FOCUS_LINE_OFFSET - maxY;
                    if ( bottomMargin > 0 ) newCanvasHeight += bottomMargin;
                    final Rectangle canvasBounds = canvas.getBounds();
                    if ( newCanvasHeight != canvasBounds.height ) {
                        canvasBounds.height = newCanvasHeight;
                        canvas.setBounds( canvasBounds );
                        for ( final Column column : treeSpinner.columns ) {
                            reusableBounds.setBounds( column.childColumn.getBounds() );
                            column.backgroundColumn.setBounds( new Rectangle( reusableBounds.x,
                                                                              0,
                                                                              reusableBounds.width,
                                                                              canvasBounds.height ) );
                            // Set focus backgroundColumn opaque, all others transparent
                            column.backgroundColumn.setOpaque( column.backgroundColumn == focusColumn.backgroundColumn );
                        }
                    }
                    if ( minY != 0 ) {
                        focusLineCenterY -= minY;
                        focusLineBounds.y -= minY;
                        focusLine.setBounds( focusLineBounds );
                        final Rectangle childrenButtonBounds = focusColumn.childrenButton.getBounds();
                        childrenButtonBounds.y = focusLineCenterY - childrenButtonBounds.height / 2;
                        for ( final Column column : treeSpinner.columns ) {
                            // Update item backgroundColumn Y
                            reusableBounds.setBounds( column.childColumn.getBounds() );
                            reusableBounds.y -= minY;
                            column.childColumn.setBounds( reusableBounds );
                            // Update children button Y
                            reusableBounds.setBounds( column.childrenButton.getBounds() );
                            reusableBounds.y = childrenButtonBounds.y;
                            column.childrenButton.setBounds( reusableBounds );
                        }
                    }

                    // Show expand icon only if item has children
                    focusColumn.childrenButton.setVisible( provider.hasChildren( focusColumn.focusChild ) );

                    // Scroll so that focus line is at focus line offset from top of view
                    scrollSmoothTo( viewBounds.x, focusLineBounds.y - DEFAULT_FOCUS_LINE_OFFSET );

                    // Update enablement of arrow buttons
                    treeSpinner.updateArrowButtonEnablement( focusColumn );
                    break;
                }
    }

    void newLabel( final String text,
                   final Shape cell,
                   final Object item,
                   final TreeSpinnerContentProvider provider ) {
        final Label label = new Label( text );
        cell.add( label );
        label.setTextAlignment( PositionConstants.CENTER );
        label.setForegroundColor( provider.foregroundColor( item ) );
        final GridData gridData = new GridData( SWT.DEFAULT, SWT.DEFAULT, false, false );
        gridData.widthHint = provider.preferredWidth( item );
        cell.setConstraint( label, gridData );
    }

    void removeColumn( final Column column ) {
        canvas.remove( column.backgroundColumn );
        canvas.remove( column.childColumn );
        canvas.remove( column.childrenButton );
    }
}
