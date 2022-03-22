/*
 * ElementToComponentMappingRule.java
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
package MappingRules;

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.sirius.business.api.session.Session;
import org.polarsys.capella.common.ef.command.AbstractReadWriteCommand;
import org.polarsys.capella.common.helpers.TransactionHelper;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.information.Property;
import org.polarsys.capella.core.data.information.Unit;
import org.polarsys.capella.core.data.information.datatype.DataType;
import org.polarsys.capella.core.data.information.datatype.PhysicalQuantity;
import org.polarsys.capella.core.data.information.datavalue.DataValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralBooleanValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralNumericValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralStringValue;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.data.pa.PhysicalComponentNature;
import org.polarsys.capella.core.data.requirement.RequirementsPkg;

import App.AppContainer;
import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.CapellaSession.ICapellaSessionService;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Utils.Ref;
import Utils.ValueSetUtils;
import Utils.Stereotypes.CapellaComponentCollection;
import Utils.Stereotypes.CapellaTypeEnumerationUtility;
import Utils.Stereotypes.HubElementCollection;
import Utils.Stereotypes.RequirementType;
import Utils.Stereotypes.StereotypeUtils;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.engineeringmodeldata.ElementBase;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ElementUsage;
import cdp4common.engineeringmodeldata.InterfaceEndKind;
import cdp4common.engineeringmodeldata.ParameterOrOverrideBase;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.BooleanParameterType;
import cdp4common.sitedirectorydata.Category;
import cdp4common.sitedirectorydata.MeasurementScale;
import cdp4common.sitedirectorydata.MeasurementUnit;
import cdp4common.sitedirectorydata.QuantityKind;
import cdp4common.sitedirectorydata.TextParameterType;

/**
 * The {@linkplain ElementToComponentMappingRule} is the mapping rule implementation for transforming Capella {@linkplain Component} to {@linkplain ElementDefinition}
 */
public class ElementToComponentMappingRule extends HubToDstBaseMappingRule<HubElementCollection, ArrayList<MappedElementDefinitionRowViewModel>>
{    
    /**
     * The {@linkplain ICapellaSessionService}
     */
    private final ICapellaSessionService sessionService;

    /**
     * The {@linkplain HubElementCollection} of {@linkplain MappedElementDefinitionRowViewModel}
     */
    private HubElementCollection elements;
    
    /**
     * The {@linkplain Collection} of {@linkplain Component} that were created during this mapping
     */
    private Collection<? extends Component> temporaryComponents = new ArrayList<>();

    /**
     * The {@linkplain Collection} of {@linkplain DataType} that were created during this mapping
     */
    private Collection<DataType> temporaryDataTypes = new ArrayList<>();

    /**
     * The {@linkplain Collection} of {@linkplain Unit} that were created during this mapping
     */
    private Collection<Unit> temporaryUnits = new ArrayList<>();
    
    /**
     * An {@linkplain Component} of reference that links the current mapping with the correct {@linkplain Session}.
     * Typically a {@linkplain ProjectElement}
     */
    private Component referenceElement;

