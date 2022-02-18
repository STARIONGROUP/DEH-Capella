/*
 * DstMappingConfigurationDialogViewModel.java
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
package ViewModels.Dialogs;

import static Utils.Operators.Operators.AreTheseEquals;
import static Utils.Stereotypes.StereotypeUtils.GetChildren;
import static Utils.Stereotypes.StereotypeUtils.GetShortName;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.capellacore.Structure;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.requirement.Requirement;
import org.polarsys.kitalpha.emde.model.Element;

import DstController.IDstController;
import Enumerations.MappedElementRowStatus;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Reactive.ObservableCollection;
import Reactive.ObservableValue;
import Utils.Ref;
import ViewModels.CapellaObjectBrowser.Interfaces.ICapellaObjectBrowserViewModel;
import ViewModels.CapellaObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.CapellaObjectBrowser.Rows.ElementRowViewModel;
import ViewModels.Dialogs.Interfaces.IDstMappingConfigurationDialogViewModel;
import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedRequirementRowViewModel;
import Views.Dialogs.DstMappingConfigurationDialog;
import cdp4common.commondata.NamedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import io.reactivex.Observable;

/**
 * The {@linkplain DstMappingConfigurationDialogViewModel} is the main view model for the {@linkplain DstMappingConfigurationDialog}
 */
