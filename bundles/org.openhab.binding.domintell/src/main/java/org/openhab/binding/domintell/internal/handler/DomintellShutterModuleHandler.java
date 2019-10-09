/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.domintell.internal.protocol.DomintellRegistry;
import org.openhab.binding.domintell.internal.protocol.model.Item;
import org.openhab.binding.domintell.internal.protocol.model.module.ShutterModule;

/**
 * The {@link DomintellShutterModuleHandler} class is handler for all shutter type Domintell modules
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class DomintellShutterModuleHandler extends DomintellModuleHandler {
    public DomintellShutterModuleHandler(Thing thing, DomintellRegistry registry) {
        super(thing, registry);
    }

    @Override
    protected void updateChannel(Item item, Channel channel) {
        Integer value = (Integer) item.getValue();
        updateState(channel.getUID(), new PercentType(value));
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
