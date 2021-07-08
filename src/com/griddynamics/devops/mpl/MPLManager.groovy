//
// Copyright (c) 2018 Grid Dynamics International, Inc. All Rights Reserved
// https://www.griddynamics.com
//
// Classification level: Public
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// $Id: $
// @Project:     MPL
// @Description: Shared Jenkins Modular Pipeline Library
//

package com.griddynamics.devops.mpl

import com.cloudbees.groovy.cps.NonCPS

/**
 * Object to help with MPL pipelines configuration & post-steps
 *
 * @author Sergei Parshev <sparshev@griddynamics.com>
 * @author Ian Waldrop <iwaldrop@gsngames.com>
 */
@Singleton
class MPLManager implements Serializable {

	/** List of paths which is used to find modules in libraries */
	private List modulesLoadPaths = ['com/griddynamics/devops/mpl']

	/** Pipeline configuration */
	private Map config = [:]

	/** Post-step lists container */
	private Map postSteps = [:]

	/** Module post-step lists container */
	private Map modulePostSteps = [:]

	/** Post-steps errors store */
	private Map postStepsErrors = [:]

	/** List of modules available on project side while enforcement */
	private List enforcedModules

	/**
	 * Initialization for the MPL manager
	 *
	 * @param pipelineConfig Map with common configuration and specific modules configs
	 *
	 * @return MPLManager singleton object
	 */
	def init(pipelineConfig = null) {
		if (pipelineConfig in Map) this.config = pipelineConfig
		this
	}

	/**
	 * Get configuration
	 *
	 * @return Configuration map
	 */
	Map getGlobalConfig() { config.subMap(config.keySet() - 'modules') }

	/**
	 * Get agent label from the specific config option
	 *
	 * @return Agent label taken from the agent_label config property
	 */
	String getAgentLabel() { config.agent_label }

	/**
	 * Get a module configuration
	 * Module config is a pipeline config without modules section and with overridden values from the module itself.
	 *
	 * @param name module name
	 *
	 * @return Overridden configuration for the specified module
	 */
	Map moduleConfig(String name) {
		config.modules ? Helper.mergeMaps(globalConfig, (config.modules[name] ?: [:]) as Map) : config
	}

	/**
	 * Determine is module exists in the configuration or not
	 *
	 * @param name module name
	 *
	 * @return Boolean about existing the module
	 */
	Boolean moduleEnabled(String name) {
		config.modules ? config.modules[name] != null : false
	}

	/**
	 * Deep merge of the pipeline config with the provided config
	 *
	 * @param cfg Map
	 */
	def configMerge(Map cfg) {
		config = Helper.mergeMaps(config, cfg)
	}

	/**
	 * Add post step to the array with specific name
	 *
	 * @param name Post-steps list name
	 *              Usual post-steps list names:
	 *                * "always"  - used to run post-steps anyway (ex: decommission of the dynamic environment)
	 *                * "success" - post-steps to run on pipeline success (ex: email with congratulations or ask for promotion)
	 *                * "failure" - post-steps to run on pipeline failure (ex: pipeline failed message)
	 * @param body Definition of steps to include in the list
	 */
	void postStep(String name, Closure body) {
		// TODO: Parallel execution - could be dangerous
		if (!postSteps[name]) postSteps[name] = []
		def blocks = Helper.getMPLBlocks()
		postSteps[name] << [block: blocks ? blocks.first() : null, body: body]
	}

	/**
	 * Add module post step to the list
	 *
	 * @param name Module post-steps list name (default: current "module(id)")
	 * @param body Definition of steps to include in the list
	 */
	void modulePostStep(String name, Closure body) {
		if (name == null) {
			def block = Helper.getMPLBlocks().first()
			name = "${block.module}(${block.id})"
		}
		// TODO: Parallel execution - could be dangerous
		if (!modulePostSteps[name]) modulePostSteps[name] = []
		final blocks = Helper.getMPLBlocks()
		final block = blocks?.any() ?blocks.first() : [module: name, id: modulePostSteps[name].size()]
		modulePostSteps[name] << [block: block, body: body]
	}