public class DstMappingConfigurationDialogViewModel implements IDstMappingConfigurationDialogViewModel
{
    /**
     * This view model logger
     */
    private Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain IDstController}
     */
    private IDstController dstController;
    
    /**
     * The {@linkplain IHubController}
     */
    private IHubController hubController;

    /**
     * The {@linkplain Collection} of {@linkplain EObject} that were originally selected 
     */
    private Collection<EObject> originalSelection;
    
    /**
     * Backing field for {@linkplain GetElementDefinitionBrowserViewModel}
     */
    private IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel;
    
    /**
     * Gets the {@linkplain IElementDefinitionBrowserViewModel}
     * 
     * @return an {@linkplain IElementDefinitionBrowserViewModel}
     */
    @Override
    public IElementDefinitionBrowserViewModel GetElementDefinitionBrowserViewModel()
    {
        return this.elementDefinitionBrowserViewModel;
    }
    
    /**
     * Backing field for {@linkplain GetRequirementBrowserViewModel}
     */
    private IRequirementBrowserViewModel requirementBrowserViewModel;
    
    /**
     * Gets the {@linkplain IRequirementBrowserViewModel}
     * 
     * @return an {@linkplain IRequirementBrowserViewModel}
     */
    @Override
    public IRequirementBrowserViewModel GetRequirementBrowserViewModel()
    {
        return this.requirementBrowserViewModel;
    }

    /**
     * Backing field for {@linkplain GetMagicDrawObjectBrowserViewModel}
     */
    private ICapellaObjectBrowserViewModel capellaObjectBrowserViewModel;

    /**
     * Gets the {@linkplain IMagicDrawObjectBrowserViewModel}
     * 
     * @return an {@linkplain IMagicDrawObjectBrowserViewModel}
     */
    @Override
    public ICapellaObjectBrowserViewModel GetCapellaObjectBrowserViewModel()
    {
        return this.capellaObjectBrowserViewModel;
    }
    
    /**
     * The {@linkplain ObservableCollection} of {@linkplain MappedElementRowViewModel} that represents all the mapped elements
     */
    private ObservableCollection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> mappedElements = new ObservableCollection<>();
    
    /**
     * Gets the collection of mapped element
     * 
     * @return {@linkplain ObservableCollection} of {@linkplain MappedElementRowViewModel}
     */
    @Override
    public ObservableCollection<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> GetMappedElementCollection()
    {
        return this.mappedElements;
    }

    /**
     * Backing field for {@linkplain GetSelectedMappedElement}
     */
    private ObservableValue<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> selectedMappedElement = new ObservableValue<>(null);
    
    /**
     * The selected {@linkplain MappedElementRowViewModel}
     * 
     * @return a {@linkplain Observable} of {@linkplain MappedElementRowViewModel}
     */
    @Override
    public Observable<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> GetSelectedMappedElement()
    {
        return this.selectedMappedElement.Observable();
    }

    /**
     * Sets the selectedMappedElement
     * 
     * @param mappedElement the {@linkplain MappedElementRowViewModel} that is to be selected
     */
    @Override
    public void SetSelectedMappedElement(MappedElementRowViewModel<? extends Thing, ? extends CapellaElement> mappedElement)
    {
        mappedElement.SetIsSelected(true);
        this.selectedMappedElement.Value(mappedElement);
    }
    
    /**
     * Backing field for {@linkplain GetShouldMapToNewHubElementCheckBoxBeEnabled}
     */
    private ObservableValue<Boolean> shouldMapToNewHubElementCheckBoxBeEnabled = new ObservableValue<>(true, Boolean.class);

    /**
     * Gets an {@linkplain Observable} value indicating whether the mapToNewHubElementCheckBox should be enabled 
     * 
     * @return an {@linkplain Observable} of {@linkplain Boolean}
     */
    @Override
    public Observable<Boolean> GetShouldMapToNewHubElementCheckBoxBeEnabled()
    {
        return this.shouldMapToNewHubElementCheckBoxBeEnabled.Observable();
    }
    
    /**
     * Initializes a new {@linkplain DstMappingConfigurationDialogViewModel}
     * 
     * @param dstController the {@linkplain IDstController}
     * @param hubController the {@linkplain IHubController}
     * @param elementDefinitionBrowserViewModel the {@linkplain IElementDefinitionBrowserViewModel}
     * @param requirementBrowserViewModel the {@linkplain IRequirementBrowserViewModel}
     * @param magicDrawObjectBrowserViewModel the {@linkplain ICapellaObjectBrowserViewModel}
     */
    public DstMappingConfigurationDialogViewModel(IDstController dstController, IHubController hubController, 
            IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel, IRequirementBrowserViewModel requirementBrowserViewModel,
            ICapellaObjectBrowserViewModel magicDrawObjectBrowserViewModel)
    {
        this.elementDefinitionBrowserViewModel = elementDefinitionBrowserViewModel;
        this.requirementBrowserViewModel = requirementBrowserViewModel; 
        this.dstController = dstController;
        this.hubController = hubController;
        this.capellaObjectBrowserViewModel = magicDrawObjectBrowserViewModel;
        
        this.InitializeObservables();
    }
    
    /**
     * Initializes the {@linkplain Observable}s of this view model
     */
    private void InitializeObservables()
    {
        this.capellaObjectBrowserViewModel.GetSelectedElement()
            .subscribe(x -> this.UpdateMappedElements(x));
        
        this.elementDefinitionBrowserViewModel.GetSelectedElement()
            .subscribe(x -> this.SetHubElement(x.GetThing()));

        this.requirementBrowserViewModel.GetSelectedElement()
            .subscribe(x -> this.SetHubElement(x.GetThing()));
        
        this.selectedMappedElement.Observable().subscribe(
                x -> this.shouldMapToNewHubElementCheckBoxBeEnabled.Value(
                        x != null && x.GetRowStatus() != MappedElementRowStatus.ExistingMapping),
                x -> this.logger.catching(x));        
    }

    /**
     * Sets the Hub element on the selected element if the element is compatible
     * 
     * @param thing the {@linkplain Thing} to assign
     */
    private void SetHubElement(Thing thing)
    {
        if(this.selectedMappedElement.Value() == null || this.selectedMappedElement.Value().GetRowStatus() == MappedElementRowStatus.ExistingMapping)
        {
            return;
        }
        
        if(thing instanceof ElementDefinition
                && this.selectedMappedElement.Value().GetTThingClass().isAssignableFrom(ElementDefinition.class))
        {
            this.SetHubElement(thing, ElementDefinition.class);
        }
        else if(thing instanceof RequirementsSpecification
                && this.selectedMappedElement.Value().GetTThingClass().isAssignableFrom(RequirementsSpecification.class))
        {
            this.SetHubElement(thing, RequirementsSpecification.class);
        }
        else
        {
            this.logger.warn("Thing is not compatible with the current selected mapped element!");
        }
    }

    /**
     * Sets the Hub element on the selected element
     * 
     * @param thing the {@linkplain Thing} to assign
     * @param clazz the class of the {@linkplain Thing}
     */
    @SuppressWarnings("unchecked")
    private <TThing extends Thing & NamedThing> void SetHubElement(Thing thing, Class<TThing> clazz)
    {
        var mappedElementRowViewModel = (MappedElementRowViewModel<TThing, ? extends CapellaElement>)this.selectedMappedElement.Value();
        
        mappedElementRowViewModel.SetHubElement((TThing)thing.clone(true));
        
        this.shouldMapToNewHubElementCheckBoxBeEnabled.Value(false);
        mappedElementRowViewModel.SetShouldCreateNewTargetElement(false);
        
        this.UpdateRowStatus(this.selectedMappedElement.Value(), clazz);
    }

    /**
     * Updates the mapped element collection 
     * 
     * @param rowViewModel the {@linkplain IElementRowViewModel}
     */
    private void UpdateMappedElements(ElementRowViewModel<? extends CapellaElement> rowViewModel)
    {
        var optionalMappedElement = this.mappedElements.stream()
            .filter(x -> AreTheseEquals(x.GetDstElement().getId(), rowViewModel.GetElement().getId()))
            .findFirst();
        
        if(!optionalMappedElement.isPresent())
        {
            MappedElementRowViewModel<? extends Thing, ? extends CapellaElement> mappedElement;
            
            if(rowViewModel.GetElement() instanceof Component)
            {
                mappedElement = new MappedElementDefinitionRowViewModel((Component) rowViewModel.GetElement(), MappingDirection.FromDstToHub);
            }
            else
            {
                mappedElement = new MappedRequirementRowViewModel((Requirement) rowViewModel.GetElement(), MappingDirection.FromDstToHub);
            }

            this.mappedElements.add(mappedElement);
            this.SetSelectedMappedElement(mappedElement);
        }
        else
        {
            this.SetSelectedMappedElement(optionalMappedElement.get());
        }
    }

    /**
     * Updates this view model properties
     */
    private void UpdateProperties()
    {
        this.mappedElements.clear();
                
        for (MappedElementRowViewModel<? extends Thing, ? extends CapellaElement> mappedElementRowViewModel : this.dstController.GetDstMapResult())
        {
            this.mappedElements.add(mappedElementRowViewModel);
        }

        this.PreMap(this.originalSelection);
    }
    
    /**
     * Sets the mappedElement picked to open this dialog and sets the DST tree
     * 
     * @param selectedElements the collection of {@linkplain EObject}
     */
    @Override
    public void SetMappedElement(Collection<EObject> selectedElements)
    {
        this.originalSelection = selectedElements;
        this.capellaObjectBrowserViewModel.BuildTree(selectedElements);
        this.UpdateProperties();
    }
    
    /**
     * Pre-map the selected elements
     * 
     * @param selectedElement the collection of {@linkplain Element}
     */
    private void PreMap(Collection<EObject> selectedElements)
    {
        for (var element : selectedElements)
        {
            if(element instanceof Structure)
            {
                this.PreMap(GetChildren(element));
            }    
            else if (element instanceof CapellaElement)
            {
                var mappedElement = this.GetMappedElementRowViewModel((CapellaElement)element);
         
                if(mappedElement != null)
                {
                    this.mappedElements.add(mappedElement);
                }
            }
        }
    }

    /**
     * Get a {@linkplain MappedElementRowViewModel} that represents a pre-mapped {@linkplain Class}
     * 
     * @param capellaElement the {@linkplain Class} element
     * @return a {@linkplain MappedElementRowViewModel}
     */
    private MappedElementRowViewModel<? extends Thing, ? extends CapellaElement> GetMappedElementRowViewModel(CapellaElement capellaElement)
    {
        Ref<Boolean> refShouldCreateNewTargetElement = new Ref<>(Boolean.class, false);
        MappedElementRowViewModel<? extends Thing, ? extends CapellaElement> mappedElementRowViewModel = null;
        
        if(capellaElement instanceof Component)
        {
            Ref<ElementDefinition> refElementDefinition = new Ref<>(ElementDefinition.class);
            
            if(this.TryGetElementDefinition((Component)capellaElement, refElementDefinition, refShouldCreateNewTargetElement))
            {
                mappedElementRowViewModel = 
                        new MappedElementDefinitionRowViewModel(refElementDefinition.Get(), (Component)capellaElement, MappingDirection.FromDstToHub);
            }
        }
        else if(capellaElement instanceof Requirement)
        {
            Ref<RequirementsSpecification> refRequirementSpecification = new Ref<>(RequirementsSpecification.class);
            
            if(this.TryGetRequirementSpecification((Requirement)capellaElement, refRequirementSpecification, refShouldCreateNewTargetElement))
            {
                mappedElementRowViewModel = 
                        new MappedRequirementRowViewModel(refRequirementSpecification.Get(), (Requirement)capellaElement, MappingDirection.FromDstToHub);
            }
        }
        
        if(mappedElementRowViewModel != null)
        {
            mappedElementRowViewModel.SetShouldCreateNewTargetElement(refShouldCreateNewTargetElement.Get());
            mappedElementRowViewModel.SetRowStatus(Boolean.TRUE.equals(refShouldCreateNewTargetElement.Get()) ? MappedElementRowStatus.NewElement : MappedElementRowStatus.ExisitingElement);
            return mappedElementRowViewModel;
        }
        
        return null;
    }

    /**
     * Updates the {@linkplain MappedElementRowStatus} of the provided {@linkplain MappedElementRowViewModel}
     * 
     * @param mappedElementRowViewModel the {@linkplain MappedElementRowViewModel} of which to update the row status
     * @param clazz the {@linkplain java.lang.Class} of the {@linkplain Thing} represented in the {@linkplain MappedElementRowViewModel}
     */
    private <TThing extends Thing> void UpdateRowStatus(MappedElementRowViewModel<? extends Thing, ? extends CapellaElement> mappedElementRowViewModel, Class<TThing> clazz)
    {
        Ref<TThing> refThing = new Ref<>(clazz);
        mappedElementRowViewModel.SetRowStatus(MappedElementRowStatus.None);
        
        if(mappedElementRowViewModel.GetShouldCreateNewTargetElementValue())
        {
            mappedElementRowViewModel.SetRowStatus(MappedElementRowStatus.NewElement);
        }
        else if(mappedElementRowViewModel.GetHubElement() != null)
        {            
            if(this.dstController.GetDstMapResult().stream()
                    .filter(x -> x.GetHubElement().getClass() == clazz)
                    .anyMatch(x -> AreTheseEquals(x.GetHubElement().getIid(), mappedElementRowViewModel.GetHubElement().getIid())))
            {
                mappedElementRowViewModel.SetRowStatus(MappedElementRowStatus.ExistingMapping);
            }
            else if(this.hubController.TryGetThingById(mappedElementRowViewModel.GetHubElement().getIid(), refThing))
            {
                mappedElementRowViewModel.SetRowStatus(MappedElementRowStatus.ExisitingElement);
            }
        }
    }

    /**
     * Gets or create an {@linkplain ElementDefinition} that can be mapped to the provided {@linkplain Class},
     * In the case the provided {@linkplain Class} is already represented in the {@linkplain mappedElements} returns false
     * 
     * @param element the {@linkplain Component} element
     * @param refElementDefinition the {@linkplain Ref} of {@linkplain ElementDefinition}
     * @param refShouldCreateNewTargetElement the {@linkplain Ref} of {@linkplain Boolean} indicating whether the target Hub element will be created
     * @return a value indicating whether the method execution was successful in getting a {@linkplain ElementDefinition}
     */
    private boolean TryGetElementDefinition(Component element, Ref<ElementDefinition> refElementDefinition, Ref<Boolean> refShouldCreateNewTargetElement)
    {
        if(this.mappedElements.stream().noneMatch(x-> AreTheseEquals(x.GetDstElement().getId(), element.getId())))
        {
            Optional<ElementDefinition> optionalElementDefinition =
                    this.hubController.GetOpenIteration().getElement().stream()
                    .filter(x -> AreTheseEquals(x.getName(), element.getName())).findFirst();
            
            if(optionalElementDefinition.isPresent())
            {
                refElementDefinition.Set(optionalElementDefinition.get().clone(true));
            }
            else
            {
                ElementDefinition elementDefinition = new ElementDefinition();
                elementDefinition.setIid(UUID.randomUUID());
                elementDefinition.setName(element.getName());
                elementDefinition.setShortName(GetShortName(element));
                elementDefinition.setOwner(this.hubController.GetCurrentDomainOfExpertise());

                refElementDefinition.Set(elementDefinition);
                refShouldCreateNewTargetElement.Set(true);
            }
        }
        
        return refElementDefinition.HasValue();
    }

    /**
     * Gets or create an {@linkplain RequirementsSpecification} that can contained a {@linkplain Requirement} that will represent the provided {@linkplain Class},
     * In the case the provided {@linkplain Class} is already represented in the {@linkplain mappedElements} returns false
     * 
     * @param requirement the {@linkplain Class} element
     * @param refRequirementSpecification the {@linkplain Ref} of {@linkplain RequirementsSecification}
     * @param refShouldCreateNewTargetElement the {@linkplain Ref} of {@linkplain Boolean} indicating whether the target Hub element will be created
     * @return a value indicating whether the method execution was successful in getting a {@linkplain RequirementSpecification}
     */
    private boolean TryGetRequirementSpecification(Requirement requirement, Ref<RequirementsSpecification> refRequirementSpecification, Ref<Boolean> refShouldCreateNewTargetElement)
    {
        Optional<RequirementsSpecification> optionalRequirementsSpecification = 
              this.hubController.GetOpenIteration().getRequirementsSpecification().stream()
              .flatMap(x -> x.getRequirement().stream())
              .filter(x -> AreTheseEquals(x.getName(), requirement.getName()))
              .map(x -> x.getContainerOfType(RequirementsSpecification.class))
              .findFirst();

        if(optionalRequirementsSpecification.isPresent())
        {
            UUID optionalRequirementsSpecificationIid = optionalRequirementsSpecification.get().getIid();
            
            if(this.mappedElements.stream().anyMatch(x -> AreTheseEquals(x.GetHubElement().getIid(), optionalRequirementsSpecificationIid)
                    && AreTheseEquals(x.GetDstElement().getId(), requirement.getId())))
            {
                return false;
            }
            
            refRequirementSpecification.Set(optionalRequirementsSpecification.get().clone(true));
        }
        else
        {
            refShouldCreateNewTargetElement.Set(true);
            
            Ref<String> possibleParentName = new Ref<>(String.class);
            
            if(this.TryGetPossibleRequirementsSpecificationName(requirement, possibleParentName))
            {
                optionalRequirementsSpecification = this.hubController.GetOpenIteration().getRequirementsSpecification().stream()
                    .filter(x -> AreTheseEquals(possibleParentName, x.getName()))
                    .findFirst();
                
                if(optionalRequirementsSpecification.isPresent())
                {
                    refRequirementSpecification.Set(optionalRequirementsSpecification.get().clone(true));
                }
            }
            
            RequirementsSpecification requirementsSpecification = new RequirementsSpecification();
            requirementsSpecification.setName(possibleParentName.HasValue() ? possibleParentName.Get() : "new RequirementsSpecification");
            requirementsSpecification.setShortName(GetShortName(possibleParentName.HasValue() ? possibleParentName.Get() : requirementsSpecification.getName()));
            requirementsSpecification.setIid(UUID.randomUUID());
            requirementsSpecification.setOwner(this.hubController.GetCurrentDomainOfExpertise());
            refRequirementSpecification.Set(requirementsSpecification);            
        }

        return refRequirementSpecification.HasValue();
    }

    /**
     * Attempts to retrieve the parent of parent of the provided {@linkplain Class} element. 
     * Hence this is not always possible if the user decides to structure its SysML project differently.
     * However, this feature is only a nice to have.
     *  
     * @param requirement the {@linkplain Class} element to get the parent from
     * @return a value indicating whether the name of the parent was retrieved with success
     */
    private boolean TryGetPossibleRequirementsSpecificationName(Requirement requirement, Ref<String> possibleParentName)
    {
        try
        {
            possibleParentName.Set(((NamedElement)requirement.eContainer()).getName());
        }
        catch(Exception exception)
        {
            this.logger.catching(exception);
            return false;
        }
        
        return possibleParentName.HasValue();
    }

    /**
     * Resets the pre-mapped things to the default way 
     */
    @Override
    public void ResetPreMappedThings()
    {
        this.UpdateProperties();
    }
    
    /**
     * Occurs when the user sets the target element of the current mapped element to be a
     * 
     * @param selected the new {@linkplain boolean} value
     */
    @Override
    public void WhenMapToNewHubElementCheckBoxChanged(boolean selected)
    {
        if(this.selectedMappedElement.Value() == null)
        {
            return;
        }
        
        if(selected && !this.selectedMappedElement.Value().GetShouldCreateNewTargetElementValue())
        {
            this.selectedMappedElement.Value().SetHubElement(null);
        }
        
        if(selected != this.selectedMappedElement.Value().GetShouldCreateNewTargetElementValue())
        {
            this.selectedMappedElement.Value().SetShouldCreateNewTargetElement(selected);
        }
        
        if(this.selectedMappedElement.Value().GetHubElement() instanceof ElementDefinition)
        {
            this.UpdateRowStatus(this.selectedMappedElement.Value(), ElementDefinition.class);
        }
        else if(this.selectedMappedElement.Value().GetHubElement() instanceof RequirementsSpecification)
        {
            this.UpdateRowStatus(this.selectedMappedElement.Value(), RequirementsSpecification.class);
        }
    }
}
