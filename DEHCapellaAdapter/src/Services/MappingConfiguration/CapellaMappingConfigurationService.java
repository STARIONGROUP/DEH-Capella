/*
 * CapellaMappingConfigurationService.java
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
package Services.MappingConfiguration;

import static Utils.Operators.Operators.AreTheseEquals;
import static Utils.Stereotypes.StereotypeUtils.GetChildren;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.requirement.Requirement;

import Enumerations.CapellaArchitecture;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.CapellaSession.ICapellaSessionService;
import Services.CapellaTransaction.ICapellaTransactionService;
import Utils.Ref;
import ViewModels.Interfaces.IMappedElementRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedHubRequirementRowViewModel;
import ViewModels.Rows.MappedRequirementBaseRowViewModel;
import ViewModels.Rows.MappedDstRequirementRowViewModel;
import cdp4common.commondata.NamedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ExternalIdentifierMap;
import cdp4common.engineeringmodeldata.RequirementsSpecification;

/**
 * The {@linkplain CapellaMappingConfigurationService} is the implementation of {@linkplain MappingConfigurationService} for the Capella adapter
 */
public class CapellaMappingConfigurationService extends MappingConfigurationService<CapellaElement, CapellaExternalIdentifier> implements ICapellaMappingConfigurationService
{
    /**
     * The {@linkplain ICapellaSessionService} instance
     */
    private final ICapellaSessionService sessionService;
    
    /**
     * The {@linkplain ICapellaTransactionService}
     */
    private final ICapellaTransactionService transactionService;

    /**
     * Initializes a new {@linkplain MagicDrawMappingConfigurationService}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param sessionService the {@linkplain ICapellaSessionService}
     * @param transactionService the {@linkplain ICapellaTransactionService}
     */
    public CapellaMappingConfigurationService(IHubController hubController, ICapellaSessionService sessionService, ICapellaTransactionService transactionService)
    {
        super(hubController, CapellaExternalIdentifier.class);
        this.sessionService = sessionService;
        this.transactionService = transactionService;
        
        this.HubController.GetIsSessionOpenObservable()
        .subscribe(x -> 
        {
            if(!x)
            {
                this.Correspondences.clear();
                this.SetExternalIdentifierMap(new ExternalIdentifierMap());
            }
        });
    }
    
    /**
     * Loads the mapping configuration and generates the map result respectively
     * 
     * @return a {@linkplain Collection} of {@linkplain IMappedElementRowViewModel}
     */
    @Override
    public Collection<IMappedElementRowViewModel> LoadMapping()
    {
        var mappedElements = new ArrayList<IMappedElementRowViewModel>();    
        var sessionsAndElementsMap = this.sessionService.GetAllCapellaElementsFromOpenSessions();
        
        for (var sessionUri : sessionsAndElementsMap.keySet())
        {
            mappedElements.addAll(this.LoadMapping(sessionsAndElementsMap.get(sessionUri)));
        }
        
        return mappedElements;
    }
    
    /**
     * Loads the mapping configuration and generates the map result respectively
     * 
     * @param elements a {@linkplain Collection} of {@code TDstElement}
     * @return a {@linkplain Collection} of {@linkplain IMappedElementRowViewModel}
     */
    @Override
    public Collection<IMappedElementRowViewModel> LoadMapping(Collection<CapellaElement> elements)
    {
        var mappedElements = new ArrayList<IMappedElementRowViewModel>();
        
        for (var element : elements)
        {
            var refMappedElementRowViewModel = new Ref<>(IMappedElementRowViewModel.class);
            
            if(this.TryGetMappedElement(element, refMappedElementRowViewModel))
            {
                mappedElements.add(refMappedElementRowViewModel.Get());
                
                var children = GetChildren(element, CapellaElement.class);
                
                if(!children.isEmpty())
                {
                    mappedElements.addAll(this.LoadMapping(children));
                }
            }
        }
        
        return mappedElements;
    }
    
