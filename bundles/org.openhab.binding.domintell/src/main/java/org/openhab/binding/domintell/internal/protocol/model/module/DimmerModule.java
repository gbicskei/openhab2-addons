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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.message.ActionMessageBuilder;
import org.openhab.binding.domintell.internal.protocol.message.StatusMessage;
import org.openhab.binding.domintell.internal.protocol.model.Item;
import org.openhab.binding.domintell.internal.protocol.model.SerialNumber;
import org.openhab.binding.domintell.internal.protocol.model.type.ActionType;
import org.openhab.binding.domintell.internal.protocol.model.type.DataType;
import org.openhab.binding.domintell.internal.protocol.model.type.ItemType;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;

/**
 * The {@link DimmerModule} class represents dimmer type Domintell modules
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public abstract class DimmerModule extends Module {
    DimmerModule(DomintellConnection connection, ModuleType type, SerialNumber serialNumber, int itemNum) {
        super(connection, type, serialNumber);

        // add channels
        for (int i = 0; i < itemNum; i++) {
            addItem(i + 1, ItemType.numericVar, Integer.class);
        }
    }

    protected void updateItems(StatusMessage message) {
        @Nullable
        String dataStr = message.getData();
        @Nullable
        DataType dataType = message.getDataType();
        if (dataStr != null && dataType == DataType.D) {
            // 064 0 0 0 0 0 0
            if (dataStr.length() < getItems().size() * 2) {
                dataStr = "0" + dataStr.replace(' ', '0');
            }
            String data = dataStr;
            getItems().values().forEach(i -> {
                @SuppressWarnings("unchecked")
                Item<Integer> item = (Item<Integer>) i;
                Integer idx = i.getItemKey().getIoNumber();
                if (idx != null) {
                    String valueStr = data.substring((idx - 1) * 2, 2 * idx);
                    Integer value = Integer.parseInt(valueStr, 16);
                    item.setValue(value);
                }
            });
        }
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    /**
     * Turn ON
     *
     * @param idx Contact index
     */
    public void on(int idx) {
        percent(idx, 100);
    }

    /**
     * Turn OFF
     *
     * @param idx Contact index
     */
    public void off(int idx) {
        percent(idx, 0);
    }

    /**
     * Set percent
     *
     * @param idx Contact index
     */
    public void percent(int idx, int percent) {
        getConnection().sendCommand(ActionMessageBuilder.createFromModuleKey(getModuleKey()).withIONumber(idx)
                .withAction(ActionType.SET_DOMMER_OR_VOLUME).withValue((double) percent).build());
    }

    /**
     * Increase
     *
     * @param idx Contact index
     */
    public void increase(int idx) {
        getConnection().sendCommand(ActionMessageBuilder.createFromModuleKey(getModuleKey()).withIONumber(idx)
                .withAction(ActionType.INCREASE_BY).withValue(10d).build());
    }

    /**
     * Increase
     *
     * @param idx Contact index
     */
    public void decrease(int idx) {
        getConnection().sendCommand(ActionMessageBuilder.createFromModuleKey(getModuleKey()).withIONumber(idx)
                .withAction(ActionType.DECREASE_BY).withValue(10d).build());
    }
}
