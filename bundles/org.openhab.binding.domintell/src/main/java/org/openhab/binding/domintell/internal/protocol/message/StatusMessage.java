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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.domintell.internal.protocol.model.SerialNumber;
import org.openhab.binding.domintell.internal.protocol.model.type.DataType;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;

/**
 * The {@link StatusMessage} class is responsible for parsing messages received from Domintell system
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class StatusMessage extends BaseMessage {
    /**
     * Module type
     */
    private ModuleType moduleType;

    /**
     * Serial number
     */
    private SerialNumber serialNumber;

    /**
     * IO number
     */
    @Nullable
    private Integer ioNumber;

    /**
     * Date type
     */
    @Nullable
    private DataType dataType;

    /**
     * Received data
     */
    private String data;

    /**
     * Constructor
     *
     * @param message Message to parse
     */
    StatusMessage(String message, boolean isDesciption) {
        super(Type.DATA, message);

        moduleType = ModuleType.valueOf(message.substring(0, 3));
        serialNumber = new SerialNumber("0x" + message.substring(3, 9).trim());
        int dataTypeIdx = 9;
        if (message.charAt(9) == '-') {
            if (ModuleType.DAL == moduleType) {
                ioNumber = Integer.parseInt(message.substring(10, 12), 16);
                dataTypeIdx = 12;
            } else {
                ioNumber = Integer.parseInt(message.substring(10, 11), 16);
                dataTypeIdx = 11;
            }
        }
        if (isDesciption) {
            data = message.substring(dataTypeIdx);
        } else {
            dataType = DataType.valueOf(message.substring(dataTypeIdx, dataTypeIdx + 1));
            data = message.substring(dataTypeIdx + 1);
        }
    }

    // getters

    public ModuleType getModuleType() {
        return moduleType;
    }

    public SerialNumber getSerialNumber() {
        return serialNumber;
    }

    public @Nullable Integer getIoNumber() {
        return ioNumber;
    }

    public @Nullable DataType getDataType() {
        return dataType;
    }

    public @Nullable String getData() {
        return data != null ? data.trim() : null;
    }

    @Override
    public String toString() {
        return "StatusMessage{" + "moduleType=" + moduleType + ", serialNumber=" + serialNumber + ", ioNumber="
                + ioNumber + ", dataType=" + dataType + ", data='" + data + '\'' + '}';
    }
}
