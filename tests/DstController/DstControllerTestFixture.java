/*
 * DstControllerTestFixture.java
 *
 * Copyright (c) 2020-2021 RHEA System S.A.
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

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.emf.ecore.EObject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.verification.Times;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.CapellaLog.ICapellaLogService;
import Services.CapellaSession.ICapellaSessionService;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Services.MappingConfiguration.IMappingConfigurationService;
import Services.MappingEngineService.IMappableThingCollection;
import Services.MappingEngineService.IMappingEngineService;
import ViewModels.Interfaces.IMappedElementRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.ClassKind;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4dal.exceptions.TransactionException;
import cdp4dal.operations.ThingTransaction;

class DstControllerTestFixture
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

        var mappedThings0 = (MappedElementRowViewModel<ElementDefinition, EObject>) mock(MappedElementRowViewModel.class);
        var mappedThings1 = (MappedElementRowViewModel<RequirementsSpecification, EObject>) mock(MappedElementRowViewModel.class);
        
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
        verify(this.mappingConfigurationService, times(1)).LoadMapping(any());
    }
    
    @Test
    public void VerifyMap()
    {
        assertTrue(this.controller.Map(mock(IMappableThingCollection.class), MappingDirection.FromDstToHub));
    }
    
    @Test
    public void VerifyTransfer() throws TransactionException
    {
        when(this.hubController.GetIterationTransaction()).thenReturn(Pair.of(new Iteration(), mock(ThingTransaction.class)));
        this.controller.ChangeMappingDirection();
        assertTrue(this.controller.Transfer());
        this.controller.ChangeMappingDirection();
        assertFalse(this.controller.Transfer());
        when(this.hubController.Refresh()).thenReturn(true);
        assertTrue(this.controller.Transfer());
        verify(this.hubController, times(3)).Refresh();
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
