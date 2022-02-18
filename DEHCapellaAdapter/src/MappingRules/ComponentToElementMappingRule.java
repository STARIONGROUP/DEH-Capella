/*
 * ComponentToElementMappingRule.java
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
import static Utils.Stereotypes.StereotypeUtils.GetShortName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.information.Property;
import org.polarsys.capella.core.data.information.datatype.BooleanType;
import org.polarsys.capella.core.data.information.datatype.StringType;
import org.polarsys.capella.core.data.information.datavalue.LiteralBooleanValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralNumericValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralStringValue;
import org.polarsys.capella.core.data.information.datavalue.NumericValue;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Services.MappingEngineService.MappingRule;
import Utils.Ref;
import Utils.ValueSetUtils;
import Utils.Stereotypes.CapellaComponentCollection;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ElementUsage;
import cdp4common.engineeringmodeldata.Parameter;
import cdp4common.engineeringmodeldata.ParameterSwitchKind;
import cdp4common.engineeringmodeldata.ParameterValueSet;
import cdp4common.sitedirectorydata.BooleanParameterType;
import cdp4common.sitedirectorydata.MeasurementScale;
import cdp4common.sitedirectorydata.MeasurementUnit;
import cdp4common.sitedirectorydata.NumberSetKind;
import cdp4common.sitedirectorydata.ParameterType;
import cdp4common.sitedirectorydata.QuantityKind;
import cdp4common.sitedirectorydata.RatioScale;
import cdp4common.sitedirectorydata.ReferenceDataLibrary;
import cdp4common.sitedirectorydata.SimpleQuantityKind;
import cdp4common.sitedirectorydata.SimpleUnit;
import cdp4common.sitedirectorydata.TextParameterType;
import cdp4common.types.ContainerList;
import cdp4common.types.ValueArray;
import cdp4dal.operations.ThingTransactionImpl;
import cdp4dal.operations.TransactionContextResolver;

/**
 * The {@linkplain ComponentToElementMappingRule} is the mapping rule implementation for transforming Capella {@linkplain Component} to {@linkplain ElementDefinition}
 */
public class ComponentToElementMappingRule extends MappingRule<CapellaComponentCollection, ArrayList<MappedElementDefinitionRowViewModel>>
{
    /**
     * The {@linkplain IHubController}
     */
    private final IHubController hubController;

    /**
     * The {@linkplain IMagicDrawMappingConfigurationService} instance
     */
    private final ICapellaMappingConfigurationService mappingConfiguration;
    
    /**
     * The {@linkplain CapellaComponentCollection} of {@linkplain MappedElementDefinitionRowViewModel}
     */
    private CapellaComponentCollection elements;
    
    /**
     * Initializes a new {@linkplain BlockDefinitionMappingRule}
     * 
     * @param HubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain IMagicDrawMappingConfigurationService}
     */
    public ComponentToElementMappingRule(IHubController hubController, ICapellaMappingConfigurationService mappingConfiguration)
    {
        this.hubController = hubController;
        this.mappingConfiguration = mappingConfiguration;
    }
    
    /**
     * Transforms an {@linkplain CapellaComponentCollection} of {@linkplain Component} to an {@linkplain ArrayList} of {@linkplain ElementDefinition}
     * 
     * @param input the {@linkplain CapellaComponentCollection} of {@linkplain Component} to transform
     * @return the {@linkplain ArrayList} of {@linkplain MappedElementDefinitionRowViewModel}
     */
    @Override
    public ArrayList<MappedElementDefinitionRowViewModel> Transform(Object input)
    {
        try
        {
            this.elements = this.CastInput(input);
            this.Map(this.elements);
            this.SaveMappingConfiguration(this.elements);
            return new ArrayList<>(this.elements);
        }
        catch (Exception exception)
        {
            this.Logger.catching(exception);
            return new ArrayList<>();
        }
    }