	/**
	 * Execute post steps filled by modules in reverse order
	 *
	 * @param name post steps list name
	 */
	void postStepsRun(String name = 'always') {
		if (postSteps[name]) {
			final configuration = [CFG: globalConfig]
			for (def i = postSteps[name].size() - 1; i >= 0; i--) {
				try {
					Closure body = postSteps[name][i].body
					body.delegate = configuration
					body.resolveStrategy = Closure.DELEGATE_FIRST
					body.call()
				}
				catch (ex) {
					def module_name = "${postSteps[name][i].block?.module}(${postSteps[name][i].block?.id})"
					postStepError(name, module_name, ex)
				}
			}
		}
	}

	/**
	 * Execute module post steps filled by module in reverse order
	 *
	 * @param name Module post steps list name (default: current "module(id)")
	 */
	void modulePostStepsRun(String name = null) {
		if (name == null) {
			def block = Helper.getMPLBlocks().first()
			name = "${block.module}(${block.id})"
		}

		modulePostSteps[name]?.reverse()?.each {
			try { it.body() }
			catch (ex) {
				def module_name = "${it.block?.module}(${it.block?.id})"
				postStepError(name, module_name, ex)
			}
		}

		// finally, run any steps that are registered generally to the module
		// check and remove pattern to prevent unbounded recursion
		if (name ==~ /.*\(.*\)/) modulePostStepsRun(name - /\(.*\)/)
	}

	/**
	 * Post steps could end with errors - and it will be stored to get it later
	 *
	 * @param name Post-steps list name
	 * @param module Name of the module
	 * @param exception Exception object with error
	 */
	void postStepError(String name, String module, Exception exception) {
		if (!postStepsErrors[name]) postStepsErrors[name] = []
		postStepsErrors[name] << [module: module, error: exception]
	}

	/**
	 * Get the list of errors become while post-steps execution
	 *
	 * @param name Post-steps list name (default: current "module(id)")
	 *
	 * @return List of errors
	 */
	List getPostStepsErrors(String name = null) {
		if (name == null) {
			def block = Helper.getMPLBlocks().first()
			name = "${block.module}(${block.id})"
		}
		postStepsErrors[name] ?: []
	}


	/**
	 * Get the modules load paths in reverse order to make sure that defined last will be listed first
	 *
	 * @return List of paths
	 */
	List getModulesLoadPaths() {
		modulesLoadPaths.reverse()
	}

	/**
	 * Add path to the modules load paths list
	 *
	 * @param path string with resource path to the parent folder of modules
	 */
	void addModulesLoadPath(String path) {
		modulesLoadPaths += path
	}

	/**
	 * Enforce modules override on project side - could be set just once while execution
	 *
	 * @param modules List of modules available to be overridden on the project level
	 */
	void enforce(List modules) {
		// Execute function only once while initialization
		if (!enforcedModules) enforcedModules = modules
	}

	/**
	 * Check module in the enforced list
	 *
	 * @param module Module name
	 * @return Boolean module in the list, will always return true if not enforced
	 */
	Boolean checkEnforcedModule(String module) {
		enforcedModules?.contains(module)
	}

	/**
	 * Get list of currently executing modules
	 * Last item is the current one
	 *
	 * @return List of modules paths
	 *
	 * @deprecated - old function, now works with the current thread, but it's
	 *               better to switch to Helper.getMPLBlocks() - it gives more
	 *               info about the currently executed modules
	 */
	@Deprecated
	// https://github.com/griddynamics/mpl/issues/54
	List getActiveModules() {
		def blocks = Helper.getMPLBlocks()
		for (def i = 0; i < blocks.size(); i++)
			blocks[i] = blocks[i].module
		return blocks.reverse()
	}

	/**
	 * Add active module to the stack-list
	 *
	 * @param path Path to the module (including library if it's the library)
	 *
	 * @return String the created block id
	 */
	static String pushActiveModule(String path) {
		return Helper.startMPLBlock(path)
	}

	/**
	 * Removing the latest active module from the list
	 *
	 * @param start_id start node ID to find in the current execution
	 */
	static void popActiveModule(String start_id) {
		Helper.endMPLBlock(start_id)
	}

	/**
	 * Restore the static object state if the pipeline was interrupted
	 *
	 * This function helps to make sure the MPL object will be restored
	 * if jenkins was restarted during the pipeline execution. It will
	 * work if the MPL object is stored in the pipeline:
	 *
	 * var/MPLPipeline.groovy:
	 *   ...
	 *   def MPL = MPLPipelineConfig(body, [
	 *   ...
	 */
	@NonCPS
	private void readObject(ObjectInputStream inp) throws IOException, ClassNotFoundException {
		inp.defaultReadObject()
		instance = this
	}
}
