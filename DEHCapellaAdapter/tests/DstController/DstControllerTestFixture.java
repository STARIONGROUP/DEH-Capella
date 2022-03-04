/*
 * DstControllerTestFixture.java
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
package DstController;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.pde.internal.core.project.RequirementSpecification;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.verification.Times;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.requirement.SystemUserRequirement;

import com.google.common.collect.ImmutableList;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.CapellaLog.ICapellaLogService;
import Services.CapellaSession.ICapellaSessionService;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Services.MappingConfiguration.IMappingConfigurationService;
import Services.MappingEngineService.IMappableThingCollection;
import Services.MappingEngineService.IMappingEngineService;
import Utils.Ref;
import ViewModels.Interfaces.IMappedElementRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedRequirementRowViewModel;
import cdp4common.commondata.ClassKind;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ElementUsage;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.Parameter;
import cdp4common.engineeringmodeldata.ParameterOverride;
import cdp4common.engineeringmodeldata.ParameterOverrideValueSet;
import cdp4common.engineeringmodeldata.ParameterValueSet;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.types.ValueArray;
import cdp4dal.Session;
import cdp4dal.exceptions.TransactionException;
import cdp4dal.operations.ThingTransaction;
import io.reactivex.Observable;

public class DstControllerTestFixture
{
    private IMappingEngineService mappingEngine;
    private DstController controller;
    private IHubController hubController;
    private ICapellaLogService logService;
    private ICapellaMappingConfigurationService mappingConfigurationService;
    private ICapellaSessionService capellaSessionService;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        this.mappingEngine = mock(IMappingEngineService.class);
        this.hubController = mock(IHubController.class);
        this.logService = mock(ICapellaLogService.class);
        this.mappingConfigurationService = mock(ICapellaMappingConfigurationService.class);
        this.capellaSessionService = mock(ICapellaSessionService.class);
        
        when(this.capellaSessionService.SessionUpdated())
            .thenReturn(Observable.fromArray(mock(org.eclipse.sirius.business.api.session.Session.class)));
        
        when(this.hubController.GetIsSessionOpenObservable()).thenReturn(Observable.fromArray(false, true));
        
        when(this.hubController.GetSessionEventObservable()).thenReturn(Observable.fromArray(true));
        
        var mappedThings0 = mock(MappedElementRowViewModel.class);
        var mappedThings1 = mock(MappedElementRowViewModel.class);
        
        ElementDefinition elementDefinition = new ElementDefinition();
        when(mappedThings0.GetHubElement()).thenReturn(elementDefinition);
        
        RequirementsSpecification requirementsSpecification = new RequirementsSpecification();
        when(mappedThings1.GetHubElement()).thenReturn(requirementsSpecification);
        
        this.controller = new DstController(this.mappingEngine, this.hubController, this.logService, 
                this.mappingConfigurationService, this.capellaSessionService);
        
        this.controller.GetDstMapResult().add(mappedThings0);
        this.controller.GetDstMapResult().add(mappedThings1);
    }

    @Test
    public void VerifyLoadMapping()
    {
        assertDoesNotThrow(() -> this.controller.LoadMapping());
        
        var loadedMapping = new ArrayList<IMappedElementRowViewModel>();
        
        loadedMapping.addAll(
                Arrays.asList(
                    (MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>)
                    new MappedElementDefinitionRowViewModel(new ElementDefinition(), mock(LogicalComponent.class), MappingDirection.FromDstToHub),
                    (MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>)
                    new MappedElementDefinitionRowViewModel(new ElementDefinition(), mock(LogicalComponent.class), MappingDirection.FromHubToDst),
                    (MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>)
                    new MappedRequirementRowViewModel(new RequirementsSpecification(), mock(SystemUserRequirement.class), MappingDirection.FromDstToHub),
                    (MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>)
                    new MappedRequirementRowViewModel(new RequirementsSpecification(), mock(SystemUserRequirement.class), MappingDirection.FromHubToDst)
                    ));
        
        when(this.mappingConfigurationService.LoadMapping()).thenReturn(loadedMapping);
        when(this.mappingEngine.Map(any())).thenReturn(loadedMapping);
        assertDoesNotThrow(() -> this.controller.LoadMapping());
        
        verify(this.mappingConfigurationService, times(4)).LoadMapping();
    }
    
    @Test
    public void VerifyMap()
    {
        assertFalse(this.controller.Map(mock(IMappableThingCollection.class), MappingDirection.FromDstToHub));
        var mapResult = new ArrayList<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>>();
        
        mapResult.addAll(
            Arrays.asList(
                (MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>)
                new MappedElementDefinitionRowViewModel(new ElementDefinition(), mock(LogicalComponent.class), MappingDirection.FromDstToHub),
                (MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>)
                new MappedElementDefinitionRowViewModel(new ElementDefinition(), mock(LogicalComponent.class), MappingDirection.FromHubToDst),
                (MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>)
                new MappedRequirementRowViewModel(new RequirementsSpecification(), mock(SystemUserRequirement.class), MappingDirection.FromDstToHub),
                (MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>)
                new MappedRequirementRowViewModel(new RequirementsSpecification(), mock(SystemUserRequirement.class), MappingDirection.FromHubToDst)
                ));
        
        when(this.mappingEngine.Map(any())).thenReturn(mapResult);
        assertTrue(this.controller.Map(mock(IMappableThingCollection.class), MappingDirection.FromDstToHub));
        assertTrue(this.controller.Map(mock(IMappableThingCollection.class), MappingDirection.FromHubToDst));
        assertTrue(this.controller.Map(mock(IMappableThingCollection.class), null));
    }
    
    @Test
    public void VerifyTransfer() throws TransactionException
    {
        var requirementSpecification = new RequirementsSpecification();
        requirementSpecification.getGroup().add(new RequirementsGroup());
        requirementSpecification.getRequirement().add(new Requirement());
        requirementSpecification.setIid(UUID.randomUUID());
        
        var elementDefinition = new ElementDefinition();
        elementDefinition.setIid(UUID.randomUUID());
        var elementUsage = new ElementUsage();
        elementUsage.setIid(UUID.randomUUID());
        elementUsage.setElementDefinition(elementDefinition);
        elementDefinition.getContainedElement().add(elementUsage);
        
        var parameter = new Parameter();
        var parameterValueSet = new ParameterValueSet();
        parameterValueSet.setManual(new ValueArray(Arrays.asList("-"), String.class));
        parameterValueSet.setReference(new ValueArray(Arrays.asList("-"), String.class));
        parameterValueSet.setComputed(new ValueArray(Arrays.asList("-"), String.class));
        parameter.getValueSet().add(parameterValueSet);
        elementDefinition.getParameter().add(parameter.clone(true));
        
        var parameterOverride = new ParameterOverride().clone(false);
        parameterOverride.setParameter(parameter);
        var parameterOverrideValueSet = new ParameterOverrideValueSet();
        parameterOverrideValueSet.setParameterValueSet(parameterValueSet);
        parameterOverrideValueSet.setManual(new ValueArray(Arrays.asList("-"), String.class));
        parameterOverrideValueSet.setReference(new ValueArray(Arrays.asList("-"), String.class));
        parameterOverrideValueSet.setComputed(new ValueArray(Arrays.asList("-"), String.class));
        parameterOverride.getValueSet().add(parameterOverrideValueSet);
        elementUsage.getParameterOverride().add(parameterOverride.clone(true));
                
        when(this.hubController.TryGetThingById(any(), any())).thenAnswer(
                new Answer()
                {
                    @Override
                    public Object answer(InvocationOnMock arg0) throws Throwable
                    {
                        var refParameter = (Ref<?>)arg0.getArguments()[1];
                        
                        if(Parameter.class.isAssignableFrom(refParameter.GetType()))
                        {
                            ((Ref<Parameter>)refParameter).Set(parameter);
                        } 
                        else if(ParameterOverride.class.isAssignableFrom(refParameter.GetType()))
                        {
                            ((Ref<ParameterOverride>)refParameter).Set(parameterOverride);
                        }
                        else if(ElementDefinition.class.isAssignableFrom(refParameter.GetType()))
                        {
                            ((Ref<ElementDefinition>)refParameter).Set(elementDefinition);
                        }
                        
                        return true;
                    }
                });
        
        this.controller.GetSelectedDstMapResultForTransfer().add(requirementSpecification);
        this.controller.GetSelectedDstMapResultForTransfer().add(elementDefinition);
        var transaction = mock(ThingTransaction.class);
        when(transaction.getAddedThing()).thenReturn(ImmutableList.of((Thing)new ElementDefinition()));
        when(this.hubController.GetIterationTransaction()).thenReturn(Pair.of(new Iteration(), transaction));
        this.controller.ChangeMappingDirection();
        assertTrue(this.controller.Transfer());
        this.controller.ChangeMappingDirection();
        assertFalse(this.controller.Transfer());
        when(this.hubController.Refresh()).thenReturn(true);
        this.controller.GetSelectedDstMapResultForTransfer().add(requirementSpecification);
        this.controller.GetSelectedDstMapResultForTransfer().add(elementDefinition);
        assertTrue(this.controller.Transfer());
        verify(this.hubController, times(5)).Refresh();
    }
    
    @Test
    public void VerifyUpdateParameterValueSets() throws TransactionException
    {
        when(this.hubController.GetIterationTransaction()).thenReturn(Pair.of(new Iteration(), mock(ThingTransaction.class)));
        assertDoesNotThrow(() -> this.controller.UpdateParameterValueSets());
        verify(this.hubController, times(1)).Write(any(ThingTransaction.class));
    }
    
    @Test
    public void AddOrRemoveAllFromSelectedThingsToTransfer()
    {
        assertEquals(0, this.controller.GetSelectedDstMapResultForTransfer().size());
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind.ElementDefinition, false));
        assertEquals(1, this.controller.GetSelectedDstMapResultForTransfer().size());
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind.RequirementsSpecification, false));
        assertEquals(2, this.controller.GetSelectedDstMapResultForTransfer().size());
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind.RequirementsSpecification, true));
        assertEquals(1, this.controller.GetSelectedDstMapResultForTransfer().size());
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind.ElementDefinition, true));
        assertEquals(0, this.controller.GetSelectedDstMapResultForTransfer().size());
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind.ElementUsage, false));
        assertEquals(0, this.controller.GetSelectedDstMapResultForTransfer().size());
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(null, false));
        assertEquals(0, this.controller.GetSelectedHubMapResultForTransfer().size());
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(null, true));
        assertEquals(0, this.controller.GetSelectedHubMapResultForTransfer().size());
    }
}