    /**
     * Saves the mapping configuration
     * 
     * @param elements the {@linkplain CapellaComponentCollection}
     */
    private void SaveMappingConfiguration(CapellaComponentCollection elements)
    {
        for (MappedElementDefinitionRowViewModel mappedElement : elements)
        {
            this.mappingConfiguration.AddToExternalIdentifierMap(
                    mappedElement.GetHubElement().getIid(), mappedElement.GetDstElement().getId(), MappingDirection.FromDstToHub);
        }
    }

    /**
     * Maps the provided collection of  {@linkplain Component}
     * 
     * @param mappedElementDefinitions the collection of {@linkplain Component} to map
     */
    private void Map(CapellaComponentCollection mappedElementDefinitions)
    {        
        for (MappedElementDefinitionRowViewModel mappedElement : new ArrayList<MappedElementDefinitionRowViewModel>(mappedElementDefinitions))
        {
            if(mappedElement.GetHubElement() == null)
            {
                mappedElement.SetHubElement(this.GetOrCreateElementDefinition(mappedElement.GetDstElement()));
            }
            
            this.MapContainedElement(mappedElement);
            
            this.MapProperties(mappedElement.GetHubElement(), mappedElement.GetDstElement());
        }
    }

    /**
     * Maps the contained element of the provided {@linkplain mappedElement}
     * 
     * @param mappedElement the {@linkplain MappedElementDefinitionRowViewModel}
     */
    private void MapContainedElement(MappedElementDefinitionRowViewModel mappedElement)
    {
        for (var containedElement : mappedElement.GetDstElement().eContents()
                .stream()
                .filter(x -> x instanceof Component)
                .map(x -> (Component)x)
                .collect(Collectors.toList()))
        {
            this.MapContainedElement(mappedElement.GetHubElement(), containedElement);
        }
    }
    
    /**
     * Maps the provided contained element
     * 
     * @param container the {@linkplain ElementDefinition} container
     * @param component the contained element to map
     */
    private void MapContainedElement(ElementDefinition container, Component component)
    {
        MappedElementDefinitionRowViewModel mappedElement = this.elements.stream()
                .filter(x -> AreTheseEquals(x.GetDstElement().getId(), component.getId()))
                .findFirst()
                .orElseGet(() -> 
                {
                    MappedElementDefinitionRowViewModel element = 
                            new MappedElementDefinitionRowViewModel(
                                    this.GetOrCreateElementDefinition(component), component, MappingDirection.FromDstToHub);
                    
                    this.elements.add(element);
                    this.MapContainedElement(element);
                    return element;
        
                });
        
        if(mappedElement.GetHubElement() == null)
        {
            mappedElement.SetHubElement(this.GetOrCreateElementDefinition(component));
        }
        else
        {
            mappedElement.SetHubElement(mappedElement.GetHubElement().clone(true));
        }

        this.MapProperties(mappedElement.GetHubElement(), component);
        
        if(container.getContainedElement()
                .stream().anyMatch(x -> AreTheseEquals(x.getName(), mappedElement.GetHubElement().getName())))
        {
            return;
        }
        
        var elementUsage = new ElementUsage();
        elementUsage.setName(mappedElement.GetHubElement().getName());
        elementUsage.setShortName(mappedElement.GetHubElement().getShortName());
        elementUsage.setIid(UUID.randomUUID());
        elementUsage.setOwner(this.hubController.GetCurrentDomainOfExpertise());
        elementUsage.setElementDefinition(mappedElement.GetHubElement());
        
        container.getContainedElement().add(elementUsage);
    }

    /**
     * Gets an existing or creates an {@linkplain ElementDefinition} that will be mapped to the {@linkplain Component} 
     * represented in the provided {@linkplain MappedElementDefinitionRowViewModel}
     *
     * @param dstElement the DST element {@linkplain Component}
     * @return an {@linkplain ElementDefinition}
     */
    private ElementDefinition GetOrCreateElementDefinition(Component component)
    {
        String shortName = GetShortName(component);
        
        ElementDefinition elementDefinition = this.elements.stream()
                .filter(x -> x.GetHubElement() != null && AreTheseEquals(x.GetHubElement().getShortName(), shortName))
                .map(x -> x.GetHubElement())
                .findFirst()
                .orElse(this.hubController.GetOpenIteration()
                    .getElement()
                    .stream()
                    .filter(x -> AreTheseEquals(x.getShortName(), shortName))
                    .findFirst()
                    .orElse(null));
        
        if(elementDefinition == null)
        {
            elementDefinition = new ElementDefinition();
            elementDefinition.setIid(UUID.randomUUID());
            elementDefinition.setName(component.getName());
            elementDefinition.setShortName(shortName);
            elementDefinition.setOwner(this.hubController.GetCurrentDomainOfExpertise());
            
            return elementDefinition;
        }

        return elementDefinition.clone(true);
    }
    
