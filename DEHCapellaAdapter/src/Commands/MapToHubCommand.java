/*
 * MapToHubCommand.java
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
package Commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import App.AppContainer;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.Mapping.IMapCommandService;

/**
 * The {@linkplain MapToHubCommand} is the command that gets executed when the user clicks on the context menu entry "Map Selection To The Hub",
 * Needless to state that {@linkplain AbstractHandler}s life cycle is handle by eclipse it-self, therefore no dependency injection is possible using DEH-CommonJ PicoContainer
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class MapToHubCommand extends AbstractHandler
{
    /**
     * The {@linkplain IMapCommandService} instance which control this {@linkplain AbstractHandler}
     */
    private final IMapCommandService commandService = AppContainer.Container.getComponent(IMapCommandService.class);
    
    /**
     * Initializes a new {@linkplain MapToHubCommand}
     */
    public MapToHubCommand()
    {
        this.setBaseEnabled(false);        
        
        this.commandService
                .CanExecuteObservable()
                .subscribe(x -> this.setBaseEnabled(x));
    }
    
    /**
     * Executes with the map of parameter values by name in the parameter event
     * 
     * @param event the {@linkplain ExecutionEvent}
     * @return an {@linkplain Object}
     * @throws ExecutionException might throw {@linkplain ExecutionException}
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        this.commandService.MapSelection(MappingDirection.FromDstToHub);
        return null;
    }
}