    /**
     * Initializes a new {@linkplain ElementToComponentMappingRule}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain ICapellaMappingConfigurationService}
     * @param sessionService the {@linkplain ICapellaSessionService}
     * @param dstController the {@linkplain IDstController}
     */
    public ElementToComponentMappingRule(IHubController hubController, ICapellaMappingConfigurationService mappingConfiguration, ICapellaSessionService sessionService)
    {
        super(hubController, mappingConfiguration);
        this.sessionService = sessionService;
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
            if(this.dstController == null)
            {
                this.dstController = AppContainer.Container.getComponent(IDstController.class);
            }
            
            this.elements = this.CastInput(input);

            this.SetReferenceElement();
            this.Map(this.elements);
            this.SaveMappingConfiguration(this.elements, MappingDirection.FromDstToHub);
            return new ArrayList<>(this.elements);
        }
        catch (Exception exception)
        {
            this.Logger.catching(exception);
            return new ArrayList<>();
        }
        finally
        {
            this.temporaryUnits.clear();
            this.temporaryComponents.clear();
            this.temporaryDataTypes.clear();
        }
    }
    
    /**
     * Sets the {@linkplain CapellaElement} of reference, {@linkplain #referenceElement}
     */
    private void SetReferenceElement()
    {
        if(this.referenceElement != null)
        {
            return;
        }
        
        this.referenceElement = (Component) this.sessionService.GetTopElement(this.sessionService.GetOpenSessions().get(0));
    }

    /**
     * Maps the provided collection of {@linkplain ElementBase}
     * 
     * @param mappedElementDefinitions the collection of {@linkplain Component} to map
     */
    private void Map(HubElementCollection mappedElementDefinitions)
    {        
        for (var mappedElement : new ArrayList<MappedElementDefinitionRowViewModel>(mappedElementDefinitions))
        {
            if(mappedElement.GetDstElement() == null)
            {
                var component = this.GetOrCreateComponent(mappedElement.GetHubElement());
                mappedElement.SetDstElement(component);
            }
            
            this.MapContainedElement(mappedElement);
            this.MapProperties(mappedElement.GetHubElement(), mappedElement.GetDstElement());
        }
    }
    
    /**
     * Updates the containment information of the provided parent and component
     * 
     * @param parent the {@linkplain Component} parent
     * @param component the {@linkplain Component} child
     */
    private void UpdateContainement(Component parent, Component component)
    {
        var project = this.sessionService.GetProject(this.sessionService.GetOpenSessions().get(0));
        
        TransactionHelper.getExecutionManager(project).execute(new AbstractReadWriteCommand() 
        {
            @Override
            public void run()
            {
                try
                {
                    if(parent instanceof PhysicalComponent)
                    {
                        ((PhysicalComponent)parent).getOwnedPhysicalComponents().add((PhysicalComponent)component);
                    }
                    else if(parent instanceof LogicalComponent)
                    {
                        ((LogicalComponent)parent).getOwnedLogicalComponents().add((LogicalComponent)component);
                    }
                }
                catch(IllegalStateException exception)
                {
                    Logger.catching(exception);
                }
            }
        });
    }
    
    /**
     * Maps the properties of the provided {@linkplain ElementDefinition}
     * 
     * @param hubElement the {@linkplain ElementDefinition} from which the properties are to be mapped
     * @param component the target {@linkplain Component}
     */
    private void MapProperties(ElementDefinition hubElement, Component component)
    {
        this.MapProperties(hubElement.getParameter(), component);
    }

    /**
     * Maps the properties of the provided {@linkplain ElementUsage}
     * 
     * @param hubElement the {@linkplain ElementUsage} from which the properties are to be mapped
     * @param component the target {@linkplain Component}
     */
    private void MapProperties(ElementUsage hubElement, Component component)
    {
        var allParametersAndOverrides = hubElement.getElementDefinition().getParameter().stream()
                .filter(x -> hubElement.getParameterOverride().stream()
                        .noneMatch(o -> AreTheseEquals(x.getParameterType().getIid(), o.getParameterType().getIid())))
                .collect(Collectors.toList());
        
        this.MapProperties(allParametersAndOverrides, component);
    }
    
    /**
     * Maps the properties of the provided {@linkplain Collection} of {@linkplain ParameterOrOverrideBase}
     * 
     * @param parameters the {@linkplain Collection} of {@linkplain ParameterOrOverrideBase} to map
     * @param component the target {@linkplain Component}
     */
    private void MapProperties(Collection<? extends ParameterOrOverrideBase> parameters, Component component)
    {
        for (var parameter : parameters)
        {
            var refScale = new Ref<>(DataType.class);
            var refProperty = new Ref<Property>(Property.class);
                        
            if(!TryGetExistingProperty(component, parameter, refProperty))
            {
                if(parameter.getScale() != null)
                {
                    this.GetOrCreateDataType(parameter.getScale(), component, refScale);
                }
                
                this.CreateProperty(parameter, refProperty, refScale);
                component.getOwnedFeatures().add(refProperty.Get());
            }
            else if(refProperty.Get().getType() instanceof PhysicalQuantity)
            {
                refScale.Set((PhysicalQuantity)refProperty.Get().getType());
            }
            
            this.UpdateValue(parameter, refProperty, refScale);
        }
    }

    /**
     * Get or creates the {@linkplain DataType} that matches the provided {@linkplain MeasurementScale}
     * 
     * @param scale the {@linkplain MeasurementScale} to map
     * @param component the target {@linkplain Component}
     * @param refScale the {@linkplain Ref} of {@linkplain DataType} that will contains the output {@linkplain DataType}
     */
    private void GetOrCreateDataType(MeasurementScale scale, Component component, Ref<DataType> refScale)
    {
        this.QueryCollectionByNameAndShortName(scale, this.temporaryDataTypes, refScale);
        
        if(!refScale.HasValue() && !this.dstController.TryGetDataType(scale, this.referenceElement, refScale))
        {
            var newDataType = StereotypeUtils.InitializeCapellaElement(PhysicalQuantity.class);
            newDataType.setName(scale.getName());
                        
            if(scale.getUnit() != null)
            {
                newDataType.setUnit(this.GetOrCreateUnit(scale.getUnit()));
            }
            
            this.temporaryDataTypes.add(newDataType);
        }
    }

    /**
     * Gets or creates the {@linkplain Unit} that matches the provided {@linkplain MeasurementUnit}
     * 
     * @param unit the {@linkplain MeasurementUnit} 
     * @return a matching {@linkplain Unit}
     */
    private Unit GetOrCreateUnit(MeasurementUnit unit)
    {
        var refUnit = new Ref<>(Unit.class);        

        this.QueryCollectionByNameAndShortName(unit, this.temporaryUnits, refUnit);
        
        if(!refUnit.HasValue() && !this.dstController.TryGetElementByName(unit, refUnit))
        {
            var newUnit = StereotypeUtils.InitializeCapellaElement(Unit.class);
            newUnit.setName(unit.getName());
            refUnit.Set(newUnit);
            this.temporaryUnits.add(newUnit);
        }

        return refUnit.Get();
    }

    /**
     * Updates the value of the provided {@linkplain Property}
     * 
     * @param parameter the {@linkplain ParameterOrOverrideBase} that contains the values to transfer
     * @param refProperty the {@linkplain Ref} of {@linkplain Property}
     * @param refScale the {@linkplain Ref} of {@linkplain DataType}
     */
    private void UpdateValue(ParameterOrOverrideBase parameter, Ref<Property> refProperty, Ref<DataType> refScale)
    {
        DataValue dataValue;
        
        if (refProperty.Get().getOwnedDefaultValue() != null)
        {
            dataValue = refProperty.Get().getOwnedDefaultValue();
        }
        else
        {
            dataValue = this.CreateDataValue(parameter, refScale);
            refProperty.Get().setOwnedDefaultValue(dataValue);
        }

        this.UpdateValue(dataValue, parameter, refProperty);
    }
    
    /**
     * Updates the value of the provided {@linkplain Property}
     * 
     * @param dataValue the {@linkplain DataValue}
     * @param parameter the {@linkplain ParameterOrOverrideBase} that contains the values to transfer
     * @param refProperty the {@linkplain Ref} of {@linkplain Property} 
     */
    private void UpdateValue(DataValue dataValue, ParameterOrOverrideBase parameter, Ref<Property> refProperty)
    {
        var value = ValueSetUtils.QueryParameterBaseValueSet(parameter, null, null);

        if(dataValue instanceof LiteralNumericValue)
        {
            ((LiteralNumericValue)dataValue).setValue(value.getActualValue().get(0));
        }
        else if(dataValue instanceof LiteralBooleanValue)
        {
            ((LiteralBooleanValue)dataValue).setValue(Boolean.valueOf(value.getActualValue().get(0)));
        }
        else if(dataValue instanceof LiteralStringValue)
        {
            ((LiteralStringValue)dataValue).setValue(value.getActualValue().get(0));
        }
    }

    /**
     * Gets the {@linkplain DataValue} class type
     * 
     * @param parameter the {@linkplain parameter}
     * @return a {@linkplain Class} of {@linkplain DataValue}
     */
    private Class<? extends DataValue> GetDataValueType(ParameterOrOverrideBase parameter)
    {
        if(parameter.getParameterType() instanceof QuantityKind)
        {
            return LiteralNumericValue.class;
        }
        if(parameter.getParameterType() instanceof BooleanParameterType)
        {
            return LiteralBooleanValue.class;
        }
        if(parameter.getParameterType() instanceof TextParameterType)
        {
            return LiteralStringValue.class;
        }
        
        return null;
    }
    
    /**
     * Creates the {@linkplain DataValue} based on the provided {@linkplain DataType}
     * 
     * @return a {@linkplain DataValue}
     */
    private DataValue CreateDataValue(ParameterOrOverrideBase parameter, Ref<DataType> dstDataType)
    {
        var valueType = this.GetDataValueType(parameter);
        
        DataValue dataValue = null;
        
        if(valueType != null)
        {
            dataValue = StereotypeUtils.InitializeCapellaElement(valueType);
            
            if(dataValue instanceof LiteralNumericValue && dstDataType.HasValue() && dstDataType.Get() instanceof PhysicalQuantity)
            {
                ((LiteralNumericValue)dataValue).setUnit(((PhysicalQuantity)dstDataType.Get()).getUnit());
            }
        }
        
        return dataValue;
    }

    /**
     * Creates a {@linkplain Property} based on the provided {@linkplain ParameterOrOverrideBase}
     * 
     * @param parameter the {@linkplain ParameterOrOverrideBase}
     * @param refProperty the {@linkplain Ref} {@linkplain Property}
     * @param dstDataType the {@linkplain Ref} of {@linkplain DataType}
     */
    private void CreateProperty(ParameterOrOverrideBase parameter, Ref<Property> refProperty, Ref<DataType> dstDataType)
    {
        var newProperty = StereotypeUtils.InitializeCapellaElement(Property.class);
        newProperty.setName(parameter.getParameterType().getName());
        
        if(parameter.getParameterType() instanceof QuantityKind && dstDataType.HasValue())
        {
            newProperty.setAbstractType(dstDataType.Get());
        }
        
        var minimumCardinality = StereotypeUtils.InitializeCapellaElement(LiteralNumericValue.class);
        minimumCardinality.setValue(Integer.toString(1));
        newProperty.setOwnedMinCard(minimumCardinality);
        
        var maximumCardinality = StereotypeUtils.InitializeCapellaElement(LiteralNumericValue.class);
        maximumCardinality.setValue(Integer.toString(1));
        newProperty.setOwnedMaxCard(maximumCardinality);
        
        refProperty.Set(newProperty);
    }

    /**
     * Tries to get an existing {@linkplain Property} that matches the provided {@linkplain ParameterOrOverrideBase}
     * 
     * @param dstElement the {@linkplain Component}
     * @param parameter the {@linkplain ParameterOrOverrideBase} 
     * @param refProperty the {@linkplain Ref} of {@linkplain Property}
     * @return a value indicating whether the {@linkplain Property} could be found
     */
    private boolean TryGetExistingProperty(Component dstElement, ParameterOrOverrideBase parameter, Ref<Property> refProperty)
    {
        var optionalProperty = dstElement.getContainedProperties().stream()
                .filter(x -> AreTheseEquals(x.getName(), parameter.getParameterType().getName(), true))
                .findFirst();
        
        if(optionalProperty.isPresent())
        {
            refProperty.Set(optionalProperty.get());
        }
        
        return refProperty.HasValue();
    }

    /**
     * Maps the contained element of the provided {@linkplain MappedElementDefinitionRowViewModel} dst element
     * 
     * @param mappedElement the {@linkplain MappedElementDefinitionRowViewModel}
     */
    private void MapContainedElement(MappedElementDefinitionRowViewModel mappedElement)
    {
        for (var containedUsage : mappedElement.GetHubElement().getContainedElement().stream()
                .filter(x -> x.getInterfaceEnd() == InterfaceEndKind.NONE).collect(Collectors.toList()))
        {
            MappedElementDefinitionRowViewModel usageDefinitionMappedElement = this.elements.stream()
                    .filter(x -> AreTheseEquals(x.GetDstElement().getName(), containedUsage.getName(), true))
                    .findFirst()
                    .orElseGet(() -> 
                    {
                        var newMappedElement = new MappedElementDefinitionRowViewModel(containedUsage.getElementDefinition(),
                                this.GetOrCreateComponent(containedUsage.getElementDefinition()), MappingDirection.FromHubToDst);
                        
                        this.elements.add(newMappedElement);
                        this.UpdateContainement(mappedElement.GetDstElement(), newMappedElement.GetDstElement());
                        return newMappedElement;
                    });
            
            this.MapProperties(containedUsage, usageDefinitionMappedElement.GetDstElement());

            this.MapContainedElement(usageDefinitionMappedElement);
        }        
    }

    /**
     * Gets or creates a component based on an {@linkplain ElementDefinition}
     * 
     * @param elementDefinition the {@linkplain ElementDefinition}
     * @return an existing or a new {@linkplain Component}
     */
    private Component GetOrCreateComponent(ElementDefinition elementDefinition)
    {
        @SuppressWarnings("unchecked")
        var refComponentType = new Ref<>((Class<Class<? extends Component>>) Component.class.getClass());

        if(!this.TryGetComponentClass(elementDefinition, refComponentType))
        {
            refComponentType.Set(PhysicalComponent.class);
        }
           
        return this.GetOrCreateComponent(elementDefinition.getName(), refComponentType.Get());
    }
    
    /**
     * Gets the {@linkplain RequirementType} based on the {@linkplain Category} applied to the provided {@linkplain cdp4common.engineeringmodeldata.Requirement}
     * 
     * @param elementDefinition the {@linkplain ElementDefinition}
     * @param refComponentType the {@linkplain Ref} of {@linkplain RequirementType}
     * @return a {@linkplain boolean} indicating whether the {@linkplain RequirementType} is different than the default value
     */
    private boolean TryGetComponentClass(ElementDefinition elementDefinition, Ref<Class<? extends Component>>  refComponentType)
    {
        for (var category : elementDefinition.getCategory())
        {
            var componentType = CapellaTypeEnumerationUtility.ComponentTypeFrom(category.getName());

            if(componentType != null)
            {
                refComponentType.Set((Class<? extends Component>) componentType.ClassType());
                break;
            }
        }
           
        return refComponentType.HasValue();
    }

    /**
     * Gets or creates the {@linkplain RequirementsPkg} that can represent the {@linkplain RequirementsSpecification}
     * 
     * @param <TComponent> the type of {@linkplain Component}
     * @param hubElementName the {@linkplain String} element name in the HUB side
     * @param componentType the {@linkplain Class} of {@linkplain #TComponent}
     * @return a {@linkplain RequirementsPkg}
     */
    @SuppressWarnings("unchecked")
    private <TComponent extends Component> TComponent GetOrCreateComponent(String hubElementName, Class<TComponent> componentType)
    {
        var refElement = new Ref<>(componentType);
        
        var existingComponent = this.temporaryComponents.stream()
                .filter(x -> AreTheseEquals(((NamedElement) x).getName(), hubElementName, true))
                .findFirst();
        
        if(existingComponent.isPresent())
        {
            refElement.Set((TComponent) existingComponent.get());
        }
        else
        {
            if(!this.dstController.TryGetElementBy(x -> x instanceof NamedElement && 
                    AreTheseEquals(((NamedElement) x).getName(), hubElementName, true), refElement))
            {
                var newComponent = StereotypeUtils.InitializeCapellaElement(componentType);
                
                if(newComponent instanceof PhysicalComponent)
                {
                    ((PhysicalComponent)newComponent).setNature(PhysicalComponentNature.BEHAVIOR);
                }
                
                newComponent.setName(hubElementName);
                refElement.Set(newComponent);
            }
        }
        
        return refElement.Get();
    }
    
    /**
     * Searches for the provided {@linkplain Collection} for an {@linkplain #TElement} 
     * where the name could match either the name or short name of the provided {@linkplain DefinedThing}.
     * If found, it assigns the value to the provided {@linkplain Ref}
     * 
     * @param <TElement> the type of {@linkplain NamedElement} to get
     * @param definedThing the {@linkplain DefinedThing}
     * @param collection the {@linkplain Collection} of {@linkplain #TElement} to query
     * @param refElement the {@linkplain Ref} of {@linkplain #TElement}
     */
    private <TElement extends NamedElement> void QueryCollectionByNameAndShortName(DefinedThing definedThing, 
            Collection<? extends TElement> collection, Ref<TElement> refElement)
    {
        collection.stream()
                .filter(x -> AreTheseEquals(x.getName(), definedThing.getName(), true) 
                        || AreTheseEquals(x.getName(), definedThing.getShortName(), true))
                .findAny()
                .ifPresent(x -> refElement.Set(x));
    }
}
