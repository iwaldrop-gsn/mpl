//
// Copyright (c) 2019 Grid Dynamics International, Inc. All Rights Reserved
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

import com.griddynamics.devops.mpl.MPLManager

/**
 * Add poststeps block to the list
 *
 * @author Sergei Parshev <sparshev@griddynamics.com>
 * @param body Definition of steps to execute
 * @see MPLManager#modulePostStep(String name, Closure body)
 */
def call(String name = null, Closure body) {
	MPLManager.instance.modulePostStep(name, body)
}
