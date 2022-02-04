/*
 * CapellaSessionServiceTestFixture.java
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

import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Reactive.ObservableValue;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;

class CapellaSessionServiceTestFixture
{
    private CapellaSessionService service;
    private ObservableValue<Session> sessionAdded;
    private ObservableValue<Session> sessionRemoved;

    @BeforeEach
    public void Setup() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
        this.sessionAdded = new ObservableValue<Session>(Session.class);
        this.sessionRemoved = new ObservableValue<Session>(Session.class);
                
        var sessionListener = mock(ICapellaSessionListenerService.class);
        
        when(sessionListener.SessionAdded()).thenReturn(this.sessionAdded.Observable());
        when(sessionListener.SessionRemoved()).thenReturn(this.sessionRemoved.Observable());
                
        this.service = new CapellaSessionService(sessionListener);
    }

    @Test
    public void VerifyHasAnyOpenSession()
    {
        var results = new ArrayList<Boolean>();
        this.service.HasAnyOpenSession().subscribe(x -> results.add(x));
        this.sessionAdded.Value(mock(Session.class));
        this.sessionRemoved.Value(mock(Session.class));
        
        assertEquals(2, results.size());
        assertFalse(results.get(0));
    }
    
    @Test
    void VerifyGetModel()
    {
        assertNull(this.service.GetModel());
        this.sessionAdded.Value(mock(Session.class));
        assertNull(this.service.GetModel());
        this.sessionRemoved.Value(mock(Session.class));
        assertNull(this.service.GetModel());
    }
}
