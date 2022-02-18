/*
 * RequirementToRequirementsSpecificationMappingRule.java
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
package MappingRules;

import static Utils.Operators.Operators.AreTheseEquals;
import static Utils.Stereotypes.StereotypeUtils.GetChildren;
import static Utils.Stereotypes.StereotypeUtils.GetShortName;
import static Utils.Stereotypes.StereotypeUtils.IsParentOf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.polarsys.capella.core.data.requirement.Requirement;
import org.polarsys.capella.core.data.requirement.RequirementsPkg;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Services.MappingEngineService.MappingRule;
import Utils.Ref;
import Utils.Stereotypes.CapellaRequirementCollection;
import ViewModels.Rows.MappedRequirementRowViewModel;
import cdp4common.commondata.Definition;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;

/**
 * The {@linkplain BlockDefinitionMappingRule} is the mapping rule implementation for transforming {@linkplain CapellaRequirementCollection} to {@linkplain RequirementsSpecification}
 */
public class RequirementToRequirementsSpecificationMappingRule extends MappingRule<CapellaRequirementCollection, ArrayList<MappedRequirementRowViewModel>>
{
    /**
     * The {@linkplain IHubController}
     */
    private IHubController hubController;
    
    /**
     * The {@linkplain ICapellaMappingConfigurationService}
     */
    private ICapellaMappingConfigurationService mappingConfiguration;

    /**
     * The collection of {@linkplain RequirementsSpecification} that are being mapped
     */
    private ArrayList<RequirementsSpecification> requirementsSpecifications = new ArrayList<RequirementsSpecification>();

    /**
     * The collection of {@linkplain RequirementsGroup} that are being mapped
     */
    private ArrayList<RequirementsGroup> temporaryRequirementsGroups = new ArrayList<RequirementsGroup>();

    /**
     * Initializes a new {@linkplain RequirementToRequirementsSpecificationMappingRule}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain ICapellaMappingConfigurationService}
     */
    public RequirementToRequirementsSpecificationMappingRule(IHubController hubController, ICapellaMappingConfigurationService mappingConfiguration)
    {
        this.hubController = hubController;
        this.mappingConfiguration = mappingConfiguration;
    }    
    
    /**
     * Transforms an {@linkplain CapellaRequirementCollection} of type {@linkplain Requirement} to an {@linkplain ArrayList} of {@linkplain RequirementsSpecification}
     * 
     * @param input the {@linkplain CapellaRequirementCollection} to transform
     * @return the {@linkplain ArrayList} of {@linkplain MappedRequirementRowViewModel}
     */
    @Override
    public ArrayList<MappedRequirementRowViewModel> Transform(Object input)
    {
        try
        {
            CapellaRequirementCollection mappedElements = this.CastInput(input);
            this.Map(mappedElements);
            this.SaveMappingConfiguration(mappedElements);
            return new ArrayList<MappedRequirementRowViewModel>(mappedElements);
        }
        catch (Exception exception)
        {
            this.Logger.catching(exception);
            return new ArrayList<MappedRequirementRowViewModel>();
        }
        finally
        {
            this.requirementsSpecifications.clear();
            this.temporaryRequirementsGroups.clear();
        }
    }
    
    /**
     * Saves the mapping configuration
     * 
     * @param elements the {@linkplain CapellaRequirementCollection}
     */
    private void SaveMappingConfiguration(CapellaRequirementCollection elements)
    {
        for (MappedRequirementRowViewModel mappedRequirementsSpecification : elements)
        {
            this.mappingConfiguration.AddToExternalIdentifierMap(
                    mappedRequirementsSpecification.GetHubElement().getIid(), mappedRequirementsSpecification.GetDstElement().getId(), MappingDirection.FromDstToHub);
        }
    }
    
