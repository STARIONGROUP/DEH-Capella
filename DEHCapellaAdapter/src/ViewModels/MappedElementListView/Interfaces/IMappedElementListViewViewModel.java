/*
 * IMappedElementListViewViewModel.java
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
package ViewModels.MappedElementListView.Interfaces;

import java.util.Collection;

import org.netbeans.swing.outline.OutlineModel;
import org.polarsys.capella.core.data.capellacore.CapellaElement;

import ViewModels.CapellaObjectBrowser.Rows.ElementRowViewModel;
import ViewModels.Interfaces.IObjectBrowserBaseViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.NamedThing;
import cdp4common.commondata.Thing;
import io.reactivex.Observable;

/**
 * The {@linkplain IMappedElementListViewViewModel} is the interface definition for the  {@linkplain MappedElementListViewViewModel}
 */
public interface IMappedElementListViewViewModel extends IObjectBrowserBaseViewModel
{
    /**
     * Compute eligible rows where the represented {@linkplain MappedElementRowViewModel} represents a mapping row,
     * and return the filtered collection for feedback application on the tree
     * 
     * @param selectedRow the collection of selected view model {@linkplain MappedElementRowViewModel}
     */
    void OnSelectionChanged(MappedElementRowViewModel<? extends Thing, ? extends CapellaElement> selectedRow);

    /**
     * Creates the {@linkplain OutlineModel} tree from the provided {@linkplain Collection} of {@linkplain MappedElementRowViewModel}
     * 
     * @param elements the {@linkplain Collection} of {@linkplain MappedElementRowViewModel}
     */
    void BuildTree(Collection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> elements);
    
    /**
     * Gets the {@linkplain Observable} of {@linkplain ElementRowViewModel} that yields the selected element
     * 
     * @return an {@linkplain Observable} of {@linkplain MappedElementRowViewModel}
     */
    Observable<? extends MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> GetSelectedElement();
}
