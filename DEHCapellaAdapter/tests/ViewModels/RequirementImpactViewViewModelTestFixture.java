/*
 * RequirementImpactViewViewModelTestFixture.java
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
package ViewModels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.polarsys.capella.core.data.capellacore.CapellaElement;

import DstController.IDstController;
import HubController.IHubController;
import Reactive.ObservableCollection;
import Utils.Ref;
import ViewModels.ObjectBrowser.Rows.IterationRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.engineeringmodeldata.Iteration;
import io.reactivex.Observable;

class RequirementImpactViewViewModelTestFixture
{  
    private IDstController dstController;
    private IHubController hubController;
    private RequirementImpactViewViewModel viewModel;
    private ObservableCollection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> elements;
    private Iteration iteration;
    private ObservableCollection<Thing> selectedDstMapResultForTransfer;
    private ObservableCollection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> dstMapResult;

    @BeforeEach
    public void setUp() throws Exception
    {
        this.hubController = mock(IHubController.class);
        this.dstController = mock(IDstController.class);
        
        this.SetupMockedHubModel();
        
        when(this.hubController.GetIsSessionOpenObservable()).thenReturn(Observable.fromArray(true, true));
        when(this.hubController.GetSessionEventObservable()).thenReturn(Observable.fromArray(false, false));
        when(this.hubController.GetIsSessionOpen()).thenReturn(true);
        when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
        
        this.dstMapResult = new ObservableCollection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>>();
        when(this.dstController.GetDstMapResult()).thenReturn(this.dstMapResult);
        
        this.selectedDstMapResultForTransfer = new ObservableCollection<Thing>();
        when(this.dstController.GetSelectedDstMapResultForTransfer()).thenReturn(this.selectedDstMapResultForTransfer);
        
        this.viewModel = new RequirementImpactViewViewModel(this.hubController, this.dstController);
    }
    
    private void SetupMockedHubModel()
    {
        this.iteration = new Iteration();
    }
    
    @Test
    public void VerifyComputeDifferences() throws Exception
    {
        var timesTheBrowserTreeModelHasBeenUpdated = new Ref<Integer>(Integer.class, 0);
        
        this.viewModel.BrowserTreeModel.Observable().subscribe(x -> 
            timesTheBrowserTreeModelHasBeenUpdated.Set(timesTheBrowserTreeModelHasBeenUpdated.Get() + 1));
        
        RequirementsSpecification RequirementsSpecification0 = new RequirementsSpecification();
        RequirementsSpecification0.setIid(UUID.randomUUID());
        RequirementsSpecification RequirementsSpecification1 = new RequirementsSpecification();
        RequirementsSpecification1.setIid(UUID.randomUUID());
        RequirementsSpecification RequirementsSpecification2 = new RequirementsSpecification();
        RequirementsSpecification2.setIid(UUID.randomUUID());
        
        var mappedElement0 = (MappedElementRowViewModel<RequirementsSpecification, ? extends CapellaElement>)mock(MappedElementRowViewModel.class);
        when(mappedElement0.GetHubElement()).thenReturn(RequirementsSpecification0);
        var mappedElement1 = (MappedElementRowViewModel<RequirementsSpecification, ? extends CapellaElement>)mock(MappedElementRowViewModel.class);
        when(mappedElement1.GetHubElement()).thenReturn(RequirementsSpecification1);
        var mappedElement2 = (MappedElementRowViewModel<RequirementsSpecification, ? extends CapellaElement>)mock(MappedElementRowViewModel.class);
        when(mappedElement2.GetHubElement()).thenReturn(RequirementsSpecification2);
    
        assertEquals(1, this.viewModel.GetBrowserTreeModel().getRowCount());
                
        var elements = new ArrayList<MappedElementRowViewModel<RequirementsSpecification, ? extends CapellaElement>>();
        elements.add(mappedElement0);
        elements.add(mappedElement1);
        elements.add(mappedElement2);
        
        this.dstMapResult.addAll(elements);
        Callable<Integer> treeRows = () -> ((IterationRowViewModel)(this.viewModel.GetBrowserTreeModel().getRoot()))
                                            .GetContainedRows().size();
        assertEquals(0, treeRows.call());
        this.dstMapResult.clear();
        assertEquals(0, treeRows.call());
        
        assertEquals(1, timesTheBrowserTreeModelHasBeenUpdated.Get());
    }
}
