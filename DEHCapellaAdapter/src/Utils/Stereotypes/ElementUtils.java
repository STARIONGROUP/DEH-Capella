/*
 * ElementUtils.java
 *
 * Copyright (c) 2020-2022 RHEA System S.A.
 *
 * Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski 
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

import org.polarsys.capella.common.data.modellingcore.ModelElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.kitalpha.emde.model.Element;
import org.polarsys.kitalpha.vp.requirements.Requirements.IdentifiableElement;
import org.polarsys.kitalpha.vp.requirements.Requirements.ReqIFElement;
import org.polarsys.kitalpha.vp.requirements.Requirements.Requirement;
import org.polarsys.kitalpha.vp.requirements.Requirements.SharedDirectAttributes;

/**
 * The ElementUtils is a class that provides utility method for {@linkplain Element}
 */
public final class ElementUtils
{
    /**
     * Gets the id of the provided {@linkplain Element}
     * 
     * @param element The {@linkplain Element}
     * @return A string
     */
    public static String GetId(Element element)
    {
        if(element instanceof IdentifiableElement)
        {
            return ((IdentifiableElement)element).getId();
        }
        
        return ((ModelElement)element).getId();
    }
    
    /**
     * Gets the name of the provided {@linkplain Element}
     * 
     * @param element The {@linkplain Element}
     * @return A string
     */
    public static String GetName(Element element)
    {
        if(element instanceof SharedDirectAttributes)
        {
            return ((SharedDirectAttributes)element).getReqIFName();
        }
        else if(element instanceof ReqIFElement)
        {
            return ((ReqIFElement)element).getReqIFLongName();
        }
        else if(element instanceof Requirement)
        {
            return ((Requirement)element).getReqIFName();
        }
        
        return ((NamedElement)element).getName();
    }

    /**
     * Gets the name of the provided {@linkplain Element}
     * 
     * @param element The {@linkplain Element}
     * @param element The {@linkplain Element} name to set
     */
    public static void SetName(Element element, String name)
    {
        if(element instanceof SharedDirectAttributes)
        {
            ((SharedDirectAttributes)element).setReqIFName(name);
        }
        else if(element instanceof ReqIFElement)
        {
            ((ReqIFElement)element).setReqIFLongName(name);
        }
        else
        {
            ((NamedElement)element).setName(name);
        }
    }
}
