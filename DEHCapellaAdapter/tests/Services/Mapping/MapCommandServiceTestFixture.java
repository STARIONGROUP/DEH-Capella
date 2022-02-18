/*
 * MapCommandServiceTestFixture.java
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
package Services.Mapping;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.data.requirement.Requirement;

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Reactive.ObservableCollection;
import Reactive.ObservableValue;
import Services.CapellaLog.ICapellaLogService;
import Services.CapellaSelection.ICapellaSelectionService;
import Services.CapellaSession.ICapellaSessionListenerService;
import Services.NavigationService.INavigationService;
import Utils.Ref;
import ViewModels.Dialogs.Interfaces.IDstMappingConfigurationDialogViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedRequirementRowViewModel;
import Views.Dialogs.DstMappingConfigurationDialog;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.RequirementsSpecification;

class MapCommandServiceTestFixture
{
    private ICapellaSelectionService selectionService;
    private IDstController dstController;
    private INavigationService navigationService;
    private IDstMappingConfigurationDialogViewModel dstMappingDialog;
    private ICapellaLogService logService;
    private IHubController hubController;
    private MapCommandService service;
    private ObservableValue<Boolean> hasAnyOpenSession;
    private ObservableValue<Boolean> isSessionOpen;

    @BeforeEach
    public void setUp() throws Exception
    {
        this.selectionService = mock(ICapellaSelectionService.class);
        this.dstController = mock(IDstController.class);
        this.navigationService = mock(INavigationService.class);
        this.dstMappingDialog = mock(IDstMappingConfigurationDialogViewModel.class);
        this.logService = mock(ICapellaLogService.class);
        this.hubController = mock(IHubController.class);
        
        this.hasAnyOpenSession = new ObservableValue<Boolean>();
        this.isSessionOpen = new ObservableValue<Boolean>();
        when(this.dstController.HasAnyOpenSessionObservable()).thenReturn(this.hasAnyOpenSession.Observable());
        when(this.hubController.GetIsSessionOpenObservable()).thenReturn(this.isSessionOpen.Observable());
                
        this.service = new MapCommandService(this.selectionService, this.dstController, 
                this.navigationService, this.dstMappingDialog, this.logService, this.hubController);
    }

    @Test
    public void VerifyCanExecute()
    {
        var results = new ArrayList<Boolean>();
        this.service.CanExecuteObservable().subscribe(x -> results.add(x));
        this.isSessionOpen.Value(true);
        this.hasAnyOpenSession.Value(true);
        this.isSessionOpen.Value(false);
        this.hasAnyOpenSession.Value(true);
        this.hasAnyOpenSession.Value(false);
        assertEquals(4, results.size());
        assertEquals(true, results.get(0));
        assertEquals(false, results.get(3));
    }
    
    @Test
    public void VerifyMapSelectionFromDstToHub() throws InterruptedException
    {
        when(this.selectionService.GetSelection()).thenReturn(new ArrayList<>());
        when(this.navigationService.ShowDialog(any(), any())).thenReturn(null);
        assertDoesNotThrow(() -> this.service.MapSelection(MappingDirection.FromHubToDst));
        when(this.navigationService.ShowDialog(any(), any())).thenReturn(false);
        assertDoesNotThrow(() -> this.service.MapSelection(MappingDirection.FromDstToHub));
        when(this.navigationService.ShowDialog(any(), any())).thenReturn(true);
        assertDoesNotThrow(() -> this.service.MapSelection(MappingDirection.FromDstToHub));
        var dialogResult = new Ref<Boolean>(Boolean.class, null);
        
        var mappedElementResultFromTheMappingDialog = new ObservableCollection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>>();
        when(this.dstMappingDialog.GetMappedElementCollection()).thenReturn(mappedElementResultFromTheMappingDialog);
        assertDoesNotThrow(() -> this.service.WhenDialogHasBeenClosed(dialogResult));
        dialogResult.Set(false);
        assertDoesNotThrow(() -> this.service.WhenDialogHasBeenClosed(dialogResult));
        dialogResult.Set(true);
        assertDoesNotThrow(() -> this.service.WhenDialogHasBeenClosed(dialogResult));
        var mappedElement0 = new MappedElementDefinitionRowViewModel(new ElementDefinition(), mock(PhysicalComponent.class), MappingDirection.FromDstToHub);
        var mappedElement1 = new MappedRequirementRowViewModel(new RequirementsSpecification(), mock(Requirement.class), MappingDirection.FromDstToHub);
        mappedElementResultFromTheMappingDialog.add(mappedElement0);
        mappedElementResultFromTheMappingDialog.add(mappedElement1);
                
        when(this.dstController.Map(any(), any())).thenReturn(false);

        assertDoesNotThrow(() -> this.service.WhenDialogHasBeenClosed(dialogResult));
        when(this.dstController.Map(any(), any())).thenReturn(true);
        assertDoesNotThrow(() -> this.service.WhenDialogHasBeenClosed(dialogResult));
        Thread.sleep(100);
        verify(this.logService, times(3)).Append(any(String.class), any(Boolean.class));
        verify(this.logService, times(4)).Append(any(String.class), any(Integer.class));
        verify(this.dstController, times(4)).Map(any(), any());
    } 
}
