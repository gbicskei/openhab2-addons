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
package org.openhab.binding.domintell.internal.profile;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.domintell.internal.DomintellBindingConstants;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.TriggerProfile;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DomintellButtonTriggerProfile} class implements the behavior when being linked to a String item.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class DomintellButtonTriggerProfile implements TriggerProfile {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellButtonTriggerProfile.class);

    /**
     * Callback
     */
    private ProfileCallback callback;

    /**
     * Contact index
     */
    @Nullable
    private String contactIdx;

    /**
     * Constructor.
     *
     * @param callback Callback
     * @param context Context
     */
    DomintellButtonTriggerProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;
        contactIdx = (@Nullable String) context.getConfiguration().get(DomintellBindingConstants.CONFIG_CONTACT_INDEX);
        logger.debug("Button trigger profile created");
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return DomintellProfileFactory.UID_BUTTON_TRIGGER;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
    }

    @Override
    public void onTriggerFromHandler(String payload) {
        logger.debug("Button trigger received: {}", payload);
        if (contactIdx != null) {
            int separator = payload.indexOf("/");
            if (separator > -1) {
                String idxStr = payload.substring(0, separator);
                String command = payload.substring(separator + 1);
                if (idxStr.equals(contactIdx)) {
                    callback.sendCommand(new StringType(command));
                    logger.debug("String item {} triggered with command: {}", idxStr, command);
                }
            }
        }
    }
}
