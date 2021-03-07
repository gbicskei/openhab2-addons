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
package org.openhab.binding.domintell.internal.profile;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.domintell.internal.DomintellBindingConstants;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.profiles.*;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DomintellProfileFactory} class defines and provides button triggerlog profile and its type of this
 * binding.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
@Component(service = { ProfileFactory.class, ProfileAdvisor.class, ProfileTypeProvider.class })
public class DomintellProfileFactory implements ProfileFactory, ProfileAdvisor, ProfileTypeProvider {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellProfileFactory.class);

    /**
     * Profile UID for trigger events
     */
    static final ProfileTypeUID UID_BUTTON_TRIGGER = new ProfileTypeUID(DomintellBindingConstants.BINDING_ID,
            "button-trigger");

    /**
     * Profile type for trigger events
     */
    private static final TriggerProfileType BUTTON_TRIGGER_TYPE = ProfileTypeBuilder
            .newTrigger(UID_BUTTON_TRIGGER, "Button Trigger").withSupportedItemTypes(CoreItemFactory.STRING)
            .withSupportedChannelTypeUIDs(DomintellBindingConstants.CHANNEL_TYPE_BUTTON_TRIGGER).build();

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return Stream.of(UID_BUTTON_TRIGGER).collect(Collectors.toSet());
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return Stream.of(BUTTON_TRIGGER_TYPE).collect(Collectors.toSet());
    }

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(Channel channel, @Nullable String itemType) {
        return getSuggestedProfileTypeUID(channel.getChannelTypeUID(), itemType);
    }

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(ChannelType channelType, @Nullable String itemType) {
        return getSuggestedProfileTypeUID(channelType.getUID(), itemType);
    }

    private @Nullable ProfileTypeUID getSuggestedProfileTypeUID(@Nullable ChannelTypeUID channelTypeUID,
            @Nullable String itemType) {
        if (CoreItemFactory.STRING.equals(itemType)
                && DomintellBindingConstants.CHANNEL_TYPE_BUTTON_TRIGGER.equals(channelTypeUID)) {
            return UID_BUTTON_TRIGGER;
        }
        return null;
    }

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {
        if (UID_BUTTON_TRIGGER.equals(profileTypeUID)) {
            logger.debug("Button trigger profile preated");
            return new DomintellButtonTriggerProfile(callback, profileContext);
        }
        logger.debug("No profile preated for: {}", profileTypeUID);
        return null;
    }
}
