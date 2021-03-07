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

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ContactItemConfig} class contains configuration for contact channels
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class ContactItemConfig {
    private Boolean inverted = false;
    private Boolean firePushEvents = false;
    private BigDecimal resetTimeout = BigDecimal.valueOf(0);
    private BigDecimal shortPushTimeout = BigDecimal.valueOf(300);
    private BigDecimal longPushTimeout = BigDecimal.valueOf(500);
    private BigDecimal doublePushTimeout = BigDecimal.valueOf(700);

    // getters
    public Boolean isInverted() {
        return inverted;
    }

    public Boolean getFirePushEvents() {
        return firePushEvents;
    }

    public Integer getResetTimeout() {
        return resetTimeout.intValue();
    }

    public Integer getLongPushTimeout() {
        return longPushTimeout.intValue();
    }

    public Integer getDoublePushTimeout() {
        return doublePushTimeout.intValue();
    }

    public Integer getShortPushTimeout() {
        return shortPushTimeout.intValue();
    }
}
