/*
 * StereotypeUtils.java
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

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.requirement.RequirementPackage;

import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

/**
 * The {@linkplain StereotypeUtils}  provides useful methods on Capella components
 */
public final class StereotypeUtils
{ 
    /**
     * Gets a 10-25 compliant short name from the provided stereotype name
     * 
     * @param name the {@linkplain String} name to base the short name on
     * @return a {@linkplain string}
     */
    public static String GetShortName(String name)
    {
        return name.replaceAll("[^a-zA-Z0-9]|\\s", "");
    }

    /**
     * Gets a 10-25 compliant short name from the provided stereotype name
     * 
     * @param namedElement the {@linkplain ENamedElement} to base the short name on its name
     * @return a {@linkplain string}
     */
    public static String GetShortName(ENamedElement namedElement)
    {
        return GetShortName(namedElement.getName());
    }

    /**
     * Gets a 10-25 compliant short name from the provided stereotype name
     * 
     * @param namedElement the {@linkplain NamedElement} to base the short name on its name
     * @return a {@linkplain string}
     */
    public static String GetShortName(NamedElement namedElement)
    {
        return GetShortName(namedElement.getName());
    }
    /**
     * Gets the children from the provided {@linkplain EObject} if they are assignable from with the provided {@linkplain Class}
     * 
     * @param <TCapellaElement> the {@linkplain Type} to look for 
     * @param element the {@linkplain EObject} to get the children from
     * @param clazz the {@linkplain Class} of the {@linkplain TCapellaElement} parameter
     * @return a {@linkplain Collection} of element typed as the one specified by the {@linkplain TCapellaElement} parameter
     */
    @SuppressWarnings("unchecked")
    public static <TCapellaElement> Collection<TCapellaElement> GetChildren(EObject element, Class<TCapellaElement> clazz)
    {
        var result = new ArrayList<TCapellaElement>();
        
        if(element.eContents() == null)
        {
            return result;
        }
        
        for (var child : element.eContents())
        {
            if(clazz.isAssignableFrom(child.getClass())) 
            {
                result.add((TCapellaElement) child);
            }
        }
        
        return result;   
    }
    
    /**
     * Gets the children from the provided {@linkplain EObject} as a stream-able {@linkplain Collection}
     * 
     * @param element the {@linkplain EObject} to get the children from
     * @return a {@linkplain Collection} of {@linkplain EObject}
     */
    public static Collection<EObject> GetChildren(EObject element)
    {
        return GetChildren(element, EObject.class);
    }
    
    /**
     * Verifies that the provided {@linkplain EObject} parent is the lawful parent of the provided {@linkplain CapellaElement} child
     * 
     * @param parent the {@linkplain EObject} parent
     * @param child the {@linkplain CapellaElement} child
     * @return a value indicating whether the parent is one of the child,
     * if true, it also means that the parent is {@linkplain RequirementPackage}
     */
    public static boolean IsParentOf(EObject parent, CapellaElement child)
    {
        return child.eContainer() instanceof RequirementPackage &&
               parent instanceof RequirementPackage &&
               AreTheseEquals(child.eContainer().eResource().getURI(), parent.eResource().getURI());
    }
}
