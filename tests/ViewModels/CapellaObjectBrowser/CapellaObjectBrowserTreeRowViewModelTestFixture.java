/*
 * CapellaObjectBrowserTreeRowViewModelTestFixture.java
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
package ViewModels.CapellaObjectBrowser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.polarsys.capella.core.data.cs.ComponentPkg;

import Services.CapellaSession.ICapellaSessionService;
import ViewModels.CapellaObjectBrowser.Rows.PackageRowViewModel;
import ViewModels.CapellaObjectBrowser.Rows.RootRowViewModel;

class CapellaObjectBrowserTreeRowViewModelTestFixture
{
    private CapellaObjectBrowserTreeRowViewModel rowModel;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        this.rowModel = new CapellaObjectBrowserTreeRowViewModel();
    }

    @Test
    public void VerifyGetValueFor()
    {
        var componentPkg = mock(ComponentPkg.class);
        when(componentPkg.eContents()).thenReturn(new BasicEList<EObject>());
        var rowViewModel = new PackageRowViewModel(null, componentPkg);
        assertEquals(componentPkg.getClass().getSimpleName(), this.rowModel.getValueFor(rowViewModel, 0));
        assertEquals("", this.rowModel.getValueFor(new RootRowViewModel(mock(ICapellaSessionService.class), new ArrayList<EObject>()), 0));
        assertEquals("-", this.rowModel.getValueFor("s", 0));
        assertEquals("-", this.rowModel.getValueFor(rowViewModel, 2));
    }
    
    @Test
    public void VerifyGetColumnCount()
    {
        assertEquals(1, this.rowModel.getColumnCount());
    }
    
    @Test
    public void VerifyGetColumnClass()
    {
        assertEquals(String.class, this.rowModel.getColumnClass(0));
        assertNull(this.rowModel.getColumnClass(42));
    }
    
    @Test
    public void VerifyIsCellEditable()
    {
        assertFalse(this.rowModel.isCellEditable("", 0));
    }
    
    @Test
    public void VerifyGetColumnName()
    {
        assertEquals("Row Type", this.rowModel.getColumnName(0));
        assertNull(this.rowModel.getColumnName(42));
    }
    
    @Test
    public void VerifySetValueFor()
    {
        assertDoesNotThrow(() -> this.rowModel.setValueFor("", 42, ""));
    }
}
