/*
 * ICapellaMappedElementListViewViewModel.java
 *
 * Copyright (c) 2020-2024 Starion Group S.A.
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
package ViewModels.MappedElementListView.Interfaces;

import org.polarsys.kitalpha.emde.model.Element;

/**
 * The ICapellaMappedElementListViewViewModel is {@link ThemeReader} interface definition for {@linkplain CapellaMappedEementListViewViewModel}
 */
public interface ICapellaMappedElementListViewViewModel extends IMappedElementListViewViewModel<Element>
{
    /**
     * Gets a value indicating whether the target architecture column should be displayed
     * 
     * @return a {@linkplain boolean}
     */
    boolean GetShouldDisplayTargetArchitectureColumn();

    /**
     * Sets a value indicating whether the target architecture column should be displayed
     * 
     * @param value the {@linkplain boolean} value
     */
    void SetShouldDisplayTargetArchitectureColumn(boolean value);
}
