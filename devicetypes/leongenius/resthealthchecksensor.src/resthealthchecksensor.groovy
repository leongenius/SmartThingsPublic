/**
 *  RestHealthCheckSensor
 *
 *  Copyright 2017 Leon Zhou
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "RestHealthCheckSensor", namespace: "leongenius", author: "Leon Zhou") {
		capability "Sensor"
	}

	preferences {
    	input("healthCheckMethod", "enum", title: "health check method", description: "Rest method to probe device health", required: true, options: [
        	"GET": "GET"
        ])
        input("healthCheckHost", "text", title: "IP/hostname:port", description: "Rest API host to probe device health", required: true)
        input("healthCheckPath", "text", title: "URI", description: "Rest API path to probe device health", required: true)
    }

	tiles {
        multiAttributeTile(name: "health", type: "generic", width: 6, height: 4) {
			tileAttribute("device.health", key: "PRIMARY_CONTROL") {
				attributeState "unknown", label: 'Unknown', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
				attributeState "healthy", label: 'Healthy', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc"
				attributeState "unhealthy", label: 'Unhealthy', icon: "st.doors.garage.garage-open", backgroundColor: "#e86d13"
			}
		}
        
        main "health"
        details "health"
	}
}

def updated() {
	state.health = "unknown";
    state.lastHealthyMs = null;
    state.lastDeviceUpdateMs = now();
    checkHealth();
    validateHealthExpiration();
}

def installed() {
	updated();
}

def checkHealth () {
    try {    	
        def action = new physicalgraph.device.HubAction(
        	[
            	path: healthCheckPath,
                method: healthCheckMethod,
                headers: [
                	HOST: healthCheckHost
                ]
            ],
            null,
            [
            	callback: healthCheckResponseHandler
            ]
        );

   		def result = sendHubCommand(action);
        log.debug "Probing result=${result}"
        
    } catch (e) {
        log.debug "Hit Exception $e"
    }
    runIn(60, checkHealth);
}

def markHealthy() {
    state.lastHealthyMs = now();
    log.debug("Mark device healthy")
    sendEvent([name: "health", value: "healthy"])
}

def validateHealthExpiration() {
	def lastHealthyMs = state.lastHealthyMs != null ? state.lastHealthyMs : state.lastDeviceUpdateMs;
	def healthyStatusValidSinceMs = now() - 125 * 1000;
	if (state.health != "unhealthy" && lastHealthyMs < healthyStatusValidSinceMs) {
    	log.debug("Mark device unhealthy")
		sendEvent([name: "health", value: "unhealthy"])
    } else {
    	log.debug("Device is still healthy");
    }
    runIn(30, validateHealthExpiration);
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	def msg = parseLanMessage(description)
    def status = msg.status
    log.debug "Device status=${status}"
    
}

def healthCheckResponseHandler(physicalgraph.device.HubResponse response) {
	log.debug "healthCheckResponse=${response}";
    markHealthy();
}