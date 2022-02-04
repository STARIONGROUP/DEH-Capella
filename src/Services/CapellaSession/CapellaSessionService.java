/*
 * CapellaModelService.java
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
package Services.CapellaSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;

import Reactive.ObservableValue;
import ViewModels.CapellaObjectBrowser.Rows.RootRowViewModel;
import io.reactivex.Observable;

/**
 * The {@linkplain CapellaModelService} is the service providing easier access to the capella {@linkplain Sessions}s
 */
public class CapellaSessionService implements ICapellaSessionService
{
    /**
     * The current class Logger
     */
    private final Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain ICapellaSessionListenerService} that observe changes on the {@linkplain SessionManager}
     */
    private ICapellaSessionListenerService sessionListener;
    
    /**
     * The active session
     */
    private Session activeSession;
        
    /**
     * Initializes a new {@linkplain CapellaSessionService}
     * 
     * @param sessionListener the {@linkplain ICapellaSessionListenerService} instance
     */
    public CapellaSessionService(ICapellaSessionListenerService sessionListener)
    {
        this.sessionListener = sessionListener;
        SessionManager.INSTANCE.addSessionsListener(this.sessionListener);
        
        this.sessionListener.SessionAdded()
            .subscribe(x -> this.SetActiveSession(x));
        
        this.sessionListener.SessionRemoved()
            .subscribe(x -> this.SetActiveSession(null));
    }

    /**
     * Sets the active session
     * 
     * @param session the new {@linkplain Session} null if one session got removed/closed
     */
    private void SetActiveSession(Session session)
    {
        if(session != null)
        {
            this.activeSession = session;
        }
        else
        {
            this.activeSession = this.HasAnyActiveSession() 
                    ? SessionManager.INSTANCE.getSessions()
                            .stream()
                            .reduce((first, second) -> second)
                            .get()
                    : null;
        }
        
        this.hasAnyOpenSession.Value(this.HasAnyActiveSession());
    }

    /**
     * Backing field for {@linkplain HasAnyOpenSession}
     */
    private ObservableValue<Boolean> hasAnyOpenSession = new ObservableValue<Boolean>(false, Boolean.class);
    
    /**
     * Gets the {@linkplain Observable} of value indicating whether there is any session open
     * 
     * @return an {@linkplain Observable} of {@linkplain Boolean}
     */
    @Override
    public Observable<Boolean> HasAnyOpenSession()
    {
        return this.hasAnyOpenSession.Observable();
    }
    
    /**
     * Gets a value indicating whether there is any active session in the workspace
     * 
     * @return a {@linkplain boolean}
     */
    private boolean HasAnyActiveSession()
    {
        try
        {
            return !SessionManager.INSTANCE.getSessions().isEmpty();
        }
        catch(Exception exception)
        {
            this.logger.catching(exception);
            return false;
        }
    }
    
    /**
     * Gets the model of the active session
     * 
     * @return a {@linkplain RootRowViewModel}, or null if no active session is found
     */
    @Override
    public RootRowViewModel GetModel()
    {
        if(!this.HasAnyActiveSession())
        {
            this.logger.info("No active session has been found!");
            return null;
        }
        
        return new RootRowViewModel(this.activeSession.getSessionResource().getURI().toFileString(), 
                this.activeSession.getTransactionalEditingDomain().getResourceSet().getAllContents());
    }
}
