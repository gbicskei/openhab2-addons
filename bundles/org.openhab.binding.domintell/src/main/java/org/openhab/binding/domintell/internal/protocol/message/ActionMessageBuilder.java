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
package org.openhab.binding.domintell.internal.protocol.message;

import java.text.NumberFormat;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.domintell.internal.protocol.model.ItemKey;
import org.openhab.binding.domintell.internal.protocol.model.SerialNumber;
import org.openhab.binding.domintell.internal.protocol.model.module.ModuleKey;
import org.openhab.binding.domintell.internal.protocol.model.type.ActionType;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;
import org.openhab.binding.domintell.internal.protocol.model.type.SelectionProvider;

/**
 * The {@link ActionMessageBuilder} class is message builder for outgoing Domintell messages
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class ActionMessageBuilder {
    /**
     * Temperature format
     */
    private static NumberFormat temperatureFormat;

    /**
     * Tuner frequency format
     */
    private static NumberFormat frequencyFormat;

    // initialize formats
    static {
        temperatureFormat = NumberFormat.getInstance(Locale.US);
        temperatureFormat.setMinimumIntegerDigits(2);
        temperatureFormat.setMaximumIntegerDigits(2);
        temperatureFormat.setMinimumFractionDigits(1);
        temperatureFormat.setMaximumFractionDigits(1);

        frequencyFormat = NumberFormat.getInstance(Locale.US);
        frequencyFormat.setMinimumIntegerDigits(3);
        frequencyFormat.setMaximumIntegerDigits(3);
        frequencyFormat.setMinimumFractionDigits(4);
        frequencyFormat.setMaximumFractionDigits(4);
    }

    /**
     * Module type
     */
    @NonNullByDefault({})
    private ModuleType moduleType;

    /**
     * Serial number
     */
    @NonNullByDefault({})
    private SerialNumber serialNumber;

    /**
     * IO number
     */
    @Nullable
    private Integer ioNumber;

    /**
     * Requested action
     */
    @NonNullByDefault({})
    private ActionType action;

    /**
     * Value to send
     */
    @Nullable
    private Double numValue;

    private ActionMessageBuilder() {
    }

    public static ActionMessageBuilder createFromItemKey(ItemKey key) {
        ActionMessageBuilder builder = new ActionMessageBuilder();

        builder.moduleType = key.getModuleKey().getModuleType();
        builder.serialNumber = key.getModuleKey().getSerialNumber();
        builder.ioNumber = key.getIoNumber();

        return builder;
    }

    public static ActionMessageBuilder createFromModuleKey(ModuleKey key) {
        ActionMessageBuilder builder = new ActionMessageBuilder();

        builder.moduleType = key.getModuleType();
        builder.serialNumber = key.getSerialNumber();

        return builder;
    }

    public ActionMessageBuilder withIONumber(Integer ioNumber) {
        this.ioNumber = ioNumber;
        return this;
    }

    public ActionMessageBuilder withAction(ActionType action) {
        this.action = action;
        return this;
    }

    public ActionMessageBuilder withValue(Double numValue) {
        this.numValue = numValue;
        return this;
    }

    public ActionMessageBuilder withSelection(SelectionProvider selection) {
        this.numValue = (double) selection.getValue();
        return this;
    }

    /**
     * Format the message
     *
     * @return Message string
     */
    public String build() {
        StringBuilder sb = new StringBuilder("&").append(
                ItemKey.toLabel((@NonNull ModuleType) moduleType, (@NonNull SerialNumber) serialNumber, ioNumber));
        String[] actionString = action.getActionString();
        if (actionString.length == 1) {
            sb.append(actionString[0]);
            if (action.isDecimalValueNeeded() || action.isFrequencyValueNeeded() || action.isTemperatureValueNeeded()
                    || action.isSelectionValueNeeded()) {
                if (numValue != null) {
                    if (action.isSelectionValueNeeded() || action.isDecimalValueNeeded()) {
                        sb.append(((@NonNull Double) numValue).intValue());
                    } else if (action.isTemperatureValueNeeded()) {
                        sb.append(temperatureFormat.format((@NonNull Object) numValue));
                    } else {
                        sb.append(frequencyFormat.format((@NonNull Object) numValue));
                    }
                } else {
                    throw new IllegalArgumentException("Missing value for action message: " + action);
                }
            }
        } else {
            String base = sb.toString();
            sb = new StringBuilder();
            for (String action : actionString) {
                sb.append(base).append(action);
            }
        }
        return sb.toString();
    }
}
