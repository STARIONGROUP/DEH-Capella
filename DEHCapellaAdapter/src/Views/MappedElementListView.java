/*
 * MappedElementListView.java
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
package Views;

import java.awt.Color;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;

import org.apache.commons.lang3.tuple.Pair;
import org.netbeans.swing.outline.Outline;
import org.polarsys.capella.core.data.capellacore.CapellaElement;

import Annotations.ExludeFromCodeCoverageGeneratedReport;
import Renderers.MappedElementListViewRendererDataProvider;
import ViewModels.Interfaces.IViewModel;
import ViewModels.MappedElementListView.Interfaces.IMappedElementListViewViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import Views.ContextMenu.ContextMenu;
import Views.ObjectBrowser.ObjectBrowser;
import cdp4common.commondata.Thing;

/**
 * The {@linkplain MappedElementListView} is the {@linkplain ObjectBrowser} for any view 
 * that contains a {@linkplain IMappedElementListViewViewModel}
 */
@ExludeFromCodeCoverageGeneratedReport
@SuppressWarnings("serial")
public class MappedElementListView extends ObjectBrowser
{
    /**
     * This view attached {@linkplain IViewModel}
     */
    private IMappedElementListViewViewModel dataContext;
    
    /**
     * Initializes a new {@linkplain MagicDrawObjectBrowser}
     */
    public MappedElementListView()
    {
        super();
        this.objectBrowserTree.setRenderDataProvider(new MappedElementListViewRendererDataProvider());
        this.objectBrowserTree.setRootVisible(false);
        
        this.objectBrowserTree.setToolTipText("<html><p>The collection of previously mapped elements (<span style=\"color: #E37814;\">Orange</span>),"
                + "<br>as well as pre-mapped element to existing element (<span style=\"color: #0E5AE8;\">Blue</span>),<br>and element mapped to \"to be created\" "
                + "Element (<span style=\"color: #20B818;\">Green</span>)</P></html>");
        
        this.objectBrowserTree.setSelectionBackground(new Color(104, 143, 184));
    }

    /**
     * Sets the DataContext
     * 
     * @param viewModel the {@link IViewModel} to assign
     */
    @Override
    public void SetDataContext(IViewModel viewModel)
    {
        this.dataContext = (IMappedElementListViewViewModel)viewModel;
        this.Bind();
    }
    
    /**
     * Gets the DataContext
     * 
     * @return An {@link IViewModel}
     */
    @Override
    public IMappedElementListViewViewModel GetDataContext()
    {
        return this.dataContext;
    }
    
    /**
     * Handles the selection when the user changes it
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void OnSelectionChanged()
    {
        int selectedRowIndex = objectBrowserTree.getSelectedRow();

        var row = Pair.of(selectedRowIndex, objectBrowserTree.getValueAt(selectedRowIndex, 0));

        if(!(row.getRight() instanceof MappedElementRowViewModel))
        {
            return;
        }
        
        dataContext.OnSelectionChanged((MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>)row.getRight());

        SwingUtilities.invokeLater(() -> {
            objectBrowserTree.tableChanged(new TableModelEvent(objectBrowserTree.getOutlineModel(), row.getLeft()));
        });
    }

    /**
     * Sets this {@linkplain Outline} contained component context menu
     * 
     * @param contextMenu the {@linkplain ContextMenu}
     */
    public void SetContextMenu(ContextMenu<?> contextMenu)
    {
        this.objectBrowserTree.setComponentPopupMenu(contextMenu);
    }
}
