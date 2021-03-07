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

import static org.openhab.binding.domintell.internal.DomintellBindingConstants.*;

import java.util.StringTokenizer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.message.ActionMessageBuilder;
import org.openhab.binding.domintell.internal.protocol.message.StatusMessage;
import org.openhab.binding.domintell.internal.protocol.model.Item;
import org.openhab.binding.domintell.internal.protocol.model.SerialNumber;
import org.openhab.binding.domintell.internal.protocol.model.type.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DTEMModule} is the model class for Domintell thermostats (DTEM01 and DTEM02)
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class DTEMModule extends Module {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(DTEMModule.class);

    /**
     * Measured value
     */
    private Item<Float> heatingCurrentValue;

    /**
     * Cooling setpoint
     */
    private Item<Float> coolingCurrentValue;

    /**
     * Heating preset value
     */
    private Item<Float> heatingPresetValue;

    /**
     * Cooling preset value
     */
    private Item<Float> coolingPresetValue;

    /**
     * Temperature mode
     */
    private Item<TemperatureType> temperatureType;

    /**
     * Regulation mode
     */
    private Item<RegulationType> regulationType;

    /**
     * Heating profile value
     */
    private Item<Float> heatingProfileValue; // T for heating, U for cooling

    /**
     * Cooling profile value
     */
    private Item<Float> coolingProfileValue; // T for heating, U for cooling

    public DTEMModule(DomintellConnection connection, SerialNumber serialNumber) {
        super(connection, ModuleType.TE1, serialNumber);

        // add channels
        coolingCurrentValue = addItem(CHANNEL_COOLING_CURRENT_VALUE, ItemType.numericVar, Float.class);
        heatingCurrentValue = addItem(CHANNEL_HEATING_CURRENT_VALUE, ItemType.numericVar, Float.class);
        heatingPresetValue = addItem(CHANNEL_HEATING_PRESET_VALUE, ItemType.numericVar, Float.class);
        coolingPresetValue = addItem(CHANNEL_COOLING_PRESET_VALUE, ItemType.numericVar, Float.class);
        temperatureType = addItem(CHANNEL_HEATING_MODE, ItemType.numericVar, TemperatureType.class);
        regulationType = addItem(CHANNEL_COOLING_MODE, ItemType.numericVar, RegulationType.class);
        heatingProfileValue = addItem(CHANNEL_HEATING_PROFILE_VALUE, ItemType.numericVar, Float.class);
        coolingProfileValue = addItem(CHANNEL_COOLING_PROFILE_VALUE, ItemType.numericVar, Float.class);
    }

    /**
     * Update module state from Domintell status message
     *
     * @param message Message to process
     */
    protected void updateItems(StatusMessage message) {
        try {
            // T 0.0 18.0 AUTO 18.0
            @Nullable
            DataType dataType = message.getDataType();
            if (dataType != null) {
                StringTokenizer st = new StringTokenizer(message.getData());

                if (dataType == DataType.T) {
                    heatingCurrentValue.setValue(Float.parseFloat(st.nextToken()));
                    heatingPresetValue.setValue(Float.parseFloat(st.nextToken()));
                    temperatureType.setValue(TemperatureType.valueOf(st.nextToken()));
                    heatingProfileValue.setValue(Float.parseFloat(st.nextToken()));
                } else if (dataType == DataType.U) {
                    coolingCurrentValue.setValue(Float.parseFloat(st.nextToken()));
                    coolingPresetValue.setValue(Float.parseFloat(st.nextToken()));
                    regulationType.setValue(RegulationType.valueOf(st.nextToken()));
                    coolingProfileValue.setValue(Float.parseFloat(st.nextToken()));
                }
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid TE1 module status value: {}", message, e);
        }
    }

    /**
     * Declaring the module discoverable by the discovery service
     *
     * @return True
     */
    @Override
    public boolean isDiscoverable() {
        return true;
    }

    /**
     * Sets a setpoint for the thermostat
     *
     * @param action Action to perform: HEATING | COOLING
     * @param value Value to set
     */
    public void setSetpoint(ActionType action, double value) {
        getConnection().sendCommand(
                ActionMessageBuilder.createFromModuleKey(getModuleKey()).withAction(action).withValue(value).build());
    }

    /**
     * Sets mode for thermostat
     * 
     * @param action Action to perform: TEMPERATURE_MODE | REGULATION_MODE
     * @param mode Mode to set
     */
    public void setMode(ActionType action, SelectionProvider mode) {
        getConnection().sendCommand(ActionMessageBuilder.createFromModuleKey(getModuleKey()).withAction(action)
                .withSelection(mode).build());
    }
}
