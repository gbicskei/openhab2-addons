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

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.domintell.internal.protocol.DomintellRegistry;
import org.openhab.binding.domintell.internal.protocol.model.Item;
import org.openhab.binding.domintell.internal.protocol.model.ItemKey;
import org.openhab.binding.domintell.internal.protocol.model.module.DTEMModule;
import org.openhab.binding.domintell.internal.protocol.model.type.ActionType;
import org.openhab.binding.domintell.internal.protocol.model.type.RegulationType;
import org.openhab.binding.domintell.internal.protocol.model.type.SelectionProvider;
import org.openhab.binding.domintell.internal.protocol.model.type.TemperatureType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DomintellThermostatModuleHandler} class is a handler for Domintell DTEM01 and DTEM02 thermostats
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class DomintellThermostatModuleHandler extends DomintellModuleHandler {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellThermostatModuleHandler.class);

    /**
     * Measurement unit
     */
    private boolean siUnit;

    /**
     * Constructor.
     *
     * @param thing Parent thing
     * @param registry Domintell item registry
     * @param siUnit Measurement flag: true for SI
     */
    public DomintellThermostatModuleHandler(Thing thing, DomintellRegistry registry, boolean siUnit) {
        super(thing, registry);
        this.siUnit = siUnit;
    }

    /**
     * Update channel from item
     *
     * @param item Updated item
     * @param channel Affected channel
     */
    @Override
    protected void updateChannel(Item<?> item, Channel channel) {
        switch (channel.getUID().getId()) {
            case CHANNEL_HEATING_MODE:
            case CHANNEL_COOLING_MODE:
                SelectionProvider selection = (SelectionProvider) item.getValue();
                updateState(channel.getUID(),
                        selection != null ? new DecimalType(selection.getValue()) : UnDefType.NULL);
                logger.trace("Channel update: {}.{}->{}", item.getModule().getModuleKey(), channel.getUID().getId(),
                        item.getValue());
                break;
            default:
                Float f = (Float) item.getValue();
                if (f != null) {
                    QuantityType<Temperature> temperatureQuantityType = new QuantityType<>(f,
                            siUnit ? SIUnits.CELSIUS : ImperialUnits.FAHRENHEIT);
                    updateState(channel.getUID(), temperatureQuantityType);
                    logger.trace("Channel update: {}.{}->{}", item.getModule().getModuleKey(), channel.getUID().getId(),
                            item.getValue());
                }
        }
    }

    public DTEMModule getModule() {
        return (DTEMModule) super.getModule();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_HEATING_MODE:
                DecimalType heating = (DecimalType) command;
                TemperatureType tempMode = TemperatureType.byValue(heating.intValue());
                getModule().setMode(ActionType.TEMPERATURE_MODE, tempMode);
                break;
            case CHANNEL_COOLING_MODE:
                DecimalType cooling = (DecimalType) command;
                RegulationType regMode = RegulationType.byValue(cooling.intValue());
                getModule().setMode(ActionType.REGULATION_MODE, regMode);
                break;
            case CHANNEL_COOLING_PRESET_VALUE:
                @SuppressWarnings("unchecked")
                QuantityType<Temperature> cPreset = (QuantityType<Temperature>) command;
                @Nullable
                QuantityType<Temperature> qtc = cPreset.toUnit(SIUnits.CELSIUS);
                if (qtc != null) {
                    getModule().setSetpoint(ActionType.COOLING, qtc.doubleValue());
                } else {
                    logger.debug("Unable to convert command to Celsius quantity: {}", command);
                }
                break;
            case CHANNEL_HEATING_PRESET_VALUE:
                @SuppressWarnings("unchecked")
                QuantityType<Temperature> hPreset = (QuantityType<Temperature>) command;
                @Nullable
                QuantityType<Temperature> qth = hPreset.toUnit(SIUnits.CELSIUS);
                if (qth != null) {
                    getModule().setSetpoint(ActionType.COOLING, qth.doubleValue());
                } else {
                    logger.debug("Unable to convert command to Celsius quantity: {}", command);
                }
                break;
            default:
                if (RefreshType.REFRESH == command) {
                    Item<?> item = getModule().getItems()
                            .get(new ItemKey(getModule().getModuleKey(), channelUID.getId()));
                    Channel channel = thing.getChannel(channelUID.getId());
                    if (item != null && channel != null) {
                        updateChannel(item, channel);
                    }
                }
        }
    }

    protected @Nullable Channel getChannelForItem(Item<?> item) {
        if (item.getItemKey().getName() != null) {
            return thing.getChannel((@NonNull String) item.getItemKey().getName());
        }
        return null;
    }
}
