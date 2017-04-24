/**
 *  RestMomentaryButton
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
	definition (name: "RestMomentaryButton", namespace: "leongenius", author: "Leon Zhou") {
		capability "Momentary"
	}

	preferences {
    	input("actionMethod", "enum", title: "Action method", description: "Rest method to operate the device", required: true, options: [
        	"POST": "POST"
        ])
        input("actionHost", "text", title: "IP/hostname:port", description: "Rest API host to operate the device", required: true)
        input("actionPath", "text", title: "Action URI", description: "Rest API path to operate the device", required: true)
    }
    
	simulator {
		// TODO: define status and reply messages here
	}

    tiles {
		standardTile("switch", "device.switch", width: 3, height: 4, canChangeIcon: true, decoration: "flat") {
			state "off", label: 'Push', action: "push", icon:"st.Electronics.electronics13", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'Push', action: "push", icon:"st.Electronics.electronics13", backgroundColor: "#53a7c0"
		}
		main "switch"
		details "switch"
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

}

// handle commands
def push() {
	log.debug "Executing 'push', actionMethod=$actionMethod, actionHost=$actionHost, actionPath=$actionPath";
    
    try {    	
        
        def pushAction = new physicalgraph.device.HubAction(
        	method: actionMethod,
            path: actionPath,
            headers: [
            	HOST: actionHost
            ]
        );
        sendEvent(name: "momentary", value: "pushed", isStateChange: true)
   		return sendHubCommand(pushAction);
    }
    catch (Exception e) {
        log.debug "Hit Exception $e on $pushAction"
    }
}