    /**
     * Maps the provided collection of requirements
     * 
     * @param mappedRequirements the collection of {@linkplain Requirement} to map
     */
    private void Map(CapellaRequirementCollection mappedRequirements)
    {
        for (MappedRequirementRowViewModel mappedRequirement : mappedRequirements)
        {
            var parent = mappedRequirement.GetDstElement().eContainer();
            
            Ref<RequirementsSpecification> refRequirementsSpecification = new Ref<>(RequirementsSpecification.class);
               
            if(!mappedRequirement.GetShouldCreateNewTargetElementValue() && mappedRequirement.GetHubElement() != null)
            {
                refRequirementsSpecification.Set(mappedRequirement.GetHubElement());
            }
            else
            {
                while (!(this.CanBeARequirementSpecification(parent)
                        && parent != null && parent instanceof RequirementsPkg
                        && this.TryGetOrCreateRequirementSpecification((RequirementsPkg)parent, refRequirementsSpecification)))
                {
                    if(parent == null)
                    {
                        break;
                    }
                    
                    parent = parent.eContainer();
                }
            }
            
            if(!refRequirementsSpecification.HasValue())
            {
                this.Logger.error(
                        String.format("The mapping of the current requirement %s is no possible, because no eligible parent could be found current RequirementsPkg name %s", 
                                mappedRequirement.GetDstElement().getName(), mappedRequirement.GetDstElement().eContainer()));
                
                continue;
            }

            mappedRequirement.SetHubElement(refRequirementsSpecification.Get());
            
            var refRequirementsGroup = new Ref<>(RequirementsGroup.class);
            var refRequirement = new Ref<>(cdp4common.engineeringmodeldata.Requirement.class);
            
            if(!TryCreateRelevantGroupsAndTheRequirement(mappedRequirement.GetDstElement(), GetChildren(parent), refRequirementsSpecification, refRequirementsGroup, refRequirement))
            {
                this.Logger.error(String.format("Could not map requirement %s", mappedRequirement.GetDstElement().getName()));
            }
        }
    }
    
    /**
     * Tries to create the groups between the current {@linkplain RequirementsSpecification} and the current {@linkplain Requirement} to be created,
     * and creates the {@linkplain Requirement}. This method is called recursively until the methods reaches the {@linkplain Requirement}
     *
     * @param requirement the {@linkplain Requirement} requirement from Capella
     * @param elements the children of the current {@linkplain RequirementsPkg} being processed
     * @param refRequirementsSpecification the {@linkplain Ref} of {@linkplain RequirementsSpecification}
     * @param refRequirementsGroup the {@linkplain Ref} of {@linkplain RequirementsGroup}, 
     * holds the last group that was created, also the closest to the {@linkplain Requirement}
     * @param refRequirement the {@linkplain Ref} of {@linkplain Requirement}
     * @return a value indicating whether the requirement has been created/updated
     * @throws UnsupportedOperationException in case the {@linkplain Requirement could not be created}
     */
    private boolean TryCreateRelevantGroupsAndTheRequirement(Requirement requirement, Collection<EObject> elements,
            Ref<RequirementsSpecification> refRequirementsSpecification, Ref<RequirementsGroup> refRequirementsGroup,
            Ref<cdp4common.engineeringmodeldata.Requirement> refRequirement)
    {
        for (var element : elements)
        {
            if(IsParentOf(element, requirement))
            {                
                if(!this.TryGetOrCreateRequirementGroup((RequirementsPkg)element, refRequirementsSpecification, refRequirementsGroup))
                {
                    this.Logger.error(String.format("Could not create the requirement %s, because the creation/update of the requirement group %s failed", 
                            requirement.getName(), ((RequirementsPkg)element).getName()));
                    
                    break;
                }
            }        
                        
            else if(element instanceof Requirement && AreTheseEquals(((Requirement)element).getId(), requirement.getId()))
            {
                if(!this.TryGetOrCreateRequirement((Requirement)element, refRequirementsSpecification, refRequirementsGroup, refRequirement))
                {
                    throw new UnsupportedOperationException(
                            String.format("Could not create the requirement %s", requirement.getName()));
                }
            }
            
            if(this.TryCreateRelevantGroupsAndTheRequirement(requirement, GetChildren(element), refRequirementsSpecification, refRequirementsGroup, refRequirement))
            {
                break;
            }
        }
        
        return refRequirement.HasValue();
    }