    /**
     * Maps the properties of the specified block
     * 
     * @param elementDefinition the {@linkplain ElementDefinition} that represents the Component
     * @param component the source {@linkplain Component}
     */
    private void MapProperties(ElementDefinition elementDefinition, Component component)
    {
        for (Property property : component.getContainedProperties())
        {
            Optional<Parameter> existingParameter = elementDefinition.getContainedParameter().stream()
                    .filter(x -> this.AreShortNamesEquals(x.getParameterType(), property.getName()))
                    .findAny();

            Ref<ParameterType> refParameterType = new Ref<>(ParameterType.class);
            Parameter parameter = null;
            
            Ref<String> refValue = new Ref<>(String.class, "");
            boolean hasValue = this.TryGetValueFromProperty(property, refValue);
            
            if(!existingParameter.isPresent() && hasValue)
            {
                if(this.TryCreateParameterType(property, refParameterType))
                {
                    parameter = new Parameter();
                    parameter.setIid(UUID.randomUUID());
                    parameter.setOwner(this.hubController.GetCurrentDomainOfExpertise());
                    parameter.setParameterType(refParameterType.Get());

                    elementDefinition.getParameter().add(parameter);
                    
                    if(refParameterType.Get() instanceof QuantityKind)
                    {
                        parameter.setScale(((QuantityKind)refParameterType.Get()).getDefaultScale());
                    }
                }
                else
                {
                    this.Logger.error(String.format("Coulnd create ParameterType %s", property.getName()));
                    continue;
                }
            }
            else if (existingParameter.isPresent() && hasValue)
            {
                parameter = existingParameter.get().clone(true);
            }
            else
            {
                this.Logger.error(String.format("Could not map property %s for element definition %s", property.getName(), elementDefinition.getName()));
                continue;
            }

            this.UpdateValueSet(parameter, refValue);

        }
        
        this.Logger.error(String.format("ElementDefinition has %s parameters", elementDefinition.getParameter().size()));
    }
    
    /**
     * Tries to extract the value from the provided property and returns it as string
     * 
     * @return a value indicating whether the value has been extracted
     */
    private boolean TryGetValueFromProperty(Property property, Ref<String> refValue)
    {
        var valueSpecification = property.getOwnedDefaultValue();
        
        if(valueSpecification instanceof LiteralNumericValue)
        {
            refValue.Set(((LiteralNumericValue)valueSpecification).getValue());
        }
        else if(valueSpecification instanceof LiteralBooleanValue) 
        {
            refValue.Set(String.valueOf(((LiteralBooleanValue)valueSpecification).isValue()));
        }
        else if(valueSpecification instanceof LiteralStringValue) 
        {
            refValue.Set(((LiteralStringValue)valueSpecification).getValue());
        }        
        
        return refValue.HasValue();
    }

