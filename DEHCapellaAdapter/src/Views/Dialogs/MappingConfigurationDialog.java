/*
 * MappingConfigurationDialog.java
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
package Views.Dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import Annotations.ExludeFromCodeCoverageGeneratedReport;
import Enumerations.MappingDirection;
import Utils.ImageLoader.ImageLoader;
import ViewModels.Dialogs.Interfaces.IMappingConfigurationDialogViewModel;
import ViewModels.Interfaces.IViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import Views.CapellaObjectBrowser;
import Views.MappedElementListView;
import Views.Interfaces.IDialog;
import Views.ObjectBrowser.ObjectBrowser;
import cdp4common.commondata.ClassKind;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.RequirementsSpecification;

/**
 * The MappingConfigurationDialog is the base view for all capella mapping configuration dialog
 * 
 * @param <TViewModel> the type of view model the implementing class should be attached to
 */
@ExludeFromCodeCoverageGeneratedReport
@SuppressWarnings("serial")
public abstract class MappingConfigurationDialog<TViewModel extends IMappingConfigurationDialogViewModel<?>> extends JDialog  implements IDialog<TViewModel, Boolean>
{
    /**
     * Backing field for {@linkplain #GetDialogResult()}
     */
    private Boolean dialogResult;

    /**
     * This view attached {@linkplain #IViewModel}
     */
    private TViewModel dataContext;
    
    /**
     * The {@linkplain ObjectBrowser} view for {@linkplain ElementDefinition}
     */
    private ObjectBrowser elementDefinitionBrowser;

    /**
     * The {@linkplain ObjectBrowser} view for {@linkplain RequirementsSpecification}
     */
    private ObjectBrowser requirementBrowser;    

    /**
     * The {@linkplain MappedElementListView} view for the {@linkplain MappedElementRowViewModel}
     */
    private MappedElementListView mappedElementListView;

    /**
     * View components declarations
     */
    private final JPanel contentPanel = new JPanel();
    private JButton okButton;
    private JButton cancelButton;
    private JSplitPane browserSplitPane;
    private CapellaObjectBrowser capellaObjectBrowser;
    private boolean hasBeenPaintedOnce;
    private JSplitPane mainSplitPane;
    private JPanel hubBrowserPanel;
    private JTabbedPane hubBrowserTreeViewsContainer;
    private JCheckBox mapToNewElementCheckBox;
    private JButton resetButton;
    private JPanel mappedElementsPanel;
    
    /**
     * Initializes a new {@linkplain CapellaDstToHubMappingConfigurationDialog}
     * 
     * @param mappingDirection the {@linkplain MappingDirection} the concrete view is working on  
     * @param mappedElementListView the {@linkplain MappedElementListView} instance
     */
    public MappingConfigurationDialog(MappingDirection mappingDirection, MappedElementListView mappedElementListView)
    {
        this.mappedElementListView = mappedElementListView;
        this.Initialize(mappingDirection);
    }

    /**
     * Initializes a new {@linkplain CapellaDstToHubMappingConfigurationDialog}
     * 
     * @param mappingDirection the {@linkplain MappingDirection} the concrete view is working on
     */
    public MappingConfigurationDialog(MappingDirection mappingDirection)
    {
        this.mappedElementListView = new MappedElementListView();
        this.Initialize(mappingDirection);
    }

