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

import static org.openhab.binding.domintell.internal.DomintellBindingConstants.CHANNEL_BUTTONTRIGGER;
import static org.openhab.binding.domintell.internal.DomintellBindingConstants.CHANNEL_COMMAND;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.domintell.internal.DomintellBindingConstants;
import org.openhab.binding.domintell.internal.config.ContactItemConfig;
import org.openhab.binding.domintell.internal.protocol.DomintellRegistry;
import org.openhab.binding.domintell.internal.protocol.model.Item;
import org.openhab.binding.domintell.internal.protocol.model.ItemKey;
import org.openhab.binding.domintell.internal.protocol.model.module.ContactModule;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;

/**
 * The {@link DomintellContactModuleHandler} class is handler class for all contact provider modules.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class DomintellContactModuleHandler extends DomintellModuleHandler {
    // supported commands
    private static final String SHORT_PUSH = "ShortPush";
    private static final String LONG_PUSH = "LongPush";
    private static final String DOUBLE_PUSH = "DoublePush";

    /**
     * Class logger
     */
    private final Logger logger = getLogger(DomintellContactModuleHandler.class);

    public DomintellContactModuleHandler(Thing thing, DomintellRegistry registry) {
        super(thing, registry);
        logger.debug("Contact module handler created: {}", getModule().getModuleKey());
    }

    @Override
    protected void updateChannel(Item<?> item, Channel channel) {
        // logger.debug("Updating channel from item: {}->{}", item, channel.getUID().getId());
        ContactItemConfig config = channel.getConfiguration().as(ContactItemConfig.class);
        @SuppressWarnings("unchecked")
        Boolean value = getItemValue((Item<Boolean>) item, config);

        OpenClosedType state = value ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
        updateState(channel.getUID(), state);

        if (state == OpenClosedType.OPEN) {
            // push event support
            if (config.getFirePushEvents()) {
                long[] changeDelay = item.getChangeDelay();
                // double press check
                long doublePressTime = changeDelay[3] - changeDelay[0];
                long lastPressTime = changeDelay[3] - changeDelay[2];
                if (lastPressTime > 0) { // if not in init phase
                    if (doublePressTime < config.getDoublePushTimeout() && doublePressTime > 0) {
                        triggerCommandChannel((@NonNull Integer) item.getItemKey().getIoNumber(), DOUBLE_PUSH);
                    } else if (lastPressTime > config.getLongPushTimeout()) { // long press check
                        triggerCommandChannel((@NonNull Integer) item.getItemKey().getIoNumber(), LONG_PUSH);
                    } else {
                        // this was only a short push
                        triggerCommandChannel((@NonNull Integer) item.getItemKey().getIoNumber(), SHORT_PUSH);
                    }
                }
            }
        } else if (state == OpenClosedType.CLOSED) {
            int resetTimeout = config.getResetTimeout();
            if (resetTimeout > 0) {
                scheduler.schedule(() -> {
                    updateState(channel.getUID(), OpenClosedType.OPEN);
                    @SuppressWarnings("unchecked")
                    Boolean b = getItemValue((Item<Boolean>) item, config);

                    if (b) {
                        updateState(channel.getUID(), OpenClosedType.CLOSED);
                        logger.trace("Item channel reset: {}", item.getItemKey());
                    }
                }, resetTimeout, TimeUnit.SECONDS);
                logger.trace("Automatic reset requested for channel: {}", channel.getUID().getId());
            }
        }
    }

    private Boolean getItemValue(Item<Boolean> item, ContactItemConfig config) {
        Boolean value = (Boolean) item.getValue();
        if (value == null) {
            value = false;
        }
        if (config.isInverted()) {
            logger.trace("Item value inverted: {}", item.getItemKey());
            return !value;
        }
        return value;
    }

    public ContactModule getModule() {
        return (ContactModule) super.getModule();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command received on channel: {}->{}", channelUID, command);
        if (command instanceof StringType) {
            String cmd = command.toString();
            if (CHANNEL_COMMAND.equals(channelUID.getId())) {
                try {
                    String[] parts = cmd.split("-");
                    int idx = Integer.parseInt(parts[1]);
                    logger.trace("Executing command: {}->{}", new ItemKey(getModule().getModuleKey(), idx), cmd);
                    String cmdValue = parts[0];
                    if (SHORT_PUSH.equals(cmdValue)) {
                        getModule().shortPush(idx);
                    } else if (LONG_PUSH.equals(cmdValue)) {
                        getModule().longPush(idx);
                    } else if (DOUBLE_PUSH.equals(cmdValue)) {
                        getModule().doublePush(idx);
                    } else {
                        logger.debug("Unknown command: {}->{}", new ItemKey(getModule().getModuleKey()), cmd);
                    }
                } catch (NumberFormatException e) {
                    logger.debug("Unknown command: {}->{}", new ItemKey(getModule().getModuleKey()), cmd);
                }
            }
        } else if (command == RefreshType.REFRESH) {
            if (!CHANNEL_COMMAND.equals(channelUID.getId()) && !CHANNEL_BUTTONTRIGGER.equals(channelUID.getId())) {
                int idx = Integer.parseInt(channelUID.getId());
                refreshChannelFromItem(channelUID, idx);
            }
        }
    }

    /**
     * Fire trigger event with idx/SHORT|DOUBLE|LONG_PUSH payload.
     *
     * @param idx Contact index
     * @param event Occurred event
     */
    private void triggerCommandChannel(Integer idx, String event) {
        String payload = idx + "/" + event;
        triggerChannel(DomintellBindingConstants.CHANNEL_BUTTONTRIGGER, payload);
        logger.trace("Triggering {} for {}/{}", getModule().getModuleKey(), idx, event);
    }
}
