/*
 * MappedElementListViewRootViewModel.java
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
package ViewModels.MappedElementListView.Rows;

import java.util.Collection;

import org.polarsys.capella.core.data.capellacore.CapellaElement;

import Reactive.ObservableCollection;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.Thing;

/**
 * The {@linkplain MappedElementListViewRootViewModel} is the root row view model that contains a collection of {@linkplain MappedElementRowViewModel}
 */
public class MappedElementListViewRootViewModel implements IHaveContainedRows<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>>
{
    /**
     * The {@linkplain ObservableCollection} of {@linkplain IElementRowViewModel}
     */
    private ObservableCollection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> containedRows = new ObservableCollection<>();
        
    /**
     * Gets the contained row the implementing view model has
     * 
     * @return An {@linkplain ObservableCollection} of {@linkplain IElementRowViewModel}
     */
    @Override
    public ObservableCollection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> GetContainedRows()
    {
        return this.containedRows;
    }
    
    /**
     * Initializes a new {@linkplain MappedElementListViewRootViewModel}
     * 
     * @param initialCollection the initial {@linkplain Collection} of {@linkplain MappedElementRowViewModel}
     */
    public MappedElementListViewRootViewModel(Collection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> initialCollection)
    {
        this.containedRows.addAll(initialCollection);
    }

    /**
     * Computes the contained rows of this view model
     */
    @Override
    public void ComputeContainedRows() { }
}
