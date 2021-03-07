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

import static org.openhab.binding.domintell.internal.DomintellBindingConstants.*;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.domintell.internal.config.ModuleConfig;
import org.openhab.binding.domintell.internal.protocol.DomintellRegistry;
import org.openhab.binding.domintell.internal.protocol.ItemConfigChangeHandler;
import org.openhab.binding.domintell.internal.protocol.model.Item;
import org.openhab.binding.domintell.internal.protocol.model.ItemKey;
import org.openhab.binding.domintell.internal.protocol.model.SerialNumber;
import org.openhab.binding.domintell.internal.protocol.model.group.ItemGroup;
import org.openhab.binding.domintell.internal.protocol.model.group.ItemGroupType;
import org.openhab.binding.domintell.internal.protocol.model.module.Module;
import org.openhab.binding.domintell.internal.protocol.model.module.ModuleKey;
import org.openhab.binding.domintell.internal.protocol.model.type.ItemType;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DomintellItemGroupHandler} class is a common handler class for all grouped domintell items (e.g variables)
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public abstract class DomintellItemGroupHandler extends BaseThingHandler implements ItemConfigChangeHandler {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellItemGroupHandler.class);

    /**
     * Items groups
     */
    private ItemGroup<? extends Serializable> itemGroup;

    /**
     * ItemKey/Channel map
     */
    private Map<ItemKey, Channel> channels = new HashMap<>();

    /**
     * Domintell item registry
     */
    private DomintellRegistry registry;

    /**
     * Constructor
     *
     * @param thing Parent thing
     * @param registry Domintell item registry
     */
    DomintellItemGroupHandler(Thing thing, DomintellRegistry registry) {
        super(thing);
        this.registry = registry;
        this.itemGroup = registry.getItemGroup(ItemGroupType.valueOf(thing.getUID().getId()));
        itemGroup.setStateChangeListener(this::itemStateChanged);
        itemGroup.setItemChangeListener(this);
        logger.debug("Group handler created: {}", itemGroup.getType());
    }

    private void touchItems(DomintellRegistry registry) {
        thing.getChannels().forEach(c -> {
            ModuleConfig config = c.getConfiguration().as(ModuleConfig.class);
            Module module = registry.getDomintellModule((@NonNull ModuleType) config.getModuleType(),
                    (@NonNull SerialNumber) config.getSerialNumber());
            module.updateState();
        });
    }

    private ChannelTypeUID getChannelTypeUID(ItemType itemType) {
        return ItemType.booleanVar == itemType ? CHANNEL_TYPE_VARIABLE_BOOLEAN : CHANNEL_TYPE_VARIABLE_NUM;
    }

    /**
     * Handler for item chane events
     *
     * @param item Changed item
     */
    @Override
    public void groupItemsChanged(Item<?> item) {
        @Nullable
        ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            ItemKey key = item.getItemKey();
            logger.debug("Group item changed: {}", key);
            Channel oldCh = thing.getChannel(key.toId());
            if (oldCh == null) {
                Configuration config = new Configuration();
                config.put(CONFIG_MODULE_TYPE, key.getModuleKey().getModuleType().toString());
                config.put(CONFIG_SERIAL_NUMBER, key.getModuleKey().getSerialNumber().getAddressInt().toString());

                List<Channel> thingChannels = new ArrayList<>(thing.getChannels());
                ChannelUID channelUID = new ChannelUID(thing.getUID(), key.toId());

                Channel channel = callback.createChannelBuilder(channelUID, getChannelTypeUID(item.getType()))
                        .withConfiguration(config).withLabel(item.getLabel()).withDescription(item.getDescription())
                        .build();
                thingChannels.add(channel);

                ThingBuilder thingBuilder = editThing();
                thingBuilder.withChannels(thingChannels);
                updateThing(thingBuilder.build());
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    /**
     * Translates channel labels and descriptions for channels connected to items.
     */
    @Override
    public void groupItemsTranslated() {
        logger.debug("Translate group item channels: {}", itemGroup.getType());
        @Nullable
        ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            // update description
            List<Channel> newChannels = new ArrayList<>();
            for (Item<?> item : itemGroup.getItems()) {
                ItemKey key = item.getItemKey();
                Channel c = channels.get(key);
                if (c != null) {
                    newChannels.add(callback.editChannel(thing, c.getUID()).withLabel(item.getLabel())
                            .withConfiguration(c.getConfiguration()).withDescription(item.getDescription()).build());
                } else {
                    Configuration config = new Configuration();
                    config.put(CONFIG_MODULE_TYPE, key.getModuleKey().getModuleType().toString());
                    config.put(CONFIG_SERIAL_NUMBER, key.getModuleKey().getSerialNumber().getAddressInt().toString());

                    newChannels.add(callback
                            .createChannelBuilder(new ChannelUID(thing.getUID(), key.toId()),
                                    getChannelTypeUID(item.getType()))
                            .withConfiguration(config).withLabel(item.getLabel()).withDescription(item.getDescription())
                            .build());
                }
            }

            if (newChannels.size() > 0) {
                // order
                TreeMap<String, Channel> orderedMap = new TreeMap<>();
                newChannels.forEach(c -> orderedMap.put(c.getUID().getId(), c));

                ThingBuilder thingBuilder = editThing();
                ArrayList<Channel> orderedChannels = new ArrayList<>(orderedMap.values());

                thingBuilder.withChannels(orderedChannels);
                updateThing(thingBuilder.build());
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    public @Nullable ItemGroup<?> getItemGroup() {
        return itemGroup;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        mapChannels();
        touchItems(registry);
    }

    /**
     * Creates item key from channel configuration parameters
     *
     * @param channel Channel
     * @return The ItemKey
     */
    ItemKey getItemKeyForChannel(Channel channel) {
        try {
            String moduleTypeStr = (String) channel.getConfiguration().get(CONFIG_MODULE_TYPE);
            if (moduleTypeStr != null) {
                ModuleType moduleType = ModuleType.valueOf(moduleTypeStr);
                SerialNumber serialNumber = new SerialNumber(
                        (String) channel.getConfiguration().get(CONFIG_SERIAL_NUMBER));
                return new ItemKey(new ModuleKey(moduleType, serialNumber));
            }
            throw new IllegalArgumentException("Module was not found for channel: " + channel.toString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid channel configuration", e);
        }
    }

    /**
     * Map channels by item key to help lookup
     */
    private void mapChannels() {
        channels = thing.getChannels().stream()
                .collect(Collectors.toMap(this::getItemKeyForChannel, Function.identity()));
    }

    /**
     * Callback for item change events
     *
     * @param item Updated item
     */
    private void itemStateChanged(Item<?> item) {
        Channel channel = channels.get(item.getItemKey());
        if (channel != null) {
            updateChannel(item, channel);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Handler specific channel update
     *
     * @param item Updated item
     * @param channel Affected channel
     */
    protected abstract void updateChannel(Item<?> item, Channel channel);

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Item state update command received: {}", itemGroup.getType());
            @Nullable
            Channel channel = thing.getChannel(channelUID.getId());
            if (channel != null) {
                itemGroup.queryState(getItemKeyForChannel((@NonNull Channel) channel));
            }
        }
    }
}
