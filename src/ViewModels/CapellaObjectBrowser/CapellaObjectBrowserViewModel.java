/*
 * MagicDrawObjectBrowserViewModel.java
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
package ViewModels.CapellaObjectBrowser;

import java.util.Collection;

import javax.swing.tree.TreeModel;

import org.eclipse.emf.ecore.EObject;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;

import Reactive.ObservableValue;
import Services.CapellaSession.ICapellaSessionService;
import ViewModels.ObjectBrowserBaseViewModel;
import ViewModels.CapellaObjectBrowser.Interfaces.ICapellaObjectBrowserViewModel;
import ViewModels.CapellaObjectBrowser.Rows.ElementRowViewModel;
import io.reactivex.Observable;

/**
 * The {@linkplain CapellaObjectBrowserViewModel} is the view model for the Capella object browser {@linkplain CapellaObjectBrowser}
 */
public class CapellaObjectBrowserViewModel extends ObjectBrowserBaseViewModel implements ICapellaObjectBrowserViewModel
{
    /**
     * The {@linkplain ICapellaSessionService} instance
     */
    private final ICapellaSessionService sessionService;
    
    /**
     * Backing field for {@linkplain GetSelectedElement}
     */
    private ObservableValue<ElementRowViewModel<?>> selectedElement = new ObservableValue<ElementRowViewModel<?>>();
    
    /**
     * Gets the {@linkplain Observable} of {@linkplain ClassRowViewModel} that yields the selected element
     * 
     * @return an {@linkplain Observable} of {@linkplain ClassRowViewModel}
     */
    @Override
    public Observable<ElementRowViewModel<?>> GetSelectedElement()
    {
        return this.selectedElement.Observable();
    }
    
    /**
     * Initializes a new {@linkplain CapellaObjectBrowserViewModel}
     * 
     * @param sessionService the {@linkplain ICapellaSessionService}
     */
    public CapellaObjectBrowserViewModel(ICapellaSessionService sessionService)
    {
        this.sessionService = sessionService;
    }
    
    /**
     * Occurs when the selection changes
     * 
     * @param selectedRow the selected {@linkplain ClassRowViewModel}
     */
    @Override
    public void OnSelectionChanged(ElementRowViewModel<?> selectedRow)
    {
        this.selectedElement.Value(selectedRow);            
    }
    
    /**
     * Creates the {@linkplain OutlineModel} tree from the provided {@linkplain Collection} of {@linkplain EObject}
     * 
     * @param elements the {@linkplain Collection} of {@linkplain EObject}
     */
    @Override
    public void BuildTree(Collection<EObject> elements)
    {
        this.BrowserTreeModel.Value(DefaultOutlineModel.createOutlineModel(
                new CapellaObjectBrowserTreeViewModel(this.sessionService, elements),
                new CapellaObjectBrowserTreeRowViewModel(), true));
        
        this.IsTheTreeVisible.Value(true);
    }
            
    /**
     * Creates the {@linkplain OutlineModel} tree from the provided {@linkplain Collection} of {@linkplain EObject},
     * With the specified name
     * 
     * @param name the name of the root element of the tree
     * @param elements the {@linkplain Collection} of {@linkplain EObject}
     */
    @Override
    public void BuildTree(String name, Collection<EObject> elements)
    {
        this.BrowserTreeModel.Value(DefaultOutlineModel.createOutlineModel(
                new CapellaObjectBrowserTreeViewModel(name, elements),
                new CapellaObjectBrowserTreeRowViewModel(), true));
    
        this.IsTheTreeVisible.Value(true);
    }


    /**
     * Updates this view model {@linkplain TreeModel}
     * 
     * @param isConnected a value indicating whether the session is open
     */
    protected void UpdateBrowserTrees(Boolean isConnected)
    {
    }
}
