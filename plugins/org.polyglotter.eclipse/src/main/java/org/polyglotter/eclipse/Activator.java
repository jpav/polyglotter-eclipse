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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
import org.polyglotter.Polyglotter;

/**
 * Controls the plug-in life cycle
 */
public class Activator extends Plugin {
    
    // The singleton instance of this plug-in
    private static Activator plugin;
    
    /**
     * @return the singleton instance of this plug-in.
     */
    public static Activator plugin() {
        return plugin;
    }
    
    private boolean infoEnabled;
    
//    private Polyglotter polyglotter;
    
    /**
     * @return <code>true</code> if information-level logging is enabled
     */
    public boolean infoLoggingEnabled() {
        return infoEnabled;
    }
    
    /**
     * @param severity
     * @param message
     * @param throwable
     */
    public void log( final int severity,
                     final String message,
                     final Throwable throwable ) {
        // jpav figure out way to handle debug and trace
        if ( severity == IStatus.INFO && !infoLoggingEnabled() ) return;
        if ( plugin == null ) {
            if ( severity == IStatus.ERROR ) {
                System.err.println( message );
                if ( throwable != null ) System.err.println( throwable );
            } else {
                System.out.println( message );
                if ( throwable != null ) System.out.println( throwable );
            }
        } else getLog().log( new Status( severity, getBundle().getSymbolicName(), message, throwable ) );
    }
    
    /**
     * @param severity
     * @param throwable
     */
    public void log( final int severity,
                     final Throwable throwable ) {
        log( severity, throwable.getMessage(), throwable );
    }
    
    /**
     * @param throwable
     */
    public void log( final Throwable throwable ) {
        log( IStatus.ERROR, throwable );
    }
    
    //
    // /**
    // * @return the session to Polyglotter.
    // */
    // public Session polyglotterSession() {
    // try {
    // if ( polyglotter == null ) {
    // final ProgressMonitorDialog dlg = new ProgressMonitorDialog( null );
    // dlg.open();
    // dlg.getProgressMonitor().setTaskName( "Starting Polyglotter..." );
    // dlg.run( false, false, new IRunnableWithProgress() {
    //
    // @SuppressWarnings( "synthetic-access" )
    // @Override
    // public void run( final IProgressMonitor monitor ) throws InvocationTargetException {
    // try {
    // polyglotter = new Polyglotter();
    // } catch ( final Throwable error ) {
    // throw new InvocationTargetException( error );
    // }
    // }
    // } );
    // }
    // return polyglotter.session();
    // } catch ( final Throwable error ) {
    // throw new RuntimeException( error );
    // }
    // }
    
    /**
     * @param enabled
     *            <code>true</code> if information-level logging is enabled
     */
    public void setInfoEnabled( final boolean enabled ) {
        infoEnabled = enabled;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start( final BundleContext context ) throws Exception {
        super.start( context );
        plugin = this;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop( final BundleContext context ) throws Exception {
        // Close the workspace repository session and shutdown ModeShape
        // if ( polyglotter != null ) polyglotter.stop();
        // Stop plug-in
        plugin = null;
        super.stop( context );
    }
}
