/*
 * IMappingConfigurationDialogViewModel.java
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
package ViewModels.Dialogs.Interfaces;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.polarsys.capella.core.data.capellacore.CapellaElement;

import Reactive.ObservableCollection;
import ViewModels.CapellaObjectBrowser.Interfaces.ICapellaObjectBrowserViewModel;
import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import ViewModels.Interfaces.IViewModel;
import ViewModels.MappedElementListView.Interfaces.IMappedElementListViewViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.Thing;
import io.reactivex.Observable;

/**
 * The IMappingConfigurationDialogViewModel is the base interface for the {@linkplain IDstToHubMappingConfigurationDialogViewModel} and the {@linkplain IHubToDstMappingConfigurationDialogViewModel}
 * 
 * @param <TElement> the type of element {@linkplain #SetMappedElement(Collection)} has to deal with
 */
public interface IMappingConfigurationDialogViewModel<TElement> extends IViewModel
{
    /**
     * Sets the mappedElement picked to open this dialog and sets the DST tree
     * 
     * @param selectedElement the collection of {@linkplain #TElement}
     */
    void SetMappedElement(Collection<TElement> selectedElement);
    
    /**
     * Occurs when the user sets the target element of the current mapped element to be a
     * 
     * @param selected the new {@linkplain boolean} value
     */
    void WhenMapToNewElementCheckBoxChanged(boolean selected);

    /**
     * Resets the pre-mapped things to the default way 
     */
    void ResetPreMappedThings();

    /**
     * Gets an {@linkplain Observable} value indicating whether the mapToNewHubElementCheckBox should be enabled 
     * 
     * @return an {@linkplain Observable} of {@linkplain Boolean}
     */
    Observable<Boolean> GetShouldMapToNewElementCheckBoxBeEnabled();

    /**
     * Gets the {@linkplain IMagicDrawObjectBrowserViewModel}
     * 
     * @return an {@linkplain IMagicDrawObjectBrowserViewModel}
     */
    ICapellaObjectBrowserViewModel GetCapellaObjectBrowserViewModel();

    /**
     * Gets the {@linkplain IRequirementBrowserViewModel}
     * 
     * @return an {@linkplain IRequirementBrowserViewModel}
     */
    IRequirementBrowserViewModel GetRequirementBrowserViewModel();

    /**
     * Gets the {@linkplain IElementDefinitionBrowserViewModel}
     * 
     * @return an {@linkplain IElementDefinitionBrowserViewModel}
     */
    IElementDefinitionBrowserViewModel GetElementDefinitionBrowserViewModel();
    
    /**
     * Gets the {@linkplain IMappedElementListViewViewModel}
     * 
     * @return an {@linkplain IMappedElementListViewViewModel}
     */
    IMappedElementListViewViewModel GetMappedElementListViewViewModel();

    /**
     * Sets the selectedMappedElement
     * 
     * @param mappedElement the {@linkplain MappedElementRowViewModel} that is to be selected
     */
    void SetSelectedMappedElement(MappedElementRowViewModel<? extends Thing, ? extends CapellaElement> mappedElement);

    /**
     * The selected {@linkplain MappedElementRowViewModel}
     * 
     * @return a {@linkplain Observable} of {@linkplain MappedElementRowViewModel}
     */
    Observable<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> GetSelectedMappedElement();

    /**
     * Gets the collection of mapped element
     * 
     * @return {@linkplain ObservableCollection} of {@linkplain MappedElementRowViewModel}
     */
    ObservableCollection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> GetMappedElementCollection();

}
