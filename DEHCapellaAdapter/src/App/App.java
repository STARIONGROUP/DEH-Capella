/*
 * AppTestFixture.java
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

package App;

import static org.picocontainer.Characteristics.CACHE;
import static org.picocontainer.Characteristics.NO_CACHE;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.FrameworkUtil;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.picocontainer.Characteristics;

import DstController.DstController;
import DstController.IDstController;
import HubController.IHubController;
import MappingRules.ComponentToElementMappingRule;
import MappingRules.RequirementToRequirementsSpecificationMappingRule;
import Services.CapellaLog.CapellaLogService;
import Services.CapellaLog.ICapellaLogService;
import Services.CapellaSelection.CapellaSelectionService;
import Services.CapellaSelection.ICapellaSelectionService;
import Services.CapellaSession.CapellaSessionListenerService;
import Services.CapellaSession.CapellaSessionService;
import Services.CapellaSession.ICapellaSessionListenerService;
import Services.CapellaSession.ICapellaSessionService;
import Services.CapellaSession.ISiriusSessionManagerWrapper;
import Services.CapellaSession.SiriusSessionManagerWrapper;
import Services.Mapping.IMapCommandService;
import Services.Mapping.MapCommandService;
import Services.MappingConfiguration.CapellaMappingConfigurationService;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Services.MappingEngineService.IMappingEngineService;
import Services.MappingEngineService.MappingEngineService;
import ViewModels.CapellaHubBrowserPanelViewModel;
import ViewModels.CapellaImpactViewPanelViewModel;
import ViewModels.CapellaImpactViewViewModel;
import ViewModels.ElementDefinitionImpactViewViewModel;
import ViewModels.HubBrowserPanelViewModel;
import ViewModels.RequirementImpactViewViewModel;
import ViewModels.TransferControlViewModel;
import ViewModels.CapellaObjectBrowser.CapellaObjectBrowserViewModel;
import ViewModels.CapellaObjectBrowser.Interfaces.ICapellaObjectBrowserViewModel;
import ViewModels.ContextMenu.HubBrowserContextMenuViewModel;
import ViewModels.Dialogs.DstMappingConfigurationDialogViewModel;
import ViewModels.Dialogs.Interfaces.IDstMappingConfigurationDialogViewModel;
import ViewModels.Interfaces.ICapellaHubBrowserPanelViewModel;
import ViewModels.Interfaces.ICapellaImpactViewPanelViewModel;
import ViewModels.Interfaces.ICapellaImpactViewViewModel;
import ViewModels.Interfaces.IElementDefinitionImpactViewViewModel;
import ViewModels.Interfaces.IHubBrowserContextMenuViewModel;
import ViewModels.Interfaces.IHubBrowserPanelViewModel;
import ViewModels.Interfaces.IRequirementImpactViewViewModel;
import ViewModels.Interfaces.ITransferControlViewModel;

/**
 * The {@linkplain App} class is the main entry point for the DEH-Capella adapter
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class App extends AbstractUIPlugin
{
    /**
     * The current class logger
     */
    private Logger logger = LogManager.getLogger();
    
    /**
     * Starts the DEH Capella Adapter, initializing the IoC container and logs the initialization
     * 
     * @param context the {@linkplain BundleContext}
     * @throws Exception might throw {@linkplain Exception}
     */
    @Override
    public void start(BundleContext context) throws Exception
    {
        this.RegisterDependencies();
        super.start(context);
        AppContainer.Container.getComponent(IMapCommandService.class).Initialize();
        this.LogAdapterInitialization();
    }
    
    /**
     * Logs the initialization of the adapter
     */
    private void LogAdapterInitialization()
    {
        var versionString = "";
        var bundle = FrameworkUtil.getBundle(this.getClass());
        
        if(bundle != null)
        {
            var version = bundle.getVersion();
            versionString = String.format("Version %d.%d.%d ", version.getMajor(), version.getMinor(), version.getMicro());
        }
        
        var message = String.format("DEH-Capella adapter %sinitialized with success", versionString);
        
        this.logger.info("----------------------------------------------------------------------------");
        this.logger.info(String.format("---------%s---------", message));
        this.logger.info("----------------------------------------------------------------------------");
        
        AppContainer.Container.getComponent(ICapellaLogService.class).Append(message);
    }

    /**
     * The stop method is executed when the plug-in gets unloaded, 
     * it stops the IoC container and gracefully closes open connections to the Hub
     * 
     * @param context the {@linkplain BundleContext}
     * @throws Exception might throw {@linkplain Exception}
     */
    @Override
    public void stop(BundleContext context) throws Exception
    {
        AppContainer.Container.getComponent(IHubController.class).Close();
        AppContainer.Container.stop();
        super.stop(context);
    }

    /**
     * Registers the dependencies of the DEH-Capella plugin into the {@linkplain AppContainer.Container}
     */
    public void RegisterDependencies()
    {
        try
        {
            AppContainer.Container.as(CACHE).addComponent(IDstController.class, DstController.class);
            AppContainer.Container.addConfig(MappingEngineService.AssemblyParameterName, ComponentToElementMappingRule.class.getPackage());
            AppContainer.Container.as(CACHE, Characteristics.USE_NAMES).addComponent(IMappingEngineService.class, MappingEngineService.class);
            AppContainer.Container.addComponent(ICapellaMappingConfigurationService.class, CapellaMappingConfigurationService.class);
            AppContainer.Container.as(CACHE).addComponent(ICapellaSessionService.class, CapellaSessionService.class);
            AppContainer.Container.addComponent(ICapellaSessionListenerService.class, CapellaSessionListenerService.class);
            AppContainer.Container.addConfig("platformLogger", Platform.getLog(FrameworkUtil.getBundle(CapellaLogService.class)));
            AppContainer.Container.as(Characteristics.USE_NAMES).addComponent(ICapellaLogService.class, CapellaLogService.class);
            AppContainer.Container.addComponent(ICapellaSelectionService.class, CapellaSelectionService.class);
            AppContainer.Container.as(CACHE).addComponent(IMapCommandService.class, MapCommandService.class);
            AppContainer.Container.addComponent(ISiriusSessionManagerWrapper.class, SiriusSessionManagerWrapper.class);

            AppContainer.Container.addComponent(ComponentToElementMappingRule.class.getName(), ComponentToElementMappingRule.class);
            AppContainer.Container.addComponent(RequirementToRequirementsSpecificationMappingRule.class.getName(), RequirementToRequirementsSpecificationMappingRule.class);
            
            AppContainer.Container.addComponent(IElementDefinitionImpactViewViewModel.class, ElementDefinitionImpactViewViewModel.class);
            AppContainer.Container.addComponent(IRequirementImpactViewViewModel.class, RequirementImpactViewViewModel.class);
            AppContainer.Container.addComponent(IHubBrowserPanelViewModel.class, HubBrowserPanelViewModel.class);
            AppContainer.Container.addComponent(ICapellaImpactViewPanelViewModel.class, CapellaImpactViewPanelViewModel.class);
            AppContainer.Container.addComponent(ITransferControlViewModel.class, TransferControlViewModel.class);
            AppContainer.Container.addComponent(ICapellaObjectBrowserViewModel.class, CapellaObjectBrowserViewModel.class);
            AppContainer.Container.addComponent(ICapellaImpactViewViewModel.class, CapellaImpactViewViewModel.class);
            AppContainer.Container.addComponent(IDstMappingConfigurationDialogViewModel.class, DstMappingConfigurationDialogViewModel.class);
            AppContainer.Container.as(NO_CACHE).addComponent(IHubBrowserContextMenuViewModel.class, HubBrowserContextMenuViewModel.class);           
            AppContainer.Container.addComponent(ICapellaHubBrowserPanelViewModel.class, CapellaHubBrowserPanelViewModel.class);           
        }
        catch (Exception exception) 
        {
            this.logger.error(String.format("DEHCapella register dependencies has thrown an exception with %s %n %s", exception.toString(), exception.getStackTrace()));
            throw exception;
        }
    }
}
