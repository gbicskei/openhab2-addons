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
package org.openhab.binding.domintell.internal;

import static org.openhab.binding.domintell.internal.DomintellBindingConstants.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.domintell.internal.discovery.DomintellDiscoveryService;
import org.openhab.binding.domintell.internal.handler.*;
import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.DomintellRegistry;
import org.openhab.binding.domintell.internal.protocol.model.group.ItemGroup;
import org.openhab.binding.domintell.internal.protocol.model.module.Module;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DomintellHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.domintell", service = ThingHandlerFactory.class)
public class DomintellHandlerFactory extends BaseThingHandlerFactory {
    /**
     * Domintell logger. Uses a common category for all Domintell related logging.
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellHandlerFactory.class);

    /**
     * Discovery service
     */
    @Nullable
    private DomintellDiscoveryService discoveryService;

    /**
     * Domintell connection
     */
    @Nullable
    private DomintellConnection connection;

    /**
     * Measurement unit
     */
    private boolean siUnit;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_MODULE_THING_TYPES_UIDS.contains(thingTypeUID)
                || BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID)
                || SUPPORTED_GROUP_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * By activating the component the bridge discovery service is also started.
     *
     * @param componentContext Component context.
     */
    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        if (discoveryService != null) {
            discoveryService.setBridgeUID(null);
        }
    }

    @Override
    @Nullable
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (BRIDGE_THING_TYPE.equals(thingTypeUID)) {
            if (discoveryService != null) {
                DomintellBridgeHandler domintellBridgeHandler = new DomintellBridgeHandler((Bridge) thing,
                        (@NonNull DomintellDiscoveryService) discoveryService);
                connection = domintellBridgeHandler.getConnection();
                return domintellBridgeHandler;
            } else {
                logger.error("Missing discovery service");
                return null;
            }
        } else if (connection != null) {
            DomintellRegistry registry = connection.getRegistry();
            if (SUPPORTED_MODULE_THING_TYPES_UIDS.contains(thingTypeUID)) {
                DomintellModuleHandler handler = null;
                switch (thingTypeUID.getId()) {
                    case MODULE_TRP:
                    case MODULE_BIR:
                    case MODULE_DMR:
                        handler = new DomintellReleyModuleHandler(thing, registry);
                        break;
                    case MODULE_IS4:
                    case MODULE_IS8:
                        handler = new DomintellContactModuleHandler(thing, registry);
                        break;
                    case MODULE_PBX:
                        handler = new DomintellPushButtonModuleHandler(thing, registry);
                        break;
                    case MODULE_TEX:
                        handler = new DomintellThermostatModuleHandler(thing, registry, siUnit);
                        break;
                    case MODULE_D10:
                    case MODULE_DIM:
                        handler = new DomintellDimmerModuleHandler(thing, registry);
                        break;
                    case MODULE_TRV:
                        handler = new DomintellShutterModuleHandler(thing, registry);
                        break;
                }
                if (handler != null && discoveryService != null) {
                    discoveryService.removeModule(handler.getModule());
                }
                return handler;
            } else if (THING_TYPE_GROUP.equals(thingTypeUID) && discoveryService != null) {
                try {
                    DomintellItemGroupHandler handler = new DomintellVariableItemGroupHandler(thing, registry);
                    // remove from discovery
                    @Nullable
                    ItemGroup<?> itemGroup = handler.getItemGroup();
                    if (itemGroup != null && discoveryService != null) {
                        discoveryService.removeGroup((@NonNull ItemGroup<?>) itemGroup);
                    }

                    return handler;
                } catch (IllegalArgumentException e) {
                    logger.debug("Unknown module type: {}", thingTypeUID.getId());
                }
            }
        }
        return null;
    }

    /**
     * Unregister module discovery service when the bridge handler is removed.
     *
     * @param thingHandler Bridge thing handler
     */
    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof DomintellBridgeHandler) {
            if (discoveryService != null) {
                discoveryService.setBridgeUID(null);
            }
        } else if (thingHandler instanceof DomintellItemGroupHandler) {
            @Nullable
            ItemGroup<?> itemGroup = ((DomintellItemGroupHandler) thingHandler).getItemGroup();
            if (itemGroup != null) {
                itemGroup.setItemChangeListener(null);
                itemGroup.setStateChangeListener(null);
            }
        } else if (thingHandler instanceof DomintellModuleHandler) {
            Module module = ((DomintellModuleHandler) thingHandler).getModule();
            module.setConfigChangeListener(null);
            module.setStateChangeListener(null);
        }
    }

    @Reference
    protected void setDomintellDiscoveryService(DomintellDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    protected void unsetDomintellDiscoveryService(DomintellDiscoveryService discoveryService) {
        this.discoveryService = null;
    }
}
