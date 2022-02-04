/*
 * ElementDefinitionImpactViewViewModelTestFixture.java
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
package ViewModels;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.eclipse.emf.ecore.EObject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Reactive.ObservableCollection;
import Utils.Ref;
import ViewModels.ObjectBrowser.Rows.IterationRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Iteration;
import io.reactivex.Observable;
import DstController.IDstController;
import HubController.IHubController;

class ElementDefinitionImpactViewViewModelTestFixture
{
    private IDstController dstController;
    private IHubController hubController;
    private ElementDefinitionImpactViewViewModel viewModel;
    private ObservableCollection<MappedElementRowViewModel<? extends Thing, EObject>> elements;
    private Iteration iteration;
    private ObservableCollection<Thing> selectedDstMapResultForTransfer;
    private ObservableCollection<MappedElementRowViewModel<? extends Thing, EObject>> dstMapResult;

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
        
        this.dstMapResult = new ObservableCollection<MappedElementRowViewModel<? extends Thing, EObject>>();
        when(this.dstController.GetDstMapResult()).thenReturn(this.dstMapResult);
        
        this.selectedDstMapResultForTransfer = new ObservableCollection<Thing>();
        when(this.dstController.GetSelectedDstMapResultForTransfer()).thenReturn(this.selectedDstMapResultForTransfer);
        
        this.viewModel = new ElementDefinitionImpactViewViewModel(this.hubController, this.dstController);
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
        
        ElementDefinition elementDefinition0 = new ElementDefinition();
        elementDefinition0.setIid(UUID.randomUUID());
        ElementDefinition elementDefinition1 = new ElementDefinition();
        elementDefinition1.setIid(UUID.randomUUID());
        ElementDefinition elementDefinition2 = new ElementDefinition();
        elementDefinition2.setIid(UUID.randomUUID());
        
        var mappedElement0 = (MappedElementRowViewModel<ElementDefinition, EObject>)mock(MappedElementRowViewModel.class);
        when(mappedElement0.GetHubElement()).thenReturn(elementDefinition0);
        var mappedElement1 = (MappedElementRowViewModel<ElementDefinition, EObject>)mock(MappedElementRowViewModel.class);
        when(mappedElement1.GetHubElement()).thenReturn(elementDefinition1);
        var mappedElement2 = (MappedElementRowViewModel<ElementDefinition, EObject>)mock(MappedElementRowViewModel.class);
        when(mappedElement2.GetHubElement()).thenReturn(elementDefinition2);

        assertEquals(1, this.viewModel.GetBrowserTreeModel().getRowCount());
                
        var elements = new ArrayList<MappedElementRowViewModel<ElementDefinition, EObject>>();
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
