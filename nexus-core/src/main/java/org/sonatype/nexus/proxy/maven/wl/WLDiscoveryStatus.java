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
package org.sonatype.nexus.proxy.maven.wl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Remote discovery status of a repository.
 * 
 * @author cstamas
 * @since 2.4
 */
public class WLDiscoveryStatus
{
    /**
     * Remote discovery status enumeration.
     */
    public static enum DStatus
    {
        /**
         * Given repository is not a proxy (remote discovery not applicable).
         */
        NOT_A_PROXY,

        /**
         * Remote discovery not enabled for given repository.
         */
        DISABLED,

        /**
         * Remote discovery enabled without results yet (is still working).
         */
        ENABLED,

        /**
         * Remote discovery enabled and was successful.
         */
        SUCCESSFUL,

        /**
         * Remote discovery enabled and was unsuccessful.
         */
        FAILED;
    }

    private final DStatus status;

    private final String lastDiscoveryStrategy;

    private final String lastDiscoveryMessage;

    private final long lastDiscoveryTimestamp;

    /**
     * Constructor for statuses that represent state where discovery not yet ran.
     * 
     * @param status
     */
    public WLDiscoveryStatus( final DStatus status )
    {
        checkArgument( status.ordinal() < DStatus.SUCCESSFUL.ordinal() );
        this.status = checkNotNull( status );
        this.lastDiscoveryStrategy = null;
        this.lastDiscoveryMessage = null;
        this.lastDiscoveryTimestamp = -1;
    }

    /**
     * Constructor for statuses that represent state where discovery did ran.
     * 
     * @param status
     * @param lastDiscoveryStrategy
     * @param lastDiscoveryMessage
     * @param lastDiscoveryTimestamp
     */
    public WLDiscoveryStatus( final DStatus status, final String lastDiscoveryStrategy,
                              final String lastDiscoveryMessage, final long lastDiscoveryTimestamp )
    {
        checkArgument( status.ordinal() >= DStatus.SUCCESSFUL.ordinal() );
        checkArgument( lastDiscoveryTimestamp > 0 );
        this.status = checkNotNull( status );
        this.lastDiscoveryStrategy = checkNotNull( lastDiscoveryStrategy );
        this.lastDiscoveryMessage = checkNotNull( lastDiscoveryMessage );
        this.lastDiscoveryTimestamp = lastDiscoveryTimestamp;
    }

    /**
     * Remote discovery status.
     * 
     * @return remote discovery status.
     */
    public DStatus getStatus()
    {
        return status;
    }

    public String getLastDiscoveryStrategy()
    {
        return lastDiscoveryStrategy;
    }

    public String getLastDiscoveryMessage()
    {
        return lastDiscoveryMessage;
    }

    public long getLastDiscoveryTimestamp()
    {
        return lastDiscoveryTimestamp;
    }
}