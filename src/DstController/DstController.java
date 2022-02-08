/*
 * DstController.java
 *
 * Copyright (c) 2020-2021 RHEA System S.A.
 *
 * Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski 
 *
 * This file is part of DEH-MDSYSML
 *
 * The DEH-MDSYSML is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * The DEH-MDSYSML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package DstController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.polarsys.capella.core.data.capellacore.CapellaElement;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Reactive.ObservableCollection;
import Reactive.ObservableValue;
import Services.CapellaLog.ICapellaLogService;
import Services.CapellaSession.ICapellaSessionService;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Services.MappingConfiguration.IMappingConfigurationService;
import Services.MappingEngineService.IMappableThingCollection;
import Services.MappingEngineService.IMappingEngineService;
import Utils.Ref;
import ViewModels.Interfaces.IMappedElementRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.ClassKind;
import cdp4common.commondata.Definition;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.BinaryRelationship;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ElementUsage;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.Parameter;
import cdp4common.engineeringmodeldata.ParameterValueSet;
import cdp4common.engineeringmodeldata.ParameterValueSetBase;
import cdp4common.engineeringmodeldata.Relationship;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.engineeringmodeldata.ValueSet;
import cdp4common.types.ContainerList;
import cdp4dal.exceptions.TransactionException;
import cdp4dal.operations.ThingTransaction;
import io.reactivex.Observable;

/**
 * The {@linkplain DstController} is a class that manage transfer and connection to attached running instance of Capella
 */
public final class DstController implements IDstController
{
    /**
     * Gets this running DST adapter name
     */
    public static final String THISTOOLNAME = "DEH-CAPELLA";

