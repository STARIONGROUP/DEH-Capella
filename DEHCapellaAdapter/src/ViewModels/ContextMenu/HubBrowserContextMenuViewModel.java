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

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.requirement.Requirement;

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Reactive.ObservableValue;
import Services.CapellaLog.ICapellaLogService;
import Services.CapellaSession.ICapellaSessionService;
import Services.CapellaTransaction.ICapellaTransactionService;
import Utils.Ref;
import Utils.Stereotypes.HubElementCollection;
import Utils.Stereotypes.HubRequirementCollection;
import Utils.Tasks.Task;
import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IHubBrowserContextMenuViewModel;
import ViewModels.Interfaces.IObjectBrowserViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedHubRequirementRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import io.reactivex.Observable;

/**
 * The HubBrowserContextMenuViewModel is the implementation of the {@linkplain IHubBrowserContextMenuViewModel} for the Hub browsers context menu
 */
public class HubBrowserContextMenuViewModel implements IHubBrowserContextMenuViewModel
{
    /**
     * The current class logger
     */
    protected final Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain IElementDefinitionBrowserViewModel}
     */
    private final IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel;
    
    /**
     * The {@linkplain IRequirementBrowserViewModel}
     */
    private final IRequirementBrowserViewModel requirementBrowserViewModel;
    
    /**
     * The {@linkplain ICapellaSessionService}
     */
    private final ICapellaSessionService capellaSessionService;
    
    /**
     * The {@linkplain IHubController}
     */
    private final IHubController hubController;

    /**
     * The {@linkplain ICapellaLogService}
     */
    private final ICapellaLogService logService;

    /**
     * The {@linkplain IDstController}
     */
    private final IDstController dstController;
    
    /**
     * The {@linkplain ICapellaTransactionService}
     */
    private final ICapellaTransactionService transactionService;

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
     * @param dstController the {@linkplain IDstController}
     * @param transactionService the {@linkplain ICapellaTransactionService}
     */
    public HubBrowserContextMenuViewModel(IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel,
            IRequirementBrowserViewModel requirementBrowserViewModel, ICapellaSessionService capellaSessionService,
            IHubController hubController, ICapellaLogService logService, IDstController dstController,
            ICapellaTransactionService transactionService)
    {
        this.elementDefinitionBrowserViewModel = elementDefinitionBrowserViewModel;
        this.requirementBrowserViewModel = requirementBrowserViewModel;
        this.capellaSessionService = capellaSessionService;
        this.hubController = hubController;
        this.logService = logService;
        this.dstController = dstController;
        this.transactionService = transactionService;
        
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
        
        StopWatch timer = StopWatch.createStarted();
        
        Task.Run(() -> this.MapSelectedElements(SortMappableThings(elements)), boolean.class)
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
     * @param elementsAndRequirements a {@linkplain Pair} of {@linkplain HubElementCollection} and {@linkplain HubRequirementCollection}
     * @return a value indicating whether the mapping operation succeeded
     */
    private boolean MapSelectedElements(Pair<HubElementCollection, HubRequirementCollection> elementsAndRequirements)
    {
        this.logService.Append("Mapping in progress of %s elements ...", elementsAndRequirements.getLeft().size());
        this.logService.Append("Mapping in progress of %s requirements ...", elementsAndRequirements.getRight().size());
        
        return this.dstController.Map(elementsAndRequirements.getLeft(), MappingDirection.FromHubToDst) 
                & this.dstController.Map(elementsAndRequirements.getRight(), MappingDirection.FromHubToDst);
    }

    /**
     * Sorts the provided collection of {@linkplain Thing}
     * 
     * @param elements the {@linkplain ArrayList} of {@linkplain Thing} to sort
     * @return a {@linkplain Pair} where the left element is a {@linkplain HubElementCollection} and the {@linkplain HubRequirementCollection}
     */
    private Pair<HubElementCollection, HubRequirementCollection> SortMappableThings(ArrayList<Thing> elements)
    {
        if(elements.isEmpty())
        {
            elements.addAll(this.elementDefinitionBrowserViewModel.GetSelectedElements().stream()
                    .map(x -> x.GetThing())
                    .filter(x -> x instanceof ElementDefinition)
                    .collect(Collectors.toList()));
            
            elements.addAll(this.requirementBrowserViewModel.GetSelectedElements().stream()
                    .map(x -> x.GetThing()).collect(Collectors.toList()));
        }
                
        var hubElements = new HubElementCollection();
        var hubRequirements = new HubRequirementCollection();
        
        hubElements.addAll(elements.stream()
                .filter(x -> x instanceof ElementDefinition)
                .map(x -> (ElementDefinition)x)
                .map(x -> 
                {
                    var refElement = new Ref<>(Component.class);
                    this.dstController.TryGetElementByName(x, refElement);
                    
                    return new MappedElementDefinitionRowViewModel(x, this.transactionService.Clone(refElement.Get()), MappingDirection.FromHubToDst);
                })
                .collect(Collectors.toList()));
        
        this.SortRequirements(elements, hubRequirements);
                
        return Pair.of(hubElements, hubRequirements);
    }

    /**
     * Sorts the {@linkplain cdp4common.engineeringmodeldata.Requirement} that can be mapped based on whatever container was selected in the requirement browser
     * 
     * @param elements the base collection of selected elements
     * @param hubRequirements the {@linkplain HuRequirementCollection} to pass on to the mapping rule
     */
    private void SortRequirements(ArrayList<Thing> elements, HubRequirementCollection hubRequirements)
    {
        hubRequirements.addAll(elements.stream()
                .filter(x -> x instanceof cdp4common.engineeringmodeldata.Requirement)
                .map(x -> (cdp4common.engineeringmodeldata.Requirement)x)
                .map(x -> 
                {
                    var refElement = new Ref<>(Requirement.class);
                    this.dstController.TryGetElementByName(x, refElement);
                    
                    return new MappedHubRequirementRowViewModel(x, this.transactionService.Clone(refElement.Get()), MappingDirection.FromHubToDst);
                })
                .collect(Collectors.toList()));
        

        for (var requirementsSpecification : elements.stream()
                .filter(x -> x instanceof RequirementsSpecification)
                .map(x -> (RequirementsSpecification)x)
                .collect(Collectors.toList()))
        {
            SortRequirements(hubRequirements, requirementsSpecification, null);
        }
        
        for (var requirementsGroup : elements.stream()
                .filter(x -> x instanceof RequirementsGroup)
                .map(x -> (RequirementsGroup)x)
                .collect(Collectors.toList()))
        {            
            SortRequirements(hubRequirements, requirementsGroup.getContainerOfType(RequirementsSpecification.class), 
                    x -> AreTheseEquals(x.getGroup().getIid(), requirementsGroup.getIid()));
        }
    }

    /**
     * Sorts the {@linkplain cdp4common.engineeringmodeldata.Requirement} that can be mapped based on whatever container was selected in the requirement browser
     * 
     * @param elements the base collection of selected elements
     * @param hubRequirements the {@linkplain HuRequirementCollection} to pass on to the mapping rule
     * @param requirementsSpecification the {@linkplain RequirementsSpecification} that contains all the requirements
     * @param filterOnGroup a {@linkplain Predicate} to test if only the requirements contained in a certain group should be selected
     */
    private void SortRequirements(HubRequirementCollection hubRequirements, RequirementsSpecification requirementsSpecification,
            Predicate<cdp4common.engineeringmodeldata.Requirement> filterOnGroup)
    {
        for (var requirement : requirementsSpecification.getRequirement()
                .stream()
                .collect(Collectors.toList()))
        {
            if(filterOnGroup == null || filterOnGroup.test(requirement))
            {
                var refElement = new Ref<>(Requirement.class);
                this.dstController.TryGetElementByName(requirement, refElement);
            
                hubRequirements.add(new MappedHubRequirementRowViewModel(requirement, this.transactionService.Clone(refElement.Get()), MappingDirection.FromHubToDst));
            }
        }
    }
}
