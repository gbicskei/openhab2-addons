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
import org.openhab.binding.domintell.internal.protocol.model.module.ShutterModule;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link DomintellShutterModuleHandler} class is handler for all shutter type Domintell modules
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class DomintellShutterModuleHandler extends DomintellModuleHandler {
    public DomintellShutterModuleHandler(Thing thing, DomintellRegistry registry) {
        super(thing, registry);
    }

    @Override
    protected void updateChannel(Item<?> item, Channel channel) {
        Integer value = (Integer) item.getValue();
        if (value != null) {
            updateState(channel.getUID(), new PercentType(value));
        }
    }

    public ShutterModule getModule() {
        return (ShutterModule) super.getModule();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        int channelIdx = Integer.parseInt(channelUID.getId());
        if (UpDownType.DOWN == command) {
            getModule().goLow(channelIdx);
        } else if (UpDownType.UP == command) {
            getModule().goHigh(channelIdx);
        } else if (StopMoveType.STOP == command) {
            getModule().stop(channelIdx);
        } else if (command == RefreshType.REFRESH) {
            refreshChannelFromItem(channelUID, channelIdx);
        }
    }
}
