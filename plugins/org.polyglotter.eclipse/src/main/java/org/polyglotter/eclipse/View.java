package org.polyglotter.eclipse;

import java.io.File;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Color;
import org.polyglotter.common.PolyglotterException;
import org.polyglotter.eclipse.focustree.FocusTree;
import org.polyglotter.eclipse.focustree.FocusTree.Indicator;
import org.polyglotter.eclipse.focustree.FocusTree.Model;

/**
 * 
 */
public class View extends ViewPart {

    static final Color FOLDER_COLOR = new Color( Display.getCurrent(), 0, 64, 128 );

    static final Object[] NO_ITEMS = new Object[ 0 ];

    static final Image TRANSFORMATION_INDICATOR_IMAGE = Activator.plugin().image( "transformation.png" );

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl( final Composite parent ) {
        final FocusTree focusTree = new FocusTree( parent );
        final File root = new File( "/" );
        final Model model = new Model() {

            @Override
            public Color cellBackgroundColor( final Object file ) {
                if ( ( ( File ) file ).isDirectory() ) return FOLDER_COLOR;
                return Display.getCurrent().getSystemColor( SWT.COLOR_WHITE );
            }

            @Override
            public int childCount( final Object file ) {
                return children( file ).length;
            }

            @Override
            public Object[] children( final Object file ) {
                final Object[] children = ( ( File ) file ).listFiles();
                return children == null ? NO_ITEMS : children;
            }

            @Override
            public boolean childrenAddable( final Object item ) {
                final File file = ( File ) item;
                return file.isDirectory() && file.canWrite();
            }

            @Override
            public Object createChildAt( final Object folder,
                                         final int index ) throws PolyglotterException {
                try {
                    // TODO handle file exists
                    final File file = new File( ( ( File ) folder ), "Unnamed" );
                    if ( !file.createNewFile() ) throw new PolyglotterException( EclipseI18n.focusTreeUnableToCreateFile, folder, index );
                    return file;
                } catch ( final IOException e ) {
                    throw new PolyglotterException( e );
                }
            }

            @Override
            public boolean deletable( final Object file ) {
                return ( ( File ) file ).canWrite();
            }

            @Override
            public boolean delete( final Object file ) {
                return ( ( File ) file ).delete();
            }

            @Override
            public boolean hasChildren( final Object item ) {
                return children( item ).length > 0;
            }

            @Override
            public Image icon( final Object file ) {
                return Activator.plugin().image( ( ( File ) file ).isDirectory() ? "folder.gif" : "file.gif" );
            }

            @Override
            public Indicator[] indicators( final Object item ) {
                return new Indicator[] { new Indicator( TRANSFORMATION_INDICATOR_IMAGE,
                                                        "This item is part of at least one transformation" ) {

                    @Override
                    protected void selected( final Object item ) {
                        // jpav: remove
                        System.out.println( "transformation for " + name( item ) );
                    }
                } };
            }

            @Override
            public String name( final Object file ) {
                final String name = ( ( File ) file ).getName();
                return name.isEmpty() ? "/" : name;
            }

            @Override
            public boolean nameEditable( final Object file ) {
                return ( ( File ) file ).canWrite();
            }

            @Override
            public String nameProblem( final Object file,
                                       final String name ) {
                for ( final File sibling : ( ( File ) file ).getParentFile().listFiles() )
                    if ( !sibling.equals( file ) && sibling.getName().equals( name ) )
                        return "A file with this name already exists";
                return null;
            }

            @Override
            public String qualifiedName( final Object file ) {
                return ( ( File ) file ).getAbsolutePath();
            }

            @Override
            public Object setName( final Object item,
                                   final String name ) throws PolyglotterException {
                final File file = ( File ) item;
                final File renamedFile = new File( file.getParentFile(), name );
                if ( file.renameTo( renamedFile ) ) return renamedFile;
                throw new PolyglotterException( EclipseI18n.testUnableToRenameFile, file, renamedFile );
            }

            @Override
            public String type( final Object file ) {
                return ( ( File ) file ).isDirectory() ? "Folder" : "File";
            }
        };
        setPartName( model.name( root ) );
        setTitleImage( model.icon( root ) );
        focusTree.setInitialIndexIsOne( true );
        focusTree.setInitialCellWidth( 100 );
        focusTree.setModel( model );
        focusTree.setRoot( root );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {}
}
