package org.polyglotter.eclipse;

/*
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.
 *
 * This software is made available by Red Hat, Inc. under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * See the AUTHORS.txt file in the distribution for a full listing of
 * individual contributors.
 */

import java.lang.reflect.InvocationTargetException;

import javax.jcr.Session;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
    
    private Polyglotter polyglotter;
    private Session session;
    
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
    
    /**
     * @return the session to Polyglotter.
     */
    public Session polyglotterSession() {
        if ( polyglotter == null ) try {
            final ProgressMonitorDialog dlg = new ProgressMonitorDialog( null );
            dlg.open();
            dlg.getProgressMonitor().setTaskName( "Starting Polyglotter..." );
            dlg.run( false, false, new IRunnableWithProgress() {
                
                @SuppressWarnings( "synthetic-access" )
                @Override
                public void run( final IProgressMonitor monitor ) throws InvocationTargetException {
                    try {
                        polyglotter = new Polyglotter( "repository.json" );
                        polyglotter.start(); // jpav Should be temporary
                        session = polyglotter.session();
                    } catch ( final Exception error ) {
                        throw new InvocationTargetException( error );
                    }
                }
            } );
        } catch ( InvocationTargetException | InterruptedException error ) {
            throw new RuntimeException( error );
        }
        return session;
    }
    
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
        if ( session != null ) session.logout();
        if ( polyglotter != null ) polyglotter.stop();
        // Stop plug-in
        plugin = null;
        super.stop( context );
    }
}
