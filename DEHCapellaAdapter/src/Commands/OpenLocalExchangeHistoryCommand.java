/*
 * OpenLocalExchangeHistoryCommand.java
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
package Commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import App.AppContainer;
import Services.NavigationService.INavigationService;
import Views.ExchangeHistory.ExchangeHistoryDialog;

/**
 * The {@linkplain OpenLocalExchangeHistoryCommand} is the command {@linkplain AbstractHandler} to handle the local exchange history dialog
 */
public class OpenLocalExchangeHistoryCommand extends AbstractHandler
{
    /**
     * The {@linkplain INavigationService} instance which control this {@linkplain AbstractHandler}
     */
    private final INavigationService navigationService = AppContainer.Container.getComponent(INavigationService.class);
        
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
        this.navigationService.Show(new ExchangeHistoryDialog());
        return null;
    }
}