    /**
     * Tries to get from the current {@linkplain RequirementsSpecification} the represented {@linkplain Requirement} by the provided {@linkplain Requirement} element
     * or creates it.
     * 
     * @param element the {@linkplain Requirement} element
     * @param refRequirementsGroup the {@linkplain Ref} of {@linkplain RequirementsGroup}, the closest parent in the tree of the {@linkplain Requirement}
     * @param refRequirement the {@linkplain Ref} of {@linkplain Requirement}
     * @return a value indicating whether the {@linkplain Requirement} has been created or retrieved
     */
    private boolean TryGetOrCreateRequirement(Requirement element, Ref<RequirementsSpecification> refRequirementsSpecification, 
            Ref<RequirementsGroup> refRequirementsGroup, Ref<cdp4common.engineeringmodeldata.Requirement> refRequirement)
    {
        var optionalRequirement = refRequirementsSpecification.Get()
                .getRequirement()
                .stream()
                .filter(x -> this.AreShortNamesEquals(x, GetShortName(element)))
                .findFirst();
        
        if(optionalRequirement.isPresent())
        {
            refRequirement.Set(optionalRequirement.get().clone(true));
        }
        else
        {
            var requirement = new cdp4common.engineeringmodeldata.Requirement();
            requirement.setIid(UUID.randomUUID());
            requirement.setName(element.getName());
            requirement.setShortName(GetShortName(element));
            requirement.setOwner(this.hubController.GetCurrentDomainOfExpertise());
            
            requirement.setGroup(refRequirementsGroup.Get());
            refRequirementsSpecification.Get().getRequirement().add(requirement);
            refRequirement.Set(requirement);
        }
        
        this.UpdateOrCreateDefinition(element, refRequirement);

        refRequirementsSpecification.Get().getRequirement().removeIf(x -> x.getIid().equals(refRequirement.Get().getIid()));
        refRequirementsSpecification.Get().getRequirement().add(refRequirement.Get());
        
        return refRequirement.HasValue();
    }

    /**
     * Updates or creates the definition according to the provided {@linkplain Requirement} assignable to the {@linkplain Requirement}  
     * 
     * @param requirement the {@linkplain Requirement} element that represents the requirement in Capella
     * @param refRequirement the {@linkplain Ref} of {@linkplain Requirement} to update
     */
    private void UpdateOrCreateDefinition(Requirement requirement, Ref<cdp4common.engineeringmodeldata.Requirement> refRequirement)
    {
        if(refRequirement.HasValue())
        {            
            Definition definition = refRequirement.Get().getDefinition()
                    .stream()
                    .filter(x -> x.getLanguageCode().equalsIgnoreCase("en"))
                    .findFirst()
                    .map(x -> x.clone(true))
                    .orElse(this.createDefinition());

            definition.setContent(requirement.getDescription());
            
            refRequirement.Get().getDefinition().removeIf(x -> x.getIid().equals(definition.getIid()));            
            refRequirement.Get().getDefinition().add(definition);
        }
    }
    
    /**
     * Creates a {@linkplain Definition} to be added to a {@linkplain Requirement}
     * 
     * @return a {@linkplain Definition}
     */
    private Definition createDefinition()
    {
        Definition definition = new Definition();
        definition.setIid(UUID.randomUUID());
        definition.setLanguageCode("en");
        return definition;
    }

    /**
     * Try to create the {@linkplain RequirementsSpecification} represented by the provided {@linkplain RequirementsPkg}
     * 
     * @param currentParent the {@linkplain RequirementsPkg} to create or retrieve the {@linkplain RequirementsSpecification} that represents it
     * @param refRequirementsSpecification the {@linkplain Ref} parent {@linkplain RequirementsSpecification}
     * @param refRequirementsGroup the {@linkplain Ref} of {@linkplain RequirementsGroup}
     * @return a value indicating whether the {@linkplain RequirementsGroup} has been found or created
     */
    private boolean TryGetOrCreateRequirementGroup(RequirementsPkg currentParent, Ref<RequirementsSpecification> refRequirementsSpecification, Ref<RequirementsGroup> refRequirementsGroup)
    {
        Ref<RequirementsGroup> refCurrentRequirementsGroup = new Ref<RequirementsGroup>(RequirementsGroup.class);
        
        if(this.TryToFindGroup(currentParent, refRequirementsSpecification, refCurrentRequirementsGroup))
        {
            refRequirementsGroup.Set(refCurrentRequirementsGroup.Get());
        }
        else
        {
            RequirementsGroup requirementsgroup = new RequirementsGroup();
            requirementsgroup.setName(currentParent.getName());
            requirementsgroup.setShortName(GetShortName(currentParent));
            requirementsgroup.setIid(UUID.randomUUID());
            requirementsgroup.setOwner(this.hubController.GetCurrentDomainOfExpertise());
            
            if(refRequirementsGroup.HasValue())
            {
                refRequirementsGroup.Get().getGroup().add(requirementsgroup);
            }
            else
            {
                refRequirementsSpecification.Get().getGroup().add(requirementsgroup);                
            }
            
            refRequirementsGroup.Set(requirementsgroup);
            this.temporaryRequirementsGroups.add(requirementsgroup);
        }
        
        return refRequirementsGroup.HasValue();
    }
    
