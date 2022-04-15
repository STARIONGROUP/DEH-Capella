/*
 * MappedElementListViewCellRenderer.java
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
package Renderers;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ViewModels.Rows.MappedElementRowViewModel;

/**
 * The MappedElementListViewCellRenderer is
 */
public class MappedElementListViewCellRenderer extends JLabel implements TableCellRenderer
{
    /**
     * Initializes a new {@linkplain CapellaArchitectureCellRenderer}
     */
    public MappedElementListViewCellRenderer()
    {
        this.setOpaque(true);
        this.setBackground(Color.WHITE);
    }

    /**
     * Returns the component used for drawing the cell. This method is
     * used to configure the renderer appropriately before drawing.
     * 
     * @param table the {@linkplain JTable} instance
     * @param cellValue the value
     * @param isSelected a value indicating whether the current row is selected
     * @param hasFocus a value indicating whether the current row has focus
     * @param row the row number
     * @param columnIndex the column number
     * @return a {@linkplain JComboBox}
     */
    @SuppressWarnings("null")
    @Override
    public Component getTableCellRendererComponent(JTable table, Object cellValue, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex)
    {
        var row = table.getModel().getValueAt(rowIndex, 0);

        MappedElementRowViewModel<?, ?> rowViewModel;
        
        if(row instanceof MappedElementRowViewModel && (rowViewModel = (MappedElementRowViewModel<?, ?>)row) != null)
        {
            this.UpdateRowStatus(rowViewModel);   
        }
        
        if(isSelected)
        {
            this.setBackground(new Color(104, 143, 184));
        }
        else
        {
            this.setBackground(Color.WHITE);
        }
        
        this.setText(cellValue.toString());
        return this;
    }
    
    /**
     * Updates the provided {@linkplain MappedElementRowViewModel} row status
     * 
     * @param rowViewModel the {@linkplain MappedElementRowViewModel} row view model
     */
    private void UpdateRowStatus(MappedElementRowViewModel<?, ?> rowViewModel)
    {
        if(rowViewModel.GetRowStatus() != null)
        {
            switch(rowViewModel.GetRowStatus())
            {
                case ExisitingElement:
                    this.setForeground(Color.decode("#17418f"));
                    this.setForeground(Color.decode("#17418f"));
                    break;
                case ExistingMapping:
                    this.setForeground(Color.decode("#a8601d"));
                    this.setForeground(Color.decode("#a8601d"));
                    break;
                case NewElement:
                    this.setForeground(Color.decode("#226b1e"));
                    this.setForeground(Color.decode("#226b1e"));
                    break;
                default:
                    this.setForeground(Color.BLACK);
                    this.setForeground(Color.BLACK);
                    break;            
            }
        }
    }

}
