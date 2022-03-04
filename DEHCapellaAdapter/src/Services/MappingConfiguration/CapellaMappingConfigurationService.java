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

import HubController.IHubController;
import Services.CapellaSession.ICapellaSessionService;
import Utils.Ref;
import ViewModels.Interfaces.IMappedElementRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedRequirementRowViewModel;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ExternalIdentifierMap;
import cdp4common.engineeringmodeldata.RequirementsSpecification;

/**
 * The {@linkplain CapellaMappingConfigurationService} is the implementation of {@linkplain MappingConfigurationService} for the Capella adapter
 */
public class CapellaMappingConfigurationService extends MappingConfigurationService<CapellaElement> implements ICapellaMappingConfigurationService
{
    /**
     * The {@linkplain ICapellaSessionService} instance
     */
    private ICapellaSessionService sessionService;

    /**
     * Initializes a new {@linkplain MagicDrawMappingConfigurationService}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param sessionService the {@linkplain ICapellaSessionService}
     */
    public CapellaMappingConfigurationService(IHubController hubController, ICapellaSessionService sessionService)
    {
        super(hubController);
        this.sessionService = sessionService;
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
        Optional<ImmutableTriple<UUID, ExternalIdentifier, UUID>> optionalCorrespondence = this.Correspondences.stream()
                .filter(x -> AreTheseEquals(x.middle.Identifier, element.getId()))
                .findFirst();
        
        if(!optionalCorrespondence.isPresent())
        {
            return false;
        }
        
        if(element instanceof Component)
        {
            var refElementDefinition = new Ref<>(ElementDefinition.class);
                        
            var mappedElement = new MappedElementDefinitionRowViewModel((Component)element, optionalCorrespondence.get().middle.MappingDirection);
            
            if(this.HubController.TryGetThingById(optionalCorrespondence.get().right, refElementDefinition))
            {
                mappedElement.SetHubElement(refElementDefinition.Get().clone(false));
            }
            
            refMappedElementRowViewModel.Set(mappedElement);
        }
        else if(element instanceof Requirement)
        {      
            var refRequirementsSpecification = new Ref<>(RequirementsSpecification.class);
            
            var mappedElement = new MappedRequirementRowViewModel((Requirement)element, optionalCorrespondence.get().middle.MappingDirection);
            
            if(this.HubController.TryGetThingById(optionalCorrespondence.get().right, refRequirementsSpecification))
            {
                mappedElement.SetHubElement(refRequirementsSpecification.Get().clone(true));
            }
            
            refMappedElementRowViewModel.Set(mappedElement);
        }
        
        return refMappedElementRowViewModel.HasValue();
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
}