    /**
     * Tries to find the group represented by / representing the provided {@linkplain RequirementsPkg}
     * 
     * @param currentPackage the {@linkplain RequirementsPkg}
     * @param refRequirementsSpecification the {@linkplain Ref} of the current {@linkplain RequirementsSpecification}
     * @param refRequirementsGroup the {@linkplain Ref} of {@linkplain RequirementsGroup}
     * @return a value indicating whether the {@linkplain RequirementsGroup} has been found
     */
    private boolean TryToFindGroup(RequirementsPkg currentPackage, Ref<RequirementsSpecification> refRequirementsSpecification, Ref<RequirementsGroup> refRequirementsGroup)
    {
        Optional<RequirementsGroup> optionalRequirementsGroup = Stream.concat(this.temporaryRequirementsGroups.stream(), 
                refRequirementsSpecification.Get().getAllContainedGroups().stream())
            .filter(x -> this.AreShortNamesEquals(x, GetShortName(currentPackage)))
            .findFirst();
        
        if(optionalRequirementsGroup.isPresent())
        {
            refRequirementsGroup.Set(optionalRequirementsGroup.get().getRevisionNumber() > 0 
                    ? optionalRequirementsGroup.get().clone(true)
                            : optionalRequirementsGroup.get());
        }
        
        return refRequirementsGroup.HasValue();
    }

    /**
     * Try to create the {@linkplain RequirementsSpecification} represented by the provided {@linkplain RequirementsPkg}
     * 
     * @param currentPackage the {@linkplain RequirementsPkg} to create or retrieve the {@linkplain RequirementsSpecification} that represents it
     * @param refRequirementSpecification the {@linkplain Ref} of {@linkplain RequirementsSpecification}
     * @return a value indicating whether the {@linkplain RequirementsGroup} has been found or created
     */
    private boolean TryGetOrCreateRequirementSpecification(RequirementsPkg currentPackage, Ref<RequirementsSpecification> refRequirementSpecification)
    {
        Optional<RequirementsSpecification> optionalRequirementsSpecification = this.requirementsSpecifications
                .stream()
                .filter(x -> this.AreShortNamesEquals(x, GetShortName(currentPackage)))
                .findFirst();

        if(optionalRequirementsSpecification.isPresent())
        {
            refRequirementSpecification.Set(optionalRequirementsSpecification.get());
        }
        else
        {
            optionalRequirementsSpecification = this.hubController.GetOpenIteration()
                    .getRequirementsSpecification()
                    .stream()
                    .filter(x -> this.AreShortNamesEquals(x, GetShortName(currentPackage)))
                    .findFirst();
            
            if(optionalRequirementsSpecification.isPresent())
            {
                refRequirementSpecification.Set(optionalRequirementsSpecification.get().clone(true));
            }
            else
            {
                RequirementsSpecification requirementsSpecification = new RequirementsSpecification();
                requirementsSpecification.setName(currentPackage.getName());
                requirementsSpecification.setShortName(GetShortName(currentPackage));
                requirementsSpecification.setIid(UUID.randomUUID());
                requirementsSpecification.setOwner(this.hubController.GetCurrentDomainOfExpertise());
                refRequirementSpecification.Set(requirementsSpecification);             
            }
        
            this.requirementsSpecifications.add(refRequirementSpecification.Get());
        }
        
        return refRequirementSpecification.HasValue();
    }
    
    /**
     * Verifies if the provided {@linkplain eObject} contains any requirement
     * 
     * @param eObject the current {@linkplain EObject} to verify
     * @return a value indicating whether the {@linkplain eObject} has any requirement as child
     */
    public boolean CanBeARequirementSpecification(EObject eObject)
    {
        return eObject instanceof RequirementsPkg && GetChildren(eObject, RequirementsPkg.class).size() > 0;
    }
}
