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
package org.openhab.binding.domintell.internal.protocol.message;

import static org.openhab.binding.domintell.internal.protocol.message.BaseMessage.Type.SYSTEM_TIME;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SystemTimeMessage} class is responsible for parsing domintell system time messages
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class SystemTimeMessage extends BaseMessage {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(SystemTimeMessage.class);

    /**
     * Date formatter for parsing Domintell system date
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm dd/MM/yyyy");

    @Nullable
    private Date dateTime;

    public SystemTimeMessage(String msg) {
        super(SYSTEM_TIME, msg);
        try {
            this.dateTime = DATE_FORMAT.parse(msg);
        } catch (ParseException e) {
            logger.debug("Unable to parse system date/time: {}", msg);
        }
    }

    public @Nullable Date getDateTime() {
        return dateTime;
    }
}