    /**
     * Initializes this view visual components and properties
     * 
     * @param mappingDirection the {@linkplain MappingDirection} the concrete view is working on  
     */
    @ExludeFromCodeCoverageGeneratedReport
    private void Initialize(MappingDirection mappingDirection)
    {
        var titleSuffix = mappingDirection == MappingDirection.FromDstToHub ? "Capella to the Hub" : "Hub to Capella";
        this.setTitle(String.format("Mapping Configuration dialog from %s", titleSuffix));
        this.setType(Type.POPUP);
        this.setModal(true);
        this.setBounds(100, 100, 549, 504);
        this.setMinimumSize(new Dimension(800, 600));
        this.setIconImage(ImageLoader.GetIcon("icon16.png").getImage());
        this.getContentPane().setLayout(new BorderLayout());
        this.contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(this.contentPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[] {};
        gbl_contentPanel.rowHeights = new int[] {0};
        gbl_contentPanel.columnWeights = new double[]{1.0};
        gbl_contentPanel.rowWeights = new double[]{1.0};
        this.contentPanel.setLayout(gbl_contentPanel);
        
        this.mainSplitPane = new JSplitPane();
        this.mainSplitPane.setDividerLocation(0.5);
        this.mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        
        this.browserSplitPane = new JSplitPane();
        this.browserSplitPane.setContinuousLayout(true);
        this.browserSplitPane.setDividerLocation(0.5);
        this.mainSplitPane.setLeftComponent(this.browserSplitPane);

        this.capellaObjectBrowser = new CapellaObjectBrowser();
        
        this.hubBrowserPanel = new JPanel();
        
        if(mappingDirection == MappingDirection.FromDstToHub)
        {
            this.browserSplitPane.setLeftComponent(this.capellaObjectBrowser);
            this.browserSplitPane.setRightComponent(this.hubBrowserPanel);
        }
        else
        {
            this.browserSplitPane.setRightComponent(this.capellaObjectBrowser);
            this.browserSplitPane.setLeftComponent(this.hubBrowserPanel);            
        }
                
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{0, 0};
        gbl_panel.rowHeights = new int[]{0, 0, 0};
        gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        this.hubBrowserPanel.setLayout(gbl_panel);
        
        
        this.hubBrowserTreeViewsContainer = new JTabbedPane(SwingConstants.TOP);
        GridBagConstraints gbc_hubBrowserTreeViewsContainer = new GridBagConstraints();
        gbc_hubBrowserTreeViewsContainer.insets = new Insets(0, 0, 5, 0);
        gbc_hubBrowserTreeViewsContainer.fill = GridBagConstraints.BOTH;
        gbc_hubBrowserTreeViewsContainer.gridx = 0;
        gbc_hubBrowserTreeViewsContainer.gridy = 0;
        
        this.elementDefinitionBrowser = new ObjectBrowser();
        this.elementDefinitionBrowser.setBackground(Color.WHITE);
        this.hubBrowserTreeViewsContainer.addTab("Element Definitions", ImageLoader.GetIcon(ClassKind.Iteration), this.elementDefinitionBrowser, null);
        
        this.requirementBrowser = new ObjectBrowser();
        this.hubBrowserTreeViewsContainer.addTab("Requirements", ImageLoader.GetIcon(ClassKind.RequirementsSpecification), this.requirementBrowser, null);
        
        this.hubBrowserPanel.add(this.hubBrowserTreeViewsContainer, gbc_hubBrowserTreeViewsContainer);
        
        this.mapToNewElementCheckBox = new JCheckBox("Map the current selected row to a new Hub element");
        this.mapToNewElementCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
        
        GridBagConstraints gbc_mapToNewHubElementCheckBox = new GridBagConstraints();
        gbc_mapToNewHubElementCheckBox.anchor = GridBagConstraints.WEST;
        gbc_mapToNewHubElementCheckBox.gridx = 0;
        gbc_mapToNewHubElementCheckBox.gridy = 1;
        
        this.hubBrowserPanel.add(this.mapToNewElementCheckBox, gbc_mapToNewHubElementCheckBox);
        
        this.mappedElementsPanel = new JPanel();

        GridBagLayout mappedElementsPanelLayout = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{0, 0};
        gbl_panel.rowHeights = new int[]{0, 0, 0};
        gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        this.mappedElementsPanel.setLayout(mappedElementsPanelLayout);
        
        this.mappedElementListView.setBackground(Color.WHITE);
        
        this.mappedElementsPanel.add(this.mappedElementListView);
        
        this.mainSplitPane.setRightComponent(this.mappedElementsPanel);
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 0;
        mappedElementsPanel.setLayout(new BoxLayout(mappedElementsPanel, BoxLayout.Y_AXIS));
                
        GridBagConstraints gbc_splitPane = new GridBagConstraints();
        gbc_splitPane.fill = GridBagConstraints.BOTH;
        gbc_splitPane.gridx = 0;
        gbc_splitPane.gridy = 0;
        this.contentPanel.add(mainSplitPane, gbc_splitPane);
        
        JPanel buttonPane = new JPanel();
        getContentPane().add(buttonPane, BorderLayout.SOUTH);
        GridBagLayout gbl_buttonPane = new GridBagLayout();
        gbl_buttonPane.columnWidths = new int[] {1, 588, 1, 1, 1, 1};
        gbl_buttonPane.rowHeights = new int[]{23, 0};
        gbl_buttonPane.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_buttonPane.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        buttonPane.setLayout(gbl_buttonPane);
        
        resetButton = new JButton("Reset");
        resetButton.setToolTipText("Reset  to the default mapping.");
        GridBagConstraints gbc_resetButton = new GridBagConstraints();
        gbc_resetButton.insets = new Insets(0, 5, 10, 5);
        gbc_resetButton.gridx = 0;
        gbc_resetButton.gridy = 0;
        buttonPane.add(resetButton, gbc_resetButton);
        
        this.okButton = new JButton("Next");
        this.okButton.setToolTipText("Map the current elements");
        this.okButton.setActionCommand("OK");
        GridBagConstraints gbc_okButton = new GridBagConstraints();
        gbc_okButton.insets = new Insets(0, 5, 10, 5);
        gbc_okButton.gridx = 2;
        gbc_okButton.gridy = 0;
        buttonPane.add(this.okButton, gbc_okButton);
        
        this.cancelButton = new JButton("Cancel");
        this.cancelButton.setToolTipText("Close this dialog and abort the mapping");
        this.cancelButton.setActionCommand("Cancel");
        GridBagConstraints gbc_cancelButton = new GridBagConstraints();
        gbc_cancelButton.insets = new Insets(0, 5, 10, 5);
        gbc_cancelButton.gridx = 3;
        gbc_cancelButton.gridy = 0;
        buttonPane.add(this.cancelButton, gbc_cancelButton);
        
        this.addComponentListener(new @ExludeFromCodeCoverageGeneratedReport ComponentAdapter() 
        {
            /**
             * Invoked when the component's size changes
             * 
             * @param componentEvent the {@linkplain ComponentEvent}
             */
            @Override
            @ExludeFromCodeCoverageGeneratedReport
            public void componentResized(ComponentEvent componentEvent) 
            {
                super.componentResized(componentEvent);
                mainSplitPane.setDividerLocation(0.5);
                browserSplitPane.setDividerLocation(0.5);
            }
        });
    }
    
    /**
     * Binds the {@linkplain #TViewModel} viewModel to the implementing view
     * 
     * @param viewModel the view model to bind
     */
    public void Bind()
    {
        this.elementDefinitionBrowser.SetDataContext(this.dataContext.GetElementDefinitionBrowserViewModel());
        this.requirementBrowser.SetDataContext(this.dataContext.GetRequirementBrowserViewModel());
        this.capellaObjectBrowser.SetDataContext(this.dataContext.GetCapellaObjectBrowserViewModel());
        this.mappedElementListView.SetDataContext(this.dataContext.GetMappedElementListViewViewModel());
        
        this.dataContext.GetSelectedMappedElement().subscribe(x -> 
        {
            if(x != null)
            {
                this.UpdateMapToNewHubElementCheckBoxState(x.GetShouldCreateNewTargetElementValue());
            }
        });
        
        this.mapToNewElementCheckBox.addActionListener(x -> 
        {
            this.dataContext.WhenMapToNewElementCheckBoxChanged(this.mapToNewElementCheckBox.isSelected());
        });
        
        this.dataContext.GetShouldMapToNewElementCheckBoxBeEnabled()
            .subscribe(x -> this.mapToNewElementCheckBox.setEnabled(x));
        
        this.okButton.addActionListener(x -> this.CloseDialog(true));        
        this.cancelButton.addActionListener(x -> this.CloseDialog(false));
        this.resetButton.addActionListener(x -> this.dataContext.ResetPreMappedThings());
    }

    /**
     * Updates the visual state of the {@linkplain mapToNewHubElementCheckBox} according to the selected mapped element
     * 
     * @param shouldCreateNewTargetElement the new value
     */
    private void UpdateMapToNewHubElementCheckBoxState(boolean shouldCreateNewTargetElement)
    {
        SwingUtilities.invokeLater(() -> this.mapToNewElementCheckBox.setSelected(shouldCreateNewTargetElement));        
    }

    /**
     * Sets the DataContext
     * 
     * @param viewModel the {@link IViewModel} to assign
     */
    @SuppressWarnings("unchecked")
    public void SetDataContext(IViewModel viewModel)
    {
        this.dataContext = (TViewModel) viewModel;
        this.Bind();
    }
    
    /**
    * Gets the DataContext
    * 
    * @return an {@link IViewModel}
    */
    @Override
    public TViewModel GetDataContext()
    {
        return this.dataContext;
    }

    /**
     * Shows the dialog and return the result
     * 
     * @return a {@linkplain TResult}
     */
    @Override
    public Boolean ShowDialog()
    {
        this.setVisible(true);
        return this.dialogResult;
    }
    
    /**
     * Closes the dialog and sets the {@link dialogResult}
     * 
     * @param result the {@linkplain TResult} to set
     */
    @Override
    public void CloseDialog(Boolean result)
    {
        this.dialogResult = result;
        setVisible(false);
        dispose();
    }
    
    /**
     * Gets the {@linkplain dialogResult}
     * 
     * @return a {@linkplain Boolean}
     */
    public Boolean GetDialogResult()
    {
        return this.dialogResult;
    }

    /**
     * Paints the container. This forwards the paint to any lightweight components that are children of this container. 
     * If this method is re-implemented, super.paint(g) should be called so that lightweight components 
     * are properly rendered. If a child component is entirely clipped by the current clipping setting in g, 
     * paint() will not be forwarded to that child.
     * 
     * @param graphics the specified Graphics window
     */
    @Override
    public void paint(Graphics graphics) 
    {
        super.paint(graphics);

        if (!this.hasBeenPaintedOnce) 
        {
            this.hasBeenPaintedOnce = true;
            this.mainSplitPane.setDividerLocation(0.5);
            this.browserSplitPane.setDividerLocation(0.5);
        }
    }
}
