/*
 * AppTestFixture.java
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

package App;

import static org.picocontainer.Characteristics.CACHE;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.picocontainer.Characteristics;

import HubController.IHubController;
import Services.MappingEngineService.IMappingEngineService;
import Services.MappingEngineService.MappingEngineService;

/**
 * The {@linkplain App} class is the main entry point for the DEH-Capella adapter
 */
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
        this.LogAdapterInitialization();
        this.RegisterDependencies();
        
        super.start(context);
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
        
        this.logger.info("----------------------------------------------------------------------------");
        this.logger.info(String.format("----------DEH-Capella adapter %sinitialized with success--------", versionString));
        this.logger.info("----------------------------------------------------------------------------");
    }

    /**
     * The stop method is executed when the plugin gets unloaded, 
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
            AppContainer.Container.addConfig(MappingEngineService.AssemblyParameterName, this.getClass().getPackage());
            AppContainer.Container.as(CACHE, Characteristics.USE_NAMES).addComponent(IMappingEngineService.class, MappingEngineService.class);
        }
        catch (Exception exception) 
        {
            this.logger.error(String.format("DEHCapella register dependencies has thrown an exception with %s %n %s", exception.toString(), exception.getStackTrace()));
            throw exception;
        }
    }
}
