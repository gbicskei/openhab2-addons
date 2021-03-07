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

import static org.openhab.binding.domintell.internal.DomintellBindingConstants.CHANNEL_SYSTEM_COMMAND;
import static org.openhab.binding.domintell.internal.DomintellBindingConstants.CHANNEL_SYSTEM_DATE;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.domintell.internal.config.BridgeConfig;
import org.openhab.binding.domintell.internal.discovery.DomintellDiscoveryService;
import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.StateListener;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DomintellBridgeHandler} class is responsible for managing the Domintell connection
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class DomintellBridgeHandler extends BaseBridgeHandler {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellBridgeHandler.class);

    /**
     * Domintell connection
     */
    private DomintellConnection connection;

    /**
     * Configuration
     */
    private BridgeConfig config;

    /**
     * Constructor.
     *
     * @param bridge Bridge thing.
     * @param discoveryService Discovery service
     */
    public DomintellBridgeHandler(Bridge bridge, DomintellDiscoveryService discoveryService) {
        super(bridge);
        this.config = getConfigAs(BridgeConfig.class);
        discoveryService.setBridgeUID(thing.getUID());

        connection = new DomintellConnection(config, this::updateGatewayState, discoveryService::addDiscoverable,
                scheduler);
        discoveryService.setDomintellConnection(connection);
        logger.debug("Bridge handler created.");
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        config = getConfigAs(BridgeConfig.class);
        logger.debug("Bridge configuration updated.");
    }

    /**
     * Getter
     *
     * @return Domintell connection
     */
    public @Nullable DomintellConnection getConnection() {
        return connection;
    }

    /**
     * Domintell system command handler.
     *
     * @param channelUID Channel uid
     * @param command Received command
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (connection.isOnline()) {
            if (command instanceof RefreshType) {
                if (CHANNEL_SYSTEM_DATE.equals(channelUID.getId())) {
                    updateDomintellSysDate();
                }
            } else if (CHANNEL_SYSTEM_COMMAND.equals(channelUID.getId())) {
                // connection.scan();
                StringType st = (StringType) command;
                connection.sendCommand(st.toString());
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Starting Domintell connection");
        updateStatus(ThingStatus.UNKNOWN);
        connection.startGateway(config);
    }

    /**
     * Callback function for the connection to update the state of the bridge.
     *
     * @param state New state
     * @param msg Error message (optional)
     */
    private void updateGatewayState(StateListener.State state, @Nullable String msg) {
        if (isInitialized()) {
            switch (state) {
                case ONLINE:
                    updateStatus(ThingStatus.ONLINE);
                    updateDomintellSysDate();
                    break;
                case STARTING_SESSION:
                case INITIALIZING:
                    updateStatus(ThingStatus.UNKNOWN);
                    break;
                case STALE:
                case STOPPING:
                case OFFLINE:
                    updateStatus(ThingStatus.OFFLINE);
                    break;
                case FATAL:
                case ERROR:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, msg);
                    logger.debug("Error received from the connection: {}", msg);
            }
        }
    }

    private void updateDomintellSysDate() {
        Date domintellDate = connection.getDomintellSysdate();
        if (domintellDate != null) {
            DateTimeType value = new DateTimeType(
                    ZonedDateTime.ofInstant(domintellDate.toInstant(), TimeZone.getDefault().toZoneId()));
            updateState(CHANNEL_SYSTEM_DATE, value);
        }
    }

    /**
     * Stopping the connection when the bridge is disposed.
     */
    @Override
    public void dispose() {
        connection.stopGateway();
        super.dispose();
    }
}
