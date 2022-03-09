/*
 * StereotypeUtils.java
 *
 * Copyright (c) 2020-2022 RHEA System S.A.
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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.fa.FaPackage;
import org.polarsys.capella.core.data.pa.PaFactory;
import org.polarsys.capella.core.data.pa.PaPackage;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.data.requirement.Requirement;
import org.polarsys.capella.core.data.requirement.RequirementPackage;
import org.polarsys.capella.core.data.requirement.RequirementsPkg;
import org.polarsys.capella.core.data.requirement.SystemFunctionalRequirement;

import Utils.Ref;

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
        return name.replaceAll("[^a-zA-Z0-9-]|\\s", "").toLowerCase();
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
     * @param clazz the {@linkplain Class} of the {@linkplain #TCapellaElement} parameter
     * @return a {@linkplain Collection} of element typed as the one specified by the {@linkplain #TCapellaElement} parameter
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
     * if true, it also means that the parent is {@linkplain RequirementsPkg}
     */
    public static boolean IsParentOf(EObject parent, CapellaElement child)
    {
        try
        {
            return child.eContainer() instanceof RequirementsPkg 
                    && parent instanceof RequirementsPkg 
                    && EcoreUtil.isAncestor(parent, child);
        }
        catch(ClassCastException exception)
        {
            LogManager.getLogger().catching(exception);
            return child.eContainer() == parent; 
        }
    }
  
    /**
     * Verifies the provided {@linkplain Element} element is owned by the provided {@linkplain Element} parent
     * 
     * @param element the {@linkplain EObject} to verify
     * @param parent the {@linkplain EObject} parent
     * @return a value indicating whether the element is owned by the specified parent
     */
    public static boolean IsOwnedBy(CapellaElement element, CapellaElement parent)
    {
        if(parent.eContents().isEmpty())
        {
            return false;
        }
        
        for (var child : parent.eContents().stream()
                .filter(x -> x instanceof CapellaElement)
                .map(x -> (CapellaElement)x)
                .collect(Collectors.toList()))
        {
            if(AreTheseEquals(child.getId(), element.getId()))
            {
                return true;
            }
            
            if(!child.eContents().isEmpty() && IsOwnedBy(element, child))
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Attempts to retrieve the parent of parent of the provided {@linkplain Class} element. 
     * Hence this is not always possible if the user decides to structure its SysML project differently.
     * However, this feature is only a nice to have.
     *  
     * @param requirement the {@linkplain Requirement} element to get the parent from
     * @param possibleParent the {@linkplain Ref} of {@linkplain RequirementsPkg}
     * @return a value indicating whether the name of the parent was retrieved with success
     */
    public static boolean TryGetPossibleRequirementsSpecificationElement(Requirement requirement, Ref<RequirementsPkg> possibleParent)
    {
        RequirementsPkg lastElement = null;
        EObject currentElement = requirement.eContainer();
        
        while(!possibleParent.HasValue() && currentElement != null)
        {  
            if(currentElement instanceof RequirementsPkg)
            {
                lastElement = (RequirementsPkg)currentElement;
            }
            else
            {
                possibleParent.Set(lastElement);
            }

            currentElement = lastElement == null ? requirement.eContainer() : lastElement.eContainer();
        }
        
        return possibleParent.HasValue();
    }
    
    /**
     * Initializes a new {@linkplain CapellaElement} from the specified {@linkplain #Class}
     * 
     * @param <TInstance> the {@linkplain Type} of {@linkplain CapellaElement}
     * @param clazz the {@linkplain Class} of {@linkplain #TInstance}
     * @return an instance of the provided type
     */
    public static <TInstance extends CapellaElement> TInstance InitializeCapellaElement(Class<TInstance> clazz)
    {        
        var eClassAndFactory = StereotypeUtils.GetEClassAndFactory(clazz.getSimpleName());
        
        if (eClassAndFactory.getLeft() != null && eClassAndFactory.getRight() != null && eClassAndFactory.getLeft() instanceof EClass) 
        {
            return clazz.cast(eClassAndFactory.getRight().create((EClass)eClassAndFactory.getLeft()));
        }
        
        return null;
    }

    /**
     * Gets the {@linkplain EClass} that corresponds to the provided {@linkplain String} className 
     * 
     * @param className the {@linkplain Class} name
     * @return the {@linkplain EClassifier} {@linkplain EClass}
     */
    private static Pair<EClassifier, EFactory> GetEClassAndFactory(String className)
    {
        Function<EPackage, EClassifier> fun = (EPackage ePackage) -> ePackage.getEClassifier(className);
        
        var eClass = fun.apply(PaPackage.eINSTANCE);
        var eFactory = PaPackage.eINSTANCE.getEFactoryInstance();
        
        if(eClass == null)
        {
            eClass = fun.apply(FaPackage.eINSTANCE);
            eFactory = FaPackage.eINSTANCE.getEFactoryInstance();
        }
        
        if(eClass == null)
        {
            eClass = fun.apply(RequirementPackage.eINSTANCE);
            eFactory = RequirementPackage.eINSTANCE.getEFactoryInstance();
        }

        return Pair.of(eClass, eFactory);
    }
}
