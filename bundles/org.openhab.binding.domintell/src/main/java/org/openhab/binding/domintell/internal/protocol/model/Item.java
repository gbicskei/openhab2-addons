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
package org.openhab.binding.domintell.internal.protocol.model;

import java.io.Serializable;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.domintell.internal.protocol.StateChangeListener;
import org.openhab.binding.domintell.internal.protocol.model.module.Module;
import org.openhab.binding.domintell.internal.protocol.model.module.ModuleKey;
import org.openhab.binding.domintell.internal.protocol.model.type.ItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Item} class is a Domintell item representation in the binding
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class Item<T extends Serializable> {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(Item.class);

    /**
     * Parent module
     */
    private Module module;

    /**
     * Item identification key
     */
    private ItemKey itemKey;

    /**
     * Item type
     */
    private ItemType type;

    /**
     * Listener for state changes
     */
    @Nullable
    private StateChangeListener stateChangeListener;

    /**
     * Item value
     */
    @Nullable
    private T value;

    /**
     * Last change timestamp
     */
    private long[] lastChanges = { 0, 0, 0, 0 };

    /**
     * Dirty flag
     */
    private boolean changed = false;

    /**
     * Item description
     */
    @Nullable
    private Description description;

    public Item(ItemKey itemkey, Module module, ItemType type) {
        this.itemKey = itemkey;
        this.module = module;
        this.type = type;
    }

    public void setStateChangeListener(@Nullable StateChangeListener stateChangeListener) {
        this.stateChangeListener = stateChangeListener;
    }

    public Module getModule() {
        return module;
    }

    public String getDescription() {
        ModuleKey moduleKey = itemKey.getModuleKey();
        StringBuilder sb = new StringBuilder(
                ItemKey.toLabel(moduleKey.getModuleType(), moduleKey.getSerialNumber(), itemKey.getIoNumber()));
        Description descr = description != null ? description : module.getDescription();
        if (descr != null) {
            sb.append(descr.getLocation());
        }
        return sb.toString();
    }

    public void setDescription(Description description) {
        this.description = description;
        changed = true;
    }

    public ItemType getType() {
        return type;
    }

    public @Nullable T getValue() {
        return value;
    }

    public long[] getChangeDelay() {
        return lastChanges;
    }

    public void setValue(T value) {
        if ((this.value == null && value != null) || (this.value != null && !this.value.equals(value))) {
            lastChanges[0] = lastChanges[1];
            lastChanges[1] = lastChanges[2];
            lastChanges[2] = lastChanges[3];
            lastChanges[3] = System.currentTimeMillis();

            if (logger.isTraceEnabled()) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < 4; i++) {
                    if (sb.length() > 0) {
                        sb.append("/");
                    }
                    sb.append(lastChanges[i]);
                }
                logger.trace("Value change timestamps: [{}]", sb.toString());
            }
            changed = true;
        }
        this.value = value;
    }

    public ItemKey getItemKey() {
        return itemKey;
    }

    public String getLabel() {
        return description != null ? ((@NonNull Description) description).getName()
                : module.getDescription().getName()
                        + (itemKey.getIoNumber() != null ? " " + itemKey.getIoNumber() : "");
    }

    @Override
    public String toString() {
        return "Item{" + "itemKey=" + itemKey + ", type=" + type + ", value=" + value + '}';
    }

    public void notifyStateUpdate() {
        if (stateChangeListener != null) {
            ((@NonNull StateChangeListener) stateChangeListener).itemStateChanged(this);
        }
    }

    public boolean isChanged() {
        return changed;
    }

    public void clearChanged() {
        changed = false;
    }
}
