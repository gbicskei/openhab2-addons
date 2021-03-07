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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.domintell.internal.protocol.DomintellRegistry;
import org.openhab.binding.domintell.internal.protocol.model.Item;
import org.openhab.binding.domintell.internal.protocol.model.module.ReleyModule;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link DomintellReleyModuleHandler} class is handler for all reley type Domintell modules
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class DomintellReleyModuleHandler extends DomintellModuleHandler {
    public DomintellReleyModuleHandler(Thing thing, DomintellRegistry registry) {
        super(thing, registry);
    }

    @Override
    protected void updateChannel(Item<?> item, Channel channel) {
        Boolean value = (Boolean) item.getValue();
        updateState(channel.getUID(), value != null && value ? OnOffType.ON : OnOffType.OFF);
    }

    public ReleyModule getModule() {
        return (ReleyModule) super.getModule();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        int channelIdx = Integer.parseInt(channelUID.getId());
        if (OnOffType.OFF == command) {
            getModule().resetOutput(channelIdx);
        } else if (OnOffType.ON == command) {
            getModule().setOutput(channelIdx);
        } else if (command == RefreshType.REFRESH) {
            refreshChannelFromItem(channelUID, channelIdx);
        }
    }
}
