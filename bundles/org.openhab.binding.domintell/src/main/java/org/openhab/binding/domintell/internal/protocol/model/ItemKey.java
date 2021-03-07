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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.domintell.internal.protocol.model.module.ModuleKey;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;

/**
 * The {@link ItemKey} class is key for item identification
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class ItemKey {
    /**
     * Module key
     */
    private ModuleKey moduleKey;

    /**
     * IO number
     */
    @Nullable
    private Integer ioNumber;

    /**
     * Item name if IO number is not applicable
     */
    @Nullable
    private String name;

    public ItemKey(ModuleKey moduleKey) {
        this.moduleKey = moduleKey;
    }

    public ItemKey(ModuleKey moduleKey, Integer ioNumber) {
        this.moduleKey = moduleKey;
        this.ioNumber = ioNumber;
    }

    public ItemKey(ModuleKey moduleKey, String name) {
        this.moduleKey = moduleKey;
        this.name = name;
    }

    public ModuleKey getModuleKey() {
        return moduleKey;
    }

    public @Nullable Integer getIoNumber() {
        return ioNumber;
    }

    public @Nullable String getName() {
        return name;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ItemKey itemKey = (ItemKey) o;
        return Objects.equals(moduleKey, itemKey.moduleKey) && Objects.equals(ioNumber, itemKey.ioNumber)
                && Objects.equals(name, itemKey.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleKey, ioNumber, name);
    }

    @Override
    public String toString() {
        return toLabel(moduleKey.getModuleType(), moduleKey.getSerialNumber(), ioNumber);
    }

    public static String toLabel(ModuleType moduleType, SerialNumber serialNumber, @Nullable Integer ioNumber) {
        StringBuilder sb = new StringBuilder(moduleType.toString()).append(serialNumber.toStringFix6());
        if (ioNumber != null) {
            sb.append("-").append(ioNumber);
        }
        return sb.toString();
    }

    public String toId() {
        StringBuilder sb = new StringBuilder(moduleKey.getModuleType().toString()).append("-")
                .append(moduleKey.getSerialNumber().getAddressHex());
        if (ioNumber != null) {
            sb.append("-").append(ioNumber);
        }
        return sb.toString();
    }
}
