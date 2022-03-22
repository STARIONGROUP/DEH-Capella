/*
 * MapCommandService.java
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
package Services.Mapping;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.ui.ISelectionService;
import org.polarsys.capella.core.data.capellacore.CapellaElement;

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.CapellaLog.ICapellaLogService;
import Services.CapellaSelection.ICapellaSelectionService;
import Services.CapellaSession.ICapellaSessionService;
import Services.NavigationService.INavigationService;
import Utils.Ref;
import Utils.Stereotypes.CapellaComponentCollection;
import Utils.Stereotypes.CapellaRequirementCollection;
import Utils.Tasks.Task;
import ViewModels.Dialogs.Interfaces.IDstMappingConfigurationDialogViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedDstRequirementRowViewModel;
import Views.Dialogs.DstMappingConfigurationDialog;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import io.reactivex.Observable;

/**
 * The {@linkplain MapCommandService} provides a simplest way to prepare and map elements from DST and HUB, also provides an abstraction level 
 * on {@linkplain AbstractHandler}, so the action behind {@linkplain MapToHubCommand} can be properly tested since it has many dependencies
 */
public class MapCommandService implements IMapCommandService
{
    /**
     * The current class logger
     */
    private final Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain IDstMappingConfigurationDialogViewModel} instance
     */
    private final IDstMappingConfigurationDialogViewModel dstMappingDialogViewModel;

    /**
     * The {@linkplain IDstController} instance
     */
    private final IDstController dstController;

    /**
     * The {@linkplain INavigationService} instance
     */
    private final INavigationService navigationService;
    
    /**
     * The {@linkplain ICapellaSelectionService} instance
     */
    private final ICapellaSelectionService selectionService;
    
    /**
     * The {@linkplain ICapellaLogService} instance
     */
    private final ICapellaLogService logService;

    /**
     * The {@linkplain IHubController} instance
     */
    private final IHubController hubController;

    /**
     * The {@linkplain ICapellaSessionService} instance
     */
    private final ICapellaSessionService sessionService;

    /**
     * Backing field for {@linkplain CanExecuteObservable}
     */
    private Observable<Boolean> canExecute;
    
    /**
     * Initializes a new {@linkplain MapCommandService}
     * 
     * @param selectionService the {@linkplain ICapellaSelectionService} instance
     * @param dstController the {@linkplain IDstController} instance
     * @param navigationService the {@linkplain INavigationService} instance
     * @param dstMappingDialog the {@linkplain IDstMappingConfigurationDialogViewModel} instance
     * @param logService the {@linkplain ICapellaLogService} instance
     * @param hubController the {@linkplain IHubController} instance
     * @param sessionService the {@linkplain ICapellaSessionService} instance
     */
    public MapCommandService(ICapellaSelectionService selectionService, IDstController dstController,
            INavigationService navigationService, IDstMappingConfigurationDialogViewModel dstMappingDialog,
            ICapellaLogService logService, IHubController hubController, ICapellaSessionService sessionService)
    {
        this.selectionService = selectionService;
        this.dstController = dstController;
        this.navigationService = navigationService;
        this.dstMappingDialogViewModel = dstMappingDialog;
        this.logService = logService;
        this.hubController = hubController;
        this.sessionService = sessionService;
    }
    
    /**
     * Initializes this {@linkplain MapCommandService} {@linkplain CanExecuteObservable} 
     * for later use by the context menu map command
     */
    @Override
    public void Initialize()
    {
        this.canExecute = Observable.combineLatest(this.dstController.HasAnyOpenSessionObservable().startWith(this.sessionService.HasAnyOpenSession()), 
                    this.hubController.GetIsSessionOpenObservable().startWith(this.hubController.GetIsSessionOpen()),
                (hasAnyOpenSession, isHubSessionOpen) -> hasAnyOpenSession && isHubSessionOpen);
    }
    
    /**
     * Gets an {@linkplain Observable} of {@linkplain Boolean} indicating whether the map action can be executed
     * 
     * @return an {@linkplain Observable} of {@linkplain Boolean}
     */
    @Override
    public Observable<Boolean> CanExecuteObservable()
    {
        return this.canExecute;
    }

    /**
     * Gets a value indicating whether the map action can be executed
     * 
     * @return a {@linkplain Boolean} value
     */
    @Override
    public boolean CanExecute()
    {
        return this.sessionService.HasAnyOpenSession() && this.hubController.GetIsSessionOpen();
    }
    
    /**
     * Maps the selection from the {@linkplain ISelectionService} to the specified {@linkplain MappingDirection}
     * 
     * @param mappingDirection the {@linkplain MappingDirection}
     */
    @Override
    public void MapSelection(MappingDirection mappingDirection)
    {
        switch (mappingDirection)
        {
            case FromHubToDst:
                break;
        
            case FromDstToHub:
                this.MapFromDstToHub();
                break;

            default:
                break;
        }
    }

    /**
     * Maps the selected element from the {@linkplain ICapellaSelectionService} through the {@linkplain DstMappingConfigurationDialog}
     * ShowDialog needs to be called asynchronously because of swing/AWT incompatibility, otherwise awaiting for the dialog result blocks the AWT UI Thread
     */
    private void MapFromDstToHub()
    {
        try 
        {  
            this.dstMappingDialogViewModel.SetMappedElement(this.selectionService.GetSelection());
            
            var dialogResult = new Ref<Boolean>(Boolean.class, false);
            
            Task.Run(() -> dialogResult.Set(this.navigationService.ShowDialog(new DstMappingConfigurationDialog(), this.dstMappingDialogViewModel)))
                .Observable()
                .subscribe(x -> WhenDialogHasBeenClosed(dialogResult), x -> this.logger.catching(x));
        }
        catch (Exception exception) 
        {
            this.logger.catching(exception);
        }
    }

    /**
     * Occurs when the {@linkplain DstMappingConfigurationDialog} is closed by the user. 
     * It asynchronously calls {@linkplain MapSelectedElements} with the valid mapped element resulting from the {@linkplain DstMappingConfigurationDialog}
     * 
     * @param dialogResult the {@linkplain Ref} carrying the dialog result {@linkplain Boolean}
     */
    void WhenDialogHasBeenClosed(Ref<Boolean> dialogResult)
    {
        if(!Boolean.TRUE.equals(dialogResult.Get()))
        {
            return;
        }
        
        var validMappedElements = this.dstMappingDialogViewModel.GetMappedElementCollection()
                .stream()
                .filter(m -> m.GetIsValid())
                .collect(Collectors.toList());
                    
        StopWatch timer = StopWatch.createStarted();
        
        Task.Run(() -> this.MapSelectedElements(validMappedElements), boolean.class)
            .Observable()
            .subscribe(t -> 
            {
                if(timer.isStarted())
                {
                    timer.stop();
                }
   
                this.logService.Append(String.format("Mapping action is done in %s ms", timer.getTime(TimeUnit.MILLISECONDS)), t.GetResult().booleanValue());
                
            }, t -> this.logger.catching(t));
    }
    
    /**
     * Maps the selected elements from the current tree
     * 
     * @param mappableElements The collection of {@linkplain MappedElementRowViewModel}  
     * @return a value indicating whether the mapping operation succeeded
     */
    private boolean MapSelectedElements(List<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> mappableElements)
    {
        boolean result = true;
                
        var mappedElements = new CapellaComponentCollection();
        
        mappedElements.addAll(mappableElements.stream()
                .filter(x -> x.GetTThingClass().isAssignableFrom(ElementDefinition.class))
                .map(x -> (MappedElementDefinitionRowViewModel)x)
                .collect(Collectors.toList()));

        if(!mappedElements.isEmpty())
        {
            this.logService.Append("Mapping of %s Components in progress...", mappedElements.size());
            result &= this.dstController.Map(mappedElements, MappingDirection.FromDstToHub);
        }
        
        var mappedRequirements = new CapellaRequirementCollection();
        
        mappedRequirements.addAll(mappableElements.stream()
                .filter(x -> x.GetTThingClass().isAssignableFrom(RequirementsSpecification.class))
                .map(x -> (MappedDstRequirementRowViewModel)x)
                .collect(Collectors.toList()));

        if(!mappedRequirements.isEmpty())
        {
            this.logService.Append("Mapping of %s Requirements in progress...", mappedRequirements.size());
            result &= this.dstController.Map(mappedRequirements, MappingDirection.FromDstToHub);
        }
        
        return result;
    }
}
