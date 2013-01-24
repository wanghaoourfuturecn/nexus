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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.wl.EntrySource;
import org.sonatype.nexus.proxy.maven.wl.WLConfig;
import org.sonatype.nexus.proxy.maven.wl.discovery.RemoteStrategy;
import org.sonatype.nexus.proxy.maven.wl.discovery.StrategyFailedException;
import org.sonatype.nexus.proxy.maven.wl.discovery.StrategyResult;

/**
 * Remote prefix file strategy.
 * 
 * @author cstamas
 */
@Named( RemotePrefixFileStrategy.ID )
@Singleton
public class RemotePrefixFileStrategy
    extends AbstractStrategy<MavenProxyRepository>
    implements RemoteStrategy
{
    protected static final String ID = "prefix-file";

    private final WLConfig config;

    @Inject
    public RemotePrefixFileStrategy( final WLConfig config )
    {
        super( ID, 100 );
        this.config = checkNotNull( config );
    }

    @Override
    public StrategyResult discover( final MavenProxyRepository mavenProxyRepository )
        throws StrategyFailedException, IOException
    {
        StorageFileItem item;
        final ArrayList<String> remoteFilePath =
            new ArrayList<String>( Arrays.asList( config.getRemotePrefixFilePaths() ) );
        for ( String path : remoteFilePath )
        {
            item = retrieveFromRemoteIfExists( mavenProxyRepository, path );
            if ( item != null )
            {
                // TODO: measure somehow with "age" of the file (is 1234 dqys old) or such, but watch formatting as this
                // message
                // is formatted server side, while other date fields are JS formatted!
                return new StrategyResult( "Remote publishes prefix file (is XXX days old), using it.",
                    createEntrySource( mavenProxyRepository, path ) );
            }
        }
        throw new StrategyFailedException( "Remote does not publish prefix files on paths " + remoteFilePath );
    }

    // ==

    protected EntrySource createEntrySource( final MavenProxyRepository mavenProxyRepository, final String path )
        throws IOException
    {
        if ( path.endsWith( ".gz" ) )
        {
            return new FileGzEntrySource( mavenProxyRepository, path );
        }
        else
        {
            return new FileEntrySource( mavenProxyRepository, path );
        }
    }

    protected StorageFileItem retrieveFromRemoteIfExists( final MavenProxyRepository mavenProxyRepository,
                                                          final String path )
        throws IOException
    {
        final ResourceStoreRequest request = new ResourceStoreRequest( path );
        request.setRequestAsExpired( true );
        mavenProxyRepository.removeFromNotFoundCache( request );
        try
        {
            final StorageItem item = mavenProxyRepository.retrieveItem( true, request );
            if ( item instanceof StorageFileItem )
            {
                return (StorageFileItem) item;
            }
            else
            {
                return null;
            }
        }
        catch ( IllegalOperationException e )
        {
            // eh?
            return null;
        }
        catch ( ItemNotFoundException e )
        {
            // expected when remote does not publish it
            // not let if rot in NFC as it would block us (if interval is less than NFC keep alive)
            mavenProxyRepository.removeFromNotFoundCache( request );
            return null;
        }
    }
}