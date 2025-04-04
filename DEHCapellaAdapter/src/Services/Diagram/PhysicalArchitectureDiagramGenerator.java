/*
 * PhysicalArchitectureDiagramGenerator.java
 *
 * Copyright (c) 2020-2024 Starion Group S.A.
 *
 * Author: Sam Geren�, Alex Vorobiev, Nathanael Smiechowski 
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
package Services.Diagram;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.sirius.diagram.DDiagram;
import org.eclipse.sirius.diagram.DNodeContainer;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.sirius.analysis.DiagramServices;

import Services.CapellaLog.ICapellaLogService;
import Services.CapellaSession.ICapellaSessionService;
import Services.CapellaTransaction.ICapellaTransactionService;


/**
 * The {@linkplain PhysicalArchitectureDiagramGenerator} is the service that enables generating Physical Architecture diagrams from models
 */
public class PhysicalArchitectureDiagramGenerator implements IPhysicalArchitectureDiagramGenerator
{
    /**
     * The logger
     */
    private Logger logger = LogManager.getLogger(PhysicalArchitectureDiagramGenerator.class);
    
    /**
     * The {@linkplain ICapellaSessionService}
     */
    private ICapellaSessionService sessionService;

    /**
     * The {@linkplain ICapellaLogService}
     */
    private ICapellaLogService capellaLog;

    /**
     * The {@linkplain ICapellaTransactionService}
     */
    private ICapellaTransactionService transactionService;
    
    /**
     * Initializes a new {@linkplain PhysicalArchitectureDiagramGenerator}
     * 
     * @param sessionService The {@linkplain ISessionService}
     * @param capellaLog The {@linkplain ICapellaSessionService}
     * @param capellaLog The {@linkplain ICapellaTransactionService}
     */
    public PhysicalArchitectureDiagramGenerator(ICapellaSessionService sessionService, ICapellaLogService capellaLog, ICapellaTransactionService transactionService)
    {
        this.sessionService = sessionService;
        this.capellaLog = capellaLog;
        this.transactionService = transactionService;
    }
    
    /**
     * Generates or regenerates the Physical Architecture Diagram 
     */
    @Override
    public void Generate()
    {
        var views = this.sessionService.GetOpenSessions().iterator().next().getOwnedViews();
        
        var view = views.stream().filter(x -> "Physical Architecture".equals(x.getViewpoint().getName())).findFirst().orElse(null);
        
        var descriptor = view.getOwnedRepresentationDescriptors().stream().filter(x -> x.getName().contains("[PAB] Structure")).findFirst().orElse(null);
        
        if(descriptor == null)
        {
            this.capellaLog.Append("A Physical Architecture Blank Diagram needs to be created first, and its name should match \"[PAB] Structure\"");
            return;
        }
        
        this.transactionService.Commit(() -> 
        {
            var diagram = (DDiagram)descriptor.getRepresentation();
            var diagramServices = new DiagramServices();
            
            for (var element : diagram.getNodes())
            {
                diagramServices.removeAbstractDNodeView(element);
            }
            
            var component = this.sessionService.GetTopElement();
            
            var mapping = diagramServices.getContainerMapping(diagram, "PAB_PC");
            
            if(mapping != null )
            {
                var container = diagramServices.createContainer(mapping, component, diagram, diagram);
                container.setName(component.getName());
                container.setTarget(component);
                
                for(var part : component.getContainedParts())
                {
                    var partContainer = diagramServices.createContainer(mapping, part, container, diagram);
                    partContainer.setName(part.getName());
                    partContainer.setTarget(part);
                    this.GoThroughParts(part, partContainer, diagram, diagramServices);
                }
            }
            
            this.capellaLog.Append("Diagram [%s] updated", descriptor.getName());
        });
    }

    /**
     * Iterates through the parts of a deploy-able element, adding them to a container on a diagram.
     *
     * @param deployableElement The deploy-able element to iterate through.
     * @param container         The container to add the parts to.
     * @param diagram           The diagram where the container resides.
     * @param diagramServices   The services for interacting with the diagram.
     */
    private void GoThroughParts(Part deployableElement, DNodeContainer container, DDiagram diagram, DiagramServices diagramServices)
    {
        for (var links : deployableElement.getOwnedDeploymentLinks())
        {
            var mapping = diagramServices.getContainerMapping(diagram, "PAB_Deployment");
            
            if(mapping != null && links.getDeployedElement() instanceof Part)
            {
                var node = diagramServices.createContainer(mapping, links.getDeployedElement(), container, diagram);
                node.setName(links.getDeployedElement().getName());
                node.setTarget(links.getDeployedElement());
                this.GoThroughParts((Part)links.getDeployedElement(), node, diagram, diagramServices);
            }            
        }
    }
}
