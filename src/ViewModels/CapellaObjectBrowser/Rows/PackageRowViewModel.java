/*
 * PackageRowViewModel.java
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

import java.util.stream.Collectors;

import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.cs.ComponentPkg;

import Reactive.ObservableCollection;
import ViewModels.CapellaObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;
import ViewModels.ObjectBrowser.Interfaces.IRowViewModel;

/**
 * The {@linkplain PackageRowViewModel} is is the row view model that represents a {@linkplain Package}
 */
public class PackageRowViewModel extends ElementRowViewModel<ComponentPkg> implements IHaveContainedRows<IElementRowViewModel<?>>
{
    /**
     * The {@linkplain ObservableCollection} of {@linkplain IElementRowViewModel}
     */
    private ObservableCollection<IElementRowViewModel<?>> containedRows = new ObservableCollection<IElementRowViewModel<?>>();

    /**
     * Gets the contained row the implementing view model has
     * 
     * @return An {@linkplain ObservableCollection} of {@linkplain IElementRowViewModel}
     */
    @Override
    public ObservableCollection<IElementRowViewModel<?>> GetContainedRows()
    {
        return this.containedRows;
    }

    /**
     * Initializes a new {@linkplain PackageRowViewModel}
     * 
     * @param parent the {@linkplain IRowViewModel} parent of this view model
     * {@linkplain package}
     */
    public PackageRowViewModel(IElementRowViewModel<?> parent, ComponentPkg element)
    {
        super(parent, element);
        this.ComputeContainedRows();
    }
    
    /**
     * Computes the contained rows of this row view model
     */
    @Override
    public void ComputeContainedRows() 
    {
        for (CapellaElement element : this.GetElement().eContents().stream()
                .filter(x -> x instanceof CapellaElement)
                .map(x -> (CapellaElement)x)
                .collect(Collectors.toList()))
        {
            this.ComputeContainedRow(element);
        }
    }

    /**
     * Computes the contained rows of this row view model based on the provided {@linkplain Element}
     * 
     * @param element the {@linkplain CapellaElement}
     */
    protected void ComputeContainedRow(CapellaElement element)
    {
        if(element instanceof ComponentPkg)
        {
            this.containedRows.add(new PackageRowViewModel(this, (ComponentPkg)element));
        }
    }
}
