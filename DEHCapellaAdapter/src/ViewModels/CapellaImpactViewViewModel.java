/*
 * CapellaImpactViewViewModel.java
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
package ViewModels;

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.Collection;

import javax.swing.tree.TreeModel;

import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;
import org.polarsys.capella.core.data.capellacore.CapellaElement;

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.CapellaSession.ICapellaSessionService;
import Utils.Ref;
import ViewModels.CapellaObjectBrowser.CapellaObjectBrowserTreeRowViewModel;
import ViewModels.CapellaObjectBrowser.CapellaObjectBrowserTreeViewModel;
import ViewModels.CapellaObjectBrowser.CapellaObjectBrowserViewModel;
import ViewModels.CapellaObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.CapellaObjectBrowser.Rows.ElementRowViewModel;
import ViewModels.CapellaObjectBrowser.Rows.RootRowViewModel;
import ViewModels.Interfaces.ICapellaImpactViewViewModel;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.Iteration;
import io.reactivex.Observable;

/**
 * The {@linkplain CapellaImpactViewViewModel} is the main view model for the requirement impact view in the impact view panel
 */
public class CapellaImpactViewViewModel extends CapellaObjectBrowserViewModel implements ICapellaImpactViewViewModel
{
    /**
     * The {@linkplain IDstController}
     */
    private IDstController dstController;

    /**
     * Initializes a new {@linkplain RequirementImpactViewViewModel}
     * 
     * @param dstController the {@linkplain IDstController}
     * @param sessionService the {@linkplain ICapellaSessionService}
     */
    public CapellaImpactViewViewModel(IDstController dstController, ICapellaSessionService sessionService)
    {
        super(sessionService);
        this.dstController = dstController;

        this.InitializesObservables();
        this.UpdateBrowserTrees(this.SessionService.HasAnyOpenSession());
    }

    /**
     * Initializes the needed subscription on {@linkplain Observable}
     */
    private void InitializesObservables()
    {
        this.dstController.HasAnyOpenSessionObservable()
            .subscribe(this::UpdateBrowserTrees, this.Logger::catching);
                        
        this.dstController.GetHubMapResult()
            .ItemsAdded()
            .subscribe(x -> this.ComputeDifferences(), this.Logger::catching);
        
        this.dstController.GetHubMapResult()
            .IsEmpty()
            .subscribe(isEmpty ->
            {
                if(isEmpty)
                {
                    this.UpdateBrowserTrees(this.SessionService.HasAnyOpenSession());
                }
            });

        this.dstController.GetSelectedHubMapResultForTransfer()
            .ItemsAdded()
            .subscribe(x ->
            {
                for(var thing : x)
                {
                    this.SwitchIsSelected(thing, true);
                }
                
                this.shouldRefreshTree.Value(true);
            });

        this.dstController.GetSelectedHubMapResultForTransfer()
            .ItemRemoved()
            .subscribe(x -> 
            {
                this.SwitchIsSelected(x, false);
                this.shouldRefreshTree.Value(true);
            });
    }

    /**
     * Sets is selected property on the row view model that represents the provided {@linkplain CapellaElement}
     * 
     * @param element The {@linkplain CapellaElement} to find the corresponding row view model
     * @param shouldSelect A value indicating whether the row view model should set as selected
     */
    private void SwitchIsSelected(CapellaElement element, boolean shouldSelect)
    {
        var refRowViewModel = new Ref<ElementRowViewModel<? extends CapellaElement>>(null);
        
        if(this.TryGetRowViewModel(((RootRowViewModel) this.BrowserTreeModel.Value().getRoot()).GetContainedRows(), element.getId(), refRowViewModel))
        {        
            if(!refRowViewModel.Get().GetIsSelected() && shouldSelect)
            {
                refRowViewModel.Get().SetIsSelected(true);
            }
            else if(refRowViewModel.Get().GetIsSelected() && !shouldSelect)
            {
                refRowViewModel.Get().SetIsSelected(false);            
            }
        }
    }

    /**
     * Computes the difference for the provided {@linkplain Thing}
     */
    protected RootRowViewModel ComputeDifferences()
    {
        var rootRowViewModel = (RootRowViewModel) this.BrowserTreeModel.Value().getRoot();
        try
        {
            for (var mappedElementRowViewModel : this.dstController.GetHubMapResult())
            {
                var refRowViewModel = new Ref<ElementRowViewModel<? extends CapellaElement>>(null);
                
                if(this.TryGetRowViewModel(rootRowViewModel.GetContainedRows(), mappedElementRowViewModel.GetDstElement().getId(), refRowViewModel))
                {
                    refRowViewModel.Get().UpdateElement(mappedElementRowViewModel.GetDstElement());
                }
                
            }
        }
        catch(Exception exception)
        {
            this.Logger.catching(exception);
        }
            
        return rootRowViewModel;
    }
        
