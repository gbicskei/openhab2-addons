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
package org.openhab.binding.domintell.internal.protocol.model.module;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.ItemConfigChangeHandler;
import org.openhab.binding.domintell.internal.protocol.StateChangeListener;
import org.openhab.binding.domintell.internal.protocol.message.ActionMessageBuilder;
import org.openhab.binding.domintell.internal.protocol.message.StatusMessage;
import org.openhab.binding.domintell.internal.protocol.model.*;
import org.openhab.binding.domintell.internal.protocol.model.type.ActionType;
import org.openhab.binding.domintell.internal.protocol.model.type.ItemType;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Module} class is a base class for all supported Domintell modules
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public abstract class Module extends Discoverable {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(Module.class);

    /**
     * Connection
     */
    private DomintellConnection connection;

    /**
     * Module key
     */
    private ModuleKey moduleKey;

    /**
     * Module description received from APPINFO
     */
    private Description description;

    /**
     * Module items
     */
    private Map<ItemKey, Item<?>> items = new HashMap<>();

    /**
     * Configuration change listener
     */
    @Nullable
    private ItemConfigChangeHandler configChangeListener;

    /**
     * Constructor
     *
     * @param connection Connection
     * @param moduleType Module type
     * @param serialNumber Module serial number
     */
    protected Module(DomintellConnection connection, ModuleType moduleType, SerialNumber serialNumber) {
        this.connection = connection;
        this.moduleKey = new ModuleKey(moduleType, serialNumber);
        this.description = new Description(new StringBuilder().append(moduleType).append(" ")
                .append(serialNumber.getAddressInt()).append("/").append(serialNumber.getAddressHex()).toString(), null,
                null);
    }

    /**
     * Getter
     *
     * @return Domintell connection
     */
    DomintellConnection getConnection() {
        return connection;
    }

    public ModuleKey getModuleKey() {
        return moduleKey;
    }

    public Description getDescription() {
        return description;
    }

    public Map<ItemKey, Item<?>> getItems() {
        return items;
    }

    public void setConfigChangeListener(@Nullable ItemConfigChangeHandler configChangeListener) {
        this.configChangeListener = configChangeListener;
    }

    public @Nullable ItemConfigChangeHandler getConfigChangeListener() {
        return configChangeListener;
    }

    /**
     * Process module state update message
     *
     * @param message Received message
     */
    public void processStateUpdate(StatusMessage message) {
        @Nullable
        String data = message.getData();
        if (data != null) {
            if (data.contains("[") || connection.isAppInfoCycle()) {
                logger.debug("Updating module description: {}->{}", getModuleKey(), data);
                description = Description.parseInfo(data);
                @Nullable
                Integer ioNumber = message.getIoNumber();
                if (ioNumber != null || getModuleKey().getModuleType() == ModuleType.VAR) {
                    ItemKey key = new ItemKey(getModuleKey(), (@NonNull Integer) ioNumber);
                    Object item = items.get(key);
                    if (item != null) {
                        ((Item<?>) item).setDescription(Description.parseInfo(data));
                    }
                }
            } else {
                logger.debug("Update module state: {}->{}", getModuleKey(), data);
                updateItems(message);
                for (Item<?> item : items.values()) {
                    if (item.isChanged()) {
                        item.notifyStateUpdate();
                        item.clearChanged();
                    }
                }
            }
        }
    }

    /**
     * Updates state of module items
     *
     * @param message Message to process
     */
    protected abstract void updateItems(StatusMessage message);

    /**
     * Common helper for updating boolean type (input and output) items
     *
     * @param message Message to process
     */
    void updateBooleanItems(StatusMessage message) {
        @Nullable
        String data = message.getData();
        if (data != null) {
            Integer state = Integer.parseInt(data, 16);
            for (int i = 0; i < items.size(); i++) {
                int mask = 1 << i;
                @SuppressWarnings("unchecked")
                Item<Boolean> item = (Item<Boolean>) items.get(new ItemKey(getModuleKey(), i + 1));
                if (item != null) {
                    item.setValue((state & mask) == mask);
                }
            }
        }
    }

    /**
     * Add new item to the module
     *
     * @param id Item id
     * @param itemType Item type
     * @param clazz Type parameter
     * @param <T> Item value type
     * @return Newly added module
     */
    <T extends Serializable> Item<T> addItem(Integer id, ItemType itemType, Class<T> clazz) {
        ItemKey key = new ItemKey(moduleKey, id);
        Item<T> item = new Item<T>(key, this, itemType);
        items.put(key, item);
        return item;
    }

    /**
     * Add new item to the module
     *
     * @param itemType Item type
     * @param clazz Type parameter
     * @param <T> Item value type
     * @return Newly added module
     */
    <T extends Serializable> Item<T> addItem(ItemType itemType, Class<T> clazz) {
        ItemKey key = new ItemKey(moduleKey);
        Item<T> item = new Item<T>(key, this, itemType);
        items.put(key, item);
        return item;
    }

    /**
     * Add new item to the module
     *
     * @param name Item name
     * @param itemType Item type
     * @param clazz Type parameter
     * @param <T> Item value type
     * @return Newly added module
     */
    <T extends Serializable> Item<T> addItem(String name, ItemType itemType, Class<T> clazz) {
        ItemKey key = new ItemKey(moduleKey, name);
        Item<T> item = new Item<T>(key, this, itemType);
        items.put(key, item);
        return item;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    /**
     * Notifies all event listeners about item level translation changes
     */
    public void notifyItemsTranslated() {
        if (configChangeListener != null) {
            ((@NonNull ItemConfigChangeHandler) configChangeListener).groupItemsTranslated();
        }
    }

    /**
     * Request status update for the parent Domintell module module
     */
    public void queryState() {
        connection
                .sendCommand(ActionMessageBuilder.createFromModuleKey(moduleKey).withAction(ActionType.STATUS).build());
    }

    public void setStateChangeListener(@Nullable StateChangeListener listener) {
        getItems().values().forEach(i -> i.setStateChangeListener(listener));
    }

    public void updateState() {
        connection
                .sendCommand(ActionMessageBuilder.createFromModuleKey(moduleKey).withAction(ActionType.STATUS).build());
    }
}
