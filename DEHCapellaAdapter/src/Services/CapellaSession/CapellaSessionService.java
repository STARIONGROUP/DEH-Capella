/*
 * CapellaModelService.java
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
package Services.CapellaSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.business.api.session.Session;
import org.polarsys.capella.core.data.capellacore.CapellaElement;

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
    private final ICapellaSessionListenerService sessionListener;

    /**
     * The {@linkplain ISiriusSessionManagerWrapper} instance
     */
    private ISiriusSessionManagerWrapper sessionManager;
    
    /**
     * Backing field for {@linkplain HasAnyOpenSessionObservable}
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
     * Initializes a new {@linkplain CapellaSessionService}
     * 
     * @param sessionListener the {@linkplain ICapellaSessionListenerService} instance
     * @param sessionManager the {@linkplain ISiriusSessionManagerWrapper} instance
     */
    public CapellaSessionService(ICapellaSessionListenerService sessionListener, ISiriusSessionManagerWrapper sessionManager)
    {
        this.sessionListener = sessionListener;
        this.sessionManager = sessionManager;

        this.sessionManager.AddListener(this.sessionListener);

        this.sessionListener.SessionUpdated()
            .subscribe(x -> this.hasAnyOpenSession.Value(this.sessionManager.HasAnyOpenSession()));
            
        this.sessionListener.SessionAdded()
            .subscribe(x -> this.hasAnyOpenSession.Value(this.sessionManager.HasAnyOpenSession()));
        
        this.sessionListener.SessionRemoved()
            .subscribe(x -> this.hasAnyOpenSession.Value(this.sessionManager.HasAnyOpenSession()));
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
        return this.sessionManager.GetSession(object);
    }
        
    /**
     * Gets the value emitted by {@linkplain HasAnyOpenSessionObservable} indicating whether there is any session open
     * 
     * @return a {@linkplain Boolean} value
     */
    @Override
    public boolean HasAnyOpenSession()
    {
        return this.sessionManager.HasAnyOpenSession();
    }
    
    
    /**
     * Gets the models of the active sessions
     * 
     * @return a {@linkplain RootRowViewModel}, or null if no active session is found
     */
    @Override
    public RootRowViewModel GetModels()
    {
        if(!this.sessionManager.HasAnyOpenSession())
        {
            this.logger.info("No active session has been found!");
            return null;
        }
        
        var rootRowViewModel = new RootRowViewModel("Capella Models");
        
        this.ProcessSessionsElements((uri, notifiers) -> rootRowViewModel.GetContainedRows().add(new RootRowViewModel(URI.decode(uri.lastSegment()), notifiers)));
        
        return rootRowViewModel;
    }

    /**
     * Loops through all {@linkplain Notifier} element from all the open {@linkplain Session}s
     * and accept a {@linkplain BiConsumer} on them
     * 
     * @param action the {@linkplain BiConsumer} of {@linkplain URI} and {@linkplain ArrayList} of {@linkplain Notifier}.
     * The {@linkplain URI} identifies the {@linkplain Session} to which the elements from the array belongs to
     */
    private void ProcessSessionsElements(BiConsumer<URI, ArrayList<Notifier>> action)
    {
        for (var session : this.sessionManager.GetSessions())
        {
            Notifier element;
            var elements = new ArrayList<Notifier>();
            var contents = session.getTransactionalEditingDomain().getResourceSet().getAllContents();
            
            while(contents.hasNext() && (element = contents.next()) !=null)
            {
                elements.add((Notifier)element);
            }
            
            action.accept(session.getSessionResource().getURI(), elements);
        }
    }
    
    /**
     * Gets all the {@linkplain CapellaElement} from the currently open {@linkplain Session}s
     * 
     * @return a {@linkplain HashMap} of {@linkplain URI} and a {@linkplain List} of {@linkplain CapellaElement}
     */
    @Override
    public HashMap<URI, List<CapellaElement>> GetAllCapellaElementsFromOpenSessions()
    {
        var sessionAndObjectsMap = new HashMap<URI, List<CapellaElement>>();
        
        this.ProcessSessionsElements((uri, notifiers) -> 
        {
            var elements = notifiers.stream()
                    .filter(x -> x instanceof CapellaElement)
                    .map(x -> (CapellaElement)x)
                    .collect(Collectors.toList());
            
            sessionAndObjectsMap.putIfAbsent(uri, elements);
        });
        
        return sessionAndObjectsMap;
    }
}
