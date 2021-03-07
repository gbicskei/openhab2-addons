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
package org.openhab.binding.domintell.internal.handler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.domintell.internal.protocol.DomintellRegistry;
import org.openhab.binding.domintell.internal.protocol.model.Item;
import org.openhab.binding.domintell.internal.protocol.model.ItemKey;
import org.openhab.binding.domintell.internal.protocol.model.group.ItemGroup;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DomintellVariableItemGroupHandler} class is a handler for Domintell item groups
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class DomintellVariableItemGroupHandler extends DomintellItemGroupHandler {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellVariableItemGroupHandler.class);

    public DomintellVariableItemGroupHandler(Thing thing, DomintellRegistry registry) {
        super(thing, registry);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        @Nullable
        Channel channel = thing.getChannel(channelUID.getId());
        if (channel != null) {
            ItemKey key = getItemKeyForChannel((@NonNull Channel) channel);
            logger.debug("Execute command on item: {}->{}", key, command);
            @Nullable
            ItemGroup<?> itemGroup = getItemGroup();
            if (itemGroup != null) {
                if (OnOffType.ON.equals(command)) {
                    itemGroup.setOutput(key);
                } else if (OnOffType.OFF == command) {
                    itemGroup.resetOutput(key);
                } else {
                    super.handleCommand(channelUID, command);
                }
            }
        }
    }

    protected void updateChannel(Item<?> item, Channel channel) {
        logger.trace("Updating channel from item: {}->{}", item, channel.getUID().getId());
        updateState(channel.getUID(), (Boolean) item.getValue() ? OnOffType.ON : OnOffType.OFF);
    }
}
