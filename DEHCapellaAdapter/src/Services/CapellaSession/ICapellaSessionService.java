/*
 * ICapellaSessionService.java
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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.business.api.session.Session;

import ViewModels.CapellaObjectBrowser.Rows.RootRowViewModel;
import io.reactivex.Observable;

/**
 * The {@linkplain ICapellaSessionService} is the main interface definition for {@linkplain CapellaSessionService}
 */
public interface ICapellaSessionService
{
    /**
     * Gets an {@linkplain Observable} of value indicating whether there is any session open
     * 
     * @return an {@linkplain Observable} of {@linkplain Boolean}
     */
    Observable<Boolean> HasAnyOpenSessionObservable();

    /**
     * Gets the active {@linkplain Session}
     * 
     * @return a {@linkplain Session}
     */
    Session GetActiveSession();

    /**
     * Gets the models of the active sessions
     * 
     * @return a {@linkplain RootRowViewModel}, or null if no active session is found
     */
    RootRowViewModel GetModels();

    /**
     * Gets the session corresponding to a semantic {@linkplain EObject} 
     * 
     * @param object the {@linkplain EObject} to retrieve the session it belongs to
     * @return the corresponding {@linkplain Session}
     */
    Session GetSession(EObject object);

    /**
     * Gets the value emitted by {@linkplain HasAnyOpenSessionObservable} indicating whether there is any session open
     * 
     * @return a {@linkplain Boolean} value
     */
    boolean HasAnyOpenSession();
}