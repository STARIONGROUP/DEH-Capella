/*
 * MappedElementRowViewModelRenderer.java
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
package Renderers;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import Annotations.ExludeFromCodeCoverageGeneratedReport;
import Enumerations.MappingDirection;
import ViewModels.Interfaces.IHaveTargetArchitecture;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.Thing;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import Enumerations.CapellaArchitecture;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import net.miginfocom.swing.MigLayout;
 
/**
 * The {@linkplain MappedElementRowViewModelRenderer} is the custom renderer that allows to display {@linkplain MappedElementRowViewModel} in a {@linkplain JList}
 */
@ExludeFromCodeCoverageGeneratedReport
@SuppressWarnings("serial")
public class MappedElementRowViewModelRenderer extends JPanel implements ListCellRenderer<MappedElementRowViewModel<? extends Thing, ?>> 
{
    /**
     * The current class {@linkplain Logger}
     */
    private Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain JLabel} for the Dst Element
     */
    private JLabel dstElement;

    /**
     * The {@linkplain JLabel} for the Hub Element
     */
    private JLabel hubElement;
    
    /**
     * The {@linkplain JPanel} that contains the {@linkplain #targetArchitectureComboBox}
     */
    private JPanel targetArchitecturePanel;
    
    /**
     * The {@linkplain JLabel} for the target architecture
     */
    private JLabel targetArchitectureLabel;
    
    /**
     * The {@linkplain JComboBox} to choose the target architecture for the represented mapping
     */
    private JComboBox<CapellaArchitecture> targetArchitectureComboBox;

    /**
     * The {@linkplain MappingDirection} that applies to this represented mapping
     */
    private final MappingDirection mappingDirection;

    /**
     * Initializes a new {@linkplain MappedElementRowViewModelRenderer}
     * 
     * @param mappingDirection the {@linkplain MappingDirection} that applies to this renderer
     */
    public MappedElementRowViewModelRenderer(MappingDirection mappingDirection) 
    {
        this.mappingDirection = mappingDirection;
        this.Initialize();
    }

    /**
     * Initializes this view components
     */
    private void Initialize()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {400, 0, 0, 30};
        gridBagLayout.rowHeights = new int[] {0};
        gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, 1.0};
        gridBagLayout.rowWeights = new double[]{1.0};
        setLayout(gridBagLayout);
        
        this.dstElement = new JLabel("");
        dstElement.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbc_dstElement = new GridBagConstraints();
        gbc_dstElement.fill = GridBagConstraints.BOTH;
        gbc_dstElement.insets = new Insets(5, 5, 0, 0);
        gbc_dstElement.gridx = mappingDirection == MappingDirection.FromDstToHub ? 0 : 3;
        gbc_dstElement.gridy = 0;
        add(this.dstElement, gbc_dstElement);
        
        JLabel lblNewLabel_1 = new JLabel("<html><body>&#x1F872;</body></html>");
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
        gbc_lblNewLabel_1.fill = GridBagConstraints.VERTICAL;
        gbc_lblNewLabel_1.insets = new Insets(5, 5, 0, 5);
        gbc_lblNewLabel_1.gridx = 1;
        gbc_lblNewLabel_1.gridy = 0;
        add(lblNewLabel_1, gbc_lblNewLabel_1);
        
        this.hubElement = new JLabel("");
        hubElement.setHorizontalAlignment(SwingConstants.RIGHT);
        GridBagConstraints gbc_hubElement = new GridBagConstraints();
        gbc_hubElement.insets = new Insets(5, 5, 0, 5);
        gbc_hubElement.fill = GridBagConstraints.BOTH;
        gbc_hubElement.gridx = mappingDirection == MappingDirection.FromDstToHub ? 3 : 0;
        gbc_hubElement.gridy = 0;
        this.add(this.hubElement, gbc_hubElement);
        
        if(this.mappingDirection == MappingDirection.FromDstToHub)
        {
            return;
        }
        
        this.targetArchitecturePanel = new JPanel();
        targetArchitecturePanel.setBackground(Color.WHITE);
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.insets = new Insets(0, 10, 0, 10);
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 2;
        gbc_panel.gridy = 0;
        this.add(this.targetArchitecturePanel, gbc_panel);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[] {0};
        gbl_panel.rowHeights = new int[] {0, 0};
        gbl_panel.columnWeights = new double[]{0.0};
        gbl_panel.rowWeights = new double[]{0.0, 0.0};
        this.targetArchitecturePanel.setLayout(gbl_panel);
        
        this.targetArchitectureLabel = new JLabel("Target Architecture");
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.insets = new Insets(0, 0, 10, 5);
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 0;
        this.targetArchitecturePanel.add(this.targetArchitectureLabel, gbc_lblNewLabel);
        
        this.targetArchitectureComboBox = new JComboBox<CapellaArchitecture>();
        targetArchitectureComboBox.setEditable(true);
        this.targetArchitectureComboBox.setModel(new DefaultComboBoxModel<CapellaArchitecture>(CapellaArchitecture.values()));
        this.targetArchitectureComboBox.setSelectedIndex(3);
        this.targetArchitectureComboBox.setToolTipText("Select the target Architecture");
        GridBagConstraints gbc_targetArchitecture = new GridBagConstraints();
        gbc_targetArchitecture.gridx = 0;
        gbc_targetArchitecture.gridy = 1;
        this.targetArchitecturePanel.add(this.targetArchitectureComboBox, gbc_targetArchitecture);
    }
    
    /**
     * Returns a component that has been configured to display the specified value. That component's paint method is then called to"render" the cell. 
     * If it is necessary to compute the dimensions of a list because the list cells do not have a fixed size, 
     * this method is called to generate a component on which getPreferredSizecan be invoked.
     * 
     * @param list The JList we're painting.
     * @param rowViewModel The value returned by list.getModel().getElementAt(index).
     * @param index The cells index.
     * @param isSelected True if the specified cell was selected.
     * @param cellHasFocus True if the specified cell has the focus.
     * @return A component whose paint() method will render the specified value.
     */
    @Override
    public Component getListCellRendererComponent(JList<? extends MappedElementRowViewModel<? extends Thing, ?>> list,
            MappedElementRowViewModel<? extends Thing, ?> rowViewModel, int index, boolean isSelected, boolean cellHasFocus)
    {
        this.dstElement.setText(rowViewModel.GetDstElementRepresentation());
        this.hubElement.setText(rowViewModel.GetHubElementRepresentation());
        
        this.UpdateRowStatus(rowViewModel);
        
        rowViewModel.GetShouldCreateNewTargetElement().subscribe(x -> this.UpdateLabelsAndStatus(rowViewModel));
        
        if(this.mappingDirection == MappingDirection.FromHubToDst && rowViewModel instanceof IHaveTargetArchitecture)
        {
            var targetArchitectureRowViewModel = (IHaveTargetArchitecture)rowViewModel;
            this.targetArchitectureComboBox.setSelectedItem(targetArchitectureRowViewModel.GetTargetArchitecture());
            
            targetArchitectureRowViewModel.GetTargetArchitectureObservable().subscribe(x -> this.targetArchitectureComboBox.setSelectedItem(x));
            this.targetArchitectureComboBox.addActionListener(e -> targetArchitectureRowViewModel.SetTargetArchitecture(this.targetArchitectureComboBox.getSelectedItem()));
        }
                
        rowViewModel.GetIsSelectedObservable().subscribe(x -> this.SetIsSelected(list, x));
                
        this.SetIsSelected(list, isSelected);
 
        return this;
    }

    /**
     * Updates the provided {@linkplain MappedElementRowViewModel} row status
     * 
     * @param rowViewModel the {@linkplain MappedElementRowViewModel} row view model
     */
    private void UpdateRowStatus(MappedElementRowViewModel<? extends Thing, ?> rowViewModel)
    {
        if(rowViewModel.GetRowStatus() != null)
        {
            switch(rowViewModel.GetRowStatus())
            {
                case ExisitingElement:
                    this.dstElement.setForeground(Color.decode("#17418f"));
                    this.hubElement.setForeground(Color.decode("#17418f"));
                    break;
                case ExistingMapping:
                    this.dstElement.setForeground(Color.decode("#a8601d"));
                    this.hubElement.setForeground(Color.decode("#a8601d"));
                    break;
                case NewElement:
                    this.dstElement.setForeground(Color.decode("#226b1e"));
                    this.hubElement.setForeground(Color.decode("#226b1e"));
                    break;
                default:
                    this.dstElement.setForeground(Color.BLACK);
                    this.hubElement.setForeground(Color.BLACK);
                    break;            
            }
        }
    }

    /**
     * Updates the labels and status of this row
     * 
     * @param the {@linkplain MappedElementRowViewModel}
     */
    private void UpdateLabelsAndStatus(MappedElementRowViewModel<? extends Thing, ?> rowViewModel)
    {
        SwingUtilities.invokeLater(() -> 
        {
            this.dstElement.setText(rowViewModel.GetDstElementRepresentation());
            this.hubElement.setText(rowViewModel.GetHubElementRepresentation());

            this.UpdateRowStatus(rowViewModel);
        });
    }

    /**
     * Updates the selection highlights colors of this row
     * 
     * @param list The JList we're painting.
     * @param isSelected True if the specified cell was selected.
     */
    private void SetIsSelected(JList<? extends MappedElementRowViewModel<? extends Thing, ?>> list, boolean isSelected)
    {
        if (isSelected) 
        {
            this.setBackground(list.getSelectionBackground());
            this.setForeground(list.getSelectionForeground());
        } 
        else 
        {
            this.setBackground(list.getBackground());
            this.setForeground(list.getForeground());
        }
    }
}
