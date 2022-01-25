/*
 * CapellaMappingConfigurationServiceTestFixture.java
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

import static org.mockito.Mockito.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import HubController.IHubController;

/**
 * The {@linkplain CapellaMappingConfigurationServiceTestFixture} is 
 */
class CapellaMappingConfigurationServiceTestFixture
{
    private IHubController hubController;
    private CapellaMappingConfigurationService service;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        this.hubController = mock(IHubController.class);
        this.service = new CapellaMappingConfigurationService(this.hubController);
    }

    @Test
    public void VerifyLoadMapping()
    {
        assertDoesNotThrow(() -> this.service.LoadMapping(new ArrayList()));
    }

    @Test
    public void VerifyCreateExternalIdentifierMap()
    {
        final var name = "name";
        final var modelName = "modelName";
        var newMappingConfiguration = this.service.CreateExternalIdentifierMap(name, modelName, true);
        
        assertEquals(name, newMappingConfiguration.getName());
        assertEquals(modelName, newMappingConfiguration.getExternalModelName());
        assertEquals(DstController.DstController.THISTOOLNAME, newMappingConfiguration.getExternalToolName());
    }
}