    /**
     * Tries to get the {@linkplain IMappedElementRowViewModel} depending if the provided {@linkplain Class} 
     * has a mapping defined in the currently loaded externalIdentifier map and if the corresponding {@linkplain Thing} is present in the cache
     * 
     * @param element the {@linkplain CapellaElement} element
     * @return a {@linkplain Ref} of {@linkplain IMappedElementRowViewModel}
     */
    private boolean TryGetMappedElement(CapellaElement element, Ref<IMappedElementRowViewModel> refMappedElementRowViewModel)
    {
        Optional<ImmutableTriple<UUID, CapellaExternalIdentifier, UUID>> optionalCorrespondence = this.Correspondences.stream()
                .filter(x -> AreTheseEquals(x.middle.Identifier, element.getId()))
                .findFirst();
        
        if(!optionalCorrespondence.isPresent())
        {
            return false;
        }
        
        var mappingDirection = optionalCorrespondence.get().middle.MappingDirection;
        var targetArchitecture = optionalCorrespondence.get().middle.TargetArchitecture;
        var internalId = optionalCorrespondence.get().right;
        
        if(!(targetArchitecture == null || targetArchitecture.AreSameArchitecture(element)))
        {
            return false;
        }
        
        if(element instanceof Component)
        {
            var refElementDefinition = new Ref<>(ElementDefinition.class);
            
            var mappedElement = new MappedElementDefinitionRowViewModel(this.transactionService.Clone((Component)element), mappingDirection);
            mappedElement.SetTargetArchitecture(targetArchitecture);
            
            if(this.HubController.TryGetThingById(internalId, refElementDefinition))
            {
                mappedElement.SetHubElement(refElementDefinition.Get().clone(false));
            }
                        
            refMappedElementRowViewModel.Set(mappedElement);
        }
        else if(element instanceof Requirement)
        {
            if(mappingDirection == MappingDirection.FromHubToDst)
            {
                var mappedElement = new MappedHubRequirementRowViewModel(this.transactionService.Clone((Requirement)element), mappingDirection);
                this.GetMappedRequirement(mappedElement, internalId, cdp4common.engineeringmodeldata.Requirement.class);
                mappedElement.SetTargetArchitecture(targetArchitecture);
                
                refMappedElementRowViewModel.Set(mappedElement);
            }
            else
            {
                var mappedElement = new MappedDstRequirementRowViewModel((Requirement)element, mappingDirection);
                this.GetMappedRequirement(mappedElement, internalId, RequirementsSpecification.class);
                refMappedElementRowViewModel.Set(mappedElement);
            }            
        }
        
        return refMappedElementRowViewModel.HasValue();
    }

    /**
     * Gets the mapped {@linkplain #TThing}
     * 
     * @param <TThing> the type of {@linkplain Thing} to query from the cache
     * @param mappedElement the {@linkplain MappedRequirementBaseRowViewModel}
     * @param internalId the internal id of the queried Thing
     * @param clazz the {@linkplain Class} of the queried Thing
     */
    @SuppressWarnings("unchecked")
    private <TThing extends Thing & NamedThing> void GetMappedRequirement(MappedRequirementBaseRowViewModel<TThing> mappedElement, UUID internalId, Class<TThing> clazz)
    {
        var refHubRequirement = new Ref<>(clazz);
        
        if(this.HubController.TryGetThingById(internalId, refHubRequirement))
        {
            mappedElement.SetHubElement((TThing) refHubRequirement.Get().clone(true));
        }
    }

    /**
     * Creates a new {@linkplain ExternalIdentifierMap} and sets the current as the new one
     * 
     * @param newName the {@linkplain String} name of the new configuration
     * @param addTheTemporyMapping a value indicating whether the current temporary {@linkplain ExternalIdentifierMap} 
     * contained correspondence should be transfered the new one
     * 
     * @return the new configuration {@linkplain ExternalIdentifierMap}
     */
    @Override
    public ExternalIdentifierMap CreateExternalIdentifierMap(String newName, String modelName, boolean addTheTemporyMapping)
    {
        return super.CreateExternalIdentifierMap(newName, modelName, DstController.DstController.THISTOOLNAME, addTheTemporyMapping);
    }

    /**
     * Adds one correspondence to the {@linkplain ExternalIdentifierMap}
     * 
     * @param internalId the {@linkplain UUID} that identifies the thing to correspond to
     * @param externalId the {@linkplain Object} that identifies the object to correspond to
     * @param mappingDirection the {@linkplain MappingDirection} the mapping belongs to
     * @param targetArchitecture the target {@linkplain CapellaArchitecture}
     */
    @Override
    public void AddToExternalIdentifierMap(UUID internalId, String externalId, CapellaArchitecture targetArchitecture,
            MappingDirection mappingDirection)
    {
        var externalIdentifier = new CapellaExternalIdentifier();
        externalIdentifier.MappingDirection = mappingDirection;
        externalIdentifier.Identifier = externalId;
        externalIdentifier.TargetArchitecture = targetArchitecture;
        
        this.AddToExternalIdentifierMap(internalId, externalIdentifier, x -> x.getMiddle().TargetArchitecture == targetArchitecture);
    }
}