    /**
     * Gets the {@linkplain Thing} by its Iid from the capella sessions
     * 
     * @param childrenCollection the {@linkplain Collection} collection from the parent row view model
     * @param id the id of the searched {@linkplain TElementRowViewModel}
     * @param refElement the {@linkplain Ref} of {@linkplain TElementRowViewModel} as ref parameter
     */
    @SuppressWarnings("unchecked")
    private boolean TryGetRowViewModel(Collection<IElementRowViewModel<? extends CapellaElement>> childrenCollection, 
            String id, Ref<ElementRowViewModel<? extends CapellaElement>> refElement)
    {
        if(childrenCollection == null || childrenCollection.isEmpty())
        {
            return false;
        }

        for (var childRowViewModel : childrenCollection)
        {
            if (AreTheseEquals(childRowViewModel.GetElement().getId(), id))
            {
                refElement.Set((ElementRowViewModel<? extends CapellaElement>)childRowViewModel);
                break;
            }
            
            if(childRowViewModel instanceof IHaveContainedRows)
            {
                if(this.TryGetRowViewModel(((IHaveContainedRows<IElementRowViewModel<? extends CapellaElement>>)childRowViewModel).GetContainedRows(), id, refElement))
                {
                    break;
                }
            }
        }
        
        return refElement.HasValue();
    }
    
    /**
     * Updates this view model {@linkplain TreeModel}
     * 
     * @param isConnected a value indicating whether the session is open
     */
    @Override
    protected void UpdateBrowserTrees(Boolean isConnected)
    {
        if(isConnected)
        {
            var treeModel = this.dstController.GetHubMapResult().isEmpty() 
                    ? new CapellaObjectBrowserTreeViewModel(this.SessionService.GetModels())
                    : new CapellaObjectBrowserTreeViewModel(this.ComputeDifferences());
                        
            this.SetOutlineModel(DefaultOutlineModel.createOutlineModel(treeModel, new CapellaObjectBrowserTreeRowViewModel(), true));
        }
    
        this.IsTheTreeVisible.Value(isConnected);
    }
    
    /**
     * Updates the {@linkplain browserTreeModel} based on the provided {@linkplain Iteration}
     * 
     * @param iteration the {@linkplain Iteration}
     */
    protected void SetOutlineModel(OutlineModel model)
    {
        this.UpdateHighlightOnRows(model);
        this.BrowserTreeModel.Value(model);
    }

    /**
     * Updates the <code>IsHighlighted</code> property on each row of the specified model
     * 
     * @param model the {@linkplain OutlineModel}
     */
    @SuppressWarnings("unchecked")
    private void UpdateHighlightOnRows(OutlineModel model)
    {
        Object root = model.getRoot();
        
        if(root instanceof IHaveContainedRows)
        {
            for (IElementRowViewModel<?> rowViewModel : ((IHaveContainedRows<IElementRowViewModel<?>>)root).GetContainedRows())
            {
                    boolean isHighlighted = this.dstController.GetHubMapResult().stream()
                            .anyMatch(r -> AreTheseEquals(r.GetDstElement().getId(), rowViewModel.GetElement().getId()));
                    
                    rowViewModel.SetIsHighlighted(isHighlighted);
            }
        }
    }

    /**
     * Compute eligible rows where the represented {@linkplain Thing} can be transfered,
     * and return the filtered collection for feedback application on the tree
     * 
     * @param selectedRow the selected view model {@linkplain ElementRowViewModel}
     */
    @Override
    public void OnSelectionChanged(ElementRowViewModel<?> selectedRow) 
    {
        if(selectedRow != null && selectedRow.GetElement() != null && this.dstController.GetHubMapResult().stream()
                .anyMatch(r -> AreTheseEquals(r.GetDstElement().getId(), selectedRow.GetElement().getId())))
        {
            this.AddOrRemoveSelectedRowToTransfer(selectedRow);
        }
    }

    /**
     * Adds or remove the {@linkplain Thing} to/from the relevant collection depending on the {@linkplain MappingDirection}
     * 
     * @param rowViewModel the {@linkplain ElementRowViewModel} that contains the element to add or remove
     */
    private void AddOrRemoveSelectedRowToTransfer(ElementRowViewModel<?> rowViewModel)
    {
        if(rowViewModel.SwitchIsSelectedValue())
        {
            this.dstController.GetSelectedHubMapResultForTransfer().add(rowViewModel.GetElement());
        }
        else
        {
            this.dstController.GetSelectedHubMapResultForTransfer().Remove(rowViewModel.GetElement());
        }
    }
}
