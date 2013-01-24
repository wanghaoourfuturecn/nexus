/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.maven.wl.internal;

import static org.sonatype.nexus.util.PathUtils.elementsOf;
import static org.sonatype.nexus.util.PathUtils.pathFrom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.proxy.maven.wl.EntrySource;

import com.google.common.io.Closeables;

/**
 * Simple text based file with prefixes with dead simple syntax: Lines starting with '#' are comments, and any other non
 * empty line is actually a prefix.
 * 
 * @author cstamas
 * @since 2.4
 */
public class PrefixesFileMarshaller
    implements EntrySourceMarshaller
{
    protected static final Charset CHARSET = Charset.forName( "UTF-8" );

    @Override
    public void write( final EntrySource entrySource, final OutputStream outputStream )
        throws IOException
    {
        final List<String> entries = entrySource.readEntries();
        final PrintWriter printWriter = new PrintWriter( new OutputStreamWriter( outputStream, CHARSET ) );
        printWriter.println( "# Prefix file generated by Sonatype Nexus" );
        printWriter.println( "# Do not edit, changes will be overwritten!" );
        for ( String entry : entries )
        {
            printWriter.println( entry );
        }
        printWriter.flush();
    }

    @Override
    public EntrySource read( final InputStream inputStream )
        throws IOException
    {
        try
        {
            final ArrayList<String> entries = new ArrayList<String>();
            final BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream, CHARSET ) );
            String line = reader.readLine();
            while ( line != null )
            {
                // trim
                line = line.trim();
                if ( !line.startsWith( "#" ) && line.length() > 0 )
                {
                    // Igor's find command makes path like "./org/apache/"
                    while ( line.startsWith( "." ) )
                    {
                        line = line.substring( 1 );
                    }
                    // win file separators? Highly unlikely but still...
                    line = line.replace( '\\', '/' );
                    // normalization
                    entries.add( pathFrom( elementsOf( line ) ) );
                }
                line = reader.readLine();
            }
            return new ArrayListEntrySource( entries );
        }
        finally
        {
            Closeables.closeQuietly( inputStream );
        }
    }
}