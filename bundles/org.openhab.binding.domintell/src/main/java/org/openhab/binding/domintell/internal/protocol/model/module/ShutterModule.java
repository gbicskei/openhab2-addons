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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.message.ActionMessageBuilder;
import org.openhab.binding.domintell.internal.protocol.message.StatusMessage;
import org.openhab.binding.domintell.internal.protocol.model.Item;
import org.openhab.binding.domintell.internal.protocol.model.ItemKey;
import org.openhab.binding.domintell.internal.protocol.model.SerialNumber;
import org.openhab.binding.domintell.internal.protocol.model.type.ActionType;
import org.openhab.binding.domintell.internal.protocol.model.type.ItemType;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;

/**
 * The {@link ShutterModule} class is a base class for all shutter type Domintell modules
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public abstract class ShutterModule extends Module {
    private static Integer UP = 0;
    private static Integer DOWN = 100;
    private static Integer MIDDLE = 50;

    ShutterModule(DomintellConnection connection, ModuleType type, SerialNumber serialNumber, int inputNum) {
        super(connection, type, serialNumber);

        // add channels
        for (int i = 0; i < inputNum; i++) {
            addItem(i + 1, ItemType.shutter, Integer.class);
        }
    }

    protected void updateItems(StatusMessage message) {
        updateShutterItems(message);
    }

    private void updateShutterItems(StatusMessage message) {
        String data = message.getData();
        if (data != null) {
            int state = Integer.parseInt(data, 16);
            Map<ItemKey, Item<?>> items = getItems();
            for (int i = 0; i < items.size(); i++) {
                int idx = i * 2;
                int mask1 = 1 << idx;
                int mask2 = 1 << (idx + 1);

                @SuppressWarnings("unchecked")
                Item<Integer> item = (Item<Integer>) items.get(new ItemKey(getModuleKey(), i + 1));
                if (item != null) {
                    if ((state & mask1) == mask1) {
                        item.setValue(UP);
                    } else if ((state & mask2) == mask2) {
                        item.setValue(DOWN);
                    } else {
                        item.setValue(MIDDLE);
                    }
                }
            }
        }
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    /**
     * Set boolean value
     *
     * @param idx Output index
     */
    public void goHigh(int idx) {
        getConnection().sendCommand(ActionMessageBuilder.createFromModuleKey(getModuleKey()).withIONumber(getIO(idx))
                .withAction(ActionType.SET_SHUTTER).build());
    }

    /**
     * Reset boolean output
     *
     * @param idx Output index
     */
    public void goLow(int idx) {
        getConnection().sendCommand(ActionMessageBuilder.createFromModuleKey(getModuleKey()).withIONumber(getIO(idx))
                .withAction(ActionType.RESET_SHUTTER).build());
    }

    public void stop(int idx) {
        getConnection().sendCommand(ActionMessageBuilder.createFromModuleKey(getModuleKey()).withIONumber(getIO(idx))
                .withAction(ActionType.STOP_SHUTTER).build());
    }

    private int getIO(int idx) {
        return (idx - 1) * 2 + 1;
    }
}
