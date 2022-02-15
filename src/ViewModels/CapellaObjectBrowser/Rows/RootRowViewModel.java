/*
 * RootRowViewModel.java
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
package ViewModels.CapellaObjectBrowser.Rows;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notifier;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.Structure;
import org.polarsys.capella.core.data.capellamodeller.Project;

/**
 * The {@linkplain RootRowViewModel} represents the root element in one containment tree
 */
public class RootRowViewModel extends ProjectStructuralElementRowViewModel<Structure, CapellaElement>
{
    /**
     * Initializes a new {@linkplain RootRowViewModel}
     * 
     * @param name the name of this row
     */
    public RootRowViewModel(String name)
    {
        super(null, null, null);
        this.UpdateProperties(name);
    }
    
    /**
     * Initializes a new {@linkplain RootRowViewModel}
     * 
     * @param name the name of this row
     */
    public RootRowViewModel(String name, Collection<Notifier> elements)
    {
        super(null, null, null);
        this.UpdateProperties(name, elements);
    }
    
    /**
     * Updates this view model properties
     * 
     * @param elements the children element that this row contains
     */
    protected void UpdateProperties(String name, Collection<Notifier> elements)
    {
        super.UpdateProperties(name);
        
        for(var resource : elements)
        {
            if(resource instanceof Project)
            {
                this.GetContainedRows().add(new ProjectRowViewModel(this, (Project)resource));
            }
        }
    }

    /**
     * @param element
     */
    @Override
    protected void AddToContainedRows(CapellaElement element)
    {
        if(element instanceof Project)
        {
            this.GetContainedRows().add(new ProjectRowViewModel(this, (Project)element));
        }
    }
}
