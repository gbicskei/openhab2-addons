/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.model.SerialNumber;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;

/**
* The {@link DMR01Module} class is model class for DMR01 Domintell module
*
* @author Gabor Bicskei - Initial contribution
*/
public class DMR01Module extends ReleyModule {
    public DMR01Module(DomintellConnection connection, SerialNumber serialNumber) {
        super(connection, ModuleType.DMR, serialNumber, 5);
    }
}
