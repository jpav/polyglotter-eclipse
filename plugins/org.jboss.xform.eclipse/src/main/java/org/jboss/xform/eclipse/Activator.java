package org.jboss.xform.eclipse;

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
import org.jboss.xform.XFormEngine;
import org.osgi.framework.BundleContext;

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
    
    private XFormEngine xForm;
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
        if ( xForm != null ) xForm.stop();
        // Stop plug-in
        plugin = null;
        super.stop( context );
    }
    
    /**
     * @return the session to the ModeShape workspace repository.
     */
    public Session xFormSession() {
        if ( xForm == null ) try {
            final ProgressMonitorDialog dlg = new ProgressMonitorDialog( null );
            dlg.open();
            dlg.getProgressMonitor().setTaskName( "Starting workspace repository..." );
            dlg.run( false, false, new IRunnableWithProgress() {
                
                @SuppressWarnings( "synthetic-access" )
                @Override
                public void run( final IProgressMonitor monitor ) throws InvocationTargetException {
                    try {
                        xForm = new XFormEngine( "workspaceRepository.json" );
                        xForm.start(); // jpav Should be temporary
                        session = xForm.session();
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
}
