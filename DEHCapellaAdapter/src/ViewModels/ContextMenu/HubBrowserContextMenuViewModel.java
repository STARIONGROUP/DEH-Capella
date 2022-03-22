/*
 * HubBrowserContextMenuViewModel.java
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
package ViewModels.ContextMenu;

import java.util.ArrayList;
import java.util.stream.Collectors;

import HubController.IHubController;
import Reactive.ObservableValue;
import Services.CapellaLog.ICapellaLogService;
import Services.CapellaSession.ICapellaSessionService;
import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IHubBrowserContextMenuViewModel;
import ViewModels.Interfaces.IObjectBrowserViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import io.reactivex.Observable;

/**
 * The HubBrowserContextMenuViewModel is the implementation of the {@linkplain IHubBrowserContextMenuViewModel} for the Hub browsers context menu
 */
public class HubBrowserContextMenuViewModel implements IHubBrowserContextMenuViewModel
{
    /**
     * The {@linkplain IElementDefinitionBrowserViewModel}
     */
    private IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel;
    
    /**
     * The {@linkplain IRequirementBrowserViewModel}
     */
    private IRequirementBrowserViewModel requirementBrowserViewModel;
    
    /**
     * The {@linkplain ICapellaSessionService}
     */
    private ICapellaSessionService capellaSessionService;
    
    /**
     * The {@linkplain IHubController}
     */
    private IHubController hubController;

    /**
     * The {@linkplain ICapellaLogService}
     */
    private ICapellaLogService logService;

    /**
     * Maps the top element towards the DST
     */
    @Override
    public void MapTopElement()
    {
        this.Map(this.hubController.GetOpenIteration().getTopElement());
    }
    
    /**
     * Maps the selection towards the DST
     */
    @Override
    public void MapSelection()
    {
        this.Map(null);
    }

    /**
     * Backing field for {@linkplain #CanMapTopElement()}
     */
    private ObservableValue<Boolean> canMapTopElement = new ObservableValue<>();
        
    /**
     * Gets an {@linkplain Observable} of {@linkplain Boolean} indicating whether the {@linkplain #MapTopElement()} can execute
     * 
     * @return an {@linkplain Observable} of {@linkplain Boolean}
     */
    @Override
    public Observable<Boolean> CanMapTopElement()
    {
        return this.canMapTopElement.Observable();
    }

    /**
     * Backing field for {@linkplain #CanMapSelection()}
     */
    private ObservableValue<Boolean> canMapSelection = new ObservableValue<>();

    /**
     * Gets an {@linkplain Observable} of {@linkplain Boolean} indicating whether the {@linkplain #MapSelection()} can execute
     * 
     * @return an {@linkplain Observable} of {@linkplain Boolean}
     */
    @Override
    public Observable<Boolean> CanMapSelection()
    {
        return this.canMapSelection.Observable();
    }

    /**
     * Backing field for {@linkplain #SetBrowserType()}
     */
    private Class<? extends IObjectBrowserViewModel> browserType;

    /**
     * Sets the browser type with the specified {@linkplain Class} of {@linkplain IObjectBrowserViewModel}
     * 
     * @param browserType the {@linkplain Class} {@linkplain IObjectBrowserViewModel} that identifies the type of browser
     */
    @Override
    public void SetBrowserType(Class<? extends IObjectBrowserViewModel> implementingBrowser)
    {
        this.browserType = implementingBrowser;
    }

    /**
     * Initializes a new {@linkplain HubBrowserContextMenuViewModel}
     * 
     * @param elementDefinitionBrowserViewModel the {@linkplain IElementDefinitionBrowserViewModel}
     * @param requirementBrowserViewModel the {@linkplain IRequirementBrowserViewModel}
     * @param capellaSessionService the {@linkplain ICapellaSessionService}
     * @param hubController the {@linkplain IHubController}
     * @param logService the {@linkplain ICapellaLogService}
     */
    public HubBrowserContextMenuViewModel(IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel,
            IRequirementBrowserViewModel requirementBrowserViewModel, ICapellaSessionService capellaSessionService,
            IHubController hubController, ICapellaLogService logService)
    {
        this.elementDefinitionBrowserViewModel = elementDefinitionBrowserViewModel;
        this.requirementBrowserViewModel = requirementBrowserViewModel;
        this.capellaSessionService = capellaSessionService;
        this.hubController = hubController;
        this.logService = logService;
        
        this.InitializeObservables();
    }
    
    /**
     * Initializes the {@linkplain Observable} used by this view model 
     */
    private void InitializeObservables()
    {
        this.canMapSelection.Value(this.capellaSessionService.HasAnyOpenSession() && this.hubController.GetIsSessionOpen());
        this.UpdateCanMapTopElement();
        
        Observable.combineLatest(this.capellaSessionService.HasAnyOpenSessionObservable().startWith(this.capellaSessionService.HasAnyOpenSession()), 
                this.hubController.GetIsSessionOpenObservable().startWith(this.hubController.GetIsSessionOpen()),
            (hasAnyOpenSession, isHubSessionOpen) -> hasAnyOpenSession && isHubSessionOpen)
        .subscribe(x -> 
            this.canMapSelection.Value(x));
        
        this.canMapSelection.Observable()
            .subscribe(x -> UpdateCanMapTopElement());

        this.hubController.GetSessionEventObservable().subscribe(x -> this.UpdateCanMapTopElement());
    }

    /**
     * Updates the {@linkplain #CanMapTopElement()} value
     */
    private void UpdateCanMapTopElement()
    {
        this.canMapTopElement.Value(this.canMapSelection.Value() 
                && this.browserType == IElementDefinitionBrowserViewModel.class 
                && this.hubController.GetOpenIteration().getTopElement() != null);
    }
    
    /**
     * Maps either the specified top element {@linkplain ElementDefinition} or the currently selected elements
     * 
     * @param topElement the {@linkplain ElementDefinition} optional
     */
    private void Map(ElementDefinition topElement)
    {        
        var elements = new ArrayList<Thing>();
        
        if(topElement != null)
        {
            elements.add(topElement);
        }
        
        if(elements.isEmpty())
        {
            elements.addAll(this.elementDefinitionBrowserViewModel.GetSelectedElements().stream()
                    .map(x -> x.GetThing()).collect(Collectors.toList()));
            
            elements.addAll(this.requirementBrowserViewModel.GetSelectedElements().stream()
                    .map(x -> x.GetThing()).collect(Collectors.toList()));
        }
        
        this.logService.Append("Mapping in progress of %s elements", elements.size());
    }
}
