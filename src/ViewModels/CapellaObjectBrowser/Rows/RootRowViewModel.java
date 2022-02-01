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
import org.eclipse.emf.common.util.TreeIterator;
import org.polarsys.capella.core.data.cs.ComponentPkg;

/**
 * The {@linkplain RootRowViewModel} represents the root element in one containment tree
 */
public class RootRowViewModel extends PackageRowViewModel
{

    /**
     * The {@linkplain Collection} of {@linkplain Element} as {@linkplain TreeIterator} of {@linkplain Notifier} that the {@linkplain RootRowViewModel} should contain 
     */
    private TreeIterator<Notifier> containedElements;
    
    /**
     * Initializes a new {@linkplain RootRowViewModel}
     * 
     * @param name the name of this row
     * @param tree the children element that this row contains
     */
    public RootRowViewModel(String name, TreeIterator<Notifier> tree)
    {
        super(null, null);
        this.containedElements = tree;
        this.UpdateProperties(name);
    }

    /**
     * Updates this view model properties
     * 
     * @param name the name of this row
     * @param elements the children element that this row contains
     */
    protected void UpdateProperties(String name)
    {
        super.UpdateProperties(name);
        this.ComputeContainedRows();
    }

    /**
     * Computes the contained rows of this row view model
     */
    @Override
    public void ComputeContainedRows() 
    {
        if(this.containedElements == null)
        {
            return;
        }
        
        Notifier element;
        
        while((element = this.containedElements.next()) !=null)
        {  
            if(element instanceof ComponentPkg)
            {
                this.ComputeContainedRow((ComponentPkg)element);
            }
        }
    }
}
