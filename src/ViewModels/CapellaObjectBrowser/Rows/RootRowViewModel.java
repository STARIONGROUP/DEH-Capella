/*
 * RootRowViewModel.java
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
package ViewModels.CapellaObjectBrowser.Rows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.business.api.session.Session;
import org.polarsys.capella.core.data.cs.ComponentPkg;

import Services.CapellaSession.ICapellaSessionService;

/**
 * The {@linkplain RootRowViewModel} represents the root element in one containment tree
 */
public class RootRowViewModel extends PackageRowViewModel
{
    /**
     * The {@linkplain ICapellaSessionService} instance
     */
    private ICapellaSessionService sessionService;
    
    /**
     * The {@linkplain Collection} of {@linkplain EObject} that the {@linkplain RootRowViewModel} should contain 
     */
    private ArrayList<EObject> containedElements = new ArrayList<>(); 
    
    /**
     * Initializes a new {@linkplain RootRowViewModel}
     * 
     * @param sessionService the {@linkplain ICapellaSessionService} instance
     */
    protected RootRowViewModel(ICapellaSessionService sessionService)
    {
        super(null, null);
        this.sessionService = sessionService;
    }
    
    /**
     * Initializes a new {@linkplain RootRowViewModel}
     * 
     * @param name the name of this row
     * @param tree the children element that this row contains
     */
    public RootRowViewModel(String name, TreeIterator<Notifier> tree)
    {
        this(null);
        Notifier element;
        
        while((element = tree.next()) !=null)
        {
            this.containedElements.add((EObject)element);
        }
        
        this.UpdateProperties(name);
    }
    
    /**
     * Initializes a new {@linkplain RootRowViewModel}
     * 
     * @param name the name of this row
     * @param elements the children element that this row contains
     */
    public RootRowViewModel(String name, Collection<EObject> elements)
    {
        this(null);
        this.containedElements.addAll(elements);
        this.UpdateProperties(name);
    }
    
    /**
     * Initializes a new {@linkplain RootRowViewModel}
     * 
     * @param sessions the {@linkplain Session} represented by this row
     * @param elements the children element that this row contains
     */
    public RootRowViewModel(Session session, Collection<EObject> elements)
    {
        this(null);
        this.containedElements.addAll(elements);
        this.UpdateProperties(URI.decode(session.getSessionResource().getURI().lastSegment()));
    }
    
    /**
     * Initializes a new {@linkplain RootRowViewModel}
     * 
     * @param sessionService the {@linkplain ICapellaSessionService} instance
     * @param elements the children element that this row contains
     */
    public RootRowViewModel(ICapellaSessionService sessionService, Collection<EObject> elements)
    {
        this(sessionService);
        this.UpdateProperties(elements);
    }
    
    /**
     * Updates this view model properties
     * 
     * @param elements the children element that this row contains
     */
    protected void UpdateProperties(Collection<EObject> elements)
    {
        super.UpdateProperties("Capella Models");
        
        var sessionToCapellaElementMap = new HashMap<Session, ArrayList<EObject>>();
        
        for (var element : elements)
        {
           final var sessionKey = this.sessionService.GetSession(element);
           
           if(sessionKey == null)
           {
               continue;
           }
           
           var entry = sessionToCapellaElementMap
                   .putIfAbsent(sessionKey, new ArrayList<EObject>());
           
           if(entry == null)
           {
               entry = sessionToCapellaElementMap.get(sessionKey);
           }
           
           entry.add(element);
        }
        
        for (var sessionKey : sessionToCapellaElementMap.keySet())
        {
            this.GetContainedRows().add(new RootRowViewModel(sessionKey, sessionToCapellaElementMap.get(sessionKey)));
        }
    }

    /**
     * Updates this view model properties
     * 
     * @param name the name of this row
     * @param elements the children element that this row contains
     */
    protected void UpdateProperties(String name)
    {
        super.UpdateProperties(name);
        this.ComputeContainedRows();
    }

    /**
     * Computes the contained rows of this row view model
     */
    @Override
    public void ComputeContainedRows() 
    {
        if(this.containedElements == null)
        {
            return;
        }
        
        for (var element : this.containedElements)
        { 
            if(element instanceof ComponentPkg)
            {
                this.ComputeContainedRow((ComponentPkg)element);
            }
        }
    }
}
