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
package org.openhab.binding.domintell.internal.protocol.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AddressException} class handles exceptions for incorrectly formatted Domintell addresses
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class AddressException extends RuntimeException {
    /**
     * Serial version id.
     */
    private static final long serialVersionUID = 435326705649311927L;

    /**
     * Constructs a new exception instance with a given message and the string
     * representation of a Domintell address that could not be parsed.
     * 
     * @param message exception message
     *
     */
    public AddressException(String message) {
        super(message);
    }
}
