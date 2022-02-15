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

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;

import Reactive.ObservableValue;
import Services.CapellaSelection.ICapellaSelectionService;
import ViewModels.CapellaObjectBrowser.Rows.ProjectRowViewModel;
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
    private final ICapellaSessionListenerService sessionListener;

    /**
     * The {@linkplain ICapellaSelectionService} instance
     */
    private final ICapellaSelectionService selectionService;
    
    /**
     * The active session
     */
    private Session activeSession;

    /**
     * Gets the active {@linkplain Session}
     * 
     * @return a {@linkplain Session}
     */
    @Override
    public Session GetActiveSession()
    {
        if(this.activeSession == null)
        {
            this.SetActiveSession(null);
        }
        
        return this.activeSession;
    }
        
    /**
     * Initializes a new {@linkplain CapellaSessionService}
     * 
     * @param sessionListener the {@linkplain ICapellaSessionListenerService} instance
     * @param selectionService the {@linkplain ICapellaSelectionService} instance
     */
    public CapellaSessionService(ICapellaSessionListenerService sessionListener, ICapellaSelectionService selectionService)
    {
        this.sessionListener = sessionListener;
        this.selectionService = selectionService;
        
        SessionManager.INSTANCE.addSessionsListener(this.sessionListener);
        
        this.selectionService.SelectionChanged().subscribe(x -> 
            this.SetActiveSession(SessionManager.INSTANCE.getSession(x)));
              
        this.sessionListener.SessionAdded()
            .subscribe(x -> this.SetActiveSession(x));
        
        this.sessionListener.SessionRemoved()
            .subscribe(x -> this.SetActiveSession(null));
    }

    /**
     * Gets the session corresponding to a semantic {@linkplain EObject} 
     * 
     * @param object the {@linkplain EObject} to retrieve the session it belongs to
     * @return the corresponding {@linkplain Session}
     */
    @Override
    public Session GetSession(EObject object)
    {
        return SessionManager.INSTANCE.getSession(object);
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
    public Observable<Boolean> HasAnyOpenSessionObservable()
    {
        return this.hasAnyOpenSession.Observable();
    }
    
    /**
     * Gets the value emitted by {@linkplain HasAnyOpenSessionObservable} indicating whether there is any session open
     * 
     * @return a {@linkplain Boolean} value
     */
    @Override
    public boolean HasAnyOpenSession()
    {
        return this.HasAnyActiveSession();
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
     * Gets the models of the active sessions
     * 
     * @return a {@linkplain RootRowViewModel}, or null if no active session is found
     */
    @Override
    public RootRowViewModel GetModels()
    {
        if(!this.HasAnyActiveSession())
        {
            this.logger.info("No active session has been found!");
            return null;
        }
        
        var rootRowViewModel = new RootRowViewModel("Capella Models");
        
        for (var session : SessionManager.INSTANCE.getSessions())
        {
            Notifier element;
            var elements = new ArrayList<Notifier>();
            var contents = session.getTransactionalEditingDomain().getResourceSet().getAllContents();
            
            while((element = contents.next()) !=null)
            {
                elements.add((Notifier)element);
            }
            
            rootRowViewModel.GetContainedRows().add(new RootRowViewModel(URI.decode(session.getSessionResource().getURI().lastSegment()) ,elements));
        }
        
        return rootRowViewModel;
    }
}