    /**
     * Tries to create a {@linkplain ParameterType} if it doesn't exist yet in the chain of rdls
     * 
     * @param property the {@linkplain Property} to create the {@linkplain ParameterType} from
     * @param refParameterType the {@linkplain Ref} of {@linkplain ParameterType} 
     * @return a value indicating whether the {@linkplain ParameterType} has been successfully created or retrieved from the chain of rdls
     */
    private boolean TryCreateParameterType(Property property, Ref<ParameterType> refParameterType)
    {
        try
        {
            String shortName = GetShortName(property.getName());
            
            if(!this.hubController.TryGetThingFromChainOfRdlBy(x -> this.AreShortNamesEquals(x, shortName) || this.AreShortNamesEquals(x, shortName.substring(0, 1)), refParameterType))
            {
                ParameterType parameterType = null;
                
                if(property.getOwnedDefaultValue() instanceof LiteralNumericValue)
                {
                    parameterType = new SimpleQuantityKind();

                    SimpleQuantityKind quantityKind = ((SimpleQuantityKind)parameterType);
                    
                    Ref<MeasurementScale> refScale = new Ref<>(MeasurementScale.class);
                    
                    if(this.TryCreateOrGetMeasurementScale(property, refScale))
                    {
                        quantityKind.getAllPossibleScale().add(refScale.Get());
                        quantityKind.setDefaultScale(refScale.Get());
                    }
                }                
                else if(property.getOwnedDefaultValue() instanceof LiteralBooleanValue)
                {
                    parameterType = new BooleanParameterType();
                }
                else if(property.getOwnedDefaultValue() instanceof LiteralStringValue)
                {
                    parameterType = new TextParameterType();
                }                
                else
                {
                    this.Logger.error(String.format("The property %s value type isn't supported by the adapter", property.getName()));
                }
                                
                if(parameterType != null)
                {
                    parameterType.setIid(UUID.randomUUID());
                    parameterType.setName(property.getName());
                    parameterType.setShortName(shortName);
                    parameterType.setSymbol(shortName.substring(0, 1));
                    
                    ReferenceDataLibrary referenceDataLibrary = this.hubController.GetDehpOrModelReferenceDataLibrary().clone(false);
                    referenceDataLibrary.getParameterType().add(parameterType);
                    return this.TryCreateReferenceDataLibraryThing(parameterType, referenceDataLibrary, refParameterType);
                }
                
                return false;
            }
            
            return true;
        }
        catch(Exception exception)
        {
            this.Logger.error(String.format("Could not create the parameter type with the shortname: %s, because %s", property.getName(), exception));
            this.Logger.catching(exception);
            return false;
        }
    }

    /**
     * Tries to create a new {@linkplain MeasurementScale} based on the provided {@linkplain valueSpecification}
     * 
     * @param property the typed property
     * @param refScale the {@linkplain Ref} of {@linkplain MeasurementScale} as out parameter
     * @return a {@linkplain boolean} indicating whether the {@linkplain refScale} is not null
     */
    @SuppressWarnings("resource")
    private boolean TryCreateOrGetMeasurementScale(Property property, Ref<MeasurementScale> refScale)
    {
        var unit = ((LiteralNumericValue)property.getOwnedDefaultValue()).getUnit();
        
        String unitName;
        String scaleShortName;
        
        if(unit == null || unit.getName() == null)
        {
            unitName = "1";
            scaleShortName = "-";
        }
        else
        {
            unitName = unit.getName();
            scaleShortName = GetShortName(unitName);
        }
        
        
        if(!this.hubController.TryGetThingFromChainOfRdlBy(x -> x.getShortName().equals(scaleShortName), refScale))
        {
            MeasurementScale newScale = new RatioScale();
            newScale.setName(unitName);
            newScale.setNumberSet(NumberSetKind.REAL_NUMBER_SET);
            
            Ref<MeasurementUnit> refMeasurementUnit = new Ref<>(MeasurementUnit.class);
            
            if(!this.TryCreateOrGetMeasurementUnit(unitName, refMeasurementUnit))
            {   
                return false;
            }
            
            newScale.setUnit(refMeasurementUnit.Get());
            newScale.setShortName(refMeasurementUnit.Get().getShortName());
            
            var referenceDataLibrary = this.hubController.GetDehpOrModelReferenceDataLibrary().clone(false);
            referenceDataLibrary.getScale().add(newScale);
            return this.TryCreateReferenceDataLibraryThing(newScale, referenceDataLibrary, refScale);
        }
        
        return true;
    }
    
