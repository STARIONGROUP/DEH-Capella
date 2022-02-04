/*
 * CapellaMappingConfigurationService.java
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
package Services.MappingConfiguration;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.ecore.EObject;

import HubController.IHubController;
import ViewModels.Interfaces.IMappedElementRowViewModel;
import cdp4common.engineeringmodeldata.ExternalIdentifierMap;

/**
 * The {@linkplain CapellaMappingConfigurationService} is the implementation of {@linkplain MappingConfigurationService} for the Capella adapter
 */
public class CapellaMappingConfigurationService extends MappingConfigurationService<EObject> implements ICapellaMappingConfigurationService
{
    /**
     * Initializes a new {@linkplain MagicDrawMappingConfigurationService}
     * 
     * @param HubController the {@linkplain IHubController}
     */
    public CapellaMappingConfigurationService(IHubController hubController)
    {
        super(hubController);
    }

    /**
     * Loads the mapping configuration and generates the map result respectively
     * 
     * @param elements a {@linkplain Collection} of {@code TDstElement}
     * @return a {@linkplain Collection} of {@linkplain IMappedElementRowViewModel}
     */
    @Override
    public Collection<IMappedElementRowViewModel> LoadMapping(Collection<EObject> elements)
    {
        return new ArrayList<IMappedElementRowViewModel>();
    }

    /**
     * Creates a new {@linkplain ExternalIdentifierMap} and sets the current as the new one
     * 
     * @param newName the {@linkplain String} name of the new configuration
     * @param addTheTemporyMapping a value indicating whether the current temporary {@linkplain ExternalIdentifierMap} 
     * contained correspondence should be transfered the new one
     * 
     * @return the new configuration {@linkplain ExternalIdentifierMap}
     */
    @Override
    public ExternalIdentifierMap CreateExternalIdentifierMap(String newName, String modelName, boolean addTheTemporyMapping)
    {
        return super.CreateExternalIdentifierMap(newName, modelName, DstController.DstController.THISTOOLNAME, addTheTemporyMapping);
    }
}
