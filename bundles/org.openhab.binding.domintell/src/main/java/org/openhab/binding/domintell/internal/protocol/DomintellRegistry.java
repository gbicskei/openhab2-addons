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
package org.openhab.binding.domintell.internal.protocol;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.domintell.internal.protocol.model.SerialNumber;
import org.openhab.binding.domintell.internal.protocol.model.group.ItemGroup;
import org.openhab.binding.domintell.internal.protocol.model.group.ItemGroupType;
import org.openhab.binding.domintell.internal.protocol.model.module.Module;
import org.openhab.binding.domintell.internal.protocol.model.module.ModuleKey;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;

/**
 * The {@link DomintellRegistry} class is a registry for all Domintell modules and groups
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class DomintellRegistry {
    /**
     * Module cache
     */
    private final HashMap<ModuleKey, Module> moduleCache = new HashMap<>();

    /**
     * Item groups
     */
    private final HashMap<ItemGroupType, ItemGroup<?>> itemGroups = new HashMap<>();

    /**
     * Configuration event listener
     */
    @Nullable
    private ConfigurationEventHandler configEventListener;

    /**
     * Domintell connection
     */
    private DomintellConnection connection;

    public DomintellRegistry(DomintellConnection connection) {
        this.connection = connection;
    }

    /**
     * Setter.
     *
     * @param configEventListener Config event listener instance
     */
    void setConfigEventListener(@Nullable ConfigurationEventHandler configEventListener) {
        this.configEventListener = configEventListener;
    }

    /**
     * Retrieves Domintell module from the cache. The module is created if missing.
     *
     * @param serialNumber Module serialNumber
     * @return Requested module
     */
    public Module getDomintellModule(ModuleType moduleType, SerialNumber serialNumber) {
        @Nullable
        Class<? extends Module> moduleClass = moduleType.getClazz();
        if (moduleClass != null) {
            ModuleKey key = new ModuleKey(moduleType, serialNumber);
            Module module = moduleCache.get(key);
            if (module == null) {
                // missing the module - creating
                try {
                    Constructor<? extends Module> constructor = moduleClass.getConstructor(DomintellConnection.class,
                            SerialNumber.class);
                    module = constructor.newInstance(connection, serialNumber);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unable to instantiate module", e);
                }
            }

            if (module.getConfigChangeListener() == null && configEventListener != null) {
                ((@NonNull ConfigurationEventHandler) configEventListener).handleNewDiscoverable(module);
            }

            if (!(moduleClass.isInstance(module))) {
                throw new IllegalArgumentException("Invalid module type found at given serialNumber: " + serialNumber);
            }
            moduleCache.put(key, module);
            return (@NonNull Module) module;
        }
        throw new IllegalArgumentException("No class was defined for module type: " + moduleType);
    }

    /**
     * Find or create new item group by type
     *
     * @param type Group type
     * @return Requested item group
     */
    public <T extends Serializable> ItemGroup<T> getItemGroup(ItemGroupType type) {
        @SuppressWarnings("unchecked")
        ItemGroup<T> itemGroup = (ItemGroup<T>) itemGroups.get(type);
        if (itemGroup == null) {
            itemGroup = new ItemGroup<T>(connection, type);
            itemGroups.put(type, itemGroup);
            if (configEventListener != null) {
                ((@NonNull ConfigurationEventHandler) configEventListener).handleNewDiscoverable(itemGroup);
            }
        }
        return itemGroup;
    }

    public Collection<Module> getModules() {
        return Collections.unmodifiableCollection(moduleCache.values());
    }

    Collection<ItemGroup<?>> getGroups() {
        return Collections.unmodifiableCollection(itemGroups.values());
    }
}