    /**
     * Tries to create a new {@linkplain MeasurementScale} based on the provided {@linkplain valueSpecification}
     * or to retrieve it from the cache
     * 
     * @param unitName the unit name
     * @param refMeasurementUnit the {@linkplain Ref} of {@linkplain MeasurementUnit} as out parameter
     * @return a {@linkplain boolean} indicating whether the {@linkplain refMeasurementUnit} is not null
     */
    private boolean TryCreateOrGetMeasurementUnit(String unitName, Ref<MeasurementUnit> refMeasurementUnit)
    {
        String unitShortName = GetShortName(unitName);
        
        if(!this.hubController.TryGetThingFromChainOfRdlBy(x -> x.getShortName().equals(unitShortName) || x.getName().equals(unitName) || x.getShortName().equals("-"), refMeasurementUnit))
        {
            var newMeasurementUnit = new SimpleUnit();
            newMeasurementUnit.setName(unitName);
            newMeasurementUnit.setShortName(unitShortName);

            var referenceDataLibrary = this.hubController.GetDehpOrModelReferenceDataLibrary().clone(false);
            referenceDataLibrary.getUnit().add(newMeasurementUnit);
            return this.TryCreateReferenceDataLibraryThing(newMeasurementUnit, referenceDataLibrary, refMeasurementUnit);
        }
        
        return true;
    }
    
    /**
     * Updates the correct value set depending on the selected @Link
     * 
     * @param parameter the {@linkplain Parameter}
     * @param refValue the {@linkplain Ref} of {@linkplain String} holding the value to assign
     */
    private void UpdateValueSet(Parameter parameter, Ref<String> refValue)
    {
        ParameterValueSet valueSet = null;
        
        if(parameter.getOriginal() != null || !parameter.getValueSet().isEmpty())
        {
            valueSet = (ParameterValueSet) ValueSetUtils.QueryParameterBaseValueSet(parameter, null, null);    
        }
        else
        {
            valueSet = new ParameterValueSet();
            valueSet.setIid(UUID.randomUUID());
            valueSet.setReference(new ValueArray<>(Arrays.asList(""), String.class));
            valueSet.setFormula(new ValueArray<>(Arrays.asList(""), String.class));
            valueSet.setPublished(new ValueArray<>(Arrays.asList(""), String.class));
            valueSet.setComputed(new ValueArray<>(Arrays.asList(""), String.class));
            valueSet.setValueSwitch(ParameterSwitchKind.MANUAL);
            parameter.getValueSet().add(valueSet);
        }

        valueSet.setManual(new ValueArray<>(Arrays.asList(refValue.Get()), String.class));
    }

    /**
     * Tries to add the specified {@linkplain newThing} to the provided {@linkplain ContainerList} and retrieved the new reference from the cache after save
     * 
     * @param <TThing> the type of {@linkplain Thing}
     * @param newThing the new {@linkplain Thing}
     * @param clonedReferenceDataLibrary the cloned {@linkplain ReferenceDataLibrary} where the {@linkplain newThing} is contained
     * @param refThing the {@linkplain Ref} acting as an out parameter here
     * @return a value indicating whether the {@linkplain newThing} has been successfully created and retrieved from the cache
     */
    private <TThing extends Thing> boolean TryCreateReferenceDataLibraryThing(TThing newThing, ReferenceDataLibrary clonedReferenceDataLibrary, Ref<TThing> refThing)
    {
        try
        {
            var transaction = new ThingTransactionImpl(TransactionContextResolver.resolveContext(clonedReferenceDataLibrary), clonedReferenceDataLibrary);
            transaction.createOrUpdate(clonedReferenceDataLibrary);
            transaction.createOrUpdate(newThing);
            
            this.hubController.Write(transaction);
            this.hubController.RefreshReferenceDataLibrary(clonedReferenceDataLibrary);
            
            return this.hubController.TryGetThingFromChainOfRdlBy(x -> x.getIid().compareTo(newThing.getIid()) == 0, refThing);
        }
        catch(Exception exception)
        {
            this.Logger.error(String.format("Could not create the %s because %s", newThing.getClassKind(), exception));
            this.Logger.catching(exception);
            return false;
        }       
    }
}
