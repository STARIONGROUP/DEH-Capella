/*
 * CapellaDstToHubMappingConfigurationDialog.java
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
package Views.Dialogs;

import Annotations.ExludeFromCodeCoverageGeneratedReport;
import Enumerations.MappingDirection;
import ViewModels.Dialogs.Interfaces.IDstToHubMappingConfigurationDialogViewModel;
import Views.CapellaMappedElementListView;

/**
 * The {@linkplain CapellaDstToHubMappingConfigurationDialog} is the dialog view to allow to configure a mapping 
 * to be defined between a selection of DST elements and the hub element
 */
@SuppressWarnings("serial")
@ExludeFromCodeCoverageGeneratedReport
public class CapellaDstToHubMappingConfigurationDialog extends MappingConfigurationDialog<IDstToHubMappingConfigurationDialogViewModel>
{
    /**
     * Initializes a new {@linkplain CapellaDstToHubMappingConfigurationDialog}
     */
    public CapellaDstToHubMappingConfigurationDialog()
    {
        super(MappingDirection.FromDstToHub, new CapellaMappedElementListView());
    }
}
