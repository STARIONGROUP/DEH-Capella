/*
 * CapellaHubBrowserPanel.java
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
package Views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import App.AppContainer;
import ViewModels.Interfaces.IHubBrowserPanelViewModel;
import Views.ViewParts.BaseViewPart;

/**
 * The {@linkplain CapellaHubBrowserPanel} is the main panel view for the Hub controls like session controls and tree views.
 * This view is meant to be integrated into another container view specific to DST * 
 */
public class CapellaHubBrowserPanel extends BaseViewPart<IHubBrowserPanelViewModel, HubBrowserPanel>
{
    /**
     * Initializes a new {@linkplain CapellaHubBrowserPanel}
     */
    public CapellaHubBrowserPanel()
    {
        super(HubBrowserPanel.class.getSimpleName());
    }
    
    /**
     * Creates the part control for this {@linkplain ViewPart}
     * 
     * @param parent the {@linkplain Composite} parent
     */
    @Override
    public void createPartControl(Composite parent)
    {
        this.CreateBasePartControl(parent);
        this.View = new HubBrowserPanel();
        this.SetDataContext(AppContainer.Container.getComponent(IHubBrowserPanelViewModel.class));
        this.Container.add(this.View);
    }

    /**
     * Binds the <code>TViewModel viewModel</code> to the implementing view
     * 
     * @param <code>viewModel</code> the view model to bind
     */
    @Override
    public void Bind()
    {
        this.View.GetSessionControlPanel().SetDataContext(this.DataContext.GetSessionControlViewModel());
        this.View.getHubBrowserHeader().SetDataContext(this.DataContext.GetHubBrowserHeaderViewModel());
        this.View.GetElementDefinitionBrowser().SetDataContext(this.DataContext.GetElementDefinitionBrowserViewModel());
        this.View.GetRequirementBrowser().SetDataContext(this.DataContext.GetRequirementBrowserViewModel());
    }
}
