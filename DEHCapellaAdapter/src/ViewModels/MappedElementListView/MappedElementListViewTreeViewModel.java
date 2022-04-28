/*
 * MappedElementListViewTreeViewModel.java
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
package ViewModels.MappedElementListView;

import java.util.Collection;

import javax.swing.tree.TreeModel;

import org.polarsys.capella.core.data.capellacore.CapellaElement;

import ViewModels.CapellaObjectBrowser.Rows.RootRowViewModel;
import ViewModels.MappedElementListView.Rows.MappedElementListViewRootViewModel;
import ViewModels.ObjectBrowser.BrowserTreeBaseViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.Thing;

/**
 * The {@linkplain MappedElementListViewTreeViewModel} is the {@linkplain TreeModel} for the Capella object browser
 */
public class MappedElementListViewTreeViewModel extends BrowserTreeBaseViewModel
{
    /**
     * The root element of the tree
     */
    private Object root;

    /**
     * Gets the root element of the tree
     * 
     * @return an {@linkplain Object}
     */
    @Override
    public Object getRoot()
    {
        return this.root;
    }
    
    /**
     * Initializes a new {@linkplain MappedElementListViewTreeViewModel}
     * 
     * @param mappedElements the {@linkplain Collection} of {@linkplain MappedElementRowViewModel}
     */
    public MappedElementListViewTreeViewModel(Collection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> mappedElements)
    {
        this.root = new MappedElementListViewRootViewModel(mappedElements);
    }
}
