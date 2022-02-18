/*
 * IDstController.java
 *
 * Copyright (c) 2020-2022 RHEA System S.A.
 *
 * Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski 
 *
 * This file is part of DEH-MDSYSML
 *
 * The DEH-MDSYSML is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * The DEH-MDSYSML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package DstController;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.business.api.session.Session;
import org.polarsys.capella.core.data.capellacore.CapellaElement;

import Enumerations.MappingDirection;
import Reactive.ObservableCollection;
import Services.MappingEngineService.IMappableThingCollection;
import Utils.Ref;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.Thing;
import io.reactivex.Observable;

/**
 * The {@linkplain IDstController} is the interface definition for the {@linkplain DstController}
 */
public interface IDstController extends IDstControllerBase
{
    /**
     * Transfers all the {@linkplain Thing} contained in the {@linkplain dstMapResult} to the Hub
     * 
     * @return a value indicating that all transfer could be completed
     */
    boolean TransferToHub();

    /**
     * Transfers the selected things to be transfered depending on the current {@linkplain MappingDirection}
     * 
     * @return a value indicating that all transfer could be completed
     */
    boolean Transfer();

    /**
     * Maps the {@linkplain input} by calling the {@linkplain IMappingEngine}
     * and assign the map result to the dstMapResult or the hubMapResult
     * 
     * @param input the {@linkplain IMappableThingCollection} in other words the  {@linkplain Collection} of {@linkplain Object} to map
     * @param mappingDirection the {@linkplain MappingDirection} towards the {@linkplain IMappableThingCollection} maps to
     * @return a {@linkplain boolean} indicating whether the mapping operation went well
     */
    boolean Map(IMappableThingCollection input, MappingDirection mappingDirection);

    /**
     * Loads the saved mapping and applies the mapping rule to the loaded things
     * 
     * @return the number of mapped things loaded
     */
    void LoadMapping();

    /**
     * Switches the {@linkplain MappingDirection}
     * 
     * @return the new {@linkplain MappingDirection}
     */
    MappingDirection ChangeMappingDirection();

    /**
     * Gets the current {@linkplain MappingDirection} from {@linkplain currentMappingDirection}
     * 
     * @return the {@linkplain MappingDirection}
     */
    MappingDirection CurrentMappingDirection();

    /**
     * Gets the {@linkplain Observable} of {@linkplain MappingDirection} from {@linkplain currentMappingDirection}
     * 
     * @return a {@linkplain Observable} of {@linkplain MappingDirection}
     */
    Observable<MappingDirection> GetMappingDirection();

    /**
     * Gets the {@linkplain ObservableCollection} of {@linkplain Thing} that are selected for transfer to the Hub
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain Thing}
     */
    ObservableCollection<Thing> GetSelectedDstMapResultForTransfer();

    /**
     * Gets the {@linkplain ObservableCollection} of that are selected for transfer to the 
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain CapellaElement}
     */
    ObservableCollection<CapellaElement> GetSelectedHubMapResultForTransfer();

    /**
     * Gets The {@linkplain ObservableCollection} of DST map result
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain MappedElementRowViewModel}
     */
    ObservableCollection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> GetDstMapResult();

    /**
     * Gets The {@linkplain ObservableCollection} of Hub map result
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain MappedElementRowViewModel}
     */
    ObservableCollection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> GetHubMapResult();

    /**
     * Gets an {@linkplain Observable} of value indicating whether there is any session open in Capella
     * 
     * @return {@linkplain Observable} of {@linkplain Boolean} 
     */
    Observable<Boolean> HasAnyOpenSessionObservable();
}
