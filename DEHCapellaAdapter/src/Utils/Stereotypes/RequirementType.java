/*
 * RequirementType.java
 *
 * Copyright (c) 2020-2021 RHEA System S.A.
 *
 * Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski, Antoine Théate
 *
 * This file is part of DEH-Capella
 *
 * The DEH-Capella is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * The DEH-Capella is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package Utils.Stereotypes;

import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.polarsys.capella.core.data.requirement.*;

/**
 * The {@linkplain RequirementType} is an enumerations of all Capella requirements type
 */
public enum RequirementType
{
    /**
     * Represents an undefined {@linkplain Requirement}
     */
    Undefined("", null),
    
    /**
     * Represents a {@linkplain SystemNonFunctionalRequirement}
     */
    NonFunctional("Non-Functional", SystemNonFunctionalRequirement.class),
    
    /**
     * Represents a {@linkplain SystemNonFunctionalInterfaceRequirement}
     */
    NonFunctionalInterface("Non-Functional Interface", SystemNonFunctionalInterfaceRequirement.class),
    
    /**
     * Represents a {@linkplain SystemFunctionalRequirement}
     */
    Functional("Functional", SystemFunctionalRequirement.class),
    
    /**
     * Represents a {@linkplain SystemFunctionalInterfaceRequirement}
     */
    FunctionalInterface("Functional Interface", SystemFunctionalInterfaceRequirement.class),
    
    /**
     * Represents a {@linkplain SystemUserRequirement}
     */
    User("User Requirement", SystemUserRequirement.class);

    /**
     * The current class logger
     */
    private static final Logger logger = LogManager.getLogger();

    /**
     * The {@linkplain Class} represented by this enum value
     */
    private final Class<? extends Requirement> clazz;
    
    /**
     * The display-able label that represent this enum value
     */
    public final String Label;
    
    /**
     * Initializes a new {@linkplain RequirementType}
     * 
     * @param label the label associated to the enumeration value
     * @param clazz the class associated to the enumeration value
     */
    private RequirementType(String label, Class<? extends Requirement> clazz)
    {
        this.Label = label;
        this.clazz = clazz;
    }

    /**
     * Returns the name of this enum constant, as contained in the declaration
     * 
     * @return a {@linkplain String}
     */
    @Override
    public String toString()
    {
        return this.Label;
    }
    
    /**
     * Gets an instance of {@linkplain RequirementType} with the {@linkplain clazz} matching the provided {@linkplain Class} 
     * 
     * @param requirementClass the {@linkplain class} that could match the {@linkplain clazz} from one of the possible enum value
     * @return a {@linkplain RequirementType}
     */
    public static RequirementType From(Class<? extends Requirement> requirementClass)
    {
        return Arrays.asList(RequirementType.values())
                .stream()
                .filter(x -> x.clazz == requirementClass)
                .findFirst()
                .orElse(RequirementType.Undefined);
    }
    
    /**
     * Gets an instance of {@linkplain RequirementType} with the value or {@linkplain Label} matching the provided {@linkplain String} 
     * 
     * @param valueOrLabel a {@linkplain String} that could potentially match the {@linkplain Label} 
     * or value of one of the enumeration value from this {@linkplain RequirementType}
     * @return a {@linkplain RequirementType}
     */
    public static RequirementType From(String valueOrLabel)
    {
        try
        {
            return RequirementType.valueOf(valueOrLabel);
        }
        catch(IllegalArgumentException exception)
        {
            logger.catching(exception);
        }
        
        for(var value : RequirementType.values())
        {
            if(value.Label.equalsIgnoreCase(valueOrLabel)) 
            {
                return value;
            }
        }

        return RequirementType.Undefined;
    }
}