    /**
     * The current class Logger
     */
    private final Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain IMappingEngine} instance
     */
    private final IMappingEngineService mappingEngine;

    /**
     * The {@linkplain IHubController} instance
     */
    private final IHubController hubController;

    /**
     * The {@linkplain ICapellaLogService} instance
     */
    private final ICapellaLogService logService;

    /**
     * The {@linkplain IMappingConfigurationService} instance
     */
    private final ICapellaMappingConfigurationService mappingConfigurationService;
    
    /**
     * the {@linkplain ICapellaSessionService} instance 
     */
    private final ICapellaSessionService capellaSessionService;
    
    /**
     * Backing field for {@linkplain GetDstMapResult}
     */
    private ObservableCollection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> hubMapResult = new ObservableCollection<>();
    
    /**
     * Gets The {@linkplain ObservableCollection} of Hub map result
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain Class}
     */
    @Override
    public ObservableCollection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> GetHubMapResult()
    {
        return this.hubMapResult;
    }    
    
    /**
     * Backing field for {@linkplain GetDstMapResult}
     */
    private ObservableCollection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> dstMapResult = new ObservableCollection<>();

    /**
     * Gets The {@linkplain ObservableCollection} of DST map result
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain MappedElementRowViewModel}
     */
    @Override
    public ObservableCollection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> GetDstMapResult()
    {
        return this.dstMapResult;
    }
    
    /**
     * Backing field for {@linkplain GetSelectedHubMapResultForTransfer}
     */    
    private ObservableCollection<CapellaElement> selectedHubMapResultForTransfer = new ObservableCollection<>(CapellaElement.class);
    
    /**
     * Gets the {@linkplain ObservableCollection} of that are selected for transfer to the Capella
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain CapellaElement}
     */
    @Override
    public ObservableCollection<CapellaElement> GetSelectedHubMapResultForTransfer()
    {
        return this.selectedHubMapResultForTransfer;
    }
    
    /**
     * Backing field for {@linkplain GetSelectedDstMapResultForTransfer}
     */
    private ObservableCollection<Thing> selectedDstMapResultForTransfer = new ObservableCollection<>(Thing.class);
    
    /**
     * Gets the {@linkplain ObservableCollection} of {@linkplain Thing} that are selected for transfer to the Hub
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain Thing}
     */
    @Override
    public ObservableCollection<Thing> GetSelectedDstMapResultForTransfer()
    {
        return this.selectedDstMapResultForTransfer;
    }
    
    /**
     * Backing field for {@linkplain GeMappingDirection}
     */
    private ObservableValue<MappingDirection> currentMappingDirection = new ObservableValue<>(MappingDirection.FromDstToHub, MappingDirection.class);

    /**
     * Gets the {@linkplain Observable} of {@linkplain MappingDirection} from {@linkplain currentMappingDirection}
     * 
     * @return a {@linkplain Observable} of {@linkplain MappingDirection}
     */
    @Override
    public Observable<MappingDirection> GetMappingDirection()
    {
        return this.currentMappingDirection.Observable();
    }

    /**
     * Gets the current {@linkplain MappingDirection} from {@linkplain currentMappingDirection}
     * 
     * @return the {@linkplain MappingDirection}
     */
    @Override
    public MappingDirection CurrentMappingDirection()
    {
        return this.currentMappingDirection.Value();
    }

    /**
     * Switches the {@linkplain MappingDirection}
     * 
     * @return the new {@linkplain MappingDirection}
     */
    @Override
    public MappingDirection ChangeMappingDirection()
    {
        this.currentMappingDirection.Value(
                this.currentMappingDirection.Value() == MappingDirection.FromDstToHub 
                ? MappingDirection.FromHubToDst
                : MappingDirection.FromDstToHub);
        
        return this.currentMappingDirection.Value();
    }
    
    /**
     * Gets an {@linkplain Observable} of value indicating whether there is any session open in Capella
     * 
     * @return {@linkplain Observable} of {@linkplain Boolean} 
     */
    @Override
    public Observable<Boolean> HasAnyOpenSession()
    {
        return this.capellaSessionService.HasAnyOpenSession();
    }
    
    /**
     * Initializes a new {@linkplain DstController}
     * 
     * @param mappingEngine the {@linkplain IMappingEngine} instance
     * @param HubController the {@linkplain IHubController} instance
     * @param logService the {@linkplain ICapellaLogService} instance
     * @param mappingConfigurationService the {@linkplain ICapellaMappingConfigurationService} instance
     * @param mappingConfigurationService the {@linkplain ICapellaSessionService} instance
     */
    public DstController(IMappingEngineService mappingEngine, IHubController hubController, ICapellaLogService logService, 
            ICapellaMappingConfigurationService mappingConfigurationService, ICapellaSessionService capellaSessionService)
    {
        this.mappingEngine = mappingEngine;
        this.hubController = hubController;
        this.logService = logService;
        this.mappingConfigurationService = mappingConfigurationService;
        this.capellaSessionService = capellaSessionService;
    }
        
    /**
     * Loads the saved mapping and applies the mapping rule to the loaded things
     * 
     * @return the number of mapped things loaded
     */
    @Override
    public void LoadMapping()
    {
        StopWatch timer = StopWatch.createStarted();
        
        Collection<IMappedElementRowViewModel> things = this.mappingConfigurationService.LoadMapping(null);
            
        boolean result = true;
        
        this.dstMapResult.clear();
        this.hubMapResult.clear();
                
        timer.stop();
        
        if(!result)
        {
            this.logService.Append(String.format("Could not load %s saved mapped things for some reason, check the log for details", things.size()), Level.ERROR);
            things.clear();
            return;
        }
        
        this.logService.Append(String.format("Loaded %s saved mapping, done in %s ms", things.size(), timer.getTime(TimeUnit.MILLISECONDS)));
    }

    /**
     * Maps the {@linkplain input} by calling the {@linkplain IMappingEngine}
     * and assign the map result to the dstMapResult or the hubMapResult
     * 
     * @param input the {@linkplain IMappableThingCollection} in other words the  {@linkplain Collection} of {@linkplain Object} to map
     * @param mappingDirection the {@linkplain MappingDirection} towards the {@linkplain IMappableThingCollection} maps to
     * @return a {@linkplain boolean} indicating whether the mapping operation went well
     */
    @Override
    public boolean Map(IMappableThingCollection input, MappingDirection mappingDirection)
    {
        return true;
    }
    
    /**
     * Transfers the selected things to be transfered depending on the current {@linkplain MappingDirection}
     * 
     * @return a value indicating that all transfer could be completed
     */
    @Override
    public boolean Transfer()
    {
        boolean result;
        
        switch(this.CurrentMappingDirection())
        {
            case FromDstToHub:
                result = this.TransferToHub();
                break;
            case FromHubToDst:
                result = true;
                break;
            default:
                result = false;
                break;        
        }
        
        this.LoadMapping();
        return result;
    }
    
    /**
     * Transfers all the {@linkplain Thing} contained in the {@linkplain dstMapResult} to the Hub
     * 
     * @return a value indicating that all transfer could be completed
     */
    @Override
    public boolean TransferToHub()
    {
        try
        {
            Pair<Iteration, ThingTransaction> iterationTransaction = this.hubController.GetIterationTransaction();
            Iteration iterationClone = iterationTransaction.getLeft();
            ThingTransaction transaction = iterationTransaction.getRight();
            
            this.PrepareThingsForTransfer(iterationClone, transaction);

            this.mappingConfigurationService.PersistExternalIdentifierMap(transaction, iterationClone);
            transaction.createOrUpdate(iterationClone);
            
            this.hubController.Write(transaction);
            this.mappingConfigurationService.RefreshExternalIdentifierMap();
            boolean result = this.hubController.Refresh();
            this.UpdateParameterValueSets();
            return result && this.hubController.Refresh();
        }
        catch (Exception exception)
        {
            this.logService.Append(exception.toString(), exception);
            return false;
        }
        finally
        {
            this.selectedDstMapResultForTransfer.clear();
        }
    }
    
    /**
     * Updates the {@linkplain ValueSet} with the new values
     * 
     * @return a value indicating whether the operation went OK
     * @return a {@linkplain Pair} of a value indicating whether the transaction has been committed with success
     * and a string of the exception if any
     * @throws TransactionException
     */
    public void UpdateParameterValueSets() throws TransactionException
    {
        Pair<Iteration, ThingTransaction> iterationTransaction = this.hubController.GetIterationTransaction();
        Iteration iterationClone = iterationTransaction.getLeft();
        ThingTransaction transaction = iterationTransaction.getRight();

        List<Parameter> allParameters = this.dstMapResult.stream()
                .filter(x -> x.GetHubElement() instanceof ElementDefinition)
                .flatMap(x -> ((ElementDefinition)x.GetHubElement()).getParameter().stream())
                .collect(Collectors.toList());
        
        for(Parameter parameter : allParameters)
        {
            Ref<Parameter> refNewParameter = new Ref<>(Parameter.class);
            
            if(this.hubController.TryGetThingById(parameter.getIid(), refNewParameter))
            {
                Parameter newParameterCloned = refNewParameter.Get().clone(false);
    
                for (int index = 0; index < parameter.getValueSet().size(); index++)
                {
                    ParameterValueSet clone = newParameterCloned.getValueSet().get(index).clone(false);
                    this.UpdateValueSet(clone, parameter.getValueSet().get(index));
                    transaction.createOrUpdate(clone);
                }
    
                transaction.createOrUpdate(newParameterCloned);
            }
        }
        
        transaction.createOrUpdate(iterationClone);
        this.hubController.Write(transaction);
    }    

    /**
     * Updates the specified {@linkplain ParameterValueSetBase}
     * 
     * @param clone the {@linkplain ParameterValueSetBase} to update
     * @param valueSet the {@linkplain ValueSet} that contains the new values
     */
    private void UpdateValueSet(ParameterValueSetBase clone, ValueSet valueSet)
    {
        clone.setManual(valueSet.getManual());
        clone.setValueSwitch(valueSet.getValueSwitch());
    }

    /**
     * Prepares all the {@linkplain Thing}s that are to be updated or created
     * 
     * @param iterationClone the {@linkplain Iteration} clone
     * @param transaction the {@linkplain ThingTransaction}
     * @throws TransactionException
     */
    private void PrepareThingsForTransfer(Iteration iterationClone, ThingTransaction transaction) throws TransactionException
    {
        ArrayList<Thing> thingsToTransfer = new ArrayList<>(this.selectedDstMapResultForTransfer);
        
        Predicate<? super MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> selectedMappedElement = 
                x -> this.selectedDstMapResultForTransfer.stream().anyMatch(t -> t.getIid().equals(x.GetHubElement().getIid()));
                
        Collection<Relationship> relationships = this.dstMapResult.stream()
                .filter(selectedMappedElement)
                .flatMap(x -> x.GetRelationships().stream())
                .collect(Collectors.toList());
        
        this.logService.Append("Processing %s relationship(s)", relationships.size());
        
        thingsToTransfer.addAll(relationships);
                
        for (Thing thing : thingsToTransfer)
        {
            switch(thing.getClassKind())
            {
                case ElementDefinition:
                    this.PrepareElementDefinitionForTransfer(iterationClone, transaction, (ElementDefinition)thing);
                    break;
                case RequirementsSpecification:
                    this.PrepareRequirementForTransfer(iterationClone, transaction, (RequirementsSpecification)thing);
                    break;
                case BinaryRelationship:
                    this.AddOrUpdateIterationAndTransaction((BinaryRelationship)thing, iterationClone.getRelationship(), transaction);
                    break;
                default:
                    break;
            }
            
            if(thing.getContainer() == null)
            {
                this.logService.Append("%s thing %s has a null container", thing.getClassKind(), Level.ERROR, thing.getUserFriendlyName());
            }
        }
    }

    /**
     * Prepares the provided {@linkplain ElementDefinition} for transfer
     * 
     * @param iterationClone the {@linkplain Iteration} clone
     * @param transaction the {@linkplain ThingTransaction}
     * @param elementDefinition the {@linkplain ElementDefinition} to prepare
     * @throws TransactionException
     */
    private void PrepareElementDefinitionForTransfer(Iteration iterationClone, ThingTransaction transaction, 
            ElementDefinition elementDefinition) throws TransactionException
    {
        for (ElementUsage elementUsage : elementDefinition.getContainedElement())
        {
            this.AddOrUpdateIterationAndTransaction(elementUsage.getElementDefinition(), iterationClone.getElement(), transaction);
            this.AddOrUpdateIterationAndTransaction(elementUsage, elementDefinition.getContainedElement(), transaction);
        }

        this.AddOrUpdateIterationAndTransaction(elementDefinition, iterationClone.getElement(), transaction);
        
        for(Parameter parameter : elementDefinition.getParameter())
        {            
            transaction.createOrUpdate(parameter);
        }
    }

    /**
     * Prepares the provided {@linkplain ElementDefinition} for transfer
     * 
     * @param iterationClone the {@linkplain Iteration} clone
     * @param transaction the {@linkplain ThingTransaction}
     * @param requirementsSpecification the {@linkplain RequirementsSpecification} to prepare
     * @throws TransactionException
     */
    private void PrepareRequirementForTransfer(Iteration iterationClone, ThingTransaction transaction, 
            RequirementsSpecification requirementsSpecification) throws TransactionException
    {        
        this.AddOrUpdateIterationAndTransaction(requirementsSpecification, iterationClone.getRequirementsSpecification(), transaction);
        
        ContainerList<RequirementsGroup> groups = requirementsSpecification.getGroup();
        
        this.RegisterRequirementsGroups(transaction, groups);
        
        for(Requirement requirement : requirementsSpecification.getRequirement())
        {
            transaction.createOrUpdate(requirement);
            
            for (Definition definition : requirement.getDefinition())
            {
                transaction.createOrUpdate(definition);
            }
        }
    }

    /**
     * Registers the {@linkplain RequirementsGroup} to be created or updated
     * 
     * @param transaction
     * @param groups
     * @throws TransactionException
     */
    private void RegisterRequirementsGroups(ThingTransaction transaction, ContainerList<RequirementsGroup> groups) throws TransactionException
    {
        for(RequirementsGroup requirementsGroup : groups)
        {
            transaction.createOrUpdate(requirementsGroup);
            
            if(!requirementsGroup.getGroup().isEmpty())
            {
                this.RegisterRequirementsGroups(transaction, requirementsGroup.getGroup());
            }
        }
    }

    /**
     * Updates the {@linkplain ThingTransaction} and the {@linkplain ContainerList} with the provided {@linkplain Thing}
     * 
     * @param <T> the Type of the current {@linkplain Thing}
     * @param thing the {@linkplain Thing}
     * @param containerList the {@linkplain ContainerList} of {@linkplain Thing} typed as T
     * @param transaction the {@linkplain ThingTransaction}
     * @throws TransactionException
     */
    private <T extends Thing> void AddOrUpdateIterationAndTransaction(T thing, ContainerList<T> containerList, ThingTransaction transaction) throws TransactionException
    {
        try
        {
            if(containerList.stream().noneMatch(x -> x.getIid().equals(thing.getIid())))
            {
                containerList.add(thing);
            }

            transaction.createOrUpdate(thing);
        }
        catch (Exception exception)
        {
            this.logger.catching(exception);
        }
    }

    /**
     * Adds or Removes all {@linkplain TElement} from/to the relevant selected things to transfer
     * depending on whether the {@linkplain ClassKind} was specified
     * 
     * @param classKind the {@linkplain ClassKind} of the {@linkplain Thing}s to add or remove depending on which impact view it has been called from
     * @param shouldRemove a value indicating whether the things are to be removed
     */
    @Override
    public void AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind classKind, boolean shouldRemove)
    {
        if(classKind == null)
        {
            this.AddOrRemoveAllFromSelectedHubMapResultForTransfer(shouldRemove);
        }
        else
        {
            this.AddOrRemoveAllFromSelectedDstMapResultForTransfer(classKind, shouldRemove);
        }
    }
    
    /**
     * Adds or Removes all {@linkplain Thing} from/to the relevant selected things to transfer
     * 
     * @param classKind the {@linkplain ClassKind} of the {@linkplain Thing}s to add or remove depending on which impact view it has been called from
     * @param shouldRemove a value indicating whether the things are to be removed
     */
    private void AddOrRemoveAllFromSelectedDstMapResultForTransfer(ClassKind classKind, boolean shouldRemove)
    {
        Predicate<? super Thing> predicateClassKind = x -> x.getClassKind() == classKind;
        
        this.selectedDstMapResultForTransfer.removeIf(predicateClassKind);
        
        if(!shouldRemove)
        {
            this.selectedDstMapResultForTransfer.addAll(
                    this.dstMapResult.stream()
                        .map(MappedElementRowViewModel::GetHubElement)
                        .filter(predicateClassKind)
                        .collect(Collectors.toList()));
        }
    }

    /**
     * Adds or Removes all {@linkplain Class} from/to the relevant selected things to transfer
     * 
     * @param shouldRemove a value indicating whether the things are to be removed
     */
    private void AddOrRemoveAllFromSelectedHubMapResultForTransfer(boolean shouldRemove)
    {
        this.selectedHubMapResultForTransfer.clear();
        
        if(!shouldRemove)
        {
            this.selectedHubMapResultForTransfer.addAll(this.hubMapResult.stream()
                    .map(MappedElementRowViewModel::GetDstElement)
                    .collect(Collectors.toList()));
        }
    }
}
