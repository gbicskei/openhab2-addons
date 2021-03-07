/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.domintell.internal.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BridgeConfig} class is a Domintell connection configuration holder
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class BridgeConfig {
    /**
     * Class logger
     */
    private Logger logger = LoggerFactory.getLogger(BridgeConfig.class);

    /**
     * Host name or IP address of the DETH02 module
     */
    @Nullable
    private String address;

    /**
     * Port of the DETH02 module
     */
    private Integer port = 17481;

    public Integer getPort() {
        return port;
    }

    public boolean isValid() {
        try {
            return address != null && port != null && InetAddress.getByName(address) != null;
        } catch (UnknownHostException e) {
            logger.debug("Invalid configuration: {}", e.getMessage(), e);
            return false;
        }
    }

    public @Nullable InetAddress getInternetAddress() throws UnknownHostException {
        return address != null ? InetAddress.getByName(address) : null;
    }

    @Override
    public String toString() {
        return "BridgeConfig{" + "address='" + address + '\'' + ", port=" + port + '}';
    }
}
