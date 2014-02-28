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

import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.Shape;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * 
 */
public abstract class TreeSpinnerContentProvider {

    /**
     * 
     */
    public static final Color DEFAULT_CHILD_INDEX_COLOR = new Color( Display.getCurrent(), 0, 128, 255 );

    /**
     * @param item
     *        an item in a tree
     * @return the background color of the supplied item's cell. Default is white.
     */
    public Color backgroundColor( final Object item ) {
        return Display.getCurrent().getSystemColor( SWT.COLOR_WHITE );
    }

    /**
     * @param item
     *        an item in a tree
     * @return the number of children of the supplied item. Default is 0.
     */
    public int childCount( final Object item ) {
        return 0;
    }

    /**
     * @param item
     *        an item in a tree
     * @return the color of the child index shown in the supplied item's cell. Default is {@value #DEFAULT_CHILD_INDEX_COLOR}.
     */
    public Color childIndexColor( final Object item ) {
        return DEFAULT_CHILD_INDEX_COLOR;
    }

    /**
     * @param item
     *        an item in a tree
     * @return the children of the supplied item. Default is an empty array.
     */
    public Object[] children( final Object item ) {
        return new Object[ 0 ];
    }

    /**
     * @param item
     *        an item in a tree
     * @return Creates a cell for the supplied item. Default is to create a {@link RoundedRectangle}.
     */
    public Shape createCell( final Object item ) {
        return new RoundedRectangle();
    }

    /**
     * @param item
     *        an item in a tree
     * @return the foreground color of the supplied item's cell. Default is white or black, whichever contrasts more with the
     *         {@link #backgroundColor(Object) background color}.
     */
    public Color foregroundColor( final Object item ) {
        final Color color = backgroundColor( item );
        final double yiq = ( ( color.getRed() * 299 ) + ( color.getGreen() * 587 ) + ( color.getBlue() * 114 ) ) / 1000.0;
        return yiq >= 128.0 ? Display.getCurrent().getSystemColor( SWT.COLOR_BLACK )
                           : Display.getCurrent().getSystemColor( SWT.COLOR_WHITE );
    }

    /**
     * @param item
     *        an item in a tree
     * @return <code>true</code> if the supplied item has children. Default is <code>false</code>.
     */
    public boolean hasChildren( final Object item ) {
        return false;
    }

    /**
     * @param item
     *        an item in a tree
     * @return the name of the supplied item's cell. Default is the item's {@link Object#toString()}
     */
    public String name( final Object item ) {
        return item.toString();
    }

    /**
     * @param item
     *        an item in a tree
     * @return the preferred width of the cell for the supplied item. Default is 50.
     */
    public int preferredWidth( final Object item ) {
        return SWT.DEFAULT;
    }

    /**
     * @param item
     *        an item in a tree
     * @return the type of the supplied item's cell. Default is the item's simple class name.
     */
    public String type( final Object item ) {
        return item.getClass().getSimpleName();
    }

    /**
     * @param item
     *        an item in a tree
     * @return the value of the supplied item's cell. Default is <code>null</code>.
     */
    public String value( final Object item ) {
        return null;
    }
}
