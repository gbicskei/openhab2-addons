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
package org.openhab.binding.domintell.internal.protocol.model.group;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.ItemConfigChangeHandler;
import org.openhab.binding.domintell.internal.protocol.StateChangeListener;
import org.openhab.binding.domintell.internal.protocol.message.ActionMessageBuilder;
import org.openhab.binding.domintell.internal.protocol.model.Discoverable;
import org.openhab.binding.domintell.internal.protocol.model.Item;
import org.openhab.binding.domintell.internal.protocol.model.ItemKey;
import org.openhab.binding.domintell.internal.protocol.model.type.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ItemGroup} class is container for all non-module Domintell items
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class ItemGroup<T extends Serializable> extends Discoverable {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(ItemGroup.class);

    /**
     * Domintell connection
     */
    private DomintellConnection connection;

    /**
     * Group type
     */
    private ItemGroupType type;

    /**
     * Set of items
     */
    private Set<Item<T>> items = new HashSet<>();

    /**
     * State change listener implementation
     */
    @Nullable
    private StateChangeListener stateChangeListener;

    /**
     * Setup change listener implementation
     */
    @Nullable
    private ItemConfigChangeHandler itemChangeListener;

    /**
     * Constructor
     *
     * @param connection Domintell connection
     * @param type Group type
     */
    public ItemGroup(DomintellConnection connection, ItemGroupType type) {
        this.type = type;
        this.connection = connection;
    }

    /**
     * Add new item
     *
     * @param item Item to add
     */
    public void addItem(Item<T> item) {
        items.add(item);
        item.setStateChangeListener(stateChangeListener);
    }

    /**
     * Getter
     *
     * @return Group type
     */
    public ItemGroupType getType() {
        return type;
    }

    /**
     * Getter
     *
     * @return Item set
     */
    public Set<Item<T>> getItems() {
        return Collections.unmodifiableSet(items);
    }

    /**
     * Setter for change listener. It sets the same listener for items.
     *
     * @param stateChangeListener Listener
     */
    public void setStateChangeListener(@Nullable StateChangeListener stateChangeListener) {
        this.stateChangeListener = stateChangeListener;
        items.forEach(i -> i.setStateChangeListener(stateChangeListener));
    }

    /**
     * Setter
     *
     * @param itemChangeListener Item change listener
     */
    public void setItemChangeListener(@Nullable ItemConfigChangeHandler itemChangeListener) {
        this.itemChangeListener = itemChangeListener;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    public void notifyItemsChanged(Item<T> item) {
        if (itemChangeListener != null) {
            ((@NonNull ItemConfigChangeHandler) itemChangeListener).groupItemsChanged(item);
        }
    }

    /**
     * Notify the listeners about translation changes
     */
    public void notifyItemsTranslated() {
        if (itemChangeListener != null) {
            ((@NonNull ItemConfigChangeHandler) itemChangeListener).groupItemsTranslated();
        }
    }

    /**
     * Query state of the item's parent module
     *
     * @param itemKey Item key
     */
    public void queryState(ItemKey itemKey) {
        logger.debug("Updating state of item's parent module: {}", itemKey.getModuleKey());
        connection.sendCommand(
                ActionMessageBuilder.createFromModuleKey(itemKey.getModuleKey()).withAction(ActionType.STATUS).build());
    }

    /**
     * Set boolean value
     *
     * @param itemKey Item key
     */
    public void setOutput(ItemKey itemKey) {
        logger.debug("Setting output: {}", itemKey);
        connection
                .sendCommand(ActionMessageBuilder.createFromItemKey(itemKey).withAction(ActionType.SET_OUTPUT).build());
    }

    /**
     * Reset boolean output
     *
     * @param itemKey Item key
     */
    public void resetOutput(ItemKey itemKey) {
        logger.debug("Resetting output: {}", itemKey);
        connection.sendCommand(
                ActionMessageBuilder.createFromItemKey(itemKey).withAction(ActionType.RESET_OUTPUT).build());
    }

    public void updateState() {
        items.forEach(i -> connection.sendCommand(ActionMessageBuilder
                .createFromModuleKey(i.getItemKey().getModuleKey()).withAction(ActionType.STATUS).build()));
    }
}